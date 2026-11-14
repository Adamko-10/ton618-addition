# Previz hand-over: TA-01 · Last Light (Fable's Part 1)

Kept by the Fable 5.2 session (started 2026-11-13 from Adam's SA Series session). If usage runs out, resume from here.

## Status (2026-11-13, late evening)

- **PART 1 IS DONE: Adam approved previz v2 on 2026-11-13 ("Approve v2").** Nothing more to do here until Part 2.
  Part 2 (the in-game film) starts only when `CLAUDE.md` §7 says "ready" and Adam sends "Do Part 2 of
  prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md"; build it from `previz/NOTES.md` (game ticks).
  - Page (private artifact, same link for every version): https://claude.ai/artifact/Ta6Z91SRwtCYN1z27zKQtJ
  - Source: `previz/last-light-previz.html` (one file; three.js r128 from cdnjs; all art and sounds made by the page).
    To update the artifact from another session, publish that file with the URL above as `url` (read it first).
  - Tick table (game ticks), HUD numbers, educational labels, planned sounds, notes for Opus: `previz/NOTES.md`.
- v2 was checked once in the browser pane (game ticks 124, 134, 235, 683, 732, 752, 800): draws, no console errors.
- For the Opus session: commit `previz\` (Fable ran no git), take the key ticks and the "Things the Opus session
  needs to know" list from `previz/NOTES.md`, and mark `CLAUDE.md` §7 "ready" when the base can host the film.

## Rules I follow (from the task)

- Write only in `previz\` (and `reviews\fable\` for proposals). No git commands that change the repo: the Opus
  session ("TON 618 Addition") handles git, so **`previz\` is uncommitted** until it commits it.
- Nothing of rimuru_dev's or SS-21's: nothing was downloaded; all art is procedural and ours.
- Ask Adam before installing or downloading anything. Ponytail off.

## Adam's feedback

- **v1, 2026-11-13** (his words): "small change, make the cutscene longer and slow down how fast the text passes and
  make it clear and visible that the universe has sped up and make it send a massive wave all around dont overdo it,
  the current one is good BUT just make it better"; parts: "i mean its great aside from what i said"; length:
  **longer, about 40 s**; other players in the dark: **the whole film**.
- What v2 did with it:
  - 660 → **850 ticks (42.5 s)**, by a retime table (below), most of the extra time where text sits.
  - Text slower: captions hold 2.3 to 4.7 s (v1's shortest was 0.9 s); educational labels were shortened and hold
    2.4 to 5.9 s.
  - Speed-up made visible: a "1 SECOND = … YEARS" line under the year counter; in space the whole sky wheels round
    in streaks; in the dark, dashes of light rush out over the floor and the dust motes become streaks.
  - A massive wave, twice (my reading of "send a massive wave all around"; asked Adam whether that is what he
    meant): at RACE (118) a front of light with a wall of light on it runs out over the void to the horizon, and at
    CLEAR (720) a wave from the caster rolls the white away and the overworld is there behind it. Also a ring of
    light at the burst (675-684). The press (78) now charges for 2 s before the wave is released.
  - Everyone sees the whole film: noted for Opus in NOTES.md.

- **v2, 2026-11-13** (his words): "its near perfect amazing job on it". The two waves: **"Yes, keep both"**. Text
  speed: **"Yes, it's fine"**. The return: **"Always a morning"** (the skill sets the overworld's time to sunrise
  when the world comes back: Opus's side, noted in NOTES.md).

## How the page is built (for whoever edits it)

- **Two clocks.** `tick` is game ticks (0-850), shown everywhere on the page. Every curve is written in storyboard
  time (0-660, v1's timing); `RT` is the retime table, `toSb(g)` / `toGame(t)` convert. `SHOTS` and `KEYROWS` are in
  game ticks; `CAPS`, `EDU`, the audio `CUES` and all the drawing code are in storyboard time. To retime the film,
  change `RT`'s game column, then `SHOTS`, `KEYROWS` and the tick numbers inside their texts, then NOTES.md.
- `draw(g)` picks one of three scenes by storyboard time (`drawDark` t<150, `drawCosmos` t<504, `drawWorld` after),
  then `drawHud` and `drawEdu`. Everything is a pure function of the tick, so the scrubber is exact.
- The hole is one camera-facing quad with our own fragment shader (`holeFrag`); pairs, motes and stars are points;
  star trails and mote streaks are line segments; both waves use `makeWall` (an open cylinder of light); the dark's
  wave band and floor dashes are in the floor shader; the overworld's reveal is white fog whose near/far follow the
  wave's radius.
- `window.__lastlight.seek(gameTick)` is there for checking frames from the console.
- Sound sketch: WebAudio, `audioInit` / `audioFrame` / `CUES`; only runs when the switch is on.

## Decisions so far (mine, open to Adam's notes)

- 850 ticks, 10 shots. Key ticks: PRESS 78, RACE 118, COSMOS 200, CROSS 270, STARS_OUT 377, PAIRS 440, SHRINK 540,
  GOOGOL 625, HUSH 665, BURST 680, RESTORE 690, CLEAR 720, END 850.
- The film leaves the dark at tick 200 for **our own opaque space scene** (like the SA Series' ProximaCosmos), so TON
  618's own sky hole and its time effects never have to be hidden or fought: the shrinking TON618 is ours.
- RESTORE (the server's `ton618 restore`) sits 10 ticks into a 40-tick solid white, so the teleport is never seen.
- Shot 2 shows only a glow in the raised hand: fits TON 618's item and the Stellar Remote.
- Year counter on a log scale; mass and temperature follow T = 1.227×10²³ K / M(kg).
- The space-cools-below-the-hole moment is put at about 10¹² years (my estimate from today's expansion rate), before
  the last stars die at 10¹⁴.
- The waves are pictures only (they do nothing to the world) unless Adam wants them to.

## Open points raised for Opus / Adam (also in NOTES.md)

- Does a RimuLib cutscene survive the dimension change at RESTORE, or must the film be two cutscenes bridged by our
  white? (No proposal written yet: nothing is built.)
- The cast must reach every client in the dark (whole film for everyone).
- Whether the return is always a morning; music (CLAUDE.md §3 Q4).

---

# PART 2 (the in-game film): BUILT, waiting for Adam's first test in a game (2026-11-14)

Started 2026-11-14 on the Opus session's message (Adam: "yes send it"; CLAUDE.md §7 READY, base 0.1.0 `a786dc6`).
Adam, same day: "alright finish it up".

## Where it stands

- **Built**: `./gradlew --no-daemon build` passes (JDK 21). Test jar for Adam:
  `C:\Users\User\Desktop\test builds\2026-11-14 TON 618 film\TON 618 Addition 0.1.0 film [NeoForge 1.21.1].jar`.
- A copy of the jar is in `C:/Users/User/Downloads/` (Adam wants test jars there on the VM).
- **Never run in a game.** The VM has no OpenGL (`runClient` fails at "Failed to find any valid GLFW profile"), so
  the first real look is Adam's test. **Nothing committed** (Fable runs no git; the Opus session commits).
- Checked on the VM:
  - the shader: `last_light.vsh/.fsh` compile and link as GLSL ES 3.00 in the browser's WebGL2 (stricter about types
    than the game's GLSL 150), and the hole material draws as in the previz;
  - the soundtrack: `FilmSynth.render()` run on its own (0.75 s to make; peak about 24000 of 32767; the hush silent);
  - not checked: the game's own shader loader with the `shaders/film/` remap, every draw call, the camera, the HUD's
    layout, the sound through the game's engine, the dimension change.
- Files (all in the film's own places, CLAUDE.md §7):
  - `src/main/java/dev/adamko/ton618addition/client/film/`: `LastLightFilm` (the four entry points + the controller),
    `FilmScript` (timeline, numbers, captions, labels; storyboard clock + retime table as in the previz), `FilmCamera`
    (one RimuLib cutscene on the cast's clock: dark shots 1-3, parked under space, overworld shots 9-10),
    `FilmWorld` (in-world: hand glow, press ring, the time wave + wall + floor dashes + dust in the dark; the
    returning wave as a growing white shell + wall + settling motes in the overworld), `FilmSpace` (the opaque space
    scene, storyboard 150-504 = game 200-684), `FilmHud` (bars, captions, year counter, "1 SECOND = ... YEARS",
    readouts, title, educational labels, flicker/flash/white), `FilmSound` (plays the track through the game's sound
    engine, Players slider, restarts after the dimension change), `FilmSynth` (makes the 42.5 s stereo track in code;
    no sound files), `FilmGl` (the one shader + draw helpers).
  - `src/main/resources/assets/ton618_addition/shaders/film/last_light.{json,vsh,fsh}` (loaded from `shaders/film/`
    by remapping the game's `shaders/core/` lookup in `FilmGl.register`).
  - No textures, no `sounds.json`, no sound files were needed.
  - `reviews/fable/PROPOSALS.md`: the hand-over to Opus (nothing to wire; one optional proposal).

## Next

1. Adam tests the jar (what to test: below). Fix what he reports, in the film's files only.
2. If the pictures don't draw at all: look in his `logs/latest.log` for "The Last Light film's shader didn't load"
   (the JSON/loader path is the untested part) or "The Last Light film stopped drawing".
3. After the film works: music (CLAUDE.md §3 Q1), then the "After the film" review in the prompt
   (`reviews\fable\FINDINGS.md`).

## What to test (told to Adam)

Cast Last Light in TON 618's dark with films on. Watch for: the camera behind the caster, the glow in the hand, the
big wave at tick 118, the title, the cut into space, stars streaking and going out, the pairs, the shrinking, the
hush and the burst, no "loading terrain" screen showing through the white, the world coming back inside a growing
wave, the morning. Sound all the way through (Players slider). Educational labels with the setting on. A second
player watching. The film key to skip.

## How to check things without a game (the VM can't run one)

- GLSL: generate a page that inlines the two shader files with `#version 150` replaced by `#version 300 es` +
  `precision highp float;`, compile them in a WebGL2 context, open it in the browser pane and read the logs (a page in
  `previz\` works; delete it after).
- Soundtrack: compile a small `main` in package `dev.adamko.ton618addition.client.film` against
  `build/classes/java/main` that calls `FilmSynth.render()` and prints per-second levels or writes a WAV.

## Design decisions (Part 2)

- Camera: RimuLib `FilmApi.film(...)` with `.clock(cast::tick).hardCut().fullFrame().priority(2)`, one shot 0-836;
  RimuLib blends back to the player by 850. RimuLib's director is not cleared on a dimension change (read in its
  1.2.3 jar), so one cutscene should survive RESTORE; if the director drops it, `LastLightFilm.tick` hands it back.
- Films off in RimuLib, or skipped with its film key: only the white of the burst is drawn (677-735).
- The "loading terrain" screen during the trip back is covered white (`ScreenEvent.Render.Post`).
- The space scene and the HUD draw in `RenderGuiEvent.Post` (and in `Pre` if another mod cancels the HUD), so they
  sit above TON 618's own post effects. In-world effects draw at `RenderLevelStageEvent.AFTER_WEATHER`.
- Dark shots are placed along the way to TON618 (not the caster's facing); overworld shots face east (the sunrise).
- The returning wave is a white shell round the caster that grows (drawn with the world's depth: what is nearer
  than the shell is the world, back), not fog.
- Soundtrack = code-made PCM through a custom `SoundInstance` (`resolve` + `getStream` overridden),
  `SoundSource.PLAYERS`. The game stops all sounds on the dimension change; the controller restarts the track at the
  clock's position.
- At PRESS the caster's arm swings on the client only (`swing(hand, false)`, nothing sent). The arm is not raised as
  in the previz (no pose control from the film): the glow sits at the caster's side.
- Stars are drawn a little larger than the previz's points (quads under about three pixels flicker); 3200 stars
  instead of 5200.
- Input is not held during the film (as in the SA Series' films).

## Known risks to watch in the first test

- Shader packs (Iris) may hide the in-world parts; "Fabulous" graphics untested.
- TON 618's own time effects can still play over the first 200 ticks in the dark.
- The HUD's sizes and positions were laid out from the previz's numbers but never seen in the game's lettering.
- The soundtrack's balance was only measured, never heard.
