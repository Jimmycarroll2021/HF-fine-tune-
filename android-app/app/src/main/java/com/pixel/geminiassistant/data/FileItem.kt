package com.pixel.geminiassistant.data

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents a file or directory in the file system
 */
data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String = "",
    val mimeType: String = ""
) {
    companion object {
        fun fromFile(file: File): FileItem {
            val extension = if (file.isFile) {
                file.extension.lowercase()
            } else ""

            return FileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0,
                lastModified = file.lastModified(),
                extension = extension,
                mimeType = getMimeType(extension)
            )
        }

        private fun getMimeType(extension: String): String {
            return when (extension) {
                "jpg", "jpeg", "png", "gif", "bmp", "webp" -> "image/*"
                "mp4", "avi", "mkv", "mov", "wmv" -> "video/*"
                "mp3", "wav", "flac", "aac", "ogg" -> "audio/*"
                "pdf" -> "application/pdf"
                "doc", "docx" -> "application/msword"
                "xls", "xlsx" -> "application/vnd.ms-excel"
                "txt", "log" -> "text/plain"
                "zip", "rar", "7z" -> "application/zip"
                else -> "*/*"
            }
        }
    }

    fun getFormattedSize(): String {
        if (isDirectory) return "--"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            size >= gb -> String.format("%.2f GB", size / gb)
            size >= mb -> String.format("%.2f MB", size / mb)
            size >= kb -> String.format("%.2f KB", size / kb)
            else -> "$size B"
        }
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(lastModified))
    }

    fun getFileType(): String {
        return when {
            isDirectory -> "Folder"
            extension.isEmpty() -> "File"
            else -> extension.uppercase()
        }
    }
}
