# Gemini AI Assistant - Complete Project

This repository contains two main components:
1. **FunctionGemma Training Scripts** - Fine-tune models for tool-calling
2. **Android App for Pixel 9** - Production-ready AI file assistant

---

## 📱 Android App - Gemini File Assistant

**A complete Android application for Google Pixel 9 that uses Gemini Nano for AI-powered file management.**

### Quick Start (Android App)

1. **Open in Android Studio**:
   ```bash
   cd android-app
   # Open folder in Android Studio
   ```

2. **Build & Install**:
   - Connect your Pixel 9 via USB
   - Click "Run" in Android Studio
   - Grant storage permissions when prompted

3. **Start Using**:
   - Talk to the AI: "List files in Downloads"
   - "Search for vacation photos in DCIM"
   - "Organize my Downloads by type"

### Features

- 🤖 **Gemini Nano integration** - On-device AI (no cloud required)
- 📁 **File operations** - Browse, search, organize, create, delete, rename
- 💬 **Chat interface** - Natural language commands
- 🎯 **Smart organization** - Auto-organize by type, date, or name
- 🔒 **Privacy-focused** - All processing on-device

[Full Android App Documentation →](android-app/README.md)

---

## 🧠 FunctionGemma Training Scripts

**Fine-tune FunctionGemma models for tool-calling capabilities.**

### Overview

The project offers four training approaches:

1. **Coding Agent Integration**: Leverage Hugging Face Skills to command agents (Claude, Gemini, Codex) to launch training via HF Jobs

2. **Colab Notebooks**: Execute the notebook directly in Google Colab with a single click

3. **Hugging Face Jobs**: Run distributed training using `hf jobs uv run` on HF infrastructure with GPU acceleration (A10G flavor)

4. **Local Development**: Use the UV package manager to execute scripts locally with automatic dependency resolution

### Training Files

- `sft-tool-calling.py` - Main training script for FunctionGemma
- `trl_sft_nemotron_3_nano_30b_a3b_a100.py` - Nemotron model training
- `function_gemma_sft_tool_calling.ipynb` - Colab notebook
- `requirements.txt` - Python dependencies

### Quick Start (Training)

```bash
# Install dependencies
pip install -r requirements.txt

# Run training locally
python sft-tool-calling.py --max-steps 100 --push-to-hub

# Or use Hugging Face Jobs
hf jobs uv run sft-tool-calling.py --max-steps 100
```

### Key Technologies

- **Model**: google/functiongemma-270m-it
- **Training Framework**: TRL (Transformers Reinforcement Learning)
- **Optimization**: LoRA/QLoRA support
- **Package Manager**: UV for streamlined dependency handling
- **Infrastructure**: Hugging Face Jobs and Colab

---

## 📂 Repository Structure

```
HF-fine-tune-/
├── android-app/              # Complete Android application
│   ├── app/
│   │   └── src/main/java/com/pixel/geminiassistant/
│   │       ├── MainActivity.kt
│   │       ├── data/         # Data models
│   │       └── utils/        # AI & file management
│   ├── build.gradle
│   └── README.md            # Detailed app documentation
│
├── sft-tool-calling.py      # FunctionGemma training script
├── trl_sft_nemotron_3_nano_30b_a3b_a100.py
├── function_gemma_sft_tool_calling.ipynb
├── requirements.txt
└── README.md               # This file
```

---

## 🚀 Getting Started

### For App Users (Pixel 9)
1. Navigate to `android-app/`
2. Follow the [Android App README](android-app/README.md)
3. Build and install on your Pixel 9

### For ML Engineers (Training)
1. Install dependencies: `pip install -r requirements.txt`
2. Run training scripts or open Colab notebook
3. Fine-tune models for your use case

---

## 💡 Use Cases

### Android App
- Voice-free file management on Pixel 9
- Quick file search and organization
- Accessible interface for all users
- Privacy-focused (on-device AI)

### Training Scripts
- Fine-tune models for custom function calling
- Train AI assistants for mobile actions
- Multilingual tool-calling capabilities
- API integration training

---

## 🛠️ Tech Stack

**Android App:**
- Kotlin + Jetpack Compose
- Gemini Nano (AICore)
- Material 3 Design
- Coroutines for async operations

**Training:**
- Python + PyTorch
- Transformers + TRL
- LoRA/QLoRA optimization
- Hugging Face ecosystem

---

## 📋 Requirements

**Android App:**
- Google Pixel 9 (or Pixel with Gemini Nano)
- Android 14+ (API 30+)
- Storage permissions

**Training:**
- Python 3.10+
- CUDA GPU (for local training)
- Or Hugging Face Jobs account

---

## 🤝 Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Test thoroughly
4. Submit a pull request

---

## 📄 License

Apache 2.0 - See LICENSE file for details

---

## 🔗 Links

- [Android App Documentation](android-app/README.md)
- [HuggingFace Dataset](https://huggingface.co/datasets/burtenshaw/function-gemma-tuning)
- [Gemini Nano Documentation](https://ai.google.dev/gemini-api/docs/models/gemini-nano)
- [TRL Documentation](https://huggingface.co/docs/trl)

---

**Built with ❤️ for Google Pixel devices and AI enthusiasts**
