package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Drawing for TA-02's world effects (the named stars, the second sun's glare, the jet, the lobe): coloured triangles
 * built round the camera each frame, added as light (no textures, vanilla's position-colour shader), drawn with the
 * frame's own view and projection as the film's {@code FilmGl} does. Soft edges come from vertices whose alpha is 0.
 */
final class QuasarGl {
    @Nullable
    private static VertexBuffer buffer;

    private QuasarGl() {
    }

    static BufferBuilder begin() {
        return Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    }

    /** One vertex; colour is premultiplied by alpha (the blend adds it). */
    static void vertex(BufferBuilder b, float x, float y, float z, int rgb, float a) {
        float k = a < 0f ? 0f : Math.min(a, 1f);
        b.addVertex(x, y, z).setColor(((rgb >> 16) & 255) / 255f * k, ((rgb >> 8) & 255) / 255f * k, (rgb & 255) / 255f * k, k);
    }

    static void vertex(BufferBuilder b, Vector3f p, int rgb, float a) {
        vertex(b, p.x, p.y, p.z, rgb, a);
    }

    /** A soft round glow facing the camera: bright in the middle, gone at {@code radius}. */
    static void glow(BufferBuilder b, Vector3f c, Vector3f right, Vector3f up, float radius, int rgb, float a) {
        int n = 20;
        Vector3f prev = null;
        for (int i = 0; i <= n; i++) {
            double ang = i * Math.PI * 2.0 / n;
            Vector3f p = new Vector3f(c)
                .add(new Vector3f(right).mul((float) Math.cos(ang) * radius))
                .add(new Vector3f(up).mul((float) Math.sin(ang) * radius));
            if (prev != null) {
                vertex(b, c, rgb, a);
                vertex(b, prev, rgb, 0f);
                vertex(b, p, rgb, 0f);
            }
            prev = p;
        }
    }

    /** A soft band from {@code from} to {@code to}, {@code half} wide each side of its axis, turned to face the eye (0,0,0). */
    static void band(BufferBuilder b, Vector3f from, Vector3f to, float half, int rgb, float a) {
        Vector3f axis = new Vector3f(to).sub(from);
        Vector3f mid = new Vector3f(from).add(to).mul(0.5f);
        Vector3f side = new Vector3f(axis).cross(mid);
        if (side.lengthSquared() < 1.0e-8f) {
            return;
        }
        side.normalize(half);
        Vector3f fl = new Vector3f(from).sub(side);
        Vector3f fr = new Vector3f(from).add(side);
        Vector3f tl = new Vector3f(to).sub(side);
        Vector3f tr = new Vector3f(to).add(side);
        // two halves, bright on the axis and clear at the edges
        vertex(b, from, rgb, a);
        vertex(b, to, rgb, a);
        vertex(b, tl, rgb, 0f);
        vertex(b, from, rgb, a);
        vertex(b, tl, rgb, 0f);
        vertex(b, fl, rgb, 0f);
        vertex(b, from, rgb, a);
        vertex(b, fr, rgb, 0f);
        vertex(b, tr, rgb, 0f);
        vertex(b, from, rgb, a);
        vertex(b, tr, rgb, 0f);
        vertex(b, to, rgb, a);
    }

    static void draw(BufferBuilder b, Matrix4f view, Matrix4f proj) {
        MeshData mesh = b.build();
        if (mesh == null) {
            return;
        }
        ShaderInstance shader = GameRenderer.getPositionColorShader();
        if (shader == null) {
            mesh.close();
            return;
        }
        if (buffer == null) {
            buffer = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
        }
        buffer.bind();
        buffer.upload(mesh);
        buffer.drawWithShader(view, proj, shader);
        VertexBuffer.unbind();
    }

    /** Light added in the world: hidden by what stands in front (unless {@code depth} is false), writes no depth. */
    static void additive(boolean depth) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        if (depth) {
            RenderSystem.enableDepthTest();
        } else {
            RenderSystem.disableDepthTest();
        }
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
