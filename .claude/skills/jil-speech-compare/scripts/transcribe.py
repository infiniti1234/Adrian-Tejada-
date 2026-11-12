# Transcribes everything in ~/JIL-decks/speech/<name>/audio/ with Whisper (faster-whisper), one file at a time,
# into speech/<name>/transcripts/NN.json (lines with start and end times) and NN.txt (readable, with times).
# Files already transcribed are skipped. GPU (NVIDIA) uses large-v3; CPU uses medium unless you pass --model.
#
#   python scripts/transcribe.py <name> [--model large-v3|medium|small] [--device auto|cuda|cpu]
import argparse, glob, json, os, site, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402


def register_cuda_dlls():
    """pip's nvidia-* wheels put cuBLAS/cuDNN under site-packages/nvidia/*/bin, which Windows doesn't search."""
    for sp in site.getsitepackages() + [site.getusersitepackages()]:
        for pkg in ("cublas", "cudnn", "cuda_nvrtc"):
            p = os.path.join(sp, "nvidia", pkg, "bin")
            if os.path.isdir(p):
                if hasattr(os, "add_dll_directory"): os.add_dll_directory(p)
                os.environ["PATH"] = p + os.pathsep + os.environ["PATH"]


def load_model(want, device):
    from faster_whisper import WhisperModel
    order = {"auto": ["cuda", "cpu"], "cuda": ["cuda"], "cpu": ["cpu"]}[device]
    for dev in order:
        name = want or ("large-v3" if dev == "cuda" else "medium")
        try:
            kw = dict(device="cuda", compute_type="int8_float16") if dev == "cuda" else \
                 dict(device="cpu", compute_type="int8", cpu_threads=max(1, (os.cpu_count() or 4) - 1))
            m = WhisperModel(name, **kw)
            import numpy as np
            list(m.transcribe(np.zeros(16000, dtype=np.float32), language="ja", beam_size=1, without_timestamps=True)[0])
            print(f"Whisper {name} on {dev}", flush=True)
            return m, dev, name
        except Exception as e:
            print(f"{dev} unavailable ({type(e).__name__}: {str(e)[:120]})", flush=True)
    sys.exit("Couldn't load Whisper. Run scripts/setup_check.py.")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("name")
    ap.add_argument("--model")
    ap.add_argument("--device", default="auto", choices=["auto", "cuda", "cpu"])
    a = ap.parse_args()
    d = os.path.join(jp.DATA, "speech", a.name)
    files = sorted(glob.glob(os.path.join(d, "audio", "*.wav")))
    if not files: sys.exit(f"No audio in {os.path.join(d, 'audio')}. Run scripts/get_audio.py first.")
    td = os.path.join(d, "transcripts"); os.makedirs(td, exist_ok=True)
    todo = [f for f in files if not os.path.exists(os.path.join(td, os.path.basename(f)[:-4] + ".json"))]
    if not todo:
        print("Everything is already transcribed."); return
    if a.device != "cpu": register_cuda_dlls()
    model, dev, mname = load_model(a.model, a.device)
    for f in todo:
        stem = os.path.basename(f)[:-4]; t0 = time.time()
        segs, info = model.transcribe(f, language="ja", beam_size=5, vad_filter=True,
                                      vad_parameters=dict(min_silence_duration_ms=400), condition_on_previous_text=False)
        lines = [{"start": round(s.start, 2), "end": round(s.end, 2), "text": s.text.strip()} for s in segs if s.text.strip()]
        json.dump({"file": os.path.basename(f), "model": mname, "device": dev, "duration": round(info.duration, 1), "lines": lines},
                  open(os.path.join(td, stem + ".json"), "w", encoding="utf-8"), ensure_ascii=False, indent=0)
        with open(os.path.join(td, stem + ".txt"), "w", encoding="utf-8") as fh:
            for l in lines: fh.write(f"[{stem} {int(l['start'] // 60)}:{int(l['start'] % 60):02d}] {l['text']}\n")
        print(f"{stem}: {len(lines)} lines, {info.duration / 60:.1f} min of audio in {(time.time() - t0) / 60:.1f} min", flush=True)
    print(f"Done -> {td}")


if __name__ == "__main__":
    main()
