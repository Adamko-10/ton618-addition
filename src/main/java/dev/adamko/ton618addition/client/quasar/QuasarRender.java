package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarStars;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * TA-02's world effects, drawn for everyone who can see them (not the film's: that is Fable's). After the sky: our
 * named stars in the night sky and the second sun's glare. After the weather: the ground mark, the jet coming in from
 * the horizon, the radio lobe and its pop. Also keeps the frame's sky view and projection for {@link QuasarReticle}.
 */
final class QuasarRender {
    /** Our sky things are drawn this far from the eye (inside even the shortest render distance's far plane). */
    private static final float SKY = 100f;

    static final Matrix4f skyView = new Matrix4f();
    static final Matrix4f skyProj = new Matrix4f();
    static boolean skyKnown;

    private QuasarRender() {
    }

    static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            skyView.set(event.getModelViewMatrix());
            skyProj.set(event.getProjectionMatrix());
            skyKnown = true;
            sky(event, level);
        } else if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            world(event);
        }
    }

    // ---- the sky -------------------------------------------------------------------------------------------------------

    private static void sky(RenderLevelStageEvent event, ClientLevel level) {
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Camera camera = event.getCamera();
        Vector3f left = camera.getLeftVector();
        Vector3f right = new Vector3f(-left.x, -left.y, -left.z);
        Vector3f up = new Vector3f(camera.getUpVector());
        float night = level.getStarBrightness(partial) * (1f - level.getRainLevel(partial));
        QuasarCast cast = QuasarClient.current();
        float sun = QuasarClient.secondSun(partial);
        if (night <= 0.01f && sun <= 0.01f) {
            return;
        }
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        Matrix4f proj = new Matrix4f(event.getProjectionMatrix());
        QuasarGl.additive(false);
        BufferBuilder b = QuasarGl.begin();
        boolean any = false;
        // our 8 named stars: bigger and brighter than vanilla's, in their colours (the locked one pulses)
        float strength = Math.min(1f, night * 2.2f);
        // only while holding TON 618's item or the Stellar Remote with TA-02 picked (Adam, 2026-11-16)
        if (strength > 0.01f && QuasarClient.holdingQuasarJets()) {
            for (int i = 0; i < QuasarStars.ALL.size(); i++) {
                QuasarStars.Star star = QuasarStars.ALL.get(i);
                if (!QuasarClient.starShown(i)) {
                    continue;
                }
                Vector3f at = toVector(star.direction()).mul(SKY);
                float size = star.type().size;
                float pulse = i == QuasarClient.lockedStar() ? 1.3f + 0.3f * (float) Math.sin(level.getGameTime() * 0.4) : 1f;
                QuasarGl.glow(b, at, right, up, 2.2f * size * pulse, star.type().color, 0.55f * strength);
                QuasarGl.glow(b, at, right, up, 0.55f * size * pulse, 0xFFFFFF, strength);
                any = true;
            }
        }
        // the second sun: TON618 blazing as a quasar, low in the sky
        if (cast != null && sun > 0.01f) {
            Vector3f at = toVector(cast.quasarDirection()).mul(SKY);
            QuasarGl.glow(b, at, right, up, 38f, 0x7FA8FF, 0.45f * sun);
            QuasarGl.glow(b, at, right, up, 9f, 0xDDEBFF, 0.9f * sun);
            QuasarGl.glow(b, at, right, up, 2.5f, 0xFFFFFF, sun);
            any = true;
        }
        if (any) {
            QuasarGl.draw(b, view, proj);
        } else {
            com.mojang.blaze3d.vertex.MeshData empty = b.build();
            if (empty != null) {
                empty.close();
            }
        }
        QuasarGl.end();
    }

    // ---- in the world --------------------------------------------------------------------------------------------------

    private static void world(RenderLevelStageEvent event) {
        QuasarCast cast = QuasarClient.current();
        if (cast == null) {
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float t = cast.tick(partial);
        if (t > QuasarClock.POP + 30) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 eye = camera.getPosition();
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        Matrix4f proj = new Matrix4f(event.getProjectionMatrix());
        QuasarStars.Type type = cast.star().type();
        Vector3f mark = new Vector3f((float) (cast.mark().x - eye.x), (float) (cast.mark().y - eye.y), (float) (cast.mark().z - eye.z));
        QuasarGl.additive(true);
        BufferBuilder b = QuasarGl.begin();

        // the ground mark, until the pop
        if (t < QuasarClock.POP) {
            float pulse = t < 24 ? 1f + 1.5f * (1f - t / 24f) : 1f + 0.08f * (float) Math.sin(t * 0.3f);
            ring(b, mark, 1.2f * pulse, 1.7f * pulse, type.color, 0.7f);
        }

        // the jet: from the horizon to its head, then to the mark (SWEEP..POP+6)
        if (t >= QuasarClock.SWEEP && t < QuasarClock.POP + 6) {
            float fade = t > QuasarClock.POP ? 1f - (t - QuasarClock.POP) / 6f : 1f;
            double head = QuasarClock.beamHead(t);
            double far = Math.min(QuasarClock.BEAM_FAR, Minecraft.getInstance().gameRenderer.getDepthFar() * 0.95);
            if (head < far) {
                Vector3f dir = toVector(cast.quasarDirection());
                Vector3f from = new Vector3f(mark).add(new Vector3f(dir).mul((float) head));
                Vector3f to = new Vector3f(mark).add(new Vector3f(dir).mul((float) far));
                float core = (float) (type.size * 0.45 + 0.2);
                QuasarGl.band(b, from, to, core * 3.4f, type.color, 0.35f * fade);
                QuasarGl.band(b, from, to, core, 0xE6F0FF, 0.95f * fade);
                if (head < 1.0) {
                    // where it lands
                    Vector3f camRight = new Vector3f(camera.getLeftVector()).negate();
                    QuasarGl.glow(b, new Vector3f(mark).add(0, 0.6f, 0), camRight, new Vector3f(camera.getUpVector()),
                        3.5f + core * 3f, 0xE6F0FF, 0.8f * fade);
                }
            }
        }

        // the radio lobe (LOBE..POP), then its pop: the dome jumps and is gone, a ring runs out
        float lobe = QuasarClock.lobe(t);
        if (lobe > 0f) {
            dome(b, mark, (float) type.lobe * lobe, type.color, 0.9f);
        }
        if (t >= QuasarClock.POP && t < QuasarClock.POP + 6) {
            float u = (t - QuasarClock.POP) / 6f;
            dome(b, mark, (float) type.lobe * (1f + 0.3f * u), 0xFFFFFF, 1.2f * (1f - u));
        }
        if (t >= QuasarClock.POP && t < QuasarClock.POP + 26) {
            float u = (t - QuasarClock.POP) / 26f;
            float r = (float) type.lobe * (1f + 3.5f * u);
            ring(b, new Vector3f(mark).add(0, 0.4f, 0), r * 0.94f, r, type.color, 0.9f * (1f - u));
        }
        QuasarGl.draw(b, view, proj);
        QuasarGl.end();
    }

    /** A flat soft ring on the ground. */
    private static void ring(BufferBuilder b, Vector3f c, float inner, float outer, int rgb, float a) {
        int n = 48;
        float mid = (inner + outer) * 0.5f;
        for (int i = 0; i < n; i++) {
            double a0 = i * Math.PI * 2.0 / n;
            double a1 = (i + 1) * Math.PI * 2.0 / n;
            Vector3f i0 = at(c, a0, inner);
            Vector3f i1 = at(c, a1, inner);
            Vector3f m0 = at(c, a0, mid);
            Vector3f m1 = at(c, a1, mid);
            Vector3f o0 = at(c, a0, outer);
            Vector3f o1 = at(c, a1, outer);
            quad(b, i0, i1, m1, m0, rgb, 0f, 0f, a, a);
            quad(b, m0, m1, o1, o0, rgb, a, a, 0f, 0f);
        }
    }

    /** A half sphere over the mark, lit at its rim (where it is seen edge-on) so it reads as a bubble. */
    private static void dome(BufferBuilder b, Vector3f c, float r, int rgb, float a) {
        int rings = 12;
        int segs = 36;
        for (int i = 0; i < rings; i++) {
            double p0 = Math.PI * 0.5 * i / rings;
            double p1 = Math.PI * 0.5 * (i + 1) / rings;
            for (int j = 0; j < segs; j++) {
                double q0 = Math.PI * 2.0 * j / segs;
                double q1 = Math.PI * 2.0 * (j + 1) / segs;
                Vector3f v00 = sphere(c, r, p0, q0);
                Vector3f v01 = sphere(c, r, p0, q1);
                Vector3f v11 = sphere(c, r, p1, q1);
                Vector3f v10 = sphere(c, r, p1, q0);
                quad(b, v00, v01, v11, v10, rgb, rim(v00, c, a), rim(v01, c, a), rim(v11, c, a), rim(v10, c, a));
            }
        }
    }

    private static float rim(Vector3f p, Vector3f c, float a) {
        Vector3f normal = new Vector3f(p).sub(c).normalize();
        Vector3f toEye = new Vector3f(p).negate().normalize();
        float edge = 1f - Math.abs(normal.dot(toEye));
        return a * (0.12f + 0.75f * edge * edge);
    }

    private static Vector3f sphere(Vector3f c, float r, double polar, double around) {
        // polar 0 = the ground ring, pi/2 = the top
        return new Vector3f(c).add((float) (Math.cos(polar) * Math.cos(around) * r), (float) (Math.sin(polar) * r),
            (float) (Math.cos(polar) * Math.sin(around) * r));
    }

    private static Vector3f at(Vector3f c, double angle, float r) {
        return new Vector3f(c).add((float) Math.cos(angle) * r, 0.05f, (float) Math.sin(angle) * r);
    }

    private static void quad(BufferBuilder b, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3, int rgb, float a0, float a1,
                             float a2, float a3) {
        QuasarGl.vertex(b, p0, rgb, a0);
        QuasarGl.vertex(b, p1, rgb, a1);
        QuasarGl.vertex(b, p2, rgb, a2);
        QuasarGl.vertex(b, p0, rgb, a0);
        QuasarGl.vertex(b, p2, rgb, a2);
        QuasarGl.vertex(b, p3, rgb, a3);
    }

    static Vector3f toVector(Vec3 v) {
        return new Vector3f((float) v.x, (float) v.y, (float) v.z);
    }
}
