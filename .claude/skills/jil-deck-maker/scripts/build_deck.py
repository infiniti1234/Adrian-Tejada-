# Builds decks/<name>/<name>.apkg from decks/<name>/cards.json (+ decks/<name>/audio/NNN.mp3 if you made audio),
# into your master deck (config "master_deck"), with the JIL sentence card design in the learner's chosen look
# (config "card_theme": "dark" = black and gold, "light" = the Immersion Tracker look). Then adds every card's word/chunk to
# your known-words list so the next video never re-teaches it.
# Card (the JIL Sentence + Breakdown card, scripts/card.py): front = the sentence (+ audio if config audio is "front");
# back = sentence with furigana, audio (if "back"), Target word (Meaning, Breakdown, English equivalent), Sentence breakdown
# (Meaning, chunk Breakdown, English equivalent), a short note, and where it came from. Labels switch to 意味（日本語で）/
# 分解（日本語）with config "definitions": "japanese".
# Usage: python scripts/build_deck.py <deck-name>
import hashlib, html, json, os, re, shutil, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402
import known_words  # noqa: E402
import genanki  # noqa: E402

import card as CARD  # noqa: E402  (the JIL Sentence + Breakdown card)

# Two looks for the cards (config "card_theme"): dark = the JIL card, black and gold; light = the JIL Immersion
# Tracker's 日の丸 look. Same fields, so a learner can switch an existing deck later with Anki's Change Note Type.
THEMES = {
    "dark": dict(css=CARD.DARK_CSS, model_id=1730004021, name="JIL Sentence + Breakdown (dark)"),
    "light": dict(css=CARD.LIGHT_CSS, model_id=1730004031, name="JIL Sentence + Breakdown (light)"),
}
FIELDS = CARD.FIELDS


def model(theme):
    """Fixed ids per look: every deck the deck-maker builds in that look shares one note type, so cards merge cleanly."""
    t = THEMES[theme if theme in THEMES else "dark"]
    q, a = CARD.templates("Japanese Immersion Lab")
    return genanki.Model(t["model_id"], t["name"], fields=[{"name": f} for f in FIELDS],
                         templates=[{"name": "Sentence → meaning", "qfmt": q, "afmt": a}],
                         css=t["css"], sort_field_index=0)


def main():
    name = sys.argv[1]
    cfg = jp.config()
    d = os.path.join(jp.DATA, "decks", name)
    cards = json.load(open(os.path.join(d, "cards.json"), encoding="utf-8"))
    meta = json.load(open(os.path.join(d, "transcript.json"), encoding="utf-8"))
    deck_id = int(hashlib.md5(cfg["master_deck"].encode()).hexdigest()[:8], 16)
    deck = genanki.Deck(deck_id, cfg["master_deck"])
    media, stage = [], os.path.join(d, "media"); os.makedirs(stage, exist_ok=True)
    known = known_words.load()
    jj = cfg.get("definitions", "english") == "japanese"
    for i, c in enumerate(cards, 1):
        tl = jp.target_lemmas(c["sentence"], c["target"])
        front, furi = jp.sentence_html(c["sentence"], tl, c["target"], multi=c.get("kind") == "chunk")
        reading = c.get("reading") or ("" if re.fullmatch(r"[぀-ヿー・]+", c["target"]) else jp.word_reading(c["target"]))
        audio_tag, a = "", os.path.join(d, "audio", f"{i:03d}.mp3")
        if os.path.exists(a) and cfg.get("audio", "back") in ("front", "back"):
            fn = f"jil_{hashlib.md5((name + c['sentence']).encode()).hexdigest()[:12]}.mp3"
            shutil.copyfile(a, os.path.join(stage, fn)); media.append(os.path.join(stage, fn)); audio_tag = f"[sound:{fn}]"
        src = html.escape(meta.get("title", name)) + (f" · {c['t'] // 60}:{c['t'] % 60:02d}" if isinstance(c.get("t"), int) else "")
        kind = c.get("kind", "word")
        word = jp.ruby(c["target"], reading) if reading and reading != c["target"] else html.escape(c["target"])
        vals = dict(Number=str(i), Sentence=front, SentenceFurigana=furi, Word=word, Reading=html.escape(reading),
                    Pitch="", PitchPattern="", WordMeaning=CARD.li(c.get("wm")), WordBreakdown=CARD.li(c.get("wb")),
                    WordEnglish=CARD.li(c.get("we")), SentenceMeaning=CARD.li(c.get("sm")),
                    SentenceBreakdown=CARD.li(c.get("sb")), SentenceEnglish=CARD.li(c.get("se") or [c["translation"]]),
                    Note=html.escape(c.get("note", "")),
                    AudioFront=audio_tag if cfg.get("audio") == "front" else "",
                    Audio=audio_tag if cfg.get("audio", "back") == "back" else "",
                    JP="1" if jj else "", Source=src, Kind=c.get("kind", "word"), SentencePlain=html.escape(c["sentence"]))
        fields = [vals[f] for f in FIELDS]
        tags = ["jil", "jil-deck-maker", name.replace(" ", "_")] + (["chunk"] if kind == "chunk" else [])
        deck.add_note(genanki.Note(model=model(cfg.get("card_theme", "dark")), fields=fields, tags=tags, guid=genanki.guid_for("jil-dm", c["target"], c["sentence"])))
        # the deck's words become "known" for every future deck
        if kind == "chunk": known["chunks"].setdefault(c["target"], name)
        for l in tl | jp.lemmas_in(c["sentence"]): known["lemmas"].setdefault(l, name)
    # words deliberately skipped as names, titles or tokeniser fragments never come back on later videos
    sp = os.path.join(d, "skip.json")
    skipped = json.load(open(sp, encoding="utf-8")) if os.path.exists(sp) else []
    known.setdefault("ignore", {})
    for s in skipped:
        if s.get("reason") in ("name", "title", "fragment", "sound effect"): known["ignore"].setdefault(s["lemma"], s["reason"])
    out = os.path.join(d, f"{name}.apkg")
    pkg = genanki.Package(deck); pkg.media_files = media; pkg.write_to_file(out)
    known_words.save(known)
    print(f"Built {out}: {len(cards)} cards into deck '{cfg['master_deck']}', {len(media)} with audio.")
    print(f"Known-words list now {len(known['lemmas'])} words, {len(known['chunks'])} chunks, {len(known['ignore'])} names/fragments ignored.")
    up = os.path.join(d, "unknown.json")
    if os.path.exists(up):
        print(f"Before this deck you knew {json.load(open(up, encoding='utf-8')).get('known_before_pct', '?')}% of the words in the video.")
    print("To import: double-click the .apkg file (or in Anki: File > Import). New cards arrive in your master deck;"
          " suspend them all, then unsuspend 10 at a time as your retention allows.")


if __name__ == "__main__":
    main()
