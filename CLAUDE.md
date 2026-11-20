# CLAUDE.md: TON 618 Addition

Handoff notes for every Claude session in this folder (`C:\Users\User\Desktop\ton 618 addition`, on Adam's VM; since
2026-11-15 on Adam's PC: `C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition`, run by the SA Series' session).
Started 2026-11-13 by an Opus 5.5 session (xhigh) that was started from Adam's SA Series session. **Read `BRIEF.md`
first** (Adam's brief, his own words at its end), then this file. Fable's part: `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md`.

### >>> 2026-11-20 ~14:15 (Adam's PC): a player's report: no H menu in the dark, "quasar doesn't work" <<<

Done by a side session started from the SA Series' session.
- **The report** (Discord, player "Savini Jason", NeoForge 1.21.1, CurseForge app, "all the latest versions of
  dependencies"): "u cant open the H menu when ton618 devoured the world" and "the quasar addition doesnt work too".
- **Checked**: CurseForge's own file lists (`https://www.curseforge.com/api/v1/mods/<project id>/files?...`; cfwidget's
  slug lookup for `ton618` 404s, use `https://api.cfwidget.com/1798822`): nothing newer than TON 618 1.1.0, The
  Shooting Star 1.6.0, RimuLib 1.3.0, SS-21 Quasar 1.8.0 (= Adam's instance). RimuLib 1.3.0 still has
  `WhiteSpace.beforeHud/render` and draws the menu at `Gui.render` TAIL as 1.2.3 did; TON 618 1.1.0's `GuiMixin` still
  cancels `Gui.render` at HEAD in the dark or when `CutsceneDirector.hideHud()`. SS-21 Quasar 1.8.0 references no TSS
  class at all (RimuLib only), so TSS 1.6.0 can't have broken it.
- **Not reproduced**, harness runs with exactly those versions (`..\test builds\2026-11-20 player report, menu in the
  dark\`): the H menu shows in the dark (`menu\`, also with the released SA Series 1.8.3: `menu with SA Series
  1.8.3\`); SS-21 Quasar's whole strike runs, no errors (`ss21\`); TA-02 runs (`ta02\`). **Likeliest causes for him**:
  TON 618 Addition not installed (TON 618 alone hides the menu in its dark), not holding TON 618 / the Stellar Remote
  (RimuLib's `openMenu` needs one in a hand), or another mod in his pack; and "quasar" = trying SS-21 Quasar or TA-02
  **in the dark**, where both refuse (overworld at night only) **with the reason hidden: TON 618's dark hides the
  action bar too**.
- **Fixed, `1331305` on main (local, not pushed)**: `client/HudRescue` draws the **action bar in the dark** (not during
  a film; `client/mixin/GuiAccessor` invokes `Gui.renderOverlayMessage`), so every refusal there shows (screenshot
  `fix normal\deny_dark.png`: "Quasar Jets: only in the overworld, at night"); a **fallback**: if another mod changes
  `GameRenderer.render`'s HUD call and our hook after it never runs, the same is drawn at `RenderFrameEvent.Post` with
  the HUD's matrices (tested with `-Dton618test.nohudhook=true`: `fix nohook\`); **log lines** once: "TON 618 hid the
  game's HUD ...: TON 618 Addition draws the H menu over it" (INFO) and the fallback's WARN, so a player's log shows it.
  Harness: `menu=true`, `skill=ss21`, `nohudhook`.
- **TEST jar** `TON 618 Addition 1.1.1 dark HUD fix TEST [NeoForge 1.21.1].jar` (version inside 1.1.1) in his
  `NeoForge 1.21.1 (1)` instance (the `1.1.1 limits TEST` there renamed `.disabled`); copy in the test-builds folder
  above. Main only (his main-first rule): ports after his test.
- **Reply for the player** (Adam pastes it): `..\test builds\2026-11-20 player report, menu in the dark\REPLY for
  Savini Jason (Discord).md` (what to check, then `latest.log` + mod list + what he did).
- **libs/ now**: RimuLib 1.3.0 and TSS 1.6.0 (copied from the SA Series' `libs\`, no download); 1.2.3 and TSS 1.5.2 in
  `libs\old\`. The build still declares RimuLib `[1.2.3,2.0)` (no new API used).
- **Found on the way, then fixed (Adam: "Yes, fix the keys"), `ba14264`**: **TA-01's and TA-02's default keys (; ' , .)
  never existed.** RimuLib's `SkillKeys.register` skips every skill whose `selectWarning()` isn't null when it runs
  (at start, no world: both our warnings were non-null then; the warning is read nowhere else but the menu's warning
  page). Now both warnings return null while there is no world (TA-02 stays locked, no key, without SS-21 Quasar); a
  key only picks the skill, the server still refuses with its reason. Harness log at start (`run/clienttest/keys.log`):
  all four keys exist, defaults semicolon, apostrophe, comma, period. **A key already saved unbound in `options.txt`
  stays unbound**: Adam's instance has all four as `key.keyboard.unknown` (the SA Series' `SkillKeysMixin` had added
  unbound ones), so he presses Reset next to them in Controls; players who had the SA Series installed too, same (say
  so in the changelog). **In his instance now**: `TON 618 Addition 1.1.1 dark HUD and keys fix TEST [NeoForge
  1.21.1].jar` (the dark HUD fix TEST `.disabled`; copy in the test-builds folder above).
- Also seen: the released SA Series 1.8.3's plate at 1280x720 draws its red no-limits line over the "No cooldown" row
  (SA's, 1.9.0 redid that menu).

### >>> 2026-11-19 ~15:30 (Adam's PC): all six 1.1.1 ports built; Adam's test handed to Claude, paused <<<

- All six ports are committed locally (forge-1.21.1 `6239002`, fabric-1.21.1 `326c135`, forge-1.20.1 `07260c7`,
  fabric-1.20.1 `1958413`, fabric-1.21.11 `5edce9b`, fabric-26.3 `1af746e`) with test jars in his instances (no
  Fabric 1.20.1 instance). Nothing pushed.
- Adam couldn't test ("Claude tests for me", clickable, 2026-11-19): the test was sent to the ports session
  ([TON 618 Addition: Fabric 1.21.11 and 26.3 ports](#local_767018e5-bb55-4bd0-8b32-2cf3c28ff89e)). It got Fabric
  1.21.11 running (SS-21 Quasar's multi jar crashed it at first: not ours; the right inner jar fixed it) and then
  stopped waiting for his OK to control the game window (computer use): its question is open there. Then: the other
  instances, fixes on their branches, then the release prompts.
- New 2026-11-19: decisions/ideas from his Discord notes are in §10 (new UI with SA's 1.9.0 look, smoother hand-over
  into our films + audio: check with him, locked skills both ways, CelesTech); shared notes for every session in
  `C:\Users\adam_\Documents\Claude notes\` (`INBOX.md`, `IDEAS.md`; rule in his personal rules).

### >>> RESUME HERE (2026-11-18 ~18:30, Adam's PC): porting 1.1.1 to the six loaders (Opus, easiest first) <<<

- **Adam's picks (clickable)**: "Me, here (Recommended)" (Opus ports all six) and "Easiest first" (Forge 1.21.1, Fabric
  1.21.1, the two 1.20.1, then 1.21.11 and 26.3; each test jar into his instance as it builds). Method: a worktree per
  branch (`ports/<branch>`, `git worktree add ports/<b> <b>`), `git merge --no-commit --no-ff main`, resolve, adapt.
  Scripts used (copied from the scratchpad): `C:\Users\adam_\Documents\TON 618 Addition\port scripts (2026-11-18)\`.
- **forge-1.21.1 DONE, committed `6239002`** (local, not pushed): builds; dev client starts (Last Light #2, Quasar Jets
  #3, locked without SS-21, no errors). Forge 1.21.1 runs official names at runtime (javap of the instance's
  `forge-1.21.1-52.1.16-client.jar`), so main's new mixins work as is. Forge HAS `ToastAddEvent` (cancellable);
  no RenderGuiEvent: `HudRescue.afterGui` draws `QuasarJetsFilm.screen(g, partial, calledOff)` and `QuasarClient.hud`.
  Sound packets: `IMPACT_NEAR.getHolder().orElseThrow()`. The dev SERVER stops at Minecraft's EULA: not accepted
  for him (don't). Jar `TON 618 Addition 1.1.1 TEST [Forge 1.21.1].jar` + `ton618-1.1.0-forge.jar` are in his
  `Forge 1.21.1` instance (copy in `..\test builds\2026-11-18 TON 618 Addition 1.1.1 ports\`). TA-02 there needs
  SS-21 Quasar Forge 1.8.0 (CurseForge file 9575837; Fabric 9435512): **not downloaded, ask him first**.
  **Later (~19:45)**: SS-21 Quasar Forge 1.8.0 downloaded with his OK ("Yes, download it"; 4.2 MB, mod id
  `shooting_star_ss21`) into `libs/ports/` and his `Forge 1.21.1` and `Forge 1.20.1 (1)` instances.
- **fabric-1.21.1 DONE, committed `326c135`**: builds (Loom on JDK 25 at `~/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2`);
  dev client starts (both skills join, no errors of ours). Fabric hooks: `LevelRendererStagesMixin` (AFTER_SKY,
  AFTER_PARTICLES; as the SA Series' Fabric one), `QuasarGameRendererMixin` (frame start, shake via `CameraAccessor`),
  `FogRendererMixin`, `ToastComponentMixin`; `WorldRenderEvents.LAST` = AFTER_WEATHER; shaders in `shaders/core/`
  (`CoreShaderRegistrationCallback`); `QuasarContent.Registered<T>`. Loom remaps the mixin names in the bytecode
  (checked: `method_`/`field_`). Jar + `ton618-1.1.0-fabric.jar` + `rimulib-1.2.3-fabric.jar` in his `Fabric 1.21.1`
  instance (it also has TSS 1.4.2 and SS-21 Quasar 1.6.0 Fabric, left as they were).
- **forge-1.20.1 DONE, committed `07260c7`** (merged from forge-1.21.1): 1.20.1 drawing through `client/quasar/Gl1201`
  (pooled BufferBuilders, `vertex().uv().color().endVertex()`, `RenderedBuffer`; `RenderType.end` for the pack quads),
  `RenderTickEvent` START for the frame's start, phased tick events, `getPoseStack().last().pose()`, the sky pass taking
  the camera pose at `renderSky`'s HEAD (`LevelRendererMixin`), QuasarSky with `computeIfAbsent(load, new, name)`, no
  block codec, `ClipContext(..., (Entity) null)`, `Sound(String)`, plural data folders (`loot_tables`, `tags/blocks`).
  Refmap checked (SRG names). Dev runs don't work on this branch (RimuLib's mixin): his game test. Jar + TON 618 Forge
  + Quasar Forge in his `Forge 1.20.1 (1)` instance.
- **fabric-1.20.1 DONE, committed `1958413`** (merged from fabric-1.21.1; forge-1.20.1's loader-free drawing files copied,
  `fabric1201.js`): built first try. No Fabric 1.20.1 instance on his PC: jar only in the test-builds folder.
- **fabric-1.21.11 DONE, committed `5edce9b`** (2026-11-18 ~20:50, the fresh session
  [TON 618 Addition: Fabric 1.21.11 and 26.3 ports](#local_767018e5-bb55-4bd0-8b32-2cf3c28ff89e); merged from
  fabric-1.21.1): builds; dev client starts (both skills join, TA-02 locked without SS-21, no warnings of ours) and **all
  8 TA-02 render pipelines compile** (logged once at start: "TA-02 · Quasar Jets' render pipelines compiled"; the
  pipelines would otherwise compile mid-strike). No strike was cast in a dev game (no Fabric harness): never drawn yet.
  - Drawing as the film's `FilmGl`: `QuasarGl` (pipelines LIGHT / LIGHT_THROUGH / BAKE, `QuasarParams` std140 block =
    view + (Mode, Bake) + Param + Param2, the level's own `Projection` buffer, a pass per draw), `QuasarMesh` (public copy
    of `FilmMesh`), `QuasarAtlas` (painted into a texture of our own, a pass per tile; drawn with `RenderTypes.eyes` via
    `submitCustomGeometry` at `AFTER_ENTITIES`, each quad both ways round), `JetGl` (the film's scene into its own
    picture, `JetGl.present` blits it in the HUD between `nextStratum()`s), shaders `#version 330` with uniform blocks.
  - **The glow (Opus's pick: real HDR, the NeoForge look)**: 1.21.11 has no float `TextureFormat`, but its GL textures
    are made with a plain `glTexImage2D`, so `JetPost` makes each glow target as RGBA8 and at once re-specifies it as
    `GL_RGBA16F` under the same GL name (as the NeoForge branch does with its targets); the final pass goes into an
    8-bit picture the HUD shows. If that fails (not GL), the scene is drawn without glow (the old fallback).
  - Hooks: the frame's matrices at `LevelRenderer.renderLevel` HEAD (`LevelRendererStagesMixin`); Fabric `START_MAIN` =
    after the sky (no render pass open there on 1.21.11), `END_MAIN` = world, `AFTER_ENTITIES` = shader pack; shake after
    `Camera.setup` in `GameRenderer.updateCamera`; fog at `FogRenderer.computeFogColor` RETURN; the second sun as
    environment-attribute layers (`SKY_LIGHT_FACTOR`, `SKY_COLOR`, `CLOUD_COLOR`, `STAR_BRIGHTNESS`); `ToastManager`.
    **Last Light in the dark now draws after `LevelRenderer.renderLevel` returns** (RimuLib paints TON 618's dark at its
    RETURN, which painted out the hand glow/ring/wave before: Fable's P2), the overworld part stays at `END_MAIN`.
  - With a shader pack the sky's things are glowing-eyes geometry too (no sky stage reachable for Iris here): the named
    stars may show over the pack's clouds (as 1.1.0 did before its NeoForge fix). Not tried with Iris.
  - Other: packets as `CustomPacketPayload` records; `QuasarSky` as `SavedDataType` + codec; block/item ids on their
    properties + `useBlockDescriptionPrefix`; `assets/ton618_addition/items/*.json`; `isDarkOutside`, `getMinY`,
    permissions, `snapTo`, flash particles as `ColorParticleOption`, a `notifySound` packet helper.
- **fabric-26.3 DONE, committed `1af746e`** (merged from fabric-1.21.11): builds; dev client starts (both skills join,
  no warnings of ours), **all TA-02 pipelines compile**. Renderpearl versions (bind groups, colour targets, reversed
  depth, pooled `ByteBufferBuilder`s); **the glow on real `RGBA16_FLOAT` targets** (26.3 has them; `JetGl`/`JetPost`
  keep an 8-bit and a 16-bit-float pipeline each). Fabric 26.3's `START_MAIN` fires inside an open render pass, so the
  sky's things draw at `SkyRenderer.render` TAIL onto its target (`SkyRendererMixin`, as the SA Series); frame matrices
  from the `CameraRenderState`; frame start `GameRenderer.extract`; shake `CameraShakeMixin` (Camera.update TAIL);
  `COLLECT_SUBMITS` for the pack; `GuiGraphicsExtractor`, `Minecraft.gui` (toasts, overlay, `hud.isHidden()`), world
  clocks (`getDefaultClockTime`), `syncVelocity`, `PushReaction.POPPED`, no block codec, `CreativeModeTabEvents`.
- Scripts for both: `..\port scripts (2026-11-18)\fabric12111-*.js`, `fabric263-*.js` (+ `res.js`, the conflict helper).
- **Test jars**: `..\test builds\2026-11-18 TON 618 Addition 1.1.1 ports\TON 618 Addition 1.1.1 TEST [Fabric 1.21.11].jar`
  and `[Fabric 26.3].jar`. In his instances (no download needed: **SS-21 Quasar 1.8.0 Fabric was already on the PC**, a
  multi-version jar in his `Fabric 26.3` instance with 1.20.1/1.21.1/1.21.11/26.3 inside):
  - `Fabric 1.21.11 (1)`: + the test jar, `ton618-1.1.0-fabric.jar`, `ss-21-quasar-1.8.0.jar` (it had RimuLib 1.2.3,
    TSS 1.5.2, Fabric API).
  - `Fabric 26.3`: + the test jar, `rimulib-1.2.3-fabric.jar` and `ton618-1.1.0-fabric.jar`; renamed `.disabled`:
    `rimulib-1.2.2-fabric.jar` (the addon needs 1.2.3) and `ton618-experience-1.0.1-26.3.jar` (TON 618 1.0.1, id
    `ton618`; the addon needs 1.1.0). It has Iris + Sodium: a shader-pack test is possible there.
- **Next: one test round of all six in his instances** (Fabric 1.20.1 has no instance), then release prompts (GitHub
  releases per branch, CurseForge files). Pushing waits for his yes.
- Note: the mod description in the metadata (`mod_description`) still names only Last Light, on main too: add TA-02
  at the next release.

### >>> NEXT UPDATE (after 1.1.0): Adam's list, 2026-11-17 (started 2026-11-17 ~22:45, NeoForge first) <<<

**1.1.0 is being published by ChatGPT** (Adam, 2026-11-17 ~22:40: "i am already getting chatgpt to post the update")
with `..\releases\1.1.0\RELEASE-PROMPT.md` (the remade one, head `93901d5`). Check its report when he brings it:
GitHub `v1.1.0` Latest, CurseForge file + relations, description, summary, Source link.

**Adam, 2026-11-17 ~22:40, the next update's main items** (his words: "next update we make it more clear that its ton
618 idk how other then text saying that but yea i wanna show just how big ton 618 is just like how the original mod does
in its cutsceen and tell me some stuff about ton 618 like is it at the center of a galaxy and stuff / and obv improve
sfx especially at impact but in general overhaul them and make the explosion look better and more unique / write all of
this down and lets get started"):
A. **Make it clear it is TON 618, and show how big it is**, in TA-02's film: a scale reveal of our own (the original
   TON 618 mod's film shows its size: take the idea only, never its assets or shots, rimuru_dev's terms). Ideas: pull
   back from the Sun and the Solar System, drawn to scale, until TON618's event horizon dwarfs Neptune's orbit (~27x),
   with labels; name cards ("TON 618", "40 BILLION SUNS"); in educational mode short facts on screen.
B. **TON 618 facts** (told to Adam 2026-11-17; check each against a source before it goes in the game): an ultramassive
   black hole powering a hyperluminous quasar at the centre of a galaxy (the galaxy can't be seen: the quasar, ~140
   trillion Suns bright, outshines it); ~40 billion Suns (older estimate 66 billion), ~10,000x the Milky Way's Sgr A*
   (4 million); event horizon ~1,600 AU across (40 billion; ~2,600 AU for 66 billion): ~27x Neptune's orbit, light takes
   9+ days to cross; light left it ~10.8 billion years ago (redshift ~2.22); in Canes Venatici near the Big Dipper's
   handle; entry 618 of the Tonantzintla (Mexico) survey of 1957, first logged as a faint blue star.
C. **Sounds: overhaul them all, the impact most** ("obv improve sfx especially at impact but in general overhaul
   them"): TA-02's world sounds and both films' (the Sonniss recipe engine, `art/sonniss_sounds.sh`; Last Light too, as
   item 2 below).
D. **The explosion: better looking and more unique** (since 1.1.0 it is 5x bigger: craters 200-600 blocks; the camera
   at 110 blocks ends up inside the fireball). Make it its own thing (not a generic orange fireball): e.g. the jet's
   blue-white plasma, a shock dome, a column, the star's own colour.

**Adam's picks (2026-11-17 ~22:50, clickable)**: **1.1.1 = a polish update now** ("now we are making smth like version
1.1.1 or smth which is ig a polish thing and next one will be bigger polish with the changed cutscene"): the explosion
and the impact sound first (his pick), then the rest of the sound overhaul (C), the power slider (**0.1x to 3x of
today's size, warning above 2x**), the third-person HUD (**a mock-up for him first**). **1.2.0 = the bigger polish**:
the scale reveal (A), "prolly dad's plan" (Fable via his dad's Claude Max: write it one self-contained prompt then),
"lets leave that for later". He asked whether 1.1.0 needs the Quasar addon: no, it's optional (without SS-21 Quasar
TA-02 stays hidden, Last Light works; CurseForge lists it optional).
**The ports (Adam, 2026-11-17 ~23:05, asked "did you port the 1.1.0 to other mod loaders": no, NeoForge only, on
purpose)**: his pick, **after 1.1.1**: one porting round for all six with 1.1.0 + 1.1.1. ChatGPT's release: GitHub
`v1.1.0` published 2026-11-17 21:43 UTC at `45d8f29` (Latest; asset = the 1,615,938-byte jar with every fix); `main`
pushed to `45d8f29`; CurseForge part not reported yet.
**1.1.1 progress (2026-11-17 ~23:10, uncommitted on main, not pushed)**: the explosion (`quasar_world.fsh` modes 10
SPLASH and 11 ARC, the fireball recoloured into a plasma ball with field threads; `QuasarRender`: a blue-white flash,
the plasma splash, 5-12 magnetic arcs from the crater (seeded by the mark), a white condensation ring, electric sparks
and end-rod particles; from inside the plasma ball its glow over the screen instead (`insidePlasma`, `QuasarReticle`);
the atlas 6 x 5 with SPLASH/ARC tiles) and the impact sound (`quasar_impact_near` / `_far` replace the three old ones:
an electric impact core (Sonniss #151), a zap (#160), trailer booms, arcs, a buzz tail; each player gets a mix by
distance, pitched by the star, delayed by the speed of sound (17.15 blocks a tick, up to 3 s) unless they get the film;
`QuasarJets.impactSound`/`booms`). Harness: `marker-shaders.sh` takes `EXTRA="-Dton618test.star=0 ...distance=200"`.
**1.1.1, 2026-11-17 ~23:20 (committed `9dea376` explosion + sound, `28174d3` sliders + lock; version 1.1.1; test jar
`TON 618 Addition 1.1.1 sliders TEST` in his instance, the explosion TEST renamed `.disabled`; not pushed)**:
- **Power and Damage sliders** (`QuasarSkill.power()` = `Power(0.1, 3, 1, factor, craterAcross)`, `warnAbove` 2,
  `powerSliders` true: RimuLib shows Power **and Damage** together, no way to show Power alone). Power scales the
  crater's width (x p), depth (x p^0.75), debris (x min(p, 1.5)), the hits' reach, the safe distance
  (`QuasarStars.safeDistance(type, power)` = max(lobe, blast x p / 5) + 6), the marker, the aim preview
  (`QuasarClient.aimPower` reads RimuLib's internal `SkillTuning.power`), every visual (`QuasarCast.blast`). Damage =
  the hit at the blast's middle (falling to 0.35 at its reach); **∞, the menu's default (RimuLib's `maxDamage`), kills
  everything in reach** (before: the star's `blastDamage` 14-45, now unused). **Adam, 2026-11-17 (clickable): "Keep ∞
  kills"** as the default. Both in `QuasarFx` (power, damage). Tested
  at 0.3x: a 180-block crater, everything scaled (`..\test builds\2026-11-17 1.1.1 power 0.3\`). Not seen: the menu's
  slider rows (no harness view of TON 618's menu): his test.
- **Locked, not hidden, without SS-21 Quasar** (Adam: "it should be there but locked, make it work like that"): joins
  both menus always; `selectWarning` "Locked: needs the SS-21 Quasar addon (The Shooting Star: SS-21 Quasar)"; the
  server refuses ("Locked: ... needs ... installed"); no named stars. Not tested without SS-21 (the harness has it).
- He asked again (2026-11-17): the addon is needed only for TA-02 (optional for the mod); TA-02's name: **TA-02 ·
  Quasar Jets**. Proposed for 1.2.0's scale reveal (told him): a pull-back ladder Earth -> Sun -> Solar System -> Sgr A*
  -> TON 618's horizon filling the screen (27 Solar Systems across), cards "TON 618" / "40 BILLION SUNS" / "LIGHT
  NEEDS 9 DAYS TO CROSS IT", more facts in educational mode.
- **The third-person HUD, done (`3056700`)**: mock-up shown inline (a top-left panel, a phase bar, a jet-inbound
  countdown, three moments); Adam (clickable): "Build it like this", "Everyone near". `client/quasar/QuasarHud`
  (RenderGuiEvent.Post, RimuLib's overlay like `QuasarReticle`): for anyone within 2000 blocks of the mark (or the
  caster) without the film; the panel's rows change by phase (`rows`); the phase bar sits at the top beside the panel
  (at the bottom the hotbar hid it); "JET INBOUND" + countdown on a plate (the second sun's sky is bright); the
  educational line when the player's or the caster's educational mode is on. Harness shots `..\test builds\2026-11-17
  1.1.1 hud 2\`. Jar `TON 618 Addition 1.1.1 HUD TEST` in his instance (the sliders TEST renamed `.disabled`). The
  film's caption still says "66 billion Suns" (`JetScript`): check the facts (40 billion) in 1.2.0.
- **2026-11-18 ~00:15, Adam**: "lets finish up the sounds for now and work on other stuff for 1.1.1 we do need to make
  sfx better later but rn its not wotth too much attention" -> **the film sound overhaul is parked for later** (the
  impact is done). The HUD: "the ui is fine as is but i will likely wanna change it in the future" (his "the clock
  speeding up" was about the film's sounds, a mix-up). His picks for the rest of 1.1.1 (clickable, all four), **done
  `5548cd8`**: `[quasar_jets] max_power` (0.1-3, default 3) / `max_damage` (0 = no cap; above 0 ∞ becomes it), a cast
  above them held and the caster told in chat (tested: Power 3 on max_power 2 -> "held to Power 2.0×"; with Antares
  at 2x the harness's aim was then "too close": the server's own ray hits treetops nearer than the 126-block safe
  distance; `dev/test/limits.sh` now uses Alpha Centauri A); the debris thrown 40 a tick (not yet measured: his game
  opened); the film's card "about 40 billion Suns"; `dev/publish/changelog-1.1.1.md` + the description for 1.1.1.
  Jar `..\test builds\2026-11-18 TON 618 Addition 1.1.1 limits\TON 618 Addition 1.1.1 limits TEST [NeoForge
  1.21.1].jar`, **in his instance** since 00:20 (the HUD TEST there renamed `.disabled`).
- **Left for 1.1.1**: his test round; then release (NeoForge first: a remade release prompt for 1.1.1, as 1.1.0's);
  then the ports of everything to the six loaders. the description's "without it, TA-02 stays hidden" -> "locked" and a 1.1.1 changelog
  (Power/Damage sliders with ∞ = kill by default, the new blast and impact, locked TA-02) at release.
- **2026-11-18 ~09:15: 1.1.1's release prompt is made** (the user: "yea make the prompt"):
  `..\releases\1.1.1\RELEASE-PROMPT.md` (head `9eae265`; the jar staged as `..\releases\1.1.1\jars\TON 618 Addition
  1.1.1 [NeoForge 1.21.1].jar` = the limits TEST build; changelog `..\releases\1.1.1\changelogs\main.md`). Its checks
  passed: version 1.1.1 in the jar, origin/main `45d8f29` fast-forwards to main, no jars/libs/run in the diff, no
  v1.1.1 release yet. Sent to him for ChatGPT/Codex. **Meridian came out the same night** (The Shooting Star 1.6.0 +
  RimuLib 1.3.0; TON 618 itself still 1.1.0): checked 1.1.1 against RimuLib 1.3.0 (the user: "Check it first"):
  `dev/tools/RefCheck.java` in the SA repo finds 0 missing references, and the two fields `Ton618Link` reflects
  (`SkillSet.rows/skills`) are still there, so 1.1.1 ships as is with its range `[1.2.3,2.0)`. TON 618 1.1.0 and SS-21
  Quasar 1.8.0 check clean against both too. When porting to the other loaders, take RimuLib 1.3.0's file ids
  (Forge 9676536, Fabric 9676539).
- **2026-11-18 ~13:30: a new CurseForge description format** is drafted on the SA Series first (`C:\Users\adam_\
  Documents\Shooting Star Addition\releases\description rework\`); Adam reads it, and once he likes it, this project's
  description gets the same format ("i want to later apply it to all projects like ton addition"). The players'
  roadmap (`...\Shooting Star Addition\announcements\roadmap 2026-11-18.md`) now has a TON 618 Addition section
  (ports next, then 1.2.0's scale reveal, real music for TA-02 later).

- **2026-11-18: 1.1.1 release** (NeoForge 1.21.1 only). Pushed `main` to `e805b9f`; GitHub
  `v1.1.1` is Latest, with the checked jar and changelog:
  https://github.com/Adamko-10/ton618-addition/releases/tag/v1.1.1.
  CurseForge file `9680692` is **Approved** and public:
  https://www.curseforge.com/minecraft/mc-mods/ton-618-addition/files/9680692. Verified Release,
  Minecraft 1.21.1, NeoForge, Java 21, Client and Server, the exact Markdown changelog, and all four
  relations (TON 618 and RimuLib required, The Shooting Star [Demo] and SS-21 Quasar optional).
  The Summary and Source already matched. The 1.1.1 description is published and verified on the
  public page, with the existing links at the top preserved. Adam chose to keep the working live
  `/mc-mods/` Quasar link because the supplied `/mc-addons/` link returns 404. The other six loaders
  are still on 1.0.0.

Adam, 2026-11-17 (with a screenshot of TA-02 in third person, no film, Complementary on): "for next update write this
down". In his words, then what it means here:
1. **A power slider for the explosion**: "lets add a slider to select the power of the explosion just like the shooting
   star addition and the original shooting star". TA-02's Power slider in RimuLib's H menu (as the SA Series' skills:
   `Power(min, max, initial, factor, blocks)`, warning above some value), scaling the landing blast (crater radius and
   depth, damage, debris, the visuals). Decide the range with him (e.g. 0.1x to 10x of today's 5x size).
2. **Better sounds in both films**: "lets also improve sfx for both cutscenes" (TA-01 Last Light's and TA-02's films;
   the Sonniss recipe engine, `art/sonniss_sounds.sh`; ask him which moments sound wrong first).
3. **A HUD for third person without the film**: "lets also add a ui for 3rd person no cutscene ... make it similar to
   shooting star addition but make it make sense so adjust it accordingly yk". Like the SA Series' strike overlays
   (Halley/Luna/Proxima: a top-left readout, phase captions, a countdown/descent bar, the target), but saying what
   TA-02 is doing (the star's flight and its clock racing, TON618 feeding, the jet coming in, time to landing,
   distance to the mark, the lobe and its pop). Show him a mock-up first.
4. **The second sun showing over mountains/blocks** in third person without the film ("lets make this not display
   over mountains/blocks (its the "second sun" in no cutscene 3rd person)"): with a shader pack the sky's things are
   drawn after the particles at 100 blocks, so land further away than that didn't hide them. **Done already in 1.1.0
   (`6b14bcb`)**: with a pack a ray from the eye fades TON618's point of light and the second sun out when land is in
   the way (without a pack the land covers them anyway). Check in his game; the far part of the jet can still show
   over far land with a pack.
5. ~~With a shader pack the named stars show over the pack's clouds~~: **solved in 1.1.0 (`e2fd65e`)**, see the
   2026-11-17 ~22:30 section.
Also waiting from before: the ports of 1.1.0 to the other six loaders (after his OK on 1.1.0), real music for Quasar
Jets (written down 2026-11-14), Codex's test findings (2026-11-17 "its fixes go to the next update").

### >>> RESUME HERE (2026-11-17 ~22:30, Adam's PC): stars behind the clouds with shaders too; release prompt remade <<<

- **Adam (2026-11-17, with a Complementary screenshot: Betelgeuse and Wolf 359 over the clouds)**: "did u put it in my
  instance cuz still no" (it was: his game started 22:06, after the swap; with a pack the stars were still drawn after
  the particles), then "there are NO clouds without shaders so fix that please aswell, do whatever would make the stars
  go behind clouds with and without shaders and the clouds are still there". **No clouds without shaders = his own
  setting**: his `options.txt` has `renderClouds:"false"` (Video Settings > Clouds): told him to turn it on.
- **Done (`e2fd65e`, harness-tested with Complementary; `..\test builds\2026-11-17 stars in the sky pass\`)**: with a
  pack, `sky()` and `stars()` are drawn from **inside `LevelRenderer.renderSky`** (`client/mixin/LevelRendererMixin`,
  INVOKE `ClientLevel.getStarBrightness(F)F` shift AFTER, so Iris's stage is STARS) through `QuasarRender.skyPass` ->
  `QuasarAtlas.drawSky`: POSITION_TEX_COLOR quads (corners turned by the sky's pose, as vanilla's sun) drawn with
  `GameRenderer::getPositionTexColorShader`, which Iris swaps for the pack's `gbuffers_skytextured` in a sky stage
  (Iris `MixinGameRenderer`: isSky() -> SKY_TEXTURED_COLOR). How it was found: Iris's phases are set only inside
  `renderSky` (CUSTOM_SKY before it, NONE right after, so NeoForge's AFTER_SKY event is already NONE); Complementary's
  skytextured keeps anything that isn't the sun or the moon as "custom sky" (`color.rgb *= color.rgb *
  smoothstep(sqrt(VdotU))`: squared and faded toward the horizon; the sun/moon are discarded with its round style),
  and its `deferred1` keeps sky pixels and mixes its clouds over them. So the sky's looks are baked as square roots
  (`Bake` 2, tiles 18-22) and the vertex colour square-rooted. Seen: Barnard's Star in clear sky, Tau Ceti and Rigel
  hidden where its clouds are (the vanilla stars around them hidden too); the marker, jet and blast still draw. The
  land ray for the second sun (`quasarSeen`) is gone: the land covers sky things now. Iris's own read of the pack
  (`run/clienttest/shaderpacks`) and Iris's jar were read in the session scratchpad only.
- Jar `..\releases\1.1.0\jars\` (+ `..\test builds\2026-11-17 TON 618 Addition 1.1.0 stars behind clouds\`), **in his
  instance** (the 5x build renamed `... (5x blast).jar.disabled`); `RELEASE-PROMPT.md` remade.

### >>> 2026-11-17 ~21:45 (Adam's PC): 1.1.0 final (5x blast, stars under clouds); release prompt for ChatGPT <<<

- **Adam (2026-11-17)**: "make them display under clouds but the text above clouds", then "once youre done just make
  the explosion like 5x larger ... So for now finish that up and lets write a prompt for chatgpt to do everything with
  desc and changelog and everything inlcuded and then we plan". **Done, `6b14bcb`, harness-tested**:
  - **Stars under the clouds**: without a pack `sky()` and `stars()` are drawn at `AFTER_SKY` (the land and the game's
    clouds cover them; the names are the HUD's). With a pack (Complementary) stars drawn with the sky were **wiped by the
    pack's sky** (`..\test builds\2026-11-17 stars under clouds\pack\sky.png`), so with one everything stays at
    `AFTER_PARTICLES` (stars over the pack's clouds, item 5 of the next update's list) and `QuasarRender.quasarSeen`
    (a ray from the eye, up to the render distance) hides TON618's point and the second sun behind land.
  - **5x blast**: `QuasarStars.Type` blast 100/150/225/300 (craters 200 to 600 blocks across), depth 20/30/44/60,
    debris 90/135/195/270; `QuasarJets.RING` 1.25; the safe distance kept as before via `QuasarStars.safeDistance`
    (max(lobe, blast/5) + 6 = 26 to 66; also the reticle's "too close"); the 5 x 5 columns under the caster are kept;
    the shock ring to 3 radii, the dust wall to 2.1, the flash up to 900; particle counts capped as before;
    `JetCamera`'s wave timing matched (its camera, capped at 106 blocks off, is now inside the blast: the fireball fills
    the screen for a moment; not looked at in the film). **The crater**: left to RimuLib (`start()` = linger) a
    300-radius crater wasn't cut at the mark at all within 6 s; now `cutCrater` ticks the lazy cut from the middle out
    with the shock wave for 2 s, then `handOver` gives the rest to RimuLib (also when the strike ends). Tested: cut from
    the middle by LANDS+65, a scorched 600-block crater by +165 (`..\test builds\2026-11-17 explosion 5x crater\`).
    **Lag**: the debris took 1.5 s (fall() updated every neighbour: bamboo/plant chains): now its place is emptied with
    UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE first: 0.29 s. The log line "the landing blast (N blocks across): hits, debris,
    the crater set up" times it. A "Can't keep up" of ~2 s still comes over the next seconds while RimuLib cuts (the
    harness runs the client on the same PC).
  - Harness: `castQuasar` loads the target's chunk first (press 2 was refused twice: height read as -64).
  - Jar `..\releases\1.1.0\jars\TON 618 Addition 1.1.0 [NeoForge 1.21.1].jar` (+ `..\test builds\2026-11-17 TON 618
    Addition 1.1.0 5x blast\`), **in his instance** (the marker build renamed `... (marker and shaders).jar.disabled`).
  - **The release prompt for ChatGPT/Codex**: `..\releases\1.1.0\RELEASE-PROMPT.md`, made by
    `node dev/publish/make-release-prompt.js 1.1.0` from `dev/publish/release-prompt.template.md` (fills the head
    commit, the jar's SHA-256, and the changelog, description and summary word for word). Remake it after any change.
    Part A: push `main` only, `gh release create v1.1.0` (Latest). Part B: the CurseForge file (NeoForge 1.21.1, Java
    21, Client + Server, Markdown changelog, relations TON 618 + RimuLib required, The Shooting Star [Demo] + SS-21
    Quasar optional), the description (his links at the top kept as live), the summary, the Source link.
  - The description's Plans line now names the next update's items.

### >>> 2026-11-17 ~19:45 (Adam's PC): shaders done properly + landing marker built; 1.1.0 jar ready <<<

- **Built and harness-tested (`9549112`)**, both with Complementary Reimagined on and off (`dev/test/marker-shaders.sh`,
  films off in the harness's own `config/rimulib.properties` so the camera stays on the land; screenshots in
  `..\test builds\2026-11-17 marker and shaders\nopack\` and `...marker and shaders 3\pack\`):
  - **Shaders**: `client/quasar/QuasarAtlas` bakes every look of `quasar_world` into one 6x3 texture (192 px tiles)
    at `RenderFrameEvent.Pre` (shader uniform `Bake` = 1: light softly clipped, alpha 1 wherever there is light: packs
    discard near-transparent pixels, which is why the first try showed only the jet's core), then `QuasarRender` draws
    sky, stars and world at `AFTER_PARTICLES` through `QuasarGl.packing` -> `QuasarAtlas.emit` (NEW_ENTITY quads,
    uv moved into the tile, colour x alpha x strength) with an eyes render type (additive, LEQUAL depth, no fog of
    ours). The old late overlay (`QuasarRender.late`, its `GameRendererMixin` call, `QuasarGl.late`) is gone. Only
    light is added with a pack: smoke and the lobe's skin don't darken. Seen with the pack: the named stars, the marker,
    the jet, the fireball, the lobe, the pop's rings, all hidden by trees in front.
  - **Landing marker**: a `MARK` target as wide as the crater (half size blast/0.86, so its outer ring is the crater's
    edge), the small bright middle, a pillar of light (a `BEAM` band 170 blocks up, bright foot, fading out) and a
    glow at its foot; a ring runs out from the middle (`QuasarRender.markPulse`: one at the cast, then every 3 s,
    faster from SWEEP-160 to one every 0.8 s at LANDS, counted as a phase). For everyone who sees the strike, from the
    cast to the landing. The aim preview before the second press also shows the crater's size (red when too close).
  - Release texts: the changelog and the description mention the marker and the shader support.
- **The jar**: `..\releases\1.1.0\jars\TON 618 Addition 1.1.0 [NeoForge 1.21.1].jar` (and `changelogs\main.md`
  refreshed), a copy in `..\test builds\2026-11-17 TON 618 Addition 1.1.0 marker and shaders\`, and **in his instance**
  `NeoForge 1.21.1 (1)` (the copy there of the same name was overwritten, not renamed: PowerShell's `Test-Path` reads
  `[...]` as a wildcard; use `-LiteralPath` next time). Waiting for his look, then his OK to publish (push, GitHub
  v1.1.0, CurseForge file + description + summary + Source link).

### >>> 2026-11-17 ~19:15 (Adam's PC): before 1.1.0: real shader-pack support and a landing marker <<<

- **Shaders, round 1 (`b1ed557`)**: with Iris on, every TA-02 world effect was painted over by the pack; now they are
  drawn late (before the HUD, over the land, no depth). Visible, but Adam: "i want the mod to be compatible with
  shaders, so when your shaders are on by pressing K, you can see stuff like the stars in the sky without them looking
  out of place". **His pick: do it properly before the release** (clickable): like the SA Series (`GlowAtlas`: the
  shapes painted into a texture, drawn with Minecraft's glowing-eyes render type, which every pack draws: hidden
  behind land, fogged, the pack's bloom).
- **"The stars are invisible with and without shaders"**: not a bug. Reproduced with his exact 31 mods and his own
  settings (`-Pinstance`: `libs/instance/` = copies of his other mods; his `config/` and `options.txt` copied into
  `run/clienttest/`): the stars and the lock box show. His test world was new (morning) and it was day; at
  `/time set midnight` they show (Adam: "ok u right yes"). TON 618's own item is invisible with shaders: TON 618's
  renderer, not ours; he said leave it (no report). The test game closing itself looked like a crash to him (it isn't:
  "Stopping!", no crash report).
- **New (Adam, 2026-11-17)**: "can we add one of those 3rd person things where it shows where the thing will land in
  a cool way just like railgun and halley and luna etc": a landing marker on the ground at the mark, from the second
  press to the landing, visible in third person and to everyone near.
- Release 1.1.0 (NeoForge first) waits for both. Then his OK to publish.

### >>> RESUME HERE (2026-11-17 ~18:00, Adam's PC): releasing 1.1.0 on NeoForge; shader test first <<<

- **Adam (2026-11-17)**: "i tested it looks betetr, its good enough now ... the mod got approved [1.0.0 on CurseForge], lets
  decide whats next and can we honestly just release it like this". His picks (clickable): **release 1.1.0 for NeoForge
  1.21.1 now, the other six loaders later** (bends "port before CurseForge" this once; ports = 2-4 sessions, 1.21.11 and
  26.3 hardest); **a shader test first, done by Claude** ("you do that please"): his own test had Iris on but shaders
  off (`enableShaders=false`), and TA-02 has no shader-pack handling at all. Harness: `-Pshaders` adds `libs/shaders/`
  (Iris 1.8.14 + Sodium 0.8.13, copied from his instance, git-ignored); `run/clienttest/shaderpacks/` has his
  Complementary Reimagined r5.9.3 and `run/clienttest/config/iris.properties` switches it on. **Not run yet** (his game
  was open). Sounds still unheard ("i still cant hear").
- **Done for 1.1.0** (`8bff0cb`): Last Light's menu warning "Not yet: set TON618's Scale to ∞ and use it first" (his
  pick; the old one was cut off); **Last Light's icon redone as pixel art** (64x32 at 4x, `tools/icons/LastLightIcon.java`;
  Adam: "redo it in pixel style and make a rule for this": the rule "icons are pixel art, all in one style" is in his
  personal rules). TA-02's icon was already pixel art.
- **Release steps left**: the shader test (fix what breaks), the 1.1.0 changelog (`dev/publish/changelog-1.1.0.md`),
  the CurseForge description (a TA-02 section, frozen light under it; his description rework is a separate job), push
  `main`, GitHub release `v1.1.0` (NeoForge jar only), CurseForge file 1.1.0 (TON 618 + RimuLib required, SS-21 Quasar
  and The Shooting Star optional), the **Source link** on CurseForge (written down 11-15). Ask before each outward step.

### >>> RESUME HERE (2026-11-17 ~01:25, Adam's PC): the destructive landing in his instance; Fable reframes it <<<

- **Adam (2026-11-17, ~00:40, couldn't listen: late)**: "it looks much better overall but the impact is still super
  underwhelming, make it actually destructive pls". Done (`9a964a6`): `QuasarStars.Type` `blast` 20/30/45/60,
  `blastDepth` 10/15/22/30, `blastDamage` 14/22/32/45, `debris` 60/90/130/180; the crater cut by **RimuLib's
  `CarveApi.flatten`** (a bowl under the mark, scorched floor; the biggest: 64 chunks in 70 ms, worst tick 22 ms, logged
  in `run/clienttest/rimulib.log` as `ton618_addition:quasar_crater`); a **blast ring** (`QuasarJets.ring`, out to
  `RING` = 1.8 x `blast`, 4 ms a tick) flattening trees, bamboo, mushrooms and plants, coarse-dirt scorch, a few fires
  near the rim; hit within 1.3 x `blast`, harder shove; **the mark drops through trees to the ground**
  (`groundBelow`; the harness hit a jungle-giant treetop at y 94 before); the caster must aim at least
  `max(lobe, blast) + 6` away (66 for supergiants). Harness defaults now `distance` 110, `perch` 35.
- Test jar `..\test builds\2026-11-17 TA-02 destructive\TON 618 Addition 1.1.0 TA-02 destructive TEST [NeoForge
  1.21.1].jar`, **in his instance** (the "quality and sounds" jar there renamed `.disabled`).
- **Sent to Fable** (round 2, small): at the landing the film's camera now sits above the clouds (its framing scales
  with `blast`); keep the impact shots under the clouds and framed on the blast; its reticle preview should use
  `groundBelow`'s rule. Fable makes `... TA-02 impact TEST ...`; Opus commits and swaps it in.
- **2026-11-17 ~16:35**: the PC went off during Fable's round 2 (its last activity 00:20 UTC); its six files were left
  uncommitted on disk (cameras under the clouds, dust strips capped, the lobe as a ball in the crater, the ground
  preview on `groundBelow`; HANDOVER "Round 2" says where it stopped) and build. Resumed (Adam: "continue work, resume
  anything that needs to be resumed"): it checks them in the harness and makes the impact test jar. Ponytail checked:
  still off (config `off`, no flags). Adam asked for the Last Light previz: sent him `previz/last-light-previz.html`
  and the link https://claude.ai/artifact/Ta6Z91SRwtCYN1z27zKQtJ.
- **Fable now needs paid usage credits** (its resume stopped: "Fable 5.2 requires usage credits"). Adam: "you can do
  opus for now for fixes, my dad has a claude max plan and he has spare usage so i could ask him to run a prompt and give
  me the result". So **Opus finishes round 2 and does film/visual fixes in the film files** (in Fable's style; §7b's
  ownership rule is relaxed for fixes), and **big Fable jobs** (SA-04B / SA-03 previz, new films) get a self-contained
  prompt Adam runs on his dad's Max plan (in his personal rules too).
- **Round 2 finished by Opus** (`7c07cdf`): harness run clean, the impact shot under the clouds with the fireball
  filling the frame. Test jar `..	est builds6-11-17 TA-02 impactTON 618 Addition 1.1.0 TA-02 impact TEST [NeoForge
  1.21.1].jar`, **in his instance** (the destructive jar there renamed `.disabled`). Next: Adam's test (sounds included).

### >>> RESUME HERE (2026-11-17 ~00:30, Adam's PC): TA-02 redo round 1 + sounds in his instance; his test next <<<

- **Fable's first redo round is committed** (`45dd562`; its report: film look, the remote fast-forwarding the star's
  time with two clocks, sky stars with rays and names, the lock hint clear of RimuLib's label, the ground preview, the
  landing's flash/fireball/shock wave/dust/shake, cameras keeping a clear view, a new shot into the bowl, 12 reading
  windows for TA-02 (~27 s longer slowed) and 8 for Last Light (~9 s), the cue sheet). Its screenshots
  `previz/quasar-jets/redo-check/` stay on disk, kept out of git (`.git/info/exclude`: the repo is public). One of its 6
  test runs died with a native crash and no crash file (Medal was running; the 3 runs after were clean).
- **The sounds are made** (`0060021`, Adam's pick "Wait for sounds" = one jar with both): `art/sonniss_sounds.sh` (the
  SA Series' recipe engine, plus stereo for the film's sounds, seamless loops by crossfade, and a level fix for sounds
  under 0.4 s), 37 sounds in 40 files in `assets/ton618_addition/sounds/quasar/` (1.4 MB), `sounds.json`, subtitles for
  the world ones; `art/sounds/sources.md` lists every Sonniss file used. World sounds are registered
  (`QuasarContent.LOCK` ... `NIGHT`) and played by `QuasarJets.sounds()` (vanilla ones gone): lock (caster only), mark,
  second sun (every overworld player at their own place), jet incoming, impact by star size, debris, beam and lobe
  loops (replayed every 60 real ticks, stopped at the pop with `ClientboundStopSoundPacket`), lobe rise, pop, night;
  crystal per crystal, lift per thing picked up. The film plays its 22 by name (Fable's `JetCues`). Harness run clean
  (no missing-sound warnings of ours). **Nobody has heard them** (picked by measuring).
- **Test jar** `..\test builds\2026-11-17 TA-02 quality and sounds\TON 618 Addition 1.1.0 TA-02 quality and sounds TEST
  [NeoForge 1.21.1].jar` (+ SS-21 1.8.0), **in his instance** `NeoForge 1.21.1 (1)` (the TA-02 film jar there renamed
  `.disabled`). Next: Adam's test; his notes go to Fable (look) and Opus (sounds, land, logic).

### >>> RESUME HERE (2026-11-16 ~14:20, Adam's PC): Adam's first game test of TA-02; the quality redo plan <<<

**Adam's test (2026-11-16, his NeoForge 1.21.1 (1) instance, Iris on; log clean: cast, film, pop all ran)**, his words:
"it looks much worse in game, severe lack of sfx"; "the sail is supposed to make the star go really fast not over
years"; "there should be more time to read, maybe when educational mode is enabled you get more time"; "the stars in the
sky should only appear when you hold the ton remote specifically and they look a lil low quality and so does the
cutscene, needs heavy work quality wise in every aspect"; "text is over itself, when u select a star theres cyan text
over the other text"; "the quasar hitting the earth is really underwhelming, make it feel like a big impact and a proper
explosion"; "also apply a similar educational thing to ton 618 last light so like give more time to read if neccesary".
He asked whether ponytail made it worse: probably partly (its rules were in the context of Fable's film session and of
this session while TA-02 was built; Fable's own "Stop ponytail" doesn't switch it off). It is off in its own setting now.

**His picks (clickable, 2026-11-16)**:
- The sail: **the remote fast-forwards the star's own time** (as Last Light fast-forwards); Earth's clock stays normal
  (the film shows the star's years racing while seconds pass on Earth).
- Reading time: **longer in educational mode**, slowing **only the parts with text** (the action keeps its pace), for
  everyone watching that strike; **the same for Last Light**.
- Named stars in the sky (and the reticle): **only while holding TON 618's item or the Stellar Remote with TA-02
  picked** (done, `a5ec28e`: `QuasarClient.holdingQuasarJets()`). TA-02 stays on both.
- Sounds: **the Sonniss GDC 2026 library** (as Proxima's; the project folder of the SA Series has it:
  `C:\Users\adam_\Documents\Shooting Star Addition\sound library\sonniss gdc 2026\`, `catalogue.csv`, and its
  `envelope.js` measuring tool); only processed sounds go in the jar.
- The jet's landing: **a bigger crater + debris** (a real blast at LANDS: crater, blocks and debris thrown out, a shock
  wave, flash, shake, boom; sized by the star's type, kept clear of the caster).
- **Fable redoes film + world visuals** (the film, the named stars, the jet, the lobe, the reticle: drawing files only).
- **Test games on his PC are allowed when he isn't playing** (check no `javaw` first), for Fable and Opus, to screenshot
  and fix the look.
Done already: the doubled "locked" text (the server's cyan action-bar line is gone; the HUD hint stays).

**Who does what**:
- **Opus**: (1) a test harness for `runClient` that casts TA-02 / Last Light on its own and saves screenshots by film
  tick (for Fable and Opus); (2) educational mode's slow-down: the client sends its switch to the server, the cast
  carries it, a time warp (real tick -> story tick) slows only the reading windows, on the server and the client alike;
  the windows live in data files Fable owns (`quasar/QuasarReading`, `lastlight/LastLightReading`); (3) the landing
  blast's land part (crater, debris, damage, knock-back); (4) the Sonniss sounds (files, `sounds.json`, the world's
  sounds; Fable places the film's).
- **Fable** (one session, its prompt `prompts\FABLE-TA02-QUALITY-PROMPT.md`): the quality redo of the film and the world
  visuals, the sail story change, the landing blast's look, the reading windows, Last Light's reading windows.

**Done by Opus (2026-11-16, built, committed, not pushed, not run in a game)**:
- `a5ec28e` the doubled lock text; named stars + reticle only while `QuasarClient.holdingQuasarJets()`.
- `5631da5` **educational reading time**: the client sends its switch (`net/EducationChoice`, once per world and on every
  change, from `Ton618AdditionClient.sendEducation`), the server keeps it (`link/Education`), the cast carries it
  (`QuasarFx.educational`, `LastLightFx.educational`), and `ReadingTime.story/real` warp real ticks <-> story ticks
  inside the windows of `quasar/QuasarReading` / `lastlight/LastLightReading` (Fable's data files, empty until Fable
  fills them) on the server (`Running.tick`) and the client (`QuasarCast.tick`, `LastLightCast.tick`) alike.
  `realTick(partial)` = unslowed (soundtracks), `slowed()` = the strike's flag, `educational()` = the viewer's own
  setting (labels). The maths checked both ways (a scratch check, all ok).
- `5631da5` **the test harness**: source set `src/clienttest` (mod `ton618_clienttest`, never in the release jar),
  `./gradlew --no-daemon runClientTest -Dton618test.skill=quasar|lastlight -Dton618test.star=Antares
  -Dton618test.distance=80 -Dton618test.shots=600-1000/10 -Dton618test.edu=false` (also `world=normal|flat`,
  `seed`, default 618): game dir `run/clienttest` (its mods synced from `libs/` by `copyTestMods`), 1280x720, a fresh
  world at midnight, TON 618's item with TA-02 picked; shots `sky.png`, `locked.png`, then `<skill>_<story tick>.png`
  in `run/clienttest/screenshots/`; quits after the last; its log lines start `[TON618TEST]`. Last Light: it sets the
  devoured flag in `ton618:ton618` storage and moves the player to `ton618:devoured` (TON 618's own dark effects don't
  run). **Works** (2026-11-16 18:45, ~3 min a run): the player stands on a glass perch 12 blocks above the trees
  (`516ea25`: seed 618 spawns in jungle, where the second press hit leaves and was refused). Before a run, this must
  return nothing: `Get-Process java,javaw -ErrorAction SilentlyContinue | Where-Object MainWindowTitle -like
  'Minecraft*'`. Found by it: Adam's "cyan text over the other text" is our lock hint landing on RimuLib's
  selected-skill label ("◆ TA-02 · Quasar Jets", above the hotbar), not the action-bar line `a5ec28e` removed:
  Fable moves it; the film's camera at LANDS was buried in jungle trees (Fable).
- `2647305` **the landing blast** (land part) at LANDS: `QuasarJets.blast()` (hit + shove within 1.6 x `blast`, debris =
  surface blocks flung as falling blocks, `dropItem` off) and `crater()` (cut from the middle out over 6 ticks, scorched
  floor: blackstone, basalt, some magma); `QuasarStars.Type.blast` 7/10/15/20, `blastDepth` 5/7/10/13, `blastDamage`
  8/12/16/20, `debris` 20/30/45/60.
- Fable's session [Fable: TA-02 quality redo](#local_89d133b7-381e-4222-b7db-49bd77b35ec8) (Fable 5.2, High): started
  ~14:20 (came up on Opus: model set, then "go"), hit the 5-hour limit before writing anything, resumed 18:33 with the
  blast numbers. **Next for Opus**: the Sonniss sounds once Fable's cue sheet (`previz/quasar-jets/SOUND-CUES.md`) is
  in; commit Fable's files when it reports; the harness's first run when Adam isn't playing.

### >>> RESUME HERE (2026-11-15 ~20:25, Adam's PC): TA-02 built on main, not tested in a game; usage limit hit <<<

- **TA-02 · Quasar Jets is BUILT on `main` (NeoForge 1.21.1)**, version 1.1.0, committed (not pushed), Adam: "Build TA-02 in the mod". Code: `quasar/` (QuasarInfo, QuasarClock = Fable's ticks, QuasarStars = 8 stars + type table with the lobe/bowl/crystals x1.5 for "bigger explosion", QuasarSkill, QuasarJets = two presses + every world effect, QuasarSky = missing stars saved, QuasarContent = 4 frozen-light blocks + radio_lobe damage type), `net/QuasarFx|QuasarLock|QuasarMissing`, `client/quasar/` (QuasarClient, QuasarCast = what the film reads, QuasarRender = named stars, second-sun glare, mark, jet, dome, pop ring, QuasarReticle = lock-on HUD + pop flash, QuasarGl), `client/mixin/ClientLevelMixin` (second sun: sky light/colour/clouds/stars), `client/quasarfilm/QuasarJetsFilm` = **placeholder by Opus (bars, words, title, flood): Fable's package for the real film (Part 2)**. Server config `[quasar_jets]`. Joins only with SS-21 Quasar (`shooting_star_ss21`); `Ton618Link.showInMenu` (SA Series' fix) puts our skills back in the Stellar Remote's menu, which SS-21 freezes. Art: `tools/icons/QuasarJetsArt.java` (skill icon, crystal textures). **The mod icon is in the metadata now** (`ton618_addition_icon.png`, `logoFile`).
- Checked: builds; dev server with TSS 1.5.2 + SS-21 1.8.0 NeoForge (downloaded with Adam's OK to `libs/extra`, 4.2 MB): TA-02 joins TON 618's menu (#3) and the remote (#8); without SS-21 it stays hidden. **Not seen in a game.**
- Test jar: `..	est builds6-11-15 TA-02 Quasar JetsTON 618 Addition 1.1.0 TA-02 TEST [NeoForge 1.21.1].jar` + SS-21 1.8.0 NeoForge beside it.
- **Next**: Adam's game test; then Fable's in-game film (a Part 2 prompt, contract = QuasarJetsFilm's 4 entry points + QuasarCast); open: players carried (config, off). Then the 6 ports (with the 1.0.1 fixes and Fable's P2), **only once TA-02 is tested and polished on the main version (NeoForge 1.21.1) until Adam says it's good** (Adam, 2026-11-16: "FIRST we make the main one (usually neoforge 1.21.1) we test it and polish it and make sure its good, then we make other mod loader jars"); then the other loaders' jars, then CurseForge. Fable queue after: SA-04B, SA-03A Roche.

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

## 0. Rules (Adam's; also in his personal rules, `C:\Users\adam_\.claude\CLAUDE.md`)

2026-11-16: his personal rules now carry the general rules of every project ("Every mod project": git, releases,
CurseForge, other people's assets, the main version first, test jars in his Modrinth instance); they apply here too.

- Short replies, answer first (he skims). **Every open question as clickable choices** (AskUserQuestion); re-ask
  anything he left unanswered.
- After coding: a list of **what he needs to do**; for testing: a list of **what to test**.
- Budget-conscious: say what a big step costs (time, usage, money) before it; don't over-test (he tests in game).
- **Never push, publish or upload without asking.** GitHub: https://github.com/Adamko-10/ton618-addition. Commit as
  `Adamko-10 <Adamko-10@users.noreply.github.com>` (set in this repo's git config) with the session's Co-Authored-By
  line.
- **Never commit or ship other people's jars, code or assets** (`libs/*.jar` is git-ignored). Decompile only into a
  scratch folder outside the repo. Never use rimuru_dev's assets (textures, shaders, sounds, models, code), nor SS-21
  Quasar's: make our own. NASA / ESO / Solar System Scope imagery is fine with credit (download from the source, ask
  first: file, source, size). **No Bedrock ports.** Credit TON 618 and RimuLib as required dependencies (The Shooting
  Star and SS-21 Quasar as optional ones).
- New features **and fixes** on **NeoForge 1.21.1 first** (Adam plays it), tested and polished there until he says
  it's good; only then the other loaders' jars, then CurseForge (his personal rules).
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
- Adam plays on his own PC (Modrinth, NeoForge 1.21.1): test jars go in `..\test builds\<date> <what>\` **and into
  his Modrinth instance** `D:\ModrinthApp\profiles\NeoForge 1.21.1 (1)\mods` (older copies `.disabled`, never while
  he plays); tell him the path. (On the VM: `C:\Users\User\Desktop\test builds\` and Downloads.)

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
for the film. **The film side is done too** (Fable; committed): `JetSynth` no longer copies those cues (keeps the pickup pops,
drone, music); the last reticle says "MISSING · back in N nights" ("back tomorrow night" for 1; "FED TO TON618" when
the server keeps no star out). Test jar rebuilt in place (holds both sides). Not heard or seen in a game. Known limits: the caster isn't posed in shot 8; films off (J) = world effects only; the last
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

- **From Adam's Discord notes to himself** (Carl-bot DMs, exported 2026-11-19; only what wasn't written down before):
  - **A new UI for TON 618 Addition** (2026-11-14): "make ton 618 ui new just like how the shooting star addition ui is
    getting an update", i.e. its plates/panels redone together with the SA Series' 1.9.0 settings menu (option B).
  - **"I will fix audio issues and make the transition from ton 618 to cutscene smoother"** (2026-11-17, a forwarded
    message, then "Do this"): whose promise it was and whether it was done isn't in these notes: check with him (the
    smoother hand-over from TON 618's own film or dark into ours, and the audio issues).
  - **Locked skills both ways** (2026-11-19: "Display ton618 addition skills when shooting star addition is installed
    but lock it and tell what u need installed"; his answer, clickable: "Both ways"): each addon lists the other's skills
    as locked until it's installed (with the SA Series but not this addon, the remote shows Last Light and Quasar Jets
    locked: "install TON 618 Addition and TON 618"; this addon shows the SA Series' skills the same way). Not built.
    **The SA Series' half is built** (SA 1.9.0 on its main, not released; from the inbox, 2026-11-20). How it does it,
    for the reverse here: placeholder RimuLib skills with their own ids (SA uses `ton618_last_light`,
    `ton618_quasar_jets`, never the real ids, so icons, keys and lang never clash when both are installed), joined with
    `SkillApi.extend` only while the other mod is not loaded, `selectWarning()` = what to install (so RimuLib gives them
    no key), the cast denies with what is missing, Convergence and commands skip them, a greyed icon with a pixel
    padlock (SA: `art/make_locked_icons.sh`). SA code: `compat/tss/LockedSkill`, `compat/tss/client/LockedCard`. Mod id
    to check for the SA Series: `shooting_star_addition`.
  - **CelesTech compat** ("one sky": a star launched by Quasar Jets gone from CelesTech's radar and the other way
    round; black holes): ideas in `C:\Users\adam_\Documents\Claude notes\IDEAS.md`.

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
