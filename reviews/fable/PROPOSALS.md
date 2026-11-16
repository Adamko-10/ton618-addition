# Fable's proposals and hand-over notes (for the Opus session; Adam picks)

Fable writes only in the film's own files (CLAUDE.md §7). Anything else it wants is proposed here: what, where, why,
the patch. Nothing in this file has been applied by Fable.

## 2026-11-14: hand-over of the Last Light film (Part 2)

**No change outside the film's files is needed.** The four entry points are kept exactly
(`LastLightFilm.init/start/stop/active`), so `Ton618AdditionClient` works as it is. What was built and how:
`previz/HANDOVER.md`, "PART 2".

To do on your side:
1. **Commit the film** (Fable ran no git commands): everything new under
   `src/main/java/dev/adamko/ton618addition/client/film/` (8 new classes + the rewritten `LastLightFilm.java`),
   `src/main/resources/assets/ton618_addition/shaders/film/` (3 files), `previz/HANDOVER.md` and this file.
2. **CLAUDE.md §7**: the film uses no `sounds.json`, no sound files and no textures (the soundtrack is made by code
   and played through a custom `SoundInstance`), so the `DeferredRegister` note there no longer applies.
3. **CLAUDE.md §3 Q1 (music)**: still open. The track's two music cues (M1, a minor pad from tick 137 to the hush; M2,
   a major pad from tick 720) are placeholders inside `FilmSynth`. If Adam wants real music, the film can play an
   `.ogg` beside its own track (that would need `sounds.json` and a registered `SoundEvent`).

### Proposal P1 (optional, small): tell the film when a cast is cancelled on the server

- **What**: nothing today tells the client if the server drops a running cast (for example the restore failed, or an
  operator ran `/ton618 restore` by hand mid-film). The film then plays on to tick 850 over a world that changed
  early.
- **Where**: `lastlight/LastLight` (server) and `client/Ton618AdditionClient`.
- **Why**: rare, and harmless today (the film is pictures only), so only worth doing if Adam sees it happen.
- **Patch (sketch)**: a second tiny packet `LastLightEnd` sent to the cast's watchers when the server clears a cast
  before `END`; the client handler calls the existing `stop()`.

### Things to tell Adam when he tests (known, not fixed)

- Never seen in a game yet: only the shader (in a browser's WebGL), the soundtrack (rendered on its own) and the
  build were checked on the VM.
- With a shader pack (Iris) the in-world parts (the two waves, the hand's glow) may not show; the space scene, words
  and white are drawn over the finished picture and should.
- TON 618's own effects in the dark (drag, stop, rewind, gravity waves) can still play over the first 200 ticks; from
  the cut into space on they are covered.
- A player who has RimuLib's films switched off, or skips with its film key, sees only the white of the burst.

## 2026-11-15: hand-over of the Last Light polish (next update)

What and why: `previz/HANDOVER.md`, "Polish (2026-11)". In short: the early hand glow, ring and wave were not faint
but painted out by TON 618's post pass in the dark (its floor is invisible, so everything but the players is "sky"
to it); the film now draws there at `AFTER_LEVEL`, with a looser shot 2 and stronger light. Toasts are cleared at
the film's start and held back while it plays (`ToastAddEvent`). **No change outside the film's files was needed on
NeoForge.**

To do on your side:
1. **Commit**: `client/film/FilmWorld.java`, `FilmCamera.java`, `LastLightFilm.java`, `FilmGl.java`,
   `shaders/film/last_light.fsh`, `previz/HANDOVER.md`, `previz/polish-check/` (2 files, mine), this file.
2. **Version**: the test jar is built from `mod_version=1.0.0`; bump it for the update.
3. **CLAUDE.md**: the "faint early glow" item is really "painted out in the dark by TON 618's post pass"; §7's note
   on drawing in the dark could say that in-world film light there must be drawn at `AFTER_LEVEL`.
4. **Ports**: "For the ports" in the hand-over. The Fabric branches need one mixin of yours (below).

### Proposal P2 (needed for the Fabric ports only): a hook after the level render

- **What**: a client mixin on `GameRenderer.renderLevel`, injected right after its call to
  `LevelRenderer.renderLevel(...)`, that calls the film's dark draw (camera, partial tick, the view and projection
  matrices passed to that call).
- **Where**: the Fabric branches' `client/mixin/` and `ton618_addition.fabric.mixins.json` (not film files).
- **Why**: Fabric's `WorldRenderEvents.LAST` fires inside `renderLevel`, before RimuLib's post pass (a RETURN hook
  on the same method), so in the dark TON 618 paints over everything drawn there. NeoForge and Forge have
  `AFTER_LEVEL` at exactly that spot; Fabric API has nothing dependable after it.
- **Patch (sketch)**: `@Inject(method = "renderLevel", at = @At(value = "INVOKE", target =
  "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(...)V", shift = At.Shift.AFTER))` →
  `LastLightFilm.afterLevel(camera, partial, view, projection)`, which draws `FilmWorld`'s dark part; keep
  `WorldRenderEvents.LAST` for the overworld part.

### Things to tell Adam when he tests

- Not seen in a game: checked by reading where TON 618 and RimuLib paint, and in a preview page that draws the
  effects with the film's real shader over Codex's game frame (`previz/polish-check/`).
- Shot 2 (the press) sits a little further back and lower than before, so the hand and the floor are in the picture.
- Toasts on screen when the film starts are cleared (not brought back); ones that arrive during it show after it.

## 2026-11-16: hand-over of TA-02 · Quasar Jets' film (Part 2)

What and how: `previz/quasar-jets/HANDOVER.md`, "Part 2". **No change outside the film's files was needed**: the four
entry points are kept (`QuasarJetsFilm.init/start/stop/active`). Builds; the shader and the soundtrack were checked
on their own; **never seen in a game**. Test jar: `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-16
TA-02 film\TON 618 Addition 1.1.0 TA-02 film TEST [NeoForge 1.21.1].jar` (SS-21 Quasar 1.8.0 beside it).

To do on your side:
1. **Commit** (Fable ran no git): `client/quasarfilm/` (`QuasarJetsFilm.java` rewritten + 7 new: `JetScript`,
   `JetCamera`, `JetSpace`, `JetHud`, `JetGl`, `JetSound`, `JetSynth`), `shaders/quasarfilm/` (3 files),
   `previz/quasar-jets/HANDOVER.md`, `previz/quasar-jets/film-check/shader-check.html` (mine), this file.
2. **CLAUDE.md §7b**: status "built, not seen in a game"; the film uses no `sounds.json`, sound files or textures;
   captions and labels were retimed (the hand-over's decision 1), the key ticks weren't.
3. **Ports**: "For the ports" in the hand-over. No in-world drawing this time, so P2 isn't needed for this film.

### P3 and P4: APPLIED 2026-11-16 (Adam said yes)

Opus built the non-film side (`2cb94f6`: `QuasarJets.sounds()`, `QuasarClient.backOnDay`). Fable did the film side
the same day: `JetSynth` drops its copies of the jet's arrival and hum, the landing, the lobe, the pop and the
crystal chimes (it keeps the drone, the second sun's hum, the soft pickup pops and the music); `JetHud`'s last
reticle says "MISSING · back in N nights" ("back tomorrow night" for 1; "FED TO TON618" when the server keeps no
star out of the sky). Test jar rebuilt in place. **To commit**: `client/quasarfilm/JetSynth.java`, `JetHud.java`,
`previz/quasar-jets/HANDOVER.md`, this file. One thing to hear in the game: whether the vanilla pop is loud enough
from shot 10's camera, which stands 2.3 lobe radii or more from the mark (up to about 76 blocks for a supergiant;
the pop's sounds reach 96 and 128 blocks, so it should be, but quieter with distance).

### Proposal P3 (optional): world sounds for the strike, for everyone

- **What**: the server plays no sounds for the jet arriving (745 to 795), its landing (795), the lobe swelling (830 to
  890), the pop (890) or the crystals growing. Players who get the film hear them in the film's own track
  (unpositioned); everyone else hears nothing.
- **Where**: `quasar/QuasarJets` (server; vanilla sounds at the mark, or our own through `sounds.json` and registered
  `SoundEvent`s).
- **Why**: the previz planned them as real positioned world sounds ("others hear them too").
- **If applied**: tell Fable, and the film's track drops its own copies (`JetSynth`: the cues from tick 745 on), so
  film viewers don't hear them twice.

### Proposal P4 (small): let the film read how long the star is gone

- **What**: a public getter on `client/quasar/QuasarClient` (or `QuasarCast`) for the day the launched star is back
  (`backOnDay[star]`, already sent by `QuasarMissing`).
- **Why**: the film's last reticle now says "MISSING · gone from the sky for a few nights"; with the number it can say
  "back in 3 nights", as the previz did.
- **Patch (sketch)**: `public static long backOnDay(int star) { return backOnDay[star]; }` in `QuasarClient`; the film
  then shows `backOnDay - QuasarSky.day(level.getDayTime())`.

### Things to tell Adam when he tests

- The list: the hand-over's "To test in the game".
- The caster isn't posed in shot 8 (no turn, no hand over the eyes): the film can't pose a player.
- With RimuLib's films off (key J), or after skipping, only the world effects are seen.
- ~~The last text ("MISSING") is on for 1.3 s~~: **Adam picked "2 s longer" (2026-11-16)**, done in the film's files:
  the film's own end is tick 996 (`JetScript.END`), the reticle is on about 2.6 s, `QuasarClock` untouched. The test
  jar was rebuilt in place (same path). **To commit**: `client/quasarfilm/JetScript.java`, `JetHud.java`,
  `JetSynth.java`, `JetSound.java`, `previz/quasar-jets/HANDOVER.md`, this file. **CLAUDE.md §7b** still says "the
  ending could run ~2 s longer, Adam's call": it is done. Keep `QuasarClient`'s `QuasarClock.END + 40` (the film's end
  must stay under it).

## 2026-11-16: hand-over of TA-02's quality redo (Part 3)

What and why: `previz/quasar-jets/HANDOVER.md`, "Part 3". Fable ran no git. Builds; seen in the game through the
harness (six runs, the hand-over says what each showed); **nothing was heard**.

To do on your side:
1. **Commit** (all Fable's):
   - rewritten: `client/quasar/QuasarGl.java`, `QuasarRender.java`, `QuasarReticle.java`;
   - new: `shaders/quasar/` (3 files), `client/quasarfilm/JetPost.java`, `JetCues.java`,
     `shaders/quasarfilm/quasar_post.{json,vsh,fsh}`, `previz/quasar-jets/SOUND-CUES.md`,
     `previz/quasar-jets/redo-check/` (8 pictures, 3.6 MB: contact sheets of the new look, three of your first
     harness shots as "before"; leave the three PNGs out if that is too much for the repo);
   - changed: `client/quasarfilm/` (`JetSpace`, `JetScript`, `JetHud`, `JetCamera`, `JetSynth`, `JetSound`, `JetGl`,
     `QuasarJetsFilm`), `shaders/quasarfilm/quasar_jets.fsh`, `quasar/QuasarReading.java`,
     `lastlight/LastLightReading.java`, `client/film/FilmSynth.java`, `FilmSound.java`, `LastLightFilm.java`,
     `previz/quasar-jets/HANDOVER.md`, this file.
2. **The sounds**: `previz/quasar-jets/SOUND-CUES.md`. World sounds are yours to play (`QuasarJets.sounds`); film
   sounds only need their names in `sounds.json` (the film finds them by name, no registry entry needed).
3. **CLAUDE.md §7b**: the film and the world visuals are both Fable's now; `QuasarRender`, `QuasarReticle`, `QuasarGl`
   and `shaders/quasar/` moved to Fable's list; `QuasarJetsFilm.hasCamera()` is new (public); the film registers three
   shader programs.
4. **Test jar**: `..\test builds\2026-11-16 TA-02 quality\TON 618 Addition 1.1.0 TA-02 quality TEST [NeoForge
   1.21.1].jar` (SS-21 Quasar 1.8.0 beside it). Built from the working tree as it was when the job ended; rebuild
   after you add sounds.

### Things to know (no change needed)

- **The reading windows are set**: TA-02 slows 12 holds (+27 s in all), Last Light 8 (+9 s). Story ticks kept free of
  windows for your timed sounds: 56-92, 620-668, 742-800, 866-900 (TA-02); 60-240 and 660-730 (Last Light).
- `QuasarRender.MARK_RANGE` (256) and the "too close" rule (lobe + 4 blocks) are copies of yours in `QuasarJets`, for
  the mark shown before the second press. Change both sides together.
- `QuasarRender` registers a client tick and a `ViewportEvent.ComputeCameraAngles` listener itself (a static block);
  the world shader is registered from `QuasarJetsFilm.init`. Move them to `QuasarClient.init` if you would rather
  have them there.
- The server marks the launched star missing at the second press, so `QuasarRender` keeps drawing it itself until
  story tick 92 (for the film's zoom).

### Proposal P5 (the harness): say why a press was refused, and aim where the ray can reach

- **What**: on 2026-11-16 one jungle run ended "press 2 at (89.5, 79.5, -6.5): REFUSED" (the server's reason goes to
  the action bar, so the log doesn't have it). The spawn differs from run to run, and the target was a treetop.
- **Patch (sketch)**: log the denial (`QuasarJets.deny` already has the text); pick the target by a ray from the
  player's eye (as the server does) instead of a heightmap column, or step the distance down until a press is taken.
  `-Dton618test.world=flat` always works.

### Proposal P6 (optional): the second sun's light on the land

- **What**: the second sun lifts the sky's light everywhere at once, so the land is lit evenly as at noon, while the
  light in the sky sits low on one side. A warm-to-cold tilt of the light map (or longer shadows) is not something a
  film file can do.
- **Where**: `client/mixin/ClientLevelMixin` / the light map. Only if Adam asks for more from the second sun.

### One crash to keep in mind

Harness run 3 died near story tick 920 with exit code 0xC0000005 and left no crash file of any kind; the same run again
and the two after it were clean. Medal was running (CLAUDE.md, 2026-11-15: the same with Medal's hook). Not shown to
be ours.
