**For Minecraft {MC}, {LOADER}{ALSO}.** Install on the server and every client. Needs **TON 618 1.1.0** and **RimuLib 1.2.3** or newer{FABRIC_API}. TA-02 also needs **SS-21 Quasar**.

## 1.1.2: TA-02 · Quasar Jets comes to {LOADER} {MC}

Everything from 1.1.0 and 1.1.1 (NeoForge only until now), and the fixes of 1.1.2.

### TA-02 · Quasar Jets (new)

*   **Only with SS-21 Quasar installed** (without it TA-02 shows in the menus, locked). At night, hold TON 618's item (or the Stellar Remote) with TA-02 picked and eight named stars appear in the sky: Barnard's Star, Wolf 359, Alpha Centauri A, Tau Ceti, Rigel, Spica, Betelgeuse and Antares. Cast at one to lock it, then aim at the ground and cast again.
*   **What happens**: a star engine drives the star into TON618 while its own time races. TON618 tears it apart, lights up as a quasar and fires a jet at the Earth. A **second sun** turns the night to day, the jet sweeps down carrying mobs and items, and it **lands in a huge blast** of plasma: a blue-white flash, a white-hot plasma ball, magnetic arcs leaping out of a crater 200 to 600 blocks wide (by the star's type), debris flung out, trees flattened. A **radio lobe** swells over the crater and pops, and **Frozen Light** crystals in the star's colour are left in it (minable). The star is gone from the sky for a few nights.
*   **Power and Damage sliders** in the H menu, like The Shooting Star's own skills: Power 0.1× to 3× the star's own blast (a warning above 2×); Damage at the blast's middle, **∞** by default (kills everything in its reach).
*   **A landing marker** everyone near can see, and a **readout for watching without the film** (the phase, the time to impact, the mark's distance, a countdown as the jet comes in).
*   **Its own film** and **its own sounds**: the impact's boom reaches you at the speed of sound.
*   **Server settings** under `[quasar_jets]`: turn it off, operators only, terrain changes, the second sun, carrying players, how many nights a star stays missing, `max_power` and `max_damage`.
{only:fabric-1.21.11,fabric-26.3}*   With a **shader pack**, the named stars may show in front of the pack's clouds on this version.

### Fixes

{not:fabric-26.3}*   **Messages in TON 618's dark**: TON 618 hides the whole HUD there, the action bar too, so when a skill couldn't be cast you never saw why. Now the action bar shows in the dark; the hotbar and chat stay hidden, as TON 618 wants.
*   **Default keys**: TA-01 · Last Light's **;** (on TON 618's item) and **'** (on the Stellar Remote), and TA-02 · Quasar Jets' **,** and **.**, never existed. Now they do. **If they show as unbound** in Options > Controls (likely if you also play with The Shooting Star Addition: SA Series), press **Reset** next to each.

### Last Light

*   Its film's early glow, ring and floor wave now show properly in TON 618's dark.
*   Minecraft's pop-up toasts no longer cover the film.
*   **Educational mode** now also gives you **more time to read**: in a cast made with it on, the parts with text slow down for everyone watching (Last Light and TA-02).
*   A new pixel-art menu icon, and a shorter "not yet" message in the menu.

### Also

*   The mod's icon now shows in your launcher's mod list.
{only:forge-1.20.1}
{only:forge-1.20.1}## Known issue
{only:forge-1.20.1}
{only:forge-1.20.1}*   On **Forge and NeoForge 1.20.1**, the game may close without a Minecraft crash report, with a crash in Windows' `d3d11.dll`. It comes from the graphics side, not from this addon (it can happen on the loading screen, before any world loads), and was seen with the **Medal** screen recorder running. Close screen recorders and overlays, or set `earlyWindowControl = false` in `config/fml.toml`.
