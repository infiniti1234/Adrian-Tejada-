# Same file as jil-deck-maker/scripts/jp.py: keep the two copies identical.
# Shared Japanese helpers for the JIL deck-maker: tokenising (fugashi + unidic-lite), which words count as
# vocabulary vs free grammar, readings, furigana HTML and the target-word marker.
import html, json, os, re, sys
import fugashi

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

tagger = fugashi.Tagger()

# The learner's own data (decks, known-words list, settings, API key file) lives outside the skill folder,
# so updating the skill never touches it. Override with the JIL_DECKS_DIR environment variable.
DATA = os.environ.get("JIL_DECKS_DIR") or os.path.join(os.path.expanduser("~"), "JIL-decks")
os.makedirs(DATA, exist_ok=True)
DEFAULT_CONFIG = {"master_deck": "Japanese", "card_theme": "dark", "definitions": "english", "audio": "back", "voice_id": "Mv8AjrYZCBkdsmDHNwcB", "tts_model": "eleven_v3"}


def config():
    p = os.path.join(DATA, "config.json")
    if not os.path.exists(p):
        json.dump(DEFAULT_CONFIG, open(p, "w", encoding="utf-8"), indent=1)
    c = dict(DEFAULT_CONFIG); c.update(json.load(open(p, encoding="utf-8")))
    return c
KANJI = re.compile(r"[㐀-鿿々〆]")
SMALL = set("ゃゅょぁぃぅぇぉャュョァィゥェォ")

# Grammar that doesn't count as a new word on a card: particles, auxiliaries, copula, prefixes/suffixes, symbols,
# interjections, numbers, and the grammar items Tae Kim covers.
FREE_POS = {"助詞", "助動詞", "補助記号", "記号", "空白", "接頭辞", "接尾辞", "感動詞"}
FREE_LEMMA = {"仕舞う", "置く", "知れる", "過ぎる", "下さる", "御座る", "みたい", "そう-様態", "そう-伝聞", "様", "為さる", "ちゃう", "ん", "の"}
CONTENT_POS = {"名詞", "動詞", "形容詞", "副詞", "代名詞", "連体詞", "接続詞", "形状詞"}
# readings the tokeniser gets wrong for everyday speech
UNVOICE = dict(zip("がぎぐげござじずぜぞだぢづでどばびぶべぼぱぴぷぺぽ", "かきくけこさしすせそたちつてとはひふへほはひふへほ"))
READ_OVERRIDE = {"今日": "きょう", "何時": "なんじ", "明日": "あした", "私": "わたし"}


def hira(s):
    return "".join(chr(ord(c) - 0x60) if "ァ" <= c <= "ヶ" else c for c in (s or ""))


def is_free(t):
    f = t.feature
    lem = f.lemma or t.surface
    return f.pos1 in FREE_POS or lem in FREE_LEMMA or f.pos2 == "数詞" or t.surface.isdigit() or not re.search(r"[぀-ヿ㐀-鿿]", t.surface)


def vocab_tokens(text):
    """Tokens that count as vocabulary: [(surface, lemma, dictionary form, reading, pos)]"""
    out = []
    for t in tagger(text):
        if is_free(t):
            continue
        f = t.feature
        if f.pos1 not in CONTENT_POS or f.pos2 == "固有名詞":  # names and titles never count as words to learn
            continue
        out.append((t.surface, f.lemma or t.surface, f.orthBase or t.surface, hira(f.kanaBase or f.kana or t.surface), f.pos1, f.pos2))
    return out


def lemmas_in(text):
    return {x[1] for x in vocab_tokens(text)}


def target_lemmas(sentence, target):
    """The lemmas the target covers AS USED in this sentence (the tokeniser can read a word differently on its own:
    レア alone is 'rate', in a sentence it's 'rare'). Falls back to the target on its own if it isn't written literally."""
    i = sentence.find(target)
    if i < 0:
        return lemmas_in(target) or {target}
    out, pos = set(), 0
    for t in tagger(sentence):
        j = sentence.find(t.surface, pos); pos = j + len(t.surface)
        if j < i + len(target) and pos > i:
            out.add(t.feature.lemma or t.surface)
    return out or {target}


def ruby(surface, reading):
    """Reading over the kanji only, leaving okurigana outside the ruby (申し訳 -> 申[もう]し訳[わけ])."""
    r = hira(reading)
    if not KANJI.search(surface) or not r:
        return html.escape(surface)
    pre = 0
    while pre < len(surface) and pre < len(r) and not KANJI.match(surface[pre]) and surface[pre] == r[pre]:
        pre += 1
    suf = 0
    while suf < len(surface) - pre and suf < len(r) - pre and not KANJI.match(surface[-1 - suf]) and surface[-1 - suf] == r[-1 - suf]:
        suf += 1
    core, rc = surface[pre:len(surface) - suf], r[pre:len(r) - suf]
    parts = re.findall(r"[㐀-鿿々〆]+|[^㐀-鿿々〆]+", core)
    if len(parts) > 1:
        pat = "".join("(.+?)" if KANJI.match(x[0]) else re.escape(hira(x)) for x in parts)
        m = re.fullmatch(pat, rc)
        if m:
            gi, mid = 0, []
            for x in parts:
                if KANJI.match(x[0]):
                    gi += 1; mid.append(f"<ruby>{html.escape(x)}<rt>{html.escape(m.group(gi))}</rt></ruby>")
                else:
                    mid.append(html.escape(x))
            return html.escape(surface[:pre]) + "".join(mid) + html.escape(surface[len(surface) - suf:])
    return f"{html.escape(surface[:pre])}<ruby>{html.escape(core)}<rt>{html.escape(rc)}</rt></ruby>{html.escape(surface[len(surface) - suf:])}"


def _wrap_literal(furi, form):
    tokens = re.findall(r"<ruby>.*?</ruby>|<[^>]+>|&[a-z#0-9]+;|.", furi, re.S)
    text = [re.sub(r"<[^>]+>", "", re.sub(r"<rt>.*?</rt>", "", x)) for x in tokens]
    for i in range(len(tokens)):
        acc = ""
        for j in range(i, len(tokens)):
            acc += html.unescape(text[j])
            if acc == form:
                return "".join(tokens[:i]) + '<span class="tw">' + "".join(tokens[i:j + 1]) + "</span>" + "".join(tokens[j + 1:])
            if not form.startswith(acc):
                break
    return furi


def token_readings(sent):
    """[(token, kana)] with the fixes for readings the tokeniser gets wrong (今日, 何, 一回, 方, no rendaku at a phrase start)."""
    toks = list(tagger(sent)); out, prev = [], ""
    for ti, t in enumerate(toks):
        f = t.feature
        kana = READ_OVERRIDE.get(t.surface) or f.kana or ""
        # undo rendaku only: the first kana is the voiced twin of the dictionary form's (ぶ for ふ), at a phrase start
        if (t.surface not in READ_OVERRIDE and kana and f.kanaBase and UNVOICE.get(hira(kana[:1])) == hira(f.kanaBase[:1])
                and (ti == 0 or toks[ti - 1].feature.pos1 in ("助詞", "補助記号", "記号"))):
            kana = hira(f.kanaBase[:1]) + hira(kana[1:])
        if t.surface == "中" and prev in ("今日", "一日", "世界"): kana = "じゅう"
        if t.surface == "一" and ti + 1 < len(toks) and toks[ti + 1].surface in ("回", "個", "階"): kana = "いっ"
        if t.surface == "方" and f.pos1 == "名詞": kana = "ほう"
        if t.surface == "何":
            nxt = toks[ti + 1].surface if ti + 1 < len(toks) else ""
            nxt_verb = ti + 1 < len(toks) and toks[ti + 1].feature.pos1 == "動詞"
            kana = "なに" if nxt_verb or (nxt[:1] in ("を", "が", "も", "か", "し", "に", "、", "？", "！", "") or nxt.startswith("これ")) else "なん"
        out.append((t, hira(kana))); prev = t.surface
    return out


def sentence_html(sent, target_lemmas, target_form, multi=False):
    """(front HTML, back furigana HTML) with the target word wrapped in <span class="tw">."""
    plain, furi, hit = [], [], False
    pos = 0
    literal = len(target_form) > 1 and target_form in sent
    for t, kana in token_readings(sent):
        i = sent.find(t.surface, pos)
        if i > pos:
            gap = html.escape(sent[pos:i]); plain.append(gap); furi.append(gap)
        pos = i + len(t.surface)
        f = t.feature
        p, r = html.escape(t.surface), ruby(t.surface, kana)
        if (f.lemma in target_lemmas) and (multi or not hit) and not literal:
            hit = True
            p, r = f'<span class="tw">{p}</span>', f'<span class="tw">{r}</span>'
        plain.append(p); furi.append(r)
    if pos < len(sent):
        plain.append(html.escape(sent[pos:])); furi.append(html.escape(sent[pos:]))
    a, b = "".join(plain), "".join(furi)
    b = b.replace("<ruby>今日<rt>きょう</rt></ruby><ruby>中<rt>じゅう</rt></ruby>", "<ruby>今日中<rt>きょうじゅう</rt></ruby>")
    if literal:
        a = a.replace(html.escape(target_form), f'<span class="tw">{html.escape(target_form)}</span>', 1)
        b = _wrap_literal(b, target_form)
    return a, b


def kana_text(sent):
    """The sentence in kana, using the same readings as the furigana (for voicing tricky sentences)."""
    _, furi = sentence_html(sent, set(), "")
    t = re.sub(r"<ruby>.*?<rt>(.*?)</rt></ruby>", r"\1", furi)
    return html.unescape(re.sub(r"<[^>]+>", "", t))


def word_reading(form):
    toks = list(tagger(form))
    return "".join(READ_OVERRIDE.get(t.surface) or hira(t.feature.kana or t.surface) for t in toks)
