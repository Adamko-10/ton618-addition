package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

/**
 * TA-02's world effects with a shader pack on (Iris). A pack draws the level again in its own passes and shows nothing
 * a mod's own shader draws there. So, as the SA Series does it ({@code GlowAtlas}), each look of {@code quasar_world.fsh}
 * is painted with that same shader onto one texture before the level is drawn ({@link #paint}, the moving ones every
 * frame, at the strike's own clock: there is only ever one strike), and the effects' quads are then drawn from that
 * texture with Minecraft's glowing-eyes render type ({@link #PACK}), which every pack draws: hidden behind land, lit
 * and blended by the pack (Adam, 2026-11-17: "when your shaders are on ... without them looking out of place").
 *
 * <p>{@link QuasarGl#draw} sends each mesh here instead of drawing it ({@link #emit}): its uv is moved into the tile of
 * its material and variant, its colour times its alpha times the material's strength becomes the vertex colour. Light
 * is only added this way: smoke and the lobe's skin don't darken what is behind them as they do without a pack.
 *
 * <p><b>The sky's things</b> (the named stars, TON618's light, the second sun, the far jet) are drawn from inside the
 * game's own sky drawing instead ({@link #drawSky}, from {@code LevelRendererMixin}), with the game's textured sky shader,
 * which a pack swaps for its own sky program there, as for the sun and the moon: the pack keeps them in its sky, and its
 * clouds and the land are drawn over them (Adam, 2026-11-17: "do whatever would make the stars go behind clouds with and
 * without shaders"). Drawn anywhere else, a pack paints its sky over them. Complementary's sky program squares what it
 * is given, so those looks are painted as their square roots ({@code *_SKY} tiles, {@code Bake} 2).
 *
 * <p><b>Minecraft 1.21.11</b> (Fabric 1.21.11 branch, Opus 2026-11-18, as the SA Series' 1.21.11 {@code GlowAtlas}): the
 * looks are painted with {@link QuasarGl#BAKE} into a texture of our own ({@link Baked}), one render pass a tile, and
 * everything (the sky's things too) is handed to Minecraft as glowing-eyes geometry ({@link #pack()}) from
 * {@code WorldRenderEvents.AFTER_ENTITIES}, each quad both ways round (that render type culls back faces). There is no
 * drawing inside the sky with a pack here: Iris on 1.21.11 has no stage we can reach there, so with a pack the named
 * stars can show over the pack's clouds (as in 1.1.0 before its sky fix). Not tried with Iris on this version.
 */
final class QuasarAtlas {
    private static final int TILE = 192;
    private static final int COLUMNS = 6;
    private static final int ROWS = 5;
    /** Half a texel, in tiles: keeps every lookup inside its own tile. */
    private static final float INSET = 0.5F / TILE;
    private static final Identifier LOCATION = Ton618Addition.id("quasar_atlas");

    private static final int STAR = 0;
    private static final int STAR_LOCKED = 1;
    private static final int SUN = 2;
    private static final int MARK = 3;
    private static final int SHOCK_24 = 4;
    private static final int SHOCK_28 = 5;
    private static final int SHOCK_36 = 6;
    private static final int DUST = 7;
    private static final int DOME = 8;
    private static final int FIREBALL = 9;
    private static final int BEAM = 10;
    /** The round glows, one tile per (tightness, haze) the effects use. */
    private static final float[][] GLOWS = {{2.2f, 0.2f}, {14f, 0.6f}, {40f, 0.45f}, {30f, 0.5f}, {3.5f, 0.3f}, {10f, 0.6f},
        {6f, 0.5f}};
    private static final int GLOW0 = 11;
    /** The sky's looks, painted as square roots (see the class comment); right after the 7 glows (11 to 17). */
    private static final int STAR_SKY = 18;
    private static final int STAR_LOCKED_SKY = 19;
    private static final int SUN_SKY = 20;
    private static final int GLOW_SKY = 21;
    private static final int BEAM_SKY = 22;
    /** The landing's plasma splash and a magnetic arc. */
    private static final int SPLASH = 23;
    private static final int ARC = 24;
    private static final int TILES = 25;

    private static final Baked TEXTURE = new Baked(LOCATION, TILE * COLUMNS, TILE * ROWS);
    private static boolean painted;
    private static final QuasarMesh QUAD = new QuasarMesh("TA-02 Quasar Jets atlas");
    @Nullable
    private static PerspectiveProjectionMatrixBuffer ortho;
    /** While gathering for the pack (QuasarRender.packed): the pose each corner is put through. */
    @Nullable
    static Matrix4fc packPose;

    /**
     * The effects' quads from the painted looks: Minecraft's glowing-eyes render type on this texture (light added,
     * hidden behind land), which every shader pack draws with its own program.
     */
    static RenderType pack() {
        return RenderTypes.eyes(LOCATION);
    }

    private QuasarAtlas() {
    }

    static boolean ready() {
        return TEXTURE.ready() && painted;
    }

    // ---- painting (before the level is drawn: while a pack draws the level, our shader can't draw anything) ----------

    /**
     * Paints the looks: all of them the first time, then the moving ones, at the strike's clock {@code t} (story ticks,
     * or the day's ticks before a strike) and the sky's {@code seconds}. {@code markPulse}: where the mark's running ring
     * is (0 none); {@code dome}: the lobe's radius and how white it is (the pop); {@code fire}: the plasma ball's age and
     * radius; {@code splash}: how far the landing's splash has run and its age.
     */
    static void paint(float t, float seconds, float markPulse, float domeRadius, float domeWhite, float fireAge, float fireRadius,
                      float splashFront, float splashAge) {
        RenderSystem.assertOnRenderThread();
        boolean all = !painted || !TEXTURE.ready();
        if (ortho == null) {
            ortho = new PerspectiveProjectionMatrixBuffer("TA-02 Quasar Jets atlas");
        }
        Matrix4f proj = new Matrix4f().setOrtho(0f, COLUMNS, 0f, ROWS, -1f, 1f);
        Matrix4f view = new Matrix4f();
        boolean[] clear = {all};
        Painter program = (bake, tile, mode, loU, loV, p0, p1, p2, p3, q0) -> {
            paintTile(view, proj, bake, tile, mode, loU, loV, p0, p1, p2, p3, q0, clear[0]);
            clear[0] = false;
        };
        {
            for (int i = 0; i < TILES; i++) {
                // the glows never change: painted once
                if (!all && (i >= GLOW0 && i < STAR_SKY || i == GLOW_SKY)) {
                    continue;
                }
                float bake = i >= STAR_SKY && i <= BEAM_SKY ? 2f : 1f;
                switch (i) {
                    case STAR, STAR_SKY -> tile(program, bake, i, QuasarGl.STAR, true, seconds, 0f, 0f, 0.3f, 0f);
                    case STAR_LOCKED, STAR_LOCKED_SKY -> tile(program, bake, i, QuasarGl.STAR, true, seconds, 0f, 1f, 0.3f, 0f);
                    case SUN, SUN_SKY -> tile(program, bake, i, QuasarGl.SUN, true, seconds, 0f, 0f, 0f, 0f);
                    case MARK -> tile(program, bake, i, QuasarGl.MARK, true, t, 1f, markPulse, 0f, 0f);
                    case SHOCK_24 -> tile(program, bake, i, QuasarGl.SHOCK, false, t, 1f, 24f, 0f, 0f);
                    case SHOCK_28 -> tile(program, bake, i, QuasarGl.SHOCK, false, t, 1f, 28f, 0f, 0f);
                    case SHOCK_36 -> tile(program, bake, i, QuasarGl.SHOCK, false, t, 1f, 36f, 0f, 0f);
                    case DUST -> tile(program, bake, i, QuasarGl.DUST, false, t, 1f, 32f, 0f, 0f);
                    case DOME -> tile(program, bake, i, QuasarGl.DOME, false, t, 1f, Math.max(4f, domeRadius), domeWhite, 0f);
                    case FIREBALL -> tile(program, bake, i, QuasarGl.FIREBALL, false, t, fireAge, Math.max(4f, fireRadius), 1f, 0f);
                    // no head and no far end: the glows at its ends and a fade along it stand in for them (see emit)
                    case BEAM, BEAM_SKY -> tile(program, bake, i, QuasarGl.BEAM, true, t, -1.0e9f, 0.13f, 1.0e9f, 0f);
                    case GLOW_SKY -> tile(program, bake, i, QuasarGl.GLOW, true, GLOWS[0][0], GLOWS[0][1], 0f, 0f, 0f);
                    case SPLASH -> tile(program, bake, i, QuasarGl.SPLASH, true, t, 1f, splashFront, splashAge, 0f);
                    // across -1..1, along 0..1, grown whole (emit cuts it to how much has grown)
                    case ARC -> tile(program, bake, i, QuasarGl.ARC, -1f, 0f, t, 1f, 0f, 1f, 0f);
                    default -> {
                        float[] g = GLOWS[i - GLOW0];
                        tile(program, bake, i, QuasarGl.GLOW, true, g[0], g[1], 0f, 0f, 0f);
                    }
                }
            }
        }
        painted = true;
    }

    /** Paints one tile (the uv's low end given for each way; the high end is 1). */
    @FunctionalInterface
    private interface Painter {
        void tile(float bake, int tile, int mode, float loU, float loV, float p0, float p1, float p2, float p3, float q0);
    }

    /** One tile: a quad over it, uv over the material's whole range ({@code centred}: -1..1, else 0..1), white. */
    private static void tile(Painter program, float bake, int tile, int mode, boolean centred, float p0, float p1, float p2, float p3,
                             float q0) {
        program.tile(bake, tile, mode, centred ? -1f : 0f, centred ? -1f : 0f, p0, p1, p2, p3, q0);
    }

    /** One tile, the uv's low end given for each way (the high end is 1). */
    private static void tile(Painter program, float bake, int tile, int mode, float loU, float loV, float p0, float p1, float p2, float p3,
                             float q0) {
        program.tile(bake, tile, mode, loU, loV, p0, p1, p2, p3, q0);
    }

    /**
     * One tile painted in a render pass of its own (the uniform block can only be written between passes):
     * {@code clear} the whole texture to transparent black first.
     */
    private static void paintTile(Matrix4f view, Matrix4f proj, float bake, int tile, int mode, float loU, float loV, float p0, float p1,
                                  float p2, float p3, float q0, boolean clear) {
        float x = tile % COLUMNS;
        float y = (float) (tile / COLUMNS);
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        b.addVertex(x, y, 0f).setUv(loU, loV).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x + 1f, y, 0f).setUv(1f, loV).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x + 1f, y + 1f, 0f).setUv(1f, 1f).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x, y + 1f, 0f).setUv(loU, 1f).setColor(1f, 1f, 1f, 1f);
        if (!QUAD.upload(b.build())) {
            return;
        }
        GpuBufferSlice params = QuasarGl.params(view, mode, bake, p0, p1, p2, p3, q0);
        GpuBufferSlice projection = ortho.getBuffer(proj);
        try (RenderPass pass = TEXTURE.begin(clear)) {
            pass.setPipeline(QuasarGl.BAKE);
            pass.setUniform("Projection", projection);
            pass.setUniform("QuasarParams", params);
            QUAD.draw(pass);
        }
    }

    // ---- drawing from the painted looks -------------------------------------------------------------------------------

    /**
     * Writes {@code mesh} (POSITION_TEX_COLOR quads, as {@link QuasarGl} builds them) into {@code out} as entity vertices
     * on this texture, for material {@code mode} with the parameters its draw call was given. Inside the sky
     * ({@link #drawSky}) the sky's tiles are used, the corners turned by the sky's pose and the colour square-rooted.
     */
    static void emit(MeshData mesh, VertexConsumer out, int mode, float p0, float p1, float p2, float p3) {
        // (1.21.11: no sky pass with a pack, so never the sky's tiles here)
        Matrix4f pose = null;
        boolean sky = false;
        Matrix4fc packed = packPose;
        float[] quad = new float[4 * 8];
        int tile;
        // the uv runs -1..1 over the material (else 0..1)
        boolean centred = true;
        float strength = 1f;
        // the share of white mixed into the colour (the looks were painted white: a star's core and the jet's are white)
        float whiten = 0f;
        switch (mode) {
            case QuasarGl.STAR -> {
                tile = p2 > 0.5f ? sky ? STAR_LOCKED_SKY : STAR_LOCKED : sky ? STAR_SKY : STAR;
                whiten = 0.35f;
            }
            case QuasarGl.SUN -> tile = sky ? SUN_SKY : SUN;
            case QuasarGl.MARK -> {
                tile = MARK;
                strength = p1;
            }
            case QuasarGl.SHOCK -> {
                tile = p2 < 26f ? SHOCK_24 : p2 < 32f ? SHOCK_28 : SHOCK_36;
                centred = false;
                strength = p1;
            }
            case QuasarGl.DUST -> {
                tile = DUST;
                centred = false;
                // (only added: kept faint, or the dust glows)
                strength = p1 * 0.7f;
            }
            case QuasarGl.DOME -> {
                tile = DOME;
                centred = false;
                strength = p1;
                whiten = p3;
            }
            case QuasarGl.FIREBALL -> {
                tile = FIREBALL;
                centred = false;
                strength = p3;
            }
            case QuasarGl.BEAM -> {
                tile = sky ? BEAM_SKY : BEAM;
                whiten = 0.4f;
            }
            case QuasarGl.SPLASH -> {
                tile = SPLASH;
                strength = p1;
            }
            case QuasarGl.ARC -> {
                tile = ARC;
                strength = p1;
            }
            default -> {
                int best = 0;
                float bestD = Float.MAX_VALUE;
                for (int i = 0; i < GLOWS.length; i++) {
                    float d = Math.abs(GLOWS[i][0] - p0) / Math.max(1f, p0) + Math.abs(GLOWS[i][1] - p1);
                    if (d < bestD) {
                        bestD = d;
                        best = i;
                    }
                }
                tile = sky ? GLOW_SKY : GLOW0 + best;
            }
        }
        float tx = tile % COLUMNS;
        float ty = (float) (tile / COLUMNS);
        ByteBuffer data = mesh.vertexBuffer();
        int count = mesh.drawState().vertexCount();
        int stride = DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize();
        Vector3f corner = new Vector3f();
        for (int i = 0; i < count; i++) {
            int o = i * stride;
            float x = data.getFloat(o);
            float y = data.getFloat(o + 4);
            float z = data.getFloat(o + 8);
            float u = data.getFloat(o + 12);
            float v = data.getFloat(o + 16);
            float r = (data.get(o + 20) & 255) / 255f;
            float g = (data.get(o + 21) & 255) / 255f;
            float bl = (data.get(o + 22) & 255) / 255f;
            float a = (data.get(o + 23) & 255) / 255f;
            float lu;
            float lv;
            float k = strength;
            if (mode == QuasarGl.ARC) {
                // along the arc: only as much as has grown
                lu = (u + 1f) * 0.5f;
                lv = v;
                k *= a * (1f - smooth(p3 - 0.03f, p3, v));
            } else if (mode == QuasarGl.BEAM) {
                // along the jet the look doesn't change: across it only; it fades out far off as the shader's does
                lu = (u + 1f) * 0.5f;
                lv = 0.5f;
                float f = p3 <= 0f ? 0f : smooth(p3 * 0.55f, p3, v);
                k *= a * (1f - f);
            } else if (mode == QuasarGl.DOME) {
                // its alpha says how squarely the skin faces the eye: bright at its rim, thin in the middle
                lu = u;
                lv = v;
                float rim = (float) Math.pow(1f - Math.min(1f, a), 2.4);
                k *= 0.35f + 1.5f * rim;
            } else if (mode == QuasarGl.FIREBALL) {
                lu = u;
                lv = v;
                k *= smooth(0f, 0.6f, a);
            } else if (centred) {
                lu = (u + 1f) * 0.5f;
                lv = (v + 1f) * 0.5f;
                k *= a;
            } else {
                lu = u;
                lv = v;
                k *= a;
            }
            lu = Math.max(0f, Math.min(1f, lu));
            lv = Math.max(0f, Math.min(1f, lv));
            r = Math.min(1f, (r + (1f - r) * whiten) * k);
            g = Math.min(1f, (g + (1f - g) * whiten) * k);
            bl = Math.min(1f, (bl + (1f - bl) * whiten) * k);
            if (sky) {
                // the pack's sky program squares the colour: give it the square root
                r = (float) Math.sqrt(r);
                g = (float) Math.sqrt(g);
                bl = (float) Math.sqrt(bl);
                pose.transformPosition(x, y, z, corner);
                x = corner.x;
                y = corner.y;
                z = corner.z;
            }
            int c = (i & 3) * 8;
            quad[c] = x;
            quad[c + 1] = y;
            quad[c + 2] = z;
            quad[c + 3] = r;
            quad[c + 4] = g;
            quad[c + 5] = bl;
            quad[c + 6] = (tx + INSET + lu * (1f - 2f * INSET)) / COLUMNS;
            quad[c + 7] = (ty + INSET + lv * (1f - 2f * INSET)) / ROWS;
            if ((i & 3) == 3) {
                // both ways round: the glowing-eyes render type culls back faces, and our quads face every way
                for (int n = 0; n < 4; n++) {
                    corner(out, packed, quad, n);
                }
                for (int n = 3; n >= 0; n--) {
                    corner(out, packed, quad, n);
                }
            }
        }
    }

    private static void corner(VertexConsumer out, @Nullable Matrix4fc pose, float[] quad, int k) {
        int c = k * 8;
        (pose == null ? out.addVertex(quad[c], quad[c + 1], quad[c + 2]) : out.addVertex(pose, quad[c], quad[c + 1], quad[c + 2]))
            .setColor(quad[c + 3], quad[c + 4], quad[c + 5], 1f)
            .setUv(quad[c + 6], quad[c + 7])
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(0f, 1f, 0f);
    }

    private static float smooth(float from, float to, float x) {
        float u = Math.max(0f, Math.min(1f, (x - from) / (to - from)));
        return u * u * (3f - 2f * u);
    }

    /**
     * The atlas's texture: drawn into like a render target, and registered under a name so the glowing-eyes render type
     * can bind it (the SA Series' 1.21.11 {@code BakedTexture}). Smoothly filtered, never repeated.
     */
    private static final class Baked extends AbstractTexture {
        private final Identifier location;
        private final int width;
        private final int height;
        private boolean registered;

        Baked(Identifier location, int width, int height) {
            this.location = location;
            this.width = width;
            this.height = height;
        }

        boolean ready() {
            return this.texture != null && !this.texture.isClosed();
        }

        /** A render pass onto the whole texture, made the first time; {@code clear}: start from transparent black. */
        RenderPass begin(boolean clear) {
            if (!this.ready()) {
                GpuDevice device = RenderSystem.getDevice();
                this.release();
                this.texture = device.createTexture(this.location::toString,
                    GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, this.width, this.height, 1, 1);
                this.textureView = device.createTextureView(this.texture);
                this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
                clear = true;
                if (!this.registered) {
                    Minecraft.getInstance().getTextureManager().register(this.location, this);
                    this.registered = true;
                }
            }
            return RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "TA-02 Quasar Jets atlas",
                this.getTextureView(), clear ? OptionalInt.of(0) : OptionalInt.empty());
        }

        /** The texture manager closes it with the rest when resources reload; it is made again when next painted. */
        @Override
        public void close() {
            this.release();
            this.registered = false;
            painted = false;
        }

        private void release() {
            if (this.textureView != null) {
                this.textureView.close();
                this.textureView = null;
            }
            if (this.texture != null) {
                this.texture.close();
                this.texture = null;
            }
        }
    }
}
