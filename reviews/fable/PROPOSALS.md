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
- The last text ("MISSING") is on for 1.3 s; the ending can be made about 2 s longer without touching the world
  effects, if he wants (his call: the film would run to about 995 ticks).
