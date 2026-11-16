package dev.adamko.ton618addition.client.quasarfilm;

import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.Locale;

/**
 * <b>FABLE'S FILE.</b> The Quasar Jets film's timeline, numbers and words, ported from the approved previz
 * ({@code previz/quasar-jets/quasar-jets-previz.html}, {@code previz/quasar-jets/NOTES.md}). One clock: game ticks from
 * the second press, as {@code QuasarClock} names them (no retime table this time). Everything is a pure function of the
 * tick.
 *
 * <p>The captions and labels sit a little longer than in the previz (Adam on v1: "slow down the labels a little"): none
 * is on screen for less than about 2.3 s. Every key tick of {@code QuasarClock} is unchanged; only the film's own
 * ending runs on about 2 s past the strike's END (960), holding on the gap in the sky (Adam, 2026-11-16: "2 s
 * longer"). The world effects are over by then, and {@code QuasarClient} keeps the strike until END + 40.
 */
final class JetScript {
    /** The film's own end (the strike's is 960). Must stay under {@code QuasarClock.END + 40}. */
    static final int END = 996;
    /** The camera's last shot ends here; RimuLib then blends back to the player's own view by END. */
    static final int LAST_SHOT_END = 984;
    /** Our own space scene covers the screen from the flash cut to under the flood's white. */
    static final float SPACE_FROM = 80F;
    static final float SPACE_TO = 680F;
    /** Shot 10's last move: the camera tilts up to the gap in the sky. */
    static final float TILT_FROM = 928F;
    static final float TILT_TO = 948F;

    private JetScript() {
    }

    // ---- small maths ------------------------------------------------------------------------------------------------

    static float sat(float x) {
        return x < 0F ? 0F : Math.min(x, 1F);
    }

    static float lerp(float a, float b, float u) {
        return a + (b - a) * u;
    }

    /** 0 at {@code a}, 1 at {@code b}, straight. */
    static float lin(float a, float b, float x) {
        return sat((x - a) / (b - a));
    }

    /** 0 at {@code a}, 1 at {@code b}, eased at both ends. */
    static float ss(float a, float b, float x) {
        float u = lin(a, b, x);
        return u * u * (3F - 2F * u);
    }

    /** A kick at {@code at} dying away over about {@code over} ticks. */
    static float kick(float t, float at, float over) {
        return t >= at ? (float) Math.exp(-(t - at) / over) : 0F;
    }

    /** Straight lines through {@code k}'s points (tick, value). */
    static float keys(float[][] k, float x) {
        if (x <= k[0][0]) {
            return k[0][1];
        }
        for (int i = 1; i < k.length; i++) {
            if (x <= k[i][0]) {
                return lerp(k[i - 1][1], k[i][1], (x - k[i - 1][0]) / (k[i][0] - k[i - 1][0]));
            }
        }
        return k[k.length - 1][1];
    }

    // ---- the film's numbers ----------------------------------------------------------------------------------------

    // The sail (Adam, 2026-11-16: "the sail is supposed to make the star go really fast not over years"; his pick: the
    // remote fast-forwards the star's own time, Earth's clock stays normal). The star creeps while its clock runs as
    // ours does (to 162), then the fast-forward takes hold: it is at full speed four seconds after the engine lit.
    private static final float[][] SPEED = {{150, 0}, {162, 0.5F}, {172, 5}, {188, 28}, {212, 56}, {300, 62}, {322, 20}, {330, 6}};
    private static final float[][] YEARS = {{150, 0}, {162, 0.9F}, {176, 5}, {212, 9.6F}, {300, 11.6F}, {330, 11.86F}};
    private static final float[][] HOLE_X = {{80, 12000}, {200, 12000}, {270, 6500}, {300, 3000}, {322, 900}, {330, 520}};
    /** How far the stars have gone by (a running total of {@link #speed}). */
    private static final float[] TRAVEL = new float[END + 3];

    static {
        for (int i = 1; i < TRAVEL.length; i++) {
            TRAVEL[i] = TRAVEL[i - 1] + speed(i - 1);
        }
    }

    /** How fast the stars go by in the flight, scene units a tick. */
    static float speed(float t) {
        return t <= 150F || t > 332F ? 0F : keys(SPEED, t);
    }

    static float travel(float t) {
        float x = Math.max(0F, Math.min(t, END));
        int i = (int) x;
        return lerp(TRAVEL[i], TRAVEL[i + 1], x - i);
    }

    /** log10 of the years gone by on the star's own clock. */
    static float logYears(float t) {
        return keys(YEARS, t);
    }

    /** How many turns the hand of the fast-forward's dial has made: creeping to 162, then racing. */
    static float dialTurns(float t) {
        float u = Math.max(0F, t - 162F);
        return 0.012F * Math.max(0F, t - 150F) + 0.0025F * u * u + 0.00004F * u * u * u;
    }

    /** How smeared the dial's hand is: 0 a sharp line, 1 one ring of light. */
    static float dialSmear(float t) {
        float u = ss(164F, 206F, t);
        return u * (float) Math.sqrt(u);
    }

    /** Light years still to go to TON618. */
    static double distanceLy(float t) {
        return 18.2e9 * (1.0 - travel(t) / travel(330F));
    }

    /** How far the disc is tipped toward us (radians: 0.2 = nearly edge-on, a quarter turn = face-on). */
    static float beta(float t) {
        return lerp(0.2014F, (float) (Math.PI / 2.0), ss(610F, 665F, t));
    }

    /** How far ahead TON618 is during the flight (scene units). */
    static float holeX(float t) {
        return keys(HOLE_X, t);
    }

    /** The blue-white flood: up as the jet turns to us, solid 668 to 684, clearing onto the second sun by 704. */
    static float white(float t) {
        if (t < 650F) {
            return 0F;
        }
        if (t < 668F) {
            float u = lin(650F, 668F, t);
            return u * u;
        }
        return t < 684F ? 1F : 1F - ss(684F, 704F, t);
    }

    /**
     * The star's fall into TON618, in the disc's own flat coordinates (x right, y away from us): where something is at
     * its own time {@code tau}, ending up circling at radius {@code rf}; {@code j} spreads the ribbon sideways.
     */
    static void fall(float tau, float rf, float j, float[] out) {
        if (tau < 350F) {
            out[0] = -(260F + (350F - tau) * 5F);
            out[1] = 0F;
            return;
        }
        float w = (tau - 350F) / 70F;
        float u = Math.min(w, 1F);
        float r = lerp(260F, rf, u * (2F - u));
        double th = Math.PI - (0.5 * u + 4.4 * u * u) - j * u;
        if (w > 1F) {
            th -= 0.12 * Math.pow(60.0 / rf, 1.5) * (w - 1F) * 70.0;
        }
        out[0] = (float) Math.cos(th) * r;
        out[1] = (float) Math.sin(th) * r;
    }

    // ---- what the star's type changes in the film --------------------------------------------------------------------

    private static final float[] JET_WIDTH = {0.4F, 0.6F, 0.85F, 1.15F};
    private static final String[] TYPE_FACT = {
        "A red dwarf: small, cool and dim. Most stars are like this, yet none can be seen by eye.",
        "A star like the Sun: yellow-white, about 5,500 °C at the surface.",
        "A blue giant: tens of thousands of times brighter than the Sun. It burns out in millions of years, not billions.",
        "A red supergiant: hundreds of Suns wide. Where the Sun is, it would swallow Mars."};

    /** How wide the jets are in the space scene (and how hard they shake the picture). */
    static float jet(QuasarStars.Type type) {
        return JET_WIDTH[Math.min(type.ordinal(), JET_WIDTH.length - 1)];
    }

    static String typeFact(QuasarStars.Type type) {
        return TYPE_FACT[Math.min(type.ordinal(), TYPE_FACT.length - 1)];
    }

    static float[] rgb(int colour) {
        return new float[] {(colour >> 16 & 255) / 255F, (colour >> 8 & 255) / 255F, (colour & 255) / 255F};
    }

    /** The star's mass in Suns, from its text ("17 Suns"). */
    static float mass(QuasarStars.Star star) {
        try {
            return Float.parseFloat(star.mass().trim().split(" ")[0]);
        } catch (RuntimeException unreadable) {
            return 1F;
        }
    }

    // ---- words --------------------------------------------------------------------------------------------------------

    /**
     * A caption over the lower bar: from, to (ticks), the caption, the line under it. {@code %STAR%} is the star's name,
     * {@code %TYPE%} "type · distance away".
     */
    record Caption(float from, float to, String title, String detail) {
    }

    static final Caption[] CAPTIONS = {
        new Caption(4, 50, "QUASAR JETS", "strike point marked"),
        // held across the flash cut: the same star, now close
        new Caption(52, 100, "%STAR%", "%TYPE%"),
        new Caption(102, 150, "THE STAR ENGINE", "a mirror to catch its own light"),
        new Caption(154, 202, "FAST FORWARD", "the remote speeds up the star's own time"),
        new Caption(206, 262, "ACROSS THE UNIVERSE", "18 billion light years, in seconds for us"),
        new Caption(270, 326, "TON618", "66 billion Suns"),
        new Caption(336, 384, "CAUGHT", "the mirror falls in first"),
        new Caption(388, 438, "TIDAL DISRUPTION", "the star is pulled into a ribbon"),
        new Caption(444, 526, "IGNITION", "the brightest thing in the universe"),
        new Caption(534, 606, "THE JETS", "out of both poles, at nearly the speed of light"),
        new Caption(612, 662, "BLAZAR", "one jet swings to face Earth"),
        new Caption(692, 742, "SECOND SUN", "night turns to day"),
        new Caption(749, 792, "SWEPT ALONG", "the jet carries what it touches to the mark"),
        new Caption(797, 828, "IMPACT", "the jet hits the ground at nearly the speed of light"),
        new Caption(834, 886, "RADIO LOBE", "a bubble of plasma where the jet ends"),
        new Caption(892, 940, "FROZEN LIGHT", "what is left of %STAR%")};

    /** What an educational label points at (JetHud works out where that is on the screen). */
    enum Anchor {
        STAR, MIRROR, TAIL, DIAL, FAR_HOLE, RIBBON, DISC, NEAR_JET, HOLE, SECOND_SUN, BEAM, CRATER, DOME, CRYSTALS
    }

    /**
     * An educational label: from, to (ticks), what it points at, where its box sits from there (hundredths of the
     * screen's width and height), its text ({@code %TYPE_FACT%} and {@code %TIDAL%} depend on the star).
     */
    record Label(float from, float to, Anchor anchor, float offX, float offY, String text) {
    }

    static final Label[] LABELS = {
        new Label(40, 94, Anchor.STAR, 9, 10, "%TYPE_FACT%"),
        new Label(98, 150, Anchor.MIRROR, 9, -17,
            "A star engine (Shkadov thruster, 1987): a giant mirror held up by the star's own light. With light escaping only one way, the star is pushed the other way."),
        new Label(154, 204, Anchor.DIAL, -8, 16,
            "A real star engine is slow: a billion years to move a Sun-like star 34,000 light years. So the remote fast-forwards the star's own time. On Earth only seconds go by."),
        new Label(274, 326, Anchor.FAR_HOLE, -10, -15, "TON 618: a real black hole of 40 to 66 billion Suns, about 18 billion light years away."),
        new Label(388, 438, Anchor.RIBBON, -9, 15, "Tidal disruption: the near side is pulled much harder than the far side. %TIDAL%"),
        new Label(448, 526, Anchor.DISC, 9, -24, "Falling matter rubs itself to millions of degrees. TON 618 shines like 140 trillion Suns."),
        new Label(538, 606, Anchor.NEAR_JET, -9, 6,
            "Magnetic fields, wound up by the spinning hole, fling matter out of both poles at over 99% of the speed of light."),
        new Label(612, 662, Anchor.HOLE, -10, 16,
            "A quasar seen straight down its jet is a blazar. In 2011 a shredded star's jet pointed at Earth: Swift J1644+57."),
        new Label(692, 742, Anchor.SECOND_SUN, -9, -13,
            "Beaming: light from a jet aimed at you is boosted thousands of times. From its real distance TON 618 is far too faint to see by eye."),
        // (the previz's label here, on knots that seem to outrun light, gave way to one about what is on screen: there
        // is no calm moment for it while the jet comes in)
        new Label(800, 828, Anchor.CRATER, -9, -16,
            "The strongest quasar jets carry, every second, about as much energy as the Sun gives off in 100,000 years."),
        new Label(838, 886, Anchor.DOME, 10, -12,
            "A radio lobe: where a jet ploughs into gas it blows a bubble of hot plasma that glows in radio waves. Real ones are bigger than galaxies."),
        new Label(892, 942, Anchor.CRYSTALS, 10, -15,
            "Frozen light is made up. But light has been stopped: in 2013, for one minute, inside a crystal in a lab.")};

    static String tidal(QuasarStars.Type type) {
        return type == QuasarStars.Type.RED_SUPERGIANT ? "A puffy supergiant really would be torn apart outside a hole this big."
            : "In truth a hole this big swallows a star this small whole. Smaller holes shred them.";
    }

    /** 10^log as "m.m × 10^{e}" (with the film's raised-power markup). */
    static String sci(float log) {
        int e = (int) Math.floor(log);
        double m = Math.pow(10.0, log - e);
        String ms = String.format(Locale.ROOT, "%.1f", m);
        if (ms.startsWith("10")) {
            ms = "1.0";
            e++;
        }
        return ms + " × 10^{" + e + "}";
    }

    /** The year counter's number: whole years under a million, then powers of ten. */
    static String years(float log) {
        return log < 6F ? String.format(Locale.ROOT, "%,d", Math.round(Math.pow(10.0, log))) : sci(log);
    }

    /** Light years in words: "18.2 billion", "340 million", "12,400". */
    static String lightYears(double ly) {
        if (ly >= 1.0e9) {
            return String.format(Locale.ROOT, "%.1f billion", ly / 1.0e9);
        }
        if (ly >= 1.0e6) {
            return Math.round(ly / 1.0e6) + " million";
        }
        return String.format(Locale.ROOT, "%,d", Math.round(ly));
    }
}
