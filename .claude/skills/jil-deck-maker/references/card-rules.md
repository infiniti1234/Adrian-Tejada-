# How to write the cards (Harry's rules)

## 1. Pick what gets a card
Read `unknown.json` together with the transcript lines.
- **Skip names:** people, places, shops, brands, and titles of games, shows and products (妖怪ウォッチ, モンスターハンター, こびとづかん, ハローキティ).
  - **Everyday kanji words that appear in a title still count:** 伝説 (legend), 脱出 (escape), 観察 (observation). Give them a card, with a sentence of their own.
  - **Loanwords that only appear inside titles** (ワールド, プリンセス, コレクション) get a card only if the video also uses them on their own, or they're everyday Japanese words.
- **Merge fragments.** The tokeniser sometimes splits a katakana word into pieces. The pieces are never cards; the whole word is (if it's a real word and not a title).
  - アルティ + メット → アルティメット
  - ガ + チャ → ガチャ
  - アイ + ビス → アイビス (a shop name, so skip it)
- **Fix wrong word types.** ガチャ may come out tagged as an adverb. Teach it as what it is in the video: gacha, a capsule-toy machine or lottery.
- **Kansai or slang words** (ほんま for 本当, ちゃう for 違う, おもろい, あかん, ええ): give each a card if the creator says it a lot, with `"dialect": true` and a note like "Kansai for 本当 (really)".
  - **Endings and in-between sounds** (やん, へん, とる, や for だ) get no card: like particles, they come from listening. A Kansai set phrase the creator keeps using (なんでやねん) can be a chunk card.
  - The learner's language parent decides their accent, so these are worth knowing if their creator uses them.
  - The tokeniser often misreads Kansai forms (おもろい as 面白い, あかん as two pieces). `"dialect": true` stops the checker treating those cards as already known.
- **Rare or specialist words still get a card.** The method is to learn every word in the video. Put them later in the order, after the common ones.
- **Add the chunks** the video uses (see `chunks.md`): どっちかというと, って感じ, しかない, という訳で, って事は. Give each its own card with `"kind": "chunk"`.
  - **Idioms count as chunks** (念のため, 手を抜く, ハードルを上げる), even when they carry a new word.
- **Order:** most frequent and most useful first. A word that later cards will lean on should come early.
- **Record what you skipped** in `decks/<name>/skip.json`: `[{"lemma": "蛾", "reason": "fragment"}, {"lemma": "妖怪", "reason": "title"}, ...]`.
  - Use the `lemma` exactly as it appears in `unknown.json`.
  - Reasons `"name"`, `"title"`, `"fragment"` and `"sound effect"` are ignored on every future video.

## 2. The one-new-word rule (checked by `check_cards.py`)
- **Each card teaches exactly one new thing:** its `target` word or chunk.
- **Everything else in the sentence must be:**
  - a word on the learner's known-words list, or
  - a word taught by an earlier card in this deck, or
  - grammar: particles, です/だ, ます and all conjugations, ない/た/たい/て/てる/ちゃう, お/ご, suffixes like さん/たち/個, numbers, interjections, and the grammar items しまう, ておく, かもしれない, すぎる, ください, ございます, みたい, 〜そう, よう.
- **One extra new word is allowed** when a one-new-word sentence would feel forced. Keep that to about 1 card in 10. Two or more extra words fails.
- **If the real line from the video already passes the rule,** use it: that's the best possible card. Usually it won't (real lines are full of unknown words), so write a new one.

## 3. What a good sentence is
- **In the style of the video.** The same kind of Japanese you'd hear in it: same register (casual or polite), same world (a gacha video's cards are about gacha, prices, prizes, 店長), same kind of reactions. The learner should feel the card came from the video.
- **Clear, self-contained context.** A beginner must be able to fully understand it from the English. No guessing what "this" is, no vague lines from titles or lyrics.
- **Varied.** Mix questions, exclamations and statements, short and longer lines, casual and polite where the video has both. Don't start lots of cards the same way.
- **Natural.** If a native wouldn't say it, rewrite it. Standard spelling; kanji where it's normally written in kanji.
- **Short enough to read in one go:** usually 3 to 12 words.
- **The target appears in the sentence.** Conjugated is fine (買う → 買っちゃった).

## 4. The card fields (`cards.json`)
A JSON array, one object per card, in teaching order:
```json
[
 {"target": "大当たり", "kind": "word", "sentence": "やった、大当たりだ！", "reading": "おおあたり",
  "meaning": "jackpot, top prize", "translation": "Yes, it's the jackpot!", "note": "", "t": 312},
 {"target": "って感じ", "kind": "chunk", "sentence": "今日はゆっくりって感じかな。", "reading": "",
  "meaning": "kind of like..., a vibe of...", "translation": "Today's more of a chill kind of day.",
  "note": "って感じ softens what you say: 'it's like...', 'kind of...'.", "t": 95}
]
```
- **`target`:** write it exactly as it appears in the sentence where you can (for a chunk, always), so the right words get marked. For a conjugated word, use the dictionary form (買う for 買っちゃった).
- **`kind`:** `"word"` or `"chunk"`.
- **`reading`:** hiragana for the target. Leave it empty for kana-only words.
- **`meaning`:** the target's meaning as used here, 1 to 6 words.
- **`translation`:** natural English for the whole sentence.
- **`note`:** one short line, only when needed: a conjugation, a particle, a casual ending, "Kansai for X". Plain English. Empty otherwise.
- **`t`:** seconds into the video where the word is first said. Use `first_t` from `unknown.json`, not `first` (that's a line number). Optional.
- **`dialect`:** `true` for Kansai or slang forms (see section 1). Leave it out otherwise.

## 4b. Japanese definitions (from about 4,000 words)
When `definitions` in the settings is `"japanese"` (Harry's switch point is about 4,000 words):
- **`meaning` becomes a short Japanese definition** of the target as used in this sentence, in plain everyday Japanese, the way a native would explain it to a friend.
- **Words with a plain one-for-one English equivalent keep English** (most nouns: 太陽 = the sun). Japanese definitions are for words without one: near-synonyms that share an English word, feeling words, nuance.
- **Every word in the definition must be known.** If a definition needs one or two words the learner doesn't know, give each of those its own card first (earlier in the deck) with an English meaning. Never define them in Japanese too: that's the endless loop. If a definition would need more than two unknown words, use English for that word.
- `translation` and `note` stay in English.
- `check_cards.py` checks every Japanese definition against the known-words list and the cards before it.

## 4c. The breakdowns (every card)
Every card is a JIL Sentence + Breakdown card: the back has a **Target word** section and a **Sentence breakdown** section, each with Meaning, Breakdown and English equivalent. Each card in `cards.json` carries six lists of plain-text lines (no HTML, no markdown, no dashes), on top of the fields in section 4:

```json
{"target": "全て", "kind": "word", "sentence": "今日は全て100円です！", "reading": "すべて", "meaning": "everything",
 "translation": "Everything is 100 yen today!", "note": "",
 "wm": ["all, everything, every one of them", "A bit more formal than 全部: common on shop signs and in announcements."],
 "wb": ["全：all, whole", "て：part of the word (okurigana)", "Whole: all of it."],
 "we": ["everything", "all", "every single one"],
 "sm": ["Today, every item costs 100 yen.", "A shop announcing a sale."],
 "sb": ["今日は：today (は marks the topic)", "全て：everything", "100円です：is 100 yen (polite)", "Whole: today everything is 100 yen."],
 "se": ["Everything is 100 yen today!", "Today, it's all 100 yen!", "All items are 100 yen today!"]}
```

**With English definitions (the default, until about 4,000 words):** everything in plain, short English; Japanese appears only as the parts being explained.
- **`wm` (word, Meaning): 2 lines.** What it means; then how it's used (register, when people say it, how it differs from a similar word). A chunk's `wm` says what the chunk does.
- **`wb` (word, Breakdown): its parts, then `Whole: ...`.** One line per kanji or meaningful part, `part：meaning` (full-width colon). Katakana words: one line with the origin (`ガチャ：from "gachapon", the sound of the capsule machine`), no Whole line. Pure hiragana words: one line saying what it is, or `[]` if there's nothing useful. Chunks: split into their pieces, then Whole.
- **`we` (word, English equivalent): 2 to 4 short items,** lower-case, no full stop.
- **`sm` (sentence, Meaning): exactly 2 lines.** A plain rewording of what the sentence says; then a short context label (`A reaction while opening a gacha.`).
- **`sb` (sentence, Breakdown): 2 to 6 chunks, then `Whole: ...`.** Split the sentence in order into chunks of a word plus its particle, each `chunk：gloss`, with a few words in brackets when a form needs naming (`(て-form of 使う)`). **The chunks joined in order must spell the sentence** (the checker tests it).
- **`se` (sentence, English equivalent): exactly 3 natural English sentences.** The first is `translation`, word for word; the other two say it differently.

**With Japanese definitions** (`"definitions": "japanese"`, from about 4,000 words): the same six lists, written like Harry's own 国語 cards. English stays only in `we` and `se`.
- **`wm`: 2 lines.** A plain-Japanese definition like a learner's 国語辞典 entry, usually ending in 「〜こと。」 or 「〜様子。」; then the nuance or typical use (「『集まる』より少し文学的な響き。」). **Every word in the first line must be known** (section 4b's rule; the checker tests it). If it can't be done with known words, write the card with English definitions instead.
- **`wb`: parts, then `全体：...。`** `大：大きい、すごい` · `当たり：くじなどが当たること` · `全体：すごく良い当たり。` Katakana: the origin (`サンクス：英語のthanks（ありがとう）`).
- **`we`: 2 to 4 English items,** as above.
- **`sm`: 2 lines.** A paraphrase in simpler Japanese ending 「…という意味。」; then a context label (「ガチャ動画でよく聞く喜びの言葉。」).
- **`sb`: chunks `チャンク：やさしい言い換え`, then `全体：…ということ。`** The chunks must spell the sentence.
- **`se`: 3 English sentences,** the first being `translation`.
- One layer deep: an unknown word inside a definition gets its own card earlier, with English explanations, never another Japanese definition.

## 5. Before you build
Run `check_cards.py` and fix every FAIL (including the breakdown checks). If a card fails only because the tokeniser misreads a word (it happens with katakana and compounds), rephrase so the word reads cleanly rather than weakening the rule. Then read the cards once more as a native speaker would: anything unnatural, ambiguous or off-style gets rewritten, even if it passed.
