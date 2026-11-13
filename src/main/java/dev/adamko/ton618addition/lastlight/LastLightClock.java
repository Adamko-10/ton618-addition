package dev.adamko.ton618addition.lastlight;

/**
 * TA-01 · Last Light's clock: game ticks from the cast (20 a second), from the previz Adam approved on 2026-11-13
 * (v2, {@code previz/NOTES.md}, "Key ticks"). The server acts on {@link #RESTORE} and {@link #END}; the rest are for the
 * film. Owned by the Opus session: Fable proposes changes in {@code reviews/fable/PROPOSALS.md}.
 */
public final class LastLightClock {
    /** The cast reaches the clients; the film starts. */
    public static final int CAST = 0;
    /** The item is pressed (a flash in the hand). */
    public static final int PRESS = 78;
    /** Time is released: the first wave, the year counter starts. */
    public static final int RACE = 118;
    /** Flash cut from the dark into the film's own space scene. */
    public static final int COSMOS = 200;
    /** Space falls colder than the hole (about 10^12 years): it starts to shrink. */
    public static final int CROSS = 270;
    /** The last star goes out (about 10^14 years). */
    public static final int STARS_OUT = 377;
    /** Hawking pairs become visible (the black-hole era). */
    public static final int PAIRS = 440;
    /** The hole visibly shrinks, faster and faster. */
    public static final int SHRINK = 540;
    /** A point of light; the counter nears 10^100. */
    public static final int GOOGOL = 625;
    /** All sound and motion stop for 15 ticks. */
    public static final int HUSH = 665;
    /** The final burst: full white by 681. */
    public static final int BURST = 680;
    /** The server runs {@code ton618 restore} (hidden by the white): everyone in the dark goes back to the overworld. */
    public static final int RESTORE = 690;
    /** A wave from the caster rolls the white away: the overworld is back. */
    public static final int CLEAR = 720;
    /** The camera is handed back; the skill is over. */
    public static final int END = 850;

    private LastLightClock() {
    }
}
