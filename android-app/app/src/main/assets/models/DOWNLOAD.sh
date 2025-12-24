#!/bin/bash
# Download FunctionGemma-270M model for the Android app
# Run this script from the project root directory

set -e

MODEL_URL="https://huggingface.co/JackJ1/functiongemma-270m-it-mobile-actions-litertlm/resolve/main/mobile-actions_q8_ekv1024.litertlm"
OUTPUT_DIR="android-app/app/src/main/assets/models"
OUTPUT_FILE="$OUTPUT_DIR/functiongemma_mobile_actions.litertlm"

echo "🔄 Downloading FunctionGemma-270M model..."
echo "Source: $MODEL_URL"
echo "Destination: $OUTPUT_FILE"
echo "Size: ~272 MB"
echo ""

# Create directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Download the model
if command -v wget &> /dev/null; then
    wget -O "$OUTPUT_FILE" "$MODEL_URL"
elif command -v curl &> /dev/null; then
    curl -L -o "$OUTPUT_FILE" "$MODEL_URL"
else
    echo "❌ Error: Neither wget nor curl is installed"
    echo "Please install wget or curl to download the model"
    exit 1
fi

echo ""
echo "✅ Model downloaded successfully!"
echo "File: $OUTPUT_FILE"
echo "Size: $(du -h "$OUTPUT_FILE" | cut -f1)"
echo ""
echo "You can now build the Android app with the model included."
