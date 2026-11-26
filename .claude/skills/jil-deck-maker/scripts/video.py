# The creator's own voice on the cards, cut from the video's audio (references/video-audio.md).
#   python scripts/video.py words  <name> <audio-or-video-file>   Whisper transcript with a time for every word -> decks/<name>/words.json
#   python scripts/video.py clips  <name>                         a clip of the creator saying each card's word -> decks/<name>/clips/NNN.mp3,
#                                                                 plus "video_line" / "video_word" / "video_clip" on each card
#   python scripts/video.py fronts <name>                         for cards with "video_cut", cut the exact front clip and set front = "video"
#   python scripts/video.py verify <name>                         re-transcribe every front clip and print what Whisper hears next to the card text
# The audio file comes from the learner (YouTube often blocks downloads from scripts). Clips and the built deck stay on their computer.
import json, os, re, subprocess, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
os.environ.setdefault("HF_HUB_DISABLE_XET", "1")  # plain downloads for the Whisper model
import jp  # noqa: E402

BOUND = "。！？?!"


def deck(name):
    d = os.path.join(jp.DATA, "decks", name)
    return d, os.path.join(d, "cards.json")


def pcm(path, start=None, end=None):
    import numpy as np
    cmd = ["ffmpeg", "-v", "error"] + (["-ss", f"{start:.2f}", "-to", f"{end:.2f}"] if start is not None else []) + ["-i", path, "-ac", "1", "-ar", "16000", "-f", "f32le", "-"]
    return np.frombuffer(subprocess.run(cmd, capture_output=True, check=True).stdout, dtype=np.float32)


def model():
    from faster_whisper import WhisperModel
    return WhisperModel(jp.config().get("whisper_model", "small"), device="auto", compute_type="int8")


def timeline(d):
    """Whisper words -> one string with a (start, end) time per character, and the word edges."""
    words = json.load(open(os.path.join(d, "words.json"), encoding="utf-8"))
    chars, starts, ends = [], set(), set()
    for w, s, e in words["words"]:
        w = w.strip(); n = max(len(w), 1)
        if w: starts.add(len(chars)); ends.add(len(chars) + len(w))
        for i, ch in enumerate(w): chars.append((ch, s + (e - s) * i / n, s + (e - s) * (i + 1) / n))
    return "".join(c[0] for c in chars), chars, starts, ends, words["audio"]


def cut(audio, a, b, out):
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-ss", f"{a:.2f}", "-to", f"{b:.2f}", "-i", audio,
                    "-af", "afade=t=in:d=0.05,areverse,afade=t=in:d=0.12,areverse", "-ac", "1", "-b:a", "96k", out], check=True)


def cmd_words(name, src):
    d, _ = deck(name); os.makedirs(d, exist_ok=True)
    segs, _ = model().transcribe(pcm(src), language="ja", word_timestamps=True, vad_filter=True)
    out, srt = [], []
    def ts(x): return f"{int(x // 3600):02d}:{int(x % 3600 // 60):02d}:{int(x % 60):02d},{int(x * 1000) % 1000:03d}"
    for s in segs:
        out += [[w.word, round(w.start, 2), round(w.end, 2)] for w in s.words]
        srt.append(f"{len(srt) + 1}\n{ts(s.start)} --> {ts(s.end)}\n{s.text.strip()}\n")
        print(f"{int(s.start) // 60}:{int(s.start) % 60:02d} {s.text[:50]}", flush=True)
    open(os.path.join(d, "whisper.srt"), "w", encoding="utf-8").write("\n".join(srt))  # a transcript for get_transcript.py when YouTube blocks the subtitles
    json.dump({"audio": os.path.abspath(src), "words": out}, open(os.path.join(d, "words.json"), "w", encoding="utf-8"), ensure_ascii=False)
    print(f"{len(out)} words -> {os.path.join(d, 'words.json')}")


def cmd_clips(name):
    d, cf = deck(name)
    T, chars, starts, ends, audio = timeline(d)
    cards = json.load(open(cf, encoding="utf-8")); os.makedirs(os.path.join(d, "clips"), exist_ok=True); missing = []
    for i, c in enumerate(cards, 1):
        out = os.path.join(d, "clips", f"{i:03d}.mp3")
        t = c.get("t")
        keys = c.get("video_forms") or [c["target"]] + ([c["target"][:-1]] if len(c["target"]) > 2 and c["target"][-1] in "るすうくぐむぶつい" else [])
        hits = []
        for k in keys:
            hits = [(m.start(), m.end()) for m in re.finditer(re.escape(k), T) if t is None or abs(chars[m.start()][1] - t) < 45]
            if hits: break
        if not hits:
            missing.append(f"#{i} {c['target']}")
            for k in ("video_line", "video_word", "video_clip"): c.pop(k, None)
            if os.path.exists(out): os.remove(out)
            continue
        s0, e0 = min(hits, key=lambda h: abs(chars[h[0]][1] - (t or 0)))
        ws, we = chars[s0][1], chars[e0 - 1][2]
        L, R = s0, e0
        while L > 0 and T[L - 1] not in BOUND and ws - chars[L - 1][1] < 2.2: L -= 1
        while R < len(T) and T[R] not in BOUND and chars[R][2] - we < 2.2: R += 1
        if R < len(T) and T[R] in BOUND: R += 1
        while L < s0 and L not in starts: L += 1   # start and end on whole words
        while R > e0 and R not in ends: R -= 1
        a, b = max(0, chars[L][1] - 0.2), chars[R - 1][2] + 0.35
        cut(audio, a, max(b, a + 2.0), out)
        line = ("…" if L > 0 and T[L - 1] not in BOUND else "") + T[L:R].strip() + ("…" if R < len(T) and T[R - 1] not in BOUND else "")
        c.update(video_line=line, video_word=T[s0:e0], video_clip=[round(a, 2), round(b, 2)])
        print(f"#{i} {c['target']}: {line}")
    json.dump(cards, open(cf, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"\n{len(cards) - len(missing)} clips. Not found (give the card \"video_forms\" with Whisper's spelling, or leave it without a clip): {', '.join(missing) or 'none'}")


def cmd_fronts(name):
    d, cf = deck(name)
    T, chars, starts, ends, audio = timeline(d)
    cards = json.load(open(cf, encoding="utf-8")); probs = []
    for i, c in enumerate(cards, 1):
        if not c.get("video_cut"): continue  # cards without a cut keep whatever front they have
        sa, ea = c["video_cut"]; ref = sum(c.get("video_clip", [c.get("t", 0)] * 2)[:2]) / 2
        cand = [k for k in range(len(T)) if T.startswith(sa, k) and abs(chars[k][1] - ref) < 15]
        if not cand: probs.append(f"#{i}: start '{sa}' not found"); continue
        s0 = min(cand, key=lambda k: abs(chars[k][1] - ref)); e0 = T.find(ea, s0)
        if e0 < 0 or chars[e0][1] - chars[s0][1] > 15: probs.append(f"#{i}: end '{ea}' not found"); continue
        a, b = max(0, chars[s0][1] - 0.15), chars[e0 + len(ea) - 1][2] + 0.35
        cut(audio, a, b, os.path.join(d, "clips", f"{i:03d}.mp3"))
        c.update(front="video", video_clip=[round(a, 2), round(b, 2)])
    json.dump(cards, open(cf, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"{sum(1 for c in cards if c.get('front') == 'video')} cards with the creator's line on the front. Problems: {'; '.join(probs) or 'none'}")


def cmd_verify(name):
    d, cf = deck(name); m = model()
    for i, c in enumerate(json.load(open(cf, encoding="utf-8")), 1):
        if c.get("front") != "video": continue
        segs, _ = m.transcribe(pcm(os.path.join(d, "clips", f"{i:03d}.mp3")), language="ja")
        print(f"#{i} {c['target']}\n   card : {c.get('video_front')}\n   heard: {''.join(s.text for s in segs)}")


if __name__ == "__main__":
    a = sys.argv[1:]
    if len(a) < 2 or a[0] not in ("words", "clips", "fronts", "verify") or (a[0] == "words" and len(a) < 3):
        sys.exit(__doc__ or "usage: video.py words|clips|fronts|verify <name> [audio file]")
    {"words": lambda: cmd_words(a[1], a[2]), "clips": lambda: cmd_clips(a[1]),
     "fronts": lambda: cmd_fronts(a[1]), "verify": lambda: cmd_verify(a[1])}[a[0]]()
