package dev.adamko.ton618addition.client.film;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.PerspectiveProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

/**
 * <b>FABLE'S FILE</b> (ported to Minecraft 1.21.11's renderer by Opus, Adam's OK 2026-11-14). The film's one shader
 * ({@code shaders/core/last_light}) and the few drawing helpers everything in the film uses: quads built on the CPU
 * each frame, drawn with one material ("mode") per call.
 *
 * <p>On 1.21.11 there is no shader object to bind and no global blend state: each blend ({@link #opaque()},
 * {@link #additive()}, {@link #premultiplied()}, {@link #blended()}) and depth mode ({@link #beginOverlay()},
 * {@link #beginWorld()}) is a {@link RenderPipeline} of its own, the material and the view matrix go in a uniform block
 * ({@code FilmParams}), the projection in Minecraft's {@code Projection} block, and each {@link #draw} uploads its quads
 * ({@link FilmMesh}) and runs one render pass. The space scene ({@code FilmSpace}, between {@link #beginOverlay()} and
 * {@link #end()}) draws into a texture of its own, which {@link #present} puts on screen in the HUD (as the SA Series'
 * {@code ProximaRender} does); the world effects ({@link #beginWorld()}) draw onto the game's own picture and depth.
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

    private static final int OPAQUE = 0;
    private static final int ADDITIVE = 1;
    private static final int PREMULTIPLIED = 2;
    private static final int BLENDED = 3;
    /** FilmParams: the view matrix, then Mode (x), Param and Param2. */
    private static final int UNIFORM_SIZE = 64 + 3 * 16;
    private static final Identifier SCENE = Ton618Addition.id("film_scene");
    private static final RenderPipeline[][] PIPELINES = new RenderPipeline[4][2];
    private static final FilmMesh MESH = new FilmMesh("TA-01 Last Light film");

    private static int blend = BLENDED;
    /** Drawing the world effects (onto the game's picture, depth-tested), else the space scene (its own texture). */
    private static boolean world;
    @Nullable
    private static GpuBuffer uniforms;
    @Nullable
    private static PerspectiveProjectionMatrixBuffer projection;
    @Nullable
    private static Scene scene;

    private FilmGl() {
    }

    /** The pipelines compile their shaders themselves on first use: nothing to load up front on 1.21.11. */
    static boolean ready() {
        return true;
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
        if (!MESH.upload(mesh) || !world && scene == null) {
            return;
        }
        GpuDevice device = RenderSystem.getDevice();
        if (uniforms == null || uniforms.isClosed()) {
            uniforms = device.createBuffer(() -> "TA-01 Last Light film parameters", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                UNIFORM_SIZE);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE).putMat4f(view)
                .putVec4((float) mode, 0F, 0F, 0F).putVec4(p0, p1, p2, p3).putVec4(q0, q1, q2, q3).get();
            device.createCommandEncoder().writeToBuffer(uniforms.slice(), data);
        }
        // the world's own projection while drawing in the world; the film's for its space scene
        GpuBufferSlice projectionSlice = world ? RenderSystem.getProjectionMatrixBuffer() : projection(proj);
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView color = world ? main.getColorTextureView() : scene.getTextureView();
        GpuTextureView depth = world ? main.getDepthTextureView() : scene.depthView;
        try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "TA-01 Last Light", color,
                OptionalInt.empty(), depth, OptionalDouble.empty())) {
            pass.setPipeline(pipeline(blend, world));
            pass.setUniform("Projection", projectionSlice);
            pass.setUniform("FilmParams", uniforms);
            MESH.draw(pass);
        }
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3) {
        draw(b, view, proj, mode, p0, p1, p2, p3, 0F, 0F, 0F, 0F);
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode) {
        draw(b, view, proj, mode, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    private static GpuBufferSlice projection(Matrix4f matrix) {
        if (projection == null) {
            projection = new PerspectiveProjectionMatrixBuffer("TA-01 Last Light film projection");
        }
        return projection.getBuffer(matrix);
    }

    private static RenderPipeline pipeline(int blendMode, boolean depthTested) {
        int d = depthTested ? 1 : 0;
        if (PIPELINES[blendMode][d] == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
                .withLocation(Ton618Addition.id("pipeline/last_light_" + blendMode + "_" + d))
                .withVertexShader(Ton618Addition.id("core/last_light"))
                .withFragmentShader(Ton618Addition.id("core/last_light"))
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("FilmParams", UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withDepthTestFunction(depthTested ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withCull(false)
                .withColorWrite(true, false);
            switch (blendMode) {
                case ADDITIVE -> b.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE));
                case PREMULTIPLIED -> b.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA));
                case BLENDED -> b.withBlend(BlendFunction.TRANSLUCENT);
                default -> b.withoutBlend();
            }
            PIPELINES[blendMode][d] = b.build();
        }
        return PIPELINES[blendMode][d];
    }

    // ---- blending ------------------------------------------------------------------------------------------------------

    /** Light added to what's there. */
    static void additive() {
        blend = ADDITIVE;
    }

    /** Colour already multiplied by its alpha (the hole: its light adds, its shadow covers). */
    static void premultiplied() {
        blend = PREMULTIPLIED;
    }

    static void blended() {
        blend = BLENDED;
    }

    static void opaque() {
        blend = OPAQUE;
    }

    /**
     * A scene of our own over everything: no depth, both faces. On 1.21.11 it is drawn into the film's own texture
     * (cleared to black here, the size of the window); {@link #present} puts it on screen.
     */
    static void beginOverlay() {
        world = false;
        Minecraft mc = Minecraft.getInstance();
        int width = Math.max(1, mc.getWindow().getWidth());
        int height = Math.max(1, mc.getWindow().getHeight());
        if (scene == null || !scene.ready() || scene.width != width || scene.height != height) {
            scene = new Scene(width, height);
            mc.getTextureManager().register(SCENE, scene);
        }
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(scene.getTexture(), 0xFF000000, scene.depth, 1.0);
    }

    /** Light and veils in the world: hidden by what stands in front, writing no depth, both faces. */
    static void beginWorld() {
        world = true;
    }

    static void end() {
        blend = BLENDED;
    }

    /** The space scene drawn since {@link #beginOverlay()}, onto the screen (between the HUD under it and the words). */
    static void present(GuiGraphics graphics) {
        if (scene == null || !scene.ready()) {
            return;
        }
        graphics.nextStratum();
        graphics.blit(RenderPipelines.GUI_TEXTURED, SCENE, 0, 0, 0.0F, -graphics.guiHeight(),
            graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), -graphics.guiHeight());
        graphics.nextStratum();
    }

    /** The space scene's own picture and depth. */
    private static final class Scene extends AbstractTexture {
        final int width;
        final int height;
        @Nullable
        private GpuTexture depth;
        @Nullable
        private GpuTextureView depthView;

        Scene(int width, int height) {
            this.width = width;
            this.height = height;
            GpuDevice device = RenderSystem.getDevice();
            this.texture = device.createTexture(SCENE::toString, GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
                TextureFormat.RGBA8, width, height, 1, 1);
            this.textureView = device.createTextureView(this.texture);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            this.depth = device.createTexture(() -> "TA-01 Last Light film depth", GpuTexture.USAGE_RENDER_ATTACHMENT,
                TextureFormat.DEPTH32, width, height, 1, 1);
            this.depthView = device.createTextureView(this.depth);
        }

        boolean ready() {
            return this.texture != null && !this.texture.isClosed();
        }

        @Override
        public void close() {
            super.close();
            if (this.depthView != null) {
                this.depthView.close();
                this.depthView = null;
            }
            if (this.depth != null) {
                this.depth.close();
                this.depth = null;
            }
        }
    }
}
