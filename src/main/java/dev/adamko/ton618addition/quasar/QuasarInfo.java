package dev.adamko.ton618addition.quasar;

import java.util.function.Supplier;

/** TA-02 · Quasar Jets' fixed facts, shared by both menus it joins (plan: CLAUDE.md §10, previz/quasar-jets/NOTES.md). */
public final class QuasarInfo {
    public static final String ID = "quasar_jets";
    public static final String CODE = "TA-02";
    public static final String NAME = "Quasar Jets";
    public static final String TITLE = CODE + " · " + NAME;
    public static final String TIER = "TON 618 Addition";
    /** The jets' blue-white. */
    public static final int COLOR = 0xA8C8FF;
    /** Ready again a minute after the film ends. */
    public static final int COOLDOWN = QuasarClock.END + 1200;
    /**
     * Default keys: "," on TON 618's item and "." on the Stellar Remote (Last Light has ";" and "'"; every letter in that
     * menu is taken by someone, see LastLightInfo).
     */
    public static final int KEY_ON_TON618 = 44;
    public static final int KEY_ON_REMOTE = 46;

    /** SS-21 Quasar's mod id (Aether21; NeoForge 1.7.1 and Fabric 1.8.0 jars both say this): TA-02 only joins with it. */
    public static final String SS21_MOD_ID = "shooting_star_ss21";

    /** The menu's warning when TA-02 is picked, or null; set by the client (night, overworld). */
    public static volatile Supplier<String> selectWarning = () -> null;

    private QuasarInfo() {
    }
}
