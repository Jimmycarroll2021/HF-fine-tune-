package com.pixel.geminiassistant.utils

import android.content.Context
import android.os.Environment
import com.pixel.geminiassistant.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Handles all file system operations
 */
class FileManager(private val context: Context) {

    companion object {
        const val DEFAULT_PATH = "/storage/emulated/0"
    }

    /**
     * List all files in a directory
     */
    suspend fun listFiles(path: String = DEFAULT_PATH): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext Result.failure(Exception("Directory does not exist: $path"))
            }

            val files = directory.listFiles()?.map { file ->
                FileItem.fromFile(file)
            }?.sortedWith(
                compareBy<FileItem> { !it.isDirectory }
                    .thenBy { it.name.lowercase() }
            ) ?: emptyList()

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search for files matching a query
     */
    suspend fun searchFiles(query: String, path: String = DEFAULT_PATH): Result<List<FileItem>> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext Result.failure(Exception("Directory does not exist: $path"))
            }

            val results = mutableListOf<FileItem>()
            searchRecursive(directory, query.lowercase(), results)

            Result.success(results.sortedBy { it.name.lowercase() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun searchRecursive(directory: File, query: String, results: MutableList<FileItem>, maxDepth: Int = 5, currentDepth: Int = 0) {
        if (currentDepth >= maxDepth) return

        directory.listFiles()?.forEach { file ->
            if (file.name.lowercase().contains(query)) {
                results.add(FileItem.fromFile(file))
            }

            if (file.isDirectory && currentDepth < maxDepth) {
                searchRecursive(file, query, results, maxDepth, currentDepth + 1)
            }
        }
    }

    /**
     * Create a new folder
     */
    suspend fun createFolder(path: String, name: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val parentDir = File(path)
            if (!parentDir.exists() || !parentDir.isDirectory) {
                return@withContext Result.failure(Exception("Parent directory does not exist: $path"))
            }

            val newFolder = File(parentDir, name)
            if (newFolder.exists()) {
                return@withContext Result.failure(Exception("Folder already exists: ${newFolder.absolutePath}"))
            }

            val success = newFolder.mkdirs()
            if (success) {
                Result.success("Folder created: ${newFolder.absolutePath}")
            } else {
                Result.failure(Exception("Failed to create folder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a file or folder
     */
    suspend fun deleteFile(path: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: $path"))
            }

            val success = file.deleteRecursively()
            if (success) {
                Result.success("Deleted: ${file.name}")
            } else {
                Result.failure(Exception("Failed to delete: ${file.name}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Rename a file or folder
     */
    suspend fun renameFile(path: String, newName: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: $path"))
            }

            val newFile = File(file.parent, newName)
            if (newFile.exists()) {
                return@withContext Result.failure(Exception("A file with that name already exists"))
            }

            val success = file.renameTo(newFile)
            if (success) {
                Result.success("Renamed to: $newName")
            } else {
                Result.failure(Exception("Failed to rename file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Move a file or folder
     */
    suspend fun moveFile(sourcePath: String, destinationPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            val destination = File(destinationPath)

            if (!source.exists()) {
                return@withContext Result.failure(Exception("Source file does not exist"))
            }

            val destDir = if (destination.isDirectory) destination else destination.parentFile
            if (destDir == null || !destDir.exists()) {
                return@withContext Result.failure(Exception("Destination directory does not exist"))
            }

            val finalDest = if (destination.isDirectory) {
                File(destination, source.name)
            } else {
                destination
            }

            val success = source.renameTo(finalDest)
            if (success) {
                Result.success("Moved to: ${finalDest.absolutePath}")
            } else {
                Result.failure(Exception("Failed to move file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Copy a file or folder
     */
    suspend fun copyFile(sourcePath: String, destinationPath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            val destination = File(destinationPath)

            if (!source.exists()) {
                return@withContext Result.failure(Exception("Source file does not exist"))
            }

            val destDir = if (destination.isDirectory) destination else destination.parentFile
            if (destDir == null || !destDir.exists()) {
                return@withContext Result.failure(Exception("Destination directory does not exist"))
            }

            val finalDest = if (destination.isDirectory) {
                File(destination, source.name)
            } else {
                destination
            }

            if (source.isDirectory) {
                source.copyRecursively(finalDest, overwrite = false)
            } else {
                FileInputStream(source).use { input ->
                    FileOutputStream(finalDest).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Result.success("Copied to: ${finalDest.absolutePath}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get detailed information about a file
     */
    suspend fun getFileInfo(path: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: $path"))
            }

            Result.success(FileItem.fromFile(file))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Organize files by type, date, or name
     */
    suspend fun organizeFiles(path: String, method: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val directory = File(path)
            if (!directory.exists() || !directory.isDirectory) {
                return@withContext Result.failure(Exception("Directory does not exist: $path"))
            }

            val files = directory.listFiles()?.filter { it.isFile } ?: emptyList()

            when (method.lowercase()) {
                "type" -> organizeByType(directory, files)
                "date" -> organizeByDate(directory, files)
                "name" -> organizeByName(directory, files)
                else -> return@withContext Result.failure(Exception("Invalid organization method. Use: type, date, or name"))
            }

            Result.success("Files organized by $method")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun organizeByType(parentDir: File, files: List<File>) {
        files.forEach { file ->
            val extension = file.extension.ifEmpty { "other" }
            val typeFolder = File(parentDir, extension.uppercase())
            if (!typeFolder.exists()) {
                typeFolder.mkdir()
            }
            file.renameTo(File(typeFolder, file.name))
        }
    }

    private fun organizeByDate(parentDir: File, files: List<File>) {
        files.forEach { file ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date(file.lastModified()))
            val dateFolder = File(parentDir, date)
            if (!dateFolder.exists()) {
                dateFolder.mkdir()
            }
            file.renameTo(File(dateFolder, file.name))
        }
    }

    private fun organizeByName(parentDir: File, files: List<File>) {
        files.forEach { file ->
            val firstChar = file.name.first().uppercase()
            val letterFolder = File(parentDir, firstChar)
            if (!letterFolder.exists()) {
                letterFolder.mkdir()
            }
            file.renameTo(File(letterFolder, file.name))
        }
    }

    /**
     * Get common storage paths
     */
    fun getCommonPaths(): List<Pair<String, String>> {
        return listOf(
            "Internal Storage" to DEFAULT_PATH,
            "Downloads" to "${Environment.getExternalStorageDirectory()}/Download",
            "Pictures" to "${Environment.getExternalStorageDirectory()}/Pictures",
            "Documents" to "${Environment.getExternalStorageDirectory()}/Documents",
            "DCIM" to "${Environment.getExternalStorageDirectory()}/DCIM",
            "Music" to "${Environment.getExternalStorageDirectory()}/Music",
            "Movies" to "${Environment.getExternalStorageDirectory()}/Movies"
        )
    }
}
