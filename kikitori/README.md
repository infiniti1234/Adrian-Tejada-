# Kikitori 聞き取り

A local web app for learning Japanese from native videos. Paste a YouTube link and it:

1. **Downloads** the video (yt-dlp)
2. **Extracts** the audio (ffmpeg)
3. **Transcribes** it: uses the video's own Japanese subtitles if it has them, otherwise Whisper
4. **Tokenizes** every line into words (kuromoji)
5. **Matches** the words against your decks, so you see which ones are new
6. **Cuts** a real audio clip for every line

Then you commit it to your **Library** and study it on the **Watch** page:

- The video plays with a synced transcript beside it; click any line to jump there.
- Under the video, the current line is broken into word cards (reading, meaning, part of speech), each with **Known / Shaky / Unknown** buttons.
- Toggles: Show English, Hide known words, Hide meanings, and Pause after each line.
- Word lists split into **New**, **Shaky** and **Known / in your decks**. Select words, then **Add to my vocab** or **Export for Anki** (.tsv).
- **Drill flashcards** gives spaced repetition (Again / Hard / Good / Easy / 1 Month / Mastered). Each card plays the real line from the video the word came from.

Keyboard shortcuts on the Watch page: `space` play/pause, `r` replay line, `shift + ←/→` previous/next line. In the drill: `space` reveals the card, `1`–`6` grade it.

## Setup

You need **Node.js 18+**, **ffmpeg** and **yt-dlp**. Whisper is only needed for videos without Japanese subtitles.

```bash
# macOS (Homebrew)
brew install node ffmpeg yt-dlp

# Windows (winget)
winget install OpenJS.NodeJS FFmpeg yt-dlp.yt-dlp
```

Then, in this folder:

```bash
npm install
npm run setup-dict      # one-time: downloads the English dictionary (JMdict, ~60 MB)
npm start               # open http://localhost:4321
```

### Whisper (for videos without subtitles)

Pick one:

- **openai-whisper** (simplest): `pip install -U openai-whisper`. Set `WHISPER_MODEL` (`small` by default; `medium` is more accurate but slower).
- **whisper.cpp** (fast, uses the GPU on Macs): build it, download a model such as `ggml-large-v3-turbo.bin`, then start the app with
  `WHISPER_CPP_MODEL=/path/to/ggml-large-v3-turbo.bin npm start`.
  If the binary isn't called `whisper-cli`, also set `WHISPER_CPP_BIN`.

To use Whisper even when a video has subtitles, set `KIKITORI_FORCE_WHISPER=1`.

### English translations (optional)

Start the app with an Anthropic API key and every committed video gets an English translation of each line. Words the dictionary doesn't have also get short meanings.

```bash
ANTHROPIC_API_KEY=sk-ant-... npm start
```

Translation starts automatically when you commit a video. For videos already in your library, press **Add English** on the Watch page. It uses `claude-opus-5-5` with low effort; set `KIKITORI_MODEL` to change the model.

### Voice for single words (optional)

The ▶ next to a word speaks it with [VOICEVOX](https://voicevox.hiroshiba.jp/) if it's running (free; the desktop app or `docker run -p 50021:50021 voicevox/voicevox_engine`). Otherwise it uses your browser's Japanese voice. Settings: `VOICEVOX_URL` (default `http://127.0.0.1:50021`) and `VOICEVOX_SPEAKER` (default `3`).

## Your decks

Put word lists in `data/decks/`: one word per line in a `.txt` file, named after the deck. The Kaishi 1.5k list from this repo is already included as `kaishi-1.5k.txt`. Add as many as you like (for example a `hikaru.txt`). Restart the app, or call `POST /api/decks/reload`.

A word counts as **known** if it's in any deck, or if you marked it Known. Marking it Shaky or Unknown overrides the deck.

## Where things are stored

Everything lives in `data/` and is ignored by git:

| Path | What |
|---|---|
| `data/videos/<id>/` | Video, clips, `review.json` (transcript + words), `job.json` |
| `data/status.json` | Your Known / Shaky / Unknown marks |
| `data/vocab.json` | Words you added with "Add to my vocab" |
| `data/srs.json` | Flashcard progress |
| `data/dict/` | The dictionary |

You can also process a file already on your computer: paste its full path instead of a URL. If a `.srt` or `.vtt` file with the same name sits next to it, those subtitles are used.

## Credits

Inspired by Ryuji Bamford's immersion pipeline, shared in the Japanese Immersion Lab community. Dictionary data comes from [JMdict](https://www.edrdg.org/jmdict/j_jmdict.html) via [jmdict-simplified](https://github.com/scriptin/jmdict-simplified) (CC BY-SA 4.0).
