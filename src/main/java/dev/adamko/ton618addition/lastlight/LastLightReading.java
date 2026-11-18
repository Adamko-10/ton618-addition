package dev.adamko.ton618addition.lastlight;

/**
 * <b>FABLE'S FILE (data only).</b> Educational mode's reading windows for TA-01 · Last Light (Adam, 2026-11-16: "apply a
 * similar educational thing to ton 618 last light so like give more time to read if neccesary"). Same format and use as
 * {@code quasar/QuasarReading}: with the caster's educational mode on, story time runs slower inside each window.
 *
 * <p>Last Light's eight labels were already on screen 2.4 to 5.9 s (Adam called that fine), so these are mild: one calm
 * stretch inside each label's time, slowed to between a half and two thirds of its speed. Together they make the film
 * about 9 seconds longer (51 s for 42.5 s). Nothing is slowed where the film's sounds must land on an event: the press
 * (78), the first wave (118), the cut into space (200), the hush and the burst (665 to 690: the server brings the world
 * back at 690) and the return (720).
 */
public final class LastLightReading {
    /** {@code {from, to, slow}} in story ticks ({@code LastLightClock}), {@code slow} >= 1, in order, not overlapping. */
    public static final float[][] WINDOWS = {
        // "a real black hole": in the dark, before the press (+1 s)
        {24, 44, 2F},
        // Hawking temperature: space cooling round the hole (+1.1 s)
        {290, 318, 1.8F},
        // the last stars burn out (+1.3 s)
        {350, 376, 2F},
        // what is left over (+1 s)
        {404, 430, 1.8F},
        // Hawking radiation: the pairs at the horizon (+1.4 s)
        {480, 520, 1.7F},
        // smaller, hotter, faster (+0.6 s)
        {580, 604, 1.5F},
        // a googol of years: the point of light, before the hush (+1.4 s)
        {636, 660, 2.2F},
        // the information paradox: the world is back (+0.8 s)
        {770, 810, 1.4F}};

    private LastLightReading() {
    }
}
