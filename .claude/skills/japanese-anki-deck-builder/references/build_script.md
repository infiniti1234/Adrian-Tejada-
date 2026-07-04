# Build script template

Use this script verbatim. Only change: the `CARDS` list, `MODEL_ID`/`DECK_ID` (fresh random large integers per build), the deck name, and the output filename. Do not touch `CARD_CSS`, `QFMT`, or `AFMT`.

Audio is generated automatically via ElevenLabs if `ELEVENLABS_API_KEY` is set in the environment (e.g. `source` a local `.env` before running). If it's not set, the build still succeeds — the audio fields are just left empty and cards have no sound. Never hardcode a key into the script; always read it from the environment.

```python
# -*- coding: utf-8 -*-
import genanki
import hashlib
import os
import re
import time
import requests

# Fresh random large integers per build — never reuse across different decks.
MODEL_ID = 1734890051030  # REPLACE with a new random large int
DECK_ID = 1734890051031   # REPLACE with a new random large int

# ─── ElevenLabs audio (optional) ───────────────────────────────────
# Reads the key from the environment only — never hardcode it here.
ELEVENLABS_API_KEY = os.environ.get('ELEVENLABS_API_KEY')
# Default voice is a documented ElevenLabs premade voice ("Rachel") that
# works with the multilingual model. Override with your own voice_id via
# ELEVENLABS_VOICE_ID if you have one you prefer for Japanese.
ELEVENLABS_VOICE_ID = os.environ.get('ELEVENLABS_VOICE_ID', '21m00Tcm4TlvDq8ikWAM')
ELEVENLABS_MODEL_ID = 'eleven_multilingual_v2'
MEDIA_DIR = 'jadb_audio'
os.makedirs(MEDIA_DIR, exist_ok=True)

_tts_cache = {}

def strip_furigana(text):
    """Drop 漢字[かんじ] bracket readings before sending text to TTS, keep the kanji."""
    return re.sub(r'\[[^\]]*\]', '', text)

def strip_word_reading(word_header):
    """'格納（かくのう）' -> '格納' — speak the word itself, not the parenthetical reading."""
    return re.sub(r'[（(].*?[）)]', '', word_header).strip()

def tts_mp3(text):
    """Generate an mp3 via ElevenLabs for `text`, return its filename, or None if unavailable/failed."""
    if not ELEVENLABS_API_KEY or not text:
        return None
    if text in _tts_cache:
        return _tts_cache[text]
    filename = f"jadb_{hashlib.md5(text.encode('utf-8')).hexdigest()[:12]}.mp3"
    path = os.path.join(MEDIA_DIR, filename)
    if not os.path.exists(path):
        try:
            resp = requests.post(
                f"https://api.elevenlabs.io/v1/text-to-speech/{ELEVENLABS_VOICE_ID}",
                headers={
                    "xi-api-key": ELEVENLABS_API_KEY,
                    "Content-Type": "application/json",
                    "Accept": "audio/mpeg",
                },
                json={
                    "text": text,
                    "model_id": ELEVENLABS_MODEL_ID,
                    "voice_settings": {"stability": 0.5, "similarity_boost": 0.75},
                },
                timeout=30,
            )
            resp.raise_for_status()
            with open(path, 'wb') as f:
                f.write(resp.content)
            time.sleep(0.3)  # be gentle on rate limits
        except requests.RequestException as e:
            print(f"  [audio skipped] {text!r}: {e}")
            return None
    _tts_cache[text] = filename
    return filename

CARD_CSS = """
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@300;400;500;700;900&family=Outfit:wght@200;300;400;500;600&family=Inter:wght@300;400;500;600&display=swap');
:root {
--bg: #0a0a0a;
--gold: #c4a44a;
--gold-light: #d4b85a;
--text: #e8e8e8;
--text-secondary: #cccccc;
--text-dim: #555555;
--muted: #666666;
--line: #2a2a2a;
}
html, body, .card {
margin: 0 !important;
padding: 0 !important;
background: #0a0a0a !important;
background-color: #0a0a0a !important;
height: 100% !important;
width: 100% !important;
color: var(--text);
font-family: 'Noto Sans JP', 'Yu Gothic', 'Hiragino Kaku Gothic ProN', sans-serif;
font-weight: 400;
}
.card.nightMode, .card.night_mode {
background: #0a0a0a !important;
}
.screen {
position: fixed;
top: 0; left: 0; right: 0; bottom: 0;
background: #0a0a0a;
overflow-y: auto;
display: flex;
flex-direction: column;
align-items: center;
padding: 28px 22px 60px;
box-sizing: border-box;
}
.screen-inner {
max-width: 640px;
width: 100%;
display: flex;
flex-direction: column;
align-items: center;
}
.brand {
font-family: 'Outfit', sans-serif;
font-size: 10px;
font-weight: 500;
letter-spacing: 5px;
text-transform: uppercase;
color: var(--text-dim);
margin-bottom: 36px;
text-align: center;
}
.sentence-front {
font-size: 32px;
font-weight: 700;
color: var(--text);
text-align: center;
line-height: 1.55;
margin: 60px 0 40px;
max-width: 92%;
word-break: keep-all;
overflow-wrap: break-word;
letter-spacing: 1px;
}
.sentence-back {
font-size: 24px;
font-weight: 500;
color: var(--text-secondary);
text-align: center;
line-height: 1.5;
margin: 16px 0 8px;
max-width: 92%;
word-break: keep-all;
overflow-wrap: break-word;
letter-spacing: 0.5px;
}
.divider {
width: 240px;
max-width: 70%;
position: relative;
display: flex;
align-items: center;
justify-content: center;
margin: 28px 0 18px;
}
.divider::before {
content: '';
position: absolute;
top: 50%; left: 0; right: 0;
height: 1px;
background: linear-gradient(90deg, transparent, var(--line), transparent);
}
.diamond {
position: relative;
color: var(--gold);
font-size: 7px;
background: var(--bg);
padding: 0 14px;
z-index: 1;
}
.section-label {
font-family: 'Inter', sans-serif;
font-size: 9px;
font-weight: 500;
letter-spacing: 4px;
text-transform: uppercase;
color: var(--muted);
margin: 4px 0 16px;
text-align: center;
}
.block {
width: 100%;
max-width: 560px;
margin-bottom: 8px;
}
.word-header {
font-family: 'Noto Sans JP', sans-serif;
font-size: 26px;
font-weight: 700;
color: var(--gold-light);
text-align: center;
letter-spacing: 2px;
margin-bottom: 24px;
}
.sentence-bracketed {
font-family: 'Noto Sans JP', sans-serif;
font-size: 18px;
font-weight: 500;
color: var(--gold-light);
text-align: center;
letter-spacing: 0.5px;
line-height: 1.7;
margin-bottom: 24px;
max-width: 95%;
margin-left: auto;
margin-right: auto;
word-break: keep-all;
overflow-wrap: break-word;
}
.audio-row {
margin: -8px 0 20px;
text-align: center;
}
.audio-row:empty {
margin: 0;
}
ul.bullets, ul.bullets ul {
list-style: none;
padding-left: 0;
margin: 4px 0;
}
ul.bullets > li {
position: relative;
padding: 6px 0 6px 20px;
color: var(--text);
font-size: 15.5px;
line-height: 1.65;
margin-top: 6px;
}
ul.bullets > li::before {
content: '';
position: absolute;
left: 4px; top: 16px;
width: 5px; height: 5px;
background: var(--gold);
transform: rotate(45deg);
}
ul.bullets > li > .label {
font-weight: 500;
color: var(--gold-light);
letter-spacing: 0.5px;
}
ul.bullets > li > ul {
margin-top: 4px;
padding-left: 4px;
}
ul.bullets > li > ul > li {
position: relative;
padding: 3px 0 3px 18px;
color: var(--text-secondary);
font-size: 15px;
line-height: 1.65;
font-weight: 400;
margin-top: 0;
}
ul.bullets > li > ul > li::before {
content: '';
position: absolute;
left: 4px; top: 13px;
width: 4px; height: 1px;
background: var(--text-dim);
}
@media (max-width: 420px) {
.screen { padding: 22px 16px 50px; }
.sentence-front { font-size: 24px; margin: 36px 0 28px; }
.sentence-back { font-size: 19px; line-height: 1.5; }
.word-header { font-size: 22px; }
.sentence-bracketed { font-size: 15px; }
ul.bullets > li { font-size: 14.5px; }
ul.bullets > li > ul > li { font-size: 14px; }
.brand { margin-bottom: 24px; }
}
@media (max-width: 340px) {
.sentence-front { font-size: 21px; }
.sentence-back { font-size: 17px; }
.sentence-bracketed { font-size: 14px; }
}
"""

QFMT = """<div class="screen">
<div class="screen-inner">
<div class="brand">Japanese Immersion Lab</div>
<div class="sentence-front">{{kanji:Sentence}}</div>
<div class="audio-row">{{SentenceAudio}}</div>
</div>
</div>"""

AFMT = """<div class="screen">
<div class="screen-inner">
<div class="brand">Japanese Immersion Lab</div>
<div class="sentence-back">{{kanji:Sentence}}</div>
<div class="divider"><span class="diamond">&#9670;</span></div>
<div class="section-label">Target word</div>
<div class="block">
<div class="word-header">{{WordHeader}}</div>
<div class="audio-row">{{WordAudio}}</div>
<ul class="bullets">
<li><span class="label">Meaning</span><ul>{{WordMeaning}}</ul></li>
<li><span class="label">Kanji breakdown</span><ul>{{WordBreakdown}}</ul></li>
<li><span class="label">English equivalents</span><ul>{{WordEnglish}}</ul></li>
</ul>
</div>
<div class="divider"><span class="diamond">&#9670;</span></div>
<div class="section-label">Sentence breakdown</div>
<div class="block">
<div class="sentence-bracketed">{{Sentence}}</div>
<div class="audio-row">{{SentenceAudio}}</div>
<ul class="bullets">
<li><span class="label">Meaning</span><ul>{{SentenceMeaning}}</ul></li>
<li><span class="label">Chunk breakdown</span><ul>{{SentenceBreakdown}}</ul></li>
<li><span class="label">Natural translations</span><ul>{{SentenceEnglish}}</ul></li>
</ul>
</div>
</div>
</div>"""

model = genanki.Model(
    MODEL_ID,
    'Japanese Sentence + English Breakdown',
    fields=[
        {'name': 'Sentence'},
        {'name': 'WordHeader'},
        {'name': 'WordMeaning'},
        {'name': 'WordBreakdown'},
        {'name': 'WordEnglish'},
        {'name': 'SentenceMeaning'},
        {'name': 'SentenceBreakdown'},
        {'name': 'SentenceEnglish'},
        {'name': 'WordAudio'},
        {'name': 'SentenceAudio'},
    ],
    templates=[{'name': 'Card 1', 'qfmt': QFMT, 'afmt': AFMT}],
    css=CARD_CSS,
)

def lis(items):
    return ''.join(f'<li>{x}</li>' for x in items)

# ─── Fill this list with one dict per card ────────────────────────
# `sentence` requires 漢字[かんじ] furigana brackets on every kanji word.
# All other fields are plain English text (chunk breakdowns lead with
# the Japanese chunk followed by an English explanation). Audio fields
# are generated automatically below — don't add them to CARDS.
CARDS = [
    {
        'sentence': 'データを格納[かくのう]するためのフォルダを作成[さくせい]した。',
        'word_header': '格納（かくのう）',
        'word_meaning': [
            'To store items or data in a designated place.',
            'Carries a nuance of organised, systematic storage — common in IT and technical contexts.',
        ],
        'word_breakdown': [
            '格: frame, category, fixed position',
            '納: to put away, to deliver into storage',
            'Overall: to put something away neatly in its designated place.',
        ],
        'word_english': ['store', 'house', 'put away', 'file (data)'],
        'sentence_meaning': [
            'The speaker made a folder whose purpose is to hold data.',
            'Neutral, slightly technical register — typical of a work log or report.',
        ],
        'sentence_breakdown': [
            'データを: the data (を marks the object of 格納する)',
            '格納するための: for the purpose of storing (ための modifies the following noun)',
            'フォルダを: a folder (object of 作成した)',
            '作成した: created (plain past of 作成する)',
            'Overall: I created a folder to store the data in.',
        ],
        'sentence_english': [
            'I created a folder for storing the data.',
            'I made a folder to house the data.',
            'I set up a folder where the data could be filed.',
        ],
    },
    # ... append one dict per word here
]

deck = genanki.Deck(DECK_ID, 'Japanese Sentence + English Breakdown')  # rename per topic
media_files = set()

if not ELEVENLABS_API_KEY:
    print("ELEVENLABS_API_KEY not set — building without audio.")

for c in CARDS:
    word_audio_file = tts_mp3(strip_word_reading(c['word_header']))
    sentence_audio_file = tts_mp3(strip_furigana(c['sentence']))
    if word_audio_file:
        media_files.add(os.path.join(MEDIA_DIR, word_audio_file))
    if sentence_audio_file:
        media_files.add(os.path.join(MEDIA_DIR, sentence_audio_file))

    note = genanki.Note(
        model=model,
        fields=[
            c['sentence'],
            c['word_header'],
            lis(c['word_meaning']),
            lis(c['word_breakdown']),
            lis(c['word_english']),
            lis(c['sentence_meaning']),
            lis(c['sentence_breakdown']),
            lis(c['sentence_english']),
            f'[sound:{word_audio_file}]' if word_audio_file else '',
            f'[sound:{sentence_audio_file}]' if sentence_audio_file else '',
        ],
    )
    deck.add_note(note)

output_path = '/mnt/user-data/outputs/japanese_vocab_deck.apkg'  # rename per topic
genanki.Package(deck, media_files=list(media_files)).write_to_file(output_path)
print(f"Wrote {len(CARDS)} cards to {output_path}" + ("" if ELEVENLABS_API_KEY else " (no audio)"))
```
