# CLAUDE.md: TON 618 Addition

Handoff notes for every Claude session in this folder (`C:\Users\User\Desktop\ton 618 addition`, on Adam's VM).
Started 2026-11-13 by an Opus 5.5 session (xhigh) that was started from Adam's SA Series session. **Read `BRIEF.md`
first** (Adam's brief, his own words at its end), then this file. Fable's part: `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`.

### >>> RESUME HERE (2026-11-14 evening, the VM): NeoForge 1.21.1 approved; GitHub repo; then the 7 ports; then release <<<

- **Adam tested the cast fix: "it works, i say we post it"** (via the SA Series session). NeoForge 1.21.1 (0.1.0 cast
  fix, `0e8c7e1`) is approved.
- **His release picks (2026-11-14, clickable)**:
  - **Port all eight first, post them together** (not NeoForge now). Told the cost: very roughly 8-14 h of session
    time over several sessions and a large share of usage; the film's drawing code is hardest on 1.21.11/26.3.
  - **A branch per loader**, like the SA Series: `main` = NeoForge 1.21.1, plus `forge-1.21.1`, `forge-1.20.1`,
    `neoforge-1.20.1`, `fabric-1.21.1`, `fabric-1.20.1`, `fabric-1.21.11`, `fabric-26.3`; changes cherry-picked across.
  - **Opus ports everything, Fable's film files included** (as they are, changing only what each loader needs).
  - **Public GitHub repo now**: "Yes, create and push" `Adamko-10/ton618-addition` with everything in the folder
    except `libs/` jars (he was told BRIEF.md, CLAUDE.md, prompts, previz, docs and Fable's notes go public). The
    **v1.0.0 GitHub release** comes later with CurseForge.
  - **CurseForge: Opus in his browser** (he signs in himself; Opus fills the project form and uploads, asking his yes
    before each submit). **Page header like the SA Series'** (Discord, YouTube, other mods). Version **1.0.0,
    Release**; Opus drafts the page text for his review.

- **Done (2026-11-14 evening)**: public repo **https://github.com/Adamko-10/ton618-addition** created and `main`
  pushed (Adam: "Yes, create and push"; files checked: no secrets, commits by the noreply address). **Standing OK for
  CI**: "Yes, wip pushes are fine" (`wip/*` branches and CI draft releases on this repo; real branches, tags,
  releases and CurseForge still wait for his yes each time).
- **Release drafts** in `dev/publish/` (as the SA Series): `curseforge-description.md`, `summary.txt` (235 chars),
  `changelog-1.0.0.md` (template: `{MC}`, `{LOADER}`, `{ALSO}`, `{FABRIC_API}`), `project-icon.png` (400×400, from
  `tools/icons/LastLightIcon.java --size=400x400`). Adam: "just cross reference with shooting star addition SA
  series": done against the SA repo copy and its **live** page (summary starts with the name and ends "Needs
  RimuLib."; header lines; builds list; "The H menu" like SA's "The remote's menu"; "More"; credits and the
  distribution line word for word). Categories to pick on the site: **Addons, Magic, Technology** (the SA page's).
  Not yet re-shown to Adam after the cross-reference.
- **7 jars, not 8** (2026-11-14, from the SA precedent): SA's **Forge 1.20.1 jar also runs on NeoForge 1.20.1**
  (NeoForge 1.20.1 is the Forge 47.1 fork), so no `neoforge-1.20.1` branch. Branches: `main` (NeoForge 1.21.1),
  `forge-1.21.1`, `forge-1.20.1` (also NeoForge 1.20.1), `fabric-1.21.1`, `fabric-1.20.1`, `fabric-1.21.11`,
  `fabric-26.3`. Tell Adam.

### Before that (2026-11-14 ~16:15): "can't trigger it" fix built (`0e8c7e1`)

- **Adam's bug (via the SA Series session, logs in `C:\Users\User\Downloads\logs\`)**: "idk why it isnt working i
  cant trigger it". His answers (clickable): "i wasnt able to cast it" (nothing showed), pressed J: "Not sure",
  TON 618's own film: "Yes, it played", cast by right-click. The **server did cast it** (13:52:00, "1 player(s) see
  it"), 6 s after he landed in the dark; he quit 15 s later. No error from us. His profile also has two RimuLibs (1.2.2
  and 1.2.3; NeoForge took 1.2.3; the SA Series session tells him to disable 1.2.2), Iris, Sodium, C2ME, Distant
  Horizons, ModernFix, a server lagging up to 106 ticks.
- **Why nothing showed** (read from RimuLib 1.2.3's `CutsceneDirector.play`): RimuLib plays **one film at a time** and
  queues a new film behind **any** film still running, whatever its priority; TON 618's own film runs on for ~6 s in
  the dark after the devour (its shots to tick 530), so Last Light's film waited unseen (Fable's screen layer draws
  only while RimuLib shows our film; its soundtrack only with a film). Also: with RimuLib's films **off** (its key J;
  in the dark its "Spell cutscenes: OFF" message is hidden with the action bar) the film never starts at all. In the
  dark every action-bar message is hidden too (TON 618's HUD cancel), so a cast gave no feedback.
- **Fix (`0e8c7e1`, ours, outside the film)**: `Ton618AdditionClient.begin` logs "TA-01 · Last Light reached this game
  at film tick N (films on/off; another film was playing and was skipped)", and if a film is running calls
  `FilmApi.skip()` (public; works once that film is skippable) before `LastLightFilm.start`; if films get switched on
  mid-cast it calls `LastLightFilm.start(current)` again (mid-way start, the contract allows it).
  `client/LastLightNotice`: a small card at the top, drawn after the HUD by `HudRescue` (so also in the dark), while
  a cast runs **without** a film showing (`!FilmApi.active()`), until the burst: "TA-01 · LAST LIGHT / TON618 is
  evaporating · the world comes back in N s / Films are off: press J (RimuLib's film key) to watch it" (or "Its film
  isn't showing (skipped, or waiting for another film)"). `RimuLib`'s `clear()` only clears films owned by the
  calling mod, so TON 618's film can't be cleared, only skipped.
- **Test jar**: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 cast fix\TON 618 Addition 0.1.0 cast fix
  [NeoForge 1.21.1].jar` (replaces all earlier ones; the SA Series session copies it to Downloads).
- Next time it fails: his `latest.log` now has the client line above; look for it after the server's cast line.

### Before that (2026-11-14 ~13:50): icon + unlock note built (`53e9726`)

- **Adam's test of TON 618's H menu (screenshot, via the SA Series session)**: "fix this and also add a little note
  somewhere that tells people that u have to set the blackhole to infinite power and then use it and THEN finally u
  can use last light (im talking about the UI)".
  - **Missing icon fixed**: RimuLib draws a skill's icon from the gui sprites of its **set's** namespace (as the SA
    Series ships `shooting_star_demo:.../skill/stellar_remote/<id>.png`). Ours: `assets/ton618/textures/gui/sprites/
    skill/ton_618/last_light.png` and `assets/shooting_star_demo/textures/gui/sprites/skill/stellar_remote/
    last_light.png`, 256×128 (the menu's icons are 2:1), our own art drawn by code: `tools/icons/LastLightIcon.java`
    (`java tools/icons/LastLightIcon.java <out.png>...`): the last flash, its burst ring, Hawking pairs, stars. A
    candidate for Fable to redo nicer later.
  - **The red triangle** right of the row is RimuLib's normal unofficial-addon marker (`AddonSkills`, the SA Series'
    skills have it too): nothing to fix.
  - **Unlock note**: the plate (`EducationPlate`) shows, under the switch, "How to unlock Last Light: 1. Set TON618's
    Scale to ∞ (no limit) 2. Use TON618: it devours the world 3. In the dark, cast Last Light" whenever the player is
    not in the dark, and "In the dark: Last Light is ready" in the dark. Same wording in the menu's select warning
    ("Locked: set TON618's Scale to ∞ and use it, then cast Last Light in the dark"), the early-cast message and both
    skill descriptions (lang).
- **Test jar**: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 icon and unlock note\TON 618 Addition 0.1.0 icon
  and unlock note [NeoForge 1.21.1].jar` (replaces all earlier ones; the SA Series session copies it to Downloads).

### Before that (2026-11-14 ~13:20): edu plate moved + new key

- **Adam on the first switch jar**: "can u make the key not J since the cutscene key uses J and move the ui to the top
  right for now like the educational mode ui". The key was "]", whose cap reads as "J" in Minecraft's small font. Now
  (`client/EducationPlate`): key **"\"** (GLFW 92), the plate in the menu's **top right** corner (accent bar on its
  right edge, like the SA Series' right plate). On the Stellar Remote it shows only while Last Light is the picked
  skill (`ClientSkills.selected`, RimuLib internal, guarded), because the SA Series' Convergence plate takes that
  corner when Stellar Convergence is picked; on TON 618's item it always shows.
- **Test jar**: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 educational mode 2\TON 618 Addition 0.1.0
  educational mode 2 [NeoForge 1.21.1].jar` (replaces the one below).

### Before that (2026-11-14 ~13:00): educational-mode switch built (`5e623cd`)

- **Adam tested the menu fix: "it works"** (2026-11-14, via the SA Series session). He then noticed "theres no
  education mode" (only NeoForge's Config screen had it) and picked (clickable, in the SA Series session):
  **"Switch in the H menu"**; and "write down real music quasar jets for another update, for now just educational
  mode then we port and post it on curseforge". "Waves affect the world" was offered and **not** picked: leave it.
- **The plan now**: (1) educational-mode switch → test jar (done, below); (2) **port** to the other loaders; (3)
  **post on CurseForge**. Before porting and before anything goes on CurseForge, ask Adam (clickable): which loaders
  and versions, a GitHub repo for CI (no JDK 25 on the VM), the project's name, summary, description; publishing waits
  for his yes. **Later update**: real film music (§3 Q1) and TA-02 · Quasar Jets (§10).
- **Built (`5e623cd`)**: `client/EducationPlate`: a small plate in the H menu's **bottom left** (the SA Series' plates
  take the top corners), shown while the menu is open for TON 618's item or the Stellar Remote; key **"]"** flips
  educational mode (saved to the client toml; off by default), with a click and a sliding switch like the SA plate.
  (Superseded: now top right, key "\", see above.) Drawn and keyed with `UiApi.onMenuDraw` + `MenuApi.onKey`; never takes a key the menu, the films or a shown
  skill uses, or while a key is being rebound. In the dark it shows too (HudRescue draws the menu).
- **Keys moved**: Last Light's default keys were B (TON 618's item) and V (Stellar Remote), but RimuLib's skill keys
  pick skills inside the H menu, and the SA Series uses **B** (Stellar Convergence) and **V** (its Convergence plate's
  hide key; plus T, R, X there and M, G, L, F, E, C, -, =, [ on its other plate): V on the remote would have taken
  that plate's key. Now **";"** (TON 618's item) and **"'"** (the remote). Every letter is taken in that menu by
  someone. Players who already ran an older jar keep their saved B/V until they reset them in Controls.
- **Test jar**: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 educational mode\TON 618 Addition 0.1.0
  educational mode [NeoForge 1.21.1].jar` (the SA Series session copies test jars to Downloads for Adam).
- **Adam's port and CurseForge picks (2026-11-14, clickable)**:
  - **Port to all of TON 618's targets**: Forge 1.21.1; NeoForge + Forge 1.20.1; Fabric 1.20.1 + 1.21.1; Fabric
    1.21.11 + 26.3 (eight jars with NeoForge 1.21.1; told: very roughly 1-2 h of session time each).
  - **A public GitHub repo + CI**, like the SA Series (`Adamko-10/ton618-addition` suggested); nothing pushed until he
    says so. CI builds the JDK 25 targets.
  - **First CurseForge version: 1.0.0, Release** (Client + Server, as the SA Series).
  - **Page text: Opus drafts** the summary and description (crediting rimuru_dev's TON 618 and RimuLib) as a file for
    Adam to review; nothing posted without his yes.
  - Porting starts after Adam's test of the educational-mode jar (features are ported once approved, §0).

### Before that (2026-11-14 ~12:00): menu fix built (`f6ef1bd`); Adam: "it works"

- **Adam's bug (2026-11-14, via the SA Series session)**: "when ton 618 eats your world you cant open the ui you just
  dont see it". **Cause** (read from the jars): TON 618 1.1.0's `GuiMixin` cancels `Gui.render` at its HEAD whenever
  the client is in `ton618:devoured` **or** a RimuLib film hides the HUD (`CutsceneDirector.hideHud()`: a film with
  weight > 0.35), drawing only its own overlay. RimuLib draws its H menu at the **TAIL** of `Gui.render`
  (`WhiteSpace.render`, its `GuiMixin`, priority 1500), and NeoForge fires `RenderGuiEvent.Pre/Post` inside it, so
  both are skipped: the menu is invisible in the dark, and Fable's film screen layer (space scene, words, white; it
  draws on `RenderGuiEvent`) would be invisible in the dark and during the whole film.
- **Fix (ours, `f6ef1bd`)**: `client/HudRescue` + two optional client mixins (`ton618_addition.mixins.json`,
  `defaultRequire 0`): `client/mixin/GuiMixin` (TAIL of `Gui.render`: "reached the end") and
  `client/mixin/GameRendererMixin` (MixinExtras `@WrapOperation` round `GameRenderer.render`'s `Gui.render` call). When
  `Gui.render` was cut short: post a **cancelled** `RenderGuiEvent.Pre` (only listeners asking for cancelled events
  hear it: Fable's film does, so it draws; other mods' HUDs stay hidden), then `WhiteSpace.beforeHud()` +
  `WhiteSpace.render(g, partial(true))` (RimuLib internals, guarded: a warning if they change). The vanilla HUD stays
  hidden in the dark, as TON 618 wants. Builds; a dev server starts clean; **never seen in a game** (no OpenGL on the
  VM).
- **Workaround with the older jars** (no fix): hold TON 618 in the dark, **shift + right-click** once (switches to the
  next skill: Last Light), then **right-click** to cast (TON 618's own item does both; its action-bar messages are
  hidden in the dark too).
- **Test this jar** (newest; replaces all below): `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 menu fix\TON
  618 Addition 0.1.0 menu fix [NeoForge 1.21.1].jar` (not in Downloads: Opus's copy there was blocked earlier, §0).
- Worth telling rimuru_dev one day (TON 618's bug, not ours): its dark hides RimuLib's own menu.

### Before that (2026-11-14 ~11:40): film + P1 built (`0f0e737`)

- Its jar: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 film P1\TON 618 Addition 0.1.0 film P1 [NeoForge
  1.21.1].jar`.
- **P1 applied** (Adam: "Add it now", 2026-11-14): `net/LastLightEnd` (cast game time), declared in
  `Ton618Link.install`; `LastLight` keeps the cast's watchers and, when the world comes back before RESTORE (an op's
  `/ton618 restore`) or the restore fails, drops the cast and sends `LastLightEnd` to them; `Ton618AdditionClient`
  then calls `stop()`. Dev server starts clean with it.
- `AGENTS.md`: Adam's own Codex import ("its from me importing into codex, honestly just delete it"): moved to the
  Recycle Bin, never committed.

### Before that (2026-11-14 ~11:20): Fable's film is in (`2551dda`)

- **Fable's Part 2 is done and committed** (`2551dda`, committed by Opus for Fable): the in-game Last Light film in
  `client/film/` (LastLightFilm rewritten + FilmScript, FilmCamera, FilmWorld, FilmSpace, FilmHud, FilmSound,
  FilmSynth, FilmGl) and `shaders/film/last_light.*`. It uses RimuLib's public `FilmApi`/`UiApi`, **no sounds.json, no
  sound files, no textures** (the soundtrack is made by code, played through its own `SoundInstance`); render paths
  catch their errors. Fable's notes: `previz/HANDOVER.md` "PART 2", `reviews/fable/PROPOSALS.md`. Builds; **never run
  in a game** (the VM has no OpenGL: `runClient` fails at GLFW).
- **Film test jar** (Fable's): `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 film\TON 618 Addition 0.1.0 film
  [NeoForge 1.21.1].jar` (copy in Downloads). It replaces the base jar below.
- Waiting for Adam: music (§3 Q1). (P1: applied, above.)
- Fable's known limits for the test: with a shader pack (Iris) the in-world parts (waves, the hand's glow) may not
  show; TON 618's own dark effects can still play over the first 200 ticks; players with RimuLib films off, or who
  skip, see only the white of the burst.

### Earlier (2026-11-14 ~00:20): the base was built (0.1.0)

- **Built (2026-11-14, `a786dc6`)**: TA-01 · Last Light on NeoForge 1.21.1 (§8): joins TON 618's menu and the Stellar
  Remote, castable only in TON 618's dark, film to everyone in the dark, sunrise + `ton618 restore` at tick 690, server
  and client configs, the film's contract and a **placeholder film** (bars, year counter, title, white, labels as
  text) in Fable's package. Compiles; a dev server (TON 618 1.1.0 + RimuLib 1.2.3) logged "TA-01 · Last Light joined
  TON 618's menu (ton618:ton_618) as skill #2". **Not yet cast in a game** (Adam tests).
- **Test jar**: `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 Addition base\TON 618 Addition 0.1.0 [NeoForge
  1.21.1].jar`. Adam's profile needs TON 618 1.1.0 and **RimuLib 1.2.3** (TON 618 needs it; the SA Series accepts it).
- Fable's previz v2 is approved (`previz/`, committed for Fable in `7319b36`); §7 says **ready**: Adam can send Fable
  "Do Part 2 of prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md".
- 2026-11-14 ~00:30: at Adam's word ("yes send it") the idle Fable session (`local_3da78c21-...`, "Fable: TA-01 Last
  Light previz") was sent "Do Part 2" with a pointer to §7; its turn started.
- Next: Adam's test results; Fable's film (its proposals in `reviews\fable\PROPOSALS.md`); the open questions in §3
  when they matter; then TA-02 · Quasar Jets (§10).

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
- **No ponytail** (Adam, 2026-11-14: "lets just not use ponytail", then "every project": now also in his global
  `C:\Users\User\.claude\CLAUDE.md`). Keep it off and don't advise switching levels; if a session hook turns it back
  on, turn it off ("stop ponytail") without asking him.
- On the VM Adam wants test jars in **Downloads** too (his global rules, 2026-11-13), with a copy in `test builds\`.
  2026-11-14: copying 0.1.0 to `C:\Users\User\Downloads\` was blocked by the session's permission check; it is only in
  `test builds\` (Adam told).
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
- 2026-11-13, Fable's previz v2 approved by Adam (`previz/NOTES.md`): 850 ticks, 10 shots, two waves (pictures only),
  everyone in the dark sees the whole film, **the return is always a morning**.
- 2026-11-14 (Adam, before the build): **who may cast = a server setting, default everyone**
  (`who_can_cast = EVERYONE|OPERATORS`); **who watches = everyone in the dark**.
- 2026-11-14 (Adam): educational mode gets a **switch in the H menu** (still off by default); real music and TA-02 ·
  Quasar Jets go to a **later update**; next: port, then CurseForge; the waves stay pictures only.
- 2026-11-14 (Opus's defaults, tell Adam if he wants others): cooldown 60 s; default keys **";"** on TON 618's item and
  **"'"** on the Stellar Remote (first B and V, moved off the SA Series' keys the same day); outside the dark the menu shows
  the warning "Only in the dark, after TON 618 has devoured the world" when Last Light is picked, and a cast there says
  why in the action bar; the burst's restore is TON 618's own command; the overworld jumps to the next sunrise
  (`morning_on_return`, on by default).

## 3. Open questions (ask when they matter, as clickable choices)

1. **Later update** (Adam, 2026-11-14): real music for the film. Now: Fable's code-made soundtrack, with M1 (a minor
   pad, tick 137 to the hush) and M2 (a major pad from 720) as placeholders in `FilmSynth`. Real music = an `.ogg`
   played beside it (needs `sounds.json` + a registered `SoundEvent`).
1a. ~~Fable's proposal P1~~: applied 2026-11-14 (`0f0e737`).
2. ~~Should the waves do anything in the world?~~ Offered 2026-11-14, not picked: they stay pictures only.
3. ~~An in-game switch for educational mode~~: built 2026-11-14 (`5e623cd`, `client/EducationPlate`).
4. ~~Before the port and CurseForge~~: answered 2026-11-14 (all targets, public repo + CI, 1.0.0 Release, Opus
   drafts the page text; see RESUME HERE). Still to ask when it comes: pushing to the new repo, creating the
   CurseForge project and uploading (each needs his yes).
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

**How it is built (2026-11-14, `dev.adamko.ton618addition`):**
- `link/Ton618Link`: the only place that touches TON 618/The Shooting Star. `install()` (from `FMLConstructModEvent`'s
  queue, as the SA Series does): `RimulibApi.connect(..., "1.2.3")`, `NetApi.toClient(LastLightFx.PACKET)`,
  `SkillApi.whenRegistered("ton618", ...)` + `extend` on set `ton618:ton_618` (`ON_TON618`), and
  `whenRegistered("shooting_star_demo", ...)` + `extend` on the Stellar Remote (`ON_REMOTE`; the set holding an enum of
  skills, as the SA Series picks it). `devoured(server)` reads command storage `ton618:ton618` `devoured`;
  `inDark(player)`: dimension `ton618:devoured`; `restore(server)` runs `ton618 restore` as the server (permission 4,
  output suppressed) and returns whether the flag cleared. The sky hole's facts (`HOLE_DIRECTION`, `HOLE_DISTANCE`,
  `HOLE_SHADOW_RADIUS`) for the film.
- `lastlight/LastLight`: `cast` checks (enabled, who may cast, one at a time, devoured, in the dark), sends
  `LastLightFx` to everyone in the dark, logs; `tick` (server tick) at `RESTORE` (690) sets the next sunrise (if
  `morning_on_return`) and restores (an error and a red chat line in the dark if the restore failed), clears at `END`
  (850); `arrived` sends the film to anyone entering the dark (or logging in there) before the burst. The clock is
  `overworld game time - cast game time` (the dark shares the overworld's game time).
- `lastlight/LastLightClock` (key ticks, from the previz), `LastLightInfo` (id `last_light`, title, colour 0xBFE0FF,
  cooldown 1200, keys), `LastLightSkill` (no sliders; `selectWarning` from the client).
- `Ton618AdditionConfig` (COMMON, `config/ton618_addition-common.toml`, `[last_light]`: `enabled`, `who_can_cast`,
  `morning_on_return`); `client/Ton618AdditionClientConfig` (CLIENT, `[film] educational_mode = false`); NeoForge's own
  config screen is registered (Mods, TON 618 Addition, Config).
- Checked in TON 618 1.1.0's jar: its cast path ends in the set's `cast`, so our extended caster gets it; its per-skill
  `ton618.yml` only lists its own skill, so Last Light is never locked by it; RimuLib's `Tuning.clampFor` handles a
  skill without power.
- Not handled yet: SS-21 Quasar can hide later API skills from the Stellar Remote's menu (§10); the SA Series'
  `showInMenu` workaround isn't copied (TON 618's own menu is unaffected).

## 6. Educational mode

A client setting, **off by default** (Adam, 2026-11-13), that adds small physics labels **in the film only**. The
film reads it through one getter (§7). Fits the SA Series too (its `docs/NEXT-SKILLS.md` already has the idea): kept
apart, told to Adam.

## 7. For Fable: the film

**Status 2026-11-14: READY for Part 2.** The base is built (0.1.0, `a786dc6`) and the previz v2 is approved.

- **The film's own files (Fable's alone; write only here):**
  - Java package `dev.adamko.ton618addition.client.film` (folder `src/main/java/dev/adamko/ton618addition/client/film/`).
    It holds `LastLightFilm.java`, a **placeholder by Opus** (bars, year counter, title, white, labels as text):
    replace it freely, keeping the four entry points below;
  - shaders `src/main/resources/assets/ton618_addition/shaders/film/`;
  - textures `src/main/resources/assets/ton618_addition/textures/film/`;
  - film sounds `src/main/resources/assets/ton618_addition/sounds/film/`, and the film's own sound list
    `src/main/resources/assets/ton618_addition/sounds.json` (not used: Fable's film makes its soundtrack in code,
    `FilmSynth`/`FilmSound`; real music would need these, §3 Q1).
  Anything else: a proposal in `reviews\fable\PROPOSALS.md` (what, where, why, the patch); Opus applies the ones Adam
  picks.
- **The contract (what the rest of the mod calls; propose changes, don't make them):**
  - `LastLightFilm.init(IEventBus modBus)`: once, from the mod's constructor, on clients only. Register shaders,
    sounds, renderers, event listeners here.
  - `LastLightFilm.start(LastLightCast cast)`: a cast reached this client (on the game thread). **Everyone in the dark
    gets it**, the caster and every watcher; a player who arrives in the dark while it runs (before the burst) gets it
    mid-way: start from `cast.tick(partial)`, not from 0.
  - `LastLightFilm.stop()`: cancel, when the player leaves the server or the server drops the cast early
    (`LastLightEnd`, since P1: the world came back before the burst, or the restore failed). **Never called for the trip back to the
    overworld at RESTORE**: the film must run on through that dimension change (whether a RimuLib cutscene survives
    it is still to test; if not, two cutscenes bridged by the white, as the previz notes say).
  - `LastLightFilm.active()`: the film is running. (`Ton618AdditionClient` drops the cast 40 ticks after END and calls
    `stop()` then if `active()` is still true.)
- **Drawing over the HUD under TON 618 (2026-11-14, important)**: TON 618 cancels `Gui.render` in its dark and during
  every RimuLib film, so `RenderGuiEvent.Post` never fires then. `client/HudRescue` (Opus's) posts a **cancelled
  `RenderGuiEvent.Pre`** right after the HUD call in exactly those frames. The film's `RenderGuiEvent.Pre` listener
  (LOWEST, `receiveCanceled = true`) is therefore what draws the film's screen layer for nearly the whole film:
  **keep it** (and keep `screen(...)` safe to call from either listener; only one of them fires in a frame).
- **The clock**: `lastlight/LastLightClock` (Opus's): CAST 0, PRESS 78, RACE 118, COSMOS 200, CROSS 270, STARS_OUT
  377, PAIRS 440, SHRINK 540, GOOGOL 625, HUSH 665, BURST 680, **RESTORE 690** (the server sets the next sunrise and
  runs `ton618 restore`: everyone in the dark lands at the overworld spawn), CLEAR 720, END 850. `cast.tick(partial)`
  counts game ticks from the cast: it follows the server's game time (shared by the dark and the overworld) and counts
  on by itself while there is no level, so it doesn't stop or jump through the dimension change.
- **What the film can read** (`client/LastLightCast`, Opus's; ask for more by proposal): `tick(partial)`, `tick()`,
  `caster()` (the caster's `Player` in this client's level, by UUID: after the restore that is at the overworld spawn;
  null while not in this level), `casterUuid()`, `isCaster()`, `castPosition()`, `castYaw()`, `castPitch()`,
  `offhand()` (the firing item is in the off hand), `viaStellarRemote()` (else TON 618's item), `inDark()`,
  `educational()`, and the sky hole as TON 618 1.1.0 draws it: static `holeDirection()` (unit vector from the camera,
  (0.34, 0.2, -1) normalised), `holeDistance()` (1000 blocks from the camera, moving with it like the sun),
  `holeShadowRadius()` (≈57.5 blocks at that distance, ≈3.3°; its disk runs from 2.9 to 11 of these). Also
  `Ton618AdditionClient.current()` returns the running cast anywhere on the client.
- **Answers to the previz's points for Opus** (`previz/NOTES.md`): 1 done (everyone in the dark, late arrivals too);
  2 as above; 3 `caster()` after the restore, or the overworld spawn; 5 all in `LastLightCast`; 6 done (the next
  sunrise, day time 0, set at RESTORE under the white; `morning_on_return`, on by default); 7 the waves stay pictures
  (open question §3).
- **Building and testing**: `./gradlew --no-daemon build` (JDK 21; `libs/` already holds RimuLib 1.2.3 and TON 618
  1.1.0); `./gradlew --no-daemon runClient` opens a dev game with both (`runServer` for a server). Test jar to
  `C:\Users\User\Desktop\test builds\<date> TON 618 film\` for Adam.

## 8. Building

Set up 2026-11-14 from the SA Series `main` (ModDevGradle 2.0.x, NeoForge 21.1.248, Parchment 2024.11.17, Gradle
8.14.3 wrapper; JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`), without its TSS mapping
machinery (TON 618 needs none: we never name its classes).
- `JAVA_HOME=<JDK 21> ./gradlew --no-daemon build` → `build/libs/ton618_addition-<version>.jar` (about 20 s once
  NeoForge is cached). Versions in `gradle.properties` (`mod_version=0.1.0`); metadata from
  `src/main/templates/META-INF/neoforge.mods.toml`: TON 618 `[1.1.0,)` and RimuLib `[1.2.3,2.0)` required, The
  Shooting Star optional.
- RimuLib is `compileOnly` (its public API, plus `SkillHolder`/`Skill`/`SkillSet` types the API uses); TON 618 is never
  compiled against. `runClient`/`runServer` copy `libs/rimulib*.jar`, `libs/ton618*.jar` and `libs/extra/*.jar` (other
  mods to try alongside, e.g. The Shooting Star) into `run/mods`. NeoForge 1.21.1 runs Mojang names in dev and in
  production, so their real jars load in the dev runs.
- The dev server needs no `eula.txt` (it started without one, 2026-11-14) and waits for console input: stop it (a
  `timeout` on the command did).
- Release jar name (as the SA Series): `TON 618 Addition <version> [NeoForge 1.21.1].jar`.
- No Node, ffmpeg or JDK 25 on the VM. Other loaders only after Adam approves (§0).

## 9. Files

- `BRIEF.md`: Adam's brief. `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`: Fable's prompt (Adam runs it).
- `docs/TON618-NOTES.md`: what TON 618 does.
- `src/main/java/dev/adamko/ton618addition/`: the mod (§5); `client/film/` is Fable's (§7).
- `libs/`: jars (git-ignored), `libs/README.txt` says where they came from; `libs/extra/` (optional, git-ignored):
  more mods for the dev runs.
- `run/`: the dev runs' game folder (git-ignored).
- Fable's: `previz\` (Part 1), the film files (§7), `reviews\fable\PROPOSALS.md`, `reviews\fable\FINDINGS.md`.

## 10. Ideas

- **TA-02 · Quasar Jets** (picked 2026-11-13; **a later update**, Adam 2026-11-14, with the real film music): TON 618 is really a quasar, one of the brightest
  known; a skill firing its relativistic jets as a beam across the land. Our own film and art; **only shown when SS-21
  Quasar is installed** (Aether21, CurseForge 1739264, "The Shooting Star Addition [SS-21 Quasar]", 1.8.0 has NeoForge
  1.21.1 since 2026-11-12, file 9575770; a TSS addon with its own black-hole jet strike). Detect it by mod id (check
  its id in its jar when we get there; ask before downloading). Note: SS-21 joins the Stellar Remote by reflection and
  can hide later API skills from the H menu (SA Series `docs/ADDONS.md`); the SA Series has a workaround
  (`TssLink.showInMenu`) if our Stellar Remote skill goes missing next to it.
- Not picked yet ("we will see"): **Black hole merger** (gravitational wave ripples the land), **Time dilation**
  (everything round the caster slows), **Advancements** ("A googol of years", "Information paradox").
