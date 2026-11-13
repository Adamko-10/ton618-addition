# CLAUDE.md: the TON 618 addon (working name, not picked yet)

Handoff notes for every Claude session in this folder (`C:\Users\User\Desktop\ton 618 addition`, on Adam's VM).
Started 2026-11-13 by an Opus 5.5 session (xhigh) that was started from Adam's SA Series session. **Read `BRIEF.md`
first** (Adam's brief, his own words at its end), then this file. Fable's part: `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`.

### >>> RESUME HERE (2026-11-13 evening, the VM): brainstorm started, nothing built yet <<<

- Done: read the brief; read TON 618's page; downloaded TON 618 1.1.0 and RimuLib 1.2.3 (NeoForge) into `libs/` (Adam's
  OK); decompiled TON 618 into the session scratch folder and wrote what it does in **`docs/TON618-NOTES.md`**; made
  this folder a git repo (main, no remote).
- Now: the first brainstorm questions are with Adam (§3). His answers go in §2 as they come.
- Next: finish the brainstorm, pick the name/mod id, fill in "For Fable: the film" (§7) with real paths, then build
  the NeoForge 1.21.1 base (§8). Fable's previz (Part 1) can start any time; it doesn't wait for code.

## 0. Rules (Adam's; also in `C:\Users\User\.claude\CLAUDE.md`)

- Short replies, answer first (he skims). **Every open question as clickable choices** (AskUserQuestion); re-ask
  anything he left unanswered.
- After coding: a list of **what he needs to do**; for testing: a list of **what to test**.
- Budget-conscious: say what a big step costs (time, usage, money) before it; don't over-test (he tests in game).
- **Never push, publish or upload without asking.** No GitHub repo yet. Commit as
  `Adamko-10 <Adamko-10@users.noreply.github.com>` (set in this repo's git config) with the session's Co-Authored-By
  line.
- **Never commit or ship other people's jars, code or assets** (`libs/*.jar` is git-ignored). Decompile only into a
  scratch folder outside the repo. Never use rimuru_dev's assets (textures, shaders, sounds, models, code): make our
  own in his style. NASA / ESO / Solar System Scope imagery is fine with credit (download from the source, ask first:
  file, source, size). **No Bedrock ports.** Credit TON 618 and RimuLib as required dependencies.
- New features on **NeoForge 1.21.1 first** (Adam plays it); other loaders only after he tests and approves.
- **Fable 5.2 makes the previz and the in-game film** (Adam runs it himself). Fable writes only in the film's own files
  (§7) and in `previz\`; anything else it wants goes to `reviews\fable\PROPOSALS.md`; bugs it sees go to
  `reviews\fable\FINDINGS.md`. **Opus builds everything else and applies only the proposals Adam picks.** Fable never
  refactors our code.
- Subagents only when Adam asks. Ask before deleting his files. Ask before installing anything on the VM.
- Ponytail: **off** for this creative work; tell Adam when to switch (lite for fixes, ports, releases, CI) and if a
  session hook says it came back on.
- Update this file at the end of every turn and commit it on its own.
- Adam plays on his own PC (Modrinth, NeoForge 1.21.1), not the VM: test jars go in
  `C:\Users\User\Desktop\test builds\<date> <what>\`; tell him the path.

## 1. What this is

An unofficial addon for **TON 618** by rimuru_dev (CurseForge 1798822, 1.1.0, All Rights Reserved; needs **RimuLib
1.2.3+**), the black-hole mod (see `docs/TON618-NOTES.md`). Second addon by Adam (adamko_10) after **The Shooting
Star Addition: SA Series** (repo on the VM: `C:\Users\User\Desktop\the shooting star addition on vm`, read-only for
us: the model for build, RimuLib hooks, films, configs, music).

**The main skill (decided by Adam, 2026-11-13)**: usable only **after TON 618 has been used** in that world. The
remote fast-forwards time so far that TON 618 **evaporates**. Real physics: **Hawking radiation** (§5). An
**educational mode** setting adds small labels pointing at the real physics (§6).

## 2. Decisions

- 2026-11-13: main skill = Hawking evaporation of TON 618, only after TON 618 was used (Adam).
- 2026-11-13: educational-mode setting (Adam: "an educational mode setting that just adds little text poiting towards
  something in physics").
- 2026-11-13: Fable 5.2 makes the previz and the film; Opus the rest (Adam: "keep in mind animation and previz will be
  made by fable").
- 2026-11-13: NeoForge 1.21.1 first.
- 2026-11-13: jars downloaded to `libs/` (Adam's OK).

## 3. Open questions (asked 2026-11-13; answers go to §2)

1. Which item fires it: TON 618's own item (its H menu), The Shooting Star's Stellar Remote, or both?
2. What "after TON 618 was used" means, and where it evaporates: the devoured world only (the burst brings the world
   back), also a finite-scale landing (TON618 would have to stay over its bowl), or any earlier use.
3. Where educational mode shows its notes.
4. Which extra ideas the addon gets (§10).
Later: the addon's name and mod id; the burst's effect on the world; music.

## 4. TON 618 facts

All in **`docs/TON618-NOTES.md`**. The ones that shape our design:
- A no-limit cast "devours" the world: a flag in vanilla command storage `ton618:ton618` (`devoured`, `at`) and every
  player moved to the dark dimension `ton618:devoured`, with TON618 in its sky. The overworld itself is untouched;
  `/ton618 restore` (ops) brings everyone back.
- A finite cast lands, carves a bowl and leaves at tick 484: nothing of TON618 stays.
- Its classes are one-letter obfuscated: only RimuLib's public API, vanilla data and its commands are safe to use.
- Decompiled source (temporary, session scratch, may be cleaned):
  `C:\Users\User\AppData\Local\Temp\claude\3892cc049d580274\12ad48ac-3d60-4406-91e3-b0b8bdcfabc4\scratchpad\ton618-src\`.
  To redo it: Vineflower 1.10.1 from the Gradle cache
  (`~/.gradle/caches/modules-2/files-2.1/org.vineflower/vineflower/1.10.1/.../vineflower-1.10.1.jar`), `-e=` the
  RimuLib jar, output to a scratch folder.

## 5. The evaporation skill (design in progress)

Physics to use (from the brief): near the horizon, pairs of particles keep appearing; sometimes one falls in and the
other escapes, so the hole slowly loses mass (Hawking radiation; quantum particle pairs, **not dark matter**). Smaller
= hotter = faster; the end is a sudden bright burst. TON 618: ~40-66 billion Suns, ~10^99-10^100 years to evaporate
(a googol = 10^100). Its Hawking temperature (~10^-18 K) is far below today's 2.7 K background, so today it still
grows; it only shrinks after the universe cools below that. The fast-forward can pass the eras: stars burn out
(~10^14 years), the degenerate era, the black-hole era, evaporation, the burst. A year counter racing up fits.

Ideas from reading TON 618 (to confirm with Adam): the evaporation is the way out of the devoured dark; the burst runs
`ton618 restore` (a public command) so the world comes back. Physics hook: the black hole information paradox (does
what fell in come back out in the radiation?) fits "the world comes back" as a fantasy, labelled honestly.

## 6. Educational mode

A setting (client side, off/on; default to be decided) that adds small physics labels to the film; maybe also to the
H-menu text, a fact card after the film or tooltips (question 3). The film reads it through one getter (§7).

## 7. For Fable: the film

**Draft (2026-11-13): the paths are fixed once Adam picks the name and mod id; until then Fable works on Part 1 (the
previz) only.** What will be here:
- the film's own Java package (`<base package>.client.film`, Fable's alone), its shader folder
  (`assets/<mod id>/shaders/film/`) and texture folder (`assets/<mod id>/textures/film/`), and its sounds' folder;
- how the film is started and stopped (from the skill's effect packet on the client, RimuLib `Cutscene`/`Shot`);
- the skill's clock: the cast tick, when time starts racing, when the burst lands, when the world comes back (from
  the approved previz's tick table, `previz\NOTES.md`);
- what the film can read: the caster, TON618's position and apparent size in the dark (TON 618 draws it at a fixed
  direction, see `docs/TON618-NOTES.md`), the educational-mode switch (one getter).

## 8. Building (planned)

Model: the SA Series `main` branch (`build.gradle`, `gradle/`; NeoForge 1.21.1, JDK 21 at `C:\Program Files\Eclipse
Adoptium\jdk-21.0.13.9-hotspot`). Join TON 618 through RimuLib's public API only (`RimulibApi.connect`,
`SkillApi.whenRegistered`/`extend`/`gate`, `NetApi`, `UiApi`/`MenuApi`, `FilmApi`, `FxApi`). Compile against
`libs/rimulib-1.2.3-neoforge.jar` (and TON 618's jar only at run time). No Node, ffmpeg or JDK 25 on the VM.

## 9. Files

- `BRIEF.md`: Adam's brief. `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`: Fable's prompt (Adam runs it).
- `docs/TON618-NOTES.md`: what TON 618 does.
- `libs/`: jars (git-ignored), `libs/README.txt` says where they came from.
- Fable's: `previz\` (Part 1), the film files (§7), `reviews\fable\PROPOSALS.md`, `reviews\fable\FINDINGS.md`.

## 10. Ideas (brainstorm, 2026-11-13; Adam picks)

- **Quasar Jets**: TON 618 is really a quasar, one of the brightest known; a skill firing its relativistic jets as a
  beam across the land.
- **Black hole merger**: a second black hole spirals into TON 618; the gravitational wave ripples the land and shakes
  everyone (LIGO-style chirp).
- **Time dilation**: near TON618 time runs slower: everything round the caster slows down.
- **Advancements and facts**: e.g. "A googol of years", "Information paradox"; small and cheap.
- Fits the SA Series too (tell Adam, keep apart): educational mode for its films (already in its `docs/NEXT-SKILLS.md`).
