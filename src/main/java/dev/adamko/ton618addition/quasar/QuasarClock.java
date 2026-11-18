package dev.adamko.ton618addition.quasar;

/**
 * TA-02's clock: game ticks from the second press (the strike point marked), from Fable's approved previz v1
 * ({@code previz/quasar-jets/NOTES.md}, "Key ticks"). The film reads them; the server times the world effects by them.
 */
public final class QuasarClock {
    /** The second press: the strike point is marked, the film starts. */
    public static final int MARK = 0;
    public static final int LOCK = 38;
    public static final int ENGINE = 80;
    public static final int PUSH = 150;
    public static final int FLIGHT = 200;
    public static final int ARRIVE = 330;
    public static final int SHRED = 350;
    public static final int IGNITE = 440;
    public static final int JETS = 530;
    public static final int TURN = 610;
    public static final int FLOOD = 665;
    /** World: the second sun rises (night to day 680 to 700). */
    public static final int DAY = 680;
    /** World: the jet comes in from the horizon; it reaches the mark at {@link #LANDS}. */
    public static final int SWEEP = 745;
    public static final int LANDS = 795;
    /** World: the radio lobe swells (to full size by 876); the bowl melts 842 to 890; carried things thrown out 846 to 886. */
    public static final int LOBE = 830;
    public static final int MELT = 842;
    public static final int THROW = 846;
    /** World: the lobe pops; crystals grow 890 to 918. */
    public static final int POP = 890;
    /** World: the second sun fades (900 to 926). */
    public static final int NIGHT = 905;
    public static final int END = 960;

    /** The jet's head starts this far from the mark (blocks), along the line to the quasar. */
    public static final double BEAM_FAR = 1500.0;

    private QuasarClock() {
    }

    /** 0 to 1 from {@code from} to {@code to}, clamped. */
    public static float ramp(float t, float from, float to) {
        return t <= from ? 0f : t >= to ? 1f : (t - from) / (to - from);
    }

    /** How far the jet's head is from the mark at tick {@code t} (from {@link #BEAM_FAR} to 0 by {@link #LANDS}). */
    public static double beamHead(float t) {
        float u = ramp(t, SWEEP, LANDS);
        return BEAM_FAR * Math.pow(1.0 - u, 3.0);
    }

    /** The radio lobe's size as a share of its full radius at tick {@code t} (0 outside LOBE..POP). */
    public static float lobe(float t) {
        if (t < LOBE || t >= POP) {
            return 0f;
        }
        float u = ramp(t, LOBE, 876);
        return u * u * (3f - 2f * u);
    }

    /** The second sun: 0 (night) to 1 (full day), the same for everyone in the overworld. */
    public static float day(float t) {
        if (t < DAY || t >= 926) {
            return 0f;
        }
        return Math.min(ramp(t, DAY, 700), 1f - ramp(t, 900, 926));
    }
}
