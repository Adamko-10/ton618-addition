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
