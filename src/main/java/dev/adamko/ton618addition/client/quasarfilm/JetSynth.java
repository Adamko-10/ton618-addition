package dev.adamko.ton618addition.client.quasarfilm;

import static dev.adamko.ton618addition.client.quasarfilm.JetScript.keys;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lin;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.ss;

import java.util.Random;

/**
 * <b>FABLE'S FILE.</b> Makes the film's soundtrack: about 50 s of stereo sound built from plain oscillators and filtered
 * noise, every cue at its tick (the previz's "sound sketch" grown up; {@code previz/quasar-jets/NOTES.md}, "Sounds
 * planned"). Nothing here touches the game, so it can be run and measured on its own. {@link JetSound} plays what it
 * makes. The same track for every star.
 *
 * <p>The sounds of the world part (the jet arriving, the lobe, its pop, the crystals) are in the track too, for the
 * players who get the film: the mod plays no world sounds for those yet.
 */
final class JetSynth {
    static final int RATE = 44100;

    private JetSynth() {
    }

    private static final float[][] DRONE = {{0, 0.04F}, {80, 0.04F}, {90, 0.13F}, {330, 0.13F}, {436, 0.16F}, {439, 0.015F}, {441, 0.18F},
        {665, 0.18F}, {680, 0.03F}, {976, 0.03F}, {994, 0}};
    /** Music, placeholders until the film's music is chosen: M1 (150 to the crash at 665) and M2 (from 895). */
    private static final float[][] PAD = {{150, 0}, {200, 0.03F}, {330, 0.05F}, {438, 0.05F}, {441, 0.08F}, {600, 0.06F}, {662, 0.05F}, {666, 0}};
    private static final float[][] PAD_BACK = {{893, 0}, {905, 0.05F}, {972, 0.04F}, {994, 0}};

    /** The whole track, left and right samples in turn. */
    static short[] render() {
        int n = (int) (JetScript.END / 20F * RATE);
        float[] l = new float[n];
        float[] r = new float[n];
        Random random = new Random(1987L);

        // ---- held sounds
        // the low drone under everything: the star's roar, then the quasar's (a beat of silence before it ignites)
        voice(l, r, t -> keys(DRONE, t), t -> 43F, 0.6F, 3, 0.5F);
        // into the cut: a rising whoosh
        hiss(l, r, random, t -> t > 60F && t < 84F ? 0.2F * lin(60F, 80F, t) * (1F - lin(80F, 84F, t)) : 0F, t -> 500F + 2600F * lin(60F, 80F, t), 0.7F);
        // the mirror opening: a thin shimmer
        hiss(l, r, random, t -> t > 92F && t < 152F ? 0.03F * lin(92F, 104F, t) * (1F - lin(146F, 152F, t)) : 0F, t -> 6500F, 3F);
        // the engine's hum and the wind of stars, both rising with the speed
        voice(l, r, t -> t > 100F && t < 332F ? (0.04F + 0.05F * JetScript.speed(t) / 60F) * (1F - lin(326F, 332F, t)) : 0F,
            t -> 70F + 160F * JetScript.speed(t) / 60F, 0.3F, 1, 0F);
        hiss(l, r, random, t -> t > 150F && t < 332F ? 0.02F + 0.2F * JetScript.speed(t) / 60F : 0F, t -> 500F + 2500F * JetScript.speed(t) / 60F, 0.6F);
        // the star stretching: a long low groan sliding down
        voice(l, r, t -> t > 350F && t < 394F ? 0.1F * lin(350F, 360F, t) * (1F - lin(384F, 394F, t)) : 0F, t -> 82F - 36F * lin(350F, 392F, t), 0.9F, 2, 1.2F);
        // the ribbon meeting the disc: a hiss
        hiss(l, r, random, t -> t > 350F && t < 440F ? 0.05F * lin(350F, 430F, t) : 0F, t -> 4200F, 0.8F);
        // the jets: a tearing beam whose pitch bends up as one turns toward us, a roar of plasma round it
        voice(l, r, t -> t > 530F && t < 668F ? 0.1F * jets(t) : 0F, t -> 110F * (float) Math.pow(2.0, ss(610F, 665F, t)), 0.8F, 3, 0F);
        hiss(l, r, random, t -> t > 530F && t < 670F ? 0.2F * jets(t) * (0.8F + 0.2F * (float) Math.sin(t * 1.3)) : 0F,
            t -> 1200F + 2600F * ss(610F, 665F, t), 0.5F);
        // after the crash: a high tone ringing out, a faint electric hum from the new light in the sky
        voice(l, r, t -> t > 668F && t < 750F ? 0.014F * (1F - lin(690F, 750F, t)) : 0F, t -> 1568F, 1.5F, 1, 0F);
        voice(l, r, t -> t > 684F && t < 926F ? 0.016F * lin(684F, 704F, t) * (1F - lin(900F, 926F, t)) : 0F, t -> 120F, 0.7F, 2, 0F);
        // the jet arriving: a thin whine from far off, then its steady hum
        voice(l, r, t -> t > 745F && t < 896F ? 0.028F * lin(745F, 795F, t) * (1F - lin(888F, 896F, t)) : 0F, t -> 500F + 900F * lin(745F, 795F, t), 0.4F, 1, 0F);
        voice(l, r, t -> t > 760F && t < 896F ? 0.05F * lin(760F, 795F, t) * (1F - lin(888F, 896F, t)) : 0F, t -> 110F, 0.6F, 3, 0F);
        // the lobe swelling: a rising, crackling drone
        voice(l, r, t -> t > 830F && t < 890F ? 0.03F + 0.1F * lin(830F, 890F, t) : 0F, t -> 90F * (float) Math.pow(2.0, lin(830F, 890F, t) * 2.0), 0.5F, 2, 0F);
        hiss(l, r, random, t -> t > 830F && t < 890F ? 0.14F * lin(830F, 888F, t) : 0F, t -> 1400F, 0.5F);
        hiss(l, r, random, t -> t > 830F && t < 890F ? 0.07F * lin(830F, 890F, t) * (0.5F + 0.5F * (float) Math.sin(t * t * 0.05)) : 0F, t -> 5200F, 1.2F);
        // the music
        for (float note : new float[] {146.83F, 220F, 293.66F}) {
            voice(l, r, t -> keys(PAD, t), t -> note, 0.5F, 1, 0.4F);
        }
        for (float note : new float[] {196F, 246.94F, 369.99F}) {
            voice(l, r, t -> keys(PAD_BACK, t), t -> note, 0.5F, 1, 0.4F);
        }

        // ---- struck sounds
        // the mark: a soft thud and a ping
        tone(l, r, at(0.5F), 0.5, 60F, 60F, 0.5F, 0F);
        tone(l, r, at(0.5F), 0.5, 1320F, 1320F, 0.14F, 0F);
        // the reticle closing in, then the lock's chime
        for (float t = 24F; t < 40F; t += 3F) {
            tone(l, r, at(t), 0.05, 1200F + t * 20F, 0F, 0.06F, 0F);
        }
        tone(l, r, at(40F), 0.6, 1760F, 1760F, 0.16F, 0F);
        tone(l, r, at(40F), 0.5, 2637F, 2637F, 0.07F, 0F);
        noise(l, r, random, at(78F), 0.6, 0.35F, 2600F);
        // the mirror's panels clicking open
        for (float t = 94F; t < 150F; t += 5F) {
            tone(l, r, at(t), 0.08, 900F + random.nextFloat() * 900F, 0F, 0.05F, random.nextFloat() * 1.6F - 0.8F);
            noise(l, r, random, at(t), 0.03, 0.12F, 6000F);
        }
        // the push takes hold: a deep thump
        tone(l, r, at(150F), 2.0, 58F, 30F, 0.8F, 0F);
        // the mirror cracking away
        noise(l, r, random, at(336F), 0.25, 0.3F, 5000F);
        tone(l, r, at(336F), 0.4, 180F, 120F, 0.14F, 0.3F);
        noise(l, r, random, at(344F), 0.2, 0.2F, 4000F);
        // ignition: a deep boom and a bright rush
        tone(l, r, at(440F), 2.8, 66F, 26F, 0.95F, 0F);
        noise(l, r, random, at(440F), 2.4, 0.6F, 7000F);
        // a bell on the title
        tone(l, r, at(455F), 2.6, 293.66F, 293.66F, 0.2F, -0.2F);
        tone(l, r, at(455F), 2.2, 587.33F, 587.33F, 0.1F, 0.2F);
        // the jets launch: a double crack
        noise(l, r, random, at(530F), 0.12, 0.7F, 8000F);
        noise(l, r, random, at(533F), 0.12, 0.55F, 8000F);
        tone(l, r, at(530F), 1.6, 48F, 48F, 0.8F, 0F);
        // looking down the jet: a white crash, a high ring left hanging
        noise(l, r, random, at(665F), 2.8, 0.7F, 9000F);
        tone(l, r, at(665F), 3.0, 2093F, 2093F, 0.05F, 0F);
        // the jet lands
        tone(l, r, at(795F), 1.2, 52F, 52F, 0.6F, 0F);
        noise(l, r, random, at(795F), 0.5, 0.3F, 3000F);
        // soft pops as things are lifted into it
        for (float t = 776F; t < 826F; t += 2F + random.nextFloat() * 3F) {
            tone(l, r, at(t), 0.1, 300F + random.nextFloat() * 500F, 0F, 0.05F, random.nextFloat() * 1.6F - 0.8F);
        }
        // the pop: sharp, a deep drop under it, a ring of wind running out
        noise(l, r, random, at(890F), 0.1, 0.9F, 9000F);
        noise(l, r, random, at(890F), 1.3, 0.8F, 6000F);
        tone(l, r, at(890F), 2.0, 90F, 30F, 0.9F, 0F);
        noise(l, r, random, at(892F), 1.8, 0.3F, 900F);
        // glassy chimes as the crystals grow
        for (float t = 893F; t < 922F; t += 3F) {
            tone(l, r, at(t), 0.5, 1800F + random.nextFloat() * 2200F, 0F, 0.03F, random.nextFloat() * 1.6F - 0.8F);
        }

        short[] pcm = new short[n * 2];
        for (int i = 0; i < n; i++) {
            pcm[i * 2] = (short) (Math.tanh(l[i] * 0.75) * 30000.0);
            pcm[i * 2 + 1] = (short) (Math.tanh(r[i] * 0.75) * 30000.0);
        }
        return pcm;
    }

    /** How strongly the jets sound: in over their launch, cut by the crash. */
    private static float jets(float t) {
        return lin(530F, 560F, t) * (1F - lin(662F, 670F, t));
    }

    /** Tick {@code t} in seconds. */
    private static double at(float t) {
        return t / 20.0;
    }

    /** Something that changes over the film, by tick. */
    private interface Curve {
        float at(float t);
    }

    /** A curve read once a control step (an eighth of a game tick). */
    private static float[] control(Curve curve, int n) {
        int step = RATE / 160;
        float[] values = new float[n / step + 2];
        for (int i = 0; i < values.length; i++) {
            values[i] = curve.at(i * step / (float) RATE * 20F);
        }
        return values;
    }

    /**
     * A held tone: {@code harmonics} sine partials falling off in loudness, the right channel a shade sharp so it
     * beats slowly ({@code detune} Hz), a slow shimmer of {@code vibrato} Hz depth.
     */
    private static void voice(float[] l, float[] r, Curve gain, Curve freq, float detune, int harmonics, float vibrato) {
        int n = l.length;
        int step = RATE / 160;
        float[] gains = control(gain, n);
        float[] freqs = control(freq, n);
        double phaseL = 0.0;
        double phaseR = 0.0;
        for (int i = 0; i < n; i++) {
            int c = i / step;
            float u = (i % step) / (float) step;
            float g = gains[c] + (gains[c + 1] - gains[c]) * u;
            float f = freqs[c] + (freqs[c + 1] - freqs[c]) * u;
            double wobble = vibrato > 0F ? Math.sin(i * (2.0 * Math.PI * 4.3 / RATE)) * vibrato : 0.0;
            phaseL += 2.0 * Math.PI * (f + wobble) / RATE;
            phaseR += 2.0 * Math.PI * (f + detune - wobble) / RATE;
            if (g <= 0.0001F) {
                continue;
            }
            double a = 0.0;
            double b = 0.0;
            for (int h = 1; h <= harmonics; h++) {
                a += Math.sin(phaseL * h) / h;
                b += Math.sin(phaseR * h) / h;
            }
            l[i] += (float) a * g;
            r[i] += (float) b * g;
        }
    }

    /** Held noise through a band that can move ({@code centre} Hz, {@code q} how narrow). */
    private static void hiss(float[] l, float[] r, Random random, Curve gain, Curve centre, float q) {
        int n = l.length;
        int step = RATE / 160;
        float[] gains = control(gain, n);
        float[] centres = control(centre, n);
        double b0 = 0.0;
        double a1 = 0.0;
        double a2 = 0.0;
        double x1l = 0.0;
        double x2l = 0.0;
        double y1l = 0.0;
        double y2l = 0.0;
        double x1r = 0.0;
        double x2r = 0.0;
        double y1r = 0.0;
        double y2r = 0.0;
        for (int i = 0; i < n; i++) {
            int c = i / step;
            if (i % step == 0) {
                double w = 2.0 * Math.PI * Math.min(centres[c], RATE * 0.45F) / RATE;
                double alpha = Math.sin(w) / (2.0 * q);
                double a0 = 1.0 + alpha;
                b0 = alpha / a0;
                a1 = -2.0 * Math.cos(w) / a0;
                a2 = (1.0 - alpha) / a0;
            }
            float g = gains[c];
            if (g <= 0.0001F && gains[c + 1] <= 0.0001F) {
                x1l = x2l = y1l = y2l = x1r = x2r = y1r = y2r = 0.0;
                continue;
            }
            g += (gains[c + 1] - g) * ((i % step) / (float) step);
            double xl = random.nextFloat() * 2.0 - 1.0;
            double xr = random.nextFloat() * 2.0 - 1.0;
            double yl = b0 * (xl - x2l) - a1 * y1l - a2 * y2l;
            double yr = b0 * (xr - x2r) - a1 * y1r - a2 * y2r;
            x2l = x1l;
            x1l = xl;
            y2l = y1l;
            y1l = yl;
            x2r = x1r;
            x1r = xr;
            y2r = y1r;
            y1r = yr;
            l[i] += (float) yl * g * 2F;
            r[i] += (float) yr * g * 2F;
        }
    }

    /**
     * A struck tone starting at {@code at} seconds: from {@code f0} sliding to {@code f1} Hz (0: no slide), dying away
     * over {@code seconds}; {@code pan} -1 left to 1 right.
     */
    private static void tone(float[] l, float[] r, double at, double seconds, float f0, float f1, float gain, float pan) {
        int from = (int) (at * RATE);
        int count = (int) (seconds * RATE);
        double phase = 0.0;
        float left = gain * Math.min(1F, 1F - pan);
        float right = gain * Math.min(1F, 1F + pan);
        for (int i = 0; i < count && from + i < l.length; i++) {
            if (from + i < 0) {
                continue;
            }
            double u = i / (double) count;
            double f = f1 > 0F && f1 != f0 ? f0 * Math.pow(f1 / (double) f0, Math.min(1.0, u * 1.1)) : f0;
            phase += 2.0 * Math.PI * f / RATE;
            // a fast attack, then dying away to nothing
            double env = Math.min(1.0, i / (0.004 * RATE)) * Math.pow(0.003, u) * (1.0 - u);
            float s = (float) (Math.sin(phase) * env);
            l[from + i] += s * left;
            r[from + i] += s * right;
        }
    }

    /** A burst of noise at {@code at} seconds, dying away over {@code seconds}, dulled above {@code cutoff} Hz. */
    private static void noise(float[] l, float[] r, Random random, double at, double seconds, float gain, float cutoff) {
        int from = (int) (at * RATE);
        int count = (int) (seconds * RATE);
        double k = 1.0 - Math.exp(-2.0 * Math.PI * cutoff / RATE);
        double sl = 0.0;
        double sr = 0.0;
        for (int i = 0; i < count && from + i < l.length; i++) {
            if (from + i < 0) {
                continue;
            }
            double u = i / (double) count;
            double env = Math.min(1.0, i / (0.002 * RATE)) * Math.pow(0.003, u) * (1.0 - u);
            sl += (random.nextFloat() * 2.0 - 1.0 - sl) * k;
            sr += (random.nextFloat() * 2.0 - 1.0 - sr) * k;
            l[from + i] += (float) (sl * env) * gain;
            r[from + i] += (float) (sr * env) * gain;
        }
    }
}
