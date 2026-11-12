# Gets a video's Japanese transcript into ~/JIL-decks/decks/<name>/transcript.json: [{"t": seconds, "text": line}] plus title and url.
# Source: a YouTube URL (Japanese subtitles: the creator's own first, auto-generated if that's all there is), or a local
# .srt / .vtt / .txt file (one line per caption; "[mm:ss]" or "[h:mm:ss]" timestamps optional).
# Usage: python scripts/get_transcript.py <url-or-file> [--name my-deck-name]
import json, os, re, sys, tempfile, glob
HERE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(HERE, "scripts"))
import jp  # noqa: E402  (sets utf-8 output)

TS = re.compile(r"(\d+):(\d\d):(\d\d)[.,](\d+)|(\d+):(\d\d)[.,](\d+)")

def secs(m):
    if m.group(1) is not None:
        return int(m.group(1)) * 3600 + int(m.group(2)) * 60 + int(m.group(3))
    return int(m.group(5)) * 60 + int(m.group(6))

def parse_caption_file(path):
    raw = open(path, encoding="utf-8-sig").read()
    lines, t = [], 0
    if path.lower().endswith(".txt"):
        for ln in raw.splitlines():
            m = re.match(r"\s*\[(?:(\d+):)?(\d+):(\d\d)\]\s*(.*)", ln)
            if m:
                t = int(m.group(1) or 0) * 3600 + int(m.group(2)) * 60 + int(m.group(3)); ln = m.group(4)
            if ln.strip(): lines.append({"t": t, "text": ln.strip()})
        return lines
    for block in re.split(r"\n\s*\n", raw):
        m = TS.search(block)
        if not m: continue
        text = []
        for ln in block.splitlines():
            if "-->" in ln or re.fullmatch(r"\d+", ln.strip()) or ln.startswith(("WEBVTT", "Kind:", "Language:", "NOTE")): continue
            ln = re.sub(r"<[^>]+>", "", ln).strip()
            if ln: text.append(ln)
        if text: lines.append({"t": secs(m), "text": " ".join(text)})
    # auto-captions repeat each line as it scrolls: keep a line only when it isn't the start of the next one
    out = []
    for i, x in enumerate(lines):
        if out and (x["text"] == out[-1]["text"] or out[-1]["text"].endswith(x["text"])): continue
        if out and x["text"].startswith(out[-1]["text"]): out[-1] = x; continue
        out.append(x)
    return out

def from_youtube(url):
    import yt_dlp
    tmp = tempfile.mkdtemp()
    info = yt_dlp.YoutubeDL({"quiet": True, "no_warnings": True, "skip_download": True}).extract_info(url, download=False)
    manual = [k for k in (info.get("subtitles") or {}) if k.startswith("ja")]
    auto = [k for k in (info.get("automatic_captions") or {}) if k in ("ja", "ja-orig")]
    if not manual and not auto:
        sys.exit("This video has no Japanese subtitles (not even auto-generated ones). Pick another video, or give me a subtitle/transcript file.")
    opts = {"quiet": True, "no_warnings": True, "noprogress": True, "skip_download": True, "subtitlesformat": "vtt", "outtmpl": os.path.join(tmp, "subs.%(ext)s"),
            "writesubtitles": bool(manual), "writeautomaticsub": not manual, "subtitleslangs": manual[:1] or auto[:1]}
    yt_dlp.YoutubeDL(opts).download([url])
    f = (glob.glob(os.path.join(tmp, "*.vtt")) or glob.glob(os.path.join(tmp, "*")))[0]
    return info.get("title", url), info.get("duration") or 0, ("creator's own" if manual else "auto-generated"), parse_caption_file(f)

def main():
    src = sys.argv[1]
    name = sys.argv[sys.argv.index("--name") + 1] if "--name" in sys.argv else None
    if re.match(r"https?://", src):
        title, dur, kind, lines = from_youtube(src); url = src
    else:
        title, dur, kind, lines, url = os.path.basename(src), 0, "file", parse_caption_file(src), ""
        dur = lines[-1]["t"] if lines else 0
    if not lines: sys.exit("No Japanese lines found in that transcript.")
    name = name or re.sub(r"[^\w\-]+", "-", title)[:40].strip("-") or "deck"
    d = os.path.join(jp.DATA, "decks", name); os.makedirs(d, exist_ok=True)
    json.dump({"title": title, "url": url, "duration": dur, "subtitles": kind, "lines": lines},
              open(os.path.join(d, "transcript.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=0)
    print(f"Saved {len(lines)} lines to {os.path.join(d, 'transcript.json')}")
    print(f"DECK={name}")
    print(f"Title: {title} | subtitles: {kind} | length: {dur // 60}:{dur % 60:02d}")
    if dur and dur > 22 * 60:
        print("Note: the one-video method works best with a video of about 15 minutes (20 max). This one is longer.")
    if kind == "auto-generated":
        print("Note: these are auto-generated subtitles, so some lines may be misheard. Sentences on cards will be checked anyway.")

if __name__ == "__main__":
    main()
