// The immersion pipeline: download -> extract audio -> transcribe -> tokenize ->
// match vocab -> cut clips. Each video gets a folder in data/videos/<id>/.
import fs from "node:fs";
import path from "node:path";
import crypto from "node:crypto";
import { spawn } from "node:child_process";
import { videoDir, jobFile, reviewFile, readJSON, writeJSON, deckOf, getStatus } from "./store.js";
import { parseSubs } from "./subs.js";
import { tokenizeLine } from "./tokenize.js";
import { lookup } from "./dict.js";

export const STAGES = ["download", "extract", "transcribe", "tokenize", "match", "clips"];
const running = new Map(); // id -> job (in memory while running)

export function getJob(id) {
  return running.get(id) || readJSON(jobFile(id), null);
}

function save(job) {
  writeJSON(jobFile(job.id), job);
}

function log(job, msg) {
  const line = `[${new Date().toISOString().slice(11, 19)}] ${msg}`;
  job.log.push(line);
  if (job.log.length > 400) job.log.splice(0, job.log.length - 400);
  console.log(`[${job.id}] ${msg}`);
}

function run(job, cmd, args, opts = {}) {
  return new Promise((resolve, reject) => {
    let p;
    try {
      p = spawn(cmd, args, { cwd: opts.cwd });
    } catch (e) {
      return reject(e);
    }
    let tail = "";
    const onData = (d) => {
      tail = (tail + d.toString()).slice(-4000);
      if (opts.echo) {
        const last = d.toString().trim().split(/[\r\n]+/).pop();
        if (last && /%|segment|\[\d/.test(last)) job.progress = last.slice(0, 160);
      }
    };
    p.stdout.on("data", onData);
    p.stderr.on("data", onData);
    p.on("error", (e) =>
      reject(e.code === "ENOENT" ? new Error(`"${cmd}" is not installed or not on PATH`) : e),
    );
    p.on("close", (code) => (code === 0 ? resolve(tail) : reject(new Error(`${cmd} exited with ${code}\n${tail.slice(-800)}`))));
  });
}

export function startJob({ url, channel }) {
  const id = crypto.randomBytes(5).toString("hex");
  fs.mkdirSync(videoDir(id), { recursive: true });
  const job = {
    id,
    url: url.trim(),
    channel: (channel || "").trim() || "Unsorted",
    title: "",
    created: new Date().toISOString(),
    state: "running",
    stages: Object.fromEntries(STAGES.map((s) => [s, "pending"])),
    log: [],
    committed: false,
  };
  running.set(id, job);
  save(job);
  runPipeline(job).finally(() => {
    save(job);
    running.delete(id);
  });
  return job;
}

async function stage(job, name, fn) {
  job.stage = name;
  job.stages[name] = "running";
  job.progress = "";
  save(job);
  await fn();
  job.stages[name] = "done";
  save(job);
}

async function runPipeline(job) {
  const dir = videoDir(job.id);
  const t0 = Date.now();
  try {
    await stage(job, "download", () => download(job, dir));
    await stage(job, "extract", async () => {
      log(job, "Extracting 16 kHz mono audio...");
      await run(job, "ffmpeg", ["-y", "-v", "error", "-i", job.videoFile, "-vn", "-ac", "1", "-ar", "16000", "audio.wav"], { cwd: dir });
      if (!/\.(mp4|webm|m4v)$/i.test(job.videoFile)) {
        log(job, "Remuxing to mp4 so the browser can play it...");
        await run(job, "ffmpeg", ["-y", "-v", "error", "-i", job.videoFile, "-c:v", "copy", "-c:a", "aac", "-movflags", "+faststart", "video.mp4"], { cwd: dir })
          .catch(() => run(job, "ffmpeg", ["-y", "-v", "error", "-i", job.videoFile, "-c:v", "libx264", "-preset", "veryfast", "-c:a", "aac", "video.mp4"], { cwd: dir }));
        fs.rmSync(path.join(dir, job.videoFile), { force: true });
        job.videoFile = "video.mp4";
      }
    });
    let segments;
    await stage(job, "transcribe", async () => {
      segments = await transcribe(job, dir);
      log(job, `Parsed ${segments.length} timestamped segments.`);
    });
    let lines;
    await stage(job, "tokenize", async () => {
      log(job, "Tokenizing transcript (kuromoji)...");
      lines = [];
      for (const seg of segments) lines.push({ ...seg, tokens: await tokenizeLine(seg.text) });
    });
    let review;
    await stage(job, "match", async () => {
      review = buildReview(job, lines);
      const s = review.stats;
      log(job, `Matched against your decks: ${s.existing} words you already have, ${s.newWords} new-word candidates.`);
    });
    await stage(job, "clips", async () => {
      log(job, `Cutting ${lines.length} real audio clips...`);
      fs.mkdirSync(path.join(dir, "clips"), { recursive: true });
      for (let i = 0; i < lines.length; i++) {
        const l = lines[i];
        const start = Math.max(0, l.start - 0.15);
        const dur = Math.max(0.3, l.end - l.start + 0.3);
        await run(job, "ffmpeg", ["-y", "-v", "error", "-ss", start.toFixed(2), "-t", dur.toFixed(2), "-i", "audio.wav", "-c:a", "libmp3lame", "-q:a", "5", `clips/${i}.mp3`], { cwd: dir });
        if (i % 20 === 0) job.progress = `clip ${i + 1}/${lines.length}`;
      }
    });
    writeJSON(reviewFile(job.id), review);
    job.stats = review.stats;
    job.state = "done";
    job.seconds = Math.round((Date.now() - t0) / 1000);
    log(job, `Done in ${job.seconds}s. ${review.stats.lines} lines, ${review.stats.newWords} new words, ${review.stats.existing} existing.`);
    fs.rmSync(path.join(dir, "audio.wav"), { force: true });
  } catch (e) {
    job.state = "error";
    if (job.stage) job.stages[job.stage] = "error";
    job.error = e.message;
    log(job, `ERROR: ${e.message}`);
  }
}

async function download(job, dir) {
  const src = job.url;
  if (!/^https?:\/\//.test(src)) {
    // Local file on this computer
    if (!fs.existsSync(src)) throw new Error(`File not found: ${src}`);
    const ext = path.extname(src) || ".mp4";
    log(job, `Copying local file ${src}...`);
    fs.copyFileSync(src, path.join(dir, "video" + ext));
    job.videoFile = "video" + ext;
    job.title = path.basename(src, ext);
    for (const subExt of [".ja.vtt", ".ja.srt", ".vtt", ".srt"]) {
      const sub = src.slice(0, -ext.length) + subExt;
      if (fs.existsSync(sub)) {
        fs.copyFileSync(sub, path.join(dir, "subs" + (subExt.endsWith("vtt") ? ".vtt" : ".srt")));
        log(job, `Found subtitles next to the video: ${path.basename(sub)}`);
        break;
      }
    }
    return;
  }
  log(job, "Downloading with yt-dlp...");
  await run(
    job,
    "yt-dlp",
    [
      "-f", "bv*[height<=720][ext=mp4]+ba[ext=m4a]/b[ext=mp4]/bv*+ba/b",
      "--merge-output-format", "mp4",
      "-o", "video.%(ext)s",
      "--write-info-json",
      "--write-subs", "--sub-langs", "ja", "--sub-format", "vtt",
      "--no-playlist", "--newline",
      src,
    ],
    { cwd: dir, echo: true },
  );
  const info = readJSON(path.join(dir, "video.info.json"), {});
  job.title = info.title || src;
  job.duration = info.duration;
  job.uploader = info.channel || info.uploader;
  if (job.channel === "Unsorted" && job.uploader) job.channel = job.uploader;
  job.videoFile = fs.readdirSync(dir).find((f) => /^video\.(mp4|webm|mkv)$/.test(f));
  if (!job.videoFile) throw new Error("yt-dlp finished but no video file was found");
  const sub = fs.readdirSync(dir).find((f) => /^video\.ja.*\.vtt$/.test(f));
  if (sub) {
    fs.renameSync(path.join(dir, sub), path.join(dir, "subs.vtt"));
    log(job, "The video has human-made Japanese subtitles; using those instead of Whisper.");
  }
  log(job, `Downloaded: ${job.title}`);
}

async function transcribe(job, dir) {
  const existing = ["subs.vtt", "subs.srt"].find((f) => fs.existsSync(path.join(dir, f)));
  if (existing && process.env.KIKITORI_FORCE_WHISPER !== "1") {
    log(job, `Using subtitles from ${existing}.`);
    return parseSubs(fs.readFileSync(path.join(dir, existing), "utf8"));
  }
  const model = process.env.WHISPER_CPP_MODEL;
  if (model) {
    const bin = process.env.WHISPER_CPP_BIN || "whisper-cli";
    log(job, `Transcribing with whisper.cpp (${path.basename(model)})...`);
    await run(job, bin, ["-m", model, "-l", "ja", "-f", "audio.wav", "-osrt", "-of", "transcript"], { cwd: dir, echo: true });
  } else {
    const bin = process.env.WHISPER_BIN || "whisper";
    const size = process.env.WHISPER_MODEL || "small";
    log(job, `Transcribing with ${bin} (model: ${size})... this is the slow step.`);
    await run(job, bin, ["audio.wav", "--language", "ja", "--model", size, "--output_format", "srt", "--output_dir", "."], { cwd: dir, echo: true });
    fs.renameSync(path.join(dir, "audio.srt"), path.join(dir, "transcript.srt"));
  }
  return parseSubs(fs.readFileSync(path.join(dir, "transcript.srt"), "utf8"));
}

export function buildReview(job, lines) {
  const words = {};
  lines.forEach((l, i) => {
    for (const t of l.tokens) {
      if (!t.content) continue;
      const w = (words[t.base] ??= { reading: "", pos: t.pos, count: 0, lines: [] });
      w.count++;
      if (!w.lines.includes(i)) w.lines.push(i);
      if (!w.reading) {
        const d = lookup(t.base);
        w.reading = t.reading || d?.reading || "";
        if (d) w.gloss = d.gloss;
      }
    }
  });
  for (const [k, w] of Object.entries(words)) w.deck = deckOf(k);
  const status = getStatus();
  const vals = Object.entries(words);
  return {
    id: job.id,
    title: job.title,
    channel: job.channel,
    url: job.url,
    video: job.videoFile,
    lines: lines.map(({ start, end, text, tokens }) => ({ start, end, text, tokens })),
    words,
    stats: {
      lines: lines.length,
      unique: vals.length,
      existing: vals.filter(([k, w]) => w.deck || status[k] === "known").length,
      newWords: vals.filter(([k, w]) => !w.deck && status[k] !== "known").length,
    },
  };
}
