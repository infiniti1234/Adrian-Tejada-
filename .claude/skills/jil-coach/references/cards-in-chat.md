# Making cards in a normal chat (the free-Claude kit's card prompt)

When a member gives you a video's subtitles and their known-words list and asks for cards, follow everything below exactly. It's written as the member's request to you.

---

You're making Anki sentence cards for me from a Japanese video, using Harry's method from the Japanese Immersion Lab. I've attached:
1. The video's subtitles (a text file).
2. My known-words file: every word I already know, one per line.
If I also attach an export of an old Anki deck, every word in it counts as known too.

First, ask me two things if I haven't said them: a short name for this video (like "gacha-01"), and whether my cards are dark or light. My master deck is called "Japanese" unless I tell you otherwise.

IF I ATTACH A WORD LIST INSTEAD OF SUBTITLES
My phone note of words that popped out while I listened, one per line (sometimes with the sentence I heard it in). Every word on it that isn't in my known-words file gets a card; ask me first whether to drop any one-offs I don't care about. There's no video, so write each sentence in the style of my main creator (ask me who) and use the sentence I noted, if there is one, for its world. Use the note's name as the video name.

WHAT GETS A CARD
- Every word and chunk in the subtitles that isn't in my known-words file. A word I know in another form still counts as known (買う covers 買った and 買っちゃった). A compound is its own word: knowing 当たる doesn't cover 大当たり.
- Skip names: people, places, shops, brands, and titles of games, shows and products. Everyday words inside a title still get a card.
- Skip grammar: particles, です/だ, ます and all conjugations, ない/た/たい/て/てる/ちゃう, お/ご, suffixes like さん/たち/個, numbers, interjections, and しまう, ておく, かもしれない, すぎる, ください, ございます, みたい, 〜そう, よう.
- Kansai or slang words the creator says a lot (ほんま, ちゃう for 違う, おもろい, あかん, ええ) get a card with a note like "Kansai for 本当 (really)". Endings and in-between sounds (やん, へん, とる, や for だ) don't get cards: those come from listening. They can still appear in sentences when the creator talks that way.
- Chunks get their own card: small pieces between a phrase and grammar, like どっちかというと, って感じ, しかない, というわけで, ってことは, and idioms like 念のため. A phrase made of words I know whose meaning I couldn't guess from the parts (気になる) is a chunk card too.
- Rare or specialist words still get a card, later in the order. Put the most frequent and useful words first, and put any word a later sentence needs before that sentence.

Before batch 1, work out the full list of targets in order, tell me how many cards there will be in total, then give me batch 1.

THE ONE-NEW-WORD RULE
- Each card teaches exactly one new thing: its target word or chunk.
- Everything else in the sentence must be a word from my known-words file, a word an earlier card in this deck teaches, or grammar from the list above.
- One extra new word is allowed only when a one-new-word sentence would feel forced, about 1 card in 10 at most. Never two.
- If the real line from the video already follows the rule, use it. Usually it won't, so write a new one.

A GOOD SENTENCE
- In the style of the video: same register (casual or polite), same world, same kind of reactions, so it feels like it came from the video.
- Clear, self-contained context I can fully understand from the English. No guessing what "this" is.
- Varied: questions, exclamations and statements, short and longer lines. Don't start lots of cards the same way.
- Natural: if a native wouldn't say it, rewrite it. Standard spelling, kanji where it's normally written in kanji. Usually 3 to 12 words.
- The target appears in the sentence (conjugated is fine).

THE BREAKDOWNS (every card)
Every card's back has a Target word section and a Sentence breakdown section, each with Meaning, Breakdown and English equivalent. Write them in plain, short English (Japanese only as the parts being explained):
- WordMeaning: 2 lines. What the target means; then how it's used (casual or polite, when people say it, how it differs from a similar word).
- WordBreakdown: its parts, then "Whole: ...". One line per kanji or meaningful part as part：meaning (full-width colon), e.g. 全：all, whole / て：part of the word (okurigana) / Whole: all of it. A katakana word gets one line with its origin (ナイス：from English "nice"), no Whole line. A pure hiragana word gets one line saying what it is, or nothing. A chunk is split into its pieces, then Whole.
- WordEnglish: 2 to 4 short English equivalents, lower-case, no full stop.
- SentenceMeaning: 2 lines. A plain rewording of what the sentence says; then a short context label (A reaction while opening a gacha.).
- SentenceBreakdown: the sentence split in order into 2 to 6 chunks (a word plus its particle), each chunk：gloss, with a few words in brackets when a form needs naming ((て-form of 使う)), then "Whole: ...". The chunks joined in order must spell the whole sentence.
- SentenceEnglish: exactly 3 natural English sentences. The first is the Translation, word for word; the other two say it differently.
Each of these six fields is a list: write every line as <li>line</li>, one after another, with nothing between them.

JAPANESE DEFINITIONS (only if I ask for them, from about 4,000 words)
The same six fields, written like a learner's 国語辞典, and the card shows 意味（日本語で） and 分解（日本語） instead of Meaning and Breakdown:
- WordMeaning: a plain Japanese definition of the target as used in the sentence (usually ending 「〜こと。」 or 「〜様子。」), then its nuance or typical use, in Japanese.
- WordBreakdown: parts in Japanese (大：大きい、すごい), then 全体：... in Japanese.
- SentenceMeaning: a simpler Japanese paraphrase ending 「…という意味。」, then a Japanese context label.
- SentenceBreakdown: chunks as チャンク：やさしい言い換え, then 全体：…ということ。
- WordEnglish and SentenceEnglish stay in English.
- Put 1 in the JP column on every card.
- Every word in the first WordMeaning line must be known. If one or two aren't, give each its own card first, earlier in the deck, with English explanations (never another Japanese definition). If more than two would be needed, give that word English explanations and leave its JP column empty.

HOW TO GIVE ME THE CARDS
Give me the cards in batches of 15, each batch in one code block, in exactly this format. Lines starting with # appear only in the first batch. Use "(dark)" or "(light)" in the notetype line to match my answer.

#separator:Pipe
#html:true
#notetype:JIL Sentence + Breakdown (dark)
#deck:Japanese
#tags:jil free-claude
#columns:Number|Sentence|SentenceFurigana|Word|Reading|WordMeaning|WordBreakdown|WordEnglish|SentenceMeaning|SentenceBreakdown|SentenceEnglish|Note|JP|Kind|Source
gacha-01 001|やった、大当たりだ！|やった、<ruby>大当<rt>おおあ</rt></ruby>たりだ！|大当たり|おおあたり|<li>the jackpot: the top prize in a gacha or lottery</li><li>Shouted when the best prize comes out.</li>|<li>大：big</li><li>当たり：a win, a hit</li><li>Whole: the big win.</li>|<li>jackpot</li><li>top prize</li><li>big win</li>|<li>They just won the best prize.</li><li>A reaction while opening a gacha.</li>|<li>やった、：yes! (past of やる, "did it")</li><li>大当たりだ！：it's the jackpot (だ = is)</li><li>Whole: yes, it's the jackpot!</li>|<li>Yes, it's the jackpot!</li><li>I hit the jackpot!</li><li>Yes! Top prize!</li>|||word|gacha-01

One card per line, these 15 fields separated by | and never a | inside a field:
- Number: the video name, a space, and a 3-digit card number (gacha-01 001, gacha-01 002...).
- Sentence: the Japanese sentence.
- SentenceFurigana: the same sentence with every kanji word wrapped as <ruby>漢字<rt>かんじ</rt></ruby>, the reading over the kanji only, with okurigana and kana prefixes outside: <ruby>食<rt>た</rt></ruby>べる, お<ruby>金<rt>かね</rt></ruby>, <ruby>助<rt>すけ</rt></ruby>っ<ruby>人<rt>と</rt></ruby>. Double-check every reading.
- Word: the target as it appears in the sentence (for a conjugated word, its dictionary form).
- Reading: the target's reading in hiragana. Empty if the target is all kana.
- WordMeaning, WordBreakdown, WordEnglish, SentenceMeaning, SentenceBreakdown, SentenceEnglish: as in THE BREAKDOWNS above.
- Note: one short line only when needed (a conjugation, a particle, a casual ending, "Kansai for X"). Empty otherwise.
- JP: empty, or 1 with Japanese definitions.
- Kind: word or chunk.
- Source: the video name.

After each batch, tell me how many cards are left and wait for me to say "next". After the last batch, give me every target from this deck, one per line, in one code block headed "Add these to your known-words file".

Before each batch, check every card against the one-new-word rule and the readings once more, and fix anything that breaks it. Under each batch, write one line: "Checked: one new word per card, readings checked, chunks spell each sentence", plus any card you had to fix and why.
