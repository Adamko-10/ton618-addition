package dev.adamko.ton618addition.quasar;

/**
 * <b>FABLE'S FILE (data only).</b> Educational mode's reading windows for TA-02 · Quasar Jets (Adam, 2026-11-16: more time
 * to read with educational mode on, slowing only the parts with text). Used only when the caster had educational mode on;
 * then, for everyone watching that strike, story time runs slower inside each window. {@link QuasarClock#story} and
 * {@link QuasarClock#real} turn real ticks since the cast into story ticks and back (server and client alike).
 */
public final class QuasarReading {
    /**
     * {@code {from, to, slow}}: between story ticks {@code from} and {@code to}, story time runs at {@code 1 / slow} of real
     * time ({@code slow} >= 1; 2 = the window lasts twice as long). In story order, not overlapping. Empty = no slowing.
     */
    public static final float[][] WINDOWS = {};

    private QuasarReading() {
    }
}
