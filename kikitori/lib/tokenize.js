// Japanese tokenizing with kuromoji (IPADIC). Returns content words only:
// nouns, verbs, adjectives, adverbs — particles and auxiliaries are dropped.
import path from "node:path";
import kuromoji from "kuromoji";
import { ROOT } from "./store.js";

let tokenizerPromise = null;
export function getTokenizer() {
  tokenizerPromise ??= new Promise((resolve, reject) => {
    kuromoji
      .builder({ dicPath: path.join(ROOT, "node_modules/kuromoji/dict") })
      .build((err, t) => (err ? reject(err) : resolve(t)));
  });
  return tokenizerPromise;
}

const POS_EN = { 名詞: "noun", 動詞: "verb", 形容詞: "i-adj", 副詞: "adverb", 連体詞: "pre-noun", 感動詞: "interjection" };

export const kataToHira = (s = "") =>
  s.replace(/[ァ-ヶ]/g, (c) => String.fromCharCode(c.charCodeAt(0) - 0x60));

function keep(t) {
  const p = t.pos, d = t.pos_detail_1;
  if (!POS_EN[p]) return false;
  if (p === "名詞" && ["数", "非自立", "代名詞", "接尾", "特殊"].includes(d)) return false;
  if (p === "動詞" && d === "非自立") return false;
  if (p === "形容詞" && d === "非自立") return false;
  const base = t.basic_form === "*" ? t.surface_form : t.basic_form;
  if (/^[\x00-\x7F０-９ａ-ｚＡ-Ｚ]+$/.test(base)) return false; // romaji / numbers
  if (/^[ぁ-ゖー]$/.test(base)) return false; // lone kana
  return true;
}

// Returns { tokens: [{s, base, reading, pos, content}] } covering the whole line,
// so the UI can rebuild the sentence and make content words clickable.
const baseReadings = new Map();
// Reading of the dictionary form (話し -> はなす), not the conjugated surface.
function readingOfBase(t, base) {
  if (!baseReadings.has(base)) {
    const toks = t.tokenize(base);
    baseReadings.set(base, kataToHira(toks.map((x) => x.reading || x.surface_form).join("")));
  }
  return baseReadings.get(base);
}
const isKatakana = (s) => /^[\u30a0-\u30ffー・]+$/.test(s);

export async function tokenizeLine(text) {
  const t = await getTokenizer();
  return t.tokenize(text).map((tok) => {
    const content = keep(tok);
    const base = tok.basic_form === "*" ? tok.surface_form : tok.basic_form;
    let reading = "";
    if (content && !isKatakana(base) && !/^[ぁ-ゖー]+$/.test(base)) {
      reading = base === tok.surface_form ? kataToHira(tok.reading || "") : readingOfBase(t, base);
    }
    return { s: tok.surface_form, base, reading, pos: POS_EN[tok.pos] || tok.pos, content };
  });
}
