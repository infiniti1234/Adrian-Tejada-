// Small JSON-file storage. Everything lives under data/ so it is easy to back up.
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

export const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
export const DATA = path.join(ROOT, "data");
export const VIDEOS = path.join(DATA, "videos");
export const DECKS = path.join(DATA, "decks");
export const DICT = path.join(DATA, "dict");

for (const d of [DATA, VIDEOS, DECKS, DICT]) fs.mkdirSync(d, { recursive: true });

export function readJSON(file, fallback) {
  try {
    return JSON.parse(fs.readFileSync(file, "utf8"));
  } catch {
    return fallback;
  }
}

export function writeJSON(file, value) {
  const tmp = file + ".tmp";
  fs.writeFileSync(tmp, JSON.stringify(value, null, 1));
  fs.renameSync(tmp, file);
}

// --- word status: known / shaky / unknown -------------------------------
const STATUS_FILE = path.join(DATA, "status.json");
let status = readJSON(STATUS_FILE, {});
export const getStatus = () => status;
export function setStatus(word, value) {
  if (value) status[word] = value;
  else delete status[word];
  writeJSON(STATUS_FILE, status);
}

// --- personal vocab list (words added with "Add to vocab") ---------------
const VOCAB_FILE = path.join(DATA, "vocab.json");
let vocab = readJSON(VOCAB_FILE, {});
export const getVocab = () => vocab;
export function addVocab(entries, source) {
  const now = new Date().toISOString();
  for (const e of entries) {
    if (!e.word) continue;
    vocab[e.word] = { ...vocab[e.word], ...e, source: source || e.source || "", added: vocab[e.word]?.added || now };
  }
  writeJSON(VOCAB_FILE, vocab);
}

// --- SRS state -----------------------------------------------------------
const SRS_FILE = path.join(DATA, "srs.json");
let srs = readJSON(SRS_FILE, {});
export const getSrs = () => srs;
export function saveSrs() {
  writeJSON(SRS_FILE, srs);
}

// --- decks: data/decks/*.txt, one word per line --------------------------
let deckCache = null;
export function loadDecks() {
  if (deckCache) return deckCache;
  const decks = {};
  for (const f of fs.readdirSync(DECKS)) {
    if (!f.endsWith(".txt")) continue;
    const name = f.replace(/\.txt$/, "");
    const words = fs
      .readFileSync(path.join(DECKS, f), "utf8")
      .split(/\r?\n/)
      .map((l) => l.split("\t")[0].trim())
      .filter((l) => l && !l.startsWith("#"));
    decks[name] = new Set(words);
  }
  deckCache = decks;
  return decks;
}
export function reloadDecks() {
  deckCache = null;
  return loadDecks();
}

// Which deck (if any) a word belongs to. Your own vocab list counts as a deck.
export function deckOf(word) {
  if (vocab[word]) return "my-vocab";
  for (const [name, set] of Object.entries(loadDecks())) if (set.has(word)) return name;
  return null;
}

// --- videos --------------------------------------------------------------
export const videoDir = (id) => path.join(VIDEOS, id);
export const reviewFile = (id) => path.join(videoDir(id), "review.json");
export const jobFile = (id) => path.join(videoDir(id), "job.json");

export function listVideos() {
  return fs
    .readdirSync(VIDEOS)
    .filter((id) => fs.existsSync(jobFile(id)))
    .map((id) => readJSON(jobFile(id), null))
    .filter(Boolean)
    .sort((a, b) => (b.created || "").localeCompare(a.created || ""));
}
