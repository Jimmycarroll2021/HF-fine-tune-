package com.pixel.geminiassistant.utils

import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.FunctionDeclaration
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.FunctionType
import com.google.ai.client.generativeai.type.generationConfig
import com.pixel.geminiassistant.data.FileFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages Gemini Nano AI assistant with function calling
 */
class GeminiAssistant(private val context: Context) {

    private var model: GenerativeModel? = null
    private val fileManager = FileManager(context)

    companion object {
        // For Gemini Nano on-device, you typically don't need an API key
        // as it uses the local AICore service on Pixel devices
        private const val MODEL_NAME = "gemini-nano"
    }

    /**
     * Initialize Gemini Nano with function calling capabilities
     */
    fun initialize() {
        try {
            // Define file management functions for the model
            val fileFunctions = createFileFunctions()

            val tools = listOf(Tool(fileFunctions))

            model = GenerativeModel(
                modelName = MODEL_NAME,
                // For on-device Gemini Nano, API key might not be needed
                // The device uses AICore service
                apiKey = "AIzaSyDummy", // Replace with actual setup if needed
                generationConfig = generationConfig {
                    temperature = 0.7f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 1024
                },
                tools = tools
            )
        } catch (e: Exception) {
            // Fallback if Gemini Nano is not available
            e.printStackTrace()
        }
    }

    /**
     * Create function declarations for file management
     */
    private fun createFileFunctions(): List<FunctionDeclaration> {
        return listOf(
            FunctionDeclaration(
                name = "list_files",
                description = "Lists all files and directories in a specified path",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Directory path to list files from"
                        )
                    ),
                    "required" to listOf("path")
                )
            ),
            FunctionDeclaration(
                name = "search_files",
                description = "Searches for files matching a query",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "query" to mapOf(
                            "type" to "STRING",
                            "description" to "Search query (file name or pattern)"
                        ),
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Directory path to search in"
                        )
                    ),
                    "required" to listOf("query", "path")
                )
            ),
            FunctionDeclaration(
                name = "create_folder",
                description = "Creates a new folder",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Parent directory path"
                        ),
                        "name" to mapOf(
                            "type" to "STRING",
                            "description" to "Name of the new folder"
                        )
                    ),
                    "required" to listOf("path", "name")
                )
            ),
            FunctionDeclaration(
                name = "delete_file",
                description = "Deletes a file or folder",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Full path of the file/folder to delete"
                        )
                    ),
                    "required" to listOf("path")
                )
            ),
            FunctionDeclaration(
                name = "rename_file",
                description = "Renames a file or folder",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Current full path"
                        ),
                        "new_name" to mapOf(
                            "type" to "STRING",
                            "description" to "New name"
                        )
                    ),
                    "required" to listOf("path", "new_name")
                )
            ),
            FunctionDeclaration(
                name = "organize_files",
                description = "Organizes files by type, date, or name",
                parameters = mapOf(
                    "type" to "OBJECT",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "STRING",
                            "description" to "Directory path"
                        ),
                        "method" to mapOf(
                            "type" to "STRING",
                            "description" to "Organization method",
                            "enum" to listOf("type", "date", "name")
                        )
                    ),
                    "required" to listOf("path", "method")
                )
            )
        )
    }

    /**
     * Process user query and execute functions
     */
    suspend fun processQuery(userQuery: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (model == null) {
                initialize()
            }

            if (model == null) {
                return@withContext Result.failure(Exception("Gemini Nano not available. Make sure you're running on a Pixel device with AICore enabled."))
            }

            // Add system context about available paths
            val paths = fileManager.getCommonPaths()
            val pathsInfo = paths.joinToString("\n") { "${it.first}: ${it.second}" }

            val systemPrompt = """
                You are a helpful file management assistant on an Android device.

                Available storage paths:
                $pathsInfo

                When users ask to perform file operations, use the appropriate function.
                Always confirm before deleting files.
                Be concise and helpful.
            """.trimIndent()

            val fullPrompt = "$systemPrompt\n\nUser: $userQuery"

            val response = model!!.generateContent(fullPrompt)

            // Check if response contains function calls
            val functionCalls = response.functionCalls

            if (functionCalls.isNullOrEmpty()) {
                // Direct text response
                Result.success(response.text ?: "I'm not sure how to help with that.")
            } else {
                // Execute function calls
                val results = mutableListOf<String>()

                for (functionCall in functionCalls) {
                    val result = executeFunctionCall(
                        functionCall.name,
                        functionCall.args
                    )
                    results.add(result)
                }

                Result.success(results.joinToString("\n"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Execute a function call from the AI
     */
    private suspend fun executeFunctionCall(
        functionName: String,
        arguments: Map<String, Any>
    ): String {
        return try {
            when (functionName) {
                "list_files" -> {
                    val path = arguments["path"] as? String ?: FileManager.DEFAULT_PATH
                    val result = fileManager.listFiles(path)
                    result.fold(
                        onSuccess = { files ->
                            "Found ${files.size} items:\n" +
                                    files.take(20).joinToString("\n") {
                                        "${if (it.isDirectory) "📁" else "📄"} ${it.name}"
                                    } +
                                    if (files.size > 20) "\n... and ${files.size - 20} more" else ""
                        },
                        onFailure = { "Error: ${it.message}" }
                    )
                }

                "search_files" -> {
                    val query = arguments["query"] as? String ?: ""
                    val path = arguments["path"] as? String ?: FileManager.DEFAULT_PATH
                    val result = fileManager.searchFiles(query, path)
                    result.fold(
                        onSuccess = { files ->
                            if (files.isEmpty()) {
                                "No files found matching '$query'"
                            } else {
                                "Found ${files.size} matches:\n" +
                                        files.take(15).joinToString("\n") {
                                            "${if (it.isDirectory) "📁" else "📄"} ${it.name}\n   ${it.path}"
                                        }
                            }
                        },
                        onFailure = { "Error: ${it.message}" }
                    )
                }

                "create_folder" -> {
                    val path = arguments["path"] as? String ?: FileManager.DEFAULT_PATH
                    val name = arguments["name"] as? String ?: "New Folder"
                    val result = fileManager.createFolder(path, name)
                    result.fold(
                        onSuccess = { it },
                        onFailure = { "Error: ${it.message}" }
                    )
                }

                "delete_file" -> {
                    val path = arguments["path"] as? String ?: ""
                    if (path.isEmpty()) {
                        "Error: Path is required"
                    } else {
                        val result = fileManager.deleteFile(path)
                        result.fold(
                            onSuccess = { it },
                            onFailure = { "Error: ${it.message}" }
                        )
                    }
                }

                "rename_file" -> {
                    val path = arguments["path"] as? String ?: ""
                    val newName = arguments["new_name"] as? String ?: ""
                    if (path.isEmpty() || newName.isEmpty()) {
                        "Error: Both path and new name are required"
                    } else {
                        val result = fileManager.renameFile(path, newName)
                        result.fold(
                            onSuccess = { it },
                            onFailure = { "Error: ${it.message}" }
                        )
                    }
                }

                "organize_files" -> {
                    val path = arguments["path"] as? String ?: FileManager.DEFAULT_PATH
                    val method = arguments["method"] as? String ?: "type"
                    val result = fileManager.organizeFiles(path, method)
                    result.fold(
                        onSuccess = { it },
                        onFailure = { "Error: ${it.message}" }
                    )
                }

                else -> "Unknown function: $functionName"
            }
        } catch (e: Exception) {
            "Error executing $functionName: ${e.message}"
        }
    }
}
