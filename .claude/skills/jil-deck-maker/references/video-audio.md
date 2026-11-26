# The creator's own voice on the cards

When the learner gives you the video's audio, every card can carry the creator saying its word. Where the creator's real line is easy enough, it becomes the card itself. This is on when `video_front` is `"mix"` in `config.json`. With `"off"`, the cards keep only the written sentence and use ElevenLabs audio, if any.

## What the cards look like
- **Mixed front:** the creator's line is the front when it follows the one-new-word rule (at most about 1 card in 10 with one extra new word). The back shows his line with furigana, his clip and the English. Then the target word, then the written sentence under "Example sentence" with its breakdown.
- **Written front:** everywhere else, the written sentence is the front, and his clip sits on the back under "From the video", with the line and the word in bold.
- **No clip:** a word you can't place in the audio with confidence gets no clip. That's better than a wrong one.

Harry's rule still decides: the real line is used only when it already passes, and the written sentence stays on every card.

## Steps
1. **Get the audio.** Ask the learner for the video's audio or the video itself (MP3, M4A or MP4), downloaded on their own device. YouTube often refuses downloads from scripts.
   - Never upload it anywhere.
   - Clips and the built deck stay out of git.
2. **Word timings:** `python scripts/video.py words <name> <file>`. Whisper (the small model) takes about 20 seconds per minute of audio on a laptop CPU. It needs `faster-whisper` and ffmpeg.
3. **Clips:** `python scripts/video.py clips <name>`.
   - It cuts about 2 seconds either side of the word, snapped to whole words and nearby sentence edges.
   - Whisper often spells or mishears words differently (熟伏 for 祝福, ハズレ for 外れ). For each word it can't find, read words.json around the card's `t`. If the word is clearly there in another spelling, add `"video_forms": ["that spelling"]` to the card and run it again. If not, leave the card without a clip.
4. **Fix the line text.** `video_line` is Whisper's text, and it's shown on the card.
   - Correct every mishearing in it, using the YouTube subtitles and the context. A learner must never see a misheard word.
   - Set `video_word` to the word as it appears in the corrected line, for the bold.
5. **Pick the fronts** (only with `"video_front": "mix"`). For each card whose line can work, add:
   - `"video_cut": [start, end]`: two pieces of the raw Whisper text (as in words.json) where the front clip starts and ends. Pick a natural phrase of about 3 to 15 words that contains the target. Start and end between words.
   - `"video_front"`: that phrase as it should read, correctly spelled, in the creator's real words, with Japanese punctuation. Use … where it continues.
   - `"video_en"`: natural English for it.

   Skip a line when:
   - it has two or more other unknown words
   - you're not sure what he says
   - it's garbled
   - another card already uses it (no two cards share a front)

   Then run `python scripts/video.py fronts <name>`.
6. **Check by ear:** `python scripts/video.py verify <name>`.
   - It re-transcribes each front clip next to the card text. Homophones are fine (交換度 for 好感度).
   - If the heard text differs in substance, fix `video_front` to match what's said, or remove `video_cut` and `front` so that card keeps its written front.
   - Run `fronts` again after any change.
7. **Check the cards:** run `check_cards.py`. It applies the one-new-word rule to every front line too. If the deck was built before, check against the known-words list from before this deck, because its words are already on the list.
8. **Build** as usual. On a front-from-video card, his clip is the card's main audio.
