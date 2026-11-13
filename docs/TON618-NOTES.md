# What TON 618 1.1.0 does (read 2026-11-13)

Read from its CurseForge page and its NeoForge jar (`libs/ton618-1.1.0-neoforge.jar`, decompiled with Vineflower into
the Opus session's scratch folder, never into this repo). Facts only, in our words: **nothing here is rimuru_dev's code
or assets, and we never copy any.** TON 618 is All Rights Reserved; the page asks people to report only bugs of the
official mods (so bugs our addon causes are ours to handle).

## The mod in short

- Mod id `ton618`, version 1.1.0, by rimuru.dev; needs RimuLib `[1.2.3,2.0)`. One NeoForge jar holds both 1.20.1
  (`mc1201` package) and 1.21.1 (`mc1211`). The classes are obfuscated to one-letter names (`hole/a`, `magic/e`...),
  which will change between releases: **use RimuLib's public API, vanilla data and commands, never its class names.**
- One item, **TON 618** (`ton618:ton_618`, its own creative tab, also in Combat): "a black hole small enough to hold".
  It is RimuLib's `SkillHolder` for its own skill set. Hold it and right-click to cast; shift + right-click picks the
  next skill; **H** opens RimuLib's menu, where TON 618 is its own source (`ton618`, with its own config page).
- One skill set, `ton618:ton_618` (namespace `ton618`), orange (0xFF9A3C), one skill: **TON 618** (id `ton_618`, tier
  "TON 618", title "TON618"), duration 560 ticks, cooldown 1200 ticks (1 s in creative), default key X (88), a Scale
  slider (power 0.1-4, infinite allowed), no speed slider.
- Its own server config `ton618.yml` (enabled, permission node per skill, `ton618.*`, `ton618.admin`, `ton618.madmax`:
  "Madmax" lifts the limits).

## What one cast does

- **No limit on the Scale (infinite)**: a film pulls the camera out past the Earth and the Solar System to TON618 (66
  billion suns), its disk ignites, space warps, then the Sun, the planets and the world fall in. At **tick 416** the
  world is "devoured": every player is moved to the dimension **`ton618:devoured`** (a flat void floor at y=0, no
  light, fixed time), near 0.5/1/0.5, facing TON618, which hangs in the sky there (drawn by TON 618's own client code,
  direction about (0.34, 0.2, -1), ~1000 blocks away). Anyone who joins later is moved there too. "In the dark, time
  does not hold: it drags, stops, rewinds, and waves of gravity pass through you" (its own time effects in that
  dimension). The spell ends at tick 560.
  - **The overworld itself is not changed**: "devoured" is a flag. It lives in vanilla **command storage
    `ton618:ton618`**: `devoured` (true/false) and `at` (game time). Readable with
    `/data get storage ton618:ton618`.
  - **`/ton618 restore`** (operators, level 2) clears the flag and sends everyone in the dark back to the overworld's
    spawn. **`/ton618 status`** says whether the world is devoured (returns 1/0).
  - While devoured, TON 618 can't be cast again ("There is nothing left to devour").
- **A set Scale (finite)**: it lands where you look (radius = Scale × 400 blocks; the hole itself looks about
  radius/30 blocks big, hovering over the land), tears the land up into a bowl (RimuLib `CarveApi.flatten`, down to
  the void at large sizes), erases entities with its own damage types, then **leaves** at tick 484. Only the bowl
  stays; nothing marks that TON 618 was there.
- Only one TON 618 at a time ("TON618 is already here").
- Its films go to everyone if the caster's "films for everyone" is on (RimuLib).

## How it hooks RimuLib (1.2.3)

Mostly RimuLib **internals** (it's rimuru_dev's own library): `SkillSet.register`, `SkillHolder`, its own cast packet
(the client sends skill index + tuning; the server looks the skill up with `SkillSet.byIndex("ton618", i)` and calls the
set's `cast`), `CutsceneDirector`/`Cutscene`/`Shot`/`CameraPose` for the film, `PostFx`/`ShaderWarmup` for its
post-processing, `WhiteSpaceSource.of("ton618")` for its H-menu page, `ConfigPage`, `CarveApi` + `Erasure`.

What that means for us (to verify when building):
- **Adding skills to TON 618's own menu**: `SkillApi.whenRegistered("ton618", set -> SkillApi.extend(set, ...))`, as
  the SA Series does on The Shooting Star's set. TON 618's cast path ends in the set's `cast`, so an extended skill
  should reach our handler. Its config (enabled/permission per skill index) may need checking for our extra index.
- **Knowing TON 618 was used**: `SkillApi.gate(set, ...)` on the `ton618` set sees every cast first (who, where,
  which tuning); plus the vanilla command storage flag for the devoured world.
- **Bringing the world back**: run `ton618 restore` as the server (a public command) instead of touching its classes.
- Its sounds, shaders, textures and film are its own: we make ours in the same spirit, never reuse them.

## Assets it has (names only, for reference)

Sounds (19): press, ascent, reveal, warp, devour, collapse, flash, after, ambient, rewind, slowdown, gwave, glitch,
freeze, hiss, drop, pull, mark, mark_hum. Shaders: a held-hole core shader, fx passes (space, star, composite, world,
mark, rewind, vhs). Space imagery credited to NASA, ESO/S. Brunier (Milky Way, CC BY 4.0) and Solar System Scope (CC BY
4.0).

## Other mods it knows

It also reads another mod's devoured world (`world_magic`, "World Magic", `world_magic:devoured`) and restores it too.
Not ours to touch.
