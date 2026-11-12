---
name: jil-speech-compare
description: Japanese Immersion Lab speech comparison compares how a learner speaks Japanese with the native speaker they copy (their language parent). It transcribes both with Whisper, measures the differences that text can show (sentence length and chaining, endings, fillers, chunks, politeness, speed), and gives 5 to 10 of the easiest fixes, each quoting the speaker's real lines. Use whenever the user wants to "compare my speaking", "compare me to my language parent", "how do I sound more native", "what should I fix in my Japanese speaking", "analyse my recording", or Harry's AI speech comparison. Runs in Claude Code (it runs Python and Whisper on the user's computer). Built on Harry's method (Oojiman / JIL); pairs with jil-coach.
---

# JIL speech comparison

Harry's method for sounding natural: record yourself talking, compare it with the native speaker you copy, and fix the few biggest, easiest differences first. This skill does the comparison. `references/compare-method.md` has the rules for reading the transcripts and writing the fixes: follow it exactly.

**Where things live:**
- The scripts are in this skill's `scripts/` folder. Run them from the skill folder with the user's Python.
- The learner's files go in `~/JIL-decks/speech/` (or the folder in the `JIL_DECKS_DIR` environment variable), the same place as the deck-maker:
  - `speech/<name>/audio/` and `speech/<name>/transcripts/` for each person
  - `speech/<speaker>/profile.md` for what sets the speaker apart
  - `speech/compare/<me>-vs-<speaker>/` for the numbers and the report

## First time only
- **Garbled Japanese on Windows:** run the scripts with `PYTHONIOENCODING=utf-8` set.
- Run `python scripts/setup_check.py`. It needs fugashi, unidic-lite, yt-dlp and faster-whisper, plus ffmpeg, and prints exactly what to install. It also says whether Whisper will run on an NVIDIA GPU (fast, large-v3 model) or the CPU (the medium model; slower, roughly as long as the audio).

## Comparing
1. **Ask the learner for two things** (explain the recording tips in `references/compare-method.md` if they haven't recorded yet):
   - **Their recording:** 20-40 minutes of them talking alone, unedited, no script (a video or audio file on their computer).
   - **Their speaker:** 3 or more YouTube videos of their language parent talking alone, 30+ minutes in total. If a video has other people in it, ask which part is just the speaker, and use `--from`/`--to`.
   - A short name for each (e.g. `me`, `hina`).
2. **Collect the audio:**
   - `python scripts/get_audio.py --who speaker --name <speaker> <link> <link> ...`
   - `python scripts/get_audio.py --who me --name <me> <file>`
   - Add more later with the same command; `--from 60 --to 900` keeps only part of a single source.
3. **Transcribe:** `python scripts/transcribe.py <speaker>` and `python scripts/transcribe.py <me>`. It skips anything already done, so a speaker only gets transcribed once. Tell the learner how long it'll take before you start, and run it in the background if it's long.
4. **Measure:** `python scripts/analyse.py --me <me> --speaker <speaker>`. It writes `stats.md` and `stats.json` in `speech/compare/<me>-vs-<speaker>/`.
5. **Read and write the report**, following `references/compare-method.md`:
   - Read `stats.md`, then the speaker's `transcripts/*.txt` and the learner's, in full.
   - If `speech/<speaker>/profile.md` exists, read it first; if not, write it.
   - Write `report.md` in the compare folder: what makes the speaker sound like themselves, then the 5 to 10 easiest fixes, each quoting the speaker's real lines and the learner's.
6. **Tell the learner** the fixes in the chat (short), where the full report is, and the next step: practise for 1-2 weeks, record again, run it again.

## Rules
- **The speaker is the standard, not you.** Every fix points to lines the speaker really says. Never suggest Japanese the speaker doesn't use.
- **5 to 10 easy fixes, not every difference.**
- **No pronunciation, pitch or accent fixes from text.** Say plainly that the transcripts can't show those.
- **Only download what's needed:** the speaker videos' audio and nothing else. Keep everything in the learner's speech folder.
- **Their recordings are private.** Never upload them anywhere; everything runs on their computer.
