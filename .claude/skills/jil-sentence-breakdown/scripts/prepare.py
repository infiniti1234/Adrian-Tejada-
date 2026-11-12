# Step 1 of a sentence breakdown: gets a video's Japanese subtitles WITH start/end times, its audio, and splits every
# line into words. Writes ~/JIL-decks/breakdowns/<name>/:
#   lines.json          [{id, start, end, text}] plus title, url and the audio file name
#   audio.<ext>         the video's audio (the page plays each line from it)
#   todo/lines_NN.json  batches of 40 lines for Claude to fill: the English, the chunks, and a meaning for every word
#                       piece (pre-split by the tokeniser, grammar meanings pre-filled; Claude fixes the split if it's wrong)
# Usage: python scripts/prepare.py <YouTube URL or .srt/.vtt file> --name <short-name> [--audio <file>] [--from 0] [--to 300]
#   --from/--to  only this part of the video, in seconds (handy for a long video or a first try)
#   --audio      use a local audio/video file instead of downloading (needed for a subtitle file)
import glob, json, os, re, shutil, sys, tempfile
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402
from grammar import gloss_for  # noqa: E402

TS = re.compile(r"(?:(\d+):)?(\d\d?):(\d\d)[.,](\d{1,3})")


def t2s(m):
    return int(m.group(1) or 0) * 3600 + int(m.group(2)) * 60 + int(m.group(3)) + int(m.group(4).ljust(3, "0")) / 1000


def parse_captions(path):
    raw = open(path, encoding="utf-8-sig").read()
    cues = []
    for block in re.split(r"\n\s*\n", raw):
        arrow = [ln for ln in block.splitlines() if "-->" in ln]
        if not arrow: continue
        ts = list(TS.finditer(arrow[0]))
        if len(ts) < 2: continue
        text = []
        for ln in block.splitlines():
            if "-->" in ln or re.fullmatch(r"\d+", ln.strip()) or ln.startswith(("WEBVTT", "Kind:", "Language:", "NOTE")): continue
            ln = re.sub(r"<[^>]+>", "", ln).strip()
            if ln: text.append(ln)
        if text: cues.append({"start": t2s(ts[0]), "end": t2s(ts[1]), "text": " ".join(text)})
    # auto-captions roll: each line appears several times as it scrolls. Keep each new line once, with its full span.
    out = []
    for c in cues:
        if out and (c["text"] == out[-1]["text"] or out[-1]["text"].endswith(c["text"])):
            out[-1]["end"] = max(out[-1]["end"], c["end"]); continue
        if out and c["text"].startswith(out[-1]["text"]):
            c["start"] = out[-1]["start"]; out[-1] = c; continue
        out.append(c)
    return out


def from_youtube(url, d):
    import yt_dlp
    info = yt_dlp.YoutubeDL({"quiet": True, "no_warnings": True, "skip_download": True}).extract_info(url, download=False)
    manual = [k for k in (info.get("subtitles") or {}) if k.startswith("ja")]
    auto = [k for k in (info.get("automatic_captions") or {}) if k in ("ja", "ja-orig")]
    if not manual and not auto:
        sys.exit("This video has no Japanese subtitles (not even auto-generated ones). Pick another video.")
    tmp = tempfile.mkdtemp()
    yt_dlp.YoutubeDL({"quiet": True, "no_warnings": True, "noprogress": True, "skip_download": True, "subtitlesformat": "vtt",
                      "outtmpl": os.path.join(tmp, "subs.%(ext)s"), "writesubtitles": bool(manual), "writeautomaticsub": not manual,
                      "subtitleslangs": manual[:1] or auto[:1]}).download([url])
    cues = parse_captions(glob.glob(os.path.join(tmp, "*.vtt"))[0])
    # audio only, in a format browsers play without conversion (m4a), so no ffmpeg is needed
    yt_dlp.YoutubeDL({"quiet": True, "no_warnings": True, "noprogress": True, "format": "bestaudio[ext=m4a]/bestaudio",
                      "outtmpl": os.path.join(d, "audio.%(ext)s")}).download([url])
    audio = os.path.basename((glob.glob(os.path.join(d, "audio.*")) or [""])[0])
    return info.get("title", url), ("creator's own" if manual else "auto-generated"), cues, audio


def main():
    a = sys.argv
    src = a[1]
    name = a[a.index("--name") + 1] if "--name" in a else "breakdown"
    lo = float(a[a.index("--from") + 1]) if "--from" in a else 0
    hi = float(a[a.index("--to") + 1]) if "--to" in a else 1e9
    d = os.path.join(jp.DATA, "breakdowns", name); os.makedirs(d, exist_ok=True)
    if re.match(r"https?://", src):
        title, kind, cues, audio = from_youtube(src, d); url = src
    else:
        title, kind, cues, url = os.path.basename(src), "file", parse_captions(src), ""
        if "--audio" not in a: sys.exit("For a subtitle file, also give the video or audio file: --audio <file>")
        af = a[a.index("--audio") + 1]; ext = os.path.splitext(af)[1]
        shutil.copyfile(af, os.path.join(d, "audio" + ext)); audio = "audio" + ext
    cues = [c for c in cues if c["end"] > lo and c["start"] < hi]
    old = os.path.join(d, "index.html")
    if os.path.exists(old): os.remove(old)  # a page from an earlier run would be out of date
    lines = [{"id": i, "start": round(c["start"], 2), "end": round(c["end"], 2), "text": c["text"]} for i, c in enumerate(cues, 1)]
    json.dump({"title": title, "url": url, "subtitles": kind, "audio": audio, "lines": lines},
              open(os.path.join(d, "lines.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=0)
    todo = os.path.join(d, "todo"); os.makedirs(todo, exist_ok=True)
    for f in glob.glob(os.path.join(todo, "*.json")): os.remove(f)
    batch = []
    for l in lines:
        words, pos = [], 0
        for t, kana in jp.token_readings(l["text"]):
            i = l["text"].find(t.surface, pos)
            if i > pos and l["text"][pos:i].strip():
                words.append({"text": l["text"][pos:i], "base": "", "reading": "", "gloss": ""})
            pos = i + len(t.surface)
            f = t.feature
            word = re.search(r"[぀-ヿ㐀-鿿]", t.surface) and not jp.is_free(t) and f.pos1 in jp.CONTENT_POS
            words.append({"text": t.surface, "base": f.orthBase or t.surface, "reading": kana if re.search(r"[㐀-鿿]", t.surface) else "",
                          "gloss": "" if word else gloss_for(t.surface, f.orthBase or ""), **({"name": True} if f.pos2 == "固有名詞" else {})})
        merged = []  # 3 , 000 -> 3,000
        for w in words:
            if merged and re.fullmatch(r"[0-9０-９,，.．]+", w["text"]) and re.fullmatch(r"[0-9０-９,，.．]+", merged[-1]["text"]):
                merged[-1]["text"] += w["text"]; merged[-1]["base"] = merged[-1]["text"]
            else:
                merged.append(w)
        words = merged
        batch.append({"id": l["id"], "start": f"{int(l['start'] // 60)}:{int(l['start'] % 60):02d}", "text": l["text"],
                      "translation": "", "chunks": [], "words": words})
    for b in range(0, len(batch), 40):
        json.dump(batch[b:b + 40], open(os.path.join(todo, f"lines_{b // 40 + 1:02d}.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"{title} | subtitles: {kind} | {len(lines)} lines | audio: {audio or 'MISSING'}")
    print(f"To fill: {len(lines)} lines in {len(glob.glob(os.path.join(todo, 'lines_*.json')))} files -> {os.path.normpath(todo)}")
    print(f"NAME={name}")


if __name__ == "__main__":
    main()
