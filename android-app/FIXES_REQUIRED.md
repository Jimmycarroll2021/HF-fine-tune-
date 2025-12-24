# 🔧 Critical Fixes Required

This document provides **ready-to-use fixes** for the 3 critical blocker issues.

---

## 🚨 BLOCKER #1: Gemini Nano API Issue

### Current Problem:
The app uses the **cloud-based Gemini API** which won't work for on-device Gemini Nano.

### ⚠️ IMPORTANT DISCOVERY:
**Gemini Nano on-device API is currently in LIMITED PREVIEW** and not publicly available. The AICore API requires:
- Google AI Studio API key
- Pixel 8 Pro or Pixel 9 with Android 14+
- Enrollment in Google's AI preview program

### Solutions:

#### **Option A: Use Cloud-Based Gemini Pro (Recommended for Now)**

Replace in `app/build.gradle`:
```gradle
dependencies {
    // Use cloud-based Gemini Pro (works on all devices)
    implementation 'com.google.ai.client.generativeai:generativeai:0.2.0'
}
```

Update `GeminiAssistant.kt`:
```kotlin
companion object {
    // Use Gemini Pro (cloud-based) instead of Nano
    private const val MODEL_NAME = "gemini-pro"

    // Get API key from: https://ai.google.dev/
    // Store in local.properties: GEMINI_API_KEY=your_key_here
    private const val API_KEY = BuildConfig.GEMINI_API_KEY
}
```

**Pros**: Works immediately on all Android devices
**Cons**: Requires internet, uses API quota

---

#### **Option B: Simple Pattern Matching (No AI, Works Offline)**

Replace the entire AI processing with simple regex:

```kotlin
class SimpleAssistant(private val context: Context) {
    private val fileManager = FileManager(context)

    suspend fun processQuery(userQuery: String): Result<String> {
        val query = userQuery.lowercase()

        return try {
            when {
                // List files
                query.matches(Regex(".*list.*files.*|.*show.*files.*|.*browse.*")) -> {
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    listFiles(path)
                }

                // Search files
                query.matches(Regex(".*search.*|.*find.*")) -> {
                    val searchQuery = extractSearchQuery(query) ?: return Result.failure(
                        Exception("Please specify what to search for")
                    )
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    searchFiles(searchQuery, path)
                }

                // Create folder
                query.matches(Regex(".*create.*folder.*|.*make.*folder.*|.*new.*folder.*")) -> {
                    val name = extractFolderName(query) ?: return Result.failure(
                        Exception("Please specify folder name")
                    )
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    createFolder(path, name)
                }

                // Organize files
                query.matches(Regex(".*organize.*|.*sort.*")) -> {
                    val method = when {
                        query.contains("type") -> "type"
                        query.contains("date") -> "date"
                        query.contains("name") -> "name"
                        else -> "type"
                    }
                    val path = extractPath(query) ?: FileManager.DEFAULT_PATH
                    organizeFiles(path, method)
                }

                else -> Result.success(
                    "I can help you:\n" +
                    "• List files - 'List files in Downloads'\n" +
                    "• Search - 'Search for photos'\n" +
                    "• Create folders - 'Create folder Work'\n" +
                    "• Organize - 'Organize Downloads by type'"
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractPath(query: String): String? {
        // Map common names to paths
        return when {
            query.contains("download") -> "${Environment.getExternalStorageDirectory()}/Download"
            query.contains("picture") || query.contains("photo") ->
                "${Environment.getExternalStorageDirectory()}/Pictures"
            query.contains("dcim") || query.contains("camera") ->
                "${Environment.getExternalStorageDirectory()}/DCIM"
            query.contains("document") ->
                "${Environment.getExternalStorageDirectory()}/Documents"
            query.contains("music") ->
                "${Environment.getExternalStorageDirectory()}/Music"
            else -> null
        }
    }

    private fun extractSearchQuery(query: String): String? {
        // Extract text between quotes or after "for"
        val quotedMatch = Regex("\"([^\"]+)\"").find(query)
        if (quotedMatch != null) return quotedMatch.groupValues[1]

        val forMatch = Regex("for\\s+([\\w\\s]+?)(?:\\s+in|$)").find(query)
        if (forMatch != null) return forMatch.groupValues[1].trim()

        return null
    }

    private fun extractFolderName(query: String): String? {
        // Extract text between quotes or after "called/named"
        val quotedMatch = Regex("\"([^\"]+)\"").find(query)
        if (quotedMatch != null) return quotedMatch.groupValues[1]

        val calledMatch = Regex("(?:called|named)\\s+([\\w\\s]+?)(?:\\s+in|$)").find(query)
        if (calledMatch != null) return calledMatch.groupValues[1].trim()

        return null
    }

    // Implement actual operations
    private suspend fun listFiles(path: String): Result<String> {
        val result = fileManager.listFiles(path)
        return result.fold(
            onSuccess = { files ->
                Result.success(
                    "Found ${files.size} items:\n" +
                    files.take(20).joinToString("\n") {
                        "${if (it.isDirectory) "📁" else "📄"} ${it.name}"
                    } +
                    if (files.size > 20) "\n... and ${files.size - 20} more" else ""
                )
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun searchFiles(searchQuery: String, path: String): Result<String> {
        val result = fileManager.searchFiles(searchQuery, path)
        return result.fold(
            onSuccess = { files ->
                if (files.isEmpty()) {
                    Result.success("No files found matching '$searchQuery'")
                } else {
                    Result.success(
                        "Found ${files.size} matches:\n" +
                        files.take(15).joinToString("\n") {
                            "${if (it.isDirectory) "📁" else "📄"} ${it.name}\n   ${it.path}"
                        }
                    )
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    private suspend fun createFolder(path: String, name: String): Result<String> {
        return fileManager.createFolder(path, name)
    }

    private suspend fun organizeFiles(path: String, method: String): Result<String> {
        return fileManager.organizeFiles(path, method)
    }
}
```

**Pros**: Works offline, no API needed, lightweight
**Cons**: Less flexible than AI, requires specific command patterns

---

#### **Option C: Wait for Gemini Nano Public API**

Monitor these resources:
- https://ai.google.dev/gemini-api/docs
- https://developer.android.com/ml/aicore
- Google I/O announcements

---

## 🚨 BLOCKER #2: Fix FunctionDeclaration Syntax

**If using cloud Gemini Pro**, fix the function declaration syntax:

```kotlin
import com.google.ai.client.generativeai.type.*

private fun createFileFunctions(): List<FunctionDeclaration> {
    return listOf(
        FunctionDeclaration(
            name = "list_files",
            description = "Lists all files and directories in a specified path",
            parameters = listOf(
                Schema(
                    name = "path",
                    description = "Directory path to list files from",
                    type = FunctionType.STRING,
                    nullable = false
                )
            )
        ),
        FunctionDeclaration(
            name = "search_files",
            description = "Searches for files matching a query",
            parameters = listOf(
                Schema(
                    name = "query",
                    description = "Search query (file name or pattern)",
                    type = FunctionType.STRING,
                    nullable = false
                ),
                Schema(
                    name = "path",
                    description = "Directory path to search in",
                    type = FunctionType.STRING,
                    nullable = false
                )
            )
        )
        // Add other functions...
    )
}
```

**Note**: Check the actual Gemini SDK documentation as the API may have changed.

---

## 🚨 BLOCKER #3: Add App Icons

### Quick Fix Using Android Studio:

1. Right-click on `app` folder
2. Select **New → Image Asset**
3. Choose **Launcher Icons (Adaptive and Legacy)**
4. Use a default icon or upload your own
5. Click **Next** → **Finish**

### Manual Fix (if no Android Studio):

Create these directories and add placeholder icons:
```bash
android-app/app/src/main/res/
├── mipmap-mdpi/ic_launcher.png (48x48)
├── mipmap-hdpi/ic_launcher.png (72x72)
├── mipmap-xhdpi/ic_launcher.png (96x96)
├── mipmap-xxhdpi/ic_launcher.png (144x144)
└── mipmap-xxxhdpi/ic_launcher.png (192x192)
```

### Temporary Workaround (Just to Build):

Remove icon references from `AndroidManifest.xml`:

```xml
<application
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    <!-- Remove these lines temporarily: -->
    <!-- android:icon="@mipmap/ic_launcher" -->
    <!-- android:roundIcon="@mipmap/ic_launcher_round" -->
    android:label="@string/app_name"
    ...>
```

---

## 🔧 ADDITIONAL IMPORTANT FIXES

### Fix #4: Add onResume Permission Check

Add to `MainActivity.kt`:

```kotlin
override fun onResume() {
    super.onResume()

    // Check if permissions were granted while app was in background
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager() && messages.isEmpty()) {
            initializeAssistant()
        }
    }
}
```

---

### Fix #5: Add Path Validation (Security)

Update `FileManager.kt`:

```kotlin
companion object {
    const val DEFAULT_PATH = "/storage/emulated/0"

    // Allowed base paths
    private val ALLOWED_PATHS = listOf(
        "/storage/emulated/0",
        Environment.getExternalStorageDirectory().absolutePath,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath,
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath,
    )

    fun isPathAllowed(path: String): Boolean {
        val canonicalPath = File(path).canonicalPath
        return ALLOWED_PATHS.any { canonicalPath.startsWith(it) }
    }
}

// Use in all file operations:
suspend fun deleteFile(path: String): Result<String> = withContext(Dispatchers.IO) {
    try {
        if (!isPathAllowed(path)) {
            return@withContext Result.failure(
                SecurityException("Access to this path is not allowed")
            )
        }

        val file = File(path)
        // ... rest of code
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

### Fix #6: Add Delete Confirmation

Update `MainActivity.kt`:

```kotlin
private fun handleUserMessage(message: String) {
    if (message.isBlank()) return

    // Check if this is a delete operation
    if (message.lowercase().contains("delete")) {
        showDeleteConfirmation(message)
        return
    }

    // ... rest of existing code
}

private fun showDeleteConfirmation(message: String) {
    // You'll need to add a composable dialog for this
    // For now, just add a warning message
    messages.add(
        ChatMessage(
            text = "⚠️ Delete operations require confirmation. Please confirm by typing: 'yes, delete [filename]'",
            isUser = false
        )
    )
}
```

---

## 📦 BUILD.GRADLE FIX (Complete File)

Here's the corrected `app/build.gradle`:

```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}

android {
    namespace 'com.pixel.geminiassistant'
    compileSdk 34

    defaultConfig {
        applicationId "com.pixel.geminiassistant"
        minSdk 30
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"

        // Add this for API key (if using cloud Gemini)
        buildConfigField "String", "GEMINI_API_KEY", "\"${project.findProperty('GEMINI_API_KEY') ?: 'YOUR_API_KEY_HERE'}\""
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        viewBinding true
        compose true
        buildConfig true  // Add this
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
}

dependencies {
    // Core Android
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'

    // Jetpack Compose
    implementation platform('androidx.compose:compose-bom:2024.01.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.ui:ui-graphics'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.8.2'

    // Lifecycle
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'

    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

    // OPTION A: Cloud-based Gemini (works now)
    implementation 'com.google.ai.client.generativeai:generativeai:0.2.0'

    // OPTION B: For simple pattern matching (no AI)
    // No additional dependencies needed

    // JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'

    // Testing
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

---

## ✅ QUICK START GUIDE

### To Get App Building Immediately:

1. **Choose Option B** (Simple Pattern Matching - No AI)
   - Replace `GeminiAssistant.kt` with `SimpleAssistant.kt` code above
   - Update `MainActivity.kt` to use `SimpleAssistant` instead of `GeminiAssistant`

2. **Generate App Icons**
   - Use Android Studio Image Asset tool
   - Or temporarily remove icon references

3. **Build the App**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Test on Device**
   - Install on Pixel 9
   - Grant permissions
   - Test commands like "list files in downloads"

### To Use Cloud-Based Gemini:

1. Get API key from https://ai.google.dev/
2. Add to `local.properties`: `GEMINI_API_KEY=your_key_here`
3. Use Option A code from above
4. Requires internet connection

---

## 📞 RECOMMENDATION

**For MVP (Minimum Viable Product):**
- ✅ Use **Option B (Simple Pattern Matching)**
- ✅ Works offline, no API key needed
- ✅ Fast and reliable
- ✅ Can build and test immediately

**For Future Version:**
- 🔄 Upgrade to Gemini Nano when API is stable
- 🔄 Or use cloud Gemini Pro for more flexibility

---

**Next Steps:**
1. Choose which option (A, B, or C)
2. Apply the fixes above
3. Generate app icons
4. Build and test!
