# Checks everything the speech comparison needs and says exactly what's missing. Safe to run any time.
# Usage: python scripts/setup_check.py
import importlib.util, os, shutil, sys
NEED = {"fugashi": "fugashi", "unidic_lite": "unidic-lite", "yt_dlp": "yt-dlp", "faster_whisper": "faster-whisper"}
missing = [pip for mod, pip in NEED.items() if importlib.util.find_spec(mod) is None]
print(f"Python {sys.version.split()[0]} at {sys.executable}")
if sys.version_info < (3, 9):
    print("Python 3.9 or newer is needed.")
if missing:
    print("Missing packages: " + ", ".join(missing))
    print("Install with:  " + f'"{sys.executable}" -m pip install ' + " ".join(missing))
ff = shutil.which("ffmpeg")
print("ffmpeg: " + ("found" if ff else "MISSING. Install it (Windows: winget install ffmpeg; Mac: brew install ffmpeg) and open a new terminal."))
if not missing:
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import jp
    print("Japanese tokeniser works: " + " | ".join(t.surface for t in jp.tagger("日本語で話しています")))
    print(f"Your speech folder: {os.path.join(jp.DATA, 'speech')}")
    try:
        import ctranslate2
        gpus = ctranslate2.get_cuda_device_count()
    except Exception:
        gpus = 0
    if gpus:
        print("NVIDIA GPU found: transcription uses the large-v3 model (fast).")
        if sys.platform == "win32":
            print("  If the GPU fails, install its libraries once: "
                  f'"{sys.executable}" -m pip install nvidia-cublas-cu12 nvidia-cudnn-cu12')
    else:
        print("No NVIDIA GPU: transcription runs on the CPU with the medium model. It works, it's just slower "
              "(roughly as long as the audio itself, sometimes longer).")
print("All set." if not missing and ff else "Fix the items above, then run this again.")
