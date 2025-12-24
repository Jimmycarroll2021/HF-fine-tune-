# Gemini File Assistant for Pixel 9

An intelligent file management assistant app for Google Pixel 9 that uses **Gemini Nano** (on-device AI) to help you browse, search, and organize files through natural language conversations.

## Features

### 🤖 AI-Powered File Management
- Natural language interface using Gemini Nano
- On-device processing (privacy-focused, no cloud required)
- Function calling for executing file operations

### 📁 File Operations
- **Browse**: List files and folders in any directory
- **Search**: Find files by name across your storage
- **Create**: Make new folders
- **Delete**: Remove files and folders
- **Rename**: Change file/folder names
- **Organize**: Auto-organize files by type, date, or name

### 🎯 Smart Features
- Quick access to common folders (Downloads, Pictures, Documents, etc.)
- File type detection with emojis (📁 folders, 📄 files)
- Conversational interface with chat history
- Error handling and user-friendly messages

## Requirements

- **Device**: Google Pixel 9 (or any Pixel with Gemini Nano support)
- **Android Version**: Android 14+ (API 30+)
- **Storage**: ~50MB for app
- **Permissions**: Storage access (managed automatically)

## Installation

### Option 1: Build from Source (Android Studio)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Jimmycarroll2021/HF-fine-tune-.git
   cd HF-fine-tune-/android-app
   ```

2. **Open in Android Studio**:
   - Launch Android Studio (2023.1 or newer)
   - Select "Open an Existing Project"
   - Navigate to the `android-app` folder
   - Wait for Gradle sync to complete

3. **Connect your Pixel 9**:
   - Enable Developer Options (Settings → About phone → tap Build number 7 times)
   - Enable USB Debugging (Settings → System → Developer options)
   - Connect via USB and authorize the computer

4. **Build and Run**:
   - Click the "Run" button (green play icon) in Android Studio
   - Select your Pixel 9 device
   - Wait for build and installation

### Option 2: Install Pre-built APK

1. Download the APK from the releases page
2. Transfer to your Pixel 9
3. Open the APK file
4. Tap "Install" (you may need to allow "Install from unknown sources")

## Setup

### First Launch

1. **Grant Permissions**:
   - The app will request storage permissions
   - Tap "Allow" for all storage-related permissions
   - On Android 11+, you'll need to grant "All files access" permission

2. **Enable Gemini Nano** (if not already enabled):
   - Go to Settings → Google → AICore
   - Ensure AICore is enabled
   - Download Gemini Nano model if prompted

3. **Start Using**:
   - The assistant will greet you with example commands
   - Start typing your requests!

## Usage Examples

### Basic Commands

**List files**:
- "List files in Downloads"
- "Show me what's in my Pictures folder"
- "Browse /storage/emulated/0/Documents"

**Search files**:
- "Search for 'vacation' in DCIM"
- "Find all PDF files in Documents"
- "Look for photos with 'birthday' in the name"

**Create folders**:
- "Create a new folder called Work in Documents"
- "Make a folder named Vacation2024 in Pictures"

**Organize files**:
- "Organize my Downloads folder by file type"
- "Sort files in Documents by date"
- "Organize Pictures by name"

**Delete files**:
- "Delete the file at /storage/emulated/0/Download/old_file.txt"
- "Remove the temporary folder"

**Rename files**:
- "Rename 'photo.jpg' to 'sunset.jpg'"

## Architecture

### Components

```
android-app/
├── app/src/main/java/com/pixel/geminiassistant/
│   ├── MainActivity.kt              # Main UI with Jetpack Compose
│   ├── data/
│   │   ├── FileItem.kt              # File data model
│   │   └── FunctionCall.kt          # AI function definitions
│   ├── utils/
│   │   ├── FileManager.kt           # File operations handler
│   │   └── GeminiAssistant.kt       # AI integration
│   └── ui/theme/                    # Material 3 theme
├── app/build.gradle                  # App dependencies
└── build.gradle                      # Project config
```

### Key Technologies

- **Gemini Nano**: On-device AI via Google's AICore
- **Jetpack Compose**: Modern declarative UI
- **Kotlin Coroutines**: Async file operations
- **Material 3**: Google's latest design system
- **Function Calling**: AI → Action execution

## How It Works

1. **User Input**: You type a natural language request
2. **AI Processing**: Gemini Nano understands intent and extracts parameters
3. **Function Calling**: AI determines which file function to call
4. **Execution**: FileManager performs the actual file operation
5. **Response**: Result is formatted and displayed

### Function Calling Flow

```
User: "Search for vacation photos in DCIM"
  ↓
Gemini Nano: Understands → search_files(query="vacation", path="/sdcard/DCIM")
  ↓
FileManager: Executes search recursively
  ↓
Result: "Found 23 matches: [list of files]"
```

## Permissions

The app requires the following permissions:

- **READ_EXTERNAL_STORAGE**: Read files (Android ≤ 12)
- **WRITE_EXTERNAL_STORAGE**: Modify files (Android ≤ 12)
- **READ_MEDIA_IMAGES/VIDEO/AUDIO**: Access media (Android 13+)
- **MANAGE_EXTERNAL_STORAGE**: Full file access (Android 11+)

All permissions are requested at runtime with clear explanations.

## Troubleshooting

### "Gemini Nano not available"
- **Solution**: Ensure you're on a Pixel device with AICore enabled
- Go to Settings → Google → AICore → Enable

### "Permission denied" errors
- **Solution**: Grant all storage permissions
- Settings → Apps → Gemini Assistant → Permissions → Allow all

### Files not showing up
- **Solution**: Check the path you're querying
- Use absolute paths like `/storage/emulated/0/Download`
- Or use folder names: "Downloads", "Pictures", etc.

### App crashes on startup
- **Solution**: Clear app data
- Settings → Apps → Gemini Assistant → Storage → Clear data
- Restart the app

## Privacy & Security

- ✅ **On-device processing**: All AI runs locally on Gemini Nano
- ✅ **No cloud uploads**: Your files never leave your device
- ✅ **No tracking**: No analytics or user data collection
- ✅ **Scoped storage**: Follows Android's security best practices
- ⚠️ **Confirmation required**: Destructive operations (delete) should be confirmed

## Limitations

- Gemini Nano only available on Pixel devices (8 Pro, 9, 9 Pro, etc.)
- Android 14+ required for full functionality
- Large file operations may take time
- Search limited to 5 levels deep (prevents hanging)

## Development

### Building for Development

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing)
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Customization

To modify available functions, edit:
- `data/FunctionCall.kt` - Add new function definitions
- `utils/FileManager.kt` - Implement new operations
- `utils/GeminiAssistant.kt` - Wire up AI → execution

## Future Enhancements

- [ ] File sharing integration
- [ ] Cloud storage support (Drive, Dropbox)
- [ ] Advanced search filters (size, date range)
- [ ] Batch operations
- [ ] File preview/thumbnails
- [ ] Voice input support
- [ ] Scheduled file organization
- [ ] Smart suggestions based on usage

## License

Apache 2.0 - See LICENSE file for details

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Test on a Pixel device
4. Submit a pull request

## Support

For issues and questions:
- Open an issue on GitHub
- Check existing issues for solutions

## Credits

- Built with ❤️ for Google Pixel devices
- Uses Google's Gemini Nano AI model
- Inspired by modern AI assistants and file managers

---

**Note**: This app is designed specifically for Google Pixel 9 and requires Gemini Nano. For other devices, consider using Gemini API (cloud) instead of the on-device model.
