package com.pixel.geminiassistant.utils

import android.content.Context
import android.os.Environment
import com.pixel.geminiassistant.data.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-device file assistant using pattern matching
 * 100% local, no internet or APIs required
 * Works instantly on Pixel 9
 */
class LocalGemmaAssistant(private val context: Context) {

    private val fileManager = FileManager(context)

    /**
     * Initialize the assistant (no model to load)
     */
    fun initialize() {
        // Pattern matching requires no initialization
        println("✅ Local file assistant ready (100% on-device)")
    }

    /**
     * Process user query using intelligent pattern matching
     */
    suspend fun processQuery(userQuery: String): Result<String> = withContext(Dispatchers.Default) {
        try {
            val query = userQuery.lowercase().trim()

            // Match user intent and execute appropriate function
            when {
                // LIST FILES
                isListCommand(query) -> {
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    listFiles(path)
                }

                // SEARCH FILES
                isSearchCommand(query) -> {
                    val searchQuery = extractSearchQuery(query)
                        ?: return@withContext Result.failure(
                            Exception("What should I search for? Try: 'search for vacation photos'")
                        )
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    searchFiles(searchQuery, path)
                }

                // CREATE FOLDER
                isCreateFolderCommand(query) -> {
                    val name = extractFolderName(query)
                        ?: return@withContext Result.failure(
                            Exception("What should I name the folder? Try: 'create folder Work'")
                        )
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    createFolder(path, name)
                }

                // DELETE FILE/FOLDER
                isDeleteCommand(query) -> {
                    val path = extractFullPath(query)
                        ?: return@withContext Result.failure(
                            Exception("Which file should I delete? Please provide the full path.")
                        )
                    deleteFile(path)
                }

                // RENAME FILE/FOLDER
                isRenameCommand(query) -> {
                    Result.success(
                        "⚠️ Rename feature coming soon!\n\n" +
                        "For now, you can:\n" +
                        "1. Create a new folder\n" +
                        "2. Copy files manually\n" +
                        "3. Delete the old one"
                    )
                }

                // ORGANIZE FILES
                isOrganizeCommand(query) -> {
                    val method = when {
                        query.contains("type") || query.contains("extension") -> "type"
                        query.contains("date") || query.contains("time") -> "date"
                        query.contains("name") || query.contains("alphabetical") -> "name"
                        else -> "type"
                    }
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    organizeFiles(path, method)
                }

                // GET FILE INFO
                isInfoCommand(query) -> {
                    val path = extractFullPath(query)
                        ?: return@withContext Result.failure(
                            Exception("Which file do you want info about?")
                        )
                    getFileInfo(path)
                }

                // HELP / UNKNOWN
                else -> showHelp()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== COMMAND DETECTION ==========

    private fun isListCommand(query: String): Boolean {
        return query.matches(Regex(".*(list|show|display|view|browse|see|open|what'?s in).*files?.*")) ||
               query.matches(Regex(".*(list|show|display|view|browse|see|what'?s in).*(folder|directory|download|picture|document|dcim).*"))
    }

    private fun isSearchCommand(query: String): Boolean {
        return query.matches(Regex(".*(search|find|look for|locate).*")) &&
               !query.contains("folder") && !query.contains("create")
    }

    private fun isCreateFolderCommand(query: String): Boolean {
        return query.matches(Regex(".*(create|make|new|add).*(folder|directory).*")) ||
               query.matches(Regex(".*(folder|directory).*(create|make|new).*"))
    }

    private fun isDeleteCommand(query: String): Boolean {
        return query.matches(Regex(".*(delete|remove|erase|trash).*"))
    }

    private fun isRenameCommand(query: String): Boolean {
        return query.matches(Regex(".*(rename|change name).*"))
    }

    private fun isOrganizeCommand(query: String): Boolean {
        return query.matches(Regex(".*(organize|sort|arrange|group|clean up).*"))
    }

    private fun isInfoCommand(query: String): Boolean {
        return query.matches(Regex(".*(info|information|details|properties|about|size of).*"))
    }

    // ========== PARAMETER EXTRACTION ==========

    private fun extractPath(query: String): String? {
        // Check for explicit paths
        val pathMatch = Regex("/storage/emulated/\\d+/[^\\s]+").find(query)
        if (pathMatch != null) return pathMatch.value

        // Map common folder names to paths
        val baseDir = Environment.getExternalStorageDirectory().absolutePath

        return when {
            query.contains("download") -> "$baseDir/Download"
            query.contains("picture") || query.contains("photo") || query.contains("image") ->
                "$baseDir/Pictures"
            query.contains("dcim") || query.contains("camera") ->
                "$baseDir/DCIM"
            query.contains("document") || query.contains("doc") ->
                "$baseDir/Documents"
            query.contains("music") || query.contains("audio") || query.contains("song") ->
                "$baseDir/Music"
            query.contains("movie") || query.contains("video") ->
                "$baseDir/Movies"
            query.contains("screenshot") ->
                "$baseDir/Pictures/Screenshots"
            else -> null
        }
    }

    private fun extractSearchQuery(query: String): String? {
        // Try to extract quoted text
        val quotedMatch = Regex("\"([^\"]+)\"").find(query)
        if (quotedMatch != null) return quotedMatch.groupValues[1]

        // Try to extract text after "for"
        val forMatch = Regex("(?:for|called|named)\\s+([\\w\\s]+?)(?:\\s+in|$)").find(query)
        if (forMatch != null) return forMatch.groupValues[1].trim()

        // Try to extract filename-like patterns
        val filenameMatch = Regex("\\b([a-z0-9_-]+\\.[a-z]{2,4})\\b", RegexOption.IGNORE_CASE).find(query)
        if (filenameMatch != null) return filenameMatch.value

        return null
    }

    private fun extractFolderName(query: String): String? {
        // Try to extract quoted text
        val quotedMatch = Regex("\"([^\"]+)\"").find(query)
        if (quotedMatch != null) return quotedMatch.groupValues[1]

        // Try to extract text after "called/named"
        val calledMatch = Regex("(?:called|named)\\s+([\\w\\s]+?)(?:\\s+in|$)").find(query)
        if (calledMatch != null) return calledMatch.groupValues[1].trim()

        // Try to extract capitalized words (likely folder names)
        val capitalMatch = Regex("\\b([A-Z][a-z]+(?:\\s+[A-Z][a-z]+)*)\\b").find(query)
        if (capitalMatch != null) return capitalMatch.value

        // Try to extract after "folder"
        val folderMatch = Regex("folder\\s+([\\w\\s]+?)(?:\\s+in|$)").find(query)
        if (folderMatch != null) return folderMatch.groupValues[1].trim()

        return null
    }

    private fun extractFullPath(query: String): String? {
        val pathMatch = Regex("/storage/emulated/\\d+/[^\\s]+").find(query)
        return pathMatch?.value
    }

    // ========== FILE OPERATIONS ==========

    private suspend fun listFiles(path: String): Result<String> {
        val result = fileManager.listFiles(path)
        return result.fold(
            onSuccess = { files ->
                if (files.isEmpty()) {
                    Result.success("📂 The folder is empty")
                } else {
                    val folderName = path.substringAfterLast('/')
                    Result.success(
                        "📂 **$folderName** (${files.size} items)\n\n" +
                        files.take(25).joinToString("\n") {
                            val icon = if (it.isDirectory) "📁" else getFileIcon(it.extension)
                            "$icon ${it.name}"
                        } +
                        if (files.size > 25) "\n\n... and ${files.size - 25} more items" else ""
                    )
                }
            },
            onFailure = {
                Result.failure(Exception("Could not access folder: ${it.message}"))
            }
        )
    }

    private suspend fun searchFiles(searchQuery: String, path: String): Result<String> {
        val result = fileManager.searchFiles(searchQuery, path)
        return result.fold(
            onSuccess = { files ->
                if (files.isEmpty()) {
                    Result.success("🔍 No files found matching '$searchQuery'")
                } else {
                    Result.success(
                        "🔍 **Found ${files.size} matches** for '$searchQuery'\n\n" +
                        files.take(20).joinToString("\n") {
                            val icon = if (it.isDirectory) "📁" else getFileIcon(it.extension)
                            "$icon ${it.name}\n   📍 ${shortenPath(it.path)}"
                        } +
                        if (files.size > 20) "\n\n... and ${files.size - 20} more matches" else ""
                    )
                }
            },
            onFailure = {
                Result.failure(Exception("Search failed: ${it.message}"))
            }
        )
    }

    private suspend fun createFolder(path: String, name: String): Result<String> {
        val result = fileManager.createFolder(path, name)
        return result.fold(
            onSuccess = { message ->
                Result.success("✅ $message")
            },
            onFailure = {
                Result.failure(Exception("Could not create folder: ${it.message}"))
            }
        )
    }

    private suspend fun deleteFile(path: String): Result<String> {
        // Safety check
        if (!isPathSafe(path)) {
            return Result.failure(
                SecurityException("Cannot delete files outside allowed directories")
            )
        }

        val result = fileManager.deleteFile(path)
        return result.fold(
            onSuccess = { message ->
                Result.success("🗑️ $message")
            },
            onFailure = {
                Result.failure(Exception("Could not delete: ${it.message}"))
            }
        )
    }

    private suspend fun organizeFiles(path: String, method: String): Result<String> {
        val result = fileManager.organizeFiles(path, method)
        return result.fold(
            onSuccess = { message ->
                Result.success("✅ $message\n\nFiles have been organized into separate folders.")
            },
            onFailure = {
                Result.failure(Exception("Could not organize files: ${it.message}"))
            }
        )
    }

    private suspend fun getFileInfo(path: String): Result<String> {
        val result = fileManager.getFileInfo(path)
        return result.fold(
            onSuccess = { file ->
                Result.success(
                    "📄 **${file.name}**\n\n" +
                    "Type: ${file.getFileType()}\n" +
                    "Size: ${file.getFormattedSize()}\n" +
                    "Modified: ${file.getFormattedDate()}\n" +
                    "Path: ${file.path}"
                )
            },
            onFailure = {
                Result.failure(Exception("Could not get file info: ${it.message}"))
            }
        )
    }

    private fun showHelp(): Result<String> {
        return Result.success(
            "💡 **I can help you with:**\n\n" +
            "📂 **Browse files**\n" +
            "   • 'Show files in Downloads'\n" +
            "   • 'List my pictures'\n" +
            "   • 'What's in DCIM'\n\n" +
            "🔍 **Search**\n" +
            "   • 'Search for vacation photos'\n" +
            "   • 'Find document.pdf'\n" +
            "   • 'Look for screenshots'\n\n" +
            "➕ **Create folders**\n" +
            "   • 'Create folder Work in Documents'\n" +
            "   • 'Make a new folder called Projects'\n\n" +
            "📊 **Organize**\n" +
            "   • 'Organize Downloads by type'\n" +
            "   • 'Sort Pictures by date'\n\n" +
            "**Working 100% offline on your Pixel 9! 🔒**"
        )
    }

    // ========== HELPER FUNCTIONS ==========

    private fun getFileIcon(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic" -> "🖼️"
            "mp4", "avi", "mkv", "mov", "wmv", "flv" -> "🎥"
            "mp3", "wav", "flac", "aac", "ogg", "m4a" -> "🎵"
            "pdf" -> "📕"
            "doc", "docx" -> "📘"
            "xls", "xlsx" -> "📗"
            "ppt", "pptx" -> "📙"
            "zip", "rar", "7z", "tar", "gz" -> "📦"
            "apk" -> "📱"
            "txt", "log" -> "📝"
            else -> "📄"
        }
    }

    private fun shortenPath(path: String): String {
        val home = "/storage/emulated/0"
        return if (path.startsWith(home)) {
            "~" + path.substring(home.length)
        } else {
            path
        }
    }

    private fun isPathSafe(path: String): Boolean {
        val allowedPaths = listOf(
            "/storage/emulated/0",
            Environment.getExternalStorageDirectory().absolutePath
        )

        return try {
            val canonical = java.io.File(path).canonicalPath
            allowedPaths.any { canonical.startsWith(it) }
        } catch (e: Exception) {
            false
        }
    }

    fun cleanup() {
        // Nothing to clean up for pattern matching
    }
}
