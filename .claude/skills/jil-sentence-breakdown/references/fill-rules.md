# How to fill in the breakdown

Each `todo/lines_NN.json` holds up to 40 lines. Each line looks like this:
```json
{"id": 57, "start": "1:42", "text": "3枚入れなあかんのか！", "translation": "", "chunks": [],
 "words": [{"text": "3", "base": "", "reading": "", "gloss": ""}, {"text": "枚", "base": "枚", "reading": "まい", "gloss": ""}, ...]}
```
Fill `translation`, `chunks`, and a `gloss` for every word piece. Work through the lines in order so you have the context.

## How to fill: a compact fill file, then `apply_fill.py`
Don't hand-edit the big todo files. For each batch:
1. Read `todo/lines_NN.json` to see the pre-split pieces.
2. Write `todo/fill_NN.json`, with one entry per line id:
   ```json
   {"57": {"t": "You have to put in three bills?!",
           "c": [["なあかん", "have to (Kansai)"]],
           "w": [["3"], ["枚", "counter for bills"], ["入れ", "to put in", "入れる"], ["な", "if not (Kansai)"],
                 ["あかん", "no good (Kansai for だめ)"], ["の", "explaining"], ["か", "question marker"], ["！"]]}}
   ```
   - **`t`:** the English.
   - **`c`:** chunks, as `[text, meaning]`.
   - **`w`:** every piece in order, as `[text, gloss, base, reading, "name"]`. Only text and gloss are required:
     - `base` defaults to the text
     - `reading` is filled in automatically for kanji pieces; give one only to correct it (一闇 → ひとやみ)
     - add `"name"` as the 5th item for names
   - **Pieces already right?** Use `"g": {"piece": "gloss"}` instead of `w` to add glosses without re-listing the pieces.
3. Run `python scripts/apply_fill.py <name> NN`. It writes your fill into the todo file and lists anything still wrong. Fix the fill file and run it again until it reports 0 problems.

## Word pieces (`words`)
- **The tokeniser has split the line into pieces.** Numbers are already merged (3,000). The pieces are often wrong, so fix them. Its most common mistakes:
  - **Everyday words split in two:** いつ+も, で+も, それ+と+も, と+か, けど+も, どう+も, 何+か, か+な, お+店, お+客+様. Merge them when they're one word in this line. The exception is a real で+も, like 家でも ("even at home").
  - **Kansai や read as the verb やる:** やった = だった, やって, やて = だって.
  - **Wrong dictionary forms:** ハズレる for ハズレ, 入れ for 入れる. Fix `base`.
  - **Also often wrong:**
  - **katakana words** (ガチャ split into ガ + チャ)
  - **Kansai and slang** (あかん, こん = 来ん, いらん, えげつい)
  - **contractions** (入っとん, やて)
  - **names and titles**
- **Fix the pieces** whenever they don't match real words. Merge pieces into one, or split one into two. **The pieces must still spell the line exactly, in order:** `build_page.py` checks this.
- **Each piece:**
  - **`gloss`:** what this piece means **in this line**, 1-6 words.
    - Verbs: "to buy", "to pull (a gacha)".
    - Adjectives: "expensive".
    - Nouns: "price".
  - **`base`:** its dictionary form (買った → 買う). For Kansai forms, the Kansai form itself (あかん).
  - **`reading`:** hiragana, only when the piece has kanji. Fix it if it's wrong: こん read as これ, 一闇 is ひとやみ.
- **Grammar pieces with one usual meaning** (は, を, です, ます, て, ない...) come pre-filled. Check each one fits this line.
- **Fill the ones that change with context yourself:** に, で, と, の, か, な, って, や, し.
  - な can be the な in 大事な, a sentence-final "huh", or the な in なあかん.
  - に and で after a na-adjective make it an adverb (完全に = "completely").
- **Kansai and contracted forms:** give the meaning with the standard form in brackets.
  - "is …ing (Kansai for ている)"
  - "not (Kansai for ない)"
  - "won't come (Kansai for 来ない)"
- **Names:**
  - Pieces marked `"name": true` are names of people, places, shops, games or products. Leave the gloss empty and the page labels them "name", or write a short "name: a game" if that helps.
  - Merge a name the tokeniser has split, and add `"name": true` yourself (パズドラ, アイビス, 妖怪ウォッチ).
- **Pieces with no letters** (punctuation, numbers, symbols) need no gloss.
- **Merged pieces:** `base` is the dictionary form of the whole (やって来 → やって来る). If it isn't a conjugating word, `base` is just the text.
- **Speaker labels** at the start of a line (ま：, 店：):
  - make the label one piece (text and base both ま：) with the gloss "speaker label"
  - in the translation, use the speaker's role if you can tell it (店 = the shop staff, "Staff:"), otherwise leave the label out

## Translation (`translation`)
- **What it's for:** making the line fully understood, so the learner can then concentrate on hearing it.
- **Say what the speaker means, in natural English and their tone.** A YouTuber reacting sounds casual ("No way, it's actually here!"), not stiff.
- **Stay close to the Japanese:** the same order of ideas where English allows. Don't add explanations or summarise.
- **Subtitle lines are often fragments of a longer sentence.** Translate the fragment so it makes sense in context, and use "…" where it continues on the next line.
- **Lines with nothing to translate:**
  - laughter (ｗｗ) → "(laughs)"
  - an on-screen product name or code → the name as it is, e.g. "FC GAME BOX 2 (product name)"
  - never leave a translation empty
- **Garbled auto-subtitles:** translate what was most likely said, and add "(subtitle unclear)".

## Chunks (`chunks`)
- **What they are:** small pieces of Japanese between a phrase and grammar, the building blocks of sounding Japanese.
  - Examples: どっちかというと, というわけで, って感じ, しかない, ってことは, かもしれない, と言っても過言ではない, 念のため, 手を抜く.
- **Format:** list the ones in the line: `[{"text": "って感じ", "meaning": "kind of like…"}]`.
  - `text` must appear in the line exactly, as one unbroken piece (`build_page.py` checks this). A pattern with another word in the middle (あれば…大丈夫, と…思う) can't be a chunk here.
  - `meaning` is 2-8 words.
- **What counts:** set pieces people reach for when they talk. Idioms count.
  - **〜てみる, 〜てしまう, 〜ておく count** only when they really mean "try", "end up" or "in advance".
- **What doesn't count:**
  - single words, particles, plain conjugations
  - Kansai verb endings (入っとん, 言っとった): those are explained in the word pieces
  - Kansai set phrases do count: なんでやねん, 言っちゃ何やけど, ちゃうやん
- **Most lines have no chunk.** That's fine: leave `[]`.

## Before you build
Run `build_page.py`. If it lists things to fix, fix them all and build again until it says nothing's missing.
