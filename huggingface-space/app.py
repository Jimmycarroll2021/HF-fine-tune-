import gradio as gr
import torch
from transformers import AutoModelForCausalLM, AutoTokenizer
import json
import os

# Load FunctionGemma model
MODEL_NAME = "google/functiongemma-270m-it"

print("🔄 Loading FunctionGemma-270M model...")
tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForCausalLM.from_pretrained(
    MODEL_NAME,
    torch_dtype=torch.float16,
    device_map="auto",
    trust_remote_code=True
)
print("✅ Model loaded!")

# Available functions for file management
AVAILABLE_FUNCTIONS = {
    "list_files": {
        "description": "List files in a directory",
        "parameters": {
            "path": {"type": "string", "description": "Directory path"}
        }
    },
    "search_files": {
        "description": "Search for files by name or extension",
        "parameters": {
            "query": {"type": "string", "description": "Search query"},
            "path": {"type": "string", "description": "Directory to search in"}
        }
    },
    "create_folder": {
        "description": "Create a new folder",
        "parameters": {
            "path": {"type": "string", "description": "Parent directory"},
            "name": {"type": "string", "description": "Folder name"}
        }
    },
    "organize_files": {
        "description": "Organize files by type, date, or name",
        "parameters": {
            "path": {"type": "string", "description": "Directory to organize"},
            "method": {"type": "string", "enum": ["type", "date", "name"]}
        }
    }
}

def create_function_prompt(user_query):
    """Create FunctionGemma prompt with available functions"""
    functions_json = json.dumps(AVAILABLE_FUNCTIONS, indent=2)

    prompt = f"""<start_of_turn>system
You are a file management assistant. You have access to the following functions:

{functions_json}

When the user asks to perform file operations, respond with a function call in JSON format.
<end_of_turn>
<start_of_turn>user
{user_query}
<end_of_turn>
<start_of_turn>model
"""
    return prompt

def execute_function(function_name, args):
    """Simulate function execution (in real app, would do actual file operations)"""

    if function_name == "list_files":
        path = args.get("path", "/storage/emulated/0/Download")
        return f"📂 **Files in {path}**\n\n📄 document.pdf\n📄 vacation.jpg\n📄 notes.txt\n📁 Projects\n\n(Demo mode - showing simulated files)"

    elif function_name == "search_files":
        query = args.get("query", "")
        path = args.get("path", "/storage/emulated/0")
        return f"🔍 **Search results for '{query}'**\n\n📄 {query}_2024.pdf\n📄 {query}_backup.txt\n\n(Demo mode - showing simulated results)"

    elif function_name == "create_folder":
        name = args.get("name", "New Folder")
        path = args.get("path", "/storage/emulated/0")
        return f"✅ Created folder '{name}' in {path}\n\n(Demo mode - simulated operation)"

    elif function_name == "organize_files":
        method = args.get("method", "type")
        path = args.get("path", "/storage/emulated/0/Download")
        return f"✅ Organized files in {path} by {method}\n\n📁 Documents/\n📁 Images/\n📁 Videos/\n\n(Demo mode - simulated organization)"

    return "Function executed (demo mode)"

def chat(message, history):
    """Process user message with FunctionGemma"""

    # Create prompt
    prompt = create_function_prompt(message)

    # Tokenize
    inputs = tokenizer(prompt, return_tensors="pt").to(model.device)

    # Generate
    with torch.no_grad():
        outputs = model.generate(
            **inputs,
            max_new_tokens=256,
            temperature=0.3,
            do_sample=True,
            top_p=0.95,
            pad_token_id=tokenizer.eos_token_id
        )

    # Decode response
    response = tokenizer.decode(outputs[0][inputs['input_ids'].shape[1]:], skip_special_tokens=True)

    # Try to parse function call from response
    try:
        # Look for JSON in response
        if "{" in response and "}" in response:
            start = response.find("{")
            end = response.rfind("}") + 1
            json_str = response[start:end]
            function_call = json.loads(json_str)

            function_name = function_call.get("function")
            args = function_call.get("args", {})

            if function_name in AVAILABLE_FUNCTIONS:
                result = execute_function(function_name, args)
                return f"🤖 **Function Call Detected**\n\n`{function_name}({json.dumps(args)})`\n\n---\n\n{result}"
    except:
        pass

    # If no valid function call, return raw response
    return f"🤖 {response}\n\n💡 Try asking me to:\n• List files in Downloads\n• Search for vacation photos\n• Create folder Work\n• Organize Downloads by type"

# Create Gradio interface
demo = gr.ChatInterface(
    chat,
    title="📱 FunctionGemma File Assistant (Pixel 9 Demo)",
    description="""
    **FunctionGemma-270M** running on-device AI for file management!

    🎯 **Try these commands:**
    - "Show files in Downloads"
    - "Search for vacation photos"
    - "Create folder Work"
    - "Organize Downloads by type"

    🤖 **Model**: google/functiongemma-270m-it (270M params)

    📱 **Demo Mode**: Simulating Pixel 9 file operations (no actual file system access)

    ⚡ **100% on-device AI** - No cloud APIs required!
    """,
    examples=[
        "List files in Downloads",
        "Search for vacation photos",
        "Create folder called Projects",
        "Organize Downloads by file type"
    ],
    theme=gr.themes.Soft(),
    retry_btn=None,
    undo_btn=None,
    clear_btn="Clear Chat"
)

if __name__ == "__main__":
    demo.launch()
