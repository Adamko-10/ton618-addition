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
