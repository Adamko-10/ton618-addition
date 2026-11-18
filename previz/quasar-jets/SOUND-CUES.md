# TA-02 · Quasar Jets: sound cue sheet (Fable, 2026-11-16, for the Opus session)

Adam: "severe lack of sfx"; his pick: the Sonniss GDC 2026 library. This is every sound the strike wants. Opus cuts
each from the library (processed: trimmed, faded, mono for world sounds, stereo for film ones, `.ogg`) and lists it in
`sounds.json` under **exactly these names** (`ton618_addition:<name>`). Nothing else is needed for the film ones: no
registry entry, the film plays them by name.

**The film needs no change when a file arrives.** For each film cue, `client/quasarfilm/JetCues` looks the name up in
`sounds.json` when a film starts. Found: it plays the file. Not found: the film's code-made track keeps its own version
of that cue (as now). So files can land one at a time, in any order.

Ticks are **story ticks** (`QuasarClock`), 20 a second. "Tail" = how long the file may ring on after its main part.
Priority **A** = the strike feels empty without it, **B** = polish.

## Timing rules (educational mode's slow-down)

With the caster's educational mode on, story time runs slower inside the reading windows (`quasar/QuasarReading`).
One-shot sounds are fine (they start at a story tick). Sounds that must **end on** an event are the problem, so:
- there is **no reading window** in story ticks **56 to 92** (the zoom and its cut), **620 to 668** (the jet's turn and
  the flood), **742 to 800** (the jet coming in and its landing) and **866 to 900** (the lobe's last second and its
  pop): cues there run at real speed, always;
- every held sound is a **loop** that is stopped at a story tick (below, "loop"), never a long one-shot.

The windows as set (story ticks, each slowed 3 to 6 times): 42-54, 134-148, 152-162, 290-314, 418-434, 484-504, 590-604,
612-620, 706-738, 806-826, 842-864, 904-926. A slowed strike takes about 77 s where a plain one takes 50 s.

## World sounds (the server plays them at the mark, for everyone in range; `QuasarJets.sounds`)

Mono. Volume is the `playSound` volume (range = 16 x volume blocks).

| P | Name | Story tick | Length | What it should sound like | Volume |
|---|---|---|---|---|---|
| A | `quasar_lock` | press 1 (not on the clock) | 0.5 s | A clean targeting chirp: two rising electronic blips and a soft click. Played to the caster only | 1 |
| A | `quasar_mark` | 0 | 0.8 s | A soft sub thud with a glassy ping on top: a marker dropped on the ground | 2 |
| A | `quasar_sun_ignite` | 680 | 3 s | **To every player in the overworld, at their own position**: a far-off, sky-wide low boom, then a bright airy shimmer rising, as night turns to day. Not harsh | 1 |
| A | `quasar_jet_incoming` | 745 | **2.5 s exactly, peak at its very end** | A tearing roar racing toward you from far away: thin and high at first, growing, dropping in pitch as it arrives (Doppler). It must cut dead at 2.5 s: the impact takes over | 16 |
| A | `quasar_impact` | 795 | 0.3 s hit + 5 s tail | **The big one** (Adam: "a big impact and a proper explosion"). A hard crack first, then a huge deep explosion with real sub-bass, then a long rolling rumble. Bigger than TNT by far. Three files if possible, one per size: `quasar_impact_small` (red dwarf), `quasar_impact` (Sun-like), `quasar_impact_big` (blue giant, red supergiant) | 16 |
| A | `quasar_debris` | 801 | 3.5 s | Rock and dirt raining back down: heavy chunks first, then gravel and dust | 8 |
| A | `quasar_beam_loop` | 795 to 890, **loop** | 2 to 4 s, seamless | The jet standing on the ground: a thick, steady energy beam, a deep hum with a harsh electric fizz on top. Replay while the story tick is under 890 (or loop and stop at 890) | 8 |
| A | `quasar_lobe_loop` | 830 to 890, **loop** | 2 to 4 s, seamless | The lobe swelling: a wide crackling, bubbling plasma drone, like a giant tesla coil under water | 8 |
| A | `quasar_lobe_rise` | 870 | **1.0 s exactly, peak at its end** | A fast rising pressure whine into the pop (a "suck-in") | 8 |
| A | `quasar_pop` | 890 | 0.2 s hit + 3 s tail | The lobe bursting: one sharp thunderclap, a rush of air blowing outward, a sub drop. Clean and round, not another explosion | 16 |
| B | `quasar_crystal` | per crystal, 890 to 918 | 0.6 s | A glassy chime with an ice-crack attack. 3 or 4 variants under one name (random pitch 0.8 to 1.4 by the server) | 1.5 |
| B | `quasar_lift` | per thing picked up, 760 to 826 | 0.3 s | A soft upward "fwip": something light pulled off the ground | 1 |
| B | `quasar_night` | 905 | 2.5 s | The second sun going out: a long soft falling sigh, a little reverb | 4 |

The vanilla sounds `QuasarJets.sounds` plays today can go once these exist.

## Film sounds (only for players who get the film; the film plays them, not positioned)

Stereo. Played on the Players slider like the film's track.

| P | Name | Story tick | Length | What it should sound like |
|---|---|---|---|---|
| A | `quasar_film_reticle` | 24 | 0.8 s | Targeting ticks closing in: 5 or 6 dry electronic ticks, each a little faster and higher |
| A | `quasar_film_lock` | 40 | 0.7 s | Lock confirmed: a two-note chime, clean and techy, a short tail |
| A | `quasar_film_zoom` | 58 | **1.1 s, peak at its end** | A riser: a whoosh that sucks in toward the star, ending in a dull thump (the flash cut at 80) |
| A | `quasar_film_star_loop` | 80 to 334, **loop** | 4 to 6 s, seamless | A star up close: a low rolling fire roar with slow rumbles. Quiet bed |
| A | `quasar_film_mirror` | 92 | 2.9 s | The mirror unfolding: many metal panels sliding and clicking open one after another, with a shimmering metallic ring building under them |
| A | `quasar_film_push` | 150 | 2 s | The fast-forward takes hold: a deep sub thump and a whoosh that pulls away |
| A | `quasar_film_time_loop` | 152 to 330, **loop** | 3 to 5 s, seamless | Time racing: a fast mechanical ticking blurred into a whir (a clock or film reel spun far too fast), with a thin high whine |
| A | `quasar_film_wind_loop` | 200 to 330, **loop** | 4 to 6 s, seamless | Stars streaming past: a wide rushing wind with a hollow tone in it |
| A | `quasar_film_arrive` | 330 | 2.5 s | The cut to TON618: one low, heavy cinematic hit ("braam"), dark, a long tail |
| B | `quasar_film_mirror_break` | 336 | 1 s | The mirror torn off: metal cracking and shearing, pieces falling away |
| A | `quasar_film_stretch` | 350 | 2.2 s | The star pulled long: a huge low groan, bending metal or a whale-like moan sliding down |
| B | `quasar_film_ribbon_loop` | 362 to 440, **loop** | 3 to 4 s, seamless | The ribbon burning into the disc: a hot sizzling hiss |
| A | `quasar_film_ignite` | 440 | 0.3 s hit + 3 s tail | The quasar lights: a beat of silence is already there, then one enormous "whoomf" and a bright rush of fire |
| A | `quasar_film_title` | 455 | 3 s | The title card: a deep bell or a dark piano-and-sub hit, a long clean tail |
| A | `quasar_film_quasar_loop` | 442 to 666, **loop** | 4 to 6 s, seamless | The lit quasar: a bright steady roar, hotter and higher than the star's |
| A | `quasar_film_jets` | 530 | 0.3 s hit + 2 s tail | The jets launch: a double crack (two hits 0.15 s apart), then a tearing blast |
| A | `quasar_film_jet_loop` | 534 to 666, **loop** | 3 to 4 s, seamless | The jets running: a pulsing beam roar, the pulses about 3 a second |
| A | `quasar_film_turn` | **620** | **2.25 s exactly, peak at its end** | The jet swinging to face us: a riser bending up in pitch, getting brighter and harsher to the very end (it must end on the flood at 665) |
| A | `quasar_film_flood` | 665 | 0.3 s hit + 3 s tail | Looking down the jet: a white crash (a bright, wide noise hit) and a high ring left hanging |
| B | `quasar_film_birds` | 700 | 2.2 s | Morning birds starting up, a few, surprised (the night just turned to day). Quiet |
| A | `quasar_film_impact_sub` | 795 | 2.5 s | A sweetener for the landing under the world's sound: only the sub-bass and a short "air sucked out" drop |
| B | `quasar_film_pop_sub` | 890 | 1.5 s | The same for the pop: one round sub hit |

The film's music (M1 from 150 to the crash at 665, M2 from 895) stays the code-made pads until the real-music update.
The film's low drone also stays code-made (it follows the film's clock exactly, slow-downs included).

## What the film does until the files exist

Plays its code-made track as before, now made for the strike's reading windows (held sounds last longer through a
window, struck ones land where their story tick falls). Cue by cue, the track drops its own version when the file's
name is found in `sounds.json` (`JetSynth.FILES` lists the names it checks: the film cues above, plus the world sounds
`quasar_mark` and `quasar_lift`, which the track also has a small stand-in for).

Where it is in the code: `client/quasarfilm/JetCues.java` (the table of film cues, their story ticks and volumes; change
a volume there), `JetSynth.java` (the code-made track), `JetSound.java` (plays it on the real clock).
