package dev.adamko.ton618addition;

/**
 * Educational mode's extra reading time (Adam, 2026-11-16: "more time to read ... when educational mode is enabled",
 * slowing only the parts with text). Turns real ticks since a cast into story ticks and back: inside each reading window
 * ({@code {from, to, slow}} in story ticks, Fable's {@code QuasarReading} / {@code LastLightReading}) story time runs at
 * {@code 1 / slow}; elsewhere at full speed. The server and the clients use the same maths, so all agree where the strike
 * is. With educational mode off a strike uses no windows (story = real).
 */
public final class ReadingTime {
    private ReadingTime() {
    }

    /** The story tick reached {@code real} ticks after the cast. */
    public static float story(float[][] windows, float real) {
        float story = 0F;
        float at = 0F;
        for (float[] w : windows) {
            float slow = Math.max(1F, w[2]);
            float startReal = at + (w[0] - story);
            if (real <= startReal) {
                break;
            }
            float endReal = startReal + (w[1] - w[0]) * slow;
            if (real <= endReal) {
                return w[0] + (real - startReal) / slow;
            }
            story = w[1];
            at = endReal;
        }
        return story + (real - at);
    }

    /** The real ticks after the cast at which story tick {@code story} is reached (the inverse of {@link #story}). */
    public static float real(float[][] windows, float story) {
        float s = 0F;
        float at = 0F;
        for (float[] w : windows) {
            float slow = Math.max(1F, w[2]);
            if (story <= w[0]) {
                break;
            }
            float startReal = at + (w[0] - s);
            if (story <= w[1]) {
                return startReal + (story - w[0]) * slow;
            }
            at = startReal + (w[1] - w[0]) * slow;
            s = w[1];
        }
        return at + (story - s);
    }
}
