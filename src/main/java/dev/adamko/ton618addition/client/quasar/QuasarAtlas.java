package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Matrix4f;

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
 */
final class QuasarAtlas {
    private static final int TILE = 192;
    private static final int COLUMNS = 6;
    private static final int ROWS = 3;
    /** Half a texel, in tiles: keeps every lookup inside its own tile. */
    private static final float INSET = 0.5F / TILE;
    private static final ResourceLocation LOCATION = Ton618Addition.id("quasar_atlas");

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
    private static final int TILES = GLOW0 + GLOWS.length;

    @Nullable
    private static TextureTarget target;
    private static boolean painted;
    @Nullable
    private static VertexBuffer quad;
    private static final ByteBufferBuilder PACK_BYTES = new ByteBufferBuilder(1 << 18);

    /** The effects' quads from the painted looks: glowing-eyes, added, hidden by what stands in front, no fog of ours. */
    static final RenderType PACK = Types.pack("ton618_addition_pack_glow", true);

    private QuasarAtlas() {
    }

    static boolean ready() {
        return target != null && painted;
    }

    static BufferBuilder packBuilder() {
        return new BufferBuilder(PACK_BYTES, VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
    }

    // ---- painting (before the level is drawn: while a pack draws the level, our shader can't draw anything) ----------

    /**
     * Paints the looks: all of them the first time, then the moving ones, at the strike's clock {@code t} (story ticks,
     * or the day's ticks before a strike) and the sky's {@code seconds}. {@code markPulse}: where the mark's running ring
     * is (0 none); {@code dome}: the lobe's radius and how white it is (the pop); {@code fire}: the fireball's age and
     * radius.
     */
    static void paint(float t, float seconds, float markPulse, float domeRadius, float domeWhite, float fireAge, float fireRadius) {
        ShaderInstance program = QuasarGl.program();
        if (program == null) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        TextureTarget tex = target;
        boolean all = !painted;
        if (tex == null) {
            tex = new TextureTarget(TILE * COLUMNS, TILE * ROWS, false, Minecraft.ON_OSX);
            tex.setFilterMode(GlConst.GL_LINEAR);
            tex.setClearColor(0f, 0f, 0f, 0f);
            target = tex;
            Minecraft.getInstance().getTextureManager().register(LOCATION, new View(tex));
            all = true;
        }
        if (all) {
            tex.clear(Minecraft.ON_OSX);
        }
        tex.bindWrite(true);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        Matrix4f proj = new Matrix4f().setOrtho(0f, COLUMNS, 0f, ROWS, -1f, 1f);
        Matrix4f view = new Matrix4f();
        program.safeGetUniform("Bake").set(1f);
        try {
            for (int i = 0; i < TILES; i++) {
                // the glows never change: painted once
                if (!all && i >= GLOW0) {
                    continue;
                }
                switch (i) {
                    case STAR -> tile(program, view, proj, i, QuasarGl.STAR, true, seconds, 0f, 0f, 0.3f, 0f);
                    case STAR_LOCKED -> tile(program, view, proj, i, QuasarGl.STAR, true, seconds, 0f, 1f, 0.3f, 0f);
                    case SUN -> tile(program, view, proj, i, QuasarGl.SUN, true, seconds, 0f, 0f, 0f, 0f);
                    case MARK -> tile(program, view, proj, i, QuasarGl.MARK, true, t, 1f, markPulse, 0f, 0f);
                    case SHOCK_24 -> tile(program, view, proj, i, QuasarGl.SHOCK, false, t, 1f, 24f, 0f, 0f);
                    case SHOCK_28 -> tile(program, view, proj, i, QuasarGl.SHOCK, false, t, 1f, 28f, 0f, 0f);
                    case SHOCK_36 -> tile(program, view, proj, i, QuasarGl.SHOCK, false, t, 1f, 36f, 0f, 0f);
                    case DUST -> tile(program, view, proj, i, QuasarGl.DUST, false, t, 1f, 32f, 0f, 0f);
                    case DOME -> tile(program, view, proj, i, QuasarGl.DOME, false, t, 1f, Math.max(4f, domeRadius), domeWhite, 0f);
                    case FIREBALL -> tile(program, view, proj, i, QuasarGl.FIREBALL, false, t, fireAge, Math.max(4f, fireRadius), 1f, 0f);
                    // no head and no far end: the glows at its ends and a fade along it stand in for them (see emit)
                    case BEAM -> tile(program, view, proj, i, QuasarGl.BEAM, true, t, -1.0e9f, 0.13f, 1.0e9f, 0f);
                    default -> {
                        float[] g = GLOWS[i - GLOW0];
                        tile(program, view, proj, i, QuasarGl.GLOW, true, g[0], g[1], 0f, 0f, 0f);
                    }
                }
            }
        } finally {
            program.safeGetUniform("Bake").set(0f);
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        painted = true;
    }

    /** One tile: a quad over it, uv over the material's whole range ({@code centred}: -1..1, else 0..1), white. */
    private static void tile(ShaderInstance program, Matrix4f view, Matrix4f proj, int tile, int mode, boolean centred, float p0,
                             float p1, float p2, float p3, float q0) {
        float x = tile % COLUMNS;
        float y = (float) (tile / COLUMNS);
        float lo = centred ? -1f : 0f;
        BufferBuilder b = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        b.addVertex(x, y, 0f).setUv(lo, lo).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x + 1f, y, 0f).setUv(1f, lo).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x + 1f, y + 1f, 0f).setUv(1f, 1f).setColor(1f, 1f, 1f, 1f);
        b.addVertex(x, y + 1f, 0f).setUv(lo, 1f).setColor(1f, 1f, 1f, 1f);
        MeshData mesh = b.build();
        if (mesh == null) {
            return;
        }
        program.safeGetUniform("Mode").set((float) mode);
        program.safeGetUniform("Param").set(p0, p1, p2, p3);
        program.safeGetUniform("Param2").set(q0, 0f, 0f, 0f);
        if (quad == null) {
            quad = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        quad.bind();
        quad.upload(mesh);
        quad.drawWithShader(view, proj, program);
        VertexBuffer.unbind();
    }

    // ---- drawing from the painted looks -------------------------------------------------------------------------------

    /**
     * Writes {@code mesh} (POSITION_TEX_COLOR quads, as {@link QuasarGl} builds them) into {@code out} as entity vertices
     * on this texture, for material {@code mode} with the parameters its draw call was given.
     */
    static void emit(MeshData mesh, VertexConsumer out, int mode, float p0, float p1, float p2, float p3) {
        int tile;
        // the uv runs -1..1 over the material (else 0..1)
        boolean centred = true;
        float strength = 1f;
        // the share of white mixed into the colour (the looks were painted white: a star's core and the jet's are white)
        float whiten = 0f;
        switch (mode) {
            case QuasarGl.STAR -> {
                tile = p2 > 0.5f ? STAR_LOCKED : STAR;
                whiten = 0.35f;
            }
            case QuasarGl.SUN -> tile = SUN;
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
                tile = BEAM;
                whiten = 0.4f;
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
                tile = GLOW0 + best;
            }
        }
        float tx = tile % COLUMNS;
        float ty = (float) (tile / COLUMNS);
        ByteBuffer data = mesh.vertexBuffer();
        int count = mesh.drawState().vertexCount();
        int stride = DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize();
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
            if (mode == QuasarGl.BEAM) {
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
            r += (1f - r) * whiten;
            g += (1f - g) * whiten;
            bl += (1f - bl) * whiten;
            out.addVertex(x, y, z)
                .setColor(Math.min(1f, r * k), Math.min(1f, g * k), Math.min(1f, bl * k), 1f)
                .setUv((tx + INSET + lu * (1f - 2f * INSET)) / COLUMNS, (ty + INSET + lv * (1f - 2f * INSET)) / ROWS)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(0f, 1f, 0f);
        }
    }

    private static float smooth(float from, float to, float x) {
        float u = Math.max(0f, Math.min(1f, (x - from) / (to - from)));
        return u * u * (3f - 2f * u);
    }

    /** The render type: built in a subclass for RenderType's own building blocks. */
    private abstract static class Types extends RenderType {
        private static float fogStart;

        private Types() {
            super("", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 0, false, false, () -> {
            }, () -> {
            });
        }

        static RenderType pack(String name, boolean depth) {
            TransparencyStateShard additive = new TransparencyStateShard("ton618_addition_pack_additive", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
            TexturingStateShard noFog = new TexturingStateShard("ton618_addition_no_fog", () -> {
                fogStart = RenderSystem.getShaderFogStart();
                RenderSystem.setShaderFogStart(Float.MAX_VALUE);
            }, () -> RenderSystem.setShaderFogStart(fogStart));
            return create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1 << 16, false, false,
                CompositeState.builder()
                    .setShaderState(RENDERTYPE_EYES_SHADER)
                    .setTextureState(new TextureStateShard(LOCATION, true, false))
                    .setTransparencyState(additive)
                    .setDepthTestState(depth ? LEQUAL_DEPTH_TEST : NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setTexturingState(noFog)
                    .createCompositeState(false));
        }
    }

    /** The render target seen as a texture: never deletes it, always smoothly filtered. */
    private static final class View extends AbstractTexture {
        private final TextureTarget target;

        View(TextureTarget target) {
            this.target = target;
        }

        @Override
        public int getId() {
            return this.target.getColorTextureId();
        }

        @Override
        public void setFilter(boolean blur, boolean mipmap) {
            super.setFilter(true, false);
        }

        @Override
        public void load(ResourceManager manager) {
        }

        @Override
        public void releaseId() {
        }
    }
}
