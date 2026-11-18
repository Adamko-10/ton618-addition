package dev.adamko.ton618addition.client.quasarfilm;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasar.QuasarMesh;
import java.nio.ByteBuffer;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

/**
 * <b>FABLE'S FILE</b> (ported to Minecraft 26.3's renderer by Opus, 2026-11-18). The glow of the film's space scene
 * (the quality redo, 2026-11-16). The scene is not drawn straight onto the screen: {@link #begin} gives it a picture of
 * the film's own that can hold colours far brighter than white, and {@link #end} brings it to a picture the HUD shows
 * ({@code JetGl.present}): what is brighter than white spills light round itself (bloom), and the whole range is eased
 * into what a screen can show ({@code shaders/core/quasar_post}). That is what makes the star, the lit quasar and its
 * jets look as if they shine.
 *
 * <p><b>Minecraft 26.3</b> has 16-bit float textures of its own ({@code RGBA16_FLOAT}): the same glow and the same look as
 * on NeoForge. If they can't be made here, {@link #begin} says no and the scene is drawn without its glow, as on any
 * computer where the NeoForge one fails.
 */
final class JetPost {
    /** The scene, then the bright parts at 1/2, 1/4, 1/8 and 1/16 size, then the glow built back up at 1/8 and 1/4. */
    private static final int LEVELS = 4;
    /** PostParams: Mode (x), Param and Param2. */
    private static final int UNIFORM_SIZE = 3 * 16;
    private static final Identifier FINAL = Ton618Addition.id("quasar_film_glow");
    private static final BindGroupLayout POST_PARAMS = BindGroupLayout.builder().withUniform("PostParams", UniformType.UNIFORM_BUFFER).build();
    /** A pass into the glow's 16-bit float pictures, and the last one into the 8-bit picture the HUD shows. */
    private static final RenderPipeline PASS = pass("hdr", GpuFormat.RGBA16_FLOAT);
    private static final RenderPipeline PASS_FINAL = pass("final", GpuFormat.RGBA8_UNORM);
    private static final ByteBufferBuilder BYTES = new ByteBufferBuilder(1 << 10);

    private static RenderPipeline pass(String name, GpuFormat format) {
        return RenderPipeline.builder()
            .withLocation(Ton618Addition.id("pipeline/quasar_post_" + name))
            .withVertexShader(Ton618Addition.id("core/quasar_post"))
            .withFragmentShader(Ton618Addition.id("core/quasar_post"))
            .withBindGroupLayout(BindGroupLayouts.SAMPLER0_SAMPLER1)
            .withBindGroupLayout(POST_PARAMS)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withColorTargetState(new ColorTargetState(Optional.empty(), format, ColorTargetState.WRITE_ALL))
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .build();
    }
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
        return dev.adamko.ton618addition.client.quasar.QuasarGl.precompile(PASS) + dev.adamko.ton618addition.client.quasar.QuasarGl.precompile(PASS_FINAL);
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
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(scene.getTexture(), new Vector4f(0F, 0F, 0F, 1F));
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
        BufferBuilder q = new BufferBuilder(BYTES, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
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
                Optional.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(to.hdr ? PASS : PASS_FINAL));
            pass.setUniform("PostParams", uniforms);
            pass.setUniform("Sampler0", a.getTextureView(), smooth);
            pass.setUniform("Sampler1", (b == null ? a : b).getTextureView(), smooth);
            QUAD.draw(pass);
        }
    }

    /**
     * One of the glow's pictures: {@code hdr}, 16-bit floats a channel, else plain 8 bits.
     */
    private static final class Target extends AbstractTexture {
        private final String name;
        final boolean hdr;
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
            // (26.3's explicit clear needs COPY_DST, as the SA Series found)
            GpuTexture made = device.createTexture(() -> "TA-02 Quasar Jets glow " + this.name,
                GpuTexture.USAGE_RENDER_ATTACHMENT | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
                this.hdr ? GpuFormat.RGBA16_FLOAT : GpuFormat.RGBA8_UNORM, w, h, 1, 1);
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
