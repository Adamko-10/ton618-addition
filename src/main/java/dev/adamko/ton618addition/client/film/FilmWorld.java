package dev.adamko.ton618addition.client.film;

import static dev.adamko.ton618addition.client.film.FilmScript.lerp;
import static dev.adamko.ton618addition.client.film.FilmScript.lin;
import static dev.adamko.ton618addition.client.film.FilmScript.sat;
import static dev.adamko.ton618addition.client.film.FilmScript.ss;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.adamko.ton618addition.client.LastLightCast;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import org.joml.Quaternionf;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * <b>FABLE'S FILE.</b> What the film draws in the world itself, hidden by whatever stands in front of it:
 * <ul>
 *   <li>in TON 618's dark (the previz's shots 2 and 3): the glow in the caster's hand and its light on the floor, the
 *       small ring of the press, and <b>the time wave</b> at RACE: a bright front over the floor with a wall of light
 *       on it, and behind it dashes rushing over the floor and dust shooting upward;</li>
 *   <li>back in the overworld (shots 9 and 10): <b>the returning wave</b>: the world is there inside its radius and
 *       white outside it (a white shell round the caster that grows; everything nearer than the shell shows), a wall of
 *       light on its front, and motes of light settling on the land.</li>
 * </ul>
 * The waves are pictures only: they move and hurt nothing.
 *
 * <p><b>When it is drawn.</b> In the dark, TON 618 paints its own picture over the finished level: every pixel that
 * holds nothing solid becomes its sky and TON618, and the dark's floor is invisible, so that is every pixel but the
 * players. Light drawn any earlier in the frame is painted out (the 1.0.0 film drew at AFTER_WEATHER and none of it
 * showed there). So in the dark the film draws at AFTER_LEVEL, once that picture is done and while the level's depth
 * is still there; in the overworld it draws at AFTER_WEATHER as before.
 */
final class FilmWorld {
    private static final int MOTES = 220;
    private static final float[] MOTE = new float[MOTES * 4];
    private static final int SETTLERS = 220;
    /** Each settling mote: x, z from the caster, the height it starts at, when it starts, how long it takes, its size. */
    private static final float[] SETTLER = new float[SETTLERS * 6];
    /** The ground under each settling mote (found once, when the overworld is there). */
    private static final float[] GROUND = new float[SETTLERS];
    private static boolean groundKnown;

    static {
        Random r = new Random(11L);
        for (int i = 0; i < MOTES; i++) {
            MOTE[i * 4] = (r.nextFloat() - 0.5F) * 30F;
            MOTE[i * 4 + 1] = r.nextFloat() * 10F;
            MOTE[i * 4 + 2] = (r.nextFloat() - 0.5F) * 30F;
            MOTE[i * 4 + 3] = 0.05F + r.nextFloat() * 0.06F;
        }
        Random s = new Random(31L);
        for (int i = 0; i < SETTLERS; i++) {
            float a = s.nextFloat() * 6.2832F;
            float d = 2F + (float) Math.sqrt(s.nextFloat()) * 34F;
            SETTLER[i * 6] = (float) Math.cos(a) * d;
            SETTLER[i * 6 + 1] = (float) Math.sin(a) * d;
            SETTLER[i * 6 + 2] = 20F + s.nextFloat() * 18F;
            SETTLER[i * 6 + 3] = 548F + s.nextFloat() * 46F;
            SETTLER[i * 6 + 4] = 50F + s.nextFloat() * 36F;
            SETTLER[i * 6 + 5] = 0.13F + s.nextFloat() * 0.17F;
        }
    }

    private FilmWorld() {
    }

    static void forget() {
        groundKnown = false;
    }

    static void render(@javax.annotation.Nullable WorldRenderContext context, LastLightCast cast, float t) {
        // Fabric 1.21.11: drawn at the end of the level (LastLightFilm picks the event), after TON 618's dark picture
        boolean dark = cast.inDark();
        if (!FilmGl.ready()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 eye = camera.position();
        Vector3f left = new Vector3f(camera.leftVector());
        Vector3f up = new Vector3f(camera.upVector());
        Vector3f right = new Vector3f(-left.x, -left.y, -left.z);
        // Minecraft 1.21.11: the camera's rotation as the level's view matrix; FilmGl uses the level's own projection here
        Matrix4f view = new Matrix4f().rotation(camera.rotation().conjugate(new Quaternionf()));
        Matrix4f proj = new Matrix4f();
        if (dark) {
            if (t < FilmScript.S_COSMOS + 4F) {
                dark(cast, t, partial, eye, right, up, view, proj);
            }
        } else if (t >= FilmScript.S_WORLD) {
            overworld(cast, t, partial, eye, right, up, view, proj);
        }
    }

    // ---- in the dark ----------------------------------------------------------------------------------------------------

    private static void dark(LastLightCast cast, float t, float partial, Vec3 eye, Vector3f right, Vector3f up, Matrix4f view, Matrix4f proj) {
        Vec3 centre = FilmCamera.feet(cast, partial);
        // everything is built round the camera, so far-off coordinates lose nothing
        float cx = (float) (centre.x - eye.x);
        float cy = (float) (centre.y - eye.y);
        float cz = (float) (centre.z - eye.z);
        float press = t >= FilmScript.S_PRESS ? (float) Math.exp(-(t - FilmScript.S_PRESS) / 7F) : 0F;
        float release = t >= FilmScript.S_RACE ? (float) Math.exp(-(t - FilmScript.S_RACE) / 8F) : 0F;
        float race = lin(FilmScript.S_RACE, FilmScript.S_COSMOS, t);
        float out = 1F - lin(FilmScript.S_COSMOS, FilmScript.S_COSMOS + 4F, t);
        float idle = lin(40F, 56F, t);
        float charge = 0.9F * lin(64F, FilmScript.S_RACE, t) * (1F - lin(FilmScript.S_RACE, 96F, t));

        FilmGl.beginWorld();
        FilmGl.additive();

        // the hand's light lying on the floor round the caster: the dark's floor can't be seen, this shows it is there
        float pool = sat((0.1F * idle + 0.4F * press + 0.3F * charge + 0.4F * release + 0.16F * race) * out);
        if (pool > 0.01F) {
            float s = 15F;
            BufferBuilder b = FilmGl.begin();
            FilmGl.vertex(b, cx - s, cy + 0.02F, cz - s, -1F, -1F, 1F, 0.62F, 0.3F, pool);
            FilmGl.vertex(b, cx - s, cy + 0.02F, cz + s, -1F, 1F, 1F, 0.62F, 0.3F, pool);
            FilmGl.vertex(b, cx + s, cy + 0.02F, cz + s, 1F, 1F, 1F, 0.62F, 0.3F, pool);
            FilmGl.vertex(b, cx + s, cy + 0.02F, cz - s, 1F, -1F, 1F, 0.62F, 0.3F, pool);
            FilmGl.draw(b, view, proj, FilmGl.DOT, 1F, 0F, 0F, 0F);
        }

        // the time wave over the floor, and time running fast behind it
        float wu = lin(FilmScript.S_RACE, 135F, t);
        float radius = 520F * (float) Math.pow(wu, 1.3);
        float strength = t >= FilmScript.S_RACE ? 1F - ss(0.7F, 1F, wu) : 0F;
        // Far off the wave is a thin line with TON618's bright disk behind it, so it is drawn stronger and whiter there
        // than in 1.0.0; while it is still close to the caster it is big on screen and stays as it was
        float away = lin(10F, 60F, radius);
        if (t >= FilmScript.S_RACE) {
            float half = 600F;
            float y = cy + 0.03F;
            BufferBuilder b = FilmGl.begin();
            FilmGl.vertex(b, cx - half, y, cz - half, -half, -half, 1F, 1F, 1F, 1F);
            FilmGl.vertex(b, cx - half, y, cz + half, -half, half, 1F, 1F, 1F, 1F);
            FilmGl.vertex(b, cx + half, y, cz + half, half, half, 1F, 1F, 1F, 1F);
            FilmGl.vertex(b, cx + half, y, cz - half, half, -half, 1F, 1F, 1F, 1F);
            FilmGl.draw(b, view, proj, FilmGl.WAVE, wu >= 1F ? 100000F : radius, strength * lerp(1F, 1.5F, away), (0.35F + 0.65F * race) * out,
                FilmScript.phase(t) * 4F, -cx, -cz, half, 0F);
            if (strength > 0.001F) {
                BufferBuilder w = FilmGl.begin();
                // the wall of light on the front (not in its first ticks: that small, it is a slab of light round the caster),
                // and once it is away a low white-hot foot under it
                wall(w, cx, cy, cz, Math.max(radius, 0.01F), 5F + radius * 0.07F, 1F, lerp(0.72F, 0.84F, away), lerp(0.47F, 0.62F, away),
                    strength * lerp(0.6F, 0.9F, away) * lin(2F, 12F, radius));
                if (away > 0F) {
                    wall(w, cx, cy, cz, Math.max(radius, 0.01F), 1.5F + radius * 0.014F, 1F, 0.96F, 0.88F, strength * away);
                }
                FilmGl.draw(w, view, proj, FilmGl.WALL);
            }
        }

        // the small ring of the press
        float ru = (t - FilmScript.S_PRESS) / 30F;
        if (ru > 0F && ru < 1F) {
            float s = 1F + 13F * FilmScript.easeOut(ru);
            // a wider, brighter band than 1.0.0's, with a soft glow trailing inside it (seen nearly edge on, a thin ring is lost)
            float a = (float) Math.pow(1F - ru, 1.5);
            BufferBuilder b = FilmGl.begin();
            FilmGl.vertex(b, cx - s, cy + 0.04F, cz - s, -1F, -1F, 1F, 0.86F, 0.66F, a);
            FilmGl.vertex(b, cx - s, cy + 0.04F, cz + s, -1F, 1F, 1F, 0.86F, 0.66F, a);
            FilmGl.vertex(b, cx + s, cy + 0.04F, cz + s, 1F, 1F, 1F, 0.86F, 0.66F, a);
            FilmGl.vertex(b, cx + s, cy + 0.04F, cz - s, 1F, -1F, 1F, 0.86F, 0.66F, a);
            FilmGl.draw(b, view, proj, FilmGl.RING, 0.9F, 0.3F, 0.35F, 0F);
        }

        // the glow in the hand: a flash at the press, growing while it charges, a burst as time is let go
        float glow = (0.25F * idle + 1.7F * press + charge + 2.6F * release + race * (0.5F + 0.3F * (float) Math.sin(t * 1.3))) * out;
        hand(cast, partial, eye, right, up, view, proj, glow, sat(press + release) * out, true);

        // dust shooting upward, faster and faster
        if (race > 0F) {
            float lift = race * race * 70F;
            float bright = (0.10F + 0.5F * race) * lin(FilmScript.S_RACE, FilmScript.S_RACE + 4F, t) * out;
            BufferBuilder b = FilmGl.begin();
            for (int i = 0; i < MOTES; i++) {
                float k = 0.6F + (i % 7) * 0.1F;
                float y = (MOTE[i * 4 + 1] + lift * k) % 10F;
                FilmGl.billboard(b, cx + MOTE[i * 4], cy + y, cz + MOTE[i * 4 + 2], right, up, MOTE[i * 4 + 3], MOTE[i * 4 + 3], bright,
                    bright * 0.6F, bright * 0.3F, 1F);
            }
            FilmGl.draw(b, view, proj, FilmGl.DOT, 1F, 0F, 0F, 0F);
            float streak = race * race * 2.4F;
            if (streak > 0.05F) {
                float a = race * 0.6F * out;
                b = FilmGl.begin();
                for (int i = 0; i < MOTES; i++) {
                    float k = 0.6F + (i % 7) * 0.1F;
                    float x = cx + MOTE[i * 4];
                    float z = cz + MOTE[i * 4 + 2];
                    float y = cy + (MOTE[i * 4 + 1] + lift * k) % 10F;
                    // a ribbon standing up, turned to face the camera
                    float sx = -z;
                    float sz = x;
                    float len = (float) Math.sqrt(sx * sx + sz * sz);
                    if (len < 0.001F) {
                        continue;
                    }
                    sx *= 0.03F / len;
                    sz *= 0.03F / len;
                    FilmGl.vertex(b, x - sx, y, z - sz, -1F, 0F, a, a * 0.63F, a * 0.31F, 1F);
                    FilmGl.vertex(b, x + sx, y, z + sz, 1F, 0F, a, a * 0.63F, a * 0.31F, 1F);
                    FilmGl.vertex(b, x + sx, y - streak * k, z + sz, 1F, 1F, 0F, 0F, 0F, 1F);
                    FilmGl.vertex(b, x - sx, y - streak * k, z - sz, -1F, 1F, 0F, 0F, 0F, 1F);
                }
                FilmGl.draw(b, view, proj, FilmGl.STREAK);
            }
        }
        FilmGl.end();
    }

    // ---- back in the overworld ------------------------------------------------------------------------------------------

    private static void overworld(LastLightCast cast, float t, float partial, Vec3 eye, Vector3f right, Vector3f up, Matrix4f view,
            Matrix4f proj) {
        Vec3 centre = FilmCamera.feet(cast, partial);
        float cx = (float) (centre.x - eye.x);
        float cy = (float) (centre.y - eye.y);
        float cz = (float) (centre.z - eye.z);
        float radius = FilmScript.returnRadius(t);
        float white = FilmScript.returnWhite(t);

        FilmGl.beginWorld();
        if (white > 0.002F) {
            // the returning wave: a white shell round the caster; what is nearer than it is the world, back
            FilmGl.blended();
            BufferBuilder b = FilmGl.begin();
            int rings = 12;
            int segments = 28;
            for (int i = 0; i < rings; i++) {
                double p0 = Math.PI * i / rings - Math.PI * 0.5;
                double p1 = Math.PI * (i + 1) / rings - Math.PI * 0.5;
                float y0 = (float) Math.sin(p0) * radius;
                float y1 = (float) Math.sin(p1) * radius;
                float r0 = (float) Math.cos(p0) * radius;
                float r1 = (float) Math.cos(p1) * radius;
                for (int j = 0; j < segments; j++) {
                    double a0 = Math.PI * 2.0 * j / segments;
                    double a1 = Math.PI * 2.0 * (j + 1) / segments;
                    float c0 = (float) Math.cos(a0);
                    float s0 = (float) Math.sin(a0);
                    float c1 = (float) Math.cos(a1);
                    float s1 = (float) Math.sin(a1);
                    FilmGl.vertex(b, cx + c0 * r0, cy + y0, cz + s0 * r0, 0F, 0F, 1F, 1F, 1F, white);
                    FilmGl.vertex(b, cx + c1 * r0, cy + y0, cz + s1 * r0, 0F, 0F, 1F, 1F, 1F, white);
                    FilmGl.vertex(b, cx + c1 * r1, cy + y1, cz + s1 * r1, 0F, 0F, 1F, 1F, 1F, white);
                    FilmGl.vertex(b, cx + c0 * r1, cy + y1, cz + s0 * r1, 0F, 0F, 1F, 1F, 1F, white);
                }
            }
            FilmGl.draw(b, view, proj, FilmGl.PLAIN);
        }

        FilmGl.additive();
        float front = 0.75F * (1F - lin(55F, 118F, radius)) * lin(FilmScript.S_CLEAR, FilmScript.S_CLEAR + 3F, t);
        if (front > 0.002F) {
            // just inside the shell, so it shows against the land
            BufferBuilder b = FilmGl.begin();
            wall(b, cx, cy - 4F, cz, Math.max(radius - 1.5F, 0.5F), 18F, 1F, 0.94F, 0.82F, front);
            FilmGl.draw(b, view, proj, FilmGl.WALL);
        }

        // motes of light settling on the land
        if (t > 546F) {
            if (!groundKnown) {
                ground(centre);
            }
            BufferBuilder b = FilmGl.begin();
            for (int i = 0; i < SETTLERS; i++) {
                float u = (t - SETTLER[i * 6 + 3]) / SETTLER[i * 6 + 4];
                if (u <= 0F || u >= 1F) {
                    continue;
                }
                float e = u * u * (3F - 2F * u);
                float a = (float) Math.sin(Math.PI * u) * 0.85F;
                float y = lerp(SETTLER[i * 6 + 2], GROUND[i] - (float) centre.y + 0.3F, e);
                FilmGl.billboard(b, cx + SETTLER[i * 6], cy + y, cz + SETTLER[i * 6 + 1], right, up, SETTLER[i * 6 + 5], SETTLER[i * 6 + 5],
                    a, a * 0.9F, a * 0.65F, 1F);
            }
            FilmGl.draw(b, view, proj, FilmGl.DOT, 1F, 0F, 0F, 0F);
        }

        float glow = 0.35F * (1F - ss(560F, 620F, t)) + 1.2F * (t >= FilmScript.S_CLEAR ? (float) Math.exp(-(t - FilmScript.S_CLEAR) / 10F) : 0F);
        hand(cast, partial, eye, right, up, view, proj, glow, 0F, false);
        FilmGl.end();
    }

    /** The ground's height under each settling mote, once the land round the caster is there. */
    private static void ground(Vec3 centre) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.hasChunk(Mth.floor(centre.x) >> 4, Mth.floor(centre.z) >> 4)) {
            for (int i = 0; i < SETTLERS; i++) {
                GROUND[i] = (float) centre.y;
            }
            return;
        }
        for (int i = 0; i < SETTLERS; i++) {
            int x = Mth.floor(centre.x + SETTLER[i * 6]);
            int z = Mth.floor(centre.z + SETTLER[i * 6 + 1]);
            GROUND[i] = mc.level.hasChunk(x >> 4, z >> 4) ? mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) : (float) centre.y;
        }
        groundKnown = true;
    }

    // ---- shared ----------------------------------------------------------------------------------------------------------

    /** An open cylinder of light, brightest at its foot. */
    private static void wall(BufferBuilder b, float cx, float cy, float cz, float radius, float height, float r, float g, float bl, float a) {
        int segments = 96;
        for (int j = 0; j < segments; j++) {
            double a0 = Math.PI * 2.0 * j / segments;
            double a1 = Math.PI * 2.0 * (j + 1) / segments;
            float x0 = cx + (float) Math.cos(a0) * radius;
            float z0 = cz + (float) Math.sin(a0) * radius;
            float x1 = cx + (float) Math.cos(a1) * radius;
            float z1 = cz + (float) Math.sin(a1) * radius;
            FilmGl.vertex(b, x0, cy, z0, 0F, 0F, r, g, bl, a);
            FilmGl.vertex(b, x1, cy, z1, 1F, 0F, r, g, bl, a);
            FilmGl.vertex(b, x1, cy + height, z1, 1F, 1F, r, g, bl, a);
            FilmGl.vertex(b, x0, cy + height, z0, 0F, 1F, r, g, bl, a);
        }
    }

    /**
     * A glow where the caster holds the item that fired it (either item: only its light is shown). In the dark
     * ({@code dark}) TON618's bright disk is behind it, so it is wider and whiter there, with a white-hot core, it sits
     * just outside the hand (so the arm can't hide it from behind), and {@code kick} (0 to 1: the press and the release)
     * adds a flare.
     */
    private static void hand(LastLightCast cast, float partial, Vec3 eye, Vector3f right, Vector3f up, Matrix4f view, Matrix4f proj,
            float glow, float kick, boolean dark) {
        if (glow < 0.03F) {
            return;
        }
        Vec3 at = hand(cast, partial, dark);
        if (at == null) {
            return;
        }
        float x = (float) (at.x - eye.x);
        float y = (float) (at.y - eye.y);
        float z = (float) (at.z - eye.z);
        if (dark) {
            float size = Math.min(0.3F + 0.55F * glow, 1.3F);
            BufferBuilder b = FilmGl.begin();
            FilmGl.billboard(b, x, y, z, right, up, size, size, 1F, 0.76F, 0.48F, 1F);
            FilmGl.draw(b, view, proj, FilmGl.GLOW, sat(0.5F + glow) * 1.5F, 0F, 0F, 0F);
            // the core and the flare: a little nearer the camera along the line of sight (the same spot on screen), so a
            // swinging arm doesn't cut them
            float far = (float) Math.sqrt(x * x + y * y + z * z);
            float pull = far > 0.6F ? 1F - 0.15F / far : 1F;
            float px = x * pull;
            float py = y * pull;
            float pz = z * pull;
            float core = 0.12F + 0.05F * sat(glow);
            b = FilmGl.begin();
            FilmGl.billboard(b, px, py, pz, right, up, core, core, 1F, 0.97F, 0.9F, 1F);
            FilmGl.draw(b, view, proj, FilmGl.DOT, 1.2F + 0.6F * sat(glow), 0F, 0F, 0F);
            if (kick > 0.03F) {
                float reach = 0.7F + 1.8F * kick;
                b = FilmGl.begin();
                FilmGl.billboard(b, px, py, pz, right, up, reach, reach, 1F, 0.9F, 0.72F, kick);
                FilmGl.draw(b, view, proj, FilmGl.FLARE, 0.9F, 0F, 0F, 0F);
            }
            return;
        }
        float size = (0.3F + glow) * 0.5F;
        BufferBuilder b = FilmGl.begin();
        FilmGl.billboard(b, x, y, z, right, up, size, size, 1F, 0.69F, 0.38F, 1F);
        FilmGl.draw(b, view, proj, FilmGl.GLOW, sat(0.5F + glow) * 0.9F, 0F, 0F, 0F);
        b = FilmGl.begin();
        FilmGl.billboard(b, x, y, z, right, up, 0.09F, 0.09F, 1F, 0.94F, 0.82F, sat(0.4F + glow));
        FilmGl.draw(b, view, proj, FilmGl.DOT, 1F, 0F, 0F, 0F);
    }

    /**
     * About where the hand holding the item is: at the caster's side, a little forward. In the dark ({@code beside}) the
     * light sits just outside the hand instead of in it: the dark's shots look from behind the caster, and the arm
     * would hide a light in the hand itself.
     */
    @Nullable
    private static Vec3 hand(LastLightCast cast, float partial, boolean beside) {
        Player caster = cast.caster();
        if (caster == null) {
            return null;
        }
        Vec3 feet = caster.getPosition(partial);
        double yaw = Math.toRadians(Mth.rotLerp(partial, caster.yBodyRotO, caster.yBodyRot));
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        // the main hand is on the right unless the player is left-handed; the off hand is the other one
        double side = (caster.getMainArm() == HumanoidArm.RIGHT) != cast.offhand() ? 1.0 : -1.0;
        double rx = -fz * side;
        double rz = fx * side;
        if (beside) {
            return feet.add(rx * 0.55 + fx * 0.2, 0.78, rz * 0.55 + fz * 0.2);
        }
        return feet.add(rx * 0.4 + fx * 0.28, 0.82, rz * 0.4 + fz * 0.28);
    }
}
