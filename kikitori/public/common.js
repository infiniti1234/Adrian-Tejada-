// Shared helpers: API calls, nav, audio, and the flashcard drill dialog.
export const $ = (s, el = document) => el.querySelector(s);
export const $$ = (s, el = document) => [...el.querySelectorAll(s)];
export const esc = (s = "") => String(s).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[c]);

export async function api(path, body, method) {
  const res = await fetch(path, {
    method: method || (body ? "POST" : "GET"),
    headers: body ? { "Content-Type": "application/json" } : {},
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || res.statusText);
  return data;
}

export function nav(active) {
  const links = [["/", "Pipeline"], ["/library", "Library"], ["/drill", "Drill"]];
  document.body.insertAdjacentHTML(
    "afterbegin",
    `<div class="top"><div class="in">
      <a class="brand" href="/library"><span>聞き<b>取り</b></span><small>Kikitori</small></a>
      <nav class="nav">${links.map(([h, t]) => `<a href="${h}" class="${t === active ? "on" : ""}">${t}</a>`).join("")}</nav>
    </div></div>`,
  );
}

export function toast(msg) {
  const t = document.createElement("div");
  t.className = "toast";
  t.textContent = msg;
  document.body.append(t);
  setTimeout(() => t.remove(), 2200);
}

export const fmtTime = (s) => `${Math.floor(s / 60)}:${String(Math.floor(s % 60)).padStart(2, "0")}`;

// ---- audio --------------------------------------------------------------
let current = null;
export function playUrl(url) {
  current?.pause();
  current = new Audio(url);
  current.play().catch(() => {});
  return current;
}
export const playClip = (videoId, line) => playUrl(`/media/${videoId}/clip/${line}`);

let voicevoxDown = false;
export async function speak(text) {
  if (!voicevoxDown) {
    const res = await fetch(`/api/tts?text=${encodeURIComponent(text)}`).catch(() => null);
    if (res?.ok) {
      const blob = await res.blob();
      return playUrl(URL.createObjectURL(blob));
    }
    voicevoxDown = true;
  }
  // Fallback: the browser's built-in Japanese voice.
  const u = new SpeechSynthesisUtterance(text);
  u.lang = "ja-JP";
  speechSynthesis.cancel();
  speechSynthesis.speak(u);
}

// ---- word status (shared across pages) -------------------------------------
export let STATUS = {};
export async function loadStatus() {
  STATUS = await api("/api/status");
  return STATUS;
}
export async function setStatus(word, status) {
  if (STATUS[word] === status) status = null; // click again to clear
  if (status) STATUS[word] = status;
  else delete STATUS[word];
  await api("/api/status", { word, status });
  return status;
}
// Effective status: what you set, else "known" if it's in one of your decks.
export const statusOf = (word, w) => STATUS[word] || (w?.deck ? "known" : "unknown");

export function highlight(text, word) {
  const i = text.indexOf(word);
  if (i < 0) return esc(text);
  return esc(text.slice(0, i)) + "<mark>" + esc(word) + "</mark>" + esc(text.slice(i + word.length));
}

// ---- flashcard drill --------------------------------------------------------
// entries: [{word, reading, gloss, pos, videoId, line, sentence, en, isNew}]
const DEFAULTS = { timing: true, newPerDay: 20, reviewsPerDay: 100, includeNew: true, includeExisting: false, hideMastered: true, front: "jp" };
const loadSettings = () => {
  try { return { ...DEFAULTS, ...JSON.parse(localStorage.getItem("kk-drill") || "{}") }; } catch { return { ...DEFAULTS }; }
};
const saveSettings = (s) => { try { localStorage.setItem("kk-drill", JSON.stringify(s)); } catch {} };

const LABELS = [["again", "Again"], ["hard", "Hard"], ["good", "Good"], ["easy", "Easy"], ["month", "1 Month"], ["mastered", "Mastered"]];
const ivLabel = (d) => (d === 0 ? "<10m" : d >= 30 ? `${Math.round(d / 30)}mo` : `${d}d`);
function previewIntervals(card) {
  const iv = card?.interval || 0, ease = card?.ease || 2.5;
  return { again: 0, hard: Math.max(1, Math.round(iv * 1.2)), good: iv ? Math.round(iv * ease) : 1, easy: iv ? Math.round(iv * ease * 1.3) : 4, month: 30 };
}

export async function openDrill(title, allEntries) {
  const settings = loadSettings();
  let srs = await api("/api/srs");
  let queue = [], pos = 0, revealed = false;

  const root = document.createElement("div");
  root.className = "modal";
  root.innerHTML = `<div class="dialog" role="dialog" aria-label="Flashcards">
    <div class="row" style="justify-content:space-between;align-items:flex-start;margin-bottom:10px">
      <div><div class="jp" style="font-weight:600">${esc(title)}</div><div class="small muted" id="dq"></div></div>
      <button class="btn sm ghost" id="dclose" aria-label="Close">✕</button>
    </div>
    <div class="settings">
      <label><input type="checkbox" id="s-timing"> Only cards due today</label>
      <label>New/day <input type="number" id="s-new" min="0"></label>
      <label>Reviews/day <input type="number" id="s-rev" min="0"></label>
      <label><input type="checkbox" id="s-inew"> New words</label>
      <label><input type="checkbox" id="s-iex"> Words from your decks</label>
      <label><input type="checkbox" id="s-hm"> Hide mastered</label>
      <label>Front <select id="s-front"><option value="jp">日本語</option><option value="en">English</option></select></label>
    </div>
    <div id="dbody"></div>
  </div>`;
  document.body.append(root);

  const bind = (id, key, prop = "checked", cast = (v) => v) => {
    const el = $(id, root);
    el[prop] = settings[key];
    el.addEventListener("change", () => { settings[key] = cast(el[prop]); saveSettings(settings); build(); });
  };
  bind("#s-timing", "timing");
  bind("#s-new", "newPerDay", "value", Number);
  bind("#s-rev", "reviewsPerDay", "value", Number);
  bind("#s-inew", "includeNew");
  bind("#s-iex", "includeExisting");
  bind("#s-hm", "hideMastered");
  bind("#s-front", "front", "value");

  const close = () => { root.remove(); document.removeEventListener("keydown", onKey); };
  $("#dclose", root).onclick = close;
  root.addEventListener("click", (e) => e.target === root && close());

  function build() {
    const now = Date.now();
    const cards = srs.cards;
    let fresh = [], due = [];
    const seen = new Set();
    for (const e of allEntries) {
      if (seen.has(e.word)) continue;
      seen.add(e.word);
      const c = cards[e.word];
      if (c?.mastered && settings.hideMastered) continue;
      if (e.isNew ? !settings.includeNew : !settings.includeExisting) continue;
      if (!c) fresh.push(e);
      else if (!settings.timing || !c.due || Date.parse(c.due) <= now) due.push(e);
    }
    if (settings.timing) {
      fresh = fresh.slice(0, Math.max(0, settings.newPerDay - srs.newToday));
      due = due.slice(0, Math.max(0, settings.reviewsPerDay - srs.reviewsToday));
    }
    queue = [...due, ...fresh];
    pos = 0;
    render();
  }

  function render() {
    revealed = false;
    $("#dq", root).textContent = `today: ${srs.newToday}/${settings.newPerDay} new, ${srs.reviewsToday}/${settings.reviewsPerDay} reviews`;
    const body = $("#dbody", root);
    if (pos >= queue.length) {
      body.innerHTML = `<div class="empty">${queue.length ? "Done for now. お疲れさま！" : "Nothing due. Change the filters above, or come back later."}</div>`;
      return;
    }
    const e = queue[pos];
    const front = settings.front === "en" && e.gloss
      ? `<div class="big en">${esc(e.gloss)}</div>`
      : `<div class="big"><button class="play" data-say aria-label="Say it">▶</button>${esc(e.word)}</div><div class="small muted">${esc(e.pos || "")}</div>`;
    body.innerHTML = `<div class="fc" id="fc">${front}
      <div class="small muted" style="margin-top:22px" id="hint">tap or press space to reveal</div>
      <div class="reveal" id="rv" hidden>
        ${settings.front === "en" ? `<div class="reading" style="font-size:30px;color:var(--ink)">${esc(e.word)}</div>` : ""}
        <div class="reading">${esc(e.reading || "")}</div>
        <div class="meaning">${esc(e.gloss || "(no meaning yet)")}</div>
        ${e.sentence ? `<div class="small muted">IN THE VIDEO</div>
        <div class="ctx">${e.videoId != null ? `<button class="play" data-clip aria-label="Play clip">▶</button>` : ""}<span>${highlight(e.sentence, e.word)}</span></div>
        ${e.en ? `<div class="cen">${esc(e.en)}</div>` : ""}` : ""}
      </div></div>
      <div class="grades" id="grades" hidden>${LABELS.map(([r, l]) => {
        const iv = previewIntervals(srs.cards[e.word]);
        return `<button data-r="${r}">${l}<small>${r === "mastered" ? "done" : ivLabel(iv[r])}</small></button>`;
      }).join("")}</div>
      <div class="row small muted" style="justify-content:space-between;margin-top:10px"><span>${pos + 1} / ${queue.length}</span><span>keys: space · 1–6</span></div>`;
    $("#fc", root).onclick = (ev) => {
      if (ev.target.closest("[data-say]")) return speak(e.word);
      if (ev.target.closest("[data-clip]")) return playClip(e.videoId, e.line);
      reveal();
    };
  }

  function reveal() {
    if (revealed || pos >= queue.length) return;
    revealed = true;
    const e = queue[pos];
    $("#rv", root).hidden = false;
    $("#hint", root).hidden = true;
    $("#grades", root).hidden = false;
    $$("#grades button", root).forEach((b) => (b.onclick = () => rate(b.dataset.r)));
    if (e.videoId != null) playClip(e.videoId, e.line);
    else speak(e.word);
  }

  async function rate(r) {
    const e = queue[pos];
    const out = await api("/api/srs/grade", { word: e.word, rating: r });
    srs.cards[e.word] = out.card;
    srs.newToday = out.newToday;
    srs.reviewsToday = out.reviewsToday;
    if (r === "again") queue.push(e); // see it again this session
    pos++;
    render();
  }

  function onKey(ev) {
    if (ev.target.matches("input,select")) return;
    if (ev.key === "Escape") return close();
    if (ev.key === " ") { ev.preventDefault(); reveal(); }
    const n = Number(ev.key);
    if (revealed && n >= 1 && n <= 6) rate(LABELS[n - 1][0]);
  }
  document.addEventListener("keydown", onKey);
  build();
}
