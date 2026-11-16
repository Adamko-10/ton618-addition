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
- **2026-11-16 ~13:30: the ending made 2 s longer** at Adam's pick (decision 1); the test jar was rebuilt in place.
  That change is **not committed** (Opus committed the film before it): `JetScript.java`, `JetHud.java`,
  `JetSynth.java`, `JetSound.java` (a comment), this file, `reviews/fable/PROPOSALS.md`.
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
8. **The reticle at the end** says "MISSING · gone from the sky for a few nights": the client can't read how many
   nights (proposal P4).
9. **The soundtrack** has the world part's sounds too (the jet's whine and landing, the lobe's swell, the pop, the
   crystal chimes), unpositioned, for the players who get the film: the mod plays no world sounds yet (proposal P3).
   M1 (150 to the crash at 665) and M2 (from 895) are placeholder pads, as agreed.
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
5. Sound: a track all the way through; boom at the ignition, crack at the jets, crash at the white, pop at 44.5 s.
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
