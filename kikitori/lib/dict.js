// English glosses from JMdict (via jmdict-simplified). Run `npm run setup-dict` once
// to download it; without it the app still works, words just have no meaning yet.
import fs from "node:fs";
import path from "node:path";
import { DICT, readJSON, writeJSON } from "./store.js";

const INDEX = path.join(DICT, "index.json");
let index = null;

function buildIndex() {
  const src = fs.readdirSync(DICT).find((f) => /^jmdict-eng.*\.json$/.test(f));
  if (!src) return {};
  console.log(`[dict] building index from ${src} (one-time, ~30s)...`);
  const raw = JSON.parse(fs.readFileSync(path.join(DICT, src), "utf8"));
  const out = {};
  // Common words first so they win when a spelling has several entries.
  const words = [...raw.words].sort(
    (a, b) => Number(isCommon(b)) - Number(isCommon(a)),
  );
  for (const w of words) {
    const reading = w.kana?.[0]?.text || "";
    const sense = w.sense?.[0];
    if (!sense) continue;
    const gloss = w.sense
      .slice(0, 2)
      .map((s) => s.gloss.slice(0, 3).map((g) => g.text).join(", "))
      .join("; ");
    const pos = sense.partOfSpeech?.[0] || "";
    const entry = [reading, gloss, pos];
    for (const k of w.kanji || []) if (!out[k.text]) out[k.text] = entry;
    for (const k of w.kana || []) if (!out[k.text]) out[k.text] = entry;
  }
  writeJSON(INDEX, out);
  console.log(`[dict] indexed ${Object.keys(out).length} spellings`);
  return out;
}

function isCommon(w) {
  return (w.kanji || []).some((k) => k.common) || (w.kana || []).some((k) => k.common);
}

export function loadDict() {
  if (index) return index;
  index = readJSON(INDEX, null) || buildIndex();
  return index;
}

export function lookup(word) {
  const e = loadDict()[word];
  return e ? { reading: e[0], gloss: e[1], dictPos: e[2] } : null;
}

export const hasDict = () => Object.keys(loadDict()).length > 0;
