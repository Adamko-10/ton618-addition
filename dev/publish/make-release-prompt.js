// Fills dev/publish/release-prompt.template.md for a release done by another AI (ChatGPT/Codex) and writes it to
// ../releases/<version>/RELEASE-PROMPT.md: node dev/publish/make-release-prompt.js <version> [head commit]
// Run from the repo root after the release commit (the head defaults to HEAD) and after the jar is staged in
// ../releases/<version>/jars/.
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { execSync } = require('child_process');

const version = process.argv[2];
if (!version) {
    console.error('usage: node dev/publish/make-release-prompt.js <version> [head]');
    process.exit(1);
}
const head = process.argv[3] || execSync('git rev-parse --short HEAD').toString().trim();
const jarName = `TON 618 Addition ${version} [NeoForge 1.21.1].jar`;
const rel = path.join('..', 'releases', version);
const jar = path.join(rel, 'jars', jarName);
const sha = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
const read = p => fs.readFileSync(p, 'utf8').replace(/\r\n/g, '\n').trimEnd();
const values = {
    VERSION: version,
    HEAD: head,
    JAR: jarName,
    SHA256: sha,
    CHANGELOG: read(path.join(rel, 'changelogs', 'main.md')),
    DESCRIPTION: read('dev/publish/curseforge-description.md'),
    SUMMARY: read('dev/publish/summary.txt'),
};
let out = read('dev/publish/release-prompt.template.md');
for (const [k, v] of Object.entries(values)) {
    out = out.split(`{{${k}}}`).join(v);
}
if (/\{\{[A-Z0-9]+\}\}/.test(out)) {
    console.error('unfilled placeholder: ' + out.match(/\{\{[A-Z0-9]+\}\}/)[0]);
    process.exit(1);
}
const target = path.join(rel, 'RELEASE-PROMPT.md');
fs.writeFileSync(target, out + '\n');
console.log(`${target}: head ${head}, jar sha256 ${sha.slice(0, 12)}..., ${out.length} characters`);
