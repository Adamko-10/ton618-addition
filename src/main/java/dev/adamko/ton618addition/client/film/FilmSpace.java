package dev.adamko.ton618addition.client.film;

import static dev.adamko.ton618addition.client.film.FilmScript.lerp;
import static dev.adamko.ton618addition.client.film.FilmScript.lin;
import static dev.adamko.ton618addition.client.film.FilmScript.ss;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * <b>FABLE'S FILE.</b> The film's own space scene (the previz's shots 4 to 8, game ticks 200 to 684): TON618 and its
 * disk among stars and galaxies, the sky wheeling in streaks, the stars going out, the Hawking pairs, the shrinking,
 * the point of light and the burst's ring. A scene of its own, in its own units (the hole's shadow is 3.2 across its
 * radius), drawn over the whole screen so that nothing of the dark shows through: TON 618's own sky hole and its time
 * effects are covered while it plays. Everything is a function of the storyboard time, so nothing is simulated.
 */
final class FilmSpace {
    private static final int STARS = 3200;
    private static final int CINDERS = 60;
    private static final float SKY_RADIUS = 4000F;
    /** The hole's quad is 40 across, its shadow 0.16 of half of that. */
    private static final float HOLE_HALF = 20F;
    private static final float SHADOW = 3.2F;
    private static final float STAR_GAIN = 3.2F;
    private static final float PAIR_GAIN = 3F;
    private static final float[][] DISTANCE = {{150, 46}, {165, 104}, {185, 138}, {220, 146}, {300, 104}};
    private static final float[][] ERA = {{150, 0}, {220, 0.3F}, {256, 1F}};
    private static final float[][] FLARE_SIZE = {{385, 0}, {390, 2}, {470, 7}, {486, 9}, {494, 1.2F}, {497, 0.9F}, {500, 260}, {504, 600}};
    private static final float[][] FLARE_GAIN = {{385, 0}, {392, 0.22F}, {470, 0.8F}, {486, 1}, {494, 0.9F}, {497, 1}};

    /** Each star: where (on the sky's sphere), its colour, when it dies (0-1 of the star era; 2: a cinder), its size. */
    private static final float[] STAR_POS = new float[STARS * 3];
    private static final float[] STAR_RGB = new float[STARS * 3];
    private static final float[] STAR_DEATH = new float[STARS];
    private static final float[] STAR_SIZE = new float[STARS];
    private static final int GALAXIES = 9;
    /** Each galaxy: where, how big, how bright, how it is turned and squashed. */
    private static final float[][] GALAXY = new float[GALAXIES][7];

    static {
        Random r = new Random(618L);
        for (int i = 0; i < STARS; i++) {
            float z = r.nextFloat() * 2F - 1F;
            float a = r.nextFloat() * 6.2832F;
            float s = (float) Math.sqrt(1F - z * z);
            // a soft band, so the sky has a shape
            float squash = r.nextFloat() < 0.45F ? 0.25F : 1F;
            STAR_POS[i * 3] = (float) Math.cos(a) * s * SKY_RADIUS;
            STAR_POS[i * 3 + 1] = z * SKY_RADIUS * squash;
            STAR_POS[i * 3 + 2] = (float) Math.sin(a) * s * SKY_RADIUS;
            float k = r.nextFloat();
            float[] c;
            float death;
            if (i < CINDERS) {
                // the cinders of the degenerate era
                c = new float[] {1F, 0.5F, 0.32F};
                death = 2F;
            } else if (k < 0.2F) {
                // blue: the first to die
                c = new float[] {0.72F, 0.82F, 1F};
                death = 0.06F + r.nextFloat() * 0.34F;
            } else if (k < 0.6F) {
                c = new float[] {1F, 0.96F, 0.86F};
                death = 0.25F + r.nextFloat() * 0.4F;
            } else {
                // red dwarfs: the last
                c = new float[] {1F, 0.62F, 0.42F};
                death = 0.5F + r.nextFloat() * 0.5F;
            }
            STAR_RGB[i * 3] = c[0];
            STAR_RGB[i * 3 + 1] = c[1];
            STAR_RGB[i * 3 + 2] = c[2];
            STAR_DEATH[i] = death;
            STAR_SIZE[i] = (i < CINDERS ? 2.2F : 1.2F) + (float) Math.pow(r.nextFloat(), 6.0) * 4.5F;
        }
        Random g = new Random(77L);
        for (int i = 0; i < GALAXIES; i++) {
            float z = g.nextFloat() * 1.6F - 0.8F;
            float a = g.nextFloat() * 6.2832F;
            float q = (float) Math.sqrt(1F - z * z);
            GALAXY[i][0] = (float) Math.cos(a) * q * 3600F;
            GALAXY[i][1] = z * 3600F;
            GALAXY[i][2] = (float) Math.sin(a) * q * 3600F;
            GALAXY[i][3] = 130F + g.nextFloat() * 260F;
            GALAXY[i][4] = 0.35F + g.nextFloat() * 0.4F;
            GALAXY[i][5] = g.nextFloat() * 3.14F;
            GALAXY[i][6] = 0.3F + g.nextFloat() * 0.5F;
        }
    }

    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJ = new Matrix4f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector3f EYE = new Vector3f();
    private static final Vector3f FORWARD = new Vector3f();
    private static final Vector3f RIGHT = new Vector3f();
    private static final Vector3f UP = new Vector3f();
    /** The hole's shadow radius in the last frame drawn (for the labels' pointers). */
    private static float shadowNow = SHADOW;
    private static boolean drawn;

    private FilmSpace() {
    }

    /** Whether the scene covers the screen at storyboard time {@code t}. */
    static boolean covers(float t) {
        return t >= FilmScript.S_COSMOS && t < FilmScript.S_WORLD;
    }

    /** Where on the screen (hundredths of its width and height) the hole's edge is, {@code reach} shadow radii to the right. */
    static float[] anchor(float reach) {
        if (!drawn) {
            return new float[] {50F, 50F};
        }
        Vector3f p = new Vector3f(RIGHT).mul(reach * shadowNow);
        new Matrix4f(PROJ).mul(VIEW).transformProject(p);
        return new float[] {(p.x * 0.5F + 0.5F) * 100F, (1F - (p.y * 0.5F + 0.5F)) * 100F};
    }

    static void forget() {
        drawn = false;
    }

    /** Draws the whole scene at storyboard time {@code t} (only while {@link #covers}). */
    static void draw(float t) {
        if (!FilmGl.ready()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float aspect = mc.getWindow().getWidth() / (float) Math.max(1, mc.getWindow().getHeight());
        float tt = Math.min(t, FilmScript.S_BURST);

        // ---- the camera
        float az = 0.25F + (tt - 150F) * 0.0032F;
        float el = 0.2F;
        float dist;
        float fov;
        boolean limb = false;
        if (tt < 300F) {
            dist = FilmScript.keys(DISTANCE, tt);
            fov = 40F;
        } else if (tt < 390F) {
            // close on the edge of the shadow
            dist = lerp(16F, 12.5F, lin(300F, 390F, tt));
            fov = 30F;
            limb = true;
        } else if (tt < 470F) {
            dist = lerp(34F, 14F, ss(390F, 470F, tt));
            fov = 34F;
        } else {
            dist = lerp(14F, 8F, lin(470F, 500F, tt));
            fov = 34F;
        }
        EYE.set((float) (Math.sin(az) * Math.cos(el)), (float) Math.sin(el), (float) (Math.cos(az) * Math.cos(el))).mul(dist);
        Vector3f target = new Vector3f();
        if (limb) {
            Vector3f side = new Vector3f(EYE).negate().normalize().cross(0F, 1F, 0F).normalize();
            target.add(side.mul(lerp(3.25F, 2.85F, lin(300F, 390F, tt)))).add(0F, 0.55F, 0F);
        }
        FORWARD.set(target).sub(EYE).normalize();
        RIGHT.set(FORWARD).cross(0F, 1F, 0F).normalize();
        UP.set(RIGHT).cross(FORWARD);
        float sh = tt < FilmScript.S_HUSH ? lin(400F, 488F, tt) : 0F;
        float amp = 0.011F * sh * sh;
        if (amp > 0F) {
            float ax = amp * (float) (Math.sin(t * 7.13) + Math.sin(t * 12.7 + 1.3)) * 0.5F;
            float ay = amp * (float) (Math.sin(t * 8.31 + 2.0) + Math.sin(t * 14.9)) * 0.5F;
            FORWARD.add(UP.x * ax + RIGHT.x * ay, UP.y * ax + RIGHT.y * ay, UP.z * ax + RIGHT.z * ay).normalize();
            RIGHT.set(FORWARD).cross(0F, 1F, 0F).normalize();
            UP.set(RIGHT).cross(FORWARD);
        }
        VIEW.setLookAt(EYE.x, EYE.y, EYE.z, EYE.x + FORWARD.x, EYE.y + FORWARD.y, EYE.z + FORWARD.z, 0F, 1F, 0F);
        // Minecraft 26.3: reversed depth (far and near swapped), in the device's own clip-space range
        PROJ.setPerspective((float) Math.toRadians(fov), aspect, 20000F, 0.5F,
            com.mojang.blaze3d.systems.RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
        float hs = FilmScript.holeScale(tt);
        shadowNow = SHADOW * hs;
        drawn = true;

        FilmGl.beginOverlay();

        // ---- the backdrop: black, with a faint warmth that leaves as space cools
        float warm = 1F - lin(150F, 205F, tt);
        FilmGl.opaque();
        BufferBuilder b = FilmGl.begin();
        FilmGl.vertex(b, -1F, -1F, 0F, 0F, 0F, 0.034F * warm, 0.017F * warm, 0.03F * warm, 1F);
        FilmGl.vertex(b, 1F, -1F, 0F, 1F, 0F, 0.034F * warm, 0.017F * warm, 0.03F * warm, 1F);
        FilmGl.vertex(b, 1F, 1F, 0F, 1F, 1F, 0.034F * warm, 0.017F * warm, 0.03F * warm, 1F);
        FilmGl.vertex(b, -1F, 1F, 0F, 0F, 1F, 0.034F * warm, 0.017F * warm, 0.03F * warm, 1F);
        FilmGl.draw(b, IDENTITY, IDENTITY, FilmGl.PLAIN);

        FilmGl.additive();
        float era = FilmScript.keys(ERA, tt);
        if (tt < 302F) {
            sky(tt, era, fov);
        }

        // ---- the hole
        FilmGl.premultiplied();
        b = FilmGl.begin();
        FilmGl.billboard(b, 0F, 0F, 0F, RIGHT, UP, HOLE_HALF * hs, HOLE_HALF * hs, 1F, 1F, 1F, 1F);
        float disk = (1F - ss(236F, 292F, tt)) * (tt < 236F ? 1F : 0.82F + 0.18F * (float) Math.sin(tt * 2.1));
        FilmGl.draw(b, VIEW, PROJ, FilmGl.HOLE, FilmScript.phase(tt), disk, FilmScript.heat(tt), FilmScript.glow(tt), 0.2F, 0F, 0F, 0F);

        FilmGl.additive();
        float[] hot = FilmScript.heatRgb(FilmScript.heat(tt));
        if (tt >= 300F && tt < 490F) {
            pairs(tt, hs, hot, limb);
        }

        // ---- the glow that becomes the point of light, then the burst
        float flareSize = FilmScript.keys(FLARE_SIZE, t);
        if (flareSize > 0.001F) {
            b = FilmGl.begin();
            FilmGl.billboard(b, 0F, 0F, 0F, RIGHT, UP, flareSize * 0.5F, flareSize * 0.5F, hot[0], hot[1], hot[2], 1F);
            FilmGl.draw(b, VIEW, PROJ, FilmGl.GLOW, FilmScript.keys(FLARE_GAIN, t), 0F, 0F, 0F);
        }
        float burst = lin(496F, 503.5F, t);
        if (burst > 0F && burst < 1F) {
            float ring = 0.2F + 12F * (float) Math.pow(burst, 1.3);
            b = FilmGl.begin();
            FilmGl.billboard(b, 0F, 0F, 0F, RIGHT, UP, ring, ring, 0.84F, 0.91F, 1F, (1F - burst) * 0.95F);
            FilmGl.draw(b, VIEW, PROJ, FilmGl.RING, 0.9F, 0F, 0F, 0F);
        }
        FilmGl.end();
    }

    /** The galaxies, the stars' trails and the stars: wheeling round, flaring and going out. */
    private static void sky(float tt, float era, float fov) {
        float cinder = ss(240F, 262F, tt) * (1F - ss(266F, 300F, tt)) * 0.7F;
        float rot = FilmScript.skyRot(tt);
        float trail = (float) FilmScript.skyTurn(tt) * 5F;
        Matrix3f turn = new Matrix3f().rotateZ(0.42F).rotateY(rot);
        Matrix3f behind = new Matrix3f().rotateZ(0.42F).rotateY(rot - trail);
        float redden = ss(0F, 1F, era) * 0.45F;
        // one pixel of a 720-line picture, out at the sky's sphere
        float pixel = 2F * SKY_RADIUS * (float) Math.tan(Math.toRadians(fov) * 0.5) / 720F;
        Vector3f p = new Vector3f();
        Vector3f q = new Vector3f();
        Vector3f side = new Vector3f();

        float fade = 1F - ss(195F, 250F, tt);
        if (fade > 0.001F) {
            BufferBuilder b = FilmGl.begin();
            for (float[] galaxy : GALAXY) {
                turn.transform(galaxy[0], galaxy[1], galaxy[2], p);
                float half = galaxy[3];
                float cos = (float) Math.cos(galaxy[5]);
                float sin = (float) Math.sin(galaxy[5]);
                float a = galaxy[4] * fade;
                for (int corner = 0; corner < 4; corner++) {
                    float cx = corner == 0 || corner == 3 ? -1F : 1F;
                    float cy = corner < 2 ? -1F : 1F;
                    // the smudge is a round glow, turned and squashed
                    float u = cx * cos + cy * sin;
                    float v = (-cx * sin + cy * cos) / galaxy[6];
                    FilmGl.vertex(b, p.x + (RIGHT.x * cx + UP.x * cy) * half, p.y + (RIGHT.y * cx + UP.y * cy) * half,
                        p.z + (RIGHT.z * cx + UP.z * cy) * half, u, v, 1F, 1F, 1F, a);
                }
            }
            FilmGl.draw(b, VIEW, PROJ, FilmGl.GALAXY);
        }

        boolean any = false;
        BufferBuilder b = FilmGl.begin();
        if (trail > 0.002F) {
            float halfWidth = pixel * 1.2F;
            for (int i = 0; i < STARS; i++) {
                float alive = STAR_DEATH[i] > 1.5F ? cinder : 1F - ss(STAR_DEATH[i] - 0.02F, STAR_DEATH[i], era);
                if (alive < 0.01F) {
                    continue;
                }
                turn.transform(STAR_POS[i * 3], STAR_POS[i * 3 + 1], STAR_POS[i * 3 + 2], p);
                behind.transform(STAR_POS[i * 3], STAR_POS[i * 3 + 1], STAR_POS[i * 3 + 2], q);
                side.set(q).sub(p).cross(p.x - EYE.x, p.y - EYE.y, p.z - EYE.z);
                if (side.lengthSquared() < 1.0E-6F) {
                    continue;
                }
                side.normalize().mul(halfWidth);
                float k = alive * 0.6F;
                float r = lerp(STAR_RGB[i * 3], 1F, redden) * k;
                float g = lerp(STAR_RGB[i * 3 + 1], 0.45F, redden) * k;
                float bl = lerp(STAR_RGB[i * 3 + 2], 0.25F, redden) * k;
                FilmGl.vertex(b, p.x - side.x, p.y - side.y, p.z - side.z, -1F, 0F, r, g, bl, 1F);
                FilmGl.vertex(b, p.x + side.x, p.y + side.y, p.z + side.z, 1F, 0F, r, g, bl, 1F);
                FilmGl.vertex(b, q.x + side.x, q.y + side.y, q.z + side.z, 1F, 1F, 0F, 0F, 0F, 1F);
                FilmGl.vertex(b, q.x - side.x, q.y - side.y, q.z - side.z, -1F, 1F, 0F, 0F, 0F, 1F);
                any = true;
            }
        }
        if (any) {
            FilmGl.draw(b, VIEW, PROJ, FilmGl.STREAK);
            b = FilmGl.begin();
        }

        any = false;
        for (int i = 0; i < STARS; i++) {
            float alive;
            float flare;
            if (STAR_DEATH[i] > 1.5F) {
                alive = cinder;
                flare = 0F;
            } else {
                alive = 1F - ss(STAR_DEATH[i] - 0.02F, STAR_DEATH[i], era);
                // a last flare as it goes
                float x = (era - STAR_DEATH[i] + 0.02F) / 0.014F;
                flare = (float) Math.exp(-x * x);
            }
            float bright = alive + 2.2F * flare;
            if (bright < 0.004F) {
                continue;
            }
            turn.transform(STAR_POS[i * 3], STAR_POS[i * 3 + 1], STAR_POS[i * 3 + 2], p);
            // drawn a little larger than the previz's points, and never under about three pixels across: a quad smaller
            // than that flickers as the sky turns
            float half = Math.max(STAR_SIZE[i] * (0.7F + 0.3F * alive + 0.9F * flare) * pixel * 0.9F, pixel * 1.5F);
            float k = bright / STAR_GAIN;
            FilmGl.billboard(b, p.x, p.y, p.z, RIGHT, UP, half, half, lerp(STAR_RGB[i * 3], 1F, redden) * k,
                lerp(STAR_RGB[i * 3 + 1], 0.45F, redden) * k, lerp(STAR_RGB[i * 3 + 2], 0.25F, redden) * k, 1F);
            any = true;
        }
        if (any) {
            FilmGl.draw(b, VIEW, PROJ, FilmGl.DOT, STAR_GAIN, 0F, 0F, 0F);
        } else {
            // nothing was added: let the builder go
            FilmGl.draw(b, VIEW, PROJ, FilmGl.DOT);
        }
    }

    /**
     * The Hawking pairs, in the hole's own plane (facing the camera): each pops into being at the horizon, its blue
     * partner falls in and fades, its gold one escapes.
     */
    private static void pairs(float tt, float hs, float[] hot, boolean limb) {
        float rs = SHADOW * hs;
        float half = 0.12F * Math.max(hs, 0.3F) * (limb ? 0.8F : 1.25F);
        BufferBuilder b = FilmGl.begin();
        boolean any = false;
        for (int i = 0; i < FilmScript.PAIRS; i++) {
            float u = (tt - FilmScript.PAIR_BORN[i]) / FilmScript.PAIR_LIFE[i];
            if (u < 0F || u > 1F) {
                continue;
            }
            float th = FilmScript.PAIR_ANGLE[i];
            float pop = 1F + 2F * (1F - lin(0F, 0.12F, u));
            float in = Math.min(u / 0.06F, 1F);
            // the one that falls in
            float ra = (1.10F - 0.24F * u * u) * rs;
            float ta = th + 0.05F + 0.35F * u;
            float aa = in * (1F - ss(0.35F, 0.8F, u)) * pop * 0.9F / PAIR_GAIN;
            // the one that escapes
            float rb = (1.10F + 0.25F * u + 3.0F * u * u) * rs;
            float tb = th - 0.05F - 0.12F * u;
            float ab = in * (1F - ss(0.6F, 1F, u)) * pop / PAIR_GAIN;
            if (aa > 0.003F) {
                float x = (float) Math.cos(ta) * ra;
                float y = (float) Math.sin(ta) * ra;
                FilmGl.billboard(b, RIGHT.x * x + UP.x * y, RIGHT.y * x + UP.y * y, RIGHT.z * x + UP.z * y, RIGHT, UP, half * 0.85F,
                    half * 0.85F, 0.4F * aa, 0.72F * aa, aa, 1F);
                any = true;
            }
            if (ab > 0.003F) {
                float x = (float) Math.cos(tb) * rb;
                float y = (float) Math.sin(tb) * rb;
                FilmGl.billboard(b, RIGHT.x * x + UP.x * y, RIGHT.y * x + UP.y * y, RIGHT.z * x + UP.z * y, RIGHT, UP, half, half,
                    lerp(1F, hot[0], 0.5F) * ab, lerp(0.8F, hot[1], 0.5F) * ab, lerp(0.45F, hot[2], 0.5F) * ab, 1F);
                any = true;
            }
        }
        if (any) {
            FilmGl.draw(b, VIEW, PROJ, FilmGl.DOT, PAIR_GAIN, 0F, 0F, 0F);
        } else {
            FilmGl.draw(b, VIEW, PROJ, FilmGl.DOT);
        }
    }
}
