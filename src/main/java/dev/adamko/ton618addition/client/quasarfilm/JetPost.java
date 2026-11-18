package dev.adamko.ton618addition.client.quasarfilm;

import dev.adamko.ton618addition.client.quasar.Gl1201;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.adamko.ton618addition.Ton618Addition;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.joml.Matrix4f;

/**
 * <b>FABLE'S FILE.</b> The glow of the film's space scene (the quality redo, 2026-11-16). The scene is not drawn
 * straight onto the screen any more: {@link #begin} points the drawing at a picture of the film's own that can hold
 * colours far brighter than white, and {@link #end} brings it to the screen: what is brighter than white spills light
 * round itself (bloom), and the whole range is eased into what a screen can show ({@code shaders/quasarfilm/quasar_post}).
 * That is what makes the star, the lit quasar and its jets look as if they shine.
 *
 * <p>If any of it can't be set up on this computer, {@link #begin} says no and the scene is drawn straight onto the
 * screen as before; nothing else depends on it.
 */
final class JetPost {
    private static final int GL_RGBA16F = 0x881A;
    private static final int GL_RGBA = 0x1908;
    private static final int GL_FLOAT = 0x1406;
    private static final int GL_LINEAR = 0x2601;
    private static final Matrix4f IDENTITY = new Matrix4f();
    /** The scene, then the bright parts at 1/2, 1/4, 1/8 and 1/16 size, then the glow built back up at 1/8 and 1/4. */
    private static final int LEVELS = 4;

    @Nullable
    private static ShaderInstance shader;
    @Nullable
    private static VertexBuffer buffer;
    @Nullable
    private static Hdr scene;
    private static final Hdr[] DOWN = new Hdr[LEVELS];
    private static final Hdr[] UP = new Hdr[LEVELS - 2];
    private static boolean broken;

    private JetPost() {
    }

    static void register(RegisterShadersEvent event) {
        ResourceProvider base = event.getResourceProvider();
        ResourceProvider film = location -> {
            String path = location.getPath();
            return base.getResource(path.startsWith("shaders/core/") ? location.withPath("shaders/quasarfilm/" + path.substring(13)) : location);
        };
        try {
            event.registerShader(new ShaderInstance(film, new ResourceLocation(Ton618Addition.MOD_ID, "quasar_post"),
                DefaultVertexFormat.POSITION_TEX_COLOR), loaded -> shader = loaded);
        } catch (IOException | RuntimeException failed) {
            Ton618Addition.LOG.error("The Quasar Jets film's glow shader didn't load: its space scene is drawn without glow", failed);
            shader = null;
        }
    }

    /**
     * From now on the scene's drawing goes into the film's own picture (cleared to black). False: that can't be done
     * here, draw straight onto the screen.
     */
    static boolean begin() {
        if (shader == null || broken) {
            return false;
        }
        try {
            RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
            int w = Math.max(16, main.width);
            int h = Math.max(16, main.height);
            scene = sized(scene, w, h);
            for (int i = 0; i < LEVELS; i++) {
                DOWN[i] = sized(DOWN[i], Math.max(4, w >> i + 1), Math.max(4, h >> i + 1));
            }
            for (int i = 0; i < UP.length; i++) {
                UP[i] = sized(UP[i], DOWN[i + 1].width, DOWN[i + 1].height);
            }
            scene.setClearColor(0F, 0F, 0F, 1F);
            scene.clear(Minecraft.ON_OSX);
            scene.bindWrite(true);
            return true;
        } catch (RuntimeException failed) {
            fail(failed);
            return false;
        }
    }

    /**
     * The scene is drawn: its glow is made and the finished picture goes onto the screen. {@code bloom}: how strong the
     * glow is; {@code exposure}: how bright the whole picture is; {@code clock}: any running number, for the grain.
     */
    static void end(float bloom, float exposure, float clock) {
        Hdr from = scene;
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (from == null || shader == null) {
            main.bindWrite(true);
            return;
        }
        try {
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            // the bright parts, at half size; then halved three more times
            pass(DOWN[0], 0, from, null, 1F / from.width, 1F / from.height, 0.85F, 0F, 0F, 0F, 0F, 0F);
            for (int i = 1; i < LEVELS; i++) {
                pass(DOWN[i], 1, DOWN[i - 1], null, 1F / DOWN[i - 1].width, 1F / DOWN[i - 1].height, 0F, 0F, 0F, 0F, 0F, 0F);
            }
            // built back up: each size's glow is the smaller one's, spread, plus its own
            Hdr small = DOWN[LEVELS - 1];
            for (int i = UP.length - 1; i >= 0; i--) {
                pass(UP[i], 2, small, DOWN[i + 1], 1F / small.width, 1F / small.height, 1F, 0F, 0F, 0F, 0F, 0F);
                small = UP[i];
            }
            pass(main, 3, from, small, 1F / from.width, 1F / from.height, 0F, 0F, bloom, exposure, 0.55F, clock);
        } catch (RuntimeException failed) {
            fail(failed);
        } finally {
            main.bindWrite(true);
            RenderSystem.setShaderTexture(1, 0);
        }
    }

    private static void fail(RuntimeException failed) {
        broken = true;
        Ton618Addition.LOG.error("The Quasar Jets film's glow can't be made here: its space scene is drawn without it", failed);
        Gl1201.clear();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
    }

    private static Hdr sized(@Nullable Hdr target, int w, int h) {
        if (target == null) {
            return new Hdr(w, h);
        }
        if (target.width != w || target.height != h) {
            target.resize(w, h, Minecraft.ON_OSX);
        }
        return target;
    }

    /** One pass over the whole of {@code to}, reading {@code a} (and {@code b}). */
    private static void pass(RenderTarget to, int mode, RenderTarget a, @Nullable RenderTarget b, float p0, float p1, float p2, float p3,
            float q0, float q1, float q2, float q3) {
        ShaderInstance program = shader;
        if (program == null) {
            return;
        }
        to.bindWrite(true);
        RenderSystem.setShaderTexture(0, a.getColorTextureId());
        RenderSystem.setShaderTexture(1, b == null ? a.getColorTextureId() : b.getColorTextureId());
        program.safeGetUniform("Mode").set((float) mode);
        program.safeGetUniform("Param").set(p0, p1, p2, p3);
        program.safeGetUniform("Param2").set(q0, q1, q2, q3);
        BufferBuilder q = Gl1201.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        q.vertex(-1F, -1F, 0F).uv(0F, 0F).color(1F, 1F, 1F, 1F).endVertex();
        q.vertex(1F, -1F, 0F).uv(1F, 0F).color(1F, 1F, 1F, 1F).endVertex();
        q.vertex(1F, 1F, 0F).uv(1F, 1F).color(1F, 1F, 1F, 1F).endVertex();
        q.vertex(-1F, 1F, 0F).uv(0F, 1F).color(1F, 1F, 1F, 1F).endVertex();
        BufferBuilder.RenderedBuffer mesh = q.end();
        if (mesh.isEmpty()) {
            mesh.release();
            return;
        }
        if (buffer == null) {
            buffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        buffer.bind();
        buffer.upload(mesh);
        buffer.drawWithShader(IDENTITY, IDENTITY, program);
        VertexBuffer.unbind();
    }

    /** A picture that holds colours brighter than white (16-bit floats), read back smoothly. */
    private static final class Hdr extends TextureTarget {
        Hdr(int width, int height) {
            super(width, height, false, Minecraft.ON_OSX);
        }

        @Override
        public void createBuffers(int width, int height, boolean clearError) {
            super.createBuffers(width, height, clearError);
            // the game makes it 8 bits a channel: the same picture again, as floats
            GlStateManager._bindTexture(this.colorTextureId);
            GlStateManager._texImage2D(3553, 0, GL_RGBA16F, this.width, this.height, 0, GL_RGBA, GL_FLOAT, (IntBuffer) null);
            GlStateManager._bindTexture(0);
            this.setFilterMode(GL_LINEAR);
        }
    }
}
