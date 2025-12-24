package com.pixel.geminiassistant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.pixel.geminiassistant.data.FileItem
import com.pixel.geminiassistant.ui.theme.GeminiAssistantTheme
import com.pixel.geminiassistant.utils.FileManager
import com.pixel.geminiassistant.utils.LocalGemmaAssistant
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var geminiAssistant: LocalGemmaAssistant
    private lateinit var fileManager: FileManager
    private var permissionsGranted = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            permissionsGranted = true
            initializeApp()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geminiAssistant = LocalGemmaAssistant(this)
        fileManager = FileManager(this)

        setContent {
            GeminiAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!permissionsGranted) {
                        PermissionScreen { checkAndRequestPermissions() }
                    } else {
                        MainScreen(geminiAssistant, fileManager)
                    }
                }
            }
        }

        checkAndRequestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (!permissionsGranted && hasAllPermissions()) {
            permissionsGranted = true
            initializeApp()
        }
    }

    private fun checkAndRequestPermissions() {
        if (hasAllPermissions()) {
            permissionsGranted = true
            initializeApp()
        } else {
            requestPermissions()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO,
                    Manifest.permission.READ_MEDIA_AUDIO
                )
            )
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            geminiAssistant.initialize()
        }
    }
}

// ========== PERMISSION SCREEN ==========

@Composable
fun PermissionScreen(onRequestPermissions: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "File Access Required",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Gemini Assistant needs access to your files to help you manage them with AI.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onRequestPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Permissions", fontSize = 16.sp)
            }
        }
    }
}

// ========== MAIN SCREEN WITH BOTTOM NAVIGATION ==========

enum class Screen { FILES, AI_CHAT, SETTINGS }

@Composable
fun MainScreen(geminiAssistant: LocalGemmaAssistant, fileManager: FileManager) {
    var selectedScreen by remember { mutableStateOf(Screen.FILES) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(if (selectedScreen == Screen.FILES) Icons.Filled.Folder else Icons.Outlined.Folder, "Files") },
                    label = { Text("Files") },
                    selected = selectedScreen == Screen.FILES,
                    onClick = { selectedScreen = Screen.FILES }
                )
                NavigationBarItem(
                    icon = { Icon(if (selectedScreen == Screen.AI_CHAT) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome, "AI") },
                    label = { Text("AI Assistant") },
                    selected = selectedScreen == Screen.AI_CHAT,
                    onClick = { selectedScreen = Screen.AI_CHAT }
                )
                NavigationBarItem(
                    icon = { Icon(if (selectedScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings, "Settings") },
                    label = { Text("Settings") },
                    selected = selectedScreen == Screen.SETTINGS,
                    onClick = { selectedScreen = Screen.SETTINGS }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedScreen) {
                Screen.FILES -> FileBrowserScreen(fileManager)
                Screen.AI_CHAT -> AIChatScreen(geminiAssistant)
                Screen.SETTINGS -> SettingsScreen()
            }
        }
    }
}

// ========== FILE BROWSER (GALLERY STYLE) ==========

enum class ViewMode { GRID, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(fileManager: FileManager) {
    var currentPath by remember { mutableStateOf(FileManager.DEFAULT_PATH) }
    var files by remember { mutableStateOf<List<FileItem>>(emptyList()) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(currentPath) {
        isLoading = true
        fileManager.listFiles(currentPath).fold(
            onSuccess = { files = it },
            onFailure = { files = emptyList() }
        )
        isLoading = false
    }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isEmpty()) files
        else files.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            if (showSearch) {
                TopAppBar(
                    title = {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search files...") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { showSearch = false; searchQuery = "" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close")
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, "Clear")
                            }
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                currentPath.substringAfterLast('/'),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                currentPath.replace("/storage/emulated/0", "~"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        val canGoBack = currentPath != FileManager.DEFAULT_PATH &&
                                      File(currentPath).parent?.startsWith("/storage/emulated/0") == true
                        if (canGoBack) {
                            IconButton(onClick = { currentPath = File(currentPath).parent!! }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = true }) {
                            Icon(Icons.Filled.Search, "Search")
                        }
                        IconButton(onClick = { viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID }) {
                            Icon(if (viewMode == ViewMode.GRID) Icons.Filled.ViewList else Icons.Filled.GridView, "Toggle View")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                filteredFiles.isEmpty() -> EmptyState(searchQuery.isNotEmpty())
                viewMode == ViewMode.GRID -> GridView(filteredFiles) { if (it.isDirectory) currentPath = it.path }
                else -> ListView(filteredFiles) { if (it.isDirectory) currentPath = it.path }
            }
        }
    }
}

@Composable
fun GridView(files: List<FileItem>, onClick: (FileItem) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(files) { file ->
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

            Column(
                Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { isPressed = true; onClick(file) }
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (file.isDirectory) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getFileIcon(file),
                        null,
                        Modifier.size(40.dp),
                        tint = if (file.isDirectory) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(file.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                if (!file.isDirectory) {
                    Text(file.formattedSize, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ListView(files: List<FileItem>, onClick: (FileItem) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(files) { file ->
            Surface(
                onClick = { onClick(file) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 1.dp
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (file.isDirectory) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(getFileIcon(file), null, Modifier.size(24.dp), tint = if (file.isDirectory) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(file.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!file.isDirectory) {
                                Text(file.formattedSize, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(file.formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.MoreVert, "Options")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(isSearching: Boolean) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Icon(if (isSearching) Icons.Outlined.SearchOff else Icons.Outlined.FolderOpen, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Text(if (isSearching) "No files found" else "Folder is empty", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ========== AI CHAT SCREEN ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(geminiAssistant: LocalGemmaAssistant) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Assistant", fontWeight = FontWeight.Bold)
                        Text(geminiAssistant.getModelStatus().take(40), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item { WelcomeCard() }
                }
                items(messages) { ChatBubble(it) }
            }

            Surface(shadowElevation = 8.dp, tonalElevation = 2.dp) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        inputText,
                        { inputText = it },
                        Modifier.weight(1f),
                        placeholder = { Text("Ask me anything...") },
                        enabled = !isProcessing,
                        maxLines = 3,
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                messages = messages + ChatMessage(inputText, true)
                                val query = inputText
                                inputText = ""
                                isProcessing = true
                                scope.launch {
                                    val result = geminiAssistant.processQuery(query)
                                    messages = messages + ChatMessage(result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Error", false, result.isFailure)
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = inputText.isNotBlank() && !isProcessing,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape
                    ) {
                        if (isProcessing) CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        else Icon(Icons.AutoMirrored.Filled.Send, "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Icon(Icons.Filled.AutoAwesome, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(12.dp))
            Text("Hi! I'm your AI Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("I can help you manage files with natural language. Try:", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            listOf("Show files in Downloads", "Search for vacation photos", "Create folder Work", "Organize Downloads by type").forEach {
                Row(Modifier.padding(vertical = 4.dp)) {
                    Text("•  ", fontWeight = FontWeight.Bold)
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean, val isError: Boolean = false)

@Composable
fun ChatBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isError -> MaterialTheme.colorScheme.errorContainer
                    message.isUser -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (message.isUser) 16.dp else 4.dp, bottomEnd = if (message.isUser) 4.dp else 16.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(message.text, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ========== SETTINGS SCREEN ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { SettingItem(Icons.Outlined.Palette, "Theme", "System default") {} }
            item { SettingItem(Icons.Outlined.Storage, "Storage", "Manage app storage") {} }
            item { SettingItem(Icons.Outlined.Info, "About", "Version 1.0 - FunctionGemma 270M") {} }
        }
    }
}

@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick, Modifier.fillMaxWidth(), RoundedCornerShape(12.dp), tonalElevation = 1.dp) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
        }
    }
}

// ========== HELPERS ==========

fun getFileIcon(file: FileItem): ImageVector = when {
    file.isDirectory -> Icons.Filled.Folder
    file.extension in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> Icons.Filled.Image
    file.extension in listOf("mp4", "avi", "mkv", "mov") -> Icons.Filled.Movie
    file.extension in listOf("mp3", "wav", "flac", "m4a") -> Icons.Filled.AudioFile
    file.extension == "pdf" -> Icons.Filled.PictureAsPdf
    file.extension in listOf("doc", "docx", "txt") -> Icons.Filled.Description
    file.extension in listOf("zip", "rar", "7z") -> Icons.Filled.FolderZip
    else -> Icons.Filled.InsertDriveFile
}
