# 🚀 Deploy to HuggingFace Spaces

This is your **FunctionGemma File Assistant** ready to deploy!

## Quick Deploy (Web UI)

1. **Go to HuggingFace**: https://huggingface.co/new-space

2. **Fill in details**:
   - **Space name**: `functiongemma-file-assistant`
   - **License**: `gemma`
   - **SDK**: `Gradio`
   - **Visibility**: Public or Private

3. **Upload files**:
   - `app.py`
   - `requirements.txt`
   - `README.md`

4. **Click "Create Space"**

5. **Wait for build** (2-3 minutes)

6. **Done!** Your app will be live at:
   `https://huggingface.co/spaces/YOUR_USERNAME/functiongemma-file-assistant`

---

## Deploy via Git (CLI)

```bash
# 1. Install huggingface_hub
pip install huggingface_hub

# 2. Login to HuggingFace
huggingface-cli login

# 3. Clone your new Space
git clone https://huggingface.co/spaces/YOUR_USERNAME/functiongemma-file-assistant
cd functiongemma-file-assistant

# 4. Copy files
cp ../huggingface-space/* .

# 5. Commit and push
git add .
git commit -m "Initial deployment of FunctionGemma File Assistant"
git push

# 6. Visit your Space!
```

---

## What Happens After Deploy?

1. **HuggingFace builds** your Space automatically
2. **Downloads** the FunctionGemma-270M model (~550MB)
3. **Loads** the model into memory
4. **Starts** the Gradio interface
5. **Your app is LIVE!** 🎉

---

## Space Hardware

**Free tier**: CPU (works, but slower)
**Upgrade**: T4 GPU (faster inference) - ~$0.60/hour

For demo purposes, CPU is fine. Model is small enough (270M params).

---

## Test Locally First (Optional)

```bash
cd huggingface-space
pip install -r requirements.txt
python app.py
```

Open http://localhost:7860 in your browser.

---

## Troubleshooting

**Build fails?**
- Check requirements.txt versions
- Make sure README.md has proper YAML frontmatter

**Model won't load?**
- FunctionGemma is gated - accept the license at:
  https://huggingface.co/google/functiongemma-270m-it

**Out of memory?**
- Reduce model precision (already using float16)
- Upgrade to GPU Space

---

## Next Steps

After deployment:
1. Test the chat interface
2. Try the example commands
3. Share your Space link!
4. (Optional) Integrate real file system access
5. (Optional) Add authentication

---

**Ready to deploy?** Upload those 3 files to HuggingFace Spaces and you're done! 🚀
