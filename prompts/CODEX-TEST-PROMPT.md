# Codex: test TON 618 Addition 1.0.0 in the real game (7 jars)

You are testing a Minecraft mod for Adam (adamko_10) on **his own PC**, in a real game (his PC has a graphics card;
the machine where the mod was built has none, so nobody has seen it draw yet). **Test and report only: don't change
the mod.** Its code and builds live on another machine; a Claude session there fixes what you find.

## Adam's rules (follow them exactly)

- **Never touch his own Minecraft setup**: not his Modrinth App profiles (`D:\ModrinthApp\...`), not their mods,
  saves or configs. Work only in your own test folder: `C:\Users\adam_\Documents\TON 618 Addition\codex tests\`.
- **Ask before** you install anything, download anything (say the file, where from and its size, in one list), or
  delete anything. Never delete his files (if a mod must go, rename it to `.disabled`).
- **Don't start a game while he is playing.** You will drive the mouse and keyboard while a test runs: before each
  run, tell him and wait for his "go".
- **Never enter his passwords.** If a launcher needs his Microsoft sign-in, he signs in himself.
- Never upload, post, publish or push anything anywhere.
- Talk to him short and plain, the answer first; ask questions as short numbered choices. Say what a step costs in time
  before a long one. He is budget-conscious: if time runs short, finish the **priority** builds (below) and report.

## What the mod is

**TON 618 Addition** 1.0.0 (mod id `ton618_addition`) is an unofficial addon for **TON 618** by rimuru_dev (a
black-hole mod) and needs **RimuLib**. It adds one skill, **TA-01 · Last Light**:

1. Hold TON 618's item (`ton618:ton_618`), press **H** (RimuLib's menu), set TON618's **Scale** slider to **∞** (no
   limit), close the menu, **right-click**: TON 618's own film plays and the world is "devoured": every player lands in
   the dark dimension `ton618:devoured` (a black void floor, TON618 in the sky).
2. In the dark, pick **Last Light** (in the H menu, or shift + right-click with the item to switch skills) and
   **right-click**: a film of about **42.5 s** plays (the universe ages for a googol of years until TON618 evaporates by
   Hawking radiation), the screen goes white, and everyone lands back at the overworld spawn **on a new morning**.

## The 7 jars

Adam copies them to: `C:\Users\adam_\Documents\TON 618 Addition\test builds\2026-11-14 TON 618 Addition 1.0.0 all
loaders\` (if they aren't there, ask him where they are). Each build needs its loader, **TON 618 1.1.0** and **RimuLib
1.2.3** for that loader, and Fabric API on Fabric. Don't install shader packs (Iris) or other mods.

| # | Jar | Minecraft + loader | Also needs | Priority |
|---|-----|--------------------|------------|----------|
| 1 | `TON 618 Addition 1.0.0 [Fabric 26.3].jar` | 26.3, Fabric Loader 0.19.5+, Java 25 | Fabric API 0.162.0+26.3 or newer | **yes** (its drawing code is new) |
| 2 | `TON 618 Addition 1.0.0 [Fabric 1.21.11].jar` | 1.21.11, Fabric Loader 0.19.5+ | Fabric API 0.141.6+1.21.11 or newer | **yes** (its drawing code is new) |
| 3 | `TON 618 Addition 1.0.0 [Forge 1.20.1].jar` | 1.20.1, Forge 47.4.x | | **yes** (never run before) |
| 4 | the same Forge 1.20.1 jar | 1.20.1, **NeoForge** 47.1.x | | **yes** (it should run there too) |
| 5 | `TON 618 Addition 1.0.0 [Forge 1.21.1].jar` | 1.21.1, Forge 52.1.x | | |
| 6 | `TON 618 Addition 1.0.0 [Fabric 1.21.1].jar` | 1.21.1, Fabric Loader 0.19.5+ | Fabric API 0.116.7+1.21.1 or newer | |
| 7 | `TON 618 Addition 1.0.0 [Fabric 1.20.1].jar` | 1.20.1, Fabric Loader 0.19.5+ | Fabric API 0.92.6+1.20.1 or newer | |
| 8 | `TON 618 Addition 1.0.0 [NeoForge 1.21.1].jar` | 1.21.1, NeoForge 21.1.x | | Adam already tested it: quick load check only |

TON 618 and RimuLib from CurseForge (TON 618 project 1798822, RimuLib project 1753907); the files:

- TON 618 1.1.0: **NeoForge** file 9562579 (1.20.1 + 1.21.1 in one jar), **Forge** 9562567, **Fabric** 9562551
- RimuLib 1.2.3: **NeoForge** 9562438, **Forge** 9562444, **Fabric** 9562450
- A direct link that needs no sign-in: `https://www.cursemaven.com/curse/maven/<slug>-<project>/<file>/<slug>-<project>-<file>.jar`
  (slugs `ton618` and `rimulib`).
- The Forge and Fabric files hold one jar per Minecraft version inside; the loader picks the right one. For NeoForge
  1.20.1 (row 4), try the NeoForge files first and the Forge files if those don't load; say which worked.
- Fabric API: `https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/<version>/fabric-api-<version>.jar`.

**How to run the games**: pick the way that installs the least, and ask Adam before setting it up. For example a
command-line launcher kept in your test folder (PortableMC, HeadlessMC), or new, separate Modrinth App instances
named "TA test …" (never his existing ones). One game folder per row, so the mods never mix.

## What to test in each build

Use a **new Creative world with cheats on**. Take screenshots with **F2** (they land in the game folder's
`screenshots\`) and look at them. To press keys and click inside the game, use what Windows already has (e.g.
PowerShell with the Win32 `SendInput` API) or ask Adam before installing a tool.

1. **It loads.** The game reaches the title screen and the world opens. In `logs\latest.log` find
   `TA-01 · Last Light joined TON 618's menu (ton618:ton_618) as skill #2`. Note every `ERROR` or exception that
   mentions `ton618_addition`, `Last Light`, `TA-01` or `dev.adamko`.
2. **The H menu, before the dark.** `/give @s ton618:ton_618`, hold it, press **H**. Expect: a Last Light row with its
   icon (a small red triangle beside it is normal: RimuLib marks every unofficial addon so); a small **TON 618
   Addition** panel in the **top right** with an educational-mode switch (key **\\**) and "How to unlock Last Light:
   1. Set TON618's Scale to ∞ … 2. Use TON618 … 3. In the dark, cast Last Light". Picking Last Light shows a
   "Locked: …" warning. Screenshot it.
3. **The devour.** In the H menu set TON618's **Scale** to **∞**, close it, right-click. TON 618's own film plays
   (about 28 s); you end in the dark. `/ton618 status` should say it is devoured.
4. **The cast.** In the dark: press H; the menu must show (with "In the dark: Last Light is ready" in the panel). Pick
   Last Light, close the menu, right-click. In `latest.log` expect
   `TA-01 · Last Light cast … in TON 618's dark; 1 player(s) see it. The world comes back in 34 s.` and
   `TA-01 · Last Light reached this game at film tick … (films on…)`
5. **The film** (about 42.5 s from the cast). Press F2 at about these times after the right-click and say for each
   what you see (roughly; report what is really there):
   - **3 s**: the dark, from behind your shoulder, a caption "AFTER TON 618"
   - **5 s**: a glow in your hand and a small ring over the floor ("LAST LIGHT", "armed")
   - **8 s**: a bright wave racing out over the dark floor with a wall of light; a year counter at the top; the title
     "TON 618 ADDITION / TA-01 · LAST LIGHT"
   - **12 s**: **space**: TON618 with a glowing disk among streaking stars; "THE UNIVERSE COOLS"; readouts on the right
   - **18 s**: stars going out; "THE LAST STARS"
   - **24 s**: close on the black hole's edge; small blue and gold particle pairs; "THE BLACK HOLE ERA"
   - **29 s**: the hole shrinking, glowing hotter; "EVAPORATING"
   - **33 s**: a blinding point of light; "A GOOGOL OF YEARS"
   - **35 s**: all white
   - **38 s**: back in the overworld at spawn; a wave of light rolling out over the land, white beyond it;
     "THE WORLD RETURNS"
   - **41 s**: morning, motes of light settling, the camera behind you
   Also say whether you heard the film's soundtrack (a hum, ticks, a boom at the burst) if you can tell from the log
   or the system's audio; if not, skip it.
6. **After the film.** You are at the overworld spawn, it is morning (the sun low in the east), the HUD is back,
   `/ton618 status` says not devoured, and the log has no new errors from the mod.
7. **Extra checks, priority builds only, if time allows** (Last Light's cooldown is 60 s; devour again first):
   - **Educational mode**: press **\\** while the H menu is open (the switch flips), then devour and cast again: small
     physics labels show in the film (e.g. at ~24 s "Hawking radiation: quantum particle pairs appear at the
     horizon …").
   - **Films off**: press **J** (RimuLib's film key; in the dark its message is hidden), then cast: instead of the
     film a small card at the top says "TA-01 · LAST LIGHT … the world comes back in N s … press J …". The world
     still comes back at ~34.5 s.
   - **Fabric 26.3 only**: press **F1** during the film: the space scene, the words and the white must still show.

Normal, not bugs: in the dark TON 618 hides the game's own HUD and action-bar messages, and plays its own time effects
(drag, rewind, waves) during the first seconds; the red triangle in the menu; the burst's full-screen white flash.

## The report

Write `C:\Users\adam_\Documents\TON 618 Addition\codex tests\REPORT.md`, then give Adam a short summary.

- **Top**: one line per build: PASS / FAIL / NOT RUN, and the one thing that failed.
- **Per build**: the versions you used (Minecraft, loader, TON 618 and RimuLib files, Fabric API), each step 1-7
  with what you saw, the screenshots (file names; copy them to `codex tests\screenshots\<build>\`), and every error or
  warning about the mod **copied word for word** from `latest.log` (with the lines just before it). On a crash, copy
  the crash report's top part.
- Don't guess fixes into the report as facts; a "maybe because …" line is fine.
