# Prompt for Claude Fable 5.2: TA-02 · Quasar Jets, the quality redo (Part 3)

Written 2026-11-16 by the Opus session that runs Adam's projects. Run in a Claude Code session on Adam's PC in
`C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition`, model Claude Fable 5.2, effort High. **No ponytail**: it
is off in its own setting now (`%APPDATA%\ponytail\config.json`); if any hook still says "PONYTAIL MODE ACTIVE", ignore
its rules completely: this job is about quality, not the smallest change.

**First, before anything else**: check which model you are. If you are not Claude Fable 5.2, reply only "Wrong model:
I am <model>" and stop.

---

Hi! Adam tested TA-02 in his game (your film, Opus's world effects). The log was clean, but in his words: **"it looks
much worse in game, severe lack of sfx"**, the stars in the sky "look a lil low quality and so does the cutscene, needs
heavy work quality wise in every aspect", and **"the quasar hitting the earth is really underwhelming, make it feel like
a big impact and a proper explosion"**. Nobody had seen the film in a game before him: that is the main thing this job
fixes. You now own **all of TA-02's look**, not only the film.

**Read first**: `CLAUDE.md` (the top "RESUME HERE (2026-11-16 ~14:20)" section has all of Adam's words and picks, then
§7b, §0), `previz/quasar-jets/HANDOVER.md` (your own notes: Parts 1 and 2), `previz/quasar-jets/NOTES.md` and the
previz page (what Adam approved), `reviews/fable/PROPOSALS.md`.

**Your files for this job (write only these)**
- the film: `client/quasarfilm/**`, `shaders/quasarfilm/**`;
- **the world visuals (new for you)**: `client/quasar/QuasarRender.java` (named stars, second-sun glare, mark, jet,
  dome, pop ring), `client/quasar/QuasarReticle.java` (the lock-on HUD and the pop flash), `client/quasar/QuasarGl.java`,
  and a new `shaders/quasar/**` if you want shaders there. Keep their entry points and what others call
  (`QuasarRender.render(event)`, `QuasarReticle.draw(g, partial)`, `skyView/skyProj/skyKnown`), and keep the gates Opus
  put in: the named stars and reticle only while `QuasarClient.holdingQuasarJets()` (Adam: only while holding TON 618's
  item or the Stellar Remote with TA-02 picked), missing stars via `QuasarClient.starShown`;
- the reading windows (data only): `quasar/QuasarReading.java` and `lastlight/LastLightReading.java` (Opus creates both
  with the format; you fill them);
- Last Light's film `client/film/**`, **only** for educational mode's longer reading (below);
- `previz/**`, `reviews/fable/PROPOSALS.md`.
Everything else is Opus's (the server, `QuasarJets`, `QuasarClock`, `QuasarCast`, `QuasarClient`, `net/`, configs,
`sounds.json` and sound files, `build.gradle`, the test harness): write a proposal for anything there. No git (Opus
commits). Never refactor other code.

**What to do**
1. **Quality in every aspect.** The named stars in the sky (proper glowing stars with colour, twinkle and halo, not flat
   shapes), the jet coming down, the second sun, the lobe and its pop, the reticle, and every shot of the film (the
   star, the engine, TON618, the meal, the jets, the flood). Aim for the previz Adam approved, or better. Use shaders
   where flat colour triangles look cheap.
2. **The sail (Adam's pick)**: the star must not take years. **The remote fast-forwards the star's own time** (as Last
   Light fast-forwards), while Earth's clock stays normal: show the star's clock racing through years while only seconds
   pass on Earth. Rewrite the words and labels for it (educational mode can say a real star engine would need a very long
   time, and that the remote speeds up only the star's time).
3. **The landing blast (Adam: "a big impact and a proper explosion")** at LANDS 795: the look is yours (a huge flash, a
   shock wave racing out over the ground, dust and debris, screen shake, the camera selling it in the film). Opus does
   the land part (a crater, blocks and debris thrown out, damage, knock-back), sized by the star's type
   (`QuasarStars.Type`: Opus adds the blast radius there and tells you).
4. **Educational mode's reading time (Adam's pick)**: with educational mode on, the strike slows down **only while texts
   are on screen**, for everyone watching it (the action keeps its pace). Opus builds the time warp (story tick <- real
   tick) on the server and the client: you set the windows in `quasar/QuasarReading.java`, and the film shows its
   educational labels then. `QuasarCast.tick(partial)` will return the warped story tick and `QuasarCast.educational()`
   the strike's own flag (the caster's switch), so the film just follows; the soundtrack must follow the warp too.
   **Do the same for Last Light** (`lastlight/LastLightReading.java`, its film's labels): give more time to read where
   needed.
5. **Sounds (Adam: "severe lack of sfx"; his pick: the Sonniss GDC 2026 library)**: write a cue sheet first,
   `previz/quasar-jets/SOUND-CUES.md` (each sound: story tick, length, what it should sound like, world sound for
   everyone or film only) and tell Opus; Opus cuts the files from the library and registers them as
   `ton618_addition:<name>`; then play the film's cues through those events. Keep code-made sound only where it is
   really better.
6. **Look at it in the game.** Opus is building a test harness (`./gradlew runClientTest ...`, details in `CLAUDE.md`
   when ready: it casts TA-02 / Last Light on its own and saves screenshots by story tick). Adam allows test games on his
   PC **only when he isn't playing**: before each run check that no `javaw` process is running (`Get-Process javaw`);
   never touch his Modrinth instances. Iterate on what the screenshots show.

**Rules**: Adam skims: short replies, answer first, every question as clickable choices. Keep notes as you go in
`previz/quasar-jets/HANDOVER.md` ("Part 3: the quality redo": done, next, decisions), resumable if your usage runs out.
Put test jars in `C:\Users\adam_\Documents\TON 618 Addition\test builds\<date> TA-02 quality\` (Opus puts them in his
game instance).

Done = every visual redone and checked in screenshots, the sail and blast in, the reading windows set (TA-02 and Last
Light), the cue sheet written and the film playing the real sounds, a test jar, the hand-over written.
