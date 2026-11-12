---
name: jil-deck-maker
description: Japanese Immersion Lab deck-maker turns a Japanese YouTube video (or a subtitle/transcript file) into an Anki deck of sentence cards for every word and chunk the learner doesn't know yet, one new word per card, written in the style of that video, checked against the learner's known-words list, with optional ElevenLabs audio. Use whenever the user wants to "make a deck", "make cards from this video", "turn this video into Anki cards", "learn the words in this video", the one-video method's step 2, update their known-words list, or add audio to their cards. Runs in Claude Code (it needs to run Python on the user's computer). Built on Harry's method (Oojiman / JIL); pairs with jil-coach.
---

# JIL deck-maker

This is step 2 of Harry's one-video method: **learn every word in the video before you watch it properly.** You take a video of about 15 minutes, find every word and chunk the learner doesn't know, and write a sentence card for each. Each card teaches one new thing and uses only words they already know. The result is a deck they import into their master deck.

**Where things live:**
- The scripts are in this skill's `scripts/` folder. Run them with the user's Python, e.g. `python scripts/known_words.py --show`, from the skill folder.
- The learner's own data (decks, known-words list, settings, API key file) lives in `~/JIL-decks/`, so updating the skill never touches it.
  - To keep it somewhere else, set the environment variable `JIL_DECKS_DIR` to that folder before running the scripts.

## First time only
1. **Check the set-up:** run `python scripts/setup_check.py`. If packages are missing, it prints the exact `pip install` line. Run that, then check again.
2. **Build the known-words list:** `python scripts/known_words.py --anki`. Ask the user to close Anki (or sync) first. It reads their collection read-only and counts every word in any card they've started, in any deck (an old Core or textbook deck counts too). Every word in the Hikaru 500 is always on the list as everyone's starting point, even after the learner deletes Hikaru 500 cards they already knew.
   - **No Anki history yet, but they've finished the Hikaru 500:** `--apkg "path/to/Hikaru 500 (dark).apkg" --all` (or the light one: same cards).
   - **Words they know but haven't got in Anki:** `--add 食べる 本当`; chunks with `--add-chunk というわけで`.
3. **Settings** are in `~/JIL-decks/config.json`. `setup_check.py` creates it with the defaults:
   - `master_deck`: the name of their main Anki deck. New cards go straight into it.
   - `card_theme`: `"dark"` (black and gold, the default) or `"light"` (like the JIL Immersion Tracker: washi white, white card, flag red). It should match the Hikaru 500 look they chose.
   - `audio`: `"back"` (default), `"front"` or `"off"`.
   - `voice_id` / `tts_model`: for audio.

   - `definitions`: `"english"` (the default) or `"japanese"`. Switch to Japanese definitions at about 4,000 words (Harry's switch point); the rules are in `references/card-rules.md` section 4b.
   The first time, ask the user two things and set them: their master deck's name (the deck for their own cards; the Hikaru 500 stays its own deck), and whether they want their cards dark or light (the same look as their Hikaru 500). To switch later, change `card_theme`; cards already in Anki can be switched with Anki's Change Note Type.

## Making a deck from a video
1. **Get the transcript:** `python scripts/get_transcript.py <YouTube URL or .srt/.vtt/.txt file> --name <short-name>`.
   - It uses the creator's own Japanese subtitles, or auto-generated ones if that's all there is.
   - If there are none, ask for another video or a transcript file.
   - It warns if the video is much longer than about 15 minutes. Mention it; the method works best with a ~15-minute video.
2. **Find the unknown words:** `python scripts/extract.py <short-name>`.
   - It writes `decks/<name>/unknown.json`: every unknown word, most frequent first, with the lines it's said in.
   - It also prints how much of the video they already know. That figure is saved as `known_before_pct`, and `build_deck.py` repeats it at the end.
3. **Decide what gets a card.** Read `unknown.json` and the transcript, then follow `references/card-rules.md`:
   - **Skip:** names, game/product/shop titles, and tokeniser fragments.
   - **Merge:** fragments back into the real word.
   - **Add the chunks** from the video (see `references/chunks.md`). Chunks are as important as words.
   - **Order:** most useful first, roughly by how often the word is said.
   - **Write `decks/<name>/skip.json`** listing every skipped word with its reason. Names, titles and fragments are then ignored on every future video (see `references/card-rules.md`).
4. **Write the cards** into `decks/<name>/cards.json`, following `references/card-rules.md` exactly. In short:
   - one new word or chunk per card
   - everything else in the sentence is a word they know, a word from an earlier card in this deck, or grammar
   - a sentence in the same style as the video
   - clear context they'd fully understand
   - varied in length and type
   - **with its breakdowns:** every card's back is the JIL Sentence + Breakdown card (Target word: Meaning, Breakdown, English equivalent; Sentence breakdown: Meaning, chunk Breakdown, 3 English equivalents). Write the six lists `wm`, `wb`, `we`, `sm`, `sb`, `se` exactly as `references/card-rules.md` section 4c says, in English, or in Japanese when `definitions` is `"japanese"`. For a big deck, write them in batches of about 50 cards.
5. **Check them:** `python scripts/check_cards.py <name>`. Fix every FAIL by rewriting the sentence (never by weakening the rule), and rerun until it passes. Keep cards with one extra new word to about 1 in 10.
6. **Audio (optional):** see `references/audio.md`. Only if the user wants it and has an ElevenLabs key in `~/JIL-decks/.env`.
   - **Never ask them to paste a key into the chat.** If they paste one anyway, don't use it or repeat it, and tell them to delete it in ElevenLabs and make a new one.
   - Run `python scripts/make_audio.py <name>`, and have them listen to a few clips.
7. **Build:** `python scripts/build_deck.py <name>`.
   - It writes `decks/<name>/<name>.apkg` and adds the deck's words to the known-words list, so the next video won't re-teach them.
   - If the learner studied Japanese before the course, some cards may teach nothing new to them. Tell them what Harry says: in Anki, delete any card where they know every word and understand the whole sentence, the same as they did with the Hikaru 500.
   - Tell them where the file is and how to import it: double-click it, or File > Import in Anki.
   - Then remind them how to pace it: suspend the new cards and unsuspend 10 at a time, adding more while retention holds at about 75-80%.

## Making a deck from a word list (your phone note)
Harry's second word source: every word that pops out while they listen goes into one phone note, and every couple of weeks the whole note becomes a deck.
1. **Save the note** as a .txt file, one word per line (a line can also hold the sentence they heard it in). Then `python scripts/get_transcript.py <note.txt> --name notes-01`. The 15-minute check doesn't apply.
2. **Find the unknown words:** `python scripts/extract.py notes-01`. Words already on the known-words list drop out, so a word they noted twice or already learned won't get a second card.
3. **Skim the list with them first.** Ask whether any word is a one-off they don't care about, and delete those; a word from their niche stays.
4. **Write the cards** as in "Making a deck from a video", with two changes: there's no video, so write each sentence in the style of their main creator (ask who, if you don't know), and if they noted the sentence they heard the word in, use that world for the card. Leave `t` out.
5. **Check, add audio if they want it, and build** exactly as above. The cards go into the same master deck.

## What to tell the learner when it's done
- **The numbers:** how many cards, how many of them are chunks, and what share of the video they knew before.
- **What's next in the one-video method:**
  1. Learn the cards.
  2. Go through the video sentence by sentence, replaying each line until their ear gets it.
  3. Watch it in full and check every second.
  4. Keep it on passive repeat until they're close to 100%.
  5. Move to the creator's next video.
- **A lot of new words is normal:** the method is to learn every word in the one video, so the deck covers all of them.
- **Big decks are normal early on.** 150-250 cards from one video is fine: pacing happens in Anki, by unsuspending 10 at a time while retention holds. Each video after that needs fewer cards, because the creator keeps reusing the same words.

## Rules that never bend
- **The one-new-word rule** is checked by `check_cards.py`. Don't build a deck that fails it.
- **Cards are for words and chunks.** Particles, endings (standard or Kansai, like やん, へん, とる) and the little in-between sounds come from listening, not cards. Kansai words (ほんま, あかん) and set phrases can get cards.
- **No pitch accent on these cards** (only the Hikaru 500 shows pitch).
- **The learner's data and key stay on their computer.** Only read their Anki collection, never write to it. Never print or upload their API key.
