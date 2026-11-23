# Codex: test TON 618 Addition's six ports in the real game (before 1.1.2)

You are testing a Minecraft mod for Adam (adamko_10) on **his own PC**, in a real game. **Test and report only: don't
change the mod.** A Claude session fixes what you find. You did the 1.0.0 test on this PC (2026-11-15): your test
folder, your Prism instances and your old `REPORT.md` are still in `C:\Users\adam_\Documents\TON 618 Addition\codex
tests\`. Reuse them.

## Adam's rules (follow them exactly)

- **Never change his own Minecraft setup**: his Modrinth App profiles (`D:\ModrinthApp\...`): never move, rename or
  delete anything there, never launch them, never write there. You may **copy** a jar *from* them (the list below).
  Work only in `C:\Users\adam_\Documents\TON 618 Addition\codex tests\`.
- **Ask before** you install or download anything (say the file, where from and its size, in one list) or delete
  anything. Never delete his files: a mod that must go is renamed to `.disabled`.
- **Don't start a game while he is playing** (check `Get-Process javaw`). You drive the mouse and keyboard while a test
  runs: before each run, tell him and wait for his "go".
- Never enter his passwords; he signs in himself if a launcher asks. Never upload, post, publish or push anything.
- Talk short and plain, the answer first; ask as short numbered choices. Say what a step costs in time before a long
  one. If time runs short, finish the **priority** rows and report.

## What changed (what this test is for)

TON 618 Addition (mod id `ton618_addition`, an addon for rimuru_dev's **TON 618**, needs **RimuLib**) has two skills:
**TA-01 · Last Light** (in TON 618's dark: a ~42 s film, then the world comes back on a morning) and **TA-02 · Quasar
Jets** (needs **SS-21 Quasar** + **The Shooting Star**: at night, launch a named star into TON 618; a ~48 s film and a
jet that blasts a crater; without SS-21 Quasar it shows as **locked**). Its NeoForge 1.21.1 build is released and was
tested; **the six other builds were never run in a game.** Two fixes went into them on 2026-11-23:

1. **Default keys**: the skills' keys never existed. Now: Last Light `;` (TON 618's item) and `'` (the Stellar Remote),
   Quasar Jets `,` (TON 618's item) and `.` (the Stellar Remote). A key picks the skill (the server still refuses with
   its reason).
2. **The action bar in TON 618's dark**: TON 618 hides the whole HUD in its dark, action-bar messages too, so every "you
   can't cast this here" reason was invisible. Now the action bar shows in the dark (the hotbar and chat stay hidden:
   TON 618 wants that). **Fabric 26.3 got only fix 1**: TON 618 never hides the HUD on 26.3.

## The six jars

In `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-23 fixes into the ports\`, named `TON 618 Addition
1.1.1 dark HUD and keys fix TEST [<Loader> <MC>].jar` (the version inside says 1.1.1; it will ship as 1.1.2). In each of
your Prism instances, rename the old `TON 618 Addition 1.0.0 …jar` to `.disabled` and copy the new one in.

| Prism instance | Jar | Priority |
|----------------|-----|----------|
| `01-Fabric-26.3` | `[Fabric 26.3]` | **yes** (its drawing code is its own; TA-02 never drawn) |
| `02-Fabric-1.21.11` | `[Fabric 1.21.11]` | **yes** (same) |
| `05-Forge-1.21.1` | `[Forge 1.21.1]` | **yes** |
| `03-Forge-1.20.1` | `[Forge 1.20.1]` | **yes** (its action bar goes through Forge's own HUD code) |
| `07-Fabric-1.20.1` | `[Fabric 1.20.1]` | (its action bar is copied vanilla code) |
| `06-Fabric-1.21.1` | `[Fabric 1.21.1]` | |
| `04-NeoForge-1.20.1` | the same `[Forge 1.20.1]` jar | load check + fix 2 only |

**Dependencies** (TON 618 1.1.0 and RimuLib 1.2.3 are already in your instances from 1.0.0; keep them). For TA-02 add
**The Shooting Star** and **SS-21 Quasar**, copied from Adam's instances (no download needed):
- Forge 1.21.1 and Forge 1.20.1: `D:\ModrinthApp\profiles\Forge 1.21.1\mods\the-shooting-star-demo-1.6.0-forge.jar` and
  `...\ss-21-quasar-1.8.0-forge.jar` (both may hold several Minecraft versions inside), and from the same folder
  `rimulib-1.3.0-forge.jar` (The Shooting Star 1.6.0 came with RimuLib 1.3.0; rename the instance's RimuLib 1.2.3 to
  `.disabled`). This also tests the newest RimuLib, what players have.
- Fabric 1.21.11: `D:\ModrinthApp\profiles\Fabric 1.21.11 (1)\mods\the-shooting-star-demo-1.5.2-fabric.jar` and
  `ss-21-quasar-1.8.0+mc1.21.11.jar` (**not** the multi-version SS-21 jar: it crashed 1.21.11 before).
- Fabric 26.3, 1.21.1, 1.20.1: `D:\ModrinthApp\profiles\Fabric 26.3\mods\the-shooting-star-demo-1.5.2-fabric.jar` and
  `ss-21-quasar-1.8.0.jar` (multi-version).
- If one of them doesn't load on a version (a crash or a missing-dependency screen naming it), take it out again
  (`.disabled`), say so, and test TA-02 there as **locked**. Don't install shader packs or other mods.

## What to test in each build

A **new Creative world with cheats on**, a superflat one is fine. Screenshots with **F2** (the game folder's
`screenshots\`); look at them. Key presses and clicks: what Windows already has (PowerShell + Win32 `SendInput`), or ask.

1. **It loads.** In `logs\latest.log`: `TA-01 · Last Light joined …` and `TA-02 · Quasar Jets joined …` lines (one
   per menu). Note every `ERROR`, `WARN` or exception naming `ton618_addition`, `TON 618 Addition`, `TA-01`, `TA-02` or
   `dev.adamko`, word for word. On Fabric 1.21.11 and 26.3 also: `TA-02 · Quasar Jets' render pipelines compiled`
   (and whether it says "failed").
2. **Keys (fix 1).** Options > Controls > Key Binds: find TA-01 · Last Light (`;` and `'`) and TA-02 · Quasar Jets
   (`,` and `.`). Screenshot. If one shows as unbound, press its Reset, note it, go on.
3. **The H menu, overworld.** `/give @s ton618:ton_618`, hold it, press **H**: both skills listed with their icons (a
   small red triangle beside them is normal). Close it, press `,`: TA-02 becomes the picked skill (its name above the
   hotbar). Screenshot.
4. **The dark (fix 2).** Quick way, as the NeoForge test did: `/data modify storage ton618:ton618 devoured set value 1b`
   then `/execute in ton618:devoured run tp @s 0 1 0` (or the real way: in the H menu set TON618's Scale to ∞, close,
   right-click; ~28 s film). In the dark, holding TON 618's item:
   - press **H**: the menu shows. Screenshot.
   - with TA-02 picked (`,`), right-click: **the action bar shows** "Quasar Jets: only in the overworld, at night" (or,
     without SS-21 Quasar, its "Locked: …" reason) above where the hotbar would be. Screenshot. **This is the main
     check.** On every build but 26.3 the log gets once: `TON 618 hid the game's HUD (its dark or a film): TON 618
     Addition draws the H menu over it, and the action bar in the dark.`
5. **Last Light.** Still in the dark: press `;` (picks Last Light), right-click. Its film plays (~42 s; F2 at about 3,
   12, 24, 35 and 41 s and say in a word what you see), and you land at the overworld spawn on a morning, HUD back,
   no new errors. (Its cooldown is 60 s.)
6. **TA-02 · Quasar Jets** (priority rows; only where SS-21 Quasar loads). Overworld, `/time set midnight`, clear sky.
   Hold TON 618's item, press `,`. Named stars show in the sky with their names while you hold it. Look at one,
   right-click (locks it: a reticle with its name), then look at the ground **at least ~70 blocks away** and
   right-click: a ~48 s film plays (F2 at about 5, 15, 25, 35 and 45 s), then you see the crater. Say what you saw and
   whether anything was missing or black. Then once more with films off (press **J**, RimuLib's film key) from about
   100 blocks away in third person (F5): the second sun, the jet coming down, the blast (F2 a few times).
7. **Fabric 26.3 only**: in the dark the action bar should show without any help (TON 618 doesn't hide it there).

Normal, not bugs: in the dark the hotbar and chat stay hidden; TON 618's own time effects in the first seconds there;
the red triangle; the burst's white flash; the H menu needs TON 618's item or the Stellar Remote in hand; TA-02 refuses
by day, outside the overworld, or when the ground mark is too close (it says why on the action bar).

## The report

Write `C:\Users\adam_\Documents\TON 618 Addition\codex tests\REPORT-ports-1.1.2.md` (keep the old `REPORT.md`), then
give Adam a short summary.
- **Top**: one line per build: PASS / FAIL / NOT RUN, and the one thing that failed.
- **Per build**: the versions used (Minecraft, loader, TON 618, RimuLib, The Shooting Star, SS-21 Quasar, Fabric API),
  steps 1-7 with what you saw, screenshots copied to `codex tests\screenshots\ports-1.1.2\<build>\`, and every error or
  warning about the mod **word for word** from `latest.log` (with the lines just before it). On a crash, the crash
  report's top part.
- No guessed fixes stated as facts; a "maybe because …" line is fine.
