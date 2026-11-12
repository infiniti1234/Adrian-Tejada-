# Finds every word in the video the learner doesn't know yet: decks/<name>/unknown.json, most frequent first,
# each with its dictionary form, reading, part of speech, how often it's said and up to 3 lines it appears in.
# Also prints how much of the video the learner already knows (by word count), and lists names separately.
# Usage: python scripts/extract.py <deck-name>
import collections, json, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import jp  # noqa: E402
import known_words  # noqa: E402

def main():
    name = sys.argv[1]
    d = os.path.join(jp.DATA, "decks", name)
    tr = json.load(open(os.path.join(d, "transcript.json"), encoding="utf-8"))
    known = known_words.load()
    kl = set(known["lemmas"])
    ignore = set(known.get("ignore", {}))
    words = collections.OrderedDict(); names = collections.Counter(); total = known_hits = 0
    for i, line in enumerate(tr["lines"]):
        for t in jp.tagger(line["text"]):
            if jp.is_free(t): continue
            f = t.feature
            if f.pos1 not in jp.CONTENT_POS: continue
            if f.pos2 == "固有名詞":
                names[t.surface] += 1; continue
            total += 1
            lem = f.lemma or t.surface
            if lem in kl or lem in ignore:
                known_hits += 1; continue
            if lem in ignore: continue
            w = words.setdefault(lem, {"lemma": lem, "form": f.orthBase or t.surface, "reading": jp.hira(f.kanaBase or ""),
                                       "pos": f.pos1, "count": 0, "first": i, "first_t": line["t"], "lines": []})
            w["count"] += 1
            if len(w["lines"]) < 3 and line["text"] not in [x["text"] for x in w["lines"]]:
                w["lines"].append({"t": line["t"], "text": line["text"]})
    unknown = sorted(words.values(), key=lambda w: (-w["count"], w["first"]))
    known_chunks_heard = [c for c in known["chunks"] if any(c in l["text"] for l in tr["lines"])]
    cov = known_hits / total * 100 if total else 0
    json.dump({"title": tr["title"], "url": tr["url"], "known_before_pct": round(cov, 1), "unknown": unknown, "names": dict(names.most_common()),
               "known_chunks_heard": known_chunks_heard},
              open(os.path.join(d, "unknown.json"), "w", encoding="utf-8"), ensure_ascii=False, indent=0)
    print(f"{tr['title']}: {total} words said, you know {cov:.0f}% of them.")
    print(f"{len(unknown)} words you don't know yet (said {sum(w['count'] for w in unknown)} times) -> {os.path.join(d, 'unknown.json')}")
    print("Most common unknown words: " + ", ".join(f"{w['form']}({w['count']})" for w in unknown[:25]))
    if names: print("Names (no cards): " + ", ".join(list(names)[:15]))
    if len(unknown) > 250:
        print("Note: that's a lot of new words for one video. A shorter or easier video may suit your level better; the deck will still work.")

if __name__ == "__main__":
    main()
