package dev.adamko.ton618addition.client.quasarfilm;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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
import dev.adamko.ton618addition.client.quasar.QuasarMesh;
import java.nio.ByteBuffer;
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
 * <b>FABLE'S FILE</b> (ported to Minecraft 1.21.11's renderer by Opus, 2026-11-18, as Last Light's {@code FilmGl} was).
 * The Quasar Jets film's one shader program ({@code shaders/core/quasar_jets}) and the few drawing helpers its space
 * scene uses: quads built on the CPU each frame, drawn with one material ("mode") per call. The same approach as Last
 * Light's {@code FilmGl} (a copy: the two films share no code, so neither can break the other).
 *
 * <p>On 1.21.11 there is no shader object to bind and no global blend state: each blend ({@link #opaque()},
 * {@link #additive()}, {@link #premultiplied()}, and the plain see-through one between scenes) is a {@link RenderPipeline}
 * of its own, the material and the view matrix go in a uniform block ({@code JetParams}), the projection in Minecraft's
 * {@code Projection} block, and each {@link #draw} uploads its quads and runs one render pass. The scene is drawn into a
 * picture of the film's own (JetPost's high-range one when it can be made, else {@link Scene}), which {@link #present}
 * puts on screen in the HUD.
 */
final class JetGl {
    /** The shader's materials (see {@code quasar_jets.fsh}). */
    static final int PLAIN = 0;
    static final int DOT = 1;
    static final int HOLE = 2;
    static final int STAR = 3;
    static final int MIRROR = 4;
    static final int GALAXY = 6;
    static final int GLOW = 7;
    static final int STREAK = 8;
    static final int SOFT = 9;
    static final int JET = 10;
    static final int TAIL = 11;
    static final int CORONA = 12;
    static final int DIAL = 13;
    static final int SPACE = 14;

    private static final int OPAQUE = 0;
    private static final int ADDITIVE = 1;
    private static final int PREMULTIPLIED = 2;
    private static final int BLENDED = 3;
    /** JetParams: the view matrix, then Mode (x), Param and Param2. */
    private static final int UNIFORM_SIZE = 64 + 3 * 16;
    private static final Identifier SCENE = Ton618Addition.id("quasar_film_scene");
    private static final RenderPipeline[] PIPELINES = new RenderPipeline[4];
    private static final QuasarMesh MESH = new QuasarMesh("TA-02 Quasar Jets film");

    private static int blend = BLENDED;
    /** The picture this scene is drawn into ({@link #beginOverlay}), or null outside a scene. */
    @Nullable
    private static GpuTextureView target;
    @Nullable
    private static GpuBuffer uniforms;
    @Nullable
    private static PerspectiveProjectionMatrixBuffer projection;
    @Nullable
    private static Scene scene;

    private JetGl() {
    }

    /** Compiles the film's pipelines now: how many failed (logged). */
    static int precompile() {
        int bad = 0;
        for (int i = 0; i < PIPELINES.length; i++) {
            bad += dev.adamko.ton618addition.client.quasar.QuasarGl.precompile(pipeline(i));
        }
        return bad;
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
        GpuTextureView into = target;
        if (into == null) {
            if (mesh != null) {
                mesh.close();
            }
            return;
        }
        if (!MESH.upload(mesh)) {
            return;
        }
        GpuDevice device = RenderSystem.getDevice();
        if (uniforms == null || uniforms.isClosed()) {
            uniforms = device.createBuffer(() -> "TA-02 Quasar Jets film parameters", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                UNIFORM_SIZE);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE).putMat4f(view)
                .putVec4((float) mode, 0F, 0F, 0F).putVec4(p0, p1, p2, p3).putVec4(q0, q1, q2, q3).get();
            device.createCommandEncoder().writeToBuffer(uniforms.slice(), data);
        }
        if (projection == null) {
            projection = new PerspectiveProjectionMatrixBuffer("TA-02 Quasar Jets film projection");
        }
        GpuBufferSlice projectionSlice = projection.getBuffer(proj);
        try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "TA-02 Quasar Jets film", into, OptionalInt.empty())) {
            pass.setPipeline(pipeline(blend));
            pass.setUniform("Projection", projectionSlice);
            pass.setUniform("JetParams", uniforms);
            MESH.draw(pass);
        }
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode, float p0, float p1, float p2, float p3) {
        draw(b, view, proj, mode, p0, p1, p2, p3, 0F, 0F, 0F, 0F);
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj, int mode) {
        draw(b, view, proj, mode, 1F, 0F, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    private static RenderPipeline pipeline(int blendMode) {
        if (PIPELINES[blendMode] == null) {
            RenderPipeline.Builder b = RenderPipeline.builder()
                .withLocation(Ton618Addition.id("pipeline/quasar_jets_" + blendMode))
                .withVertexShader(Ton618Addition.id("core/quasar_jets"))
                .withFragmentShader(Ton618Addition.id("core/quasar_jets"))
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withUniform("JetParams", UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                .withDepthWrite(false)
                .withCull(false)
                .withColorWrite(true, false);
            switch (blendMode) {
                case ADDITIVE -> b.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE));
                case PREMULTIPLIED -> b.withBlend(new BlendFunction(SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA));
                case BLENDED -> b.withBlend(BlendFunction.TRANSLUCENT);
                default -> b.withoutBlend();
            }
            PIPELINES[blendMode] = b.build();
        }
        return PIPELINES[blendMode];
    }

    // ---- blending ------------------------------------------------------------------------------------------------------

    /** Light added to what's there. */
    static void additive() {
        blend = ADDITIVE;
    }

    /** Colour already multiplied by its alpha (the hole: its light adds, its shadow covers; the star; the mirror). */
    static void premultiplied() {
        blend = PREMULTIPLIED;
    }

    static void opaque() {
        blend = OPAQUE;
    }

    /**
     * A scene of our own over everything: no depth, both faces. On 1.21.11 it is drawn into a picture of the film's own:
     * JetPost's high-range one when {@code JetPost.begin()} said yes, else {@link Scene} (cleared to black here, the size
     * of the window). {@link #present} puts the result on screen.
     */
    static void beginOverlay() {
        GpuTextureView post = JetPost.sceneView();
        if (post != null) {
            target = post;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int width = Math.max(1, mc.getWindow().getWidth());
        int height = Math.max(1, mc.getWindow().getHeight());
        if (scene == null || !scene.ready() || scene.width != width || scene.height != height) {
            scene = new Scene(width, height);
            mc.getTextureManager().register(SCENE, scene);
        }
        RenderSystem.getDevice().createCommandEncoder().clearColorTexture(scene.getTexture(), 0xFF000000);
        target = scene.getTextureView();
    }

    static void end() {
        blend = BLENDED;
        target = null;
    }

    /**
     * The space scene drawn since {@link #beginOverlay()} (through JetPost's glow if it ran), onto the screen, between the
     * HUD under it and the film's words over it.
     */
    static void present(GuiGraphics graphics) {
        Identifier shown = JetPost.finished();
        if (shown == null) {
            if (scene == null || !scene.ready()) {
                return;
            }
            shown = SCENE;
        }
        graphics.nextStratum();
        graphics.blit(RenderPipelines.GUI_TEXTURED, shown, 0, 0, 0.0F, -graphics.guiHeight(),
            graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), -graphics.guiHeight());
        graphics.nextStratum();
    }

    /** The scene's own picture, when the glow can't be made: plain colours, 8 bits a channel. */
    private static final class Scene extends AbstractTexture {
        final int width;
        final int height;

        Scene(int width, int height) {
            this.width = width;
            this.height = height;
            GpuDevice device = RenderSystem.getDevice();
            this.texture = device.createTexture(SCENE::toString, GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING,
                TextureFormat.RGBA8, width, height, 1, 1);
            this.textureView = device.createTextureView(this.texture);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        }

        boolean ready() {
            return this.texture != null && !this.texture.isClosed();
        }
    }
}
