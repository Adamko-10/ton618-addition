# CLAUDE.md: TON 618 Addition

Handoff notes for every Claude session in this folder (`C:\Users\User\Desktop\ton 618 addition`, on Adam's VM; since
2026-11-15 on Adam's PC: `C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition`, run by the SA Series' session).
Started 2026-11-13 by an Opus 5.5 session (xhigh) that was started from Adam's SA Series session. **Read `BRIEF.md`
first** (Adam's brief, his own words at its end), then this file. Fable's part: `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`.

### >>> RESUME HERE (2026-11-15 ~20:25, Adam's PC): TA-02 built on main, not tested in a game; usage limit hit <<<

- **TA-02 · Quasar Jets is BUILT on `main` (NeoForge 1.21.1)**, version 1.1.0, committed (not pushed), Adam: "Build TA-02 in the mod". Code: `quasar/` (QuasarInfo, QuasarClock = Fable's ticks, QuasarStars = 8 stars + type table with the lobe/bowl/crystals x1.5 for "bigger explosion", QuasarSkill, QuasarJets = two presses + every world effect, QuasarSky = missing stars saved, QuasarContent = 4 frozen-light blocks + radio_lobe damage type), `net/QuasarFx|QuasarLock|QuasarMissing`, `client/quasar/` (QuasarClient, QuasarCast = what the film reads, QuasarRender = named stars, second-sun glare, mark, jet, dome, pop ring, QuasarReticle = lock-on HUD + pop flash, QuasarGl), `client/mixin/ClientLevelMixin` (second sun: sky light/colour/clouds/stars), `client/quasarfilm/QuasarJetsFilm` = **placeholder by Opus (bars, words, title, flood): Fable's package for the real film (Part 2)**. Server config `[quasar_jets]`. Joins only with SS-21 Quasar (`shooting_star_ss21`); `Ton618Link.showInMenu` (SA Series' fix) puts our skills back in the Stellar Remote's menu, which SS-21 freezes. Art: `tools/icons/QuasarJetsArt.java` (skill icon, crystal textures). **The mod icon is in the metadata now** (`ton618_addition_icon.png`, `logoFile`).
- Checked: builds; dev server with TSS 1.5.2 + SS-21 1.8.0 NeoForge (downloaded with Adam's OK to `libs/extra`, 4.2 MB): TA-02 joins TON 618's menu (#3) and the remote (#8); without SS-21 it stays hidden. **Not seen in a game.**
- Test jar: `..	est builds6-11-15 TA-02 Quasar JetsTON 618 Addition 1.1.0 TA-02 TEST [NeoForge 1.21.1].jar` + SS-21 1.8.0 NeoForge beside it.
- **Next**: Adam's game test; then Fable's in-game film (a Part 2 prompt, contract = QuasarJetsFilm's 4 entry points + QuasarCast); open: players carried (config, off). Then the 6 ports (with the 1.0.1 fixes and Fable's P2). Fable queue after: SA-04B, SA-03A Roche.

### >>> RESUME HERE (2026-11-15 ~14:30, Adam's PC): Codex's test is done; its fixes go to the next update <<<

- **Codex finished all 8 runs** (Adam: "its done"; `..\codex tests\REPORT.md`, 14:16; contact sheets per build in
  `..\codex tests\screenshots\<nn>-<build>\film-contact-sheet.jpg`):
  - **Works on Fabric 26.3, 1.21.11, 1.21.1, 1.20.1, Forge 1.21.1 and NeoForge 1.20.1 (on retry)**: devour, the ready
    panel, the cast, the whole film, the return on a morning ~34.5 s after the cast, educational mode, films off with
    the countdown card; no warnings or errors of ours. NeoForge 1.21.1: load check only (Adam played it before).
  - **All six marked FAIL for one bug: the clipped warning.** `LastLight.cast`'s `deny(...)` "Nothing to evaporate
    yet: set TON618's Scale to ∞ and use it first, then cast Last Light in the dark" (~100 characters) goes through
    `SkillApi.deny` to the vanilla action bar, one line; at 1280×720 (GUI scale 3) only ~70 fit. Same line on all 7
    branches (`LastLight.java:64`; `:65` on fabric-1.21.11 and fabric-26.3).
  - **Forge 1.20.1 crashed** twice in TON 618's dark (the second time on the loading screen), before Last Light ran: a
    native access violation in Windows' `d3d11.dll` (offset 0x1969e1), no Minecraft crash report. NeoForge 1.20.1 with
    the same jar crashed the same way once, then ran everything. Medal's hook DLL (Adam's clip recorder) was loaded in
    all three. Not shown to be ours.
  - Seen, not failures: Minecraft's movement-tutorial toast stays over the film (brand-new games only); the early hand
    glow, floor ring and outgoing floor wave (~4.5-8.6 s) are faint on **every** version, NeoForge too (so not a port
    bug; the rest of the film matches on all).
- **Adam's answers (2026-11-15, clickable)**:
  - the warning: **"fix this next update, add it to the list"** (1.0.0 ships as it is). Texts offered: "Not yet: set
    TON618's Scale to ∞ and use it first" (50 characters, recommended) or "Not yet: TON618 must devour the world
    first"; ask which when doing it.
  - the 1.20.1 crash: **"idk decide later"**. Options given: retest Forge 1.20.1 with Medal closed (then TON 618 alone
    if it still crashes), or ship anyway.
  - the toast: **"hide but on later version"**: clear Minecraft's toasts when the film starts, on all 7 (the toast
    API differs on 1.21.11/26.3).
  - the faint early glow: **"idk lets do it later tho and u decide"**. Opus's call: in the next update Fable checks
    those shots against the previz's tick table (`previz\NOTES.md`) and fixes them in its own film files
    (`client/film/`, `shaders/film/`); Opus changes nothing there.
- **The next update's list**: the warning text; hide toasts in the film; Fable's look at the early glow; the 1.20.1
  crash decision; real film music; TA-02 · Quasar Jets (only with SS-21 Quasar); Last Light's in-game icon in the
  pixel style.
- **Building on this PC**: JDK 21 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`; Gradle-provisioned
  JDK 25 and 17 in `C:\Users\adam_\.gradle\jdks\` (`eclipse_adoptium-25-amd64-windows.2`, `-17-...`; Loom needs a
  Java 25 JVM), so all 7 branches can build here. This repo has `dev/publish` but no `dev/ci`. Nothing built here yet.
- **The 7 branches are PUSHED** (Adam, 2026-11-15, clickable: "Yes, push branches"; checked first: GitHub's `main`
  `43b0e14` was an ancestor, every new commit by the noreply address, no secrets or big files, no jars but Gradle's
  wrapper). `main` `43b0e14..0ca0beb`; new on GitHub: forge-1.21.1 `85b8dd7`, forge-1.20.1 `3b5e2e8`, fabric-1.21.1
  `d725beb`, fabric-1.20.1 `953edeb`, fabric-1.21.11 `87c6cea`, fabric-26.3 `ce7f85f`. Commits after `0ca0beb` (this
  file) are local until the next push.
- **v1.0.0 RELEASED ON GITHUB, all 7** (Adam, 2026-11-15: "Yes, make the release"; clickable "Release all 7"):
  one release per branch, like the SA Series: tags `v1.0.0` (main, **Latest**), `v1.0.0+forge-1.21.1`,
  `v1.0.0+forge-1.20.1`, `v1.0.0+fabric-1.21.1`, `v1.0.0+fabric-1.20.1`, `v1.0.0+fabric-1.21.11`,
  `v1.0.0+fabric-26.3`, each at its branch head on GitHub, titled `TON 618 Addition 1.0.0 [<Loader> <MC>]`, its jar
  attached (asset label = the jar's name). Jars = the VM's 2026-11-14 builds, checked before: each built after its
  branch's last code commit, resources and class counts match the branch. Release files in the project folder's
  `releases\1.0.0\` (`jars\`, `changelogs\<branch>.md` = `dev/publish/changelog-1.0.0.md` with {MC} {LOADER} {ALSO}
  {FABRIC_API} filled: forge-1.20.1 ALSO " (also runs on NeoForge 1.20.1)", Fabric ", plus **Fabric API**").
- **The Forge 1.20.1 crash, looked into (Adam, 2026-11-15: "ideally try to make a quick fix for forge so it works and
  oesnt crash, then we publish it on curseforge")**: not our code. Windows' crash reports
  (`C:\ProgramData\Microsoft\Windows\WER\ReportArchive\AppCrash_javaw*`): all three = `d3d11.dll` c0000005 at
  0x1969e1, Mojang's Java 17 in Codex's Prism, `medal-hook64-v2.dll` loaded each time; the Forge retry died on the
  **loading screen** (log ends at the texture atlases, before any world), and Fabric 1.20.1 on the same Java never
  crashed: so likely Medal's DXGI hook with Forge/NeoForge 1.20.1's early loading window (`fmlearlywindow`), not
  verified. (Adam's own Modrinth games crashed on 11-11 and 11-13 in `OpenAL.dll`, c0000409: unrelated, sound.)
  **Adam (clickable): "Publish + known-issue note"**: the Forge 1.20.1 changelog (and its GitHub release, edited)
  ends with a "Known issue" section: a `d3d11.dll` crash with no crash report is the graphics side, seen with Medal;
  close screen recorders/overlays or set `earlyWindowControl = false` in `config/fml.toml`. Use it on CurseForge too.
- **ON CURSEFORGE (2026-11-15 ~15:30)**: project **TON 618 Addition, id 1807506**
  (https://authors.curseforge.com/#/projects/1807506), made in Adam's Chrome (he was already signed in). Adam's
  clickable picks: "Yes, start CurseForge", distribution **"Allow 3rd party"**, "Yes, create it", "Yes, add it" (the
  NeoForge file) and **"Yes, add all 6"** (the rest without asking each). Settings: class Mods, main category Addons +
  Magic + Technology, comments on, license All Rights Reserved, logo `dev/publish/project-icon.png`, summary
  `dev/publish/summary.txt` (235/256), description = `dev/publish/curseforge-description.md` (+ one "Known issue,
  Forge and NeoForge 1.20.1" bullet under More, also in the repo copy now), social links as his profile prefilled
  them. **Files** (all Release, Client + Server, auto-publish once approved, Markdown changelogs from
  `releases\1.0.0\changelogs\`; TON 618 + Rimulib required, The Shooting Star [Demo] optional, + Fabric API required on
  the four Fabric files): 9629847 NeoForge 1.21.1 (Java 21), 9629865 Forge 1.21.1 (Java 21), 9629873 Forge 1.20.1
  (Forge + NeoForge, Java 17, with the known-issue section), 9629885 Fabric 1.21.1 (Java 21), 9629891 Fabric 1.20.1
  (Java 17), 9629903 Fabric 1.21.11 (Java 21), 9629912 Fabric 26.3 (Java 25). Checked through
  `/_api/project-files/1807506/files/<id>`: names, versions, Release, changelogType 5 with its lines and headings.
  **Status then: the project waits for CurseForge's moderators** ("Until approved by a Moderator, the project will not
  be visible"); the files were Processing.
- **How the CurseForge forms were driven this time** (for next time): the project wizard's description is TinyMCE 6
  (`tinymce.activeEditor.setContent(html)`); a file upload = inject an `<input type=file>`, fill it with the
  extension's file_upload (only from this session's folders: copied to its scratchpad), then dispatch
  dragenter/dragover/drop on `div.css-1x73roo`; the file form copies env/loader/Java/MC from the newest file but no
  relations; MUI chips cleared one at a time (re-query after each delete); the changelog: switch the mode select to
  Markdown first (it comes up WYSIWYG after some files), then set `textarea.mde-text` with the native setter + input
  event; relations through the "Add related project" dialog (search by name; ids don't search), each row's MUI select
  opened with a mousedown.
- **Last Light polish DONE by Fable (2026-11-15), committed `27e90b7` on `main`, not seen in a game yet**: the early
  hand glow, ring and wave weren't faint, **TON 618's post pass painted them out in the dark** (its floor is invisible,
  so everything but players counts as sky); the film now draws them at `AFTER_LEVEL` on NeoForge, shot 2 a little
  looser, the light stronger; toasts cleared at the film's start and held back during it (`ToastAddEvent`). Details:
  `previz/HANDOVER.md` "Polish (2026-11)", `previz/polish-check/` (Fable's preview page). Test jar
  `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-15 Last Light polish\TON 618 Addition 1.0.1 polish TEST
  [NeoForge 1.21.1].jar` (version inside still 1.0.0: bump for the update). **For the ports**: Fable's proposal **P2**
  (`reviews/fable/PROPOSALS.md`): Fabric has no hook after the level render, so the Fabric branches need a mixin on
  `GameRenderer.renderLevel` after `LevelRenderer.renderLevel(...)` calling the film's dark draw (Adam picks).
- **Next Fable job: TA-02 · Quasar Jets previz**, prompt `prompts/FABLE-TA02-QUASAR-JETS-PREVIZ-PROMPT.md` (Fable asks
  Adam the open design points first: when it can be cast, what it does to the world, how it differs from SS-21's
  strike, length). Not started yet: a session in this folder can only be started from Adam's own turn.
- **Fable jobs (2026-11-15)**: the Last Light polish ran
  ([Fable: Last Light film polish](#local_22632a54-fabf-4609-a051-0906500bfd9b), `prompts/FABLE-LAST-LIGHT-POLISH-PROMPT.md`:
  the faint early glow + toasts hidden during the film; film files only, no git: Opus commits and ports it). Next in
  line, one at a time: TA-02 · Quasar Jets previz (then the SA Series' SA-04B and SA-03 previz).
- **Write-down (Adam, 2026-11-15: "add the mod icon to the mod itself in the metadata so the icon shows when its
  installed in the downloaded mods ui in the instance")**: **TON 618 Addition ships no mod icon** (checked: no
  `logoFile`/`"icon"` on any of the 7 branches). For the next update, on all 7: copy `dev/publish/project-icon.png`
  (256x256 pixel art) to `src/main/resources/ton618_addition_icon.png`; `logoFile = "ton618_addition_icon.png"` in
  `neoforge.mods.toml` (main) / `mods.toml` (Forge branches), `"icon": "ton618_addition_icon.png"` in
  `fabric.mod.json` (Fabric branches). (The SA Series already has it on all 7.)
- **Write-down (Adam, 2026-11-15)**: the CurseForge **Source** link is missing: add
  https://github.com/Adamko-10/ton618-addition; and the description gets the rework all his projects get (better
  look, links to his other projects, roadmap, Discord, emojis): the SA Series' CLAUDE.md has the list.
- **Next**: wait for CurseForge's approval (then check the public page and that the files published); then the next
  update's list above. Local commits after `0ca0beb` (notes, the description's known-issue line) aren't pushed.

### RESUME HERE (2026-11-15 ~13:45, Adam's PC): set up here; Codex has tested 2 of the 8 runs

- **This repo is on Adam's PC now**: `C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition\`, cloned from
  `ton 618 addition (all branches).bundle` (in `C:\Users\adam_\Documents\Move to PC (2026-11-15)\`). Local branches:
  `main` (`10e125a`) and the six ports, forge-1.21.1 `85b8dd7`, forge-1.20.1 `3b5e2e8`, fabric-1.21.1 `d725beb`,
  fabric-1.20.1 `953edeb`, fabric-1.21.11 `87c6cea`, fabric-26.3 `ce7f85f`. `origin` =
  https://github.com/Adamko-10/ton618-addition.git (GitHub has an older `main` only; the local `origin/<port>` refs came
  from the bundle and don't exist there). Git identity `Adamko-10 <Adamko-10@users.noreply.github.com>`. **Nothing
  pushed.** No `ports\` worktrees yet: make them when a fix needs cherry-picking (`git worktree add ports/<branch>
  <branch>`; the branches already exist).
- **`libs\` filled from jars already on this PC, no download** (Adam, 2026-11-15, clickable: "Copy local, ask for
  rest"): `libs\ton618-1.1.0-neoforge.jar` (9562579) + `libs\rimulib-1.2.3-neoforge.jar` (9562438) from
  `..\codex tests\downloads\`; `libs\ports\` = TON 618 Forge (9562567) + Fabric (9562551), RimuLib Forge (9562444) +
  Fabric (`rimulib-1.2.3-fabric.jar`, id `rimulib_multi` 1.2.3, from Codex's Prism instance `01-Fabric-26.3\mods\`);
  `libs\extra\the-shooting-star-demo-1.5.2-neoforge.jar` (from the SA Series' `libs\`, for the dev runs). Not built
  here yet (JDK 21 / Java 25 toolchains on this PC unchecked).
- **Adam, 2026-11-15** (clickable): **"Codex finishes first"**: he resumes Codex for the other six runs; then one fix
  round for everything it found.
- **Test jars**: the seven 1.0.0 jars are in `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-14 TON 618
  Addition 1.0.0 all loaders\`.
- **Codex's test already ran partly on this PC** (2026-11-15 00:38-01:35, `..\codex tests\REPORT.md`, 8 portable Prism
  instances in `..\codex tests\Prism\`, 723 screenshots):
  - **Fabric 26.3 and Fabric 1.21.11: everything works** (load, H menu, devour, cast, the whole film, the return in
    the morning ~34 s after the cast, educational mode, films off with the countdown card, no errors of ours). Marked
    FAIL for one bug only: **the magenta lock warning** (casting Last Light outside the dark, in the overworld)
    **runs off both screen edges at 1280×720**.
  - Also seen (not failures): the vanilla movement-tutorial toast stays on top of the film (top right, also through
    the white flash; F1 hides it); **the early hand glow, floor ring and outgoing floor wave (4.5-8.6 s) were
    "indistinct"** in the captures on both (worth a look: those are world effects, drawn by the rewritten pipelines
    on these two versions).
  - **Not run yet**: Forge 1.20.1, NeoForge 1.20.1, Forge 1.21.1, Fabric 1.21.1, Fabric 1.20.1, NeoForge 1.21.1
    (Codex waits for Adam's go-ahead for each launch; it stopped while Forge 1.20.1 was launching).
- **Next**: Codex finishes the other six runs, then fix everything it found in one round on `main` first,
  cherry-picked to the ports (§8a); then, each with Adam's yes: push the branches, the v1.0.0 GitHub release, the
  CurseForge project and files (drafts already approved).

### RESUME HERE (2026-11-15, moving to Adam's PC): one fresh session takes over both projects

- **Adam, 2026-11-15**: "lets finalize moving this to a fresh session" (clickable: **"On my PC"**, **"Both projects"**):
  one fresh session on his PC carries on the SA Series and this addon. Its prompt and everything it needs are in
  `C:\Users\User\Downloads\Move to PC (2026-11-15).zip` (made by the SA Series session): this repo as a git bundle
  with every branch (`main` + the six port branches; the `ports/` worktrees and `libs/` jars are not in it), the
  seven 1.0.0 test jars, `Adam's notes\`, the Discord emoji icons and `HANDOFF PROMPT (2026-11-15).md`.
- **Planned place on the PC**: the project folder `C:\Users\adam_\Documents\TON 618 Addition\` (the Codex test prompt
  already uses it), with this repo cloned from the bundle as its `ton 618 addition\` subfolder; `origin` set back to
  https://github.com/Adamko-10/ton618-addition.git. That GitHub repo has an older `main` only (the local `main` is
  23 commits ahead; no port branch there): push nothing until Adam says so.
- After the move, this VM folder is the older copy: don't work here unless Adam comes back to the VM (then bring the
  PC's commits back with a bundle, as the SA Series does).

### RESUME HERE (2026-11-14 ~21:45, the VM): all 7 ports done; drafts OK'd; new pixel icon; next Adam's test

- **Adam on the CurseForge drafts (2026-11-14): "it looks good"** (summary, description, changelog: approved).
- **New project icon** (Adam: "make the icon pixel styled like shooting star addition SA series"):
  `tools/icons/ModIcon.java` (Java port of the SA Series' `art/make_icon.js` style: 64×64 pixel art at 4×, 256×256
  like SA's icon) → `dev/publish/project-icon.png`: TON618 (shadow, photon ring, its disk bent over the top), gold
  Hawking sparks escaping, blue partners falling in, a sunrise on Earth's limb, the SA icon's "Addon" tag and a
  **gold "TA" badge** (SA's is blue). Shown to Adam (no changes asked).
- **All of Adam's info in one place** (Adam, 2026-11-14: "take all info i told other sub conversations to write down
  ... put it all into one path or just file"; clickable: "Everything", "Summary + your words", "Desktop, outside
  repos"): `C:\Users\User\Desktop\Adam's notes\ALL YOUR INFO (2026-11-14).md` (Part 1 a summary by topic, Part 2 every
  message he typed to the 3 VM sessions word for word with his clicks, Part 3 his words as the SA Series notes quote
  them) + `full notes\` (copies of his global rules, both CLAUDE.md files, BRIEF, Fable's previz notes, SA's
  NEXT-SKILLS and SA-05/06 plan). Outside every repo on purpose (names Hamza, his folders): never commit or upload it.
- **Codex tests the jars on Adam's PC** (Adam, 2026-11-14: "make a prompt for codex to test for me"; clickable: "My own PC",
  "Real game, own folder"): `prompts/CODEX-TEST-PROMPT.md` (Codex sets up its own test games in
  `C:\Users\adam_\Documents\TON 618 Addition\codex tests\`, never his Modrinth profiles; asks before installs,
  downloads and each run; screenshots the film at set times; writes `codex tests\REPORT.md`). Adam copies the 7 jars to
  `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-14 TON 618 Addition 1.0.0 all loaders\`. When the
  report comes back: fix what it finds on `main` first and cherry-pick (§8a).
- **For later** (Adam: "for later lets change the icon in game aswell like the one that last light uses"): redo Last
  Light's in-game skill icon (the two `last_light.png` sprites, 256×128, now from `tools/icons/LastLightIcon.java`) in
  the same pixel style: **in the next update** (Adam, 2026-11-14, clickable: "Next update"; 1.0.0 keeps the current
  one). Also possible: a mod-list logo (we ship none; SA has `shooting_star_addition_icon.png`).

- **`fabric-26.3` done** (`ce7f85f`, built + dev server OK, film on 26.3's renderpearl pipelines; §8a). All 7 branches
  are built; none pushed. **Test jars** (all 7 rebuilt from each branch's commit, 2026-11-14 ~21:20):
  `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 Addition 1.0.0 all loaders\TON 618 Addition 1.0.0 [<Loader>
  <MC>].jar` (NeoForge 1.21.1, Forge 1.21.1, Forge 1.20.1 = also NeoForge 1.20.1, Fabric 1.21.1, 1.20.1, 1.21.11,
  26.3; not in Downloads: the SA Series session copies jars there). Next: Adam's in-game test (above all Fabric
  1.21.11 and 26.3, whose film drawing was rewritten and never seen on a GPU), then re-show the CurseForge drafts
  and ask (clickable) before pushing the branches, the v1.0.0 GitHub release and CurseForge.
- **Port status** (§8a has the how): `main` 1.0.0 (`ce64b00`: version bump + HUD fix without MixinExtras) ·
  **`forge-1.21.1` built + dev server OK** ("Last Light joined TON 618's menu ... skill #2", worktree
  `ports/forge-1.21.1`, commit `85b8dd7`) · **`forge-1.20.1` built** (also NeoForge 1.20.1; `ports/forge-1.20.1`,
  `3b5e2e8`; jar checked: SRG names, refmap, `MixinConfigs`; **no dev run**: RimuLib's 1.20.1 mixins name SRG targets
  directly, which the dev game can't remap, as the SA Series found) · **`fabric-1.21.1` built + dev server OK** (Loom on
  the VM's JDK 25, "Last Light joined TON 618's menu", `ports/fabric-1.21.1`, `d725beb`) · **`fabric-1.20.1` built +
  dev server OK** (`ports/fabric-1.20.1`, `953edeb`) · **`fabric-1.21.11` built + dev server OK, film on render pipelines**
  (`ports/fabric-1.21.11`, `87c6cea`; never drawn on a GPU: no OpenGL on the VM) · **`fabric-26.3` built + dev server
  OK** (`ports/fabric-26.3`, `ce7f85f`; never drawn on a GPU either) · none pushed.
- **1.21.11 and 26.3 (assessed 2026-11-14)**: Minecraft's new render pipelines (1.21.5+): no `ShaderInstance`, so the
  film's `FilmGl` (one shader, blend/depth set by `RenderSystem` per call) must become `RenderPipeline`s (one per blend
  and depth mode, uniform blocks in the GLSL), as the SA Series did (`client/render/HalleyPipelines` 153 lines,
  `DynamicMesh` 77, on its `fabric-1.21.11` branch: Fabric API 0.141.6+1.21.11, Loom 1.18.4). 26.3 adds
  `Identifier`, RimuLib's `UiApi` on `GuiGraphicsExtractor` (the plate, the notice, the film's HUD), more (SA's
  `fabric-26.3`: Fabric API 0.162.0+26.3, Loom 1.18-SNAPSHOT). Adam (2026-11-14, told ~4-8 h and a large share of usage): **"Full port, film and all"**.
- **JDK 25 is already on the VM** as a Gradle-provisioned toolchain (`~/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2`,
  from earlier SA builds; also JDK 8, no JDK 17): the Fabric 1.21.11/26.3 builds may run locally through a Java 25
  toolchain, without CI. 1.20.1 is compiled by JDK 21 with `options.release = 17` (nothing installed).
- Adam (2026-11-14): **"Yes, accept it"** for Minecraft's EULA on the VM's dev test servers (`run/eula.txt`,
  git-ignored, per port folder): Forge's dev server won't load mods without it.
- Jars for the ports (Adam's OK, 2026-11-14, ~26 MB): `libs/ports/` holds TON 618 1.1.0 Forge (9562567) + Fabric
  (9562551) and RimuLib 1.2.3 Forge (9562444) + Fabric (9562450); each worktree copies its loader's two into its own
  `libs/`. `.gitignore` now ignores `libs/**/*.jar` and `ports/`.
- Not re-shown to Adam yet: the cross-referenced CurseForge drafts (he said "just cross reference with shooting star
  addition SA series", done).

### Before that (2026-11-14 evening): release picks; repo public

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
  `changelog-1.0.0.md` (template: `{MC}`, `{LOADER}`, `{ALSO}`, `{FABRIC_API}`), `project-icon.png` (256×256 pixel
  art from `tools/icons/ModIcon.java` since 2026-11-14 ~21:45; first a 400×400 glow from `LastLightIcon.java`). Adam: "just cross reference with shooting star addition SA
  series": done against the SA repo copy and its **live** page (summary starts with the name and ends "Needs
  RimuLib."; header lines; builds list; "The H menu" like SA's "The remote's menu"; "More"; credits and the
  distribution line word for word). Categories to pick on the site: **Addons, Magic, Technology** (the SA page's).
  Re-shown to Adam 2026-11-14 ~21:30 (two lines fixed for the ports: the Config screen is NeoForge only; TON 618
  hides the HUD "on most versions", not 26.3); Adam: "it looks good".
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

## 7b. For Fable: TA-02's film (Quasar Jets)

**Status 2026-11-16: the real film is BUILT by Fable, not seen in a game** (replaced Opus's placeholder; committed by
Opus). `client/quasarfilm/`: `QuasarJetsFilm` + `JetScript` (timings; captions/labels retimed longer, key ticks kept),
`JetCamera` (framing), `JetSpace`, `JetHud`, `JetGl`, `JetSound`, `JetSynth` (soundtrack in code: no `sounds.json`, no
sound files or textures); `shaders/quasarfilm/`. Everything is drawn in the HUD pass (no in-world drawing: Fabric
ports need no P2 hook). Details and "To test in the game": `previz/quasar-jets/HANDOVER.md` "Part 2". Test jar:
`..\test builds\2026-11-16 TA-02 film\TON 618 Addition 1.1.0 TA-02 film TEST [NeoForge 1.21.1].jar` (+ SS-21 1.8.0).
**P3 and P4 applied (Adam: "Yes, build both", 2026-11-16; `2cb94f6`)**: `QuasarJets.sounds()` plays vanilla
sounds at the mark for everyone in range (SoundSource.BLOCKS, 16 x volume blocks): BEACON_ACTIVATE at SWEEP,
WARDEN_SONIC_CHARGE at 767, WARDEN_SONIC_BOOM + GENERIC_EXPLODE at LANDS, RESPAWN_ANCHOR_CHARGE rising at 830/845/860/875,
GENERIC_EXPLODE + LIGHTNING_BOLT_THUNDER at POP, AMETHYST_CLUSTER_PLACE per crystal; `QuasarClient.backOnDay(star)`
for the film. **Sent to the Fable film session** to drop its own copies of those cues in `JetSynth` (keeping the
pickup pops, drone, music) and show "back in N nights" (tonight counts as the first), then rebuild the test jar in
place: commit its film files when it reports. Not heard in a game. Known limits: the caster isn't posed in shot 8; films off (J) = world effects only; the last
"MISSING" text is on ~2.6 s: **the ending is 2 s longer** (Adam's pick, 2026-11-16): the film ends at tick 996
(`JetScript.END`; `QuasarClock.END` stays 960, no world effect moved). It fits because `QuasarClient` keeps the strike
until `QuasarClock.END + 40` = 1000: never shorten that without bringing the film's END down too. Test jar rebuilt in place.
Earlier: READY for Part 2 when TA-02 was built on `main` (1.1.0, `63a6add`) with a placeholder film by Opus.

- **The film's own files (Fable's alone; write only here):**
  - Java package `dev.adamko.ton618addition.client.quasarfilm` (`src/main/java/dev/adamko/ton618addition/client/quasarfilm/`).
    It holds `QuasarJetsFilm.java`, a **placeholder by Opus** (letterbox bars, each shot's words, the title card, the
    blue-white flood): replace it freely, keeping the four entry points below;
  - shaders `src/main/resources/assets/ton618_addition/shaders/quasarfilm/`, textures `.../textures/quasarfilm/`, film
    sounds `.../sounds/quasarfilm/` (or made in code, as Last Light's `FilmSynth`; `sounds.json` entries by proposal).
  Anything else: a proposal in `reviews/fable/PROPOSALS.md`; bugs: `reviews/fable/FINDINGS.md`.
- **The contract (propose changes, don't make them):**
  - `QuasarJetsFilm.init(IEventBus modBus)`: once, from `client/quasar/QuasarClient.init` (clients only).
  - `QuasarJetsFilm.start(QuasarCast cast)`: the strike reached this client **and it gets the film** (the caster and
    players within ~128 blocks of the caster or the mark; Adam: "Caster + nearby players"). Everyone else, and anyone
    arriving mid-way, gets only the world effects. Start from `cast.tick(partial)`.
  - `QuasarJetsFilm.stop()`: leaving the server, or 40 ticks after END. `QuasarJetsFilm.active()`: it is running.
- **The clock**: `quasar/QuasarClock` (Opus's), the previz's ticks: MARK 0 · LOCK 38 · ENGINE 80 · PUSH 150 · FLIGHT 200
  · ARRIVE 330 · SHRED 350 · IGNITE 440 · JETS 530 · TURN 610 · FLOOD 665 · DAY 680 · SWEEP 745 · LANDS 795 · LOBE 830 ·
  MELT 842 · THROW 846 · POP 890 · NIGHT 905 · END 960; helpers `ramp`, `beamHead(t)`, `lobe(t)`, `day(t)`. The server
  times every world effect by them, so the film must keep them (ask before moving one).
- **What the film can read** (`client/quasar/QuasarCast`, Opus's): `tick(partial)`, `tick()`, `star()` (name, `type()`:
  `QuasarStars.Type` with `color`, `size`, `swept`, `lobe`, `bowl`, `crystals`, `day`, `label`; `distance()`, `size()`,
  `mass()`, `direction()` = its fixed sky direction), `starIndex()`, `mark()`, `quasarDirection()` (the second sun's sky
  direction; the jet comes in along it), `caster()`, `casterUuid()`, `isCaster()`, `castPosition()`, `castYaw()`,
  `castPitch()`, `offhand()`, `viaStellarRemote()`, `secondSun()`, `film()`, `educational()`.
  `QuasarClient.current()` returns it anywhere on the client.
- **Not the film's (Opus's, everyone sees them, the film must not draw them again):** `client/quasar/QuasarRender`
  (our 8 named stars in the night sky, the second sun's glare, the ground mark, the jet's beam, the radio lobe's dome
  and its pop ring), `client/mixin/ClientLevelMixin` + `QuasarClient.fog` (the second sun's sky light, colours,
  clouds, stars, fog), `client/quasar/QuasarReticle` (the lock-on HUD, the pop's flash). The film may propose restyling
  them (Adam picks).
- **Adam's notes for the mod version** (on v1): **labels on screen a little longer** (Last Light's 2.4 to 5.9 s are
  fine; the shortest now are 1.3 to 2.2 s); the **bigger explosion** is done in the world (lobe radii 9/15/24/33, the
  shot-10 framing must fit them); the second sun ends with the film.
- **Not in TON 618's dark**: everything is in the overworld, so `AFTER_WEATHER`/`AFTER_LEVEL` drawing works (no post-pass
  problem as in Last Light's dark).
- **Building and testing**: JDK 21, `./gradlew --no-daemon build`; `libs/extra/` holds The Shooting Star 1.5.2 and
  SS-21 Quasar 1.8.0 NeoForge (TA-02 only appears with SS-21), so `runClient` has it all. Ask Adam before any game
  run. Test jar to `C:\Users\adam_\Documents\TON 618 Addition\test builds\<date> TA-02 film\`.

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

## 8a. The ports (one branch per loader, like the SA Series; Adam 2026-11-14)

- **Layout**: `main` = NeoForge 1.21.1. Each other branch has a worktree in `ports/<branch>/` (git-ignored inside the
  project folder): `git worktree add -b <branch> ports/<branch> main`. **Never `git stash`** (shared between
  worktrees). Changes are made on `main` first and cherry-picked; the branches keep their own build files and the
  loader-specific code below. Commits stay local until pushed: `wip/<branch>` pushes for CI are fine (Adam's standing
  OK); the real branches only with his yes.
- **Shared on every branch**: RimuLib's public API (`SkillApi`, `NetApi`/`NetClientApi`, `FilmApi`, `UiApi`, `MenuApi`)
  and a few guarded internals (`WhiteSpace`, `ClientSkills`, `SkillHolder`); networking is RimuLib's, so no loader
  packets. The HUD fix's `GameRendererMixin` uses plain `@Inject`s (no MixinExtras) for that reason.
- **forge-1.21.1** (`85b8dd7`, built, dev server OK): from the SA Series' `forge-1.21.1` (ForgeGradle `[7.0.23,8.0)`,
  Forge 52.1.16, Gradle 9.7.1 wrapper, `official` mappings, `net.minecraftforge.gradle.merge-source-sets=true`,
  configuration cache off). `mods.toml` (`mandatory = true/false`); mixin config in the jar **manifest**
  (`MixinConfigs`; Forge 1.21.1 doesn't read `[[mixins]]`) and `--mixin.config` for dev runs; **no Mixin annotation
  processor** (it failed for want of obfuscation mappings; runtime names are official). Code: `@Mod` constructor takes
  `FMLJavaModLoadingContext` (`getModEventBus()`, `registerConfig`), `FMLEnvironment.dist`; `MinecraftForge.EVENT_BUS`
  with typed lambdas (`(TickEvent.ServerTickEvent.Post e) -> ...`) or `addListener(EventPriority, boolean, Class,
  Consumer)`; `ForgeConfigSpec`; no generic config screen (NeoForge's `ConfigurationScreen` doesn't exist).
  **Forge 1.21.1 has no `RenderGuiEvent`** (its HUD is layers, `AddGuiOverlayLayersEvent`): `HudRescue.afterGui` calls
  `LastLightFilm.screen(graphics, partial)` itself every frame (made public), so the film's screen layer draws with
  or without TON 618's HUD cancel. `RenderLevelStageEvent`: `getPartialTick()` is a float, the view matrix is
  `getPoseStack()` (a `Matrix4f`, deprecated). Built with JDK 21 locally in ~20 s (first setup ~5 min).
- **forge-1.20.1** (`3b5e2e8`, built from `forge-1.21.1`; jar also for NeoForge 1.20.1): ModDevGradle
  `net.neoforged.moddev.legacyforge` 2.0.+ (Forge 1.20.1-47.4.10; `-Pneoforge` builds on NeoForge 1.20.1-47.1.106),
  Parchment 1.20.1/2023.09.03, Gradle 8.14.3 (SA's wrapper), toolchain 21 + `options.release = 17`; RimuLib and TON 618
  as `modCompileOnly`/`modRuntimeOnly` `local:` modules from a `flatDir libs` (they ship SRG names); Mixin annotation
  processor 0.8.5 + `mixin { add sourceSets.main, "ton618_addition.refmap.json"; config ... }`; the branch's
  `ton618_addition.mixins.json` has `"compatibilityLevel": "JAVA_17"` and `"refmap"`. Code: RimuLib imports
  `mc1201`; `new ResourceLocation(ns, path)` (deprecated on 47.4, fine); HUD mixins on `Gui.render(GuiGraphics, float)`
  and `GameRenderer.render(float, long, boolean)`, `HudRescue.afterGui(graphics, float partial)`; `LastLightFx` writes
  the position as three doubles (no `writeVec3`); Forge 47: no-arg `@Mod` constructor + `FMLJavaModLoadingContext.get()`,
  `ModLoadingContext.get().registerConfig`, `TickEvent.ServerTickEvent`/`ClientTickEvent` with `phase == END`,
  `RenderLevelStageEvent.getPoseStack().last().pose()`. Film: `FilmGl.begin/vertex/draw` on 1.20.1's
  `BufferBuilder` API (`vertex().uv().color().endVertex()`, `end()` → `RenderedBuffer`, release when empty) with four
  builders of the film's own; `FilmSound`'s `Sound(String, ...)`. **Dev runs fail** in RimuLib's own
  `ServerTickRelayMixin` (target `m_5705_` named directly; the SA refmap-remap trick doesn't cover configs without a
  refmap): not run; Adam tests the jar.
- **fabric-1.21.1** (`d725beb`, built, dev server OK; from `forge-1.21.1`): Fabric Loom **1.18.4 needs a Java 25 JVM
  to run** (`JAVA_HOME=~/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2`, already on the VM; the code still compiles
  for 21), loader 0.19.5, Fabric API 0.116.7+1.21.1, Gradle 9.7.1 (SA's wrapper), `loom.officialMojangMappings()`.
  **Loom remaps mixin targets in the bytecode** (no refmap; checked: `method_3192`/`method_1753`). RimuLib and TON 618:
  the inner `+mc1.21.1` jars of their Fabric bundles (`META-INF/jars/...`), extracted into the worktree's `libs/`,
  `modCompileOnly`/`modLocalRuntime`; inner mod ids `rimulib`, `ton618`; RimuLib's Fabric package has **no version
  tag** (`cyou.rimuru.rimulib.api...`). `fabric.mod.json` (main `Ton618Addition`, client `Ton618AdditionClient`,
  server `Ton618AdditionServer`; mixins `ton618_addition.mixins.json` + `ton618_addition.fabric.mixins.json`, both
  client). Joining: like the SA Series, `client/mixin/RimulibClientMixin` (`@Pseudo`, HEAD of
  `cyou.rimuru.rimulib.client.RimulibFabricClient.onInitializeClient`) calls `Ton618Link.install()` +
  `Ton618AdditionClient.afterConstruction()`; dedicated servers in the `server` entrypoint; `SERVER_STARTING` too
  (`install` runs once). Config: the SA Series' `config/ConfigFile` (same file names, sections and keys).
  Events: `ServerTickEvents.END_SERVER_TICK`, `ServerLifecycleEvents.SERVER_STOPPED`,
  `ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD` (package `api.entity.event.v1`),
  `ServerPlayConnectionEvents.JOIN`, `ClientTickEvents.END_CLIENT_TICK`, `ClientPlayConnectionEvents.DISCONNECT`. Film:
  `LastLightFilm.init()` (no bus), `CoreShaderRegistrationCallback` with id `ton618_addition:last_light` (**the shader
  files sit in `shaders/core/` on Fabric**), `WorldRenderEvents.LAST` (`context.camera()`, `positionMatrix()`,
  `projectionMatrix()`, `tickCounter()`), `ScreenEvents.AFTER_INIT` + `afterRender` for the loading screen's white,
  `FilmSound` overrides Fabric API's `getAudioStream(SoundBufferLibrary, ResourceLocation, boolean)`.
- **fabric-1.20.1** (`953edeb`, built, dev server OK): from `fabric-1.21.1` with `forge-1.20.1`'s Minecraft 1.20.1 files
  (`HudRescue`, the two HUD mixins, `LastLightFx`; RimuLib imports without a tag), `new ResourceLocation`,
  `Sound(String...)`, FilmGl's 1.20.1 vertex helpers; Fabric API 0.92.6+1.20.1, compiled for Java 17 (release 17,
  toolchain 21; Loom on JDK 25); mixin configs JAVA_17; Fabric 1.20.1's `WorldRenderContext.tickDelta()` and
  `matrixStack().last().pose()`; inner `+mc1.20.1` jars.
- **fabric-1.21.11 plan (2026-11-14, from the SA Series' `fabric-1.21.11`)**: Fabric API 0.141.6+1.21.11, Loom 1.18.4
  (JDK 25), Java 21; inner `+mc1.21.11` jars. Minecraft 1.21.11: `Identifier` for `ResourceLocation`; GUI recorded
  then drawn later (`GuiGraphics.pose()` is a 2D `Matrix3x2fStack`: `pushMatrix/translate(x,y)/scale(x,y)/popMatrix`,
  no `flush()`, `nextStratum()` for layering); the HUD fix needs the frame's own `GuiGraphics` (MixinExtras
  `@WrapOperation`, bundled by Fabric Loader). The film's drawing moves to render pipelines like SA's
  `HalleyPipelines` + `DynamicMesh` + `ProximaRender`: one `RenderPipeline` per blend/depth mode (vertex format
  POSITION_TEX_COLOR, QUADS), uniforms in std140 blocks (`Projection` via `PerspectiveProjectionMatrixBuffer`, and a
  film block with the view matrix, Mode, Param, Param2 written with `Std140Builder`), meshes uploaded to a GPU buffer
  (`DynamicMesh`) before a `RenderPass` opens; the space scene (`FilmSpace`) renders into an offscreen texture of its
  own (colour + depth) and is blitted in the HUD between `nextStratum()` calls (as `ProximaRender.present`); the world
  effects (`FilmWorld`) draw in passes on the main target's colour and depth. Fable's GLSL is rewritten for the
  uniform blocks.
- **fabric-1.21.11** (`87c6cea`): done as planned above. Details: packets `LastLightFx`/`LastLightEnd` are
  `CustomPacketPayload` records (`TYPE`, `CODEC`; RimuLib 1.21.11's `NetApi.toClient(TYPE, CODEC)`, `canSend(p, TYPE)`,
  `NetClientApi.receive(TYPE, ...)`); `player.level().getServer()`, `permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)`,
  `getGameProfile().name()`, `getBooleanOr`, `createCommandSourceStack()` (already top permission), 2D GUI pose,
  `net.minecraft.util.Util`, `KeyMapping.matches(new KeyEvent(key, 0, 0))`, `LevelLoadingScreen`. RimuLib 1.21.11
  draws its menu as a Fabric HUD element (`HudElementRegistry.addLast` → `WhiteSpace.render`), inside `Gui.render`,
  so the rescue still draws it when TON 618 cuts the HUD (no `beforeHud()` on this version). `GameRendererMixin` =
  MixinExtras `@WrapOperation` (checked: 1.21.11's `GameRenderer.render(DeltaTracker, boolean)` calls
  `Gui.render(GuiGraphics, DeltaTracker)`). Film: `FilmGl` rewritten (Fable's helper API kept; pipelines
  `pipeline/last_light_<blend>_<depth>`, `FilmParams` std140 block = view mat4 + Mode/Param/Param2, `Projection` from
  `PerspectiveProjectionMatrixBuffer` for the space scene or `RenderSystem.getProjectionMatrixBuffer()` in the world),
  `FilmMesh` (SA's `DynamicMesh`), the space scene in its own texture (`ton618_addition:film_scene`) blitted by
  `FilmGl.present` in the HUD; world effects at `WorldRenderEvents.END_MAIN` (camera from the game renderer, view =
  its rotation, world quads already camera-relative); shader `#version 330` with the blocks, `last_light.json` gone.
- **fabric-26.3** (`ce7f85f`, 2026-11-14 ~21:15, built + dev server OK: "Last Light joined TON 618's menu
  (ton618:ton_618) as skill #2"; worktree `ports/fabric-26.3` from `fabric-1.21.11`). Build: plugin
  `net.fabricmc.fabric-loom` 1.18-SNAPSHOT, Fabric API 0.162.0+26.3, **no mappings** and plain
  `implementation`/`compileOnly`/`localRuntime` (26.x is unobfuscated), Java 25 release + toolchain, mixin config
  JAVA_25, inner `+mc26.3` jars. Minecraft 26.3's own jar for `javap`:
  `~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.3/`. What changed:
  - Rendering is **`com.mojang.renderpearl.api`**: `FilmGl` pipelines = `BindGroupLayouts.PROJECTION` + our
    `FilmParams` `BindGroupLayout`, `withVertexBinding(0, POSITION_TEX_COLOR)`, `PrimitiveTopology.QUADS`, a
    `ColorTargetState` per blend, `DepthStencilState(GREATER_THAN_OR_EQUAL, false)` in the world (**reversed depth**),
    `RenderSystem.getCompiledPipeline`, `ProjectionMatrixBuffer`, `gameRenderer.mainRenderTarget()`, Scene textures
    `RGBA8_UNORM`/`D32_FLOAT` with `USAGE_COPY_DST`; `begin()` = `new BufferBuilder(ByteBufferBuilder, QUADS, format)`
    from four pooled byte buffers. `FilmSpace`'s projection is `setPerspective(fov, aspect, far, near, isZZeroToOne())`
    (far and near swapped, as SA's ProximaCosmos). Shaders: `#extension GL_ARB_separate_shader_objects : require`,
    `#include`, `layout(location = N)` on every in/out.
  - World effects: `LevelRenderEvents.END_MAIN` (`...rendering.v1.level`), camera from
    `context.levelState().cameraRenderState` (`pos`, `viewRotationMatrix`) and `gameRenderer.mainCamera()`.
  - HUD: `GuiGraphicsExtractor` (`text(...)` for `drawString`); `ScreenEvents.afterExtract`. **TON 618 26.3 has no HUD
    mixin and RimuLib 26.3 never hides the HUD** (only reads `Hud.isHidden`), so the old HUD mixins are gone;
    `client/mixin/GuiMixin` (MixinExtras `@WrapOperation` round `Hud.extractRenderState` in `Gui.extractRenderState`)
    calls `HudRescue.afterHud` so the film's layer and the notice draw after the HUD even with F1; a Fabric HUD
    element (`addLast`) draws them if that optional mixin didn't apply.
  - Smaller: `ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL`; `InputConstants.Type.KEYBOARD` and
    `InputConstants.KEY_BACKSLASH` (26.3's key codes are SDL scancodes); `SoundInstance.getOrResolve`;
    `swing(hand, SwingAnimation.DEFAULT, false)`; no `FriendlyByteBuf.write/readVec3` (three doubles); the day is on
    world clocks: the sunrise = `server.clockManager().setTotalTicks(overworld.dimensionType().defaultClock(), ...)`
    from `overworld.getDefaultClockTime()` (what `/time set` does).

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
- **TA-02 · Quasar Jets: the plan (Adam + Opus, 2026-11-15; draft, Adam decides on the full plan)**. Adam: "i say we
  feed ton 618 a planet or something and it turns that planet into a quasar", then "im thinking maybe a star gets
  launched into ton 618 and gets devoured and then that gets shot through a quasar at the earth or something" (told:
  the black hole becomes the quasar while it eats; real: tidal disruption events, Swift J1644+57's jet at Earth, 2011).
  Picks (clickable): **night only, pick a star** ("i really like that idea, pickign a star sounds cool"), **named real
  stars**, **star type changes the strike**, **missing from the sky for a few nights**; world effects **radio lobe +
  second sun + frozen light + swept along**.
  - **Cast**: only with SS-21 Quasar installed; TON 618's item or the Stellar Remote; overworld, **at night**, looking
    at one of our named stars (a reticle with its name and type). By day: "No stars to launch: wait for night."
  - **The stars** (drawn by us in the night sky, real-ish colours), four tiers: **red dwarf** (e.g. Barnard's Star:
    small), **Sun-like** (e.g. Alpha Centauri A: medium), **blue giant** (e.g. Rigel: huge), **red supergiant** (e.g.
    Betelgeuse: the biggest, slowest). The tier sets the jet, the lobe and bowl size and the crystals' colour.
  - **The film** (~45 s, Fable): lock-on, launch, the flight to TON 618, the meal (tidal disruption), ignition (title
    card), the two jets, the blazar turn, second sun, swept along, the radio lobe and its pop. Shot list in
    `prompts/FABLE-TA02-QUASAR-JETS-PREVIZ-PROMPT.md`.
  - **In the world**: **second sun** (night turns to day worldwide for ~1 minute); **swept along** (the thin jet comes
    in low from the horizon; mobs, items and loose blocks in its path lifted and carried toward the crosshair, capped);
    **radio lobe** (a plasma dome at the crosshair shoves entities out, melts a shallow bowl, pops); **frozen light**
    crystals (a new block, the star's colour) in the bowl; the launched star **gone from the sky for a few nights**.
  - Different from SS-21 Quasar (its Milky Way black hole, a pillar straight down, a disc overhead, a spiral-galaxy
    crater, read from its jar's description). Educational labels: star types, tidal disruption, how jets launch, blazars,
    the faster-than-light illusion, radio lobes, TON 618's brightness.
  - Settings to have: on/off, who can cast, terrain on/off, second sun on/off, missing-star nights.
  - **For a FUTURE update (Adam, 2026-11-15: "in a future update write this down, lets add proxima centauri but to
    stay consistent we keep the dyson sphere and the dyson sphere tries to fight back somehow, lets just write this
    down only for now")**: Proxima Centauri as a pickable star, still inside its Dyson sphere (as in the SA Series'
    SA-04 Proxima), and the Dyson sphere **fights back** when it is launched (how: not designed yet). Not in TA-02's
    first version. **Tie-in to keep in mind (Adam, 2026-11-15)**: the star engine below "could tie in with Proxima's
    Dyson sphere later" (a Dyson sphere is already a megastructure round a star).
  - **The launch = a star engine** (Adam, 2026-11-15, after the wormhole gate, slingshot, warp bubble, folding space,
    stealing a partner, reeling on the jet and a cosmic string were offered: "lets do star engine yeah"). A real idea
    (the **Shkadov thruster**, 1987): a giant curved mirror unfolds on the side of the star facing TON 618 and bounces
    its light back, so all its light pours out the other way and the star pushes itself toward the mirror, which stays
    ahead of it like a sail (in reality over millions of years: the film races time, with a year counter, like Last
    Light).
  - **PREVIZ v1 APPROVED (2026-11-15, Fable; no v2)**: https://claude.ai/artifact/DY9xtGvDFmCVz9jUnLQKVv, source and
    **the build notes** in `previz/quasar-jets/` (`NOTES.md`: tick table MARK 0 · LOCK 38 · ENGINE 80 · PUSH 150 ·
    FLIGHT 200 · ARRIVE 330 · SHRED 350 · IGNITE 440 · JETS 530 · TURN 610 · FLOOD 665 · DAY 680 · SWEEP 745 · LOBE 830 ·
    POP 890 · NIGHT 905 · END 960 (48 s); the star-type table; shot table with "how it would be drawn in the game";
    "Things the Opus session needs to know" 1-10). Adam's answers to Fable (clickable): **two presses** (press 1 at a
    star locks it, press 2 at the ground marks where the jet lands and starts the film); **the film for the caster and
    players within ~128 blocks** (others see only the world effects); **"Lobe hurts, jet carries"**; **8 stars, two per
    type**: red dwarfs Barnard's Star, Wolf 359; Sun-like Alpha Centauri A, Tau Ceti; blue giants Rigel, Spica; red
    supergiants Betelgeuse, Antares; **the second sun ends with the film** (not a minute). Adam on v1: "its pretty good,
    i'd say when we make the mod lets just make the explosion thing bigger is all, this is good enough, great even, good
    job on it no need for a v2 but when we make the mod version we gotta slow down the labels a little so theres a tiny
    bit more time to read. im telling u all this but opus will handle it". So for the mod: **a bigger radio lobe and
    pop** (all star types) and **labels on screen longer**. Open: are players in the beam carried too.
  - **For a LATER update (Adam, 2026-11-15: "i like the interactive thing too lets add it in a later update")**: drag
    the star yourself: hold the button and drag the locked star across your night sky to TON 618's dark spot; let go
    and it becomes a real shooting star falling toward it, then the film follows it.
- Not picked yet ("we will see"): **Black hole merger** (gravitational wave ripples the land), **Time dilation**
  (everything round the caster slows), **Advancements** ("A googol of years", "Information paradox").
