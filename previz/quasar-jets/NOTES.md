# TA-02 · Quasar Jets: previz notes and tick table

**Previz v1**, 2026-11-15 (Fable). Page: https://claude.ai/artifact/DY9xtGvDFmCVz9jUnLQKVv (private; source
`previz/quasar-jets/quasar-jets-previz.html`, one file, three.js r128 from cdnjs, everything else drawn by the page).
All ticks are **game ticks from the second press** (20 a second). The film is **960 ticks (48 s)**, 10 shots.
**Approved by Adam on 2026-11-15 ("this is good enough, great even ... no need for a v2"): this is the plan for the
in-game film**, with his two notes for the mod version ("Adam on v1" at the end): a bigger explosion, and labels on
screen a little longer.

## Adam's answers before the build (2026-11-15, clickable)

- **Two presses.** Press 1 at a star in the night sky locks it. Press 2 at the ground marks where the jet lands and
  starts the film.
- **The film: the caster and players within about 128 blocks.** Everyone else sees only the world effects.
- **Lobe hurts, jet carries.** The jet only lifts and carries; the lobe shoves and damages (more for bigger stars).
- **8 stars, two per type** (table below).

## Changes from the plan in `CLAUDE.md` §10 (shown to Adam with v1)

- 48 s, not 45: room for the text to sit (his Last Light note: "slow down how fast the text passes").
- Two presses, so shot 1 opens on the ground mark and swings up to the star that is already locked.
- **The second sun lasts as long as the film** (day 680 to 905, night back by 926), where §10 says about a minute.
  Adam, on v1: **"Ends with the film"**.
- In shot 4 the mirror falls in first (it is ahead of the star), then the star is shredded.
- Where the second sun is: **65° to the right of where the caster faces, 8° above the horizon**, so the jet crosses the
  picture low and from the side. In the game: any fixed spot low in the sky works (TON 618's "place"); the jet's line is
  from that spot to the mark.

## Key ticks (for a constants class Opus owns)

| Name | Tick | What |
|---|---|---|
| MARK | 0 | second press: the strike point is marked; the film starts; bars slide in (0 to 10) |
| LOCK | 38 | the camera has swung up to the locked star; the reticle closes on it (28 to 46), "LOCKED" from 40 |
| ENGINE | 80 | flash cut (70 to 92) to our own space scene; the mirror unfolds 92 to 150 |
| PUSH | 150 | the star starts to move; the year counter and "1 second = … years" start |
| FLIGHT | 200 | full speed: the stars streak |
| ARRIVE | 330 | cut to TON618; the mirror peels off and falls in (334 to 356) |
| SHRED | 350 | the star stretches (350 to 386) and unravels into a ribbon that winds into the disc |
| IGNITE | 440 | the disc blazes blue-white (438 to 462); title card 452 to 522 |
| JETS | 530 | two jets leave the poles (heads reach full length at 588) |
| TURN | 610 | the quasar tips toward us (610 to 665) |
| FLOOD | 665 | we look straight down the jet; the screen is blue-white (solid 668 to 684) |
| DAY | 680 | **world: the second sun starts** (night to day 680 to 700), seen as the white clears (684 to 704) |
| SWEEP | 745 | **world: the jet comes in**; it reaches the mark at 795 |
| LOBE | 830 | **world: the lobe swells** (830 to 876); the bowl melts 842 to 890; things are thrown out 846 to 886 |
| POP | 890 | **world: the lobe pops**; the jet is gone by 896; crystals grow 890 to 918 |
| NIGHT | 905 | **world: the second sun fades** (900 to 926); the star is missing from the sky |
| END | 960 | camera handed back (bars slide away 938 to 958) |

## The stars and what the type changes

| Type | Stars (distance, size, mass) | Jet radius | Swept half-width | Lobe radius | Bowl depth | Crystals | Day |
|---|---|---|---|---|---|---|---|
| Red dwarf | Barnard's Star (6 ly, 0.19 Suns wide, 0.16 Suns), Wolf 359 (7.9 ly, 0.14, 0.11) | 0.22 | 1.5 | 6 | 2 | 8 | 80% |
| Sun-like | Alpha Centauri A (4.4 ly, 1.2, 1.1), Tau Ceti (11.9 ly, 0.79, 0.78) | 0.33 | 2.6 | 10 | 3 | 14 | 90% |
| Blue giant | Rigel (860 ly, 79, 21), Spica (250 ly, 7.5, 11) | 0.47 | 4.2 | 16 | 5 | 22 | 100% |
| Red supergiant | Betelgeuse (550 ly, 760, 17), Antares (550 ly, 680, 13) | 0.63 | 6.1 | 22 | 6 | 30 | 100% |

All in blocks; every number is a first guess to tune in the game. "Swept half-width": things within that distance of
the jet's line are picked up. "Day": how bright the second sun's day is. The type also sets the colour of the star,
the mirror's lit face, the ribbon, the jets' sheath, the lobe, the crystals, the captions and the reticle
(`#ff7a4d` red dwarf, `#ffe2a0` Sun-like, `#a8c8ff` blue giant, `#ff9450` red supergiant), the star's size on screen
(0.7 / 1 / 1.6 / 2.3) and the width of the jets in space. The film's timing is the same for all.
(Rigel is strictly a blue *supergiant*; the tier is called "blue giant" as in the plan.)

## Shot table

| # | Shot | Ticks | Camera | On screen | HUD words | Sounds / music | How it would be drawn in the game |
|---|---|---|---|---|---|---|---|
| 1 | Lock-on | 0-80 | Behind the caster's right shoulder (4.4 back, 1.7 right, 2.7 up, FOV 50) on the mark; 18-46 it swings up to the locked star and moves in (2.6 back, 1.3 right, 1.9 up); 52-80 zooms, FOV 50 to 5 | A ring on the ground at the mark (a pulse 0-24, then a steady ring until POP). The night sky with our 8 named stars (bigger, in their colours). A reticle closes on the picked one. Zoom into it, flash in its colour (70-92) | Caption "QUASAR JETS", "strike point marked" (6-36); the star's name, "type · distance away" (42-78); reticle with "ACQUIRING", then the name and "LOCKED" (24-77); readout TYPE / DISTANCE / SIZE / MASS (42-78) | Night ambience. A soft thud and ping for the mark (0). Reticle ticks closing in (24-40), lock chime (40). A rising whoosh into the cut (70-80). No music | Real-world film camera (RimuLib `FilmApi`). The mark: a flat ring quad at the block. **The named stars are ours and must be in the overworld night sky all the time** (not only in the film): 8 camera-facing glow quads at fixed sky directions, drawn after the vanilla sky, hidden by day and rain; the film just zooms on one. Reticle and words: screen overlay, the reticle placed by projecting the star's direction |
| 2 | The Star Engine | 80-200 | **Our own space scene.** Close on the star from its back quarter, pulling back (7.5 to 14 star-scaled units) and drifting round | The star (a lit sphere with slow granules and a glow). A curved mirror, a cap of 62° round the way ahead at 2.5 star radii, unfolds from its centre outward in panels (92-150); its inner face lights up in the star's colour (104-150); a cone of light pours out behind (122-160). At 150 the star starts to slide: the background stars begin to drift | "THE STAR ENGINE", "a mirror to catch its own light" (86-146); "IT MOVES", "all its light now pours out behind it" (152-196); year counter in the top bar from 150 with "1 SECOND = … YEARS" | The star's low roar. Metallic clicks and a shimmer as the panels open (92-150). A deep thump as the push takes hold (150). Music M1 from 150 | One **opaque** full-screen scene in the film's overlay pass, like Last Light's `FilmSpace`: a sphere with our shader, the mirror as a sphere cap with a shader that discards beyond an "open" angle and draws panel lines (bright star-coloured inside, dark outside), an additive cone for the tail. Nothing in the world is needed from 80 to 680 |
| 3 | The Flight | 200-330 | Beside the star (200-262), then swinging round behind and above it to look ahead (262-305) | Mirror ahead, tail behind. Stars stream past as streaks (a tube of ~3200 stars that wraps round, each with a line behind it as long as 7 × the speed), 9 galaxies slide by. Ahead, TON618 grows from a dot: black shadow, a dim orange disc, its galaxy behind it | "ACROSS THE UNIVERSE", "18 billion light years to TON618" (206-262); "TON618", "66 billion Suns" (270-326); years racing to 7 × 10¹¹; readout "TO TON618: … light years" counting down (204-330) | A rushing wind of stars rising with the speed; the engine's hum; it drops away as TON618 fills the view. M1 builds | Same scene. Speed by tick (units a tick): 150→0, 200→1.5, 230→14, 300→60, 322→20, 330→6; the stars' x position is wrapped in the vertex shader from one "travel" uniform, so nothing is simulated. Our hole is the quad from Last Light's film (analytic disc, lensed arc, shadow), dim (brightness 0.45) until it is fed |
| 4 | The Meal | 330-440 | Straight on. Close on the star coming in from the left, then pulling back and across to the whole of TON618 (centred by 432) | The mirror slides ahead, tumbles, shrinks and is gone (334-356). The star stretches (to 4 × long, a quarter as wide, 350-386) and unravels into a ribbon of ~900 glowing motes in its own colour that plunges in, swings round behind the hole and winds into the disc, getting whiter as it goes | "CAUGHT", "the mirror falls in first" (336-384); "TIDAL DISRUPTION", "the star is pulled into a ribbon" (390-436); readout "FED TO TON618: x of y Suns" (352-438) | A long low groan as the star stretches. The mirror cracking away (336-352). A hiss as the ribbon meets the disc. M1 holds one tense note | Same scene. The ribbon: instanced soft quads, each a pure function of the tick (a shared path into the disc, each mote a little later than the one before, the lag growing 350 to 402; then circling at its own radius). Motes behind the shadow are skipped |
| 5 | Ignition | 440-530 | Wide on TON618, drifting back | The disc goes from orange to blinding blue-white (438-462) with a halo; the stars and the galaxy behind fade to a third (440-476). Title card under the hole | "IGNITION", "the brightest thing in the universe" (444-526); title "TON 618 ADDITION" / "TA-02 · QUASAR JETS" letter by letter 454-480, held, out 512-522; readout "BRIGHTNESS: 140 trillion Suns" | A beat of silence, a deep ignition boom (440), a bright steady roar. A bell on the title (455). M1 hits its peak chord at 440 | Same scene: two uniforms on the hole's shader (brightness 0.7 to 3.4, a warm-to-blue colour mix), an additive halo quad **behind** the hole so the shadow stays black. A short pale flash (438-456): respect a flash-reduction setting if there is one |
| 6 | The Jets | 530-610 | Pulls back fast (to 2.5 × as far by 574); the jets still run off the frame | Two narrow cones burst from the poles (heads grow 530-588): a blue-white core with knots racing outward, inside a wider, fainter sheath in the star's colour. A kick of shake at 530 | "THE JETS", "out of both poles, at nearly the speed of light" (534-606); readout "JET SPEED 99.9% of light", "LENGTH" counting to 500,000 light years | A double crack as they launch (530), then a tearing, pulsing beam roar. M1 under it | Same scene: each jet an open cone (additive, brighter where it faces the camera so it reads as a tube with a core; knots = two sine waves scrolling along its length; a "head" uniform cuts it off part-way). The far jet's first stretch is hidden where it is behind the shadow |
| 7 | Blazar Turn | 610-680 | Holds; pushes in a little | The quasar tips toward us: the disc opens from a thin ellipse to a ring (tilt 11.5° to 90°, 610-665), the near jet swings round to point at the camera and brightens (to 4.5 ×), the far one dims and hides. A flare grows over everything (640-668); white 650-668 | "BLAZAR", "one jet swings to face Earth" (614-662) | The roar bends up in pitch as the jet turns toward us. A white crash at 665. Music cut by the crash | Same scene: one tilt value drives the disc's shape in the shader and the jets' axis. Then a full-screen blue-white quad (solid 668-684), under which the film goes back to the world camera |
| 8 | Second Sun | 680-745 | Low behind the caster on the line to the new light (7.2 to 6.2 back, 1.9 to 2.2 up, FOV 52) | The white clears (684-704): the night is gone. A hard blue-white point with a wide glare low over the horizon. Deep blue sky, the land lit cold from that side. The caster turns to it (682-698) and shields their eyes (686-702) | "SECOND SUN", "night turns to day" (692-741) | The crash rings out into a high tone. Morning birds start up. A faint electric hum from the sky. No music | Real-world camera. **A world effect, for everyone in the overworld**: the sky's colour and the world's light lifted toward day for as long as it lasts (a client sky/lightmap override driven by one "day" value, or really moving the time of day and back: Opus's call), plus our glare quad at the quasar's spot in the sky |
| 9 | Swept Along | 745-830 | High to the side of the jet's path (27 along, 27 across, 8 up from the mark), turning with the jet from the horizon to the mark (752-800), then easing in | A thin beam comes down the line from the second sun to the mark: its head is 1500·(1−u)³ blocks from the mark, u over 745-795. Animals, items and loose blocks within the swept width are lifted into it (8 ticks), ride it at 1.7 blocks a tick and circle the mark. A glow where it lands | "SWEPT ALONG", "the jet carries what it touches to the mark" (749-826) | A thin rising whine arriving from far off (745-795), a thump as it lands (795), a steady beam hum. Soft pops as things are lifted. Animal sounds, pitched up | **World effect.** The beam: two additive tubes (core + a halo 3.4 × wider) from the mark toward the quasar's spot, drawn in the world pass for everyone who can see it. Carrying: server side, entities within the swept width of the line get no gravity and are moved along it toward the mark (capped; the previz carries 40 at most), then held circling over it; players are not carried unless Adam wants that |
| 10 | Radio Lobe | 830-960 | Wide from behind the caster (8 back, 3.5 right, 6 up, FOV 58); 922-948 it tilts up to the sky and narrows (FOV 46) | A dome of plasma swells at the mark (radius = the type's, 830-876), bright at its rim, in the star's colour. Everything circling is thrown outward on arcs (846-886) and lands 1.15 to 1.65 lobe radii out. The ground under it sinks into a bowl and turns to dark glass, glowing hot (842-890). At 890 the dome jumps 30% and is gone; a ring runs out to 3.4 × its radius (890-912); a flash. Crystals in the star's colour grow in the bowl (890-918). The second sun fades (900-926). Up in the sky: a gap, and an empty reticle | "RADIO LOBE", "a bubble of plasma where the jet ends" (834-886); "FROZEN LIGHT", "what is left of <star>" (896-926); reticle "<STAR> · MISSING · back in 3 nights" (930-958). The readout LOBE / BOWL / CRYSTALS (834-926) is **for the previz only** | A swelling, crackling drone (830-890). A sharp pop and a ring of wind (890). Glassy chimes as the crystals grow. Night ambience returns. Music M2 from 895, out by 960 | **World effect.** The dome: a half sphere with a rim-light shader (drawn with its own alpha, not purely additive, so it shows against a day sky). Server side: knock entities in the radius outward and damage them (by type); carve the bowl (depth × (1 − (d/R)²), stepped to whole blocks) and replace its surface with a dark glassy block; place the frozen-light crystals (a new light-emitting block in the star's colour). The star stays out of the sky for the set number of nights |

## The numbers on screen

- **Years** (top bar, 150 to 346): log10(years) by tick: 150→0, 200→6, 300→11.5, 330→11.86 (straight lines between).
  It ends near 7 × 10¹¹ years, which is about what a real Shkadov thruster would need to cross 18 billion light years
  (distance grows with time squared: 34,000 light years in the first billion years).
- **"1 SECOND = … YEARS"** (154 to 334), as in Last Light.
- **TO TON618** (204 to 330): 18.2 billion light years × the share of the flight still to go.
- **FED TO TON618** (352 to 438): the star's mass × a ramp from 384 to 450.
- **LENGTH** (534 to 662): 500,000 light years × ((tick − 530)/70)², in steps of 1,000.

## Educational labels (off by default; the page has the switch)

The same cold boxes headed PHYSICS as Last Light's, each with a line to what it is about.

| Ticks | Seconds | Points at | Text |
|---|---|---|---|
| 44-78 | 1.7 | the locked star | By type. Red dwarf: "A red dwarf: small, cool and dim. Most stars are like this, yet none can be seen by eye." Sun-like: "A star like the Sun: yellow-white, about 5,500 °C at the surface." Blue giant: "A blue giant: tens of thousands of times brighter than the Sun. It burns out in millions of years, not billions." Red supergiant: "A red supergiant: hundreds of Suns wide. Where the Sun is, it would swallow Mars." |
| 96-146 | 2.5 | the mirror | A star engine (Shkadov thruster, 1987): a giant mirror held up by the star's own light. With light escaping only one way, the star is pushed the other way. |
| 156-196 | 2.0 | the tail | It is slow. A real one would need a billion years to move a Sun-like star 34,000 light years. The film races time. |
| 274-326 | 2.6 | TON618 | TON 618: a real black hole of 40 to 66 billion Suns, about 18 billion light years away. |
| 392-436 | 2.2 | the ribbon | Tidal disruption: the near side is pulled much harder than the far side. Then by type. Red supergiant: "A puffy supergiant really would be torn apart outside a hole this big." The others: "In truth a hole this big swallows a star this small whole. Smaller holes shred them." |
| 448-526 | 3.9 | the disc | Falling matter rubs itself to millions of degrees. TON 618 shines like 140 trillion Suns. |
| 538-606 | 3.4 | the near jet | Magnetic fields, wound up by the spinning hole, fling matter out of both poles at over 99% of the speed of light. |
| 616-660 | 2.2 | the hole | A quasar seen straight down its jet is a blazar. In 2011 a shredded star's jet pointed at Earth: Swift J1644+57. |
| 694-741 | 2.4 | the second sun | Beaming: light from a jet aimed at you is boosted thousands of times. From its real distance TON 618 is far too faint to see by eye. |
| 752-826 | 3.7 | the beam | Knots in a jet aimed almost at you can seem to move faster than light. It is an illusion of angle. |
| 838-886 | 2.4 | the dome | A radio lobe: where a jet ploughs into gas it blows a bubble of hot plasma that glows in radio waves. Real ones are bigger than galaxies. |
| 898-924 | 1.3 | the crystals | Frozen light is made up. But light has been stopped: in 2013, for one minute, inside a crystal in a lab. |

The first and last are on screen too briefly for their length (1.7 s and 1.3 s): to stretch in v2 if Adam reads them.
The honest notes: red dwarfs really are too faint to see by eye (we draw them anyway); a hole over about 100 million
Suns swallows Sun-like stars whole (only a huge, low-density star is torn apart outside TON 618).

## Sounds planned (all ours; names are suggestions for `sounds/film/`)

`mark` (0), `reticle_ticks` (24-40), `lock` (40), `zoom_whoosh` (70-80), `star_roar` (80-330), `mirror_panels`
(92-150), `push` (150), `star_wind` (150-332, rising with the speed), `stretch_groan` (350-390), `mirror_break`
(336-352), `ribbon_hiss` (360-440), `ignite` (440), `title_bell` (455), `quasar_roar` (441-665), `jets_launch` (530),
`jet_beam` (530-668, pitch bending up 610-665), `flood_crash` (665), `after_ring` (680-745), `beam_whine` (745-795),
`beam_land` (795), `beam_hum` (760-892), `lift_pop` (many, 776-792), `lobe_swell` (830-890), `lobe_pop` (890),
`crystal_chimes` (892-920). Music: M1 (150-665, cut by the crash), M2 (895-960). As Last Light: one continuous track
for the film's space part (80-680) would hold together better than one-shots; the world sounds from 680 on should be
real positioned world sounds (others hear them too). The page's "Sound sketch" switch plays rough synth stand-ins.

## Things the Opus session needs to know (no proposals written: nothing is built)

1. **Two presses.** The skill needs a small state per player: "locked star" after press 1 (looking at one of our
   stars at night, in the overworld, sky visible), cleared by day, by a timeout or by changing skill. Press 2 needs a
   block under the crosshair within some range (the previz's mark is 34 blocks away). The HUD reticle with the star's
   name and type while aiming at the sky is the skill's own HUD, outside the film.
2. **Our named stars live in the overworld night sky all the time**: a small sky layer of ours (8 glow quads at fixed
   directions that turn with the vanilla sky or stay fixed: the previz keeps them fixed). The film's shot 1 and its
   last seconds use the same star direction, which the film needs from the cast.
3. **Who gets what.** The film (space scene, words, camera): the caster and players within about 128 blocks. **The
   second sun: everyone in the overworld.** The beam, the carried things, the lobe, the bowl and the crystals are real
   world things, seen by whoever is near.
4. **World effects are timed from the cast** (DAY 680, SWEEP 745, lands 795, LOBE 830, POP 890, NIGHT 905), also for
   players who get no film. A skipped film changes nothing in the world.
5. **What the film reads from the cast**: the caster (position, look, which hand), the cast tick, the mark's position,
   the star (its index or name, type, sky direction), the quasar's sky direction, `educational()`.
6. **The return from the white (684-704)** is in the same dimension (no dimension change this time).
7. **Flashes**: a strong flood at 665 and short flashes at 80, 440 and 890.
8. **The missing star**: saved with the world (which star, until which night), read by the sky layer.
9. **Different from SS-21 Quasar**, on purpose: no pillar from above, no disc overhead, no galaxy-shaped crater. Ours
   comes in low from the horizon, carries things, ends in a dome, a glassy bowl and crystals.
10. **Still to settle in the game**: whether the jet's beam passes through hills between the horizon and the mark (the
    previz's land is low enough that it never does); what "loose blocks" are (the previz just shows a few blocks
    lying about; in the game probably falling-block entities made from the top layer under the beam, capped).

## How the previz keeps time (for whoever edits it)

Everything is written in game ticks (no retime table this time). Every moving thing is a pure function of the tick, so
the scrubber is exact. `window.__quasarjets` has `seek(tick)`, `play()`, `pause()`, `setStar(index 0-7)`.
Checked in the browser pane on 2026-11-15 at ticks 14, 72, 130, 185, 245, 312, 344, 378, 415, 449, 495, 556, 592, 604,
640, 656, 700, 726, 774, 792, 818, 864, 912, 948 (Betelgeuse), 868 (Barnard's Star) and 596 (Rigel): draws, no console
errors. The last small camera change to shot 8 (lower, further back, so the caster is in the picture) was not
re-checked by eye.

## Adam on v1 (2026-11-15)

His words: "its pretty good, i'd say when we make the mod lets just make the explosion thing bigger is all, this is
good enough, great even, good job on it no need for a v2 but when we make the mod version we gotta slow down the
labels a little so theres a tiny bit more time to read. im telling u all this but opus will handle it, thanks again".
The second sun (clickable): **"Ends with the film"**. Length and text speed: "as i said".

So: **v1 is approved, no v2.** For the mod version:

1. **A bigger explosion.** Read here as the radio lobe and its pop (shot 10): a larger dome for every star type
   (the table's lobe radius 6 / 10 / 16 / 22 blocks, and the bowl and crystals with it), and a stronger pop at 890
   (the ring runs out further, more shake, a brighter flash). If he meant something else, he can say so when he
   sees it in the game.
2. **Labels on screen a little longer.** The educational labels (and the captions with them) need a bit more reading
   time. The shortest now: 44-78 (1.7 s), 898-924 (1.3 s), 156-196 (2.0 s), 392-436 and 616-660 (2.2 s). Either let
   them start earlier and end later where the shot allows, or give those shots a few more ticks (the film may grow a
   little past 960 ticks; Last Light's labels hold 2.4 to 5.9 s and he called that fine).
3. **The second sun ends with the film** (as drawn: day 680 to 905, night back by 926).

## Open

- Whether players in the beam's path are carried too (not asked: Opus's and Adam's call when it is built).
- Music M1 and M2 (comes with the real-music update).
- The numbers in the star-type table are first guesses, to tune in the game (and to grow, see "Adam on v1").
