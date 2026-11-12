# Builds and updates the learner's known-words list: ~/JIL-decks/known/known_words.json
# {"lemmas": {lemma: source}, "chunks": {chunk: source}}. A word is "known" once it's in a card you've started
# (any card that isn't still new), or in a deck you finished, or on your manual list.
#
#   python scripts/known_words.py --anki                 read your Anki collection (close Anki or sync first)
#   python scripts/known_words.py --apkg "Hikaru 500.apkg" --all    every note in a deck file counts (e.g. a finished starter deck)
#   python scripts/known_words.py --add 食べる 本当         add words by hand (chunks too: --add-chunk というわけで)
#   python scripts/known_words.py --show                 print counts
# build_deck.py also adds every new deck's words automatically, so the next video never re-teaches them.
# Everyone starts from the Hikaru 500: every word in every Hikaru 500 sentence is always on the list (hikaru500_words.json),
# even after the learner deletes the Hikaru 500 cards they already knew (Harry's rule).
import glob, json, os, re, shutil, sqlite3, sys, tempfile, zipfile
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402

KDIR = os.path.join(jp.DATA, "known"); os.makedirs(KDIR, exist_ok=True)
KFILE = os.path.join(KDIR, "known_words.json")


SEED = os.path.join(os.path.dirname(os.path.abspath(__file__)), "hikaru500_words.json")


def load():
    k = json.load(open(KFILE, encoding="utf-8")) if os.path.exists(KFILE) else {"lemmas": {}, "chunks": {}}
    if jp.config().get("hikaru500", True):  # set "hikaru500": false in config.json if you never did the Hikaru 500
        for l in json.load(open(SEED, encoding="utf-8"))["lemmas"]:
            k["lemmas"].setdefault(l, "hikaru500")
    return k


def save(k):
    json.dump(k, open(KFILE, "w", encoding="utf-8"), ensure_ascii=False, indent=0)


def strip(s):
    s = re.sub(r"<rt>.*?</rt>", "", s or "")
    s = re.sub(r"\[sound:[^\]]*\]", "", s)
    s = re.sub(r"<[^>]+>", " ", s)
    return re.sub(r"&[a-z]+;", " ", s)


def from_collection(path, only_started=True, label="anki"):
    db = sqlite3.connect(path)
    q = "select n.flds, n.tags from notes n" + ("" if not only_started else " where exists (select 1 from cards c where c.nid = n.id and c.type != 0)")
    lem, ch = {}, {}
    for flds, tags in db.execute(q):
        text = strip(flds.replace("\x1f", "\n"))
        for l in jp.lemmas_in(text):
            lem.setdefault(l, label)
        if "chunk" in (tags or "").split():  # JIL deck-maker chunk cards keep the chunk in the Word field (field 4)
            parts = flds.split("\x1f")
            if len(parts) > 3:
                ch.setdefault(strip(parts[3]).strip(), label)
    return lem, ch


def find_anki_collections():
    roots = [os.path.join(os.environ.get("APPDATA", ""), "Anki2"),
             os.path.expanduser("~/Library/Application Support/Anki2"), os.path.expanduser("~/.local/share/Anki2")]
    return [p for r in roots for p in glob.glob(os.path.join(r, "*", "collection.anki2"))]


def main():
    a = sys.argv[1:]
    k = load(); added_l = added_c = 0
    def merge(lem, ch):
        nonlocal added_l, added_c
        for l, s in lem.items():
            if l not in k["lemmas"]: k["lemmas"][l] = s; added_l += 1
        for c, s in ch.items():
            if c and c not in k["chunks"]: k["chunks"][c] = s; added_c += 1
    if "--anki" in a:
        cols = find_anki_collections()
        if not cols: sys.exit("Couldn't find an Anki collection. Is Anki installed on this computer?")
        for col in cols:
            tmp = tempfile.mkdtemp()
            for ext in ("", "-wal"):
                if os.path.exists(col + ext): shutil.copyfile(col + ext, os.path.join(tmp, "c.anki2" + ext))
            lem, ch = from_collection(os.path.join(tmp, "c.anki2"), True, "anki")
            print(f"{col}: {len(lem)} words from cards you've started")
            merge(lem, ch)
    if "--apkg" in a:
        p = a[a.index("--apkg") + 1]; tmp = tempfile.mkdtemp()
        with zipfile.ZipFile(p) as z:
            name = "collection.anki21" if "collection.anki21" in z.namelist() else "collection.anki2"
            z.extract(name, tmp)
        lem, ch = from_collection(os.path.join(tmp, name), "--all" not in a, os.path.basename(p))
        print(f"{os.path.basename(p)}: {len(lem)} words")
        merge(lem, ch)
    if "--add" in a:
        for w in a[a.index("--add") + 1:]:
            if w.startswith("--"): break
            merge({l: "manual" for l in (jp.lemmas_in(w) or {w})}, {})
    if "--add-chunk" in a:
        for w in a[a.index("--add-chunk") + 1:]:
            if w.startswith("--"): break
            merge({}, {w: "manual"})
    save(k)
    print(f"Known-words list: {len(k['lemmas'])} words, {len(k['chunks'])} chunks (+{added_l} words, +{added_c} chunks) -> {KFILE}")


if __name__ == "__main__":
    main()
