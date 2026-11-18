# Previz hand-over: TA-02 · Quasar Jets (Fable)

Kept by the Fable 5.2 session started 2026-11-15 from the Opus session that runs both projects (job:
`prompts/FABLE-TA02-QUASAR-JETS-PREVIZ-PROMPT.md`). If usage runs out, resume from here.

## Status (2026-11-15, evening): DONE, v1 approved

- **Previz v1 is built, checked and published**: https://claude.ai/artifact/DY9xtGvDFmCVz9jUnLQKVv (private; the same
  link for every version: publish `previz/quasar-jets/quasar-jets-previz.html` again, from another session with that
  link as `url` after reading it).
- Notes for the game (tick table, shot table, star types, labels, sounds, what Opus needs): `NOTES.md` here.
- **Adam approved v1 on 2026-11-15, no v2** ("this is good enough, great even ... no need for a v2"). His two notes
  are for the mod version, not the previz (`NOTES.md`, "Adam on v1"): **make the explosion bigger** and **keep the
  labels on screen a little longer**. The second sun: **"Ends with the film"**. He said Opus will handle them.
- Nothing more to do here until the in-game film is asked for (a new prompt, as Last Light's Part 2).
- Checked in the browser pane (Betelgeuse at 24 ticks across the film, a red dwarf's lobe, a blue giant's jets): it
  draws, no console errors. The last change (shot 8's camera lower so the caster is in the picture) was not re-checked.
- For the Opus session: commit `previz/quasar-jets/` (Fable ran no git). No proposals were needed.

## Rules I follow

- Write only in `previz/quasar-jets/` (proposals: `reviews/fable/PROPOSALS.md`). No git, no mod code.
- Nothing of rimuru_dev's or SS-21 Quasar's; nothing downloaded; all art procedural and ours. Ponytail off.

## Adam's answers (2026-11-15, clickable)

- **Where the jet lands: "Two presses"**: press 1 at a star in the night sky locks it; then look at the ground and
  press again: the jet lands there, and the film starts on the second press.
- **Who sees the film: "Caster + nearby players"** (about 128 blocks); everyone else only sees the world effects (the
  night turning to day, the jet far off).
- **Damage: "Lobe hurts, jet carries"**: the thin jet only lifts and carries; the radio lobe shoves and damages what
  is inside it (more for bigger stars).
- **Stars: "8: two per type"**: red dwarfs Barnard's Star, Wolf 359; Sun-like Alpha Centauri A, Tau Ceti; blue giants
  Rigel, Spica; red supergiants Betelgeuse, Antares.

## The film as I am building it (game ticks, 20 a second; 960 ticks = 48 s)

MARK 0 · LOCK 38 · ENGINE 80 · PUSH 150 · FLIGHT 200 · ARRIVE 330 · SHRED 350 · IGNITE 440 · JETS 530 · TURN 610 ·
FLOOD 665 · DAY 680 · SWEEP 745 · LOBE 830 · POP 890 · NIGHT 905 · END 960.
Shots: 1 Lock-on 0-80 · 2 The star engine 80-200 · 3 The flight 200-330 · 4 The meal 330-440 · 5 Ignition 440-530 ·
6 The jets 530-610 · 7 Blazar turn 610-680 · 8 Second sun 680-745 · 9 Swept along 745-830 · 10 Radio lobe 830-960.

## Changes from the plan (to show Adam)

- The film is 48 s, not 45 (room for the text: his Last Light note was "slow down how fast the text passes").
- Two presses, so the film opens on the ground mark, then swings up to the star that is already locked.
- The second sun lasts only as long as the film (night returns at tick 905), where `CLAUDE.md` §10 says about a
  minute: Adam picked "Ends with the film".

---

# Part 2: the in-game film (Fable, 2026-11-16)

Job: `prompts/FABLE-TA02-FILM-PROMPT.md`. Kept by the Fable 5.2 session started from the Opus session that runs both
projects. If usage runs out, resume from "Next".

## Status: BUILT, in the test jar, never seen in a game

- **The real film replaces Opus's placeholder** on `main` (NeoForge 1.21.1, mod version 1.1.0 unchanged). The four entry
  points are kept exactly (`QuasarJetsFilm.init/start/stop/active`), so `QuasarClient` works as it is. **No change
  outside the film's own files was needed.**
- **Test jar**: `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-16 TA-02 film\TON 618 Addition 1.1.0
  TA-02 film TEST [NeoForge 1.21.1].jar`, with `ss-21-quasar-1.8.0-neoforge.jar` beside it (TA-02 only appears with
  SS-21 Quasar installed).
- Checked without the game: `./gradlew build` passes; the shader compiles and every material draws in a browser
  (WebGL2; `previz/quasar-jets/film-check/shader-check.html`); the soundtrack was rendered on its own (49.8 s, made in
  about 1 s, peak 26,000 of 32,767: no clipping, every cue at its tick). **No game run** (Adam wasn't asked).
- **2026-11-16 ~13:30: the ending made 2 s longer** at Adam's pick (decision 1); committed by Opus (`86bce18`).
- **2026-11-16 ~13:45: the film side of P3 and P4** (Adam said yes; Opus built the rest in `2cb94f6`): the track
  drops its copies of the world sounds (decision 9) and the last reticle says "back in N nights" (decision 8). Builds;
  the track was rendered again on its own (49.8 s, no clipping, quiet from 745 to the music at 895); the test jar was
  rebuilt in place. **Not committed**: `JetSynth.java`, `JetHud.java`, this file, `reviews/fable/PROPOSALS.md`.
- No git was run. Proposals for Opus: `reviews/fable/PROPOSALS.md`, "2026-11-16".

## What was built (all in `client/quasarfilm/` and `shaders/quasarfilm/`)

| File | What |
|---|---|
| `QuasarJetsFilm.java` | The four entry points; hands the camera to RimuLib; starts the soundtrack; draws the screen layer on `RenderGuiEvent.Post` and on the **cancelled `RenderGuiEvent.Pre`** that `client/HudRescue` posts when TON 618 calls the HUD off for a film; holds toasts back while it plays |
| `JetScript.java` | The timeline: every curve by game tick (no retime table), the captions, the labels, the numbers |
| `JetCamera.java` | The world camera, one RimuLib cutscene on the strike's clock: shot 1, a hold under the space scene, shots 8, 9, 10 |
| `JetSpace.java` | The film's own opaque space scene, ticks 80 to 680 (shots 2 to 7) |
| `JetHud.java` | Bars, captions, reticle, year counter, readouts, title card, educational labels, flashes, the flood |
| `JetSynth.java`, `JetSound.java` | The soundtrack, made by code (no `sounds.json`, no sound files), played like Last Light's |
| `JetGl.java` | The one shader program and the drawing helpers (a copy of Last Light's `FilmGl`: the films share no code) |
| `shaders/quasarfilm/quasar_jets.{json,vsh,fsh}` | One program, 11 materials (hole, star, mirror, jets, tail, galaxies, glows, dots, streaks) |

Nothing is simulated: every position is a function of `cast.tick(partial)`.

## Decisions (and where they differ from the previz)

1. **Labels and captions sit longer (Adam's note).** Every key tick of `QuasarClock` is unchanged and the film is
   still 960 ticks. None is now on screen for less than about 2.3 s:
   - Captions (were / now): 6-36 → **4-50**; 42-78 → **52-100** (the star's name, held across the flash cut: the same
     star, now close); 86-146 → **102-150**; 152-196 → **154-202**; 390-436 → **388-438**; 614-662 → **612-662**;
     692-741 → **692-742**; 896-926 → **892-940**. The rest as in the previz.
   - Labels (were / now): star type 44-78 (1.7 s) → **40-94 (2.7 s)**, pointing at the star in the sky and, after the
     cut, at the star itself; mirror 96-146 → **98-150**; "it is slow" 156-196 (2.0 s) → **154-204 (2.5 s)**; ribbon
     392-436 → **388-438**; blazar 616-660 → **612-662**; second sun 694-741 → **692-742**; crystals 898-924 (1.3 s) →
     **892-942 (2.5 s)**. The rest were already 2.4 s or more.
   - The star's readout (TYPE / DISTANCE / SIZE / MASS) runs 42-98 instead of 42-78.
   - To make room at the end, shot 10's tilt up to the sky is **928-948** (previz 922-948).
   - **The ending is 2 s longer (Adam, 2026-11-16, clickable: "2 s longer").** The film's own end is tick **996**
     (`JetScript.END`; the strike's `QuasarClock.END` stays 960 and no world effect moved): the camera holds on the gap
     in the sky until 984 (`LAST_SHOT_END`), the "MISSING" reticle is on 932-994 (about 2.6 s at full strength, was
     1.3 s), the bars leave 976-994, the soundtrack is 49.8 s (M2 and the drone fade by 994). It fits because
     `QuasarClient` keeps the strike until `QuasarClock.END + 40` = 1000: **if Opus ever shortens that, the film's END
     must come down with it** (it must stay under it).
2. **Shot 10 is framed for the bigger lobe** (radius 9 / 15 / 24 / 33). The camera stands on the far side of the caster
   from the mark, at `max(caster's distance + 8, 2.3 x lobe radius)` from the mark (so the whole dome and its 1.3 x
   pop fit), `max(6, 15% of that)` up, looking at a point 0.3 lobe radii over the mark. FOV 58, narrowed (down to 34)
   when the caster marked a spot far off, so the dome isn't lost in the picture. The pop shakes harder for bigger
   lobes (capped) and the film's flash at the pop is a little stronger than the previz's (0.8).
3. **The film draws nothing in the world.** From 680 on it only frames what `QuasarRender` draws (second sun, jet,
   dome, pop ring, mark, named stars). The crystals, the bowl, the carried things are the real ones.
4. **Films off or skipped (RimuLib's film key): nothing of the film is shown**, not even the flood: there is no trip to
   hide this time. The world effects are seen from where the player stands.
5. **The pop's flash.** `QuasarReticle` draws it on `RenderGuiEvent.Post`, which doesn't fire while a film has the HUD
   called off. So the film draws its own flash at the pop **only in frames where the HUD was called off**; in a frame
   where `Post` fires, Opus's flash is the only one. Never both.
6. **The caster isn't posed**: no turn to the second sun, no hand shielding the eyes (the film can't pose a player, as
   in Last Light). Shot 8 sits low behind them, looking past them at the light.
7. **Camera and land.** Shots 8, 9, 10 lift the camera when the ground under it is higher than the shot's own height
   (heightmap). Shot 1's swing from the mark to the star goes round the short way (a real turn, so a star behind the
   caster doesn't flip the picture).
8. **The reticle at the end** says "MISSING · back in N nights" (P4, applied 2026-11-16): N =
   `QuasarClient.backOnDay(star) - QuasarSky.day(level.getDayTime())`. The server counts tonight as the first night, so
   "back in 3 nights" means gone tonight and the next two. N = 1 reads "back tomorrow night"; N of 0 or less (the
   server keeps no star out of the sky, or hasn't said) reads "FED TO TON618", with no promise about nights.
9. **The soundtrack no longer has the world part's big sounds** (P3, applied 2026-11-16): the server plays them at the
   mark for everyone near (`quasar/QuasarJets.sounds`: the jet arriving and landing, the lobe, the pop, the crystals),
   and the film's camera is near the mark in shots 9 and 10 to hear them. Dropped from `JetSynth`: the jet's whine and
   hum (745 and 760 on), the landing (795), the lobe's voice and two hisses (830 to 890), the pop (890, 892), the
   crystal chimes (893 to 922). Kept from 745 on: the drone, the second sun's hum, the soft pops of things being
   lifted (776 to 826), the music. M1 (150 to the crash at 665) and M2 (from 895) are placeholder pads, as agreed.
10. **Flashes**: the three short ones (the cut at 330, the ignition, the pop) are softened to a third for players who
    switched on the game's "Hide Lightning Flashes". The flash into space at 80 and the flood at 665 are not: they
    hide cuts.
11. **The space scene has no depth buffer** (it is drawn in the HUD's pass, like Last Light's): things are drawn back
    to front by hand. The star is one quad with a lit-ball shader (stretched along its way when the tide has it); the
    mirror is a real cap of 10 x 32 panels, split each frame into the half behind the star and the half in front;
    the jets and the tail are real cones whose corners carry how squarely they face the eye (that gives the core).

## To test in the game (Adam)

Night, overworld, SS-21 Quasar installed, TA-02 picked. Press 1 at a named star, press 2 at the ground.
1. Shot 1: the camera behind you on the mark, swings up to the star, the reticle closes ("LOCKED"), zoom, flash.
2. Space (4 to 34 s): the star, the mirror unfolding with its lit inside, the tail, "IT MOVES" and the year counter,
   stars streaking, TON618 growing ahead; then the mirror falls in, the ribbon winds into the disc, the disc goes
   blue-white, the title card, two jets, the tip toward us, white.
3. Back in the world: day, the second sun low in the picture (shot 8); the jet coming in from the horizon to the
   mark (shot 9); the dome whole in the picture, the pop, the crystals; then up to the sky and "MISSING" (shot 10).
4. Words: are the captions and labels on long enough now? (Educational mode: the plate in the H menu.)
5. Sound: a track all the way through; boom at the ignition, crack at the jets, crash at the white. From the jet's
   arrival on, the loud sounds are the world's (heard by everyone near the mark): do they sit well with the film, and
   is the pop at 44.5 s loud enough from shot 10's camera?
6. Try one small star (Barnard's Star) and one big one (Betelgeuse): colours, sizes, shot 10's framing.

Known limits to tell him: with a shader pack (Iris) the world effects are Opus's and may differ; the space scene,
words and flashes are drawn over the finished picture and should show. If the mark is very far away (it can be 256
blocks), shots 9 and 10 look at land that may not be loaded.

## Next

- Adam's game test, then fixes from what he sees (framing numbers are in `JetCamera`, timings in `JetScript`).
- Opus: commit (list in `reviews/fable/PROPOSALS.md`), update CLAUDE.md §7b's status, pick proposals P3/P4.

## For the ports

- **Everything is in the HUD pass**: unlike Last Light there is no in-world drawing, so Fabric needs **no hook after
  the level render** (P2 doesn't apply). The film only reads the level's view and projection matrices and the camera
  position once a frame (`QuasarJetsFilm.world`, at `AFTER_SKY`): on Fabric from `WorldRenderEvents` (any stage).
- **Forge 1.21.1** (no `RenderGuiEvent`): `HudRescue.afterGui` must call `QuasarJetsFilm.screen(graphics, partial,
  true)` every frame, as it does for Last Light (make it public there).
- **1.20.1**: `JetGl.begin/vertex/draw` on the old `BufferBuilder` API, as `FilmGl` was ported; `JetSound`'s
  `Sound(String, ...)`; `new ResourceLocation`. Fabric: the shader files go in `shaders/core/`.
- **1.21.11 / 26.3**: `JetGl` becomes pipelines as `FilmGl` did, but only three are needed (no depth anywhere):
  opaque, additive, premultiplied. The fragment shader uses `discard` in two materials (mirror, jets); it needs the
  same uniform-block rewrite. The scene goes in its own texture and is blitted in the HUD, as Last Light's.
  `JetHud`'s 2D pose calls as `FilmHud`'s.

## The shader check page

`previz/quasar-jets/film-check/shader-check.html` holds a copy of `quasar_jets.fsh` and draws each material once
(open it in a browser; it says "Shader compiled and linked" or prints the error). After changing the shader, paste
the new file between its `<script id="fsh">` tags.

---

# Part 3: the quality redo (Fable, 2026-11-16)

Job: `prompts/FABLE-TA02-QUALITY-PROMPT.md`, after Adam's first game test of TA-02 ("it looks much worse in game,
severe lack of sfx", "needs heavy work quality wise in every aspect", "make it feel like a big impact and a proper
explosion"). Fable now owns all of TA-02's look: the film **and** the world visuals. If usage runs out, resume from
"Next".

## Status (2026-11-16, ~23:55): DONE for a first round, in a test jar, seen in the game, not heard

- **Test jar**: `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-16 TA-02 quality\TON 618 Addition 1.1.0
  TA-02 quality TEST [NeoForge 1.21.1].jar`, `ss-21-quasar-1.8.0-neoforge.jar` beside it. Opus puts it in Adam's
  instance.
- **Seen in the game** through Opus's harness (six runs; "Seen in the game" below). Pictures to look at:
  `previz/quasar-jets/redo-check/` (five contact sheets of the new look by story tick, and three of Opus's first
  harness shots of the old look as `before-*.png`).
- **Not heard**: no sound was listened to. The cue sheet is written; the files are Opus's to cut.
- One line changed after the last run and is in the jar unseen: a blue star's cooler gas is drawn dark blue (it was
  brown on Rigel).
- Nothing is committed (Fable runs no git): the list is in `reviews/fable/PROPOSALS.md`, "2026-11-16 ... Part 3".

## What changed, by Adam's points

| Adam said | What was done |
|---|---|
| "looks much worse in game ... low quality" (the film) | The space scene is drawn into a picture of its own that can hold light brighter than white, then given a real glow (bloom) and brought to the screen with a soft shoulder (`JetPost`, `shaders/quasarfilm/quasar_post`). New materials: a boiling star with a corona and loops of gas, a mirror of unlike panels that glint, deep space with faint gas clouds behind everything, a finer disc for TON618 with a thin photon ring, thin bright jets, a ribbon of 1,700 motes. The star no longer burns out to a white ball; the lit quasar keeps its black shadow |
| the stars in the sky "look a lil low quality" | Each named star is a shader sprite: a hot core, a halo, four long rays and four short, twinkling. They are drawn **after the clouds** (a cloud hid Antares in the first test run), hidden only by land (a ray from the eye), and each has its name under it while the remote is held. The launched star stays in the sky until the film's cut (it used to vanish at the second press, so the film zoomed on nothing) and swells as the film zooms in |
| "text is over itself" | The lock hint is a card of its own at 58% of the screen's height, clear of the game's line with the held item's name. The lock-on HUD is redrawn in the film's lettering: a reticle that closes as the aim gets good, a ring of ticks, a card (name, type, distance, size, mass, "PRESS TO LOCK" / "LOCKED") |
| "the sail is supposed to make the star go really fast not over years" | The remote fast-forwards the star's own time: a dial appears round the star, its hand creeps (a real star engine is slow), then races into a ring of light; the star is at full speed 3 s later. Two clocks in the top bar: **THE STAR'S CLOCK** racing through years, **EARTH'S CLOCK** counting plain seconds. New words: "FAST FORWARD · the remote speeds up the star's own time", "ACROSS THE UNIVERSE · 18 billion light years, in seconds for us" |
| "the quasar hitting the earth is really underwhelming" | At LANDS 795, for everyone: a flash, a lumpy fireball that swells and rises, a shock wave racing out over the ground to 8 crater radii, a wave of dust behind it, the game's own particles (explosions, sparks, embers, smoke rising from the crater until the lobe), a flash of the screen and a shake of the view for players near it. In the film: the camera stands back far enough for the whole blast, takes a hard kick and a punch of zoom at the landing and a second kick when the shock wave reaches it; a new caption "IMPACT". Opus built the land part (crater, debris, damage) |
| "more time to read ... when educational mode is enabled" | `quasar/QuasarReading.WINDOWS`: one **hold** inside each of the 12 labels' time, where the picture is calm, slowed 3 to 6 times (+1.5 to +2.7 s each, +27 s in all). Last Light: `lastlight/LastLightReading.WINDOWS`, 8 mild windows (+9 s). Both soundtracks are made for the cast's windows, so they stay in step |
| "severe lack of sfx" | `previz/quasar-jets/SOUND-CUES.md`: 13 world sounds and 22 film sounds for Opus to cut from the Sonniss library. The film plays each file by name as soon as `sounds.json` has it (`JetCues`), and drops its own code-made version of that cue |

Also new, for everyone (not only the film's viewers): from IGNITE a new point of light appears in the night sky where
TON618 is and grows until the second sun; the jet is seen coming in from the second sun long before it reaches the
land (its far part is drawn on the sky); the mark is shown where the second press would land.

## Files (all Fable's; no git was run)

| File | What |
|---|---|
| `client/quasar/QuasarGl.java` (rewritten, now `public`) | The world shader and its shapes: quads facing the eye, the jet's band, domes, a lumpy dome, flat rings, a standing strip, the jet's far part on the sky |
| `client/quasar/QuasarRender.java` (rewritten) | Sky: the new point of light where TON618 is, the second sun, the jet's far part. World: named stars, the mark (and where the second press would land), the jet, the landing blast, the lobe and its pop. Each tick: particles. A shake of the view for players without the film |
| `client/quasar/QuasarReticle.java` (rewritten) | The lock-on HUD and the two screen flashes (landing, pop) |
| `shaders/quasar/quasar_world.{json,vsh,fsh}` (new) | One program, 9 materials (star, sun, beam, dome, shock, dust, fireball, mark, glow) |
| `client/quasarfilm/JetPost.java` (new), `shaders/quasarfilm/quasar_post.*` (new) | The space scene's glow |
| `client/quasarfilm/JetCues.java` (new) | The film's cues from sound files |
| `client/quasarfilm/JetSpace, JetScript, JetHud, JetCamera, JetSynth, JetSound, JetGl, QuasarJetsFilm` | The redo itself; `QuasarJetsFilm` gained `hasCamera()` and registers the two new shaders (it has the mod bus) |
| `shaders/quasarfilm/quasar_jets.fsh` | New materials 12 (corona), 13 (dial), 14 (deep space); the others reworked |
| `quasar/QuasarReading.java`, `lastlight/LastLightReading.java` | The reading windows |
| `client/film/FilmSynth.java`, `FilmSound.java`, `LastLightFilm.java` | Only so Last Light's soundtrack follows its reading windows |
| `previz/quasar-jets/SOUND-CUES.md` (new) | The cue sheet |

## Decisions

1. **The film's camera looks for a clear view.** Adam's test world and the harness's jungle put the old cameras inside
   trees. Shots 8, 9, 10 and the new bowl shot each try a handful of spots (to either side, then higher) and take the
   first with a clear line to what the shot is about (`JetCamera.clear`, a block ray that counts leaves). Chosen once a
   film, so the camera never jumps.
2. **Shot 10 is two shots now.** 10a, wide from behind the caster, to the pop; under the pop's flash a cut to 10b, over
   the bowl's rim looking down at the dark glass and the crystals growing, then up to the gap in the sky. (Opus's
   first harness run showed the bowl as "a dark gash far off".)
3. **The label on knots that seem faster than light is gone**; in its place, after the landing: "The strongest quasar
   jets carry, every second, about as much energy as the Sun gives off in 100,000 years." There is no calm moment while
   the jet comes in, and this one is about what is on screen.
4. **Reading windows are holds, not whole labels slowed.** A hold sits where nothing is mid-way (the mirror is whole,
   the reticle has locked, the jets have reached full length), so the action keeps its pace. The mirror now finishes
   unfolding at 134 (was 150) to leave such a moment before the fast-forward. Kept free because sounds must end on an
   event there: 56-92, 620-668, **742-800**, **866-900**.
5. **Earth's clock is real**: seconds since the fast-forward began on the real clock, also when educational mode slows
   the film.
6. **The lobe's skin covers a little of what is behind it.** Light only added washed out to a white disc against the
   second sun's day sky. So does the jet's core.
7. **The mark is shown before the second press** (a star is locked, the player looks at the ground): dim, red when it
   is too close, with the distance in the hint card. Range 256 and "lobe + 4 blocks" are copied from `QuasarJets`: if
   Opus changes them there, change `QuasarRender.MARK_RANGE`, `QuasarRender.aim()` and `QuasarReticle.hint()` too.
8. **Where the listeners are**: `QuasarRender` registers its own tick (particles) and camera-angle (shake) listeners
   in a static block (it has no `init`; `QuasarClient` is Opus's). The world shader is registered from
   `QuasarJetsFilm.init`.
9. **If the glow can't be set up on a computer** (`JetPost`: its pictures are 16-bit float textures), the film says
   so once in the log and draws the space scene straight onto the screen as before. Shader packs: the space scene and
   words are drawn over the finished picture and should show; the world effects use our own shader in the level's
   drawing, which a pack may treat differently (Adam's Iris has shaders switched off, read from his `iris.properties`).

## Seen in the game (the harness, 2026-11-16, Antares, educational mode on)

Contact sheets of about 40 story ticks a run were read.
- Run 1 (jungle, the first pass): the glow, the star's surface and corona, the new reticle and the second sun were
  good at once. Wrong: star, mirror and lit quasar burnt out to white; the jets were fat beaded columns; the dial's
  ticks were fat; the dust wave was an opaque brown wall when it passed the camera; the lobe looked like a cracked
  egg; the cameras stood in leaves. All reworked.
- Run 3 (flat world): all of that right. Wrong: the landing fireball was a plain white dome and the jet looked
  beaded. Reworked.
- Run 4 (flat world): the jet comes in with a glowing head and is one clean line; the landing is a white flash, a
  shock ring, then a lumpy orange fireball with debris flying and smoke; the lobe is an orange bubble with lightning
  crawling over it; the bowl shot shows the dark glass and the crystals; night falls; the gap in the sky and "MISSING ·
  back in 3 nights". Nothing left that looked wrong at this size.
- **One crash, not repeated**: run 3 ended at about story tick 920 with Windows exit code 0xC0000005 (an access
  violation in native code), with no Java crash file, no game crash report and no Windows crash record. The same run
  again (run 4) went through to the end, and so did the two after it (run 5: Last Light with educational mode, its
  labels and slowed soundtrack, no error; run 6: Rigel in the jungle, where the cameras now stand over the canopy
  with the jet, the blast, the lobe and the bowl all in view). Medal (the clip recorder) was running, and
  CLAUDE.md has the same kind of crash with Medal on this PC from 2026-11-15. Not shown to be ours; if Adam's game
  ever dies like that during a strike, test with Medal closed first.

## Not checked

- **Nothing was heard.** The soundtracks were only checked by the game's log (made and played without an error, plain
  and slowed). Adam has to listen.
- Only Antares and Rigel were looked at, at 1280 x 720, vanilla drawing (no Sodium or Iris in the dev game).
- The film as other players see it (a second player near the caster) and the world effects without the film.
- `previz/quasar-jets/film-check/shader-check.html` still holds the old shader: it is out of date now (the game is
  the check).

## To test in the game (Adam)

Night, overworld, SS-21 Quasar installed, TA-02 picked on TON 618's item or the Stellar Remote.
1. **Before casting**: the 8 stars (rays, twinkle, a name under each), the reticle and its card when you aim near one,
   "PRESS TO LOCK". After the first press: the card "LOCKED", the hint card above the item's name, the ring on the
   ground where the strike would land (red if too close).
2. **The film**: is the space part good enough now (the star, the mirror, the dial and the two clocks, the flight,
   TON618, the ribbon, the ignition, the jets)?
3. **The landing** (the main thing): is it a big impact now? Flash, fireball, shock wave, debris, the shake.
4. **The lobe, the pop, the bowl with its crystals**, then the gap in the sky.
5. **Educational mode on** (the plate in the H menu): is there time to read every label now? Does any slow moment
   feel wrong? The same for Last Light.
6. **Without the film** (press J for films off, or watch a friend cast it from far away): the new light in the sky,
   the second sun, the jet coming in, the blast, the shake.
7. **Sounds**: still the code-made track and vanilla world sounds until Opus cuts the files.
8. Try a red dwarf (Barnard's Star) and a blue giant (Rigel): sizes and colours.

## Next

- Opus: commit (the list is in `reviews/fable/PROPOSALS.md`), cut the sounds from the cue sheet, put the test jar in
  Adam's instance when he isn't playing.
- After Adam's test: fix what he sees. Numbers to turn: `JetPost.end(0.7, 1.4, ...)` in `JetSpace.draw` (glow,
  exposure); the blast's sizes and times in `QuasarRender.world`; the camera's distances in `JetCamera`; the holds in
  `QuasarReading`; cue volumes in `JetCues`.
- When the sound files are in: listen through once for levels (film cues against the world's sounds), and decide
  whether the code-made drone and pads stay under them.

## For the ports (adds to Part 2's list)

- `JetPost`: an off-screen picture with a 16-bit float texture and four smaller ones, drawn with `drawWithShader`. On
  1.20.1 the same calls exist (`TextureTarget`, `GlStateManager._texImage2D`). On 1.21.11 and 26.3 it becomes pipelines
  and GPU textures like the rest (the scene already goes into a texture of its own there, so the glow passes slot in
  between that and the blit). If it is too much for a port, `JetPost.begin()` returning false gives the plain scene.
- `QuasarGl` / `quasar_world`: one more program, drawn in the level at AFTER_SKY and AFTER_WEATHER with blend
  ONE / ONE_MINUS_SRC_ALPHA, depth test on and off, no depth write: three pipelines on 1.21.11 and 26.3.
- `QuasarRender` uses `ViewportEvent.ComputeCameraAngles` (the shake) and a client tick: Fabric needs a camera mixin
  or drops the shake for players without the film.
- `QuasarReticle` and `JetHud` use RimuLib's `UiApi` as Last Light's `FilmHud` does.

## Round 2 (2026-11-17, from 01:20): the impact at three times the size

Adam: "the impact is still super underwhelming, make it actually destructive". Opus tripled the crater (`blast` 20 / 30
/ 45 / 60, cut by RimuLib's carver over some ticks) and added a blast ring. Fable's part, in progress (resume here if
the PC was shut down): (1) shot 9's camera stood above the clouds: distance now 2 x blast (38 to 150 blocks), height a
fifth of that, never higher than 12 blocks under the cloud layer; the same cap on shots 10a and 10b; (2) the dust
strips capped in height (they stood like fog walls), the shock ring runs to 6 x blast; (3) the lobe is drawn as a
whole ball that sits in the crater (the crater is as deep as the lobe is wide); (4) the ground preview follows
`QuasarJets.groundBelow`; (5) test jar `... TA-02 impact TEST ...`.

**Round 2 finished by Opus (2026-11-17 ~17:00)**: Fable stopped for usage credits after the PC went off mid-round; its
steps 1-4 were all in the code and build. Checked in a harness run (Antares, 110 blocks out): the impact shot sits
under the clouds with the fireball filling the frame (`quasar_0812`), the next shot looks from the caster across the
scorched, burning ground (`quasar_0830`). Test jar `..	est builds6-11-17 TA-02 impact`.
