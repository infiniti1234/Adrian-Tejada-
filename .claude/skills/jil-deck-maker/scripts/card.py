# The JIL Sentence + Breakdown card (Harry, 11 Nov 2026). This file is the one source for the card: the deck-maker,
# the free-Claude kit's card types and the Hikaru 500 build all use it.
#: the layout of his own "JIL Sentence + Breakdown" note type
# (Russian Anki/japanese), used for every JIL deck. Back = sentence with furigana + audio, then TARGET WORD
# (header, pitch if any, Meaning / Breakdown / English equivalent) and SENTENCE BREAKDOWN (Meaning / Breakdown /
# English equivalent). Labels are English, or 意味（日本語で）/ 分解（日本語）when the JP field is filled
# (Japanese definitions from about 4,000 words). Two looks, same fields.
import html

FIELDS = ["Number", "Sentence", "SentenceFurigana", "Word", "Reading", "Pitch", "PitchPattern",
          "WordMeaning", "WordBreakdown", "WordEnglish", "SentenceMeaning", "SentenceBreakdown", "SentenceEnglish",
          "Note", "AudioFront", "Audio", "JP", "Source", "Kind", "SentencePlain"]

FONT = "@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+JP:wght@400;500;700&family=Inter:wght@400;500&display=swap');"

DARK_CSS = FONT + """
html, body, .card, .card.nightMode, .nightMode .card { margin:0; background:#0a0a0a; color:#e8e8e8; font-family:'Noto Sans JP','Yu Gothic','Hiragino Sans',sans-serif; -webkit-font-smoothing:antialiased; }
.screen { display:flex; flex-direction:column; align-items:center; padding:30px 20px 56px; box-sizing:border-box; min-height:100vh; }
.inner { width:100%; max-width:600px; display:flex; flex-direction:column; align-items:center; }
.brand { font-family:'Inter',sans-serif; font-size:9px; font-weight:500; letter-spacing:5px; text-transform:uppercase; color:#555; text-align:center; }
.num { font-family:'Inter',sans-serif; font-size:10px; color:#3a3a3a; margin-top:22px; }
.front { font-size:36px; font-weight:700; text-align:center; line-height:1.5; margin:56px 0 30px; max-width:92%; word-break:keep-all; overflow-wrap:break-word; }
.back { font-size:26px; font-weight:500; color:#d4b85a; text-align:center; line-height:2.0; margin:22px 0 4px; max-width:94%; word-break:keep-all; overflow-wrap:break-word; }
.back rt { font-size:0.45em; color:#bbb; }
.tw { color:inherit; }
.audio { margin-top:8px; }
.divider { width:240px; max-width:70%; position:relative; display:flex; justify-content:center; margin:26px 0 14px; }
.divider::before { content:''; position:absolute; top:50%; left:0; right:0; height:1px; background:linear-gradient(90deg,transparent,#2a2a2a,transparent); }
.diamond { position:relative; color:#c4a44a; font-size:7px; background:#0a0a0a; padding:0 14px; }
.section-label { font-family:'Inter',sans-serif; font-size:9px; font-weight:500; letter-spacing:4px; text-transform:uppercase; color:#666; margin:2px 0 14px; }
.block { width:100%; max-width:560px; }
.word-header { font-size:28px; font-weight:700; color:#d4b85a; text-align:center; letter-spacing:1px; }
.word-header rt { font-size:0.42em; color:#bbb; }
.pitch-row { text-align:center; margin-top:6px; }
.pitch { font-size:20px; letter-spacing:2px; }
.pitch span { display:inline-block; padding:4px 1px 0; border-top:2px solid transparent; }
.pitch .h { border-top-color:#e8e8e8; }
.pitch .d { border-right:2px solid #c0392b; }
.pattern { font-family:'Inter',sans-serif; font-size:11px; color:#777; margin-top:3px; text-align:center; }
ul.bullets, ul.bullets ul { list-style:none; padding-left:0; margin:4px 0; }
ul.bullets > li { position:relative; padding:6px 0 6px 20px; font-size:15.5px; line-height:1.65; margin-top:6px; }
ul.bullets > li::before { content:''; position:absolute; left:4px; top:16px; width:5px; height:5px; background:#c4a44a; transform:rotate(45deg); }
ul.bullets > li > .label { font-weight:500; color:#d4b85a; letter-spacing:.5px; }
ul.bullets > li > ul > li { position:relative; padding:3px 0 3px 18px; color:#ccc; font-size:15px; line-height:1.65; }
ul.bullets > li > ul > li::before { content:''; position:absolute; left:4px; top:13px; width:4px; height:1px; background:#555; }
.note { font-size:13.5px; color:#999; margin-top:14px; text-align:center; max-width:92%; line-height:1.6; }
@media (max-width:420px) { .front { font-size:26px; margin:38px 0 26px; } .back { font-size:21px; } .word-header { font-size:23px; } ul.bullets > li { font-size:14.5px; } ul.bullets > li > ul > li { font-size:14px; } }
"""

LIGHT_CSS = FONT + """
html, body, .card, .card.nightMode, .nightMode .card { margin:0; background:#f5f4f2; color:#1c1b1f; font-family:'Noto Sans JP','Yu Gothic','Hiragino Sans',sans-serif; -webkit-font-smoothing:antialiased; color-scheme:light; }
.screen { display:flex; flex-direction:column; align-items:center; box-sizing:border-box; min-height:100vh; padding:20px 14px 40px; }
.inner { width:100%; max-width:600px; background:#fff; border-radius:1.5rem; box-shadow:0 6px 20px rgb(28 27 31 / .05); padding:26px 20px 22px; box-sizing:border-box; display:flex; flex-direction:column; align-items:center; }
.brand { font-size:10px; font-weight:700; letter-spacing:.06em; text-transform:uppercase; color:#bc002d; text-align:center; }
.num { font-size:11px; color:#6b6970; margin-top:18px; }
.front { font-size:34px; font-weight:700; text-align:center; line-height:1.5; margin:44px 0 28px; max-width:94%; word-break:keep-all; overflow-wrap:break-word; }
.back { font-size:25px; font-weight:500; text-align:center; line-height:2.0; margin:18px 0 4px; max-width:94%; word-break:keep-all; overflow-wrap:break-word; }
.back rt { font-size:0.45em; color:#6b6970; }
.tw { color:inherit; }
.audio { margin-top:8px; }
.divider { display:flex; justify-content:center; margin:20px 0 10px; }
.diamond { display:block; width:10px; height:10px; border-radius:9999px; background:#bc002d; font-size:0; }
.section-label { font-size:10px; font-weight:700; letter-spacing:.12em; text-transform:uppercase; color:#6b6970; margin:2px 0 12px; }
.block { width:100%; max-width:540px; }
.word-header { font-size:28px; font-weight:700; color:#bc002d; text-align:center; }
.word-header rt { font-size:0.42em; color:#6b6970; }
.pitch-row { text-align:center; margin-top:6px; }
.pitch { font-size:20px; letter-spacing:2px; color:#1c1b1f; }
.pitch span { display:inline-block; padding:4px 1px 0; border-top:2px solid transparent; }
.pitch .h { border-top-color:#1c1b1f; }
.pitch .d { border-right:2px solid #bc002d; }
.pattern { font-size:11px; color:#6b6970; margin-top:3px; text-align:center; }
ul.bullets, ul.bullets ul { list-style:none; padding-left:0; margin:4px 0; }
ul.bullets > li { position:relative; padding:6px 0 6px 20px; font-size:15.5px; line-height:1.65; margin-top:6px; }
ul.bullets > li::before { content:''; position:absolute; left:5px; top:15px; width:7px; height:7px; border-radius:9999px; background:#bc002d; }
ul.bullets > li > .label { font-weight:700; color:#1c1b1f; }
ul.bullets > li > ul > li { position:relative; padding:3px 0 3px 18px; color:#3d3b40; font-size:15px; line-height:1.65; }
ul.bullets > li > ul > li::before { content:''; position:absolute; left:4px; top:13px; width:5px; height:1px; background:#a8a6ab; }
.note { font-size:13px; color:#6b6970; background:#fbeaee; border-radius:1rem; margin-top:14px; padding:8px 14px; text-align:center; max-width:92%; line-height:1.6; }
@media (max-width:420px) { .front { font-size:26px; margin:36px 0 24px; } .back { font-size:21px; } .word-header { font-size:23px; } ul.bullets > li { font-size:14.5px; } ul.bullets > li > ul > li { font-size:14px; } }
"""

LBL = {"m": ("意味（日本語で）", "Meaning"), "b": ("分解（日本語）", "Breakdown")}


def _label(k):
    jp, en = LBL[k]
    return "{{#JP}}" + jp + "{{/JP}}{{^JP}}" + en + "{{/JP}}"


def _section(prefix):
    return ('<ul class="bullets">'
            "{{#" + prefix + "Meaning}}<li><span class=\"label\">" + _label("m") + "</span><ul>{{" + prefix + "Meaning}}</ul></li>{{/" + prefix + "Meaning}}"
            "{{#" + prefix + "Breakdown}}<li><span class=\"label\">" + _label("b") + "</span><ul>{{" + prefix + "Breakdown}}</ul></li>{{/" + prefix + "Breakdown}}"
            "{{#" + prefix + "English}}<li><span class=\"label\">English equivalent</span><ul>{{" + prefix + "English}}</ul></li>{{/" + prefix + "English}}"
            "</ul>")


def templates(brand):
    qfmt = ('<div class="screen"><div class="inner"><div class="brand">' + brand + '</div>'
            '<div class="front">{{Sentence}}</div>{{AudioFront}}</div></div>')
    afmt = ('<div class="screen"><div class="inner"><div class="brand">' + brand + '</div>'
            '<div class="back">{{SentenceFurigana}}</div>'
            '<div class="audio">{{Audio}}</div>'
            '<div class="divider"><span class="diamond">&#9670;</span></div>'
            '<div class="section-label">Target word</div>'
            '<div class="block"><div class="word-header">{{Word}}</div>'
            '{{#Pitch}}<div class="pitch-row">{{Pitch}}</div><div class="pattern">{{PitchPattern}}</div>{{/Pitch}}'
            + _section("Word") + '</div>'
            '<div class="divider"><span class="diamond">&#9670;</span></div>'
            '<div class="section-label">Sentence breakdown</div>'
            '<div class="block">' + _section("Sentence") + '</div>'
            '{{#Note}}<div class="note">{{Note}}</div>{{/Note}}'
            '<div class="num">#{{Number}}{{#Source}} · {{Source}}{{/Source}}</div></div></div>')
    return qfmt, afmt


def li(items):
    """List of plain-text bullets -> <li> HTML for one field."""
    return "".join(f"<li>{html.escape(x)}</li>" for x in (items or []))
