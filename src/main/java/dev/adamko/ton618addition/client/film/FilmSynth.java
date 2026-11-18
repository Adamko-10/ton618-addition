package dev.adamko.ton618addition.client.film;

import static dev.adamko.ton618addition.client.film.FilmScript.lin;

import dev.adamko.ton618addition.ReadingTime;
import java.util.Random;

/**
 * <b>FABLE'S FILE.</b> Makes the film's soundtrack: 42.5 s of stereo sound built from plain oscillators and filtered
 * noise, every cue at its place on the film's clock (the previz's "sound sketch" grown up). Nothing here touches the
 * game, so it can be run and measured on its own. {@link FilmSound} plays what it makes.
 */
final class FilmSynth {
    static final int RATE = 44100;

    private FilmSynth() {
    }


    private static final float[][] DRONE = {{0, 0.2F}, {150, 0.2F}, {162, 0.1F}, {470, 0.1F}, {486, 0.14F}, {488, 0F}};
    private static final float[][] PAD = {{104, 0}, {130, 0.045F}, {220, 0.045F}, {280, 0.015F}, {310, 0.03F}, {470, 0.07F}, {487, 0.08F}, {488, 0}};
    private static final float[][] PAD_BACK = {{538, 0}, {552, 0.055F}, {620, 0.05F}, {658, 0}};
    private static final float[][] WIND = {{535, 0}, {560, 0.05F}, {640, 0.04F}, {660, 0}};

    /** This making's reading windows (one track is made at a time). */
    private static float[][] windows = {};

    /** When storyboard time {@code story} comes, in seconds of real time since the cast. */
    private static double at(float story) {
        return ReadingTime.real(windows, FilmScript.toGame(story)) / 20.0;
    }

    /**
     * The whole track, left and right samples in turn, for a cast with these reading windows
     * ({@code lastlight/LastLightReading}, or none): with educational mode's slow-down the held sounds last longer and
     * the struck ones land where their moment falls in real time, so the track is as long as that cast really takes.
     */
    static synchronized short[] render(float[][] reading) {
        windows = reading;
        int n = (int) (ReadingTime.real(reading, FilmScript.GAME_END) / 20F * RATE);
        float[] l = new float[n];
        float[] r = new float[n];
        Random random = new Random(618L);

        // the dark's low drone, quieter under the space shots, gone at the hush
        voice(l, r, t -> FilmScript.keys(DRONE, t), t -> 41F, 0.35F, 3, 0.7F);
        // the charge after the press: a hum climbing
        voice(l, r, t -> t > 62F && t < 90F ? 0.1F * lin(62F, 90F, t) : 0F, t -> 90F + 130F * lin(62F, 90F, t) * lin(62F, 90F, t), 0.2F, 1, 0F);
        // time racing: a whir climbing three octaves, wind behind the wave
        voice(l, r, t -> t > 90F && t < 150F ? 0.05F * lin(90F, 150F, t) : 0F, t -> 200F * (float) Math.pow(8.0, lin(90F, 150F, t)), 0.3F, 1, 0F);
        hiss(l, r, random, t -> t > 90F && t < 150F ? 0.12F * (1F - 0.5F * lin(90F, 150F, t)) : 0F, t -> 700F, 0.6F);
        // the evaporation: a tone rising four octaves, a hiss opening up
        voice(l, r, t -> t > 390F && t < 488F ? 0.02F + 0.09F * lin(390F, 488F, t) * lin(390F, 488F, t) : 0F,
            t -> 110F * (float) Math.pow(2.0, lin(390F, 488F, t) * 4.0), 0.4F, 2, 0F);
        hiss(l, r, random, t -> t > 330F && t < 488F ? 0.22F * lin(330F, 488F, t) * lin(330F, 488F, t) : 0F, t -> 700F + 5500F * lin(330F, 488F, t), 0.8F);
        // music, placeholders until the film's music is chosen: a minor pad (M1), cut dead at the hush; a major one (M2)
        // when the world is back
        float[] minor = {220F, 261.63F, 329.63F};
        for (int i = 0; i < minor.length; i++) {
            boolean root = i == 0;
            voice(l, r, t -> !root && t > 250F && t < 300F ? 0F : FilmScript.keys(PAD, t), constant(minor[i]), 0.5F, 1, 0.4F);
        }
        for (float note : new float[] {261.63F, 329.63F, 392F, 523.25F}) {
            voice(l, r, t -> FilmScript.keys(PAD_BACK, t), constant(note), 0.5F, 1, 0.4F);
        }
        hiss(l, r, random, t -> FilmScript.keys(WIND, t), t -> 500F, 0.5F);

        // the press: a click, a glass ping, a low thump
        double press = at(60F);
        noise(l, r, random, press, 0.05, 0.5F, 5000F);
        tone(l, r, press, 0.5, 1568F, 1568F, 0.18F, 0F);
        tone(l, r, press, 0.5, 55F, 55F, 0.5F, 0F);
        // the time wave: a deep whoomp falling away, air rushing out
        double wave = at(90F);
        tone(l, r, wave, 2.2, 64F, 26F, 0.9F, 0F);
        noise(l, r, random, wave, 1.6, 0.5F, 1800F);
        // clock ticks, faster and faster, into the whir
        for (int k = 1; k <= 26; k++) {
            noise(l, r, random, at(90F + 60F * (float) Math.sqrt(k / 26.0)), 0.03, 0.22F, 6000F);
        }
        // a bell on the title, a whoosh into the cut
        double bell = at(104F);
        tone(l, r, bell, 2.6, 220F, 220F, 0.2F, -0.2F);
        tone(l, r, bell, 2.2, 440F, 440F, 0.1F, 0.2F);
        tone(l, r, bell, 1.6, 660F, 660F, 0.04F, 0F);
        noise(l, r, random, at(147F), 0.5, 0.35F, 2500F);
        // space falls colder than the hole: a long falling sigh
        tone(l, r, at(200F), 2.2, 520F, 90F, 0.09F, 0F);
        // a soft tick for each star going out
        for (float t = 220F; t < 262F; t += 1.1F) {
            tone(l, r, at(t), 0.12, 1800F + random.nextFloat() * 1600F, 0F, 0.03F, random.nextFloat() * 1.6F - 0.8F);
        }
        // a tiny crackle for each pair (not closer together than the ear can tell apart)
        double lastCrackle = -1.0;
        for (int i = 0; i < FilmScript.PAIRS; i++) {
            if (FilmScript.PAIR_BORN[i] >= FilmScript.S_HUSH) {
                break;
            }
            double at = at(FilmScript.PAIR_BORN[i]);
            if (at - lastCrackle < 0.012) {
                continue;
            }
            lastCrackle = at;
            crackle(l, r, at, 2400F + random.nextFloat() * 4200F, 0.035F, random.nextFloat() * 1.6F - 0.8F);
        }
        // the burst, after the hush: a deep boom, a white crash, a high ring left hanging
        double burst = at(FilmScript.S_BURST);
        tone(l, r, burst, 2.6, 70F, 28F, 0.9F, 0F);
        noise(l, r, random, burst, 2.8, 0.7F, 9000F);
        tone(l, r, burst, 3.2, 2093F, 2093F, 0.05F, 0F);
        // the returning wave: a wide whoosh, two warm notes
        double back = at(FilmScript.S_CLEAR);
        noise(l, r, random, back, 2.2, 0.45F, 1400F);
        tone(l, r, back, 2.4, 523.25F, 523.25F, 0.06F, -0.3F);
        tone(l, r, back, 2.4, 783.99F, 783.99F, 0.04F, 0.3F);

        short[] pcm = new short[n * 2];
        for (int i = 0; i < n; i++) {
            pcm[i * 2] = (short) (Math.tanh(l[i] * 0.75) * 30000.0);
            pcm[i * 2 + 1] = (short) (Math.tanh(r[i] * 0.75) * 30000.0);
        }
        return pcm;
    }

    /** Something that changes over the film, by storyboard time. */
    private interface Curve {
        float at(float t);
    }

    private static Curve constant(float value) {
        return t -> value;
    }

    /** A curve read once a control step (an eighth of a game tick), in storyboard time. */
    private static float[] control(Curve curve, int n) {
        int step = RATE / 160;
        float[] values = new float[n / step + 2];
        for (int i = 0; i < values.length; i++) {
            values[i] = curve.at(FilmScript.toSb(ReadingTime.story(windows, i * step / (float) RATE * 20F)));
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

    /** One Hawking pair's crackle: 40 ms of a thin square wave. */
    private static void crackle(float[] l, float[] r, double at, float freq, float gain, float pan) {
        int from = (int) (at * RATE);
        int count = (int) (0.04 * RATE);
        float left = gain * Math.min(1F, 1F - pan);
        float right = gain * Math.min(1F, 1F + pan);
        for (int i = 0; i < count && from + i < l.length; i++) {
            double u = i / (double) count;
            float s = (float) ((Math.sin(2.0 * Math.PI * freq * i / RATE) >= 0.0 ? 1.0 : -1.0) * Math.pow(0.0001, u) * 0.5);
            l[from + i] += s * left;
            r[from + i] += s * right;
        }
    }
}
