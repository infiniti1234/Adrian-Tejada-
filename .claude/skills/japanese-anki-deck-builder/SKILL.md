---
name: japanese-anki-deck-builder
description: Builds beautifully styled Japanese Anki decks (.apkg files) with example sentences and full English breakdowns. Use this skill whenever the user asks for a Japanese Anki deck, Japanese flashcards, a vocab deck, sentence cards, or wants to study Japanese words — whether they provide a word list OR just name a topic (e.g. "make me a deck about ordering at restaurants", "business email vocab", "JLPT N2 verbs"). Trigger even if they don't say "Anki" explicitly but want downloadable/importable Japanese study cards. Every card gets an original example sentence on the front and a structured two-part English breakdown on the back, packaged with genanki in the JIL Sentence Card aesthetic.
---

# Japanese Anki Deck Builder

Builds a downloadable `.apkg` Anki deck from a Japanese word list or topic. Each card: clean example sentence on the front, structured two-part breakdown on the back (target word, then full sentence), with all explanations written in **English** (Japanese → English format).

## Accepting input

The user can give you either:

1. **A word list** — Japanese words or short phrases, one per line. Every word becomes exactly one card. Never truncate or sample from the list.
2. **A topic** — e.g. "vocabulary for going to the gym", "keigo for customer service", "JLPT N3 adverbs". Generate an appropriate word list yourself: default to **15 words** unless the user specifies a count, choose genuinely useful words for that topic, and match the difficulty to any level the user mentions (JLPT level, beginner/advanced, etc.). Show the word list briefly at the start of your reply so the user can see what made the cut, but do not wait for approval — start building immediately.

Do not ask permission to begin. When you receive a list or topic, confirm in one short line that you're building, then build.

## Known-words check

`references/known_words.txt` is a deduped, one-per-line list of words the user already knows, originally extracted from the **Kaishi 1.5k** deck in their exported `.colpkg` (their other decks are batch-mining source material, not studied vocab, so they're excluded). When generating a word list for a **topic** request, skip words already in that file and pick the next-best alternative instead, so decks don't re-teach words the user already has. Don't apply this filter to explicit user-provided word lists — if the user typed or pasted a word, make the card regardless of whether it's already known.

Every word that actually gets built into a card (whether user-supplied or topic-generated) must be appended to `references/known_words.txt` afterward, deduped and sorted, so it's treated as known in future builds — the deck being built now is the next thing the user will have studied.

## Card format

### Front

- One **original** example sentence containing the target word, written by you
- Shown clean — no readings, no furigana, no parentheses
- Realistic, modern context (business or daily life). Never pull sentences from textbooks or famous quotes. If the deck has a topic, sentences should live in that topic's world.

### Back (top to bottom)

1. The same sentence echoed, still clean
2. ◆ divider
3. Section label: **Target word**
4. The target word in big gold text with reading in full-width parens, e.g. 格納（かくのう）. Kana-only and katakana words still get the reading in parens for consistency.
5. Three bullet sections about the word:
   - **Meaning** — 1 to 3 short English bullets explaining what the word means, including nuance and typical usage contexts
   - **Kanji breakdown** — one bullet per kanji or component in `漢字: English gloss` format, plus a final `Overall: ...` bullet summarising how the parts add up to the whole word. For kana-only words, explain the word's origin or formation instead (e.g. mimetic/onomatopoeic nature, source loanword).
   - **English equivalents** — 2 to 4 short English equivalents (single words or short phrases)
6. ◆ divider
7. Section label: **Sentence breakdown**
8. The full sentence again with furigana in brackets in gold, e.g. データを格納[かくのう]するためのフォルダを作成[さくせい]した。
9. Three bullet sections about the sentence:
   - **Meaning** — 1 to 2 English bullets explaining what the sentence means and any nuance/register worth noting
   - **Chunk breakdown** — one bullet per chunk of the sentence in `chunk: English explanation` format (include grammar notes where useful, e.g. particles, verb forms), plus a final `Overall: ...` bullet paraphrasing the whole sentence in one line
   - **Natural translations** — 3 natural English translations that genuinely vary in phrasing and structure, not the same sentence reworded three times

## Hard rules

- **Furigana brackets on every kanji word** in the `sentence` field, formatted `漢字[かんじ]`. Every kanji-containing word gets brackets, including small/common ones. Anki's `{{kanji:Field}}` and `{{furigana:Field}}` template filters depend on this exact format. A space must precede each bracketed word where it follows kana or punctuation ambiguity would break furigana rendering — when in doubt, follow the pattern in the example card.
- **All explanations in English.** Meaning, breakdowns, and the Overall lines are plain English a self-studying learner can read. The only Japanese on the back is the sentence itself, the word header, and the chunk text on the left of each `chunk:` bullet.
- **The `Overall:` bullet is required** at the end of every breakdown section (both the kanji breakdown and the chunk breakdown).
- **Every word becomes a card.** No truncation, no sampling, no skipping hard words.
- **Keep all three sections** (Meaning, Breakdown, English equivalents/translations) on both the word part and the sentence part. Never simplify the format.
- **Do not change the CSS, colours, fonts, or layout.** The `position: fixed` wrapper and `!important` background rules in the template are deliberate — they force the black background edge-to-edge across Anki desktop and mobile.
- Do not add extra fields, card types, or tags unless the user asks.

## Build process

1. Draft all cards first (sentence + all eight fields per card).
2. Read `references/build_script.md` and use the script template there **verbatim** — only fill in the `CARDS` list, the deck name, and the IDs.
3. **Generate fresh random IDs per build**: replace `MODEL_ID` and `DECK_ID` with new large random integers (e.g. `random.randrange(1 << 30, 1 << 31)` run once, then hardcoded). Never reuse IDs across different decks — colliding model IDs with different templates corrupt existing decks in the user's Anki collection.
4. Name the deck and output file after the topic, e.g. `Restaurant Japanese — Sentence Cards` → `/mnt/user-data/outputs/restaurant_japanese_deck.apkg`.
5. If genanki isn't installed: `pip install genanki --break-system-packages --quiet`. Same for `requests` if missing (needed for audio).
6. If `ELEVENLABS_API_KEY` isn't already in the environment but a local `.env` exists, `source` it before running the script — never paste or hardcode the key into the script itself. If no key is available anywhere, tell the user up front that this build will ship without audio.
7. Run the script, then present the `.apkg` with the file presentation tool.
8. Append this build's target words to `references/known_words.txt` (dedupe against existing entries, keep sorted, one word per line).

## Audio

Cards include a `WordAudio` and `SentenceAudio` field (ElevenLabs TTS, `eleven_multilingual_v2`), generated automatically inside the build script — never call the ElevenLabs API from anywhere else or hand-write field values for these. Requires `ELEVENLABS_API_KEY` in the environment; the script degrades gracefully (empty audio fields, still valid deck) if it's missing or a request fails, so a blocked/rate-limited API never blocks the deck itself. Mention in the wrap-up whether audio was included.

## What to tell the user at the end

- A short summary: deck name, card count, and (for topic requests) the word list
- Any judgement calls flagged briefly: ambiguous readings you chose between, words with multiple senses where you picked one, register decisions — so the user can correct them
- One line on how to import: open the `.apkg` file with Anki (desktop or mobile) and it installs automatically

Keep the wrap-up tight. The deck is the deliverable, not an essay about it.
