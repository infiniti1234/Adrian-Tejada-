// Optional English pass using Claude. Needs ANTHROPIC_API_KEY (or an `ant auth login`
// profile). Without it, lines just show "translation pending".
import Anthropic from "@anthropic-ai/sdk";

const MODEL = process.env.KIKITORI_MODEL || "claude-opus-5-5";
let client = null;
const getClient = () => (client ??= new Anthropic());

export const translationEnabled = () =>
  Boolean(process.env.ANTHROPIC_API_KEY || process.env.ANTHROPIC_AUTH_TOKEN || process.env.ANTHROPIC_PROFILE);

const SCHEMA = {
  type: "object",
  properties: {
    lines: {
      type: "array",
      items: {
        type: "object",
        properties: { i: { type: "integer" }, en: { type: "string" } },
        required: ["i", "en"],
        additionalProperties: false,
      },
    },
    words: {
      type: "array",
      items: {
        type: "object",
        properties: { word: { type: "string" }, gloss: { type: "string" } },
        required: ["word", "gloss"],
        additionalProperties: false,
      },
    },
  },
  required: ["lines", "words"],
  additionalProperties: false,
};

const SYSTEM = `You translate Japanese video transcripts (machine transcribed, casual native speech) into natural English for a learner.
For each numbered line give a faithful, natural English translation, using surrounding lines for context. If the transcript looks misheard, translate your best guess and put "(?)" at the end.
For each word in the word list, give a short English gloss (2-6 words) for how it is used in this video.`;

async function translateChunk(lines, words, context) {
  const body = [
    context ? `Video: ${context}` : "",
    "Lines:",
    ...lines.map((l) => `${l.i}\t${l.text}`),
    "",
    "Words needing a gloss:",
    words.join("、") || "(none)",
  ].join("\n");

  const res = await getClient().beta.messages.create({
    model: MODEL,
    max_tokens: 16000,
    betas: ["server-side-fallback-2026-07-01"],
    fallbacks: "default",
    output_config: { effort: "low", format: { type: "json_schema", schema: SCHEMA } },
    system: SYSTEM,
    messages: [{ role: "user", content: body }],
  });
  if (res.stop_reason === "refusal") throw new Error("Claude declined to translate this chunk");
  const text = res.content.find((b) => b.type === "text")?.text || "{}";
  return JSON.parse(text);
}

// review: the review.json object. Fills line.en and word.gloss (where missing) in place.
export async function translateReview(review, log = console.log, onChunk = () => {}) {
  const CHUNK = 60;
  const missingWords = Object.entries(review.words)
    .filter(([, w]) => !w.gloss)
    .map(([k]) => k);
  for (let start = 0; start < review.lines.length; start += CHUNK) {
    const lines = review.lines.slice(start, start + CHUNK).map((l, k) => ({ i: start + k, text: l.text }));
    const words = missingWords.filter((w) => review.words[w].lines.some((i) => i >= start && i < start + CHUNK) && !review.words[w].gloss);
    log(`Translating lines ${start + 1}-${start + lines.length} of ${review.lines.length}...`);
    const out = await translateChunk(lines, words, review.title);
    for (const l of out.lines || []) if (review.lines[l.i]) review.lines[l.i].en = l.en;
    for (const w of out.words || []) if (review.words[w.word]) review.words[w.word].gloss = w.gloss, review.words[w.word].glossBy = "claude";
    onChunk(review);
  }
}
