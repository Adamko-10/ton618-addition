package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * <b>FABLE'S FILE</b> (since the quality redo, 2026-11-16). Drawing for TA-02's world effects: one shader program of
 * our own ({@code shaders/quasar/quasar_world}, a material per mode) and the few shapes {@link QuasarRender} builds
 * round the camera each frame (quads facing the eye, a band along the jet, domes, flat rings, standing strips). No
 * textures. Everything is drawn with colour already multiplied by its alpha: light is added, smoke and the lobe's skin
 * also cover what is behind them.
 */
public final class QuasarGl {
    /** The shader's materials (see {@code quasar_world.fsh}). */
    static final int STAR = 1;
    static final int SUN = 2;
    static final int BEAM = 3;
    static final int DOME = 4;
    static final int SHOCK = 5;
    static final int DUST = 6;
    static final int FIREBALL = 7;
    static final int MARK = 8;
    static final int GLOW = 9;
    static final int SPLASH = 10;
    static final int ARC = 11;

    @Nullable
    private static ShaderInstance shader;
    @Nullable
    private static VertexBuffer buffer;

    private QuasarGl() {
    }

    /**
     * Loads the program (called from the film's {@code init}, which has the mod's event bus). The game looks for shader
     * programs under {@code shaders/core/}; ours live in {@code shaders/quasar/}, so the lookup is pointed there.
     */
    public static void register(CoreShaderRegistrationCallback.RegistrationContext context) {
        // Fabric API loads core shaders from shaders/core/ (the NeoForge and Forge branches keep them in shaders/quasar/)
        try {
            context.register(Ton618Addition.id("quasar_world"), DefaultVertexFormat.POSITION_TEX_COLOR, loaded -> shader = loaded);
        } catch (IOException | RuntimeException failed) {
            // the strike still happens (the land, the damage, the sounds); only its light isn't drawn
            Ton618Addition.LOG.error("The Quasar Jets world shader didn't load, so the strike's light won't be drawn", failed);
            shader = null;
        }
    }

    /**
     * With a shader pack on: where the effects' quads go instead of being drawn ({@link QuasarAtlas#emit}), drawn all at
     * once from the painted looks by {@code QuasarRender}. Null without a pack.
     */
    @Nullable
    static BufferBuilder packing;
    private static java.lang.reflect.Method irisInUse;
    private static Object iris;
    private static boolean irisLooked;

    /** A shader pack is on (Iris's public API, looked up by name: Iris is optional). */
    static boolean shaderPack() {
        if (!irisLooked) {
            irisLooked = true;
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                iris = api.getMethod("getInstance").invoke(null);
                irisInUse = api.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError absent) {
                irisInUse = null;
            }
        }
        if (irisInUse == null) {
            return false;
        }
        try {
            return (Boolean) irisInUse.invoke(iris);
        } catch (ReflectiveOperationException | RuntimeException unsure) {
            return false;
        }
    }

    static boolean ready() {
        return shader != null;
    }

    @Nullable
    static ShaderInstance program() {
        return shader;
    }

    static BufferBuilder begin() {
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    static void vertex(BufferBuilder b, float x, float y, float z, float u, float v, float[] rgb, float a) {
        b.addVertex(x, y, z).setUv(u, v).setColor(rgb[0], rgb[1], rgb[2], a < 0f ? 0f : Math.min(a, 1f));
    }

    static float[] rgb(int colour) {
        return new float[]{(colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f, (colour & 255) / 255f};
    }

    /** A quad facing the camera: its middle, the camera's right and up, half its size, uv -1..1 across it. */
    static void billboard(BufferBuilder b, Vector3f c, Vector3f right, Vector3f up, float half, float[] rgb, float a) {
        float rx = right.x * half;
        float ry = right.y * half;
        float rz = right.z * half;
        float ux = up.x * half;
        float uy = up.y * half;
        float uz = up.z * half;
        vertex(b, c.x - rx - ux, c.y - ry - uy, c.z - rz - uz, -1f, -1f, rgb, a);
        vertex(b, c.x + rx - ux, c.y + ry - uy, c.z + rz - uz, 1f, -1f, rgb, a);
        vertex(b, c.x + rx + ux, c.y + ry + uy, c.z + rz + uz, 1f, 1f, rgb, a);
        vertex(b, c.x - rx + ux, c.y - ry + uy, c.z - rz + uz, -1f, 1f, rgb, a);
    }

    /** A flat quad lying on the ground, uv -1..1 across it. */
    static void flat(BufferBuilder b, Vector3f c, float half, float[] rgb, float a) {
        vertex(b, c.x - half, c.y, c.z - half, -1f, -1f, rgb, a);
        vertex(b, c.x - half, c.y, c.z + half, -1f, 1f, rgb, a);
        vertex(b, c.x + half, c.y, c.z + half, 1f, 1f, rgb, a);
        vertex(b, c.x + half, c.y, c.z - half, 1f, -1f, rgb, a);
    }

    /**
     * A band along a line, turned to face the eye (which is at 0,0,0): from {@code start} blocks along {@code dir} from
     * {@code origin} to {@code end} blocks, {@code half} wide each side. uv.x runs -1..1 across it, uv.y is the distance
     * along in blocks. Cut into pieces (short ones near the start) so it turns properly along its length.
     */
    static void band(BufferBuilder b, Vector3f origin, Vector3f dir, float start, float end, float half, float[] rgb, float a) {
        int pieces = 14;
        Vector3f prevL = new Vector3f();
        Vector3f prevR = new Vector3f();
        float prevS = 0f;
        boolean have = false;
        Vector3f p = new Vector3f();
        Vector3f side = new Vector3f();
        for (int i = 0; i <= pieces; i++) {
            float u = i / (float) pieces;
            float s = start + (end - start) * u * u;
            p.set(dir).mul(s).add(origin);
            side.set(dir).cross(p);
            if (side.lengthSquared() < 1.0e-8f) {
                have = false;
                continue;
            }
            side.normalize(half);
            float lx = p.x - side.x;
            float ly = p.y - side.y;
            float lz = p.z - side.z;
            float rx = p.x + side.x;
            float ry = p.y + side.y;
            float rz = p.z + side.z;
            if (have) {
                vertex(b, prevL.x, prevL.y, prevL.z, -1f, prevS, rgb, a);
                vertex(b, prevR.x, prevR.y, prevR.z, 1f, prevS, rgb, a);
                vertex(b, rx, ry, rz, 1f, s, rgb, a);
                vertex(b, lx, ly, lz, -1f, s, rgb, a);
            }
            prevL.set(lx, ly, lz);
            prevR.set(rx, ry, rz);
            prevS = s;
            have = true;
        }
    }

    /**
     * A dome over {@code c}, {@code r} in radius, reaching a little under the ground (so it closes over a crater).
     * uv.x runs 0-1 round it, uv.y 0 at the ground to 1 at the top; each corner's alpha says how squarely its skin
     * faces the eye (1 seen flat on, 0 at the dome's edge), which is what gives it a rim.
     */
    static void dome(BufferBuilder b, Vector3f c, float r, float[] rgb) {
        dome(b, c, r, rgb, -0.45);
    }

    /**
     * A dome whose skin starts {@code low} radians under the level of {@code c} (-0.45: a little under the ground;
     * near a quarter turn down: almost a whole ball, for one that sits in a crater).
     */
    static void dome(BufferBuilder b, Vector3f c, float r, float[] rgb, double low) {
        int rings = 18;
        int segs = 40;
        double span = Math.PI * 0.5 - low;
        for (int i = 0; i < rings; i++) {
            double p0 = low + span * i / rings;
            double p1 = low + span * (i + 1) / rings;
            for (int j = 0; j < segs; j++) {
                domeCorner(b, c, r, p0, j, segs, rgb);
                domeCorner(b, c, r, p0, j + 1, segs, rgb);
                domeCorner(b, c, r, p1, j + 1, segs, rgb);
                domeCorner(b, c, r, p1, j, segs, rgb);
            }
        }
    }

    /**
     * A dome as {@link #dome}, but lumpy and never still: its skin bulges in and out by up to {@code lumps} of its
     * radius as {@code clock} runs (a fireball, not a bowl turned over).
     */
    static void blob(BufferBuilder b, Vector3f c, float r, float[] rgb, float clock, float lumps) {
        int rings = 18;
        int segs = 56;
        double low = -0.45;
        double span = Math.PI * 0.5 - low;
        for (int i = 0; i < rings; i++) {
            double p0 = low + span * i / rings;
            double p1 = low + span * (i + 1) / rings;
            for (int j = 0; j < segs; j++) {
                domeCorner(b, c, r * bulge(p0, j, segs, clock, lumps), p0, j, segs, rgb);
                domeCorner(b, c, r * bulge(p0, j + 1, segs, clock, lumps), p0, j + 1, segs, rgb);
                domeCorner(b, c, r * bulge(p1, j + 1, segs, clock, lumps), p1, j + 1, segs, rgb);
                domeCorner(b, c, r * bulge(p1, j, segs, clock, lumps), p1, j, segs, rgb);
            }
        }
    }

    /** How far a point of the blob's skin stands out: waves that join up round it, drifting with the clock. */
    private static float bulge(double polar, int j, int segs, float clock, float lumps) {
        double around = Math.PI * 2.0 * (j % segs) / segs;
        double n = 0.5 * Math.sin(3.0 * around + polar * 6.8 + clock * 0.21) + 0.3 * Math.sin(5.0 * around - polar * 9.0 + clock * 0.33 + 1.3)
            + 0.2 * Math.sin(9.0 * around + polar * 4.0 - clock * 0.4 + 2.1);
        // (its top is one point: every corner there must agree)
        return 1f + lumps * (float) (n * Math.cos(polar));
    }

    /**
     * The far part of the jet, drawn on the sky: the line from {@code start} blocks along {@code dir} from
     * {@code origin} out to {@code end}, each point brought in to {@code sky} blocks from the eye (which is at 0,0,0),
     * so it is seen where it really is however far off, and the land in front hides it. Fading in between
     * {@code fadeFrom} and {@code fadeTo} blocks along (where the near part, drawn in the world, fades out). uv as
     * {@link #band}, but uv.y counts slowly from {@code start} so the far knots don't flicker.
     */
    static void farBand(BufferBuilder b, Vector3f origin, Vector3f dir, float start, float end, float half, float sky, float fadeFrom,
                        float fadeTo, float[] rgb, float a) {
        int pieces = 18;
        Vector3f prevL = new Vector3f();
        Vector3f prevR = new Vector3f();
        float prevV = 0f;
        float prevA = 0f;
        boolean have = false;
        Vector3f p = new Vector3f();
        Vector3f side = new Vector3f();
        for (int i = 0; i <= pieces; i++) {
            float u = i / (float) pieces;
            float s = start + (end - start) * u * u * u;
            p.set(dir).mul(s).add(origin);
            float away = p.length();
            side.set(dir).cross(p);
            if (away < 1f || side.lengthSquared() < 1.0e-8f) {
                have = false;
                continue;
            }
            float scale = sky / away;
            side.normalize(Math.max(half * scale, 0.1f));
            p.mul(scale);
            float fade = fadeTo <= fadeFrom ? 1f : Math.min(1f, Math.max(0f, (s - fadeFrom) / (fadeTo - fadeFrom)));
            float alpha = a * fade * fade * (3f - 2f * fade);
            float v = start + (s - start) * 0.04f;
            if (have) {
                vertex(b, prevL.x, prevL.y, prevL.z, -1f, prevV, rgb, prevA);
                vertex(b, prevR.x, prevR.y, prevR.z, 1f, prevV, rgb, prevA);
                vertex(b, p.x + side.x, p.y + side.y, p.z + side.z, 1f, v, rgb, alpha);
                vertex(b, p.x - side.x, p.y - side.y, p.z - side.z, -1f, v, rgb, alpha);
            }
            prevL.set(p).sub(side);
            prevR.set(p).add(side);
            prevV = v;
            prevA = alpha;
            have = true;
        }
    }

    private static void domeCorner(BufferBuilder b, Vector3f c, float r, double polar, int j, int segs, float[] rgb) {
        double around = Math.PI * 2.0 * j / segs;
        float nx = (float) (Math.cos(polar) * Math.cos(around));
        float ny = (float) Math.sin(polar);
        float nz = (float) (Math.cos(polar) * Math.sin(around));
        float x = c.x + nx * r;
        float y = c.y + ny * r;
        float z = c.z + nz * r;
        float away = (float) Math.sqrt(x * x + y * y + z * z);
        float facing = away < 1.0e-4f ? 1f : Math.abs(nx * x + ny * y + nz * z) / away;
        vertex(b, x, y, z, j / (float) segs, (float) (Math.abs(polar) / (Math.PI * 0.5)), rgb, facing);
    }

    /**
     * A band along the points (the eye is at 0,0,0), turned to face the eye, {@code half} wide each side: uv.x -1..1
     * across it, uv.y 0 at the first point to 1 at the last.
     */
    static void curve(BufferBuilder b, Vector3f[] points, float half, float[] rgb, float a) {
        int n = points.length;
        Vector3f dir = new Vector3f();
        Vector3f side = new Vector3f();
        Vector3f prevL = new Vector3f();
        Vector3f prevR = new Vector3f();
        float prevV = 0f;
        boolean have = false;
        for (int i = 0; i < n; i++) {
            Vector3f p = points[i];
            dir.set(points[Math.min(i + 1, n - 1)]).sub(points[Math.max(i - 1, 0)]);
            side.set(dir).cross(p);
            if (side.lengthSquared() < 1.0e-8f) {
                have = false;
                continue;
            }
            side.normalize(half);
            float v = i / (float) (n - 1);
            if (have) {
                vertex(b, prevL.x, prevL.y, prevL.z, -1f, prevV, rgb, a);
                vertex(b, prevR.x, prevR.y, prevR.z, 1f, prevV, rgb, a);
                vertex(b, p.x + side.x, p.y + side.y, p.z + side.z, 1f, v, rgb, a);
                vertex(b, p.x - side.x, p.y - side.y, p.z - side.z, -1f, v, rgb, a);
            }
            prevL.set(p).sub(side);
            prevR.set(p).add(side);
            prevV = v;
            have = true;
        }
    }

    /** A flat ring on the ground round {@code c}: uv.x 0 at {@code inner} to 1 at {@code outer}, uv.y 0-1 round it. */
    static void ring(BufferBuilder b, Vector3f c, float inner, float outer, float[] rgb, float a) {
        int segs = 64;
        for (int j = 0; j < segs; j++) {
            double a0 = Math.PI * 2.0 * j / segs;
            double a1 = Math.PI * 2.0 * (j + 1) / segs;
            float c0 = (float) Math.cos(a0);
            float s0 = (float) Math.sin(a0);
            float c1 = (float) Math.cos(a1);
            float s1 = (float) Math.sin(a1);
            float v0 = j / (float) segs;
            float v1 = (j + 1) / (float) segs;
            vertex(b, c.x + c0 * inner, c.y, c.z + s0 * inner, 0f, v0, rgb, a);
            vertex(b, c.x + c1 * inner, c.y, c.z + s1 * inner, 0f, v1, rgb, a);
            vertex(b, c.x + c1 * outer, c.y, c.z + s1 * outer, 1f, v1, rgb, a);
            vertex(b, c.x + c0 * outer, c.y, c.z + s0 * outer, 1f, v0, rgb, a);
        }
    }

    /** A strip standing on a circle round {@code c}: uv.x 0-1 round it, uv.y 0 at the ground to 1 at its top. */
    static void wall(BufferBuilder b, Vector3f c, float radius, float height, float[] rgb, float a) {
        int segs = 96;
        for (int j = 0; j < segs; j++) {
            double a0 = Math.PI * 2.0 * j / segs;
            double a1 = Math.PI * 2.0 * (j + 1) / segs;
            float x0 = c.x + (float) Math.cos(a0) * radius;
            float z0 = c.z + (float) Math.sin(a0) * radius;
            float x1 = c.x + (float) Math.cos(a1) * radius;
            float z1 = c.z + (float) Math.sin(a1) * radius;
            float v0 = j / (float) segs;
            float v1 = (j + 1) / (float) segs;
            // leaning outward a little, like a wave of dust rolling away
            float lean = 1.12f;
            float tx0 = c.x + (x0 - c.x) * lean;
            float tz0 = c.z + (z0 - c.z) * lean;
            float tx1 = c.x + (x1 - c.x) * lean;
            float tz1 = c.z + (z1 - c.z) * lean;
            vertex(b, x0, c.y, z0, v0, 0f, rgb, a * away(x0, c.y, z0));
            vertex(b, x1, c.y, z1, v1, 0f, rgb, a * away(x1, c.y, z1));
            vertex(b, tx1, c.y + height, tz1, v1, 1f, rgb, a * away(tx1, c.y + height, tz1));
            vertex(b, tx0, c.y + height, tz0, v0, 1f, rgb, a * away(tx0, c.y + height, tz0));
        }
    }

    /** 0 at the eye, 1 from 14 blocks away on: what is right in front of the camera is thinned out. */
    private static float away(float x, float y, float z) {
        float d = (float) Math.sqrt(x * x + y * y + z * z);
        return d <= 3f ? 0f : Math.min(1f, (d - 3f) / 11f);
    }

    /** Draws what {@code b} holds with material {@code mode} and its parameters. */
    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3, float q0) {
        MeshData mesh = b.build();
        ShaderInstance program = shader;
        if (mesh == null) {
            return;
        }
        if (packing != null) {
            QuasarAtlas.emit(mesh, packing, mode, p0, p1, p2, p3);
            mesh.close();
            return;
        }
        if (program == null) {
            mesh.close();
            return;
        }
        program.safeGetUniform("Mode").set((float) mode);
        program.safeGetUniform("Param").set(p0, p1, p2, p3);
        program.safeGetUniform("Param2").set(q0, 0f, 0f, 0f);
        if (buffer == null) {
            buffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        buffer.bind();
        buffer.upload(mesh);
        buffer.drawWithShader(view, proj, program);
        VertexBuffer.unbind();
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3) {
        draw(b, view, proj, mode, p0, p1, p2, p3, 0f);
    }

    /**
     * Light in the world: colour multiplied by its alpha (alpha 0 only adds), hidden by what stands in front unless
     * {@code depth} is false, writing no depth, both faces drawn.
     */
    static void light(boolean depth) {
        if (packing != null) {
            // (the pack's render type sets everything)
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
    }

    static void end() {
        if (packing != null) {
            return;
        }
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
