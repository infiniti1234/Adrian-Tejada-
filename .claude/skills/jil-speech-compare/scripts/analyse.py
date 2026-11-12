# Measures how you talk against how your speaker talks, from the two sets of transcripts, and writes
# ~/JIL-decks/speech/compare/<me>-vs-<speaker>/stats.json and stats.md. Only what text can show: speed, how long
# each stretch of speech runs and how often it chains on (けど, て, から...), how lines end, fillers, politeness,
# pronouns, Kansai and casual markers, and the style words and chunks one of you uses that the other doesn't.
# It can't judge sounds, pitch or accent. Whisper splits speech into short lines and usually adds no 。, so the
# measures work on its lines (stretches of speech), the same way for both people.
#
#   python scripts/analyse.py --me <me-name> --speaker <speaker-name>
import argparse, collections, glob, json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402

JA = re.compile(r"[぀-ヿ㐀-鿿々〆ー]")
SPLIT = re.compile(r"(?<=[。？！?!])")
FILLER_RE = {"まあ": r"まあ", "なんか": r"なんか", "やっぱ(り)": r"やっぱ", "なんていうか": r"なんて(い|言)うか"}
KANSAI = {"ほんま": r"ほんま", "やん": r"やん(?![ちだ])", "へん": r"(?<=[らかまさせけれて])へん", "ちゃう": r"ちゃう(?![だ])", "やろ": r"やろ",
          "あかん": r"あかん", "ねん": r"ねん", "せや": r"せや", "おる": r"おる", "めっちゃ": r"めっちゃ"}
PRON = ["私", "わたし", "あたし", "僕", "ぼく", "俺", "おれ", "うち", "自分"]
STYLE_POS = {"副詞", "接続詞", "連体詞", "形容詞", "形状詞"}          # style words: how you say it, not what about
LIGHT_NOUN = {"感じ", "事", "物", "方", "時", "所", "風", "訳", "積もり", "為", "様", "気", "位", "辺", "人"}
LIGHT_VERB = {"為る", "成る", "有る", "居る", "言う", "思う", "来る", "行く", "見る", "仕舞う", "呉れる", "貰う", "遣る", "置く", "出来る"}


def load(name):
    d = os.path.join(jp.DATA, "speech", name, "transcripts")
    files = sorted(glob.glob(os.path.join(d, "*.json")))
    if not files: sys.exit(f"No transcripts for '{name}'. Run scripts/transcribe.py {name} first.")
    return [json.load(open(f, encoding="utf-8")) for f in files]


def stretches(lines):
    out = []
    for l in lines:
        for s in SPLIT.split(l["text"]):
            s = s.strip()
            if JA.search(s): out.append(s)
    return out


def light(t):
    """A token that belongs to how someone talks rather than what they talk about."""
    f = t.feature
    if f.pos1 in ("名詞", "固有名詞") or f.pos2 == "固有名詞": return f.lemma in LIGHT_NOUN
    if f.pos1 == "動詞": return f.pos2 == "非自立可能" or f.lemma in LIGHT_VERB
    return True


def ending(toks):
    """The run of particles and auxiliaries that closes a line: よね, んです, けど, って感じ..."""
    end = []
    for t in reversed(toks):
        f = t.feature
        if f.pos1 in ("補助記号", "空白"):
            if end: break
            continue
        if f.pos1 in ("助詞", "助動詞") or t.surface in ("ん", "の", "じゃ", "ない") or (t.surface == "感じ" and not end):
            end.append(t.surface)
            if len(end) >= 4: break
        else:
            break
    return "".join(reversed(end))


def measure(trs):
    lines = [l for tr in trs for l in tr["lines"]]
    talk = sum(max(0.0, l["end"] - l["start"]) for l in lines) / 60 or 1e-9
    text = "".join(l["text"] for l in lines)
    st = stretches(lines)
    style, grams, ends, conns, runon, polite, filler = (collections.Counter(), collections.Counter(), collections.Counter(), [], 0, 0, collections.Counter())
    ntok = 0
    for s in st:
        toks = [t for t in jp.tagger(s) if t.feature.pos1 not in ("補助記号", "空白")]
        ntok += len(toks)
        if not toks: continue
        e = ending(toks); ends[e or "(no ending)"] += 1
        conns.append(sum(1 for t in toks if t.feature.pos2 == "接続助詞"))
        if toks[-1].feature.pos2 == "接続助詞" or toks[-1].surface in ("で", "て", "けど", "けれど", "から", "し", "ので", "たら", "ば", "とか"):
            runon += 1
        if {"です", "ます"} & {t.feature.lemma for t in toks[-5:]}: polite += 1
        for t in toks:
            if t.feature.pos2 == "フィラー": filler[t.surface] += 1
            if t.feature.pos1 in STYLE_POS and not jp.is_free(t): style[t.feature.lemma] += 1
        for n in (2, 3, 4):
            for i in range(len(toks) - n + 1):
                g = toks[i:i + n]
                if all(light(t) for t in g) and any(t.feature.pos1 in ("助詞", "助動詞") for t in g):
                    k = "".join(t.surface for t in g)
                    if len(k) >= 3: grams[k] += 1
    for k, rx in FILLER_RE.items(): filler[k] += len(re.findall(rx, text))
    lens = [len(JA.findall(s)) for s in st] or [0]
    n = max(1, len(st))
    return {
        "minutes_talking": round(talk, 1), "files": len(trs), "lines": len(st),
        "chars_per_min": round(len(JA.findall(text)) / talk),
        "avg_line_chars": round(sum(lens) / len(lens), 1),
        "runon_pct": round(100 * runon / n),
        "connectors_per_line": round(sum(conns) / max(1, len(conns)), 2),
        "polite_pct": round(100 * polite / n),
        "fillers_per_min": {k: round(v / talk, 2) for k, v in filler.most_common() if v},
        "endings_top": {k: round(100 * v / n) for k, v in ends.most_common(15)},
        "pronouns": {p: text.count(p) for p in PRON if text.count(p)},
        "kansai_casual_per_min": {k: round(len(re.findall(rx, text)) / talk, 2) for k, rx in KANSAI.items() if re.findall(rx, text)},
        "_style": style, "_grams": grams, "_ntok": max(1, ntok),
    }


def merge_fragments(items):
    """Drop a chunk that's just a piece of a longer listed chunk with nearly the same count (たから inside したから)."""
    keep = []
    for w, c, *rest in items:
        if any(w != w2 and w in w2 and c2 >= 0.7 * c for w2, c2, *_ in items):
            continue
        keep.append((w, c, *rest))
    return keep


def only(a, b, key, floor, top):
    """What a uses at least `floor` times that b never uses, with a's rate a minute."""
    xs = [(w, c, round(c / a["minutes_talking"], 2)) for w, c in a[key].most_common(3000) if c >= floor and b[key][w] == 0]
    return (merge_fragments(xs) if key == "_grams" else xs)[:top]


def far_more(a, b, key, floor, ratio, top):
    """What a uses at least `ratio` times as often a minute as b (b does use it)."""
    xs = []
    for w, c in a[key].most_common(3000):
        if c < floor or b[key][w] == 0: continue
        ra, rb = c / a["minutes_talking"], b[key][w] / b["minutes_talking"]
        if ra >= ratio * rb: xs.append((w, c, round(ra, 2), round(rb, 2)))
    return (merge_fragments(xs) if key == "_grams" else xs)[:top]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--me", required=True); ap.add_argument("--speaker", required=True)
    a = ap.parse_args()
    me, sp = measure(load(a.me)), measure(load(a.speaker))
    per = lambda m, w: 1000 * m["_style"][w] / m["_ntok"]
    over = sorted(((w, c, round(per(me, w) / max(per(sp, w), 1000 / sp["_ntok"]), 1)) for w, c in me["_style"].items() if c >= 3), key=lambda x: -x[2])
    out = {k: {kk: vv for kk, vv in v.items() if not kk.startswith("_")} for k, v in (("me", me), ("speaker", sp))}
    out.update({"names": {"me": a.me, "speaker": a.speaker},
                "speaker_style_words_you_never_use": only(sp, me, "_style", 3, 25),
                "your_overused_style_words": [x for x in over if x[2] >= 3][:15],
                "speaker_chunks_you_never_use": only(sp, me, "_grams", 4, 30),
                "speaker_chunks_far_more_than_you": far_more(sp, me, "_grams", 5, 3, 15),
                "your_chunks_they_never_use": only(me, sp, "_grams", 3, 20),
                "your_chunks_far_more_than_them": far_more(me, sp, "_grams", 3, 3, 15)})
    cd = os.path.join(jp.DATA, "speech", "compare", f"{a.me}-vs-{a.speaker}"); os.makedirs(cd, exist_ok=True)
    json.dump(out, open(os.path.join(cd, "stats.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=1)
    rows = [("Minutes of talking", "minutes_talking"), ("Characters a minute (speed)", "chars_per_min"),
            ("Average stretch of speech (characters per line)", "avg_line_chars"),
            ("Lines that run on into the next (けど, て, から...) %", "runon_pct"),
            ("Linking words per line", "connectors_per_line"), ("Polite (です/ます) lines %", "polite_pct")]
    md = [f"# {a.me} vs {a.speaker}: the numbers", "", f"| | {a.me} | {a.speaker} |", "|---|---|---|"]
    md += [f"| {lab} | {out['me'][k]} | {out['speaker'][k]} |" for lab, k in rows]
    for lab, k in (("Fillers a minute", "fillers_per_min"), ("How lines end (% of lines)", "endings_top"),
                   ("Pronouns (count)", "pronouns"), ("Kansai and casual markers a minute", "kansai_casual_per_min")):
        md += ["", f"## {lab}", f"- {a.me}: " + (", ".join(f"{x} {y}" for x, y in out["me"][k].items()) or "none"),
               f"- {a.speaker}: " + (", ".join(f"{x} {y}" for x, y in out["speaker"][k].items()) or "none")]
    fmt = lambda xs: ", ".join(f"{w} ({c}, {r}/min)" for w, c, r in xs) or "none"
    fmt2 = lambda xs: ", ".join(f"{w} ({c}, {ra}/min vs {rb}/min)" for w, c, ra, rb in xs) or "none"
    md += ["", f"## Chunks {a.speaker} uses that you never do (check each in the transcript; some are noise)", fmt(out["speaker_chunks_you_never_use"]),
           "", f"## Chunks {a.speaker} uses far more than you (3x+ a minute)", fmt2(out["speaker_chunks_far_more_than_you"]),
           "", f"## Your chunks {a.speaker} never uses", fmt(out["your_chunks_they_never_use"]),
           "", f"## Chunks you use far more than {a.speaker} (3x+ a minute)", fmt2(out["your_chunks_far_more_than_them"]),
           "", f"## Style words {a.speaker} uses that you never do (adverbs, linking and describing words)", fmt(out["speaker_style_words_you_never_use"]),
           "", f"## Style words you use far more than {a.speaker}",
           ", ".join(f"{w} ({c}x, {r}x their rate)" for w, c, r in out["your_overused_style_words"]) or "none",
           "", "Whisper splits speech into short lines and usually adds no 。, so these work on its lines, the same way for both of you. "
               "Topic words are left out on purpose: talk about similar things to your speaker for a fair comparison."]
    open(os.path.join(cd, "stats.md"), "w", encoding="utf-8").write("\n".join(md) + "\n")
    print("\n".join(md[:4 + len(rows)]))
    print(f"\nWrote {os.path.join(cd, 'stats.md')} and stats.json")
    if me["minutes_talking"] < 5 or sp["minutes_talking"] < 15:
        print("Warning: not much audio. The numbers get more reliable with 20+ minutes of you and 30+ of them.")


if __name__ == "__main__":
    main()
