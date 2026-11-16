package dev.adamko.ton618addition.client.quasarfilm;

import static dev.adamko.ton618addition.client.quasarfilm.JetScript.keys;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.kick;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lerp;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lin;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.sat;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.ss;

import com.mojang.blaze3d.vertex.BufferBuilder;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * <b>FABLE'S FILE.</b> The film's own space scene (the previz's shots 2 to 7, ticks 80 to 680), drawn over the whole
 * screen so nothing of the world shows through. Two set-ups, both in the scene's own units:
 * <ul>
 *   <li><b>the flight</b> (80 to 330): the star at the origin with its mirror ahead of it (+X is the way it travels) and
 *       its tail of light behind, the universe streaming past, TON618 growing from a dot far ahead;</li>
 *   <li><b>TON618</b> (330 to 680): the hole at the origin (its shadow 20.8 across its radius), the camera looking
 *       straight in; the mirror falls in, the star is pulled into a ribbon that winds into the disc, the disc ignites,
 *       the two jets leave its poles, and the whole thing tips until one jet points at us.</li>
 * </ul>
 * Nothing is simulated: every position is a function of the tick. There is no depth buffer here (it is drawn in the
 * HUD's pass), so things are drawn back to front by hand.
 */
final class JetSpace {
    private static final int TUBE_STARS = 3200;
    /** The streaming stars wrap round at this distance ahead and behind. */
    private static final float TUBE = 3000F;
    private static final int SHELL_STARS = 2600;
    private static final float SHELL = 9000F;
    private static final int FLY_GALAXIES = 9;
    private static final int MOTES = 900;
    /** The hole's quad is 260 across, its shadow 0.16 of half of that. */
    private static final float HOLE_HALF = 130F;
    private static final float SHADOW = HOLE_HALF * 0.16F;
    /** The mirror covers 62 degrees round the way ahead (the shader knows the same number). */
    private static final float MIRROR_CAP = 1.08F;
    private static final int MIRROR_RINGS = 10;
    private static final int MIRROR_SEGS = 32;
    private static final float TAN_HALF = (float) Math.tan(Math.toRadians(20.0));

    private static final float[][] FLIGHT_ANGLE = {{80, -0.2F}, {200, -0.6F}, {262, -0.4F}, {304, -1.78F}, {330, -1.84F}};
    private static final float[][] FLIGHT_DISTANCE = {{80, 7.5F}, {150, 13}, {200, 14}, {330, 17}};
    private static final float[][] HOLE_CX = {{330, -255}, {352, -232}, {398, -60}, {432, 0}};
    private static final float[][] HOLE_CY = {{330, 0}, {398, 9}, {440, 0}};
    private static final float[][] HOLE_DISTANCE = {{330, 170}, {352, 178}, {402, 330}, {440, 380}, {530, 460}, {574, 1150}, {610, 1200}, {665, 1000}};
    private static final float[][] DISC_GAIN = {{330, 0.45F}, {438, 0.7F}, {448, 3.4F}, {472, 2.5F}, {610, 2.5F}, {665, 3.4F}};
    private static final float[][] HALO_SIZE = {{440, 0}, {450, 330}, {474, 240}, {530, 260}, {610, 300}, {650, 700}, {668, 2400}};
    private static final float[][] HALO_GAIN = {{440, 0}, {450, 0.8F}, {474, 0.55F}, {610, 0.6F}, {665, 1}};

    /** The streaming stars: along the way, across it (two numbers), colour, size in pixels of a 720-line picture. */
    private static final float[] TUBE_POS = new float[TUBE_STARS * 3];
    private static final float[] TUBE_RGB = new float[TUBE_STARS * 3];
    private static final float[] TUBE_SIZE = new float[TUBE_STARS];
    /** The still sky round TON618. */
    private static final float[] SHELL_POS = new float[SHELL_STARS * 3];
    private static final float[] SHELL_RGB = new float[SHELL_STARS * 3];
    /** Each passing galaxy: x, y, z, half its size, how squashed, how turned, how bright, spiral arms or not. */
    private static final float[][] FLY = new float[FLY_GALAXIES][8];
    /** Each mote of the ribbon: how late it is, the radius it ends up circling at, its sideways spread (2), height, size. */
    private static final float[] MOTE = new float[MOTES * 6];
    private static final float[] RING_COS = new float[MIRROR_RINGS + 1];
    private static final float[] RING_SIN = new float[MIRROR_RINGS + 1];
    private static final float[] SEG_COS = new float[MIRROR_SEGS + 1];
    private static final float[] SEG_SIN = new float[MIRROR_SEGS + 1];

    static {
        Random r = new Random(618L);
        for (int i = 0; i < TUBE_STARS; i++) {
            float rho = 45F + (float) Math.pow(r.nextFloat(), 0.7) * 1700F;
            float a = r.nextFloat() * 6.2832F;
            TUBE_POS[i * 3] = (r.nextFloat() * 2F - 1F) * TUBE;
            TUBE_POS[i * 3 + 1] = (float) Math.cos(a) * rho;
            TUBE_POS[i * 3 + 2] = (float) Math.sin(a) * rho;
            float k = r.nextFloat();
            float bright = 0.45F + r.nextFloat() * 0.55F;
            TUBE_RGB[i * 3] = (k < 0.2F ? 0.72F : 1F) * bright;
            TUBE_RGB[i * 3 + 1] = (k < 0.2F ? 0.82F : k < 0.65F ? 0.96F : 0.66F) * bright;
            TUBE_RGB[i * 3 + 2] = (k < 0.2F ? 1F : k < 0.65F ? 0.86F : 0.46F) * bright;
            TUBE_SIZE[i] = 1.2F + (float) Math.pow(r.nextFloat(), 6.0) * 4F;
        }
        Random s = new Random(331L);
        for (int i = 0; i < SHELL_STARS; i++) {
            float z = s.nextFloat() * 2F - 1F;
            float a = s.nextFloat() * 6.2832F;
            float q = (float) Math.sqrt(1F - z * z);
            float bright = 0.3F + (float) Math.pow(s.nextFloat(), 3.0) * 0.7F;
            SHELL_POS[i * 3] = (float) Math.cos(a) * q * SHELL;
            SHELL_POS[i * 3 + 1] = z * SHELL * (s.nextFloat() < 0.4F ? 0.3F : 1F);
            SHELL_POS[i * 3 + 2] = (float) Math.sin(a) * q * SHELL;
            SHELL_RGB[i * 3] = bright;
            SHELL_RGB[i * 3 + 1] = bright * 0.95F;
            SHELL_RGB[i * 3 + 2] = bright * (0.8F + s.nextFloat() * 0.25F);
        }
        Random g = new Random(77L);
        for (int i = 0; i < FLY_GALAXIES; i++) {
            float a = g.nextFloat() * 6.2832F;
            float rho = 1500F + g.nextFloat() * 2400F;
            FLY[i][0] = (g.nextFloat() * 2F - 1F) * 8000F;
            FLY[i][1] = (float) Math.cos(a) * rho;
            FLY[i][2] = (float) Math.sin(a) * rho;
            FLY[i][3] = 250F + g.nextFloat() * 450F;
            FLY[i][4] = 0.35F + g.nextFloat() * 0.5F;
            FLY[i][5] = g.nextFloat() * 3F;
            FLY[i][6] = 0.35F + g.nextFloat() * 0.4F;
            FLY[i][7] = i % 2 == 0 ? 1F : 0F;
        }
        Random m = new Random(1987L);
        for (int i = 0; i < MOTES; i++) {
            MOTE[i * 6] = m.nextFloat();
            MOTE[i * 6 + 1] = 44F + (float) Math.pow(m.nextFloat(), 1.4) * 62F;
            MOTE[i * 6 + 2] = (m.nextFloat() - 0.5F) * 0.5F;
            MOTE[i * 6 + 3] = m.nextFloat() - 0.5F;
            MOTE[i * 6 + 4] = (m.nextFloat() - 0.5F) * 3F;
            MOTE[i * 6 + 5] = 0.7F + m.nextFloat() * 0.9F;
        }
        for (int i = 0; i <= MIRROR_RINGS; i++) {
            double th = i / (double) MIRROR_RINGS * MIRROR_CAP;
            RING_COS[i] = (float) Math.cos(th);
            RING_SIN[i] = (float) Math.sin(th);
        }
        for (int j = 0; j <= MIRROR_SEGS; j++) {
            double ph = j / (double) MIRROR_SEGS * Math.PI * 2.0;
            SEG_COS[j] = (float) Math.cos(ph);
            SEG_SIN[j] = (float) Math.sin(ph);
        }
    }

    private static final Matrix4f VIEW = new Matrix4f();
    private static final Matrix4f PROJ = new Matrix4f();
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static final Vector3f EYE = new Vector3f();
    private static final Vector3f FORWARD = new Vector3f();
    private static final Vector3f RIGHT = new Vector3f();
    private static final Vector3f UP = new Vector3f();
    private static final Vector3f WORLD_X = new Vector3f(1F, 0F, 0F);
    private static final Vector3f WORLD_Y = new Vector3f(0F, 1F, 0F);
    private static final Vector3f WORLD_Z = new Vector3f(0F, 0F, 1F);
    private static final float[] P2 = new float[2];
    private static final float[] P2B = new float[2];
    /** How the disc is tipped in the last frame drawn (sine and cosine of {@link JetScript#beta}). */
    private static float sinB = 0.2F;
    private static float cosB = 0.98F;
    private static boolean drawn;

    private JetSpace() {
    }

    /** Whether the scene covers the screen at tick {@code t}. */
    static boolean covers(float t) {
        return t >= JetScript.SPACE_FROM && t < JetScript.SPACE_TO;
    }

    static void forget() {
        drawn = false;
    }

    /** Where a point of the scene is on the screen (hundredths of its width and height), in the last frame drawn. */
    private static float[] project(float x, float y, float z) {
        if (!drawn) {
            return new float[] {50F, 50F};
        }
        Vector3f p = new Vector3f(x, y, z);
        new Matrix4f(PROJ).mul(VIEW).transformProject(p);
        return new float[] {(p.x * 0.5F + 0.5F) * 100F, (1F - (p.y * 0.5F + 0.5F)) * 100F};
    }

    /** Where what an educational label points at is on the screen (hundredths), for the scene's own anchors. */
    static float[] anchor(JetScript.Anchor anchor, float t, float vis) {
        switch (anchor) {
            case MIRROR:
                return project(2.3F * vis, 1.1F * vis, 0F);
            case TAIL:
                return project(-5F * vis, 0F, 0F);
            case FAR_HOLE:
                return project(JetScript.holeX(t), 0F, 0F);
            case RIBBON:
                JetScript.fall(t - 30F, 70F, 0F, P2);
                return project(P2[0], P2[1] * sinB, -P2[1] * cosB);
            case DISC:
                return project(72F, 0F, 0F);
            case NEAR_JET:
                return project(0F, cosB * 330F, sinB * 330F);
            default:
                return project(0F, 0F, 0F);
        }
    }

    /** Draws the whole scene at tick {@code t} (only while {@link #covers}). */
    static void draw(float t, QuasarStars.Type type) {
        if (!JetGl.ready()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        float aspect = mc.getWindow().getWidth() / (float) Math.max(1, mc.getWindow().getHeight());
        float[] col = JetScript.rgb(type.color);
        JetGl.beginOverlay();
        try {
            if (t < 330F) {
                flight(t, type.size, col, aspect);
            } else {
                hole(t, type, col, aspect);
            }
        } finally {
            JetGl.end();
        }
    }

    // ---- the flight: shots 2 and 3 -------------------------------------------------------------------------------------

    private static void flight(float t, float vis, float[] col, float aspect) {
        float ks = 0.6F + 0.55F * vis;
        float a = keys(FLIGHT_ANGLE, t);
        float d = keys(FLIGHT_DISTANCE, t) * ks;
        float look = ss(262F, 305F, t);
        float v = JetScript.speed(t);
        EYE.set((float) Math.sin(a) * d, lerp(0.22F, 0.34F, look) * d, (float) Math.cos(a) * d);
        camera(lerp(0.6F * vis, 40F, look), lerp(0F, 3F, look), 0F, aspect, t, 0.004F * kick(t, 150F, 10F) + 0.00012F * v);
        backdrop();
        JetGl.additive();

        // the galaxies sliding by
        float travel = JetScript.travel(t);
        for (int arms = 0; arms < 2; arms++) {
            BufferBuilder b = JetGl.begin();
            for (float[] galaxy : FLY) {
                if ((int) galaxy[7] != arms) {
                    continue;
                }
                float x = wrap(galaxy[0] - travel * 0.3F, 8000F);
                smudge(b, x, galaxy[1], galaxy[2], RIGHT, UP, galaxy[3], galaxy[4], galaxy[5], galaxy[6]);
            }
            JetGl.draw(b, VIEW, PROJ, JetGl.GALAXY, arms, 0F, 0F, 0F);
        }

        // the stars streaming past: a line behind each as long as 7 x the speed, then the stars themselves
        float streak = Math.min(v * 7F, 420F);
        Vector3f side = new Vector3f();
        if (v > 0.3F) {
            BufferBuilder b = JetGl.begin();
            for (int i = 0; i < TUBE_STARS; i++) {
                float x = wrap(TUBE_POS[i * 3] - travel, TUBE);
                float y = TUBE_POS[i * 3 + 1];
                float z = TUBE_POS[i * 3 + 2];
                float depth = (x - EYE.x) * FORWARD.x + (y - EYE.y) * FORWARD.y + (z - EYE.z) * FORWARD.z;
                if (depth < 1F && depth + streak * FORWARD.x < 1F) {
                    continue;
                }
                // across the line and across the way we look at it
                side.set(1F, 0F, 0F).cross(x - EYE.x, y - EYE.y, z - EYE.z);
                if (side.lengthSquared() < 1.0E-6F) {
                    continue;
                }
                side.normalize().mul(pixel(Math.max(depth, 1F)));
                float r = TUBE_RGB[i * 3];
                float g = TUBE_RGB[i * 3 + 1];
                float bl = TUBE_RGB[i * 3 + 2];
                JetGl.vertex(b, x - side.x, y - side.y, z - side.z, -1F, 0F, r, g, bl, 1F);
                JetGl.vertex(b, x + side.x, y + side.y, z + side.z, 1F, 0F, r, g, bl, 1F);
                JetGl.vertex(b, x + streak + side.x, y + side.y, z + side.z, 1F, 1F, r * 0.08F, g * 0.08F, bl * 0.08F, 1F);
                JetGl.vertex(b, x + streak - side.x, y - side.y, z - side.z, -1F, 1F, r * 0.08F, g * 0.08F, bl * 0.08F, 1F);
            }
            JetGl.draw(b, VIEW, PROJ, JetGl.STREAK);
        }
        BufferBuilder b = JetGl.begin();
        for (int i = 0; i < TUBE_STARS; i++) {
            float x = wrap(TUBE_POS[i * 3] - travel, TUBE);
            float y = TUBE_POS[i * 3 + 1];
            float z = TUBE_POS[i * 3 + 2];
            float depth = (x - EYE.x) * FORWARD.x + (y - EYE.y) * FORWARD.y + (z - EYE.z) * FORWARD.z;
            if (depth < 1F) {
                continue;
            }
            // never under about three pixels across: a smaller quad flickers as it moves
            float half = Math.max(TUBE_SIZE[i] * 0.5F, 1.5F) * pixel(depth);
            JetGl.billboard(b, x, y, z, RIGHT, UP, half, half, TUBE_RGB[i * 3], TUBE_RGB[i * 3 + 1], TUBE_RGB[i * 3 + 2], 1F);
        }
        JetGl.draw(b, VIEW, PROJ, JetGl.DOT, 1F, 0F, 0F, 0F);

        // TON618, far ahead: its galaxy behind it, a dim orange disc (it starves until it is fed)
        float hx = JetScript.holeX(t);
        b = JetGl.begin();
        smudge(b, hx + FORWARD.x * 900F, FORWARD.y * 900F, FORWARD.z * 900F, RIGHT, UP, 1800F, 0.417F, 0.3F, 0.5F);
        JetGl.draw(b, VIEW, PROJ, JetGl.GALAXY, 1F, 0F, 0F, 0F);
        JetGl.premultiplied();
        b = JetGl.begin();
        JetGl.billboard(b, hx, 0F, 0F, RIGHT, UP, HOLE_HALF, HOLE_HALF, 1F, 1F, 1F, 1F);
        JetGl.draw(b, VIEW, PROJ, JetGl.HOLE, t * 0.02F, 0.45F, 0.2F, 0F);

        // the star, its mirror opening ahead of it, its light pouring out behind
        starParts(t, new Vector3f(), WORLD_X, WORLD_Y, WORLD_Z, vis, 1F, 1F, col, ss(92F, 150F, t), ss(104F, 150F, t),
            ss(122F, 160F, t) * 0.5F, lerp(7F, 60F, ss(150F, 250F, t)) * ks, 1F, 0F, 0F, 0F, 2.5F * vis, 1F);
    }

    // ---- TON618: shots 4 to 7 --------------------------------------------------------------------------------------------

    private static void hole(float t, QuasarStars.Type type, float[] col, float aspect) {
        float jet = JetScript.jet(type);
        float cx = keys(HOLE_CX, t);
        float cy = keys(HOLE_CY, t);
        EYE.set(cx, cy, keys(HOLE_DISTANCE, t));
        camera(cx, cy, 0F, aspect, t, 0.012F * kick(t, 440F, 9F) + 0.016F * kick(t, 530F, 9F) * (0.5F + jet * 0.5F) + 0.006F * ss(620F, 665F, t));
        float beta = JetScript.beta(t);
        sinB = (float) Math.sin(beta);
        cosB = (float) Math.cos(beta);
        float blue = ss(440F, 462F, t);
        backdrop();
        JetGl.additive();

        // the still sky and the galaxy TON618 sits in: both fade as the disc outshines them
        float out = ss(440F, 476F, t);
        float dim = lerp(0.95F, 0.3F, out);
        float turn = t * 0.0004F;
        float cosT = (float) Math.cos(turn);
        float sinT = (float) Math.sin(turn);
        BufferBuilder b = JetGl.begin();
        for (int i = 0; i < SHELL_STARS; i++) {
            float sx = SHELL_POS[i * 3];
            float sz = SHELL_POS[i * 3 + 2];
            float x = sx * cosT + sz * sinT;
            float y = SHELL_POS[i * 3 + 1];
            float z = -sx * sinT + sz * cosT;
            float depth = (x - EYE.x) * FORWARD.x + (y - EYE.y) * FORWARD.y + (z - EYE.z) * FORWARD.z;
            if (depth < 1F) {
                continue;
            }
            float half = 1.5F * pixel(depth);
            JetGl.billboard(b, x, y, z, RIGHT, UP, half, half, SHELL_RGB[i * 3] * dim, SHELL_RGB[i * 3 + 1] * dim, SHELL_RGB[i * 3 + 2] * dim, 1F);
        }
        JetGl.draw(b, VIEW, PROJ, JetGl.DOT, 1F, 0F, 0F, 0F);
        b = JetGl.begin();
        smudge(b, 0F, 0F, -900F, WORLD_X, WORLD_Y, 1800F, 0.417F, 0.3F, lerp(0.5F, 0.1F, out));
        JetGl.draw(b, VIEW, PROJ, JetGl.GALAXY, 1F, 0F, 0F, 0F);

        // the halo of the lit quasar, behind the hole so the shadow stays black
        float halo = keys(HALO_SIZE, t);
        if (halo > 0.5F) {
            b = JetGl.begin();
            JetGl.billboard(b, 0F, 0F, 0F, WORLD_X, WORLD_Y, halo * 0.5F, halo * 0.5F, 0.74F, 0.85F, 1F, keys(HALO_GAIN, t));
            JetGl.draw(b, VIEW, PROJ, JetGl.SOFT, 1F, 0F, 0F, 0F);
        }

        // the hole and its disc: orange, then blazing blue-white from IGNITE; tipping toward us from TURN
        JetGl.premultiplied();
        b = JetGl.begin();
        JetGl.billboard(b, 0F, 0F, 0F, WORLD_X, WORLD_Y, HOLE_HALF, HOLE_HALF, 1F, 1F, 1F, 1F);
        JetGl.draw(b, VIEW, PROJ, JetGl.HOLE, t * 0.02F + Math.max(0F, t - 440F) * 0.05F, keys(DISC_GAIN, t), sinB, blue);

        // the ribbon: what is left of the star, each mote a little later than the one before, winding into the disc
        float vis = type.size * 2.2F;
        if (t >= 350F && t < 612F) {
            JetGl.additive();
            float stretch = 50F * ss(350F, 402F, t);
            float settle = lerp(1F, 0.32F, ss(440F, 472F, t));
            float shown = 0.5F * ss(350F, 366F, t) * settle * (1F - ss(560F, 610F, t));
            b = JetGl.begin();
            for (int i = 0; i < MOTES; i++) {
                float tau = t - stretch * MOTE[i * 6];
                float w = sat((tau - 350F) / 70F);
                JetScript.fall(tau, MOTE[i * 6 + 1], MOTE[i * 6 + 2], P2);
                float spread = vis * (0.9F - 0.6F * w) + 2.5F * w;
                float x = P2[0] + MOTE[i * 6 + 3] * spread * 0.5F;
                float y = P2[1] + MOTE[i * 6 + 2] * 2F * spread;
                // behind the shadow: not seen
                if (y > 0F && Math.sqrt(x * x + y * sinB * y * sinB) < SHADOW * 0.99F) {
                    continue;
                }
                float z = MOTE[i * 6 + 4] * (1F - 0.7F * w);
                float heat = w * 0.9F * 0.6F;
                float al = shown * (0.75F + 0.25F * (float) Math.sin(i * 1.7 + t * 0.2));
                float half = MOTE[i * 6 + 5] * (2.6F + vis * 1.3F) * (1F - 0.35F * w) * 0.5F;
                JetGl.billboard(b, x, y * sinB + z * cosB, -y * cosB + z * sinB, RIGHT, UP, half, half,
                    lerp(lerp(col[0], 1F, heat), 0.7F, blue), lerp(lerp(col[1], 0.95F, heat), 0.86F, blue),
                    lerp(lerp(col[2], 0.9F, heat), 1F, blue), al);
            }
            JetGl.draw(b, VIEW, PROJ, JetGl.DOT, 1F, 0F, 0F, 0F);
        }

        // the star coming in from the left along the disc's plane; its mirror peels off ahead of it and is gone
        if (t < 392F) {
            float lag = 18F * ss(350F, 400F, t);
            JetScript.fall(t - lag, 70F, 0F, P2);
            JetScript.fall(t - lag + 0.5F, 70F, 0F, P2B);
            float heading = (float) Math.atan2(P2B[1] - P2[1], P2B[0] - P2[0]);
            float ch = (float) Math.cos(heading);
            float sh = (float) Math.sin(heading);
            Vector3f at = disc(P2[0], P2[1], 0F);
            Vector3f ahead = disc(ch, sh, 0F);
            Vector3f across = disc(-sh, ch, 0F);
            Vector3f pole = disc(0F, 0F, 1F);
            float st = ss(350F, 386F, t);
            float mo = ss(334F, 356F, t);
            starParts(t, at, ahead, across, pole, vis, 1F + 3.2F * st, 1F - 0.72F * st, col, 1F, 1F - mo,
                0.5F * (1F - ss(334F, 350F, t)), 40F, 1F - ss(364F, 390F, t), 34F * mo * mo, mo * 2.2F, mo * 1.3F,
                2.5F * vis * (1F - 0.75F * mo), mo < 1F ? 1F - ss(0.75F, 1F, mo) : 0F);
        }

        // the jets: a blue-white core with knots racing outward, inside a wider, fainter sheath in the star's colour
        if (t >= 530F) {
            JetGl.additive();
            float length = lerp(1150F, 1900F, ss(610F, 665F, t));
            float head = (float) Math.pow(lin(530F, 588F, t), 0.8);
            float toUs = ss(618F, 662F, t);
            Vector3f origin = new Vector3f();
            Vector3f near = new Vector3f(0F, cosB, sinB);
            Vector3f far = new Vector3f(0F, -cosB, -sinB);
            // the far jet's first stretch is behind the shadow
            float hidden = Math.min(1F, SHADOW * 1.03F / Math.max(cosB, 0.02F) / length);
            for (int k = 0; k < 4; k++) {
                boolean away = k % 2 == 1;
                boolean sheath = k > 1;
                float wide = (sheath ? 2.3F : 1F) * jet;
                float gain = (sheath ? 0.3F : 1F) * (away ? 1.25F * (1F - 0.9F * toUs) : 1.25F * (1F + 3.5F * toUs));
                b = JetGl.begin();
                tube(b, origin, away ? far : near, 6F * wide, 60F * wide, length, 32, 8, col);
                JetGl.draw(b, VIEW, PROJ, JetGl.JET, t * 0.5F, head, away ? hidden : 0F, 46F, 0.35F, sheath ? 0F : 1F, gain, 0F);
            }
        }

        // looking straight down the jet: the flare that floods the picture (the film's white takes over at 668)
        float flood = ss(640F, 668F, t);
        if (flood > 0.001F) {
            JetGl.additive();
            float half = (40F + flood * flood * 5200F) * 0.5F;
            b = JetGl.begin();
            JetGl.billboard(b, 0F, 0F, 30F, WORLD_X, WORLD_Y, half, half, 0.91F, 0.953F, 1F, flood);
            JetGl.draw(b, VIEW, PROJ, JetGl.SOFT, 1F, 0F, 0F, 0F);
        }
    }

    /** A point of the disc's own coordinates (x right, y away from us in its plane, z along its pole) in the scene. */
    private static Vector3f disc(float x, float y, float z) {
        return new Vector3f(x, y * sinB + z * cosB, -y * cosB + z * sinB);
    }

    // ---- the star, its mirror and its tail ------------------------------------------------------------------------------

    /**
     * The star at {@code at}, travelling along {@code ahead} ({@code across} and {@code pole} complete its frame),
     * {@code vis} in radius, stretched {@code stretch} times along its way and squashed to {@code squash} across it;
     * its mirror ({@code open} 0-1 unfolded, {@code lit}, pushed {@code mirrorOut} ahead, tumbled, {@code mirrorRadius},
     * {@code mirrorAlpha}); its tail of light ({@code tailA} strong, {@code tailLen} long).
     */
    private static void starParts(float t, Vector3f at, Vector3f ahead, Vector3f across, Vector3f pole, float vis, float stretch,
            float squash, float[] col, float open, float lit, float tailA, float tailLen, float alpha, float mirrorOut,
            float mirrorRx, float mirrorRy, float mirrorRadius, float mirrorAlpha) {
        // seen from behind, the tail is between us and the star: then it is drawn last
        boolean fromBehind = (EYE.x - at.x) * ahead.x + (EYE.y - at.y) * ahead.y + (EYE.z - at.z) * ahead.z < 0F;
        if (!fromBehind) {
            tail(t, at, ahead, vis, col, tailA, tailLen);
        }

        // the mirror's frame: the star's, tumbled
        Matrix3f tumble = new Matrix3f().rotateX(mirrorRx).rotateY(mirrorRy);
        Vector3f mx = frame(tumble, 1F, 0F, 0F, ahead, across, pole);
        Vector3f my = frame(tumble, 0F, 1F, 0F, ahead, across, pole);
        Vector3f mz = frame(tumble, 0F, 0F, 1F, ahead, across, pole);
        Vector3f centre = new Vector3f(ahead).mul(mirrorOut).add(at);
        boolean mirrorShown = mirrorAlpha > 0.01F && open > 0.001F;
        if (mirrorShown) {
            mirror(false, centre, mx, my, mz, mirrorRadius, col, open, lit, mirrorAlpha);
        }

        if (alpha > 0.004F) {
            JetGl.additive();
            BufferBuilder b = JetGl.begin();
            JetGl.billboard(b, at.x, at.y, at.z, RIGHT, UP, 3.75F * vis, 3.75F * vis, col[0], col[1], col[2], 0.85F * alpha);
            JetGl.draw(b, VIEW, PROJ, JetGl.SOFT, 1F, 0F, 0F, 0F);

            // the ball itself: one quad, stretched along the way it falls once the tide has hold of it
            JetGl.premultiplied();
            b = JetGl.begin();
            float sx = ahead.dot(RIGHT);
            float sy = ahead.dot(UP);
            float flat = (float) Math.sqrt(sx * sx + sy * sy);
            if (stretch == 1F && squash == 1F || flat < 0.01F) {
                JetGl.billboard(b, at.x, at.y, at.z, RIGHT, UP, vis * squash, vis * squash, col[0], col[1], col[2], alpha);
            } else {
                Vector3f along = new Vector3f(RIGHT).mul(sx / flat).add(UP.x * sy / flat, UP.y * sy / flat, UP.z * sy / flat);
                Vector3f over = new Vector3f(RIGHT).mul(-sy / flat).add(UP.x * sx / flat, UP.y * sx / flat, UP.z * sx / flat);
                float longHalf = vis * stretch * flat;
                float deepHalf = vis * squash * (float) Math.sqrt(Math.max(0F, 1F - flat * flat));
                JetGl.billboard(b, at.x, at.y, at.z, along, over, (float) Math.sqrt(longHalf * longHalf + deepHalf * deepHalf),
                    vis * squash, col[0], col[1], col[2], alpha);
            }
            JetGl.draw(b, VIEW, PROJ, JetGl.STAR, t * 0.12F, 0F, 0F, 0F);
        }

        if (mirrorShown) {
            mirror(true, centre, mx, my, mz, mirrorRadius, col, open, lit, mirrorAlpha);
        }
        if (fromBehind) {
            tail(t, at, ahead, vis, col, tailA, tailLen);
        }
    }

    private static Vector3f frame(Matrix3f tumble, float x, float y, float z, Vector3f ahead, Vector3f across, Vector3f pole) {
        Vector3f local = tumble.transform(new Vector3f(x, y, z));
        return new Vector3f(ahead).mul(local.x).add(across.x * local.y, across.y * local.y, across.z * local.y)
            .add(pole.x * local.z, pole.y * local.z, pole.z * local.z);
    }

    /** The cone of light pouring out behind the star. */
    private static void tail(float t, Vector3f at, Vector3f ahead, float vis, float[] col, float strength, float length) {
        if (strength <= 0.001F) {
            return;
        }
        JetGl.additive();
        BufferBuilder b = JetGl.begin();
        tube(b, at, new Vector3f(ahead).negate(), 0.75F * vis, 2.3F * vis, Math.max(length, 0.01F), 24, 1, col);
        JetGl.draw(b, VIEW, PROJ, JetGl.TAIL, strength, t, 0F, 0F);
    }

    /**
     * Half of the mirror: the part of its cap that is further from us than its middle ({@code near} false, drawn before
     * the star) or nearer ({@code near} true, drawn after it). Each panel knows which of its faces we see.
     */
    private static void mirror(boolean near, Vector3f centre, Vector3f mx, Vector3f my, Vector3f mz, float radius, float[] col,
            float open, float lit, float alpha) {
        JetGl.premultiplied();
        BufferBuilder b = JetGl.begin();
        float centreDepth = (centre.x - EYE.x) * FORWARD.x + (centre.y - EYE.y) * FORWARD.y + (centre.z - EYE.z) * FORWARD.z;
        int rings = Math.min(MIRROR_RINGS, (int) Math.ceil(open * MIRROR_RINGS));
        Vector3f[] corner = {new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        Vector3f middle = new Vector3f();
        for (int i = 0; i < rings; i++) {
            for (int j = 0; j < MIRROR_SEGS; j++) {
                cap(corner[0], i, j, centre, mx, my, mz, radius);
                cap(corner[1], i + 1, j, centre, mx, my, mz, radius);
                cap(corner[2], i + 1, j + 1, centre, mx, my, mz, radius);
                cap(corner[3], i, j + 1, centre, mx, my, mz, radius);
                middle.set(corner[0]).add(corner[1]).add(corner[2]).add(corner[3]).mul(0.25F);
                float depth = (middle.x - EYE.x) * FORWARD.x + (middle.y - EYE.y) * FORWARD.y + (middle.z - EYE.z) * FORWARD.z;
                if (depth < centreDepth != near) {
                    continue;
                }
                // its outer face is toward us when the way out from the middle of the ball points at the eye
                float outward = (middle.x - centre.x) * (EYE.x - middle.x) + (middle.y - centre.y) * (EYE.y - middle.y)
                    + (middle.z - centre.z) * (EYE.z - middle.z);
                float face = outward > 0F ? 1F : 0F;
                float u0 = i / (float) MIRROR_RINGS;
                float u1 = (i + 1) / (float) MIRROR_RINGS;
                float v0 = j / (float) MIRROR_SEGS;
                float v1 = (j + 1) / (float) MIRROR_SEGS;
                JetGl.vertex(b, corner[0].x, corner[0].y, corner[0].z, u0, v0, col[0], col[1], col[2], face);
                JetGl.vertex(b, corner[1].x, corner[1].y, corner[1].z, u1, v0, col[0], col[1], col[2], face);
                JetGl.vertex(b, corner[2].x, corner[2].y, corner[2].z, u1, v1, col[0], col[1], col[2], face);
                JetGl.vertex(b, corner[3].x, corner[3].y, corner[3].z, u0, v1, col[0], col[1], col[2], face);
            }
        }
        JetGl.draw(b, VIEW, PROJ, JetGl.MIRROR, open, lit, alpha, 0F);
    }

    /** A point of the mirror's cap: ring {@code i} out from its middle, {@code j} round it. */
    private static void cap(Vector3f out, int i, int j, Vector3f centre, Vector3f mx, Vector3f my, Vector3f mz, float radius) {
        float x = RING_COS[i] * radius;
        float y = RING_SIN[i] * SEG_COS[j] * radius;
        float z = RING_SIN[i] * SEG_SIN[j] * radius;
        out.set(centre.x + mx.x * x + my.x * y + mz.x * z, centre.y + mx.y * x + my.y * y + mz.y * z,
            centre.z + mx.z * x + my.z * y + mz.z * z);
    }

    // ---- shared ----------------------------------------------------------------------------------------------------------

    /** Looks from {@link #EYE} at a point, with a small tremble of {@code amp} radians. */
    private static void camera(float tx, float ty, float tz, float aspect, float t, float amp) {
        FORWARD.set(tx, ty, tz).sub(EYE).normalize();
        RIGHT.set(FORWARD).cross(0F, 1F, 0F).normalize();
        UP.set(RIGHT).cross(FORWARD);
        if (amp > 0F) {
            float ax = amp * (float) (Math.sin(t * 7.13) + Math.sin(t * 12.7 + 1.3)) * 0.5F;
            float ay = amp * (float) (Math.sin(t * 8.31 + 2.0) + Math.sin(t * 14.9)) * 0.5F;
            FORWARD.add(UP.x * ax + RIGHT.x * ay, UP.y * ax + RIGHT.y * ay, UP.z * ax + RIGHT.z * ay).normalize();
            RIGHT.set(FORWARD).cross(0F, 1F, 0F).normalize();
            UP.set(RIGHT).cross(FORWARD);
        }
        VIEW.setLookAt(EYE.x, EYE.y, EYE.z, EYE.x + FORWARD.x, EYE.y + FORWARD.y, EYE.z + FORWARD.z, 0F, 1F, 0F);
        PROJ.setPerspective((float) Math.toRadians(40.0), aspect, 0.1F, 40000F);
        drawn = true;
    }

    /** Black over the whole screen. */
    private static void backdrop() {
        JetGl.opaque();
        BufferBuilder b = JetGl.begin();
        JetGl.vertex(b, -1F, -1F, 0F, 0F, 0F, 0F, 0F, 0F, 1F);
        JetGl.vertex(b, 1F, -1F, 0F, 1F, 0F, 0F, 0F, 0F, 1F);
        JetGl.vertex(b, 1F, 1F, 0F, 1F, 1F, 0F, 0F, 0F, 1F);
        JetGl.vertex(b, -1F, 1F, 0F, 0F, 1F, 0F, 0F, 0F, 1F);
        JetGl.draw(b, IDENTITY, IDENTITY, JetGl.PLAIN);
    }

    /** One pixel of a 720-line picture, {@code depth} away. */
    private static float pixel(float depth) {
        return 2F * depth * TAN_HALF / 720F;
    }

    /** {@code x} kept within {@code half} of zero by going round. */
    private static float wrap(float x, float half) {
        float span = half * 2F;
        float m = (x + half) % span;
        return (m < 0F ? m + span : m) - half;
    }

    /** A galaxy's smudge: a round glow, turned by {@code rot} and squashed to {@code squash}, {@code strength} bright. */
    private static void smudge(BufferBuilder b, float x, float y, float z, Vector3f right, Vector3f up, float half, float squash,
            float rot, float strength) {
        float cos = (float) Math.cos(rot);
        float sin = (float) Math.sin(rot);
        for (int corner = 0; corner < 4; corner++) {
            float cx = corner == 0 || corner == 3 ? -1F : 1F;
            float cy = corner < 2 ? -1F : 1F;
            float u = cx * cos + cy * sin;
            float v = (-cx * sin + cy * cos) / squash;
            JetGl.vertex(b, x + (right.x * cx + up.x * cy) * half, y + (right.y * cx + up.y * cy) * half,
                z + (right.z * cx + up.z * cy) * half, u, v, 1F, 1F, 1F, strength);
        }
    }

    /**
     * A tube of light from {@code base} along {@code dir}: {@code r0} wide at its foot, {@code r1} at its end,
     * {@code length} long. Each corner carries how squarely its surface faces us (1 in the middle of the tube as we see
     * it, 0 at its edges) and how far along it is, which is all the shader needs to give it a bright core.
     */
    private static void tube(BufferBuilder b, Vector3f base, Vector3f dir, float r0, float r1, float length, int segs, int steps,
            float[] col) {
        Vector3f u = Math.abs(dir.y) < 0.9F ? new Vector3f(dir).cross(0F, 1F, 0F).normalize() : new Vector3f(dir).cross(1F, 0F, 0F).normalize();
        Vector3f v = new Vector3f(dir).cross(u).normalize();
        Vector3f n = new Vector3f();
        Vector3f p = new Vector3f();
        for (int k = 0; k < steps; k++) {
            for (int j = 0; j < segs; j++) {
                for (int corner = 0; corner < 4; corner++) {
                    int jj = corner == 1 || corner == 2 ? j + 1 : j;
                    float s = (corner < 2 ? k : k + 1) / (float) steps;
                    double a = jj / (double) segs * Math.PI * 2.0;
                    float ca = (float) Math.cos(a);
                    float sa = (float) Math.sin(a);
                    n.set(u.x * ca + v.x * sa, u.y * ca + v.y * sa, u.z * ca + v.z * sa);
                    float radius = lerp(r0, r1, s);
                    p.set(base.x + dir.x * length * s + n.x * radius, base.y + dir.y * length * s + n.y * radius,
                        base.z + dir.z * length * s + n.z * radius);
                    float ex = EYE.x - p.x;
                    float ey = EYE.y - p.y;
                    float ez = EYE.z - p.z;
                    float away = (float) Math.sqrt(ex * ex + ey * ey + ez * ez);
                    float facing = away < 1.0E-4F ? 1F : Math.abs(n.x * ex + n.y * ey + n.z * ez) / away;
                    JetGl.vertex(b, p.x, p.y, p.z, facing, s, col[0], col[1], col[2], 1F);
                }
            }
        }
    }
}
