# Prompt for Claude Fable 5.2: the TON 618 addon's previz and animation

**How to run it (Adam):** in the Claude desktop app's Code tab, start a new session in the folder
`C:\Users\User\Desktop\ton 618 addition` (on the VM), model **Fable 5.2**, effort **High**, ponytail **off** (if the
session says ponytail is on, type "stop ponytail"). Send it:

> Read prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md and do Part 1 (the previz), as it says below the line.

Part 1 (the previz) can start now. When the previz is approved and the TON 618 Opus session says the mod is ready for
its film (CLAUDE.md §7 says "ready"), send the same session (or a new Fable one): "Do Part 2 of
prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md."

Written 2026-11-13 by the SA Series session, from Adam's words (quoted below); updated the same evening with Adam's
first brainstorm picks (TA-01 · Last Light, the devoured world, the world coming back).

---

Hi! I'm Adam (adamko_10). I make **The Shooting Star Addition: SA Series**, an addon for rimuru_dev's Minecraft mod
The Shooting Star. A separate Claude Opus session is now making an addon for rimuru_dev's newer mod **TON 618**, in
this folder. **You make that addon's previz and its animation (the in-game cutscene).** The Opus session builds
everything else.

My words: "i do want fable 5.2 to handle making the cutscene alright? the previz and then maybe overseeing code and
telling us about bugs it sees but without changing the code since i dont wanna have it refactor everything", and
later: "keep in mind animation and previz will be made by fable".

## Your rules

- **Write only in your own files.** Part 1: `previz\`. Part 2: only the film's own files, which the Opus session lists
  in this folder's `CLAUDE.md` under "For Fable: the film". Never rename, move, reformat or "clean up" anything else.
  If you need a change outside your files, write it as a proposal (what, where, why, the patch) to
  `reviews\fable\PROPOSALS.md`; the Opus session applies the ones I pick.
- **Keep notes as you go** in `previz\HANDOVER.md`: what's done, what's next, decisions, my feedback. If your usage
  runs out mid-way, the next session must be able to pick up from that file alone.
- Read first: `CLAUDE.md` (the Opus session's handoff; §1, §2, §5, §6 and above all **§7 "For Fable: the film"**,
  which wins over this prompt where they differ), `docs/TON618-NOTES.md` (what TON 618 does and how its devoured dark
  works) and `BRIEF.md` (the addon's brief).
- Never use rimuru_dev's assets (textures, shaders, sounds, models, code). Make our own in his style. NASA, ESO and
  Solar System Scope imagery is fine with credit (Solar System Scope: CC BY 4.0): ask me before downloading anything
  (file, source, size).
- Don't push, publish or upload anything except the previz as a private artifact (as you did for SA-05/06).
- Keep replies short (I skim). Ask open questions as clickable choices.
- This VM has **no Node and no ffmpeg** (ask me before installing anything). PowerShell and Git Bash are here; JDK 21
  at `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`.

## The skill to film: TA-01 · Last Light

**Decided with Adam (2026-11-13; details in `CLAUDE.md` §2, §5, §7):**
- The addon is **TON 618 Addition**; the skill is **TA-01 · Last Light** (title card).
- It only works **in TON 618's devoured world**: after a no-limit TON 618 has devoured the world, every player is in
  its dark dimension (a void floor, no light, TON618 hanging in the sky; see TON 618's CurseForge gallery for how it
  looks, copy nothing). The film **starts there**.
- It's fired from **TON 618's own item** (its H menu) **or The Shooting Star's Stellar Remote** (when that's
  installed): film the press so it works with either.
- Time fast-forwards until TON618 evaporates; **the final burst brings the world back** for everyone (the server sends
  everyone to the overworld spawn). So the film **ends back in the overworld**: the burst is the hand-over (for
  example a white-out that clears onto the world).
- Educational labels: in the film only, off by default (the previz has the switch). The world coming back is the
  fantasy part; the honest physics hook for a label is the **black hole information paradox** (does what fell in come
  back out in the radiation? still debated).

Adam's original idea: a new skill that can only be used **after TON 618 has been used** in that world: the remote
fast-forwards time so far that the black hole evaporates. My words: "you know how black holes slowly disappear because tiny particles of dark
matter go apart and smash back together extremely fast and it disintegrates a tiny amount of matter in physics, lets
make it so when ton 618 is used you can use the remote to fast forward time so extremely fast that it disintegrates
the black hole".

The real physics is **Hawking radiation**: near the event horizon, pairs of particles keep appearing; sometimes one
falls in and the other escapes, so the black hole slowly loses mass and evaporates. (They're quantum particle pairs,
not dark matter: the film's text uses the right physics.) Material to use as you see fit:
- the smaller a black hole gets, the hotter it is and the faster it evaporates; the very end is a sudden bright burst;
- TON 618 is one of the heaviest black holes known (estimates run from about 40 to 66 billion Suns); one that heavy
  takes around 10^99 to 10^100 years to evaporate (a googol of years is 10^100);
- its Hawking temperature is about 10^-18 K, far colder than today's universe (2.7 K), so today it still grows; it only
  starts shrinking once the universe has cooled below that. So the fast-forward could pass through the eras: the stars
  burn out (about 10^14 years), the dark "degenerate" era, the black-hole era, then the evaporation and the burst;
- a year counter racing up (the SA Series films have one) fits.

**Educational mode**: a setting that adds small labels in the film pointing at the real physics (my words: "an
educational mode setting that just adds little text poiting towards something in physics"). The previz gets a switch
to show the film with and without them.

How TON 618 looks: read its CurseForge page and gallery (https://www.curseforge.com/minecraft/mc-mods/ton618) and
`docs/TON618-NOTES.md` (the Opus session read its decompiled jar; `CLAUDE.md` §4 says where the decompiled source is,
if it's still there). TON 618 runs its own time effects in the dark (drag, stop, rewind, gravitational waves): plan
the film so it doesn't fight them. Match the style; copy nothing.

## Part 1: the previz (now)

A browser mock-up of the film, like the ones you made for SA-05/06 (the Great Attractor, version 2) and the one for
SA-04 Proxima:
- three.js on one page, shot by shot, with play, pause and a scrubber; timings in game ticks (20 a second);
- each shot's planned sounds and music cues listed on the page; title cards; the HUD words;
- the educational-mode switch;
- published as a private artifact; give me the link. Source in `previz\` (one HTML file with everything inlined is
  easiest here, with no Node to build it);
- a tick table in `previz\NOTES.md`: each shot's start and end tick, the camera, what's on screen, the sounds, and
  how each effect would be drawn in the game (Part 2 and the Opus session build from it).

Show me, take my notes, revise until I approve it, then stop.

## Part 2: the animation (only after I approve the previz and the Opus session says the mod is ready)

Build the in-game film to match the approved previz:
- RimuLib's film tools (its public API first: `FilmApi`, `FxApi`, `UiApi`; docs https://docs.rimuru.cyou/rimulib/)
  and our own shaders, as the SA Series builds its films. Read, never edit, the SA Series repo at
  `C:\Users\User\Desktop\the shooting star addition on vm`: `docs/SS-07-PROXIMA.md`, `client/film/ProximaStoryboard`,
  `client/proxima/ProximaCosmos` (a space scene drawn in the film's overlay pass), `compat/tss/client/FilmTitle`
  (title cards), `client/StrikeMusic`;
- only in the film's files listed under "For Fable: the film" in `CLAUDE.md`; the skill's clock, the caster and the
  black hole come from there too;
- build with `./gradlew --no-daemon build` (JDK 21); put a test jar in `C:\Users\User\Desktop\test builds\<date>
  TON 618 film\` and tell me the path. I test it in my game on my own PC;
- write what you built and how it works in `previz\HANDOVER.md`, and the hand-over to the Opus session (anything to
  wire up) in `reviews\fable\PROPOSALS.md`.

## After the film

You may read the rest of the code and report bugs to `reviews\fable\FINDINGS.md` (each with the file, what goes wrong
and how to see it), **without changing any code outside your film files**.
