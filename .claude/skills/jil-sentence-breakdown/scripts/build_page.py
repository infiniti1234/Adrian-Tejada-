# Step 3 of a sentence breakdown: merges lines.json with the filled todo files into breakdowns/<name>/index.html,
# after checking them: every line has English, its word pieces spell the line exactly, every word has a meaning,
# and every chunk is really in its line. Exit code 1 and a list of fixes if anything's missing.
# The page is self-contained (data inside the HTML) and plays each line from audio.<ext> sitting next to it.
# Per line: play / loop the line, the Japanese with furigana, the English, the chunks in it; hover or tap any word for
# its meaning; words not on the learner's known-words list are underlined; lines can be ticked off (saved in the browser).
# Usage: python scripts/build_page.py <name>
import glob, html, json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402

def main():
    name = sys.argv[1]
    d = os.path.join(jp.DATA, "breakdowns", name)
    data = json.load(open(os.path.join(d, "lines.json"), encoding="utf-8"))
    filled = {}
    for p in sorted(glob.glob(os.path.join(d, "todo", "lines_*.json"))):
        for x in json.load(open(p, encoding="utf-8")):
            filled[x["id"]] = x
    known = set()
    kp = os.path.join(jp.DATA, "known", "known_words.json")
    if os.path.exists(kp):
        known = set(json.load(open(kp, encoding="utf-8"))["lemmas"])
    problems, out_lines = [], []
    has_letters = lambda s: re.search(r"[぀-ヿ㐀-鿿A-Za-z]", s)
    for l in data["lines"]:
        x = filled.get(l["id"], {})
        segs = x.get("words") or []
        if not x.get("translation"): problems.append(f"line {l['id']}: no English")
        if re.sub(r"\s", "", "".join(w["text"] for w in segs)) != re.sub(r"\s", "", l["text"]):
            problems.append(f"line {l['id']}: the word pieces don't spell the line exactly ({''.join(w['text'] for w in segs)} vs {l['text']})")
        for c in x.get("chunks") or []:
            if c.get("text") and c["text"] not in l["text"]: problems.append(f"line {l['id']}: chunk {c['text']} isn't in the line")
        toks, pos = [], 0
        for w in segs:
            i = l["text"].find(w["text"], pos)
            gap = l["text"][pos:i] if i > pos else ""
            pos = (i if i >= 0 else pos) + len(w["text"])
            if has_letters(w["text"]) and not w.get("gloss") and not w.get("name"):
                problems.append(f"line {l['id']}: no meaning for {w['text']}")
            rd = w.get("reading") or (jp.word_reading(w["text"]) if jp.KANJI.search(w["text"]) and not w.get("name") else "")
            h = jp.ruby(w["text"], rd) if rd and jp.KANJI.search(w["text"]) else html.escape(w["text"])
            lem = jp.lemmas_in(w["text"])
            g = w.get("gloss") or ("name" if w.get("name") else "")
            toks.append({"gap": gap, "h": h, "b": w.get("base") or w["text"], "r": rd, "g": g,
                         "u": bool(known) and not w.get("name") and bool(lem) and not lem <= known})
        out_lines.append({"id": l["id"], "s": l["start"], "e": l["end"], "t": x.get("translation", ""), "c": x.get("chunks") or [],
                          "w": toks, "tail": l["text"][pos:]})
    page_data = json.dumps(out_lines, ensure_ascii=False).replace("</", "<\\/")
    page = (TEMPLATE.replace("__TITLE__", html.escape(data["title"])).replace("__AUDIO__", html.escape(data["audio"]))
            .replace("__URL__", html.escape(data.get("url", ""))).replace("__DATA__", page_data).replace("__KEY__", "jilbd_" + name))
    open(os.path.join(d, "index.html"), "w", encoding="utf-8").write(page)
    print(f"Built {os.path.normpath(os.path.join(d, 'index.html'))}: {len(out_lines)} lines.")
    if problems:
        print(f"{len(problems)} things to fix, then build again:")
        for p in problems[:40]: print("  " + p)
        if len(problems) > 40: print(f"  ...and {len(problems) - 40} more")
        return 1
    print("Open index.html in your browser (keep the audio file next to it).")
    return 0


# The page design (the JIL Immersion Tracker's 日の丸 look) lives in page.html next to this script.
TEMPLATE = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "page.html"), encoding="utf-8").read()

if __name__ == "__main__":
    sys.exit(main())
