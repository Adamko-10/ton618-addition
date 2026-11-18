# Prompt for Claude Fable 5.2: polish of the Last Light film (TON 618 Addition, next update)

Written 2026-11-15 by the Opus session that runs both of Adam's projects. Run in a Claude Code session on Adam's PC in
`C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition`, model Claude Fable 5.2, effort High. No ponytail (Adam's
rule in every project: if a session hook turns it on, turn it off).

---

Hi! You made the **TA-01 · Last Light** film for Adam's addon **TON 618 Addition** (previz v2 approved, then Part 2 in
the game). Version 1.0.0 is now released on all 7 loaders. A game test by Codex found two small things in the film;
please polish them for the next update.

**Read first**
- `CLAUDE.md`: the top "RESUME HERE" sections, §7 "For Fable: the film" (your files, the contract, the clock, what the
  film can read) and §0 (the rules).
- `previz/NOTES.md` (your tick table) and `previz/HANDOVER.md`, and the previz itself (`previz/last-light-previz.html`;
  published: https://claude.ai/artifact/Ta6Z91SRwtCYN1z27zKQtJ, read it with the Artifact tool).
- Codex's test: `C:\Users\adam_\Documents\TON 618 Addition\codex tests\REPORT.md` and the contact sheets
  `...\codex tests\screenshots\<nn>-<build>\film-contact-sheet.jpg` (frames at 3, 5, 8, 12, 18, 24, 29, 33, 35, 38,
  41 s; the full screenshots are beside them).

**What to polish (on `main` = NeoForge 1.21.1 only)**
1. **The early world effects read as faint.** On every version, NeoForge included, Codex called the **hand glow**, the
   **floor ring** and the **outgoing floor wave** "indistinct" in the frames from about 4.5 to 8.6 s (ticks ~90-172,
   around PRESS 78 and RACE 118). Compare what the game draws with the previz in those shots and make them read
   clearly, as the previz intended (remember the dark has no real floor; TON 618's dark sky and its accretion disk are
   behind). Keep the rest of the film as Adam approved it.
2. **Minecraft's toasts sit on top of the film** (the "Move with W, A, S and D" tutorial popup on a new game; any
   advancement or recipe toast could do the same). Adam wants them hidden during the film. If it can be done inside
   the film's own files (e.g. clearing the toasts when `LastLightFilm.start(...)` runs, and keeping new ones from
   showing while it plays), do it there; if it needs anything outside your files, write it as a proposal instead.

**Rules (as before)**
- Write **only** in the film's own files (CLAUDE.md §7: `client/film/`, `shaders/film/`, `textures/film/`,
  `sounds/film/`) and in `previz/`. Anything else you need: a proposal in `reviews/fable/PROPOSALS.md` (what, where,
  why, the patch); bugs you notice elsewhere: `reviews/fable/FINDINGS.md`. Never refactor other code.
- **No git**: don't commit, branch, push or touch the port branches or `ports/`. The Opus session commits your work,
  ports it to the six other loaders and builds those.
- Build: `JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot" ./gradlew --no-daemon build`
  (`libs/` already holds TON 618 1.1.0 and RimuLib 1.2.3). **Ask Adam before `runClient`** or any game run: it opens a
  window on his screen and he may be playing.
- Put the test jar in `C:\Users\adam_\Documents\TON 618 Addition\test builds\<date> Last Light polish\` named
  `TON 618 Addition 1.0.1 polish TEST [NeoForge 1.21.1].jar`, and tell Adam its path and a short "what to test" list.
- Adam skims: short replies, answer first, every question as clickable choices (AskUserQuestion).

**When done**: add a "Polish (2026-11)" section to `previz/HANDOVER.md`: what you changed and why, in which files, and
anything the ports must do differently (1.20.1's vertex API, 1.21.11/26.3's render pipelines; see CLAUDE.md §8a).
