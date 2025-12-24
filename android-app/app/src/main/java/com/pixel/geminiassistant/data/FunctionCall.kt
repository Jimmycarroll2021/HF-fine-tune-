package com.pixel.geminiassistant.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

/**
 * Represents a function call that the AI wants to execute
 */
data class FunctionCall(
    @SerializedName("name")
    val name: String,

    @SerializedName("arguments")
    val arguments: Map<String, Any>
) {
    companion object {
        fun fromJson(json: String): FunctionCall? {
            return try {
                Gson().fromJson(json, FunctionCall::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

/**
 * Available functions for file management
 */
enum class FileFunction(val functionName: String, val description: String) {
    LIST_FILES(
        "list_files",
        "Lists all files and directories in a specified path"
    ),
    SEARCH_FILES(
        "search_files",
        "Searches for files matching a query in a specified directory"
    ),
    CREATE_FOLDER(
        "create_folder",
        "Creates a new folder at the specified path"
    ),
    DELETE_FILE(
        "delete_file",
        "Deletes a file or folder at the specified path"
    ),
    RENAME_FILE(
        "rename_file",
        "Renames a file or folder"
    ),
    MOVE_FILE(
        "move_file",
        "Moves a file or folder to a new location"
    ),
    COPY_FILE(
        "copy_file",
        "Copies a file or folder to a new location"
    ),
    GET_FILE_INFO(
        "get_file_info",
        "Gets detailed information about a file or folder"
    ),
    ORGANIZE_FILES(
        "organize_files",
        "Organizes files in a directory by type, date, or name"
    );

    companion object {
        fun fromName(name: String): FileFunction? {
            return values().find { it.functionName == name }
        }

        fun getAllFunctionsJson(): String {
            val functions = values().map { function ->
                mapOf(
                    "name" to function.functionName,
                    "description" to function.description,
                    "parameters" to getParametersForFunction(function)
                )
            }
            return Gson().toJson(functions)
        }

        private fun getParametersForFunction(function: FileFunction): Map<String, Any> {
            return when (function) {
                LIST_FILES -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Directory path to list files from"
                        )
                    ),
                    "required" to listOf("path")
                )
                SEARCH_FILES -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "query" to mapOf(
                            "type" to "string",
                            "description" to "Search query (file name or pattern)"
                        ),
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Directory path to search in"
                        )
                    ),
                    "required" to listOf("query", "path")
                )
                CREATE_FOLDER -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Full path for the new folder"
                        ),
                        "name" to mapOf(
                            "type" to "string",
                            "description" to "Name of the new folder"
                        )
                    ),
                    "required" to listOf("path", "name")
                )
                DELETE_FILE -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Full path of the file/folder to delete"
                        )
                    ),
                    "required" to listOf("path")
                )
                RENAME_FILE -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Current full path of the file/folder"
                        ),
                        "new_name" to mapOf(
                            "type" to "string",
                            "description" to "New name for the file/folder"
                        )
                    ),
                    "required" to listOf("path", "new_name")
                )
                MOVE_FILE -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "source_path" to mapOf(
                            "type" to "string",
                            "description" to "Current path of the file/folder"
                        ),
                        "destination_path" to mapOf(
                            "type" to "string",
                            "description" to "Destination path"
                        )
                    ),
                    "required" to listOf("source_path", "destination_path")
                )
                COPY_FILE -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "source_path" to mapOf(
                            "type" to "string",
                            "description" to "Current path of the file/folder"
                        ),
                        "destination_path" to mapOf(
                            "type" to "string",
                            "description" to "Destination path"
                        )
                    ),
                    "required" to listOf("source_path", "destination_path")
                )
                GET_FILE_INFO -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Full path of the file/folder"
                        )
                    ),
                    "required" to listOf("path")
                )
                ORGANIZE_FILES -> mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "Directory path to organize"
                        ),
                        "method" to mapOf(
                            "type" to "string",
                            "description" to "Organization method: 'type', 'date', or 'name'",
                            "enum" to listOf("type", "date", "name")
                        )
                    ),
                    "required" to listOf("path", "method")
                )
            }
        }
    }
}
