# Checks everything the deck-maker needs and says exactly what's missing. Safe to run any time.
# Usage: python scripts/setup_check.py
import importlib.util, os, subprocess, sys
NEED = {"fugashi": "fugashi", "unidic_lite": "unidic-lite", "genanki": "genanki", "yt_dlp": "yt-dlp"}
missing = [pip for mod, pip in NEED.items() if importlib.util.find_spec(mod) is None]
print(f"Python {sys.version.split()[0]} at {sys.executable}")
if sys.version_info < (3, 9):
    print("Python 3.9 or newer is needed.")
if missing:
    print("Missing packages: " + ", ".join(missing))
    print("Install with:  " + f'"{sys.executable}" -m pip install ' + " ".join(missing))
else:
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import jp
    ok = [t.surface for t in jp.tagger("日本語を勉強しています")]
    print("Japanese tokeniser works: " + " | ".join(ok))
    print(f"Your deck-maker folder: {jp.DATA}")
    print(f"Settings: {jp.config()}  (edit {os.path.join(jp.DATA, 'config.json')} to change)")
    k = os.path.join(jp.DATA, "known", "known_words.json")
    print("Known-words list: " + ("found (unknown words will be underlined)" if os.path.exists(k) else "none yet (the jil-deck-maker skill builds it; without it no words are underlined)"))
    print("ElevenLabs key file: " + ("found" if os.path.exists(os.path.join(jp.DATA, ".env")) else "none (only needed for card audio)"))
    print("All set." if not missing else "")
