# CLAUDE.md: TON 618 Addition

Handoff notes for every Claude session in this folder (`C:\Users\User\Desktop\ton 618 addition`, on Adam's VM).
Started 2026-11-13 by an Opus 5.5 session (xhigh) that was started from Adam's SA Series session. **Read `BRIEF.md`
first** (Adam's brief, his own words at its end), then this file. Fable's part: `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`.

### >>> RESUME HERE (2026-11-13 evening, the VM): first brainstorm done, nothing built yet <<<

- Done: read the brief; read TON 618's page; downloaded TON 618 1.1.0 and RimuLib 1.2.3 (NeoForge) into `libs/` (Adam's
  OK); decompiled TON 618 into the session scratch folder and wrote what it does in **`docs/TON618-NOTES.md`**; made
  this folder a git repo (`main`, no remote); Adam answered the first brainstorm round (§2).
- Next: Adam's go-ahead to build the NeoForge 1.21.1 base (§8), then ask the open questions in §3 at the moment they
  matter. Fable's previz (Part 1) can start any time (Adam runs it); it doesn't wait for code. §7 tells Fable where its
  film goes.

## 0. Rules (Adam's; also in `C:\Users\User\.claude\CLAUDE.md`)

- Short replies, answer first (he skims). **Every open question as clickable choices** (AskUserQuestion); re-ask
  anything he left unanswered.
- After coding: a list of **what he needs to do**; for testing: a list of **what to test**.
- Budget-conscious: say what a big step costs (time, usage, money) before it; don't over-test (he tests in game).
- **Never push, publish or upload without asking.** No GitHub repo yet. Commit as
  `Adamko-10 <Adamko-10@users.noreply.github.com>` (set in this repo's git config) with the session's Co-Authored-By
  line.
- **Never commit or ship other people's jars, code or assets** (`libs/*.jar` is git-ignored). Decompile only into a
  scratch folder outside the repo. Never use rimuru_dev's assets (textures, shaders, sounds, models, code), nor SS-21
  Quasar's: make our own. NASA / ESO / Solar System Scope imagery is fine with credit (download from the source, ask
  first: file, source, size). **No Bedrock ports.** Credit TON 618 and RimuLib as required dependencies (The Shooting
  Star and SS-21 Quasar as optional ones).
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

**TON 618 Addition** (Adam: exactly that, case sensitive; mod id `ton618_addition`), an unofficial addon for **TON
618** by rimuru_dev (CurseForge 1798822, 1.1.0, All Rights Reserved; needs **RimuLib 1.2.3+**), the black-hole mod (see
`docs/TON618-NOTES.md`). Adam's (adamko_10) second addon after **The Shooting Star Addition: SA Series** (repo on the
VM: `C:\Users\User\Desktop\the shooting star addition on vm`, read-only for us: the model for build, RimuLib hooks,
films, configs, music).

Skills carry a code like the SA Series: **TA-xx · Name** (TA = TON 618 Addition).

- **TA-01 · Last Light** (the main skill, §5): in the dark after TON 618 has devoured the world, it fast-forwards time
  until TON618 evaporates by **Hawking radiation**; the final burst brings the world back.
- **TA-02 · Quasar Jets** (later, §10): only when **SS-21 Quasar** (Aether21's TSS addon) is installed.
- **Educational mode** (§6): small physics labels in the film, off by default.

## 2. Decisions

- 2026-11-13: main skill = Hawking evaporation of TON 618, only after TON 618 was used (Adam).
- 2026-11-13: educational-mode setting (Adam: "an educational mode setting that just adds little text poiting towards
  something in physics").
- 2026-11-13: Fable 5.2 makes the previz and the film; Opus the rest (Adam: "keep in mind animation and previz will be
  made by fable").
- 2026-11-13: NeoForge 1.21.1 first. Jars downloaded to `libs/` (Adam's OK).
- 2026-11-13, first brainstorm round (Adam's picks):
  - **Which item fires it: both.** A skill on TON 618's own item (its H-menu page) always, and on The Shooting Star's
    Stellar Remote too when The Shooting Star is installed (optional dependency).
  - **When: only in the devoured world.** Usable only after a no-limit TON 618 has devoured the world (everyone in the
    dark dimension). The evaporation is the way out: **the final burst brings the world back** (we run TON 618's own
    `ton618 restore` as the server).
  - **Educational mode: film labels only, off by default.**
  - **Extras: Quasar Jets**, our own skill (own film and art, nothing of theirs), **only shown when SS-21 Quasar is
    installed** (Adam: "quasar jets is cool but only if the shooting star quasar addon is installed"). Other extras:
    "we will see about anything else" (§10).
  - **Name: "TON 618 Addition"** exactly (case sensitive). Mod id `ton618_addition`; Java package
    `dev.adamko.ton618addition` (Opus's pick, a conventional default).
  - **Skill name: Last Light**, with codes: **TA-01 · Last Light**, TA-02 · Quasar Jets.

## 3. Open questions (ask when they matter, as clickable choices)

1. **Who may cast Last Light on a server?** It brings the world back for everyone, which TON 618 leaves to operators
   (`/ton618 restore`). Everyone / operators only / a config switch (default?). Ask before building the server side.
2. Last Light's cooldown, and whether the caster needs anything (just holding the item?).
3. What the menu shows when the world is not devoured (locked with a reason, e.g. "TON618 hasn't devoured this world
   yet"; the SA Series shows locks the same way).
4. Music for the film (the SA Series has its own tracks); sounds are Fable's film work or ours?
5. Other extras later (§10).

## 4. TON 618 facts

All in **`docs/TON618-NOTES.md`**. The ones that shape our design:
- A no-limit cast "devours" the world at its tick 416: a flag in vanilla command storage `ton618:ton618` (`devoured`,
  `at`) and every player moved to the dark dimension `ton618:devoured` (void floor at y=0), with TON618 hanging in its
  sky (drawn by TON 618's own client code). The overworld itself is untouched; `/ton618 restore` (ops) brings everyone
  back to the overworld spawn; `/ton618 status` says whether it's devoured.
- In the dark, TON 618 runs its own time effects (drag, stop, rewind, gravitational waves): check they don't fight our
  film.
- A finite cast lands, carves a bowl and leaves at tick 484: nothing of TON618 stays (so Last Light is devoured-only).
- Its classes are one-letter obfuscated: only RimuLib's public API, vanilla data and its commands are safe to use.
- Decompiled source (temporary, session scratch, may be cleaned):
  `C:\Users\User\AppData\Local\Temp\claude\3892cc049d580274\12ad48ac-3d60-4406-91e3-b0b8bdcfabc4\scratchpad\ton618-src\`.
  To redo it: Vineflower 1.10.1 from the Gradle cache
  (`~/.gradle/caches/modules-2/files-2.1/org.vineflower/vineflower/1.10.1/.../vineflower-1.10.1.jar`), `-e=` the
  RimuLib jar, output to a scratch folder.

## 5. TA-01 · Last Light (design)

**What it does (agreed 2026-11-13):** the caster stands in TON 618's devoured dark, holding TON 618 (or the Stellar
Remote) with Last Light picked, and casts. Time fast-forwards so far that TON618 evaporates; at the final burst the
world comes back for everyone (server runs `ton618 restore`: the flag is cleared, everyone in the dark goes to the
overworld spawn). Locked anywhere else.

Physics to use (from the brief): near the horizon, pairs of particles keep appearing; sometimes one falls in and the
other escapes, so the hole slowly loses mass (Hawking radiation; quantum particle pairs, **not dark matter**). Smaller
= hotter = faster; the end is a sudden bright burst. TON 618: ~40-66 billion Suns, ~10^99-10^100 years to evaporate
(a googol = 10^100). Its Hawking temperature (~10^-18 K) is far below today's 2.7 K background, so today it still
grows; it only shrinks after the universe cools below that. The fast-forward can pass the eras: stars burn out
(~10^14 years), the degenerate era, the black-hole era, evaporation, the burst. A year counter racing up fits.
"The world comes back" is the fantasy part; the honest physics hook is the **black hole information paradox** (does
what fell in come back out in the radiation? still debated): a good educational label.

How it will join (to build, §8): `SkillApi.whenRegistered("ton618", ...)` + `extend` (TON 618's set `ton618:ton_618`)
and, when The Shooting Star is loaded, `SkillApi.whenRegistered("shooting_star_demo", ...)` + `extend` on the Stellar
Remote set (as the SA Series does). Devoured check: the command storage flag (or `ton618 status`). Burst: run
`ton618 restore` from the server's own command source.

## 6. Educational mode

A client setting, **off by default** (Adam, 2026-11-13), that adds small physics labels **in the film only**. The
film reads it through one getter (§7). Fits the SA Series too (its `docs/NEXT-SKILLS.md` already has the idea): kept
apart, told to Adam.

## 7. For Fable: the film

**Status 2026-11-13: names and folders fixed; no code yet.** Fable: do Part 1 (the previz) now; Part 2 waits until
the base is built and this section says "ready".

- **The film's own files (Fable's alone):**
  - Java package `dev.adamko.ton618addition.client.film` (folder `src/main/java/dev/adamko/ton618addition/client/film/`);
  - shaders `src/main/resources/assets/ton618_addition/shaders/film/`;
  - textures `src/main/resources/assets/ton618_addition/textures/film/`;
  - film sounds `src/main/resources/assets/ton618_addition/sounds/film/` (their `sounds.json` entries and
    registration: a proposal for Opus, until §8 gives the film its own list).
  Anything else: a proposal in `reviews\fable\PROPOSALS.md`.
- **What the previz should know now:**
  - It starts in TON 618's **devoured dark**: a void floor, no light, TON618 hanging in the sky about 1000 blocks away
    in the direction (0.34, 0.2, -1) from 0/0 (look at TON 618's CurseForge gallery for how its dark looks; copy
    nothing). The caster holds **TON 618's item or the Stellar Remote** (both fire it): film the press neutrally or
    with both items in mind.
  - It **ends back in the overworld**: at the burst the server brings everyone back to the overworld spawn. Plan the
    burst as the hand-over (a white-out that clears onto the world is natural).
  - Title: **TA-01 · Last Light**. Educational labels off by default (the previz switch shows both).
- **Planned contract (built with the base, §8; may change, this section will say):**
  - start/stop: our client packet handler calls `LastLightFilm.start(LastLightCast cast)` in the film package when the
    cast reaches the client, and `LastLightFilm.stop()` if it is cancelled or the player leaves;
  - the skill's clock: ticks from the cast (20 a second), with the key ticks (time starts racing, the burst, the world
    comes back) taken from the approved previz's tick table (`previz\NOTES.md`) into one constants class Opus owns;
  - what the film can read from `LastLightCast`: the caster, the cast tick, TON618's position and apparent size in the
    dark, and `Ton618AdditionClientConfig.educational()`.

## 8. Building (planned)

Model: the SA Series `main` branch (`build.gradle`, `gradle/`; NeoForge 1.21.1, JDK 21 at `C:\Program Files\Eclipse
Adoptium\jdk-21.0.13.9-hotspot`). Join TON 618 through RimuLib's public API only (`RimulibApi.connect`,
`SkillApi.whenRegistered`/`extend`/`gate`, `NetApi`, `UiApi`/`MenuApi`, `FilmApi`, `FxApi`). Compile against
`libs/rimulib-1.2.3-neoforge.jar` (TON 618's and The Shooting Star's jars only at run time). Dependencies: TON 618
`[1.1.0,)` and RimuLib `[1.2.3,2.0)` required; The Shooting Star and SS-21 Quasar optional. No Node, ffmpeg or JDK 25
on the VM.

## 9. Files

- `BRIEF.md`: Adam's brief. `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`: Fable's prompt (Adam runs it).
- `docs/TON618-NOTES.md`: what TON 618 does.
- `libs/`: jars (git-ignored), `libs/README.txt` says where they came from.
- Fable's: `previz\` (Part 1), the film files (§7), `reviews\fable\PROPOSALS.md`, `reviews\fable\FINDINGS.md`.

## 10. Ideas

- **TA-02 · Quasar Jets** (picked 2026-11-13, after Last Light): TON 618 is really a quasar, one of the brightest
  known; a skill firing its relativistic jets as a beam across the land. Our own film and art; **only shown when SS-21
  Quasar is installed** (Aether21, CurseForge 1739264, "The Shooting Star Addition [SS-21 Quasar]", 1.8.0 has NeoForge
  1.21.1 since 2026-11-12, file 9575770; a TSS addon with its own black-hole jet strike). Detect it by mod id (check
  its id in its jar when we get there; ask before downloading). Note: SS-21 joins the Stellar Remote by reflection and
  can hide later API skills from the H menu (SA Series `docs/ADDONS.md`); the SA Series has a workaround
  (`TssLink.showInMenu`) if our Stellar Remote skill goes missing next to it.
- Not picked yet ("we will see"): **Black hole merger** (gravitational wave ripples the land), **Time dilation**
  (everything round the caster slows), **Advancements** ("A googol of years", "Information paradox").
