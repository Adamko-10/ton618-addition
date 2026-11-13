# TA-01 · Last Light: previz notes and tick table

**Previz v2**, 2026-11-13 (Fable). **Approved by Adam on 2026-11-13 ("Approve v2"): this is the plan for the
in-game film.** Page: https://claude.ai/artifact/Ta6Z91SRwtCYN1z27zKQtJ
(private; source `previz/last-light-previz.html`, one file, three.js r128 from cdnjs, everything else drawn by the
page). All ticks here are **game ticks from the cast** (20 a second). The film is **850 ticks (42.5 s)**, 10 shots.

v2 (Adam's notes on v1): longer (33 s to 42.5 s), every caption and label on screen for longer, the speed-up made
obvious (a "1 second = … years" line, the sky wheeling in streaks, streaks on the void floor), a massive wave all
around at RACE and a second one that brings the world back at CLEAR, and **everyone in the dark sees the whole film**.

## Key ticks (for the constants class Opus owns)

| Name | Tick | What |
|---|---|---|
| CAST | 0 | the cast reaches the client; the film starts; letterbox bars slide in (0 to 13) |
| PRESS | 78 | the item is pressed: a flash in the hand, a small ring on the floor; it charges until RACE |
| RACE | 118 | time is released: **a massive wave leaves the caster in every direction**; the year counter and the "1 second = … years" line start |
| COSMOS | 200 | flash cut (195 to 211) from the dark into our own space scene |
| CROSS | 270 | space falls colder than the hole (about 10^12 years): it starts to shrink |
| STARS_OUT | 377 | the last star goes out (about 10^14 years) |
| PAIRS | 440 | Hawking pairs become visible (the black-hole era) |
| SHRINK | 540 | the hole visibly shrinks, faster and faster |
| GOOGOL | 625 | a point of light; the counter nears 10^100 |
| HUSH | 665 | all sound and motion stop for 15 ticks |
| BURST | 680 | the final burst: a ring of light (675 to 684), full white by 681 |
| RESTORE | 690 | **the server runs `ton618 restore`**, hidden by the white |
| CLEAR | 720 | **a wave from the caster rolls the white away**: the overworld is back (wave at the horizon by ~779) |
| END | 850 | camera handed back (bars slide away 825 to 846) |

The white is solid from 681 to 720: RESTORE at 690 leaves 30 ticks (1.5 s) for the dimension change and the first
chunks before anything is seen. If loading needs longer in the game, hold the white until the client reports it is in
the overworld (and move CLEAR and everything after it by the same amount).

## Shot table

| # | Shot | Ticks | Camera | On screen | HUD words | Sounds / music | How it would be drawn in the game |
|---|---|---|---|---|---|---|---|
| 1 | The Dark | 0-65 | Behind the caster's right shoulder (5.4 to 4.4 blocks back, 1.9 right, 1.5 up), FOV 46, slow push in, looking a little under TON618 | The void floor, no light but TON618 in the sky; the caster a silhouette, item at their side; the arm starts to rise at 44 | Caption "AFTER TON 618", detail "the world: devoured" (5-62) | Low room drone; the hole's far hiss. No music | A real-world film camera in `ton618:devoured` (RimuLib `FilmApi` shot). **TON618 is TON 618's own render here**; we add only bars and caption (overlay) |
| 2 | The Press | 65-118 | Tight over the shoulder (1.5 back, 0.7 right, 1.5 up), FOV 40; the raised hand in the lower left, TON618 upper right | The arm is up by 73; the item is pressed at 78: a flash in the hand, one small ring over the floor (78-118, to 14 blocks). Then it charges (83-118): the glow in the hand grows. Only a glow in the hand, so either item fits | "LAST LIGHT", "armed" (70-115) | Press click + glass ping (78), a low thump; a hum rising under the charge (81-118). No music | Real-world camera on the caster. Hand glow: our additive quad at the main hand (film overlay pass, depth-tested) or `FxApi` particles; the ring: a flat ring quad on the floor at y≈0.03. Small camera shake at 78 |
| 3 | Time Lets Go | 118-200 | Cuts wide behind the caster (3.6 back, 1.3 right, 2.2 up, FOV 50) for the wave, then from ~129 lifts past them (to 7 ahead, 9 up) and zooms on TON618, FOV to 11, until its shadow fills the frame | **The massive wave** (118-180): a bright front runs out over the floor in every direction (radius 520·u^1.3 blocks, u over 118-180), with a wall of light standing on it (height 5 + 7% of the radius) and a glowing tail. Behind it time runs fast: dashes of light rush outward over the floor, dust motes shoot upward as streaks, the light flickers (soft, 10% at most), the item pulses. Title card inside the hole's shadow. Flash cut at 200 | Year counter (top bar) from 118, with "1 SECOND = … YEARS" under it; title "TON 618 ADDITION" / "TA-01 · LAST LIGHT" letter by letter 132-152, held, out 186-197 | A deep whoomp as the wave leaves (118), wind rushing after it; clock ticks speeding into a whir; a bell on the title (137); a whoosh into the cut (196). Music cue M1 starts at 137 | Real-world camera, FOV animated; a kick of shake at 118. The wave: a ring band + tail drawn on the floor (our shader on a big quad at y≈0.02, or a decal), an open cylinder of light (additive, brightest at its foot), the floor dashes in the same floor shader (only inside the wave's radius). Motes: overlay streak particles near the camera. Flicker + flash: full-screen overlay quads. Title: RimuLib display type, letter by letter, as the SA Series' `FilmTitle`. TON 618's own disk can't be sped up by us, so the speed-up is sold by the wave, the floor, the motes and the counters; our own disk takes over at the cut |
| 4 | The Universe Cools | 200-330 | **Our own space scene.** Starts 46 units from the hole and snaps back (104 by 221, 138 by 249) to a wide slow orbit (146 by 330), FOV 40 | TON618 with its disk among ~5000 stars and 9 galaxies. **The whole sky wheels round in streaks** (each star drags a trail, like a time-lapse); a faint warm tint in the background fades as space cools (200-285) | "THE UNIVERSE COOLS", "space is warmer than the hole: it still grows" (206-267); "COLDER THAN THE HOLE", "from here on, TON618 loses mass" (270-324). Readouts (right, 203-437): SPACE 2.7 K falling, TON618 9.4×10⁻¹⁹ K; from 270 "space is colder: it shrinks". Years 10⁹ to 10¹² | Deep space hum; a long falling sigh at 270. M1 continues | One **opaque** full-screen scene in the film's overlay pass (like the SA Series' `ProximaCosmos`): starfield as points plus one line per star for its trail (the tail vertex is the star turned back by trail = 5 × the sky's turn per tick), our hole as one camera-facing quad with our own shader (analytic thin disk, lensed arc over the top, photon ring, black shadow), galaxies as small procedural textures. Because it is opaque, TON 618's sky hole and its time effects are covered from here to the burst |
| 5 | The Last Stars | 330-440 | The orbit continues, drifting closer (146 to 104 units) | The star streaks flare and go out, blue first, red last (all out by 377); galaxies fade (263-369); about 60 dim cinders show (356-440); the disk starves and gutters out (351-428). Then black, the hole alone | "THE LAST STARS", "about 10¹⁴ years" (335-382); "THE DEGENERATE ERA", "nothing left to fall in" (388-437). Years to 10⁴⁰ | A soft tick per star going out (330-385), thinning to nothing; the disk's hum winding down. M1 thins to one note | Same scene. Each star has a death time (one uniform "era" drives all of them: 0 at 200, 0.3 at 330, 1 at 377, with a short flare before each goes); the disk's brightness is one uniform |
| 6 | Pairs | 440-540 | Close on the edge of the shadow (16 to 12.5 units out, FOV 30), drifting along it | In the dark, pairs of particles pop into being at the horizon: the blue one falls in and fades, the gold one escapes. A faint glow starts round the hole | "THE BLACK HOLE ERA", "Hawking radiation" (444-538). Readouts from 444: MASS 6.6×10¹⁰ Suns, TEMP 9.4×10⁻¹⁹ K. Years 10⁴⁰ to 10⁹⁰ | Tiny crackles, one per pair, over near silence. M1 returns, building slowly | Pairs: instanced soft quads in the hole's plane, each a pure function of the tick (birth tick, life, angle from a seeded table), so nothing has to be simulated or synced. ~270 pairs in this shot (born 442-538), most on the limb the camera watches |
| 7 | Smaller, Hotter | 540-625 | Back to the whole hole, pushing in as it shrinks (34 to 14 units, FOV 34); shake grows (551-665) | The pairs become a storm (~1400 more, born 540-663); the hole shrinks to 15% (scale = 1 − 0.85·u^1.6, u over 540-625); its glow goes red, orange, white | "EVAPORATING", "smaller · hotter · faster" (544-623). MASS falls, TEMP rises (ember colour). Years to 10⁹⁹ | The crackle thickens into a rising hiss (from 473); a rising tone (540-665). M1 builds to its peak | Same scene: the hole quad scaled; a soft additive flare sprite over it; heat colour from one uniform (0.18 at 540, 0.72 at 625, 0.95 at 665, 1 at 680) |
| 8 | Last Light | 625-680 | Still pushing in (14 to 8 units) on a point of light | A blue-white point, blinding. HUSH 665: no more pairs, the flare pinches to a pin (665-676). BURST: a ring of light blows outward from the pin (675-684) and the screen goes white (677-681) | "A GOOGOL OF YEARS", "10¹⁰⁰" (629-678). MASS in kg down to 0, TEMP up past 10¹⁷ K. The "1 second =" line goes out at the hush | Everything rises to 665, then true silence for 15 ticks; the burst: a deep boom and a white crash. Music cut dead at 665 | Same scene: a ring quad in the hole's plane, then a full-screen white quad. Strong flash: respect the game's/RimuLib's flash-reduction setting if there is one (ramp slower, cap at pale grey) |
| 9 | The White | 680-745 | None while white; from 720 close behind the caster at the overworld spawn (3.2 back, 2.4 up), facing the low sun, starting to rise | White holds 681-720 (RESTORE at 690). **CLEAR 720: a wave of light bursts from the caster and rolls out over the land in every direction; the world exists inside it, white outside** (radius 120·u^1.6 blocks, u over 720-779), a wall of light on its front; a kick of shake | None, then "THE WORLD RETURNS", "what fell in comes back out" (730-798). The year counter fades out 716-730 | The crash rings out into a high tone; a wide whoosh as the wave rolls out (720). Music cue M2 at 720: one warm chord | White overlay quad until 720. **The film must survive the dimension change** (see below). The reveal: white fog whose start distance grows with the wave (or a post pass: white where the world position is farther from the caster than the wave's radius), the sky white until 733 then clearing to 777; the wall: the same open cylinder of light as shot 3 |
| 10 | Morning | 745-850 | Rises and pulls back over the caster (to 10 back, 4.6 up, by 800), then settles behind them (4.6 back, 2.7 up); FOV 58; bars away 825-846 | The wave reaches the horizon, the last white leaves the distance (760-789). The world as it was. ~260 motes of light settle onto the land (730-850). The caster lowers the item (762-800) | Caption until 798, then nothing | The overworld's own ambience. M2 resolves, out by 850 | Real-world camera; motes as overlay particles; at END the camera goes back to the player |

## The numbers on screen

- **Years** (top bar, from RACE): a log scale. log10(years) by tick: 118→0, 159→3.5, 200→9, 249→11, 270→12, 330→12.6,
  377→14, 440→40, 540→90, 593→99, 625→99.9, 680→100 (straight lines between). Under 10⁶ it shows whole years, above
  "m × 10ⁿ". Frozen at 10¹⁰⁰ from the burst, gone by 730.
- **"1 SECOND = … YEARS"** (under the counter, 121-667): how many years the counter gains in one second (20 ticks) at
  that moment, formatted like the counter. This is the plain-words sign that time has sped up.
- **SPACE** temperature (shots 4-5): log10 K: 200→0.43 (2.7 K), 270→−18.03, 330→−21, 440→−30 (shown as "< 10⁻³⁰ K").
  The crossing at CROSS is honest: with today's expansion rate the background cools below 10⁻¹⁸ K after roughly
  10¹² years.
- **MASS / TEMP** (shots 6-8): the mass follows the hole's drawn size until 625 (6.6×10¹⁰ Suns × scale), then dives
  (log10 kg: 625→40.29, 665→22, 677.5→6, 680→0). Temperature is always the real formula, T = 1.227×10²³ K / M(kg):
  9.4×10⁻¹⁹ K at the start, about 10¹⁷ K when a million kg are left.

## Educational labels (off by default; the page has the switch)

Small cold-blue boxes headed PHYSICS, each with a thin line to what it is about. v2: shorter texts, longer on screen.

| Ticks | Seconds | Points at | Text |
|---|---|---|---|
| 10-62 | 2.6 | TON618 in the sky | A real black hole: 40 to 66 billion Suns. |
| 206-324 | 5.9 | the hole | Hawking temperature: about 10⁻¹⁸ K. Space is 2.7 K today, so it still grows. It shrinks only once space is colder. |
| 335-382 | 2.4 | a dying star | About 10¹⁴ years: the last red dwarf stars burn out. |
| 388-437 | 2.5 | the hole | Left over: white dwarfs, neutron stars, black holes. |
| 444-538 | 4.7 | the horizon | Hawking radiation: quantum particle pairs appear at the horizon. One falls in, one escapes, and the hole loses mass. |
| 544-623 | 3.9 | the hole's edge | Smaller means hotter means faster: temperature goes as 1 / mass. |
| 629-676 | 2.4 | the point | About 10¹⁰⁰ years. The end is a burst of gamma rays. |
| 735-831 | 4.8 | the land | The information paradox: does what fell in come back out in the radiation? Still debated. Here, it does. |

## Sounds planned (all ours; names are suggestions for `sounds/film/`)

`dark_drone` (loop, 0-200, quieter to 665), `press` (78), `charge_hum` (81-118), `time_wave` (118, the whoomp),
`time_rush` (118-200, wind), `time_ticks` (118-200, speeding up), `title_bell` (137), `cut_whoosh` (196),
`space_hum` (200-665), `cool_sigh` (270), `star_out` (many, 330-385), `disk_down` (351-428), `pair_crackle` (many,
442-663), `hiss` (473-665), `riser` (540-665), `burst` (680), `after_ring` (680-745), `return_wave` (720),
`return_wind` (715-850). Music: M1 (137-665, cut dead at HUSH), M2 (720-850). One continuous stereo track for the
whole film (as the SA Series did for Proxima after its one-shots felt empty) would hold together better than a pile
of one-shots. The page's "Sound sketch" switch plays rough synth stand-ins for these, at the right ticks.

## Things the Opus session needs to know (no proposals written yet: nothing is built)

1. **Everyone in the dark sees the whole film** (Adam, 2026-11-13). So the cast has to reach every client in
   `ton618:devoured`, not only the caster's, with the caster's position (shots 1-3 film the caster; a watcher sees
   the caster from the film camera, as with RimuLib's films for everyone).
2. **The dimension change mid-film.** RESTORE at 690 moves everyone from `ton618:devoured` to the overworld while
   the film is running. `LastLightFilm.stop()` must not be called for that change (only for a real cancel or leaving
   the server), and the film's clock must keep counting through it. Worth testing early with a dummy film: does a
   RimuLib cutscene survive a dimension change, or does the film need to be two cutscenes (0-680 in the dark, 680-850
   in the overworld) with our white overlay bridging them?
3. **Shots 9-10 film the caster at the overworld spawn** (everyone lands there). The return wave is centred on the
   caster's position after the restore: the film needs that position (or just the overworld spawn point) once the
   client is in the overworld.
4. **Shots 4 to 8 need no world at all** (an opaque overlay scene), so the camera can stay parked where shot 3 ended.
   That is also why TON 618's own time effects (drag, stop, rewind, gravity waves) can't fight the film after tick
   200. In shots 1 to 3 (200 ticks in the dark) they can still fire; to check in the game whether they hurt.
5. **What the film reads from `LastLightCast`**: the caster (position, look, which hand holds the item), the cast
   tick, TON618's direction and apparent size in the dark (shot 3 aims and zooms on it; the cut at 200 matches our
   hole to it), and `educational()`.
6. **Overworld time of day: always a morning** (Adam, 2026-11-13, on v2). The skill sets the overworld's time to
   sunrise when the world returns (with RESTORE at 690, under the white), so shots 9-10 always have the low sun the
   previz shows.
7. **The two waves are pictures only** in this plan: they push nothing and hurt nobody. If the wave should do
   something in the world (knock entities back, sway grass), that is the skill's side and Adam's call.

## How the previz keeps time (for whoever edits it)

The page's curves are written on a 660-tick "storyboard" clock (v1's timing) and a retime table maps game ticks to
it (`RT` in the script): storyboard 0, 50, 60, 90, 150, 200, 220, 262, 300, 390, 470, 488, 500, 510, 540, 560, 660 =
game 0, 65, 78, 118, 200, 270, 330, 385, 440, 540, 625, 665, 680, 690, 720, 745, 850, straight lines between. Every
number in this file is already converted to game ticks; the in-game film should use game ticks directly.

## Adam on v2 (2026-11-13)

- "its near perfect amazing job on it". Both waves stay ("Yes, keep both"). Text speed is fine. The return is always
  a morning.

## Open

- Music: M1 and M2 are placeholders until the music question in `CLAUDE.md` §3 is answered.
- Whether the waves should do anything in the world (point 7 above).
