import fs from "node:fs";
import path from "node:path";
import express from "express";
import {
  ROOT, videoDir, reviewFile, jobFile, readJSON, writeJSON, listVideos, loadDecks, reloadDecks,
  getStatus, setStatus, getVocab, addVocab, getSrs, deckOf, DATA,
} from "./lib/store.js";
import { startJob, getJob, STAGES } from "./lib/pipeline.js";
import { grade, intervals, stats as srsStats } from "./lib/srs.js";
import { hasDict, loadDict } from "./lib/dict.js";
import { translateReview, translationEnabled } from "./lib/translate.js";
import { getTokenizer } from "./lib/tokenize.js";

const PORT = Number(process.env.PORT || 4321);
const app = express();
app.use(express.json({ limit: "5mb" }));
app.use(express.static(path.join(ROOT, "public")));

const ok = (res, v) => res.json(v);
const bad = (res, msg, code = 400) => res.status(code).json({ error: msg });

app.get("/api/info", (req, res) => {
  const decks = loadDecks();
  ok(res, {
    stages: STAGES,
    decks: Object.fromEntries(Object.entries(decks).map(([k, v]) => [k, v.size])),
    myVocab: Object.keys(getVocab()).length,
    dictionary: hasDict(),
    translation: translationEnabled(),
    voicevox: process.env.VOICEVOX_URL || "http://127.0.0.1:50021",
  });
});

// ---- pipeline -------------------------------------------------------------
app.post("/api/jobs", (req, res) => {
  const { url, channel } = req.body || {};
  if (!url) return bad(res, "Paste a video URL or a local file path");
  ok(res, startJob({ url, channel }));
});
app.get("/api/jobs", (req, res) => ok(res, listVideos().map(({ log, ...j }) => j)));
app.get("/api/jobs/:id", (req, res) => {
  const job = getJob(req.params.id);
  job ? ok(res, job) : bad(res, "not found", 404);
});
app.delete("/api/jobs/:id", (req, res) => {
  const dir = videoDir(req.params.id);
  if (!fs.existsSync(jobFile(req.params.id))) return bad(res, "not found", 404);
  fs.rmSync(dir, { recursive: true, force: true });
  ok(res, { deleted: true });
});

// Commit to the library, then run the English pass in the background.
app.post("/api/jobs/:id/commit", (req, res) => {
  const job = readJSON(jobFile(req.params.id), null);
  if (!job || job.state !== "done") return bad(res, "job is not finished");
  job.committed = true;
  if (req.body?.channel) job.channel = req.body.channel;
  writeJSON(jobFile(job.id), job);
  const review = readJSON(reviewFile(job.id), null);
  review.channel = job.channel;
  writeJSON(reviewFile(job.id), review);
  ok(res, job);
  if (translationEnabled() && !review.translated) runTranslation(job.id);
});
app.post("/api/videos/:id/translate", (req, res) => {
  if (!translationEnabled()) return bad(res, "Set ANTHROPIC_API_KEY to enable English translations");
  runTranslation(req.params.id);
  ok(res, { started: true });
});

const translating = new Set();
async function runTranslation(id) {
  if (translating.has(id)) return;
  translating.add(id);
  try {
    const review = readJSON(reviewFile(id), null);
    review.translating = true;
    writeJSON(reviewFile(id), review);
    await translateReview(review, (m) => console.log(`[${id}] ${m}`), (rv) => writeJSON(reviewFile(id), rv));
    review.translated = true;
    review.translating = false;
    writeJSON(reviewFile(id), review);
  } catch (e) {
    console.error(`[${id}] translation failed:`, e.message);
    const review = readJSON(reviewFile(id), null);
    if (review) writeJSON(reviewFile(id), { ...review, translating: false, translateError: e.message });
  } finally {
    translating.delete(id);
  }
}

// ---- library --------------------------------------------------------------
function knownPct(review) {
  const status = getStatus();
  const keys = Object.keys(review.words);
  if (!keys.length) return 0;
  const known = keys.filter((k) => status[k] === "known" || (!status[k] && deckOf(k))).length;
  return Math.round((known / keys.length) * 100);
}

app.get("/api/videos", (req, res) => {
  const out = listVideos()
    .filter((j) => j.committed)
    .map((j) => {
      const r = readJSON(reviewFile(j.id), null);
      return { id: j.id, title: j.title, channel: j.channel, created: j.created, stats: j.stats, known: r ? knownPct(r) : 0, url: j.url };
    });
  ok(res, out);
});

app.get("/api/videos/:id", (req, res) => {
  const r = readJSON(reviewFile(req.params.id), null);
  if (!r) return bad(res, "not found", 404);
  for (const [k, w] of Object.entries(r.words)) w.deck = deckOf(k); // decks may have changed
  ok(res, { ...r, known: knownPct(r) });
});
app.get("/media/:id/video", (req, res) => {
  const r = readJSON(reviewFile(req.params.id), null);
  if (!r) return res.sendStatus(404);
  res.sendFile(path.join(videoDir(req.params.id), r.video));
});
app.get("/media/:id/clip/:n", (req, res) => {
  const f = path.join(videoDir(req.params.id), "clips", `${Number(req.params.n)}.mp3`);
  fs.existsSync(f) ? res.sendFile(f) : res.sendStatus(404);
});

// ---- words ----------------------------------------------------------------
app.get("/api/status", (req, res) => ok(res, getStatus()));
app.post("/api/status", (req, res) => {
  const { word, status } = req.body || {};
  if (!word || !["known", "shaky", "unknown", null].includes(status ?? null)) return bad(res, "bad status");
  setStatus(word, status);
  ok(res, { word, status });
});
app.get("/api/vocab", (req, res) => ok(res, getVocab()));
app.post("/api/vocab", (req, res) => {
  const { entries, source } = req.body || {};
  if (!Array.isArray(entries)) return bad(res, "entries must be an array");
  addVocab(entries, source);
  ok(res, { count: Object.keys(getVocab()).length });
});
app.post("/api/decks/reload", (req, res) => {
  const d = reloadDecks();
  ok(res, Object.fromEntries(Object.entries(d).map(([k, v]) => [k, v.size])));
});

// ---- flashcards -----------------------------------------------------------
app.get("/api/srs", (req, res) => ok(res, { cards: getSrs(), ...srsStats() }));
app.post("/api/srs/grade", (req, res) => {
  const { word, rating } = req.body || {};
  if (!word || !["again", "hard", "good", "easy", "month", "mastered"].includes(rating)) return bad(res, "bad rating");
  const card = grade(word, rating);
  ok(res, { card, next: intervals(card), ...srsStats() });
});

// ---- TTS: VOICEVOX if it's running, else the browser's Japanese voice ------
const ttsCache = path.join(DATA, "tts");
fs.mkdirSync(ttsCache, { recursive: true });
app.get("/api/tts", async (req, res) => {
  const text = String(req.query.text || "").slice(0, 200);
  const speaker = Number(req.query.speaker || process.env.VOICEVOX_SPEAKER || 3);
  if (!text) return res.sendStatus(400);
  const key = Buffer.from(`${speaker}:${text}`).toString("base64url").slice(0, 120);
  const file = path.join(ttsCache, key + ".wav");
  if (fs.existsSync(file)) return res.sendFile(file);
  const base = process.env.VOICEVOX_URL || "http://127.0.0.1:50021";
  try {
    const q = await fetch(`${base}/audio_query?speaker=${speaker}&text=${encodeURIComponent(text)}`, { method: "POST", signal: AbortSignal.timeout(4000) });
    if (!q.ok) throw new Error(`audio_query ${q.status}`);
    const s = await fetch(`${base}/synthesis?speaker=${speaker}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: await q.text(),
      signal: AbortSignal.timeout(20000),
    });
    if (!s.ok) throw new Error(`synthesis ${s.status}`);
    fs.writeFileSync(file, Buffer.from(await s.arrayBuffer()));
    res.sendFile(file);
  } catch {
    res.status(503).json({ error: "VOICEVOX is not running" });
  }
});

// Export selected words as a tab-separated file Anki can import.
app.post("/api/export", (req, res) => {
  const rows = (req.body?.rows || []).map((r) =>
    [r.word, r.reading, r.gloss, r.sentence, r.en || ""].map((c) => String(c || "").replace(/[\t\n]/g, " ")).join("\t"),
  );
  res.setHeader("Content-Type", "text/tab-separated-values; charset=utf-8");
  res.setHeader("Content-Disposition", 'attachment; filename="kikitori-words.tsv"');
  res.send("#separator:tab\n#html:false\n" + rows.join("\n") + "\n");
});

app.get(["/watch/:id", "/library", "/drill"], (req, res) =>
  res.sendFile(path.join(ROOT, "public", req.path.startsWith("/watch") ? "watch.html" : req.path.slice(1) + ".html")),
);

loadDict();
getTokenizer().then(() => console.log("[tokenizer] ready"));
app.listen(PORT, "127.0.0.1", () => {
  console.log(`Kikitori running at http://localhost:${PORT}`);
  if (!hasDict()) console.log("  (no dictionary yet: run `npm run setup-dict` for English meanings)");
  if (!translationEnabled()) console.log("  (no ANTHROPIC_API_KEY: line translations will show as pending)");
});
