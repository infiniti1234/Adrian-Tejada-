# Voices every card's sentence with ElevenLabs into decks/<name>/audio/NNN.mp3 (optional; run before build_deck.py).
# The API key is read from a FILE, never typed into a chat or onto the command line:
#   ~/JIL-decks/.env  containing one line:  ELEVENLABS_API_KEY=your_key_here
# Voice and model come from ~/JIL-decks/config.json ("voice_id", "tts_model"; eleven_v3 reads Japanese kanji far more
# reliably than eleven_multilingual_v2). Already-made clips are skipped, so it's safe to re-run.
# Usage: python scripts/make_audio.py <deck-name> [--only 1-5] [--redo 7,12] [--kana 7,12]
#   --kana N,...  re-voice those cards from their kana reading (fixes a misread kanji)
import json, os, sys, time, urllib.request, urllib.error
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402


def api_key():
    p = os.path.join(jp.DATA, ".env")
    if os.path.exists(p):
        for line in open(p, encoding="utf-8"):
            if line.strip().startswith("ELEVENLABS_API_KEY=") and line.split("=", 1)[1].strip():
                return line.split("=", 1)[1].strip().strip('"')
    sys.exit(f"No ElevenLabs key found. Create the file {p} with one line: ELEVENLABS_API_KEY=your_key_here "
             "(never paste the key into a chat).")


def tts(text, voice, model, out, key):
    body = json.dumps({"text": text, "model_id": model, "voice_settings": {"stability": 0.5, "similarity_boost": 0.75}}).encode()
    req = urllib.request.Request(f"https://api.elevenlabs.io/v1/text-to-speech/{voice}?output_format=mp3_44100_128", data=body,
                                 headers={"xi-api-key": key, "Content-Type": "application/json"})
    open(out, "wb").write(urllib.request.urlopen(req, timeout=120).read())


def nums(flag):
    if flag not in sys.argv: return set()
    return {int(x) for x in sys.argv[sys.argv.index(flag) + 1].split(",") if x.strip()}


def main():
    name = sys.argv[1]; cfg = jp.config(); key = api_key()
    d = os.path.join(jp.DATA, "decks", name); os.makedirs(os.path.join(d, "audio"), exist_ok=True)
    cards = json.load(open(os.path.join(d, "cards.json"), encoding="utf-8"))
    lo, hi = (map(int, sys.argv[sys.argv.index("--only") + 1].split("-")) if "--only" in sys.argv else (1, len(cards)))
    redo, kana = nums("--redo") | nums("--kana"), nums("--kana")
    made = 0
    for i, c in enumerate(cards, 1):
        if not lo <= i <= hi: continue
        out = os.path.join(d, "audio", f"{i:03d}.mp3")
        if os.path.exists(out) and i not in redo: continue
        text = jp.kana_text(c["sentence"]) if i in kana else c["sentence"]
        for attempt in range(3):
            try:
                tts(text, cfg["voice_id"], cfg["tts_model"], out, key); made += 1; break
            except urllib.error.HTTPError as e:
                msg = e.read()[:200].decode("utf-8", "ignore")
                if e.code in (401, 403): sys.exit(f"ElevenLabs refused the key ({e.code}). Check the key in your .env file. {msg}")
                if e.code == 402 or any(w in msg.lower() for w in ("paid_plan", "paid plan", "subscription", "quota_exceeded", "quota")):
                    sys.exit(f"ElevenLabs says this needs a paid plan or more credits ({e.code}): {msg} Try a default voice, or upgrade your ElevenLabs plan. Audio is optional, so you can also build the deck without it.")
                print(f"retry card {i}: {e.code} {msg}"); time.sleep(3)
            except Exception as e:
                print(f"retry card {i}: {e}"); time.sleep(3)
        time.sleep(0.2)
    print(f"Voiced {made} cards into {os.path.join(d, 'audio')}. Listen to a few before building the deck.")


if __name__ == "__main__":
    main()
