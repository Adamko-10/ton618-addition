# Prompt: an addon for TON 618 (for a new Claude session)

The brief for the TON 618 addon's Claude Code session (Opus 5.5, xhigh effort at Adam's request, ponytail **off**: this
is creative design work). Written 2026-11-13 from Adam's own words, which are kept in full at the end. Updated the same
day: **Fable 5.2 makes the previz and the animation** (item 4), and this folder is the project folder (item 5).

---

Hi! I'm Adam (adamko_10). I make **The Shooting Star Addition: SA Series**, an unofficial addon for rimuru_dev's
**The Shooting Star [Demo]** Minecraft mod. Now I want an addon for rimuru_dev's newer mod, **TON 618**.

## What I want from you

1. **Brainstorm with me what the addon should add.** Give me ideas, and ask me to pick (clickable choices, see "How I
   work" below). Don't start building until I've picked.
2. **Write this skill down; it's the main idea.** It's a new skill that can only be used **after TON 618 has been used**
   (in that world).
   - My words: "you know how black holes slowly disappear because tiny particles of dark matter go apart and smash
     back together extremely fast and it disintegrates a tiny amount of matter in physics, lets make it so when ton
     618 is used you can use the remote to fast forward time so extremely fast that it disintegrates the black hole".
   - **The real physics is Hawking radiation.** Near a black hole's event horizon, pairs of particles keep appearing
     and annihilating. Sometimes one falls in and the other escapes, so the black hole slowly loses mass and
     "evaporates". (They're quantum particle pairs, not dark matter: the in-game text should use the right physics.)
   - Good facts for the film and the texts:
     - the smaller a black hole gets, the hotter it is and the faster it evaporates;
     - the very end is a sudden bright burst;
     - for a black hole as heavy as TON 618 (about 66 billion Suns), evaporating takes around 10^100 years, a googol
       of years.
     - So "fast-forwarding time" until it's gone is a fitting fantasy.
   - Open question for me: does "the remote" mean The Shooting Star's Stellar Remote (then the addon also needs The
     Shooting Star installed), or TON 618's own item and its skill menu (H)? Maybe both. Ask me.
3. **An educational mode.** A setting that adds small bits of text in the cutscene pointing at the real physics
   ("like the dark matter thing", i.e. what's really happening, such as Hawking radiation), or at least a little text
   mentioning it. My words: "an educational mode setting that just adds little text poiting towards something in
   physics".
4. **Fable 5.2 makes the previz AND the animation (the in-game cutscene). You don't make either.**
   - My words (2026-11-13): "keep in mind animation and previz will be made by fable".
   - Fable's prompt is `prompts\FABLE-PREVIZ-AND-FILM-PROMPT.md` in this folder; I run it myself in a separate Fable
     5.2 session. Part 1 (the previz: a browser mock-up of the film, shot by shot, with timings and planned sounds,
     published as an artifact I can watch) can start right away, before there is any code. Part 2 (the in-game film,
     built with RimuLib's film tools and our own shaders to match the previz) starts once I've approved the previz
     and you've made the mod ready for it.
   - **Your side of the film**: give it a clear place and say so in your CLAUDE.md, under a heading "For Fable: the
     film". That means: the one package (and shader/texture folders) that are the film's own files, how the film is
     started and stopped, the skill's clock (its ticks: when the cast happens, when the burst lands), what the film can
     read (the caster, the black hole's position and size), and how the educational-mode setting reaches it. Fable
     writes only in those files; anything else it needs, it writes as a proposal to `reviews\fable\PROPOSALS.md` for
     you to apply (if I pick it).
   - After the film, Fable may oversee the code and tell us about bugs it sees, **without changing any code** outside
     its film files: I don't want it refactoring everything. Its findings go to `reviews\fable\FINDINGS.md`.
   - You (Opus) build everything else: the mod itself, the skills and what they do to the world, configs, menus,
     networking, sounds wiring, and you apply the Fable proposals I pick.
5. **This folder is your project folder**: `C:\Users\User\Desktop\ton 618 addition` on my VM. Write your own
   CLAUDE.md here (a handoff file like the SA Series one: what the addon is, decisions, what's built, what's open; keep
   it updated at the end of every turn and commit it on its own). Make it a git repository (commit as
   `Adamko-10 <Adamko-10@users.noreply.github.com>`); no GitHub repo, no pushing, until I say so.

## Context

- **TON 618** (CurseForge project 1798822, https://www.curseforge.com/minecraft/mc-mods/ton618), by **rimuru_dev**,
  the same developer as The Shooting Star.
  - Released 2026-11-11, version 1.1.0, for Fabric 1.20.1 / 1.21.1 / 1.21.11 / 26.3, Forge 1.20.1 / 1.21.1 and
    NeoForge 1.20.1 / 1.21.1. It needs **RimuLib 1.2.3+** (rimuru_dev's library for all his mods; Fabric API on
    Fabric). It's All Rights Reserved.
  - "A black hole that fits in your palm": an ultramassive black hole is released beside the Solar System and eats the
    Sun, planets, stars and galaxies; damage to the world is permanent.
  - You take TON 618 from its creative tab, press H for the skill menu, then hold it and right-click. Heavy
    post-processing shaders, full-screen flashing.
  - Read its page (and its jar, decompiled into a scratch folder, never into a repo) before brainstorming.
- **Don't mix it up with "The TON618 demo"** (https://www.curseforge.com/minecraft/mc-addons/the-ton618-demo), a
  different, older Shooting Star addon by another author, whose button opens a menu and video screen.
- **My SA Series addon** (the model to follow): https://github.com/Adamko-10/the-shooting-star-addition (public).
  Its `CLAUDE.md` is the full handoff, and `docs/` has the designs.
  - It joins rimuru_dev's mods through **RimuLib's public API** (`cyou.rimuru.rimulib.api` / `.client.api`; docs at
    https://docs.rimuru.cyou/rimulib/). For example `SkillApi.whenRegistered` + `SkillApi.extend` add skills to a
    mod's skill menu, and `SkillApi.gate` sees every cast first, which could tell you that TON 618 was used.
  - Use the public API wherever it has the thing: rimuru_dev's mods are obfuscated, with new names every release.
  - Look at how SA Series builds its films (RimuLib `Cutscene`/`Shot`, our own shaders, title cards), its plates in
    the H menu, its music and its configs.
  - My SA-04 Proxima film started from a previz like this one.
- **rimuru_dev's terms** (for The Shooting Star; assume the same for TON 618 until he says otherwise):
  - addons are fine;
  - **no Bedrock ports**;
  - **never use his assets** (textures, shaders, sounds, models, code). Make our own in the same style.
  - Credit TON 618 and RimuLib as required dependencies. NASA, ESO and Solar System Scope imagery is free to use with
    credit (Solar System Scope: CC BY 4.0) if we need space textures: download it from the source, never copy his
    files.
- Ideas I already have for the SA Series that touch TON 618 (in its `docs/NEXT-SKILLS.md`): TON618 as a delayed
  finale of Stellar Convergence, and an educational-mode setting for the SA Series films too. Keep the two addons'
  ideas apart, but tell me if one idea fits the other.

## The VM you run on (checked 2026-11-13)

- Windows 11; PowerShell and Git Bash. **JDK 21** at `C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot`
  (enough for NeoForge 1.21.1). **Not installed**: Node, ffmpeg, JDK 25 (the Fabric 26.3/1.21.11 builds need 25; later,
  ports can build on GitHub Actions like the SA Series does). `gh` is signed in as Adamko-10. Ask me before installing
  anything.
- **The SA Series repo** is at `C:\Users\User\Desktop\the shooting star addition on vm` (a full clone with history):
  read it as the model, never edit it. Its `libs/` has The Shooting Star's NeoForge jars and RimuLib 1.2.2 (git-ignored,
  never copy them into a commit). Its `CLAUDE.md` is long: read the parts you need (§0 rules, §4 building, §5 how it
  attaches, §16-17a RimuLib).
- **Jars you'll need**: TON 618 1.1.0 (CurseForge project 1798822, slug `ton618`; file ids from
  `https://api.cfwidget.com/minecraft/mc-mods/ton618`) and RimuLib 1.2.3 (NeoForge file 9562438, Forge 9562444, Fabric
  9562450). Download URL pattern (cursemaven):
  `https://www.cursemaven.com/curse/maven/<slug>-<project id>/<file id>/<slug>-<project id>-<file id>.jar`. **Ask me
  before downloading** (file, source, size). Keep them in `libs/` (git-ignored) and decompile only into a scratch
  folder, never into the repo.
- **Where I play**: on my own PC (Modrinth profile, NeoForge 1.21.1), not on the VM. Put test jars in
  `C:\Users\User\Desktop\test builds\<date> <what>\` and tell me the path; I copy them over.
- My personal rules are in `C:\Users\User\.claude\CLAUDE.md` (loaded in every session): follow them.

## How I work (my rules)

- Keep replies short: I skim. Ask every open question as clickable choices, and re-ask anything I left unanswered.
- After coding, a clear list of what I need to do; for testing, a list of what to test.
- I'm budget-conscious: say what a big step costs (time, usage) before it.
- New features go on one version first (NeoForge 1.21.1, which I play on). I test and approve them before they're
  ported to other loaders and versions.
- Never push, publish or upload anything without asking me.
- Fable makes the previz and the film (in the film's own files only); for everything else it only reviews and
  proposes (to files), and Opus applies what I pick. Subagents only when I ask.
- Tell me when to switch ponytail: off for creative work like this brainstorm and the film, lite for fixes, ports and
  releases.

## My original message (word for word)

> https://www.curseforge.com/minecraft/mc-mods/ton618
> make an addon for this mod and heres some extra context
> https://github.com/Adamko-10/the-shooting-star-addition
> (the claude md)
> i am the maker of shooting star addition
> essentially just help me brainstorm what to add
> also write this down too, a new skill that can only be used AFTER ton 618 is used, you know how black holes slowly
> disappear because tiny particles of dark matter go apart and smash back together extremely fast and it
> disintegrates a tiny amount of matter in physics, lets make it so when ton 618 is used you can use the remote to
> fast forward time so extremely fast that it disintegrates the black hole
> i also want either an educational mode as described here or some kind of slight text mentioning it
> however i do want fable 5.2 to handle making the cutscene alright? the previz and then maybe overseeing code and
> telling us about bugs it sees but without changing the code since i dont wanna have it refactor everything
> write this down, an educational mode setting that just adds little text poiting towards something in physics (like
> the dark matter thing)
> also make your own project folder and write your own claude md there plese

And later: "we want to use fable 5.2 to make the animation and the previz for the ton 618 addon".
