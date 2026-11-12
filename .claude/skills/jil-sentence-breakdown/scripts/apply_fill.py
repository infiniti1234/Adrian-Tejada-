# Applies a compact fill file to a todo batch, so the big todo JSON never has to be hand-edited.
# Fill file: breakdowns/<name>/todo/fill_NN.json, one entry per line id:
#   {"57": {"t": "You have to put in three bills?!",
#           "c": [["なあかん", "have to (Kansai)"]],
#           "w": [["3"], ["枚", "counter for bills"], ["入れ", "to put in", "入れる"], ["な", "if not (Kansai)"],
#                 ["あかん", "no good (Kansai for だめ)"], ["の", "explaining"], ["か", "question marker"], ["！"]]}}
#   t = English; c = chunks [text, meaning]; w = every word piece in order: [text, gloss, base?, reading?, "name"?]
#   base defaults to the text; reading is filled in automatically for kanji pieces unless you give one; add "name"
#   as a 5th item for names. Leave "w" out to keep the pre-split pieces (then every word piece needs "g" below).
#   "g": {"piece text": "gloss"} fills glosses on the pre-split pieces without re-listing them.
# Usage: python scripts/apply_fill.py <name> <NN>     (applies todo/fill_NN.json to todo/lines_NN.json and checks it)
import json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402

LETTERS = re.compile(r"[぀-ヿ㐀-鿿A-Za-z]")


def main():
    name, nn = sys.argv[1], sys.argv[2].zfill(2)
    todo = os.path.join(jp.DATA, "breakdowns", name, "todo")
    tp, fp = os.path.join(todo, f"lines_{nn}.json"), os.path.join(todo, f"fill_{nn}.json")
    lines = json.load(open(tp, encoding="utf-8")); fill = json.load(open(fp, encoding="utf-8"))
    problems = []
    for l in lines:
        f = fill.get(str(l["id"]))
        if not f:
            if not l.get("translation"): problems.append(f"line {l['id']}: not in the fill file yet")
            continue
        if "t" in f: l["translation"] = f["t"]
        if "c" in f: l["chunks"] = [{"text": c[0], "meaning": c[1] if len(c) > 1 else ""} for c in f["c"]]
        if "w" in f:
            words = []
            for w in f["w"]:
                text = w[0]; gloss = w[1] if len(w) > 1 else ""; base = (w[2] if len(w) > 2 and w[2] else text)
                reading = w[3] if len(w) > 3 and w[3] else (jp.word_reading(text) if jp.KANJI.search(text) else "")
                d = {"text": text, "base": base, "reading": reading, "gloss": gloss}
                if len(w) > 4 and w[4] == "name": d["name"] = True
                words.append(d)
            l["words"] = words
        for piece, g in (f.get("g") or {}).items():
            for w in l["words"]:
                if w["text"] == piece: w["gloss"] = g
        # checks
        if re.sub(r"\s", "", "".join(w["text"] for w in l["words"])) != re.sub(r"\s", "", l["text"]):
            problems.append(f"line {l['id']}: pieces spell {''.join(w['text'] for w in l['words'])!r}, the line is {l['text']!r}")
        for w in l["words"]:
            if LETTERS.search(w["text"]) and not w.get("gloss") and not w.get("name"):
                problems.append(f"line {l['id']}: no meaning for {w['text']}")
            if len((w.get("gloss") or "").split()) > 10:
                problems.append(f"line {l['id']}: meaning for {w['text']} is long; keep it to about 6 words")
        for c in l.get("chunks") or []:
            if c["text"] not in l["text"]:
                problems.append(f"line {l['id']}: chunk {c['text']} isn't one unbroken piece of the line")
        if not l.get("translation"): problems.append(f"line {l['id']}: no English")
    json.dump(lines, open(tp, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    print(f"Applied fill_{nn}.json to lines_{nn}.json: {len(lines)} lines, {len(problems)} problems.")
    for p in problems[:40]: print("  " + p)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
