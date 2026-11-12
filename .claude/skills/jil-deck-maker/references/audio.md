# Card audio (optional)

Audio is nice, not essential. Harry never had audio on his cards; with enough immersion you hear the words all day anyway.

## Front or back
- **Back (default):** for people who want to read properly. The reading stays the main thing they grade.
- **Front:** for people going for listening and speaking speed. They still see kanji constantly, so they won't end up unable to read; they'll just forget readings a bit more.

Set it in `~/JIL-decks/config.json`: `"audio": "back"`, `"front"` or `"off"`.

## The API key: in a file, never in a chat
1. The learner makes an ElevenLabs account and an API key.
2. **They** create the file `~/JIL-decks/.env` with one line: `ELEVENLABS_API_KEY=their_key_here`.
3. You never ask for the key, never paste it anywhere, and never print it. The script reads it from the file.
4. If they paste a key into the chat anyway, don't use it or repeat it. Tell them to delete that key in ElevenLabs, make a new one, and put the new one in the file.

## Voicing
- `python scripts/make_audio.py <name>` voices every card's sentence into `decks/<name>/audio/`.
- `--only 1-5` makes the first few, so they can test the voice before doing the whole deck.
- **The model:** the default is `eleven_v3`. It reads Japanese kanji far more reliably than `eleven_multilingual_v2`, which misreads a noticeable share of sentences (店 as てん, for example).
- **The voice:** the default `voice_id` is a natural young Japanese male voice. They can swap in any Japanese voice from their ElevenLabs library: let them choose by ear.
- **A misread word:** run `--kana 7,12` to re-voice those cards from their kana reading, which removes the guesswork.
- **Cost:** about one ElevenLabs credit per character. A 150-card deck is roughly 3,000-4,000 characters.

Then rebuild with `build_deck.py` so the audio goes into the deck.
