package dev.adamko.ton618addition.client.quasarfilm;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasar.QuasarMesh;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryStack;

/**
 * <b>FABLE'S FILE</b> (ported to Minecraft 1.21.11's renderer by Opus, 2026-11-18). The glow of the film's space scene
 * (the quality redo, 2026-11-16). The scene is not drawn straight onto the screen: {@link #begin} gives it a picture of
 * the film's own that can hold colours far brighter than white, and {@link #end} brings it to a picture the HUD shows
 * ({@code JetGl.present}): what is brighter than white spills light round itself (bloom), and the whole range is eased
 * into what a screen can show ({@code shaders/core/quasar_post}). That is what makes the star, the lit quasar and its
 * jets look as if they shine.
 *
 * <p><b>Minecraft 1.21.11</b> offers no texture format brighter than white (RGBA8, RED8, RED8I, DEPTH32 only). Its OpenGL
 * textures are made with a plain {@code glTexImage2D}, so, as the NeoForge branch does with its render targets, each of
 * ours is made again at once as 16-bit floats ({@code GL_RGBA16F}) under the same name: the same glow and the same look
 * as on NeoForge (Opus's pick for the port, 2026-11-18). If that can't be done here (another graphics backend, an error),
 * {@link #begin} says no and the scene is drawn without its glow, as on any computer where the NeoForge one fails.
 */
final class JetPost {
    private static final int GL_TEXTURE_2D = 0x0DE1;
    private static final int GL_RGBA16F = 0x881A;
    private static final int GL_RGBA = 0x1908;
    private static final int GL_FLOAT = 0x1406;
    /** The scene, then the bright parts at 1/2, 1/4, 1/8 and 1/16 size, then the glow built back up at 1/8 and 1/4. */
    private static final int LEVELS = 4;
    /** PostParams: Mode (x), Param and Param2. */
    private static final int UNIFORM_SIZE = 3 * 16;
    private static final Identifier FINAL = Ton618Addition.id("quasar_film_glow");
    private static final RenderPipeline PASS = RenderPipeline.builder()
        .withLocation(Ton618Addition.id("pipeline/quasar_post"))
        .withVertexShader(Ton618Addition.id("core/quasar_post"))
        .withFragmentShader(Ton618Addition.id("core/quasar_post"))
        .withUniform("PostParams", UniformType.UNIFORM_BUFFER)
        .withSampler("Sampler0")
        .withSampler("Sampler1")
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withDepthWrite(false)
        .withCull(false)
        .withoutBlend()
        .withColorWrite(true, true)
        .build();
    private static final QuasarMesh QUAD = new QuasarMesh("TA-02 Quasar Jets glow");

    @Nullable
    private static Target scene;
    private static final Target[] DOWN = new Target[LEVELS];
    private static final Target[] UP = new Target[LEVELS - 2];
    /** The finished picture (8 bits a channel), registered so the HUD can show it. */
    @Nullable
    private static Target done;
    @Nullable
    private static GpuBuffer uniforms;
    /** The scene is being drawn into {@link #scene} (between {@link #begin} and {@link #end}). */
    private static boolean drawing;
    /** {@link #end} made {@link #done} for this scene. */
    private static boolean finished;
    private static boolean broken;

    private JetPost() {
    }

    /** Compiles the glow's pipeline now: 1 if it failed (logged). */
    static int precompile() {
        return dev.adamko.ton618addition.client.quasar.QuasarGl.precompile(PASS);
    }

    /**
     * From now on the scene's drawing goes into the film's own picture (cleared to black). False: that can't be done
     * here, draw it without the glow.
     */
    static boolean begin() {
        finished = false;
        drawing = false;
        if (broken) {
            return false;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            int w = Math.max(16, mc.getWindow().getWidth());
            int h = Math.max(16, mc.getWindow().getHeight());
            scene = sized(scene, "scene", w, h, true);
            for (int i = 0; i < LEVELS; i++) {
                DOWN[i] = sized(DOWN[i], "down " + i, Math.max(4, w >> i + 1), Math.max(4, h >> i + 1), true);
            }
            for (int i = 0; i < UP.length; i++) {
                UP[i] = sized(UP[i], "up " + i, DOWN[i + 1].width, DOWN[i + 1].height, true);
            }
            done = sized(done, "final", w, h, false);
            if (!done.registered) {
                mc.getTextureManager().register(FINAL, done);
                done.registered = true;
            }
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(scene.getTexture(), 0xFF000000);
            drawing = true;
            return true;
        } catch (RuntimeException failed) {
            fail(failed);
            return false;
        }
    }

    /** Where the scene is drawn while {@link #begin} said yes, else null. */
    @Nullable
    static GpuTextureView sceneView() {
        Target into = scene;
        return drawing && into != null ? into.getTextureView() : null;
    }

    /** The finished picture to show for the scene just drawn, or null (no glow this time: show the plain scene). */
    @Nullable
    static Identifier finished() {
        return finished ? FINAL : null;
    }

    /**
     * The scene is drawn: its glow is made and the finished picture is ready for the HUD. {@code bloom}: how strong the
     * glow is; {@code exposure}: how bright the whole picture is; {@code clock}: any running number, for the grain.
     */
    static void end(float bloom, float exposure, float clock) {
        Target from = scene;
        boolean was = drawing;
        drawing = false;
        if (!was || from == null || done == null) {
            return;
        }
        try {
            // the bright parts, at half size; then halved three more times
            pass(DOWN[0], 0, from, null, 1F / from.width, 1F / from.height, 0.85F, 0F, 0F, 0F, 0F, 0F);
            for (int i = 1; i < LEVELS; i++) {
                pass(DOWN[i], 1, DOWN[i - 1], null, 1F / DOWN[i - 1].width, 1F / DOWN[i - 1].height, 0F, 0F, 0F, 0F, 0F, 0F);
            }
            // built back up: each size's glow is the smaller one's, spread, plus its own
            Target small = DOWN[LEVELS - 1];
            for (int i = UP.length - 1; i >= 0; i--) {
                pass(UP[i], 2, small, DOWN[i + 1], 1F / small.width, 1F / small.height, 1F, 0F, 0F, 0F, 0F, 0F);
                small = UP[i];
            }
            pass(done, 3, from, small, 1F / from.width, 1F / from.height, 0F, 0F, bloom, exposure, 0.55F, clock);
            finished = true;
        } catch (RuntimeException failed) {
            fail(failed);
        }
    }

    private static void fail(RuntimeException failed) {
        broken = true;
        drawing = false;
        finished = false;
        Ton618Addition.LOG.error("The Quasar Jets film's glow can't be made here: its space scene is drawn without it", failed);
        Tesselator.getInstance().clear();
    }

    private static Target sized(@Nullable Target target, String name, int w, int h, boolean hdr) {
        if (target != null && target.ready() && target.width == w && target.height == h) {
            return target;
        }
        if (target == null) {
            target = new Target(name, hdr);
        }
        target.make(w, h);
        return target;
    }

    /** One pass over the whole of {@code to}, reading {@code a} (and {@code b}). */
    private static void pass(Target to, int mode, Target a, @Nullable Target b, float p0, float p1, float p2, float p3, float q0, float q1,
            float q2, float q3) {
        BufferBuilder q = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        q.addVertex(-1F, -1F, 0F).setUv(0F, 0F).setColor(1F, 1F, 1F, 1F);
        q.addVertex(1F, -1F, 0F).setUv(1F, 0F).setColor(1F, 1F, 1F, 1F);
        q.addVertex(1F, 1F, 0F).setUv(1F, 1F).setColor(1F, 1F, 1F, 1F);
        q.addVertex(-1F, 1F, 0F).setUv(0F, 1F).setColor(1F, 1F, 1F, 1F);
        if (!QUAD.upload(q.build())) {
            return;
        }
        GpuDevice device = RenderSystem.getDevice();
        if (uniforms == null || uniforms.isClosed()) {
            uniforms = device.createBuffer(() -> "TA-02 Quasar Jets glow parameters", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                UNIFORM_SIZE);
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
                .putVec4((float) mode, 0F, 0F, 0F).putVec4(p0, p1, p2, p3).putVec4(q0, q1, q2, q3).get();
            device.createCommandEncoder().writeToBuffer(uniforms.slice(), data);
        }
        GpuSampler smooth = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "TA-02 Quasar Jets glow", to.getTextureView(),
                OptionalInt.empty())) {
            pass.setPipeline(PASS);
            pass.setUniform("PostParams", uniforms);
            pass.bindTexture("Sampler0", a.getTextureView(), smooth);
            pass.bindTexture("Sampler1", (b == null ? a : b).getTextureView(), smooth);
            QUAD.draw(pass);
        }
    }

    /**
     * One of the glow's pictures: {@code hdr}, 16-bit floats a channel (made again as such right after Minecraft makes it
     * 8 bits), else plain 8 bits.
     */
    private static final class Target extends AbstractTexture {
        private final String name;
        private final boolean hdr;
        int width;
        int height;
        boolean registered;

        Target(String name, boolean hdr) {
            this.name = name;
            this.hdr = hdr;
        }

        boolean ready() {
            return this.texture != null && !this.texture.isClosed();
        }

        void make(int w, int h) {
            this.release();
            GpuDevice device = RenderSystem.getDevice();
            GpuTexture made = device.createTexture(() -> "TA-02 Quasar Jets glow " + this.name,
                GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, w, h, 1, 1);
            if (this.hdr) {
                if (!(made instanceof GlTexture gl)) {
                    made.close();
                    throw new IllegalStateException("not an OpenGL texture (" + made.getClass().getName() + ")");
                }
                // the game made it 8 bits a channel: the same picture again, as floats (no view or framebuffer of it yet)
                GlStateManager._bindTexture(gl.glId());
                GlStateManager._texImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, w, h, 0, GL_RGBA, GL_FLOAT, null);
                GlStateManager._bindTexture(0);
            }
            this.texture = made;
            this.textureView = device.createTextureView(made);
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            this.width = w;
            this.height = h;
        }

        /** The texture manager closes it with the rest when resources reload; it is made again when next needed. */
        @Override
        public void close() {
            this.release();
            this.registered = false;
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
