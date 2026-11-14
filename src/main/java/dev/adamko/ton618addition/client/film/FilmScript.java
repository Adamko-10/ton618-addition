package dev.adamko.ton618addition.client.film;

import java.util.Random;

/**
 * <b>FABLE'S FILE.</b> The Last Light film's timeline and numbers, ported from the approved previz
 * ({@code previz/last-light-previz.html}, {@code previz/NOTES.md}).
 *
 * <p><b>Two clocks</b>, as in the previz: {@code g} is game ticks from the cast (0 to 850, what
 * {@code LastLightCast.tick} gives and what {@code LastLightClock} names), {@code t} is the storyboard's own time (0 to
 * 660) in which every curve here is written. {@link #toSb} and {@link #toGame} turn one into the other through the
 * retime table {@link #RETIME} (straight lines between its points; every point is one of LastLightClock's key ticks).
 */
final class FilmScript {
    /** Storyboard time, game tick. */
    private static final float[][] RETIME = {
        {0, 0}, {50, 65}, {60, 78}, {90, 118}, {150, 200}, {200, 270}, {220, 330}, {262, 385}, {300, 440}, {390, 540},
        {470, 625}, {488, 665}, {500, 680}, {510, 690}, {540, 720}, {560, 745}, {660, 850}};
    static final float GAME_END = 850F;
    static final int STORY_END = 660;
    /** The storyboard's moments used all over the film. */
    static final float S_PRESS = 60F;
    static final float S_RACE = 90F;
    static final float S_COSMOS = 150F;
    static final float S_HUSH = 488F;
    static final float S_BURST = 500F;
    /** The space scene runs on a little past the burst, under the white. */
    static final float S_WORLD = 504F;
    static final float S_CLEAR = 540F;
    /** The camera's last shot ends here (game ticks); RimuLib then blends back to the player's own view by END. */
    static final int LAST_SHOT_END = 836;

    private FilmScript() {
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

    static float easeOut(float u) {
        return 1F - (1F - u) * (1F - u);
    }

    /** Straight lines through {@code k}'s points (x in column {@code xi}, the value in column {@code yi}). */
    private static float keys(float[][] k, float x, int xi, int yi) {
        if (x <= k[0][xi]) {
            return k[0][yi];
        }
        for (int i = 1; i < k.length; i++) {
            if (x <= k[i][xi]) {
                float u = (x - k[i - 1][xi]) / (k[i][xi] - k[i - 1][xi]);
                return lerp(k[i - 1][yi], k[i][yi], u);
            }
        }
        return k[k.length - 1][yi];
    }

    static float keys(float[][] k, float x) {
        return keys(k, x, 0, 1);
    }

    static float toSb(float game) {
        return keys(RETIME, game, 1, 0);
    }

    static float toGame(float story) {
        return keys(RETIME, story, 0, 1);
    }

    /** A running total of {@code rate} per storyboard tick, read back at any time. */
    private static float[] integrate(java.util.function.DoubleUnaryOperator rate) {
        float[] sum = new float[STORY_END + 3];
        for (int i = 1; i < sum.length; i++) {
            sum[i] = sum[i - 1] + (float) rate.applyAsDouble(i - 1);
        }
        return sum;
    }

    private static float read(float[] table, float t) {
        float x = Math.max(0F, Math.min(t, STORY_END));
        int i = (int) x;
        return lerp(table[i], table[i + 1], x - i);
    }

    // ---- the film's numbers (storyboard time) --------------------------------------------------------------------------

    private static final float[][] YEARS = {
        {90, 0}, {120, 3.5F}, {150, 9}, {185, 11}, {200, 12}, {220, 12.6F}, {256, 14}, {300, 40}, {390, 90}, {440, 99},
        {470, 99.9F}, {500, 100}};
    private static final float[][] SPACE_K = {{150, 0.431F}, {200, -18.03F}, {220, -21}, {300, -30}};
    /** 66 billion Suns in kg, and one Sun (log10). */
    static final float LOG_M0 = 41.118F;
    static final float LOG_SUN = 30.2986F;
    private static final float[][] MASS_END = {{470, LOG_M0 - 0.8239F}, {488, 22}, {498, 6}, {500, 0}};
    private static final float[][] HEAT = {{300, 0}, {390, 0.18F}, {470, 0.72F}, {488, 0.95F}, {500, 1}};
    private static final float[][] GLOW = {{300, 0}, {390, 0.2F}, {470, 1.0F}, {488, 1.6F}, {494, 0.6F}, {497, 0.5F}, {500, 3}};
    private static final float[] PHASE = integrate(t -> t < 90 ? 0.006 : t < 150 ? 0.006 * Math.pow(18.0, (t - 90) / 60.0) : 0.108);
    private static final float[] SKY = integrate(FilmScript::skyTurn);

    /** log10 of the years gone by. */
    static float logYears(float t) {
        return keys(YEARS, t);
    }

    /** log10 of the temperature of space, in kelvin. */
    static float logSpaceK(float t) {
        return keys(SPACE_K, t);
    }

    /** How big the hole is drawn, 1 at full size. */
    static float holeScale(float t) {
        if (t < 390F) {
            return 1F;
        }
        if (t < 470F) {
            return 1F - 0.85F * (float) Math.pow((t - 390F) / 80F, 1.6);
        }
        return Math.max(0.004F, 0.15F * (float) Math.pow(Math.max(0F, 1F - (t - 470F) / 30F), 1.5));
    }

    /** log10 of the hole's mass in kg: it follows the drawn size, then dives to nothing at the burst. */
    static float logMassKg(float t) {
        return t <= 470F ? LOG_M0 + (float) Math.log10(holeScale(t)) : keys(MASS_END, t);
    }

    /** log10 of the hole's Hawking temperature in kelvin: T = 1.227e23 K / M(kg). */
    static float logHoleK(float t) {
        return 23.089F - logMassKg(t);
    }

    /** How far the hole's disk has turned (radians). */
    static float phase(float t) {
        return read(PHASE, t);
    }

    /** How fast the sky wheels (radians a storyboard tick). */
    static double skyTurn(double t) {
        return t < 150 ? 0.0 : lerp(0.012F, 0.03F, lin(150F, 230F, (float) t));
    }

    /** How far the sky has wheeled round (radians). */
    static float skyRot(float t) {
        return read(SKY, t);
    }

    /** The Hawking glow's heat, 0 (dull red) to 1 (blue-white), and its strength. */
    static float heat(float t) {
        return keys(HEAT, t);
    }

    static float glow(float t) {
        return keys(GLOW, t);
    }

    private static final float[][] HEAT_RGB = {{0F, 0.9F, 0.2F, 0.05F}, {0.4F, 1F, 0.6F, 0.2F}, {0.7F, 1F, 0.95F, 0.85F}, {1F, 0.72F, 0.86F, 1F}};

    /** The glow's colour at heat {@code h}. */
    static float[] heatRgb(float h) {
        for (int i = 1; i < HEAT_RGB.length; i++) {
            if (h <= HEAT_RGB[i][0]) {
                float u = (h - HEAT_RGB[i - 1][0]) / (HEAT_RGB[i][0] - HEAT_RGB[i - 1][0]);
                return new float[] {lerp(HEAT_RGB[i - 1][1], HEAT_RGB[i][1], u), lerp(HEAT_RGB[i - 1][2], HEAT_RGB[i][2], u),
                    lerp(HEAT_RGB[i - 1][3], HEAT_RGB[i][3], u)};
            }
        }
        return new float[] {HEAT_RGB[3][1], HEAT_RGB[3][2], HEAT_RGB[3][3]};
    }

    /** The white over everything: up at the burst, solid until CLEAR, then gone as the returning wave takes over. */
    static float white(float t) {
        if (t < 498F) {
            return 0F;
        }
        if (t < 501F) {
            float u = (t - 498F) / 3F;
            return u * u;
        }
        return t < S_CLEAR ? 1F : 1F - lin(S_CLEAR, 546F, t);
    }

    /** The returning wave's radius in blocks, from CLEAR on (the world is there inside it, white outside). */
    static float returnRadius(float t) {
        return 5F + 120F * (float) Math.pow(lin(S_CLEAR, 592F, t), 1.6);
    }

    /** How white the far side of the returning wave still is. */
    static float returnWhite(float t) {
        return 1F - ss(574F, 602F, t);
    }

    // ---- the Hawking pairs (shared by the picture and the sound) ----------------------------------------------------------

    static final int PAIRS = 1700;
    /** When each pair appears (storyboard time, rising), how long it lasts, where on the horizon (radians). */
    static final float[] PAIR_BORN = new float[PAIRS];
    static final float[] PAIR_LIFE = new float[PAIRS];
    static final float[] PAIR_ANGLE = new float[PAIRS];

    static {
        Random r = new Random(1998L);
        for (int i = 0; i < PAIRS; i++) {
            float f = i / (float) PAIRS;
            float born = f < 0.16F ? 302F + 86F * (float) Math.sqrt(f / 0.16F) : 390F + 97F * (float) Math.pow((f - 0.16F) / 0.84F, 0.55);
            PAIR_BORN[i] = born;
            PAIR_LIFE[i] = lerp(30F, 12F, lin(300F, 488F, born)) * (0.8F + 0.4F * r.nextFloat());
            float bell = (r.nextFloat() + r.nextFloat() + r.nextFloat() - 1.5F) * 0.9F;
            // in the close shot most of them sit on the limb the camera watches
            PAIR_ANGLE[i] = born < 392F && r.nextFloat() < 0.68F ? 0.12F + bell * 0.55F : r.nextFloat() * 6.2832F;
        }
    }

    // ---- words --------------------------------------------------------------------------------------------------------

    /** A caption over the lower bar: from, to (storyboard time), the caption, the line under it. */
    record Caption(float from, float to, String title, String detail) {
    }

    /** {@code ^{...}} in any of the film's texts is drawn raised and small (a power of ten). */
    static final Caption[] CAPTIONS = {
        new Caption(4, 48, "AFTER TON 618", "the world: devoured"),
        new Caption(54, 88, "LAST LIGHT", "armed"),
        new Caption(154, 198, "THE UNIVERSE COOLS", "space is warmer than the hole: it still grows"),
        new Caption(200, 218, "COLDER THAN THE HOLE", "from here on, TON618 loses mass"),
        new Caption(224, 260, "THE LAST STARS", "about 10^{14} years"),
        new Caption(264, 298, "THE DEGENERATE ERA", "nothing left to fall in"),
        new Caption(304, 388, "THE BLACK HOLE ERA", "Hawking radiation"),
        new Caption(394, 468, "EVAPORATING", "smaller · hotter · faster"),
        new Caption(472, 498, "A GOOGOL OF YEARS", "10^{100}"),
        new Caption(548, 610, "THE WORLD RETURNS", "what fell in comes back out")};

    /** What an educational label points at. */
    enum Anchor {
        /** TON618 in the dark's sky (shot 1's framing). */
        SKY_HOLE,
        /** Our own hole in the space scene, {@code reach} shadow radii to the right of its centre. */
        HOLE,
        /** A fixed spot on the screen ({@code x}, {@code y} in hundredths of the screen). */
        SCREEN
    }

    /**
     * An educational label: from, to (storyboard time), what it points at, where its box sits from there (hundredths of
     * the screen's width and height), its text.
     */
    record Label(float from, float to, Anchor anchor, float reach, float x, float y, float offX, float offY, String text) {
    }

    static final Label[] LABELS = {
        new Label(8, 48, Anchor.SKY_HOLE, 1F, 52, 39, 9, -16, "A real black hole: 40 to 66 billion Suns."),
        new Label(154, 218, Anchor.HOLE, 1F, 0, 0, -10, 12,
            "Hawking temperature: about 10^{-18} K. Space is 2.7 K today, so it still grows. It shrinks only once space is colder."),
        new Label(224, 260, Anchor.SCREEN, 0F, 24, 31, 5, 7, "About 10^{14} years: the last red dwarf stars burn out."),
        new Label(264, 298, Anchor.HOLE, 1.6F, 0, 0, 10, -14, "Left over: white dwarfs, neutron stars, black holes."),
        new Label(304, 388, Anchor.HOLE, 1.02F, 0, 0, -12, 14,
            "Hawking radiation: quantum particle pairs appear at the horizon. One falls in, one escapes, and the hole loses mass."),
        new Label(394, 468, Anchor.HOLE, 1.1F, 0, 0, 11, 13, "Smaller means hotter means faster: temperature goes as 1 / mass."),
        new Label(472, 497, Anchor.HOLE, 0F, 0, 0, -11, -15, "About 10^{100} years. The end is a burst of gamma rays."),
        new Label(552, 642, Anchor.SCREEN, 0F, 52, 60, 10, -22,
            "The information paradox: does what fell in come back out in the radiation? Still debated. Here, it does.")};

    /** 10^log as "m.m x 10^{e}" (with the film's raised-power markup). */
    static String sci(float log) {
        int e = (int) Math.floor(log);
        double m = Math.pow(10.0, log - e);
        String ms = String.format(java.util.Locale.ROOT, "%.1f", m);
        if (ms.startsWith("10")) {
            ms = "1.0";
            e++;
        }
        return ms + " × 10^{" + e + "}";
    }

    /** The year counter's number: whole years under a million, then powers of ten. */
    static String years(float log) {
        return log < 6F ? String.format(java.util.Locale.ROOT, "%,d", Math.round(Math.pow(10.0, log))) : sci(log);
    }
}
