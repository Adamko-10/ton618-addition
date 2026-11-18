package dev.adamko.ton618addition.client.film;

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
 * <b>FABLE'S FILE.</b> The film's one shader program ({@code shaders/film/last_light}) and the few drawing helpers
 * everything in the film uses: quads built on the CPU each frame, drawn with one material ("mode") per call.
 */
final class FilmGl {
    /** The shader's materials (see {@code last_light.fsh}). */
    static final int PLAIN = 0;
    static final int DOT = 1;
    static final int HOLE = 2;
    static final int WAVE = 3;
    static final int WALL = 4;
    static final int RING = 5;
    static final int GALAXY = 6;
    static final int GLOW = 7;
    static final int STREAK = 8;
    static final int FLARE = 9;

    @Nullable
    private static ShaderInstance shader;
    @Nullable
    private static VertexBuffer buffer;

    private FilmGl() {
    }

    /**
     * Loads the film's program through Fabric API (on Fabric the files live in {@code shaders/core/}, where its
     * shader registration looks; the NeoForge and Forge branches keep them in {@code shaders/film/}).
     */
    static void register(CoreShaderRegistrationCallback.RegistrationContext context) {
        try {
            context.register(Ton618Addition.id("last_light"), DefaultVertexFormat.POSITION_TEX_COLOR, loaded -> shader = loaded);
        } catch (IOException | RuntimeException failed) {
            // the skill still works; the film then shows its words and the white only
            Ton618Addition.LOG.error("The Last Light film's shader didn't load, so its pictures won't be drawn", failed);
            shader = null;
        }
    }

    static boolean ready() {
        return shader != null;
    }

    static BufferBuilder begin() {
        return Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
    }

    static void vertex(BufferBuilder b, float x, float y, float z, float u, float v, float r, float g, float bl, float a) {
        b.addVertex(x, y, z).setUv(u, v).setColor(clamp(r), clamp(g), clamp(bl), clamp(a));
    }

    private static float clamp(float v) {
        return v < 0F ? 0F : Math.min(v, 1F);
    }

    /**
     * A quad facing the camera: centre, the camera's right and up, half its width and height, uv -1..1 across it.
     */
    static void billboard(BufferBuilder b, float cx, float cy, float cz, Vector3f right, Vector3f up, float halfW, float halfH,
            float r, float g, float bl, float a) {
        float rx = right.x * halfW;
        float ry = right.y * halfW;
        float rz = right.z * halfW;
        float ux = up.x * halfH;
        float uy = up.y * halfH;
        float uz = up.z * halfH;
        vertex(b, cx - rx - ux, cy - ry - uy, cz - rz - uz, -1F, -1F, r, g, bl, a);
        vertex(b, cx + rx - ux, cy + ry - uy, cz + rz - uz, 1F, -1F, r, g, bl, a);
        vertex(b, cx + rx + ux, cy + ry + uy, cz + rz + uz, 1F, 1F, r, g, bl, a);
        vertex(b, cx - rx + ux, cy - ry + uy, cz - rz + uz, -1F, 1F, r, g, bl, a);
    }

    /** Draws what {@code b} holds with material {@code mode} and its two parameter sets. */
    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3,
            float q0, float q1, float q2, float q3) {
        MeshData mesh = b.build();
        ShaderInstance program = shader;
        if (mesh == null) {
            return;
        }
        if (program == null) {
            mesh.close();
            return;
        }
        program.safeGetUniform("Mode").set((float) mode);
        program.safeGetUniform("Param").set(p0, p1, p2, p3);
        program.safeGetUniform("Param2").set(q0, q1, q2, q3);
        if (buffer == null) {
            buffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        buffer.bind();
        buffer.upload(mesh);
        buffer.drawWithShader(view, proj, program);
        VertexBuffer.unbind();
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3) {
        draw(b, view, proj, mode, p0, p1, p2, p3, 0F, 0F, 0F, 0F);
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode) {
        draw(b, view, proj, mode, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    // ---- blending ------------------------------------------------------------------------------------------------------

    /** Light added to what's there. */
    static void additive() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
    }

    /** Colour already multiplied by its alpha (the hole: its light adds, its shadow covers). */
    static void premultiplied() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    static void blended() {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    static void opaque() {
        RenderSystem.disableBlend();
    }

    /** A scene of our own over everything: no depth, both faces. */
    static void beginOverlay() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
    }

    /** Light and veils in the world: hidden by what stands in front, writing no depth, both faces. */
    static void beginWorld() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
    }

    static void end() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}
