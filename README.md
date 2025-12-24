# FunctionGemma Tool-Calling SFT Demo

This repository demonstrates fine-tuning FunctionGemma for tool-calling capabilities across multiple platforms.

## Overview

The project offers four training approaches:

1. **Coding Agent Integration**: Leverage Hugging Face Skills to command agents (Claude, Gemini, Codex) to launch training via HF Jobs

2. **Colab Notebooks**: Execute the notebook directly in Google Colab with a single click

3. **Hugging Face Jobs**: Run distributed training using `hf jobs uv run` on HF infrastructure with GPU acceleration (A10G flavor)

4. **Local Development**: Use the UV package manager to execute scripts locally with automatic dependency resolution

## Key Technologies

- **Model**: google/functiongemma-270m-it
- **Training Framework**: TRL (Transformers Reinforcement Learning)
- **Optimization**: LoRA/QLoRA support
- **Package Manager**: UV for streamlined dependency handling
- **Infrastructure**: Hugging Face Jobs and Colab

## Quick Start

The project uses "a UV script (it has a `# /// script` dependency block)" to simplify execution. Install UV, authenticate with Hugging Face, then run the training script with custom parameters like `--max-steps` and `--push-to-hub`.

**License**: Apache 2.0
