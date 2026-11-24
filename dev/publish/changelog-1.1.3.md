**For Minecraft {MC}, {LOADER}{ALSO}.** Install on the server and every client. Needs **TON 618 1.1.0** and **RimuLib 1.2.3** or newer{FABRIC_API}. TA-02 also needs **SS-21 Quasar**.

## 1.1.3: fixes

{only:fabric-1.21.11,fabric-26.3}*   **Messages in TON 618's dark** now show on this version too. TON 618 hides the action bar there along with the rest of the HUD, so you never saw why a skill couldn't be cast (TA-02's "only in the overworld, at night", for example).
{only:forge-1.20.1}*   **TA-02's lock-on reticle and strike readout** now show during normal play (on Forge 1.20.1 they never did), and RimuLib's menu is no longer drawn twice.
{not:fabric-26.3}*   **With films off**, a refusal message in TON 618's dark is no longer hidden while a Last Light cast is running.
*   **TA-02's film waits its turn**: if another film is still playing when a strike reaches you, it no longer takes over your camera, the readout or the sound before it's shown.
*   **Last Light no longer skips another film** when you have films switched off.
*   **Controls**: TA-01's and TA-02's keys now have proper names ("TON 618: TA-01 · Last Light", "The Shooting Star: TA-02 · Quasar Jets"...) instead of internal ones.
{only:forge-1.20.1}
{only:forge-1.20.1}## Known issue
{only:forge-1.20.1}
{only:forge-1.20.1}*   On **Forge and NeoForge 1.20.1**, the game may close without a Minecraft crash report, with a crash in Windows' `d3d11.dll`. It comes from the graphics side, not from this addon (it can happen on the loading screen, before any world loads), and was seen with the **Medal** screen recorder running. Close screen recorders and overlays, or set `earlyWindowControl = false` in `config/fml.toml`.
