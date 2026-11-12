---
name: jil-sentence-breakdown
description: Japanese Immersion Lab sentence-by-sentence breakdown turns a Japanese YouTube video into a study page that splits it line by line, each with a play button for just that line's audio, the Japanese with furigana, the English, the chunks in it, and hover-for-meaning on every word. Use whenever the user wants to "break down this video", "go through this video sentence by sentence", a "sentence breakdown", "make the breakdown page", or step 3 of Harry's one-video method. Runs in Claude Code (it runs Python and downloads the video's audio on the user's computer). Built on Harry's method (Oojiman / JIL); pairs with jil-deck-maker and jil-coach.
---

# JIL sentence breakdown

This is step 3 of Harry's one-video method. After learning every word in the video (the deck-maker), the learner goes through it **sentence by sentence**:
1. Read each line and understand it.
2. Replay just that line's audio over and over until their ear gets used to it.
3. Move to the next line, through the whole video.

This skill builds the page they do that on.

**Where things live:**
- The scripts are in this skill's `scripts/` folder. Run them from the skill folder with the user's Python.
- The learner's data lives in `~/JIL-decks/`, or the folder in the `JIL_DECKS_DIR` environment variable. That's the same place as the deck-maker, so the page can mark the words they don't know yet.
- Each breakdown goes in `~/JIL-decks/breakdowns/<name>/`.

## First time only
- **Garbled Japanese on Windows:** if the scripts' output shows garbled Japanese, run them with `PYTHONIOENCODING=utf-8` set (and without `-I`).
- **Then:** run `python scripts/setup_check.py`. It needs the same four packages as the deck-maker (fugashi, unidic-lite, genanki, yt-dlp) and prints the `pip install` line if any are missing.

## Making a breakdown
1. **Prepare:** `python scripts/prepare.py <YouTube URL> --name <short-name>`.
   - It downloads the Japanese subtitles (with each line's start and end time) and the audio, and splits every line into words.
   - It writes the lines as batch files of 40 in `breakdowns/<name>/todo/`, each split into word pieces with the grammar meanings already filled in.
   - **Long video, or a first try:** use `--from 0 --to 300` (seconds) for just part of it. The method's videos are about 15 minutes, which is around 500-700 lines.
   - **Subtitle file instead of a link:** give `--audio <video or audio file>` as well.
2. **Fill in the English, batch by batch.** For each `todo/lines_NN.json`:
   - write a compact `todo/fill_NN.json`
   - run `python scripts/apply_fill.py <name> NN` until it reports 0 problems

   `references/fill-rules.md` has the format. For each line:
   - fix the word pieces where the tokeniser got them wrong
   - give every word piece its meaning in that line
   - fill `translation` and `chunks`

   Follow `references/fill-rules.md` exactly. Work through the lines in order, so you have the context: who's talking, what's happening in the video. Don't skip any batch file.
3. **Build the page:** `python scripts/build_page.py <name>`.
   - It checks everything first: English on every line, a meaning on every word, pieces that spell each line exactly, and chunks that really appear in their line.
   - It lists anything to fix. Fix it all and build again until it reports nothing missing.
   - It writes `breakdowns/<name>/index.html`. The page needs the `audio.m4a` (or similar) file sitting next to it.
4. **Tell the learner:**
   - where the page is, and to open it in their browser (double-click `index.html`)
   - how to use it (below)

## How the learner uses the page
- **▶** plays just that line. **Loop line** repeats it until they turn it off. **0.85× / 0.7×** slows it down.
- **Keys:** Space replays the current line, ↓ / ↑ go to the next / previous line and play it, L toggles looping.
- **Words:**
  - Hover over any word (tap on a phone) for its meaning and dictionary form.
  - Words they don't know yet are underlined, if their known-words list exists.
- **Chunks** in each line show in red under the English.
- **Furigana / English toggles:** turning English off blurs it, so they can test themselves; hovering shows it.
- **Tick each line** once their ear has it. The page remembers the ticks in that browser.
- **Harry's order:** go line by line, replaying each until the ear gets it. Then watch the whole video and check they understand every second. Then put it on passive repeat until they're close to 100%.

## Rules
- **The learner's ear does the learning.** The English and the meanings are there to make each line fully understood, so their ear can then focus on the sound. They're not something to study for their own sake.
- **Translate what's actually said,** in natural English, in the speaker's register. See `references/fill-rules.md`.
- **Their data stays on their computer.** The audio and page are for their own study; don't upload them anywhere.
