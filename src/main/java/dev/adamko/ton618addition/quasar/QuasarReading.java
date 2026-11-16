package dev.adamko.ton618addition.quasar;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>FABLE'S FILE (data only).</b> Educational mode's reading windows for TA-02 · Quasar Jets (Adam, 2026-11-16: more time
 * to read with educational mode on, slowing only the parts with text). Used only when the caster had educational mode on;
 * then, for everyone watching that strike, story time runs slower inside each window. {@code ReadingTime.story} and
 * {@code ReadingTime.real} turn real ticks since the cast into story ticks and back (server and client alike).
 *
 * <p><b>How they are placed</b> (Fable, the quality redo). Each of the film's twelve educational labels gets one
 * <b>hold</b>: a short stretch inside its time on screen where the picture is calm (something has just finished, the
 * next thing hasn't begun), slowed hard, so the label stays up about 2 to 3 seconds longer while the action itself
 * keeps its pace. With all of them the strike is about 27 seconds longer (about 77 s for 50 s).
 *
 * <p><b>Kept free on purpose</b>, because sounds there must end exactly on an event: story ticks 56 to 92 (the zoom
 * and its cut), 620 to 668 (the jet's turn toward us and the flood), <b>742 to 800</b> (the jet coming in and landing)
 * and <b>866 to 900</b> (the lobe's last second and its pop). See {@code previz/quasar-jets/SOUND-CUES.md}.
 */
public final class QuasarReading {
    /**
     * {@code {from, to, slow}}: between story ticks {@code from} and {@code to}, story time runs at {@code 1 / slow} of real
     * time ({@code slow} >= 1; 2 = the window lasts twice as long). In story order, not overlapping. Empty = no slowing.
     */
    public static final float[][] WINDOWS = holds(
        // the star's type: the reticle has just locked, the zoom hasn't begun (+2.3 s)
        42, 54, 5,
        // the star engine: the mirror is whole and lit, the fast-forward hasn't begun (+2.7 s)
        134, 148, 5,
        // "a real star engine is slow": the dial's hand still creeps, the star barely moves (+2.4 s)
        152, 162, 6,
        // TON 618: the flight at full speed, TON618 growing ahead (+2.3 s)
        290, 314, 3,
        // tidal disruption: the ribbon is winding into the disc, the disc hasn't lit (+2.3 s)
        418, 434, 4,
        // the disc's heat: under the title card (+1.9 s)
        484, 504, 3,
        // how jets are made: the jets have reached their full length (+2 s)
        590, 604, 4,
        // blazar: the turn has barely begun (+1.5 s)
        612, 620, 5,
        // beaming: the second sun over the land (+2.3 s)
        706, 738, 2.5F,
        // the jet's energy: the jet stands on the ground, the dust is settling (+2.3 s)
        806, 826, 3.4F,
        // the radio lobe: swelling (+2.1 s)
        842, 864, 3,
        // frozen light: the crystals growing, the second sun fading (+2.1 s)
        904, 926, 3);

    private QuasarReading() {
    }

    /**
     * Holds as windows: each {@code from, to, slow} gets a two-tick step at half its slowing at either end, so the
     * picture eases into and out of it rather than jerking.
     */
    private static float[][] holds(float... holds) {
        List<float[]> windows = new ArrayList<>();
        for (int i = 0; i + 2 < holds.length; i += 3) {
            float from = holds[i];
            float to = holds[i + 1];
            float slow = holds[i + 2];
            float step = Math.max(1F, (1F + slow) * 0.5F);
            if (to - from > 6F && slow > 2F) {
                windows.add(new float[] {from, from + 2F, step});
                windows.add(new float[] {from + 2F, to - 2F, slow});
                windows.add(new float[] {to - 2F, to, step});
            } else {
                windows.add(new float[] {from, to, slow});
            }
        }
        return windows.toArray(new float[0][]);
    }
}
