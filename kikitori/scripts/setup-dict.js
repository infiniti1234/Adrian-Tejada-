// Downloads the English JMdict (jmdict-simplified, CC BY-SA) into data/dict/.
import fs from "node:fs";
import path from "node:path";
import { execFileSync } from "node:child_process";
import { DICT } from "../lib/store.js";

const api = "https://api.github.com/repos/scriptin/jmdict-simplified/releases/latest";
const rel = await (await fetch(api, { headers: { "User-Agent": "kikitori" } })).json();
const asset = rel.assets?.find((a) => /^jmdict-eng-\d.*\.json\.tgz$/.test(a.name));
if (!asset) {
  console.error("Could not find the jmdict-eng .json.tgz asset in the latest release.");
  process.exit(1);
}
console.log(`Downloading ${asset.name} (${(asset.size / 1e6).toFixed(1)} MB)...`);
const buf = Buffer.from(await (await fetch(asset.browser_download_url)).arrayBuffer());
const tgz = path.join(DICT, asset.name);
fs.writeFileSync(tgz, buf);
for (const f of fs.readdirSync(DICT)) if (/^jmdict-eng.*\.json$|^index\.json$/.test(f)) fs.rmSync(path.join(DICT, f));
execFileSync("tar", ["-xzf", tgz, "-C", DICT]);
fs.rmSync(tgz);
console.log("Done. The index is built the next time the server starts.");
