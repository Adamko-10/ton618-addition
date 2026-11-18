package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Minecraft 1.20.1's way of building vertices for TA-02 (this branch only). On 1.21 every draw starts a builder over the
 * Tesselator's shared bytes ({@code Tesselator.begin}); on 1.20.1 a builder is begun, filled with
 * {@code vertex().uv().color().endVertex()} and ended into a {@code RenderedBuffer}, so TA-02 keeps a few builders of its
 * own (as the Last Light film's FilmGl does on this branch): a draw may begin a second one before the first is drawn.
 */
public final class Gl1201 {
    private static final BufferBuilder[] POOL = {new BufferBuilder(1 << 16), new BufferBuilder(1 << 16), new BufferBuilder(1 << 16),
        new BufferBuilder(1 << 16)};
    private static int next;

    private Gl1201() {
    }

    /** A builder of the pool, begun (a fresh one if all four are still being filled). */
    public static BufferBuilder begin(VertexFormat.Mode mode, VertexFormat format) {
        for (int i = 0; i < POOL.length; i++) {
            BufferBuilder b = POOL[(next + i) % POOL.length];
            if (!b.building()) {
                next = (next + i + 1) % POOL.length;
                b.begin(mode, format);
                return b;
            }
        }
        BufferBuilder fresh = new BufferBuilder(1 << 16);
        fresh.begin(mode, format);
        return fresh;
    }

    /** {@code own} begun (anything left in it from a broken draw is thrown away first). */
    public static BufferBuilder begin(BufferBuilder own, VertexFormat.Mode mode, VertexFormat format) {
        if (own.building()) {
            own.end().release();
        }
        own.begin(mode, format);
        return own;
    }

    /** After a failed draw: the pool's builders that were left half-filled are emptied (1.21's {@code Tesselator.clear}). */
    public static void clear() {
        for (BufferBuilder b : POOL) {
            if (b.building()) {
                b.end().release();
            }
        }
    }
}
