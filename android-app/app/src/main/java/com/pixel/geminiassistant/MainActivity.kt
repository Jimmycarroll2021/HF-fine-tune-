package com.pixel.geminiassistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pixel.geminiassistant.ui.theme.GeminiAssistantTheme
import com.pixel.geminiassistant.utils.LocalGemmaAssistant
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var geminiAssistant: LocalGemmaAssistant
    private val messages = mutableStateListOf<ChatMessage>()
    private var isProcessing = mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            initializeAssistant()
        } else {
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geminiAssistant = LocalGemmaAssistant(this)

        setContent {
            GeminiAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatScreen(
                        messages = messages,
                        isProcessing = isProcessing.value,
                        onSendMessage = { message ->
                            handleUserMessage(message)
                        }
                    )
                }
            }
        }

        requestPermissions()
    }

    private fun requestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            permissionLauncher.launch(permissions)
        } else {
            // Check for MANAGE_EXTERNAL_STORAGE on Android 11+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    requestManageStoragePermission()
                } else {
                    initializeAssistant()
                }
            } else {
                initializeAssistant()
            }
        }
    }

    private fun requestManageStoragePermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    }

    private fun initializeAssistant() {
        lifecycleScope.launch {
            try {
                geminiAssistant.initialize()
                messages.add(
                    ChatMessage(
                        text = "👋 Hi! I'm your local file assistant.\n\n" +
                               "I work 100% offline on your Pixel 9 - no internet needed!\n\n" +
                               "💡 Try:\n" +
                               "• 'Show files in Downloads'\n" +
                               "• 'Search for vacation photos'\n" +
                               "• 'Create folder Work'\n" +
                               "• 'Organize Downloads by type'\n\n" +
                               "Type 'help' anytime for more commands.",
                        isUser = false
                    )
                )
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        text = "⚠️ Error initializing assistant.\n\nError: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                )
            }
        }
    }

    private fun showPermissionDeniedMessage() {
        messages.add(
            ChatMessage(
                text = "⚠️ Storage permissions are required to manage files. Please grant permissions in Settings.",
                isUser = false,
                isError = true
            )
        )
    }

    private fun handleUserMessage(message: String) {
        if (message.isBlank()) return

        // Add user message
        messages.add(ChatMessage(text = message, isUser = true))
        isProcessing.value = true

        lifecycleScope.launch {
            try {
                val result = geminiAssistant.processQuery(message)
                result.fold(
                    onSuccess = { response ->
                        messages.add(ChatMessage(text = response, isUser = false))
                    },
                    onFailure = { error ->
                        messages.add(
                            ChatMessage(
                                text = "❌ Error: ${error.message}",
                                isUser = false,
                                isError = true
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                messages.add(
                    ChatMessage(
                        text = "❌ Error: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                )
            } finally {
                isProcessing.value = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        geminiAssistant.cleanup()
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    isProcessing: Boolean,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gemini File Assistant") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }

                if (isProcessing) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Processing...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Input field
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Ask me to manage files...") },
                        enabled = !isProcessing,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = !isProcessing && inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isProcessing)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = when {
                message.isError -> MaterialTheme.colorScheme.errorContainer
                message.isUser -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    message.isError -> MaterialTheme.colorScheme.onErrorContainer
                    message.isUser -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}
