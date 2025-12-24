---
title: FunctionGemma File Assistant (Pixel 9 Demo)
emoji: 📱
colorFrom: blue
colorTo: green
sdk: gradio
sdk_version: 4.44.0
app_file: app.py
pinned: false
license: gemma
---

# 📱 FunctionGemma File Assistant - Pixel 9 Demo

**On-device AI file management** powered by Google's FunctionGemma-270M model!

## 🎯 What is this?

This is a web demo of the **Pixel 9 File Management Assistant** that uses FunctionGemma-270M for intelligent file operations through natural language.

The model was specifically fine-tuned for **function calling** - extracting structured commands from natural language queries.

## 🤖 Model

- **Name**: google/functiongemma-270m-it
- **Size**: 270M parameters
- **Specialization**: Function calling for mobile actions
- **Optimization**: Designed for on-device inference
- **Training**: Fine-tuned on tool-calling datasets

## 💡 Try These Commands

- "Show files in Downloads"
- "Search for vacation photos"
- "Create folder called Work"
- "Organize Downloads by file type"

## 🚀 Features

- ✅ **Natural language** file management
- ✅ **Function calling** - Model extracts structured commands
- ✅ **On-device AI** - No cloud APIs needed
- ✅ **Lightweight** - 270M params runs efficiently
- ✅ **Specialized** - Trained for mobile actions

## 📦 Demo Mode

This Space simulates file operations (no real file system access). The actual Android app runs on Pixel 9 with real file management capabilities.

## 🔗 Related

- **Model**: [google/functiongemma-270m-it](https://huggingface.co/google/functiongemma-270m-it)
- **Dataset**: [burtenshaw/function-gemma-tuning](https://huggingface.co/datasets/burtenshaw/function-gemma-tuning)
- **Framework**: LiteRT-LM (Google's edge AI framework)

## 🏗️ Architecture

```
User Query → FunctionGemma-270M → Function Call (JSON) → Execute → Result
```

The model receives natural language, outputs structured function calls, and the app executes them.

## 📱 Pixel 9 Integration

This demo runs the same model that will be deployed on Pixel 9 devices using:
- LiteRT-LM for on-device inference
- GPU/NPU acceleration
- 100% offline operation

---

**Built with ❤️ using FunctionGemma and Gradio**
