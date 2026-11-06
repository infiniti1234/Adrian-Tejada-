// SRT / WebVTT parsing into [{start, end, text}] (seconds).
const ts = (s) => {
  const m = s.trim().replace(",", ".").match(/(?:(\d+):)?(\d+):(\d+(?:\.\d+)?)/);
  if (!m) return 0;
  return (Number(m[1] || 0) * 3600) + Number(m[2]) * 60 + Number(m[3]);
};

export function parseSubs(raw) {
  const out = [];
  const blocks = raw.replace(/\r/g, "").split(/\n\s*\n/);
  for (const b of blocks) {
    const lines = b.split("\n").filter(Boolean);
    const i = lines.findIndex((l) => l.includes("-->"));
    if (i < 0) continue;
    const [a, z] = lines[i].split("-->");
    const text = lines
      .slice(i + 1)
      .join(" ")
      .replace(/<[^>]+>/g, "")
      .replace(/\s+/g, " ")
      .trim();
    if (!text) continue;
    const seg = { start: ts(a), end: ts(z.split(" ").filter(Boolean)[0] || z), text };
    // YouTube auto-subs repeat lines as they roll; drop exact repeats.
    if (out.length && out.at(-1).text === text) out.at(-1).end = seg.end;
    else out.push(seg);
  }
  return out;
}
