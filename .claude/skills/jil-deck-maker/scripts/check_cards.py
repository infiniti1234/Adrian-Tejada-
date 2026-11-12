# Checks decks/<name>/cards.json against Harry's card rules before anything is built:
# - every card teaches ONE new word or chunk (its "target"), and its sentence otherwise only uses words the learner
#   knows, words taught by earlier cards in this deck, and free grammar (particles, endings, numbers...)
# - one extra unknown word is allowed when a one-new-word sentence would feel forced, but flagged; two or more fails
# - the target must appear in the sentence; no repeated sentences; no card for a word already known
# - with config "definitions": "japanese", a Japanese meaning may only use words the learner knows or that an earlier
#   card in this deck teaches (each unknown definition word gets its own card first, with an English meaning)
# - every card carries its breakdowns (references/card-rules.md section 4c): wm/wb/we for the word, sm/sb/se for
#   the sentence; the sentence chunks spell the sentence; the first English equivalent is the translation
# Usage: python scripts/check_cards.py <deck-name>     (exit code 1 if anything fails)
import json, os, re, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402
import known_words  # noqa: E402


STRIP = re.compile(r"[\s、。！？!?…「」『』（）()・〜~―\-,.]")
KANJI = re.compile(r"[㐀-鿿々〆]")


def breakdown_problems(c, jj):
    """The JIL Sentence + Breakdown card's six lists (card-rules.md 4c)."""
    probs = []
    for k, lo, hi in (("wm", 1, 3), ("wb", 0, 6), ("we", 2, 4), ("sm", 2, 2), ("sb", 2, 7), ("se", 3, 3)):
        v = c.get(k)
        if not isinstance(v, list) or not lo <= len(v) <= hi or not all(isinstance(x, str) and x.strip() for x in v):
            probs.append(f"breakdown {k}: needs {lo}-{hi} lines" if v is not None else f"missing breakdown {k}")
    if probs: return probs
    whole = "全体：" if jj else "Whole: "
    sb, wb = c["sb"], c["wb"]
    if not sb[-1].startswith(whole): probs.append(f"breakdown sb: the last line must start '{whole}'")
    if any("：" not in x for x in sb[:-1]): probs.append("breakdown sb: write each chunk as chunk：meaning (full-width colon)")
    elif STRIP.sub("", "".join(x.split("：", 1)[0] for x in sb[:-1])) != STRIP.sub("", c["sentence"]):
        probs.append("breakdown sb: the chunks, in order, must spell the whole sentence")
    if c["se"][0] != c["translation"]: probs.append("breakdown se: the first English equivalent must be the translation")
    parts = [x for x in wb if not x.startswith(whole)]
    if any("：" not in x for x in parts): probs.append("breakdown wb: write each part as part：meaning (full-width colon)")
    if len(parts) >= 2 and not wb[-1].startswith(whole): probs.append(f"breakdown wb: with 2+ parts, end with '{whole}'")
    if c.get("kind") != "chunk":
        lefts = "".join(x.split("：", 1)[0] for x in parts)
        miss = [k for k in KANJI.findall(c["target"]) if k not in lefts]
        if miss: probs.append(f"breakdown wb: break down every kanji ({''.join(miss)} missing)")
    for k in ("wm", "wb", "we", "sm", "sb", "se"):
        if any("—" in x or "–" in x for x in c[k]): probs.append(f"breakdown {k}: no dashes")
    return probs


def target_lemmas(card):
    return jp.target_lemmas(card["sentence"], card["target"])


def main():
    name = sys.argv[1]
    d = os.path.join(jp.DATA, "decks", name)
    cards = json.load(open(os.path.join(d, "cards.json"), encoding="utf-8"))
    known = known_words.load()
    jj = jp.config().get("definitions", "english") == "japanese"
    allowed = set(known["lemmas"])
    seen_sent, fails, extras_cards = set(), 0, 0
    for i, c in enumerate(cards, 1):
        probs, notes = [], []
        for k in ("target", "sentence", "meaning", "translation"):
            if not c.get(k): probs.append(f"missing {k}")
        if probs:
            print(f"FAIL #{i}: {'; '.join(probs)}"); fails += 1; continue
        tl = target_lemmas(c)
        if c.get("kind") != "chunk" and not c.get("dialect") and tl and tl <= set(known["lemmas"]):
            notes.append(f"{c['target']} is already on your known-words list")
        if c.get("kind") == "chunk" and c["target"] in known["chunks"]:
            notes.append(f"chunk {c['target']} is already known")
        if c["target"] not in c["sentence"] and not (tl & ({t.feature.lemma for t in jp.tagger(c["sentence"])})):
            probs.append(f"target {c['target']} isn't in the sentence")
        here = allowed | tl
        extra = sorted({x[0] + ("" if x[0] == x[2] else f"({x[2]})") for x in jp.vocab_tokens(c["sentence"]) if x[1] not in here
                        and not (c.get("kind") == "chunk" and x[0] in c["target"])})
        if len(extra) > 1:
            probs.append(f"{len(extra)} words the learner doesn't know besides the target: {', '.join(extra)}")
        elif extra:
            extras_cards += 1; notes.append(f"one extra new word: {extra[0]}")
        if jj and re.search(r"[\u3040-\u30ff\u3400-\u9fff]", c["meaning"]):
            unk = sorted({x[1] for x in jp.vocab_tokens(c["meaning"]) if x[1] not in here})
            if unk:
                probs.append(f"the Japanese definition uses {len(unk)} word(s) the learner doesn't know: {', '.join(unk)}"
                             + ("; give each its own card earlier in this deck, with an English meaning" if len(unk) <= 2
                                else "; that's too many, so give this word an English meaning instead"))
        probs += breakdown_problems(c, jj)
        if jj and c.get("wm") and re.search(r"[぀-ヿ㐀-鿿]", c["wm"][0]):
            unk = sorted({x[1] for x in jp.vocab_tokens(c["wm"][0]) if x[1] not in here})
            if unk:
                probs.append(f"the Japanese definition (wm) uses {len(unk)} word(s) the learner doesn't know: {', '.join(unk)}"
                             + ("; give each its own card earlier in this deck, with an English meaning" if len(unk) <= 2
                                else "; that's too many, so give this word English explanations instead"))
        if c["sentence"] in seen_sent: probs.append("same sentence as an earlier card")
        seen_sent.add(c["sentence"])
        if probs:
            fails += 1; print(f"FAIL #{i} {c['target']}: {c['sentence']}  -> {'; '.join(probs)}")
        elif notes:
            print(f"note #{i} {c['target']}: {c['sentence']}  ({'; '.join(notes)})")
        allowed |= tl | jp.lemmas_in(c["sentence"])  # everything on this card is "seen" for the cards after it
    share = extras_cards / len(cards) * 100 if cards else 0
    print(f"\n{len(cards)} cards, {fails} fail, {extras_cards} with one extra new word ({share:.0f}%)")
    if share > 10: print("Aim for no more than about 1 card in 10 with an extra word: rewrite some of those sentences.")
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
