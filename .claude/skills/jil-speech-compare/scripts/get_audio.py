# Collects the audio for one person: you (--who me) or the speaker you copy (--who speaker).
# Sources can be YouTube links or files on your computer (video or audio). Everything goes in
# ~/JIL-decks/speech/<name>/audio/, and meta.json records where each file came from.
#
#   python scripts/get_audio.py --who speaker --name hina "https://youtu.be/..." "https://youtu.be/..."
#   python scripts/get_audio.py --who me --name me "C:/Users/me/Videos/talking.mp4"
#   --from 60 --to 900   keep only that part (seconds); only with a single source
import argparse, json, os, shutil, subprocess, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402  (sets up the data folder and UTF-8 output)


def run(cmd):
    p = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if p.returncode != 0:
        raise RuntimeError((p.stderr or p.stdout).strip().splitlines()[-1] if (p.stderr or p.stdout).strip() else "failed")
    return p.stdout


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--who", choices=["me", "speaker"], required=True)
    ap.add_argument("--name", required=True, help="a short name, e.g. me, hina")
    ap.add_argument("--from", dest="start", type=float)
    ap.add_argument("--to", dest="end", type=float)
    ap.add_argument("sources", nargs="+")
    a = ap.parse_args()
    if (a.start is not None or a.end is not None) and len(a.sources) > 1:
        sys.exit("--from/--to only work with one source at a time.")
    if not shutil.which("ffmpeg"):
        sys.exit("ffmpeg isn't installed. Run scripts/setup_check.py for how to fix it.")
    d = os.path.join(jp.DATA, "speech", a.name); ad = os.path.join(d, "audio"); os.makedirs(ad, exist_ok=True)
    mp = os.path.join(d, "meta.json")
    meta = json.load(open(mp, encoding="utf-8")) if os.path.exists(mp) else {"who": a.who, "sources": []}
    if meta.get("who") != a.who:
        sys.exit(f"'{a.name}' is already set up as {meta.get('who')}. Use another name.")
    for src in a.sources:
        n = len(meta["sources"]) + 1
        try:
            if src.startswith(("http://", "https://")):
                title = run(["yt-dlp", "--encoding", "utf-8", "--extractor-args", "youtube:lang=ja", "--skip-download",
                             "--print", "%(title)s", src]).strip().splitlines()[-1]
                raw = os.path.join(ad, f"{n:02d}.source")
                run(["yt-dlp", "--encoding", "utf-8", "-f", "bestaudio", "-o", raw + ".%(ext)s", src])
                got = [f for f in os.listdir(ad) if f.startswith(f"{n:02d}.source.")]
                if not got: raise RuntimeError("no audio came down")
                raw = os.path.join(ad, got[0])
            else:
                if not os.path.isfile(src): raise RuntimeError("no such file")
                title, raw = os.path.basename(src), src
            out = os.path.join(ad, f"{n:02d}.wav")
            cut = (["-ss", str(a.start)] if a.start is not None else []) + (["-to", str(a.end)] if a.end is not None else [])
            run(["ffmpeg", "-y", "-loglevel", "error", "-i", raw] + cut + ["-vn", "-ac", "1", "-ar", "16000", "-c:a", "pcm_s16le", out])
            if raw.startswith(ad): os.remove(raw)
            secs = float(run(["ffprobe", "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", out]).strip() or 0)
            meta["sources"].append({"file": os.path.basename(out), "source": src, "title": title,
                                    "from": a.start, "to": a.end, "minutes": round(secs / 60, 1)})
            print(f"{n:02d}.wav  {secs / 60:.1f} min  {title}")
        except Exception as e:
            print(f"FAILED {src}: {e}")
    json.dump(meta, open(mp, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    total = sum(s["minutes"] for s in meta["sources"])
    print(f"\n{a.name}: {len(meta['sources'])} file(s), {total:.1f} minutes in total -> {ad}")
    if a.who == "me" and total < 10:
        print("Tip: 20-40 minutes of you talking gives the comparison much more to work with.")
    if a.who == "speaker" and total < 30:
        print("Tip: aim for at least 30 minutes of them talking alone (3 or more solo videos).")


if __name__ == "__main__":
    main()
