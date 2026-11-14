package dev.adamko.ton618addition.lastlight;

import java.util.function.Supplier;

/** TA-01 · Last Light's fixed facts, shared by both menus it joins. */
public final class LastLightInfo {
    public static final String ID = "last_light";
    public static final String CODE = "TA-01";
    public static final String NAME = "Last Light";
    /** "TA-01 · Last Light", as the menus show it (Adam, 2026-11-13: codes like the SA Series). */
    public static final String TITLE = CODE + " · " + NAME;
    /** The line over the title in the menus. */
    public static final String TIER = "TON 618 Addition";
    /** The blue-white of the final burst. */
    public static final int COLOR = 0xBFE0FF;
    /** Ready again a minute after a cast (there is nothing left to evaporate after one anyway). */
    public static final int COOLDOWN = 1200;
    /**
     * Its default key on TON 618's own item: ";" (it picks Last Light in the H menu). Every letter is taken in that
     * menu by someone (the SA Series: B is Stellar Convergence, V/T/R/X its plate's, M/G/L/F/E/C its other plate's).
     */
    public static final int KEY_ON_TON618 = 59;
    /** Its default key on the Stellar Remote: "'" (a different key, so the two copies never clash). */
    public static final int KEY_ON_REMOTE = 39;

    /**
     * The warning the menu shows when Last Light is picked, or null for none. The client sets it (Last Light only works
     * in TON 618's dark); the server never asks.
     */
    public static volatile Supplier<String> selectWarning = () -> null;

    private LastLightInfo() {
    }
}
