# Prompt for Claude Fable 5.2: TA-02 · Quasar Jets, Part 2 (the in-game film)

Written 2026-11-16 by the Opus session that runs both of Adam's projects. Run in a Claude Code session on Adam's PC in
`C:\Users\adam_\Documents\TON 618 Addition\ton 618 addition`, model Claude Fable 5.2, effort High. No ponytail (if a
session hook turns it on, turn it off).

---

Hi! You made TA-02 · Quasar Jets' previz and Adam approved v1 ("this is good enough, great even ... no need for a
v2"). The Opus session has now built the skill on `main` (NeoForge 1.21.1, version 1.1.0): the named stars, the
two-press lock-on, the second sun, the jet that carries things, the radio lobe, the bowl and the frozen-light crystals.
The film is still Opus's **placeholder**. **Build the real in-game film, to your previz**, as you did for Last Light.

**Read first**
- `CLAUDE.md`: the top "RESUME HERE" sections, **§7b "For Fable: TA-02's film"** (your files, the contract, the clock,
  what the film can read, and what is NOT the film's), §8 and §0 (the rules). §7b wins over this prompt where they
  differ.
- Your previz and its notes: `previz/quasar-jets/NOTES.md` (shot table with "how it would be drawn in the game",
  sounds, labels, "Adam on v1"), `previz/quasar-jets/HANDOVER.md`, `previz/quasar-jets/quasar-jets-previz.html`.
- How you built Last Light's film: `client/film/` (`LastLightFilm`, `FilmCamera`, `FilmSpace`, `FilmHud`, `FilmGl`,
  `FilmSynth`/`FilmSound`) and `previz/HANDOVER.md`. Reuse your own approach; copy what helps into `client/quasarfilm/`
  (don't change Last Light's files).
- What Opus built: `quasar/` (the server side, `QuasarClock`, `QuasarStars`), `client/quasar/` (the world effects and
  `QuasarCast`), `client/quasarfilm/QuasarJetsFilm.java` (the placeholder you replace).

**What to build**
1. The film to the previz, shot by shot (camera through RimuLib's film tools as in Last Light; the space scene from
   ENGINE 80 to the flood as your own opaque scene; the words, year counter, readouts, title card **TA-02 · QUASAR
   JETS** and the educational labels). The star's type changes colours and sizes as in the previz.
2. **Adam's notes**: keep the labels and captions on screen **a little longer** (at least ~2.4 s each, as Last Light's;
   the film may run a little past 960 ticks only if the world effects still line up: ask first if you need that), and
   frame shot 10 for the **bigger lobe** (radii 9/15/24/33).
3. Leave the world effects to Opus's `QuasarRender` (§7b lists them): from 680 on the real world shows them; frame
   your camera on them rather than drawing your own copies. If one needs to look different, write a proposal.
4. Soundtrack as you planned (made in code like Last Light's, or proposals for `sounds.json`); music M1/M2 stay
   placeholders until the real-music update.

**Rules (as before)**: write only in the film's own files (§7b) and `previz/quasar-jets/`; proposals for anything
else; never refactor other code; **no git** (Opus commits and ports it); nothing of rimuru_dev's or SS-21 Quasar's;
**ask Adam before `runClient` or any game run**. Put the test jar in `C:\Users\adam_\Documents\TON 618
Addition\test builds\<date> TA-02 film\` named `TON 618 Addition 1.1.0 TA-02 film TEST [NeoForge 1.21.1].jar`, with
SS-21 Quasar 1.8.0 beside it (copy `libs/extra/ss-21-quasar-1.8.0-neoforge.jar`), and tell Adam its path and a short
"what to test" list. Adam skims: short replies, answer first, every question as clickable choices.

**Keep notes as you go** in `previz/quasar-jets/HANDOVER.md` ("Part 2: the in-game film"): done, next, decisions,
anything the 1.20.1 and 1.21.11/26.3 ports must do differently. Resumable if your usage runs out.

Done = the film is built and in the test jar, Adam has the path, and the hand-over is written.
