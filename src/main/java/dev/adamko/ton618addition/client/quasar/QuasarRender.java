package dev.adamko.ton618addition.client.quasar;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarStars;
import javax.annotation.Nullable;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * <b>FABLE'S FILE</b> (since the quality redo, 2026-11-16). TA-02's world effects, drawn for everyone who can see them,
 * with the film or without it. All through our own shader ({@link QuasarGl}).
 * <ul>
 *   <li><b>After the sky</b>: our 8 named stars (a hot core, a halo, rays that shimmer; only while the player holds
 *       TON 618's item or the Stellar Remote with TA-02 picked), the star being launched until it leaves, the point of
 *       light that appears where TON618 is once it is fed, and the second sun.</li>
 *   <li><b>After the weather</b>: the landing marker (a target as wide as the crater, a pillar of light, a ring running
 *       out ever faster; before the second press, where the strike would land), the jet
 *       coming in from the horizon, <b>the landing blast</b> (flash, fireball, a shock wave and a wall of dust racing
 *       out over the ground), the jet standing on the ground, the radio lobe and its pop.</li>
 *   <li><b>Each tick</b>: the game's own particles for the blast and the pop (smoke, sparks, embers), and a shake of
 *       the view for players near it who aren't watching the film (the film's camera shakes itself).</li>
 * </ul>
 * Everything is a function of the strike's story tick. Also keeps the frame's sky view and projection for
 * {@link QuasarReticle}. With a shader pack on, all of it is drawn after the particles from looks painted before the
 * frame, the way the pack can show it ({@link QuasarAtlas}).
 */
public final class QuasarRender {
    /** Our sky things are drawn this far from the eye (inside even the shortest render distance's far plane). */
    private static final float SKY = 100f;
    /** As far as the second press reaches ({@code QuasarJets.MARK_RANGE}). */
    private static final double MARK_RANGE = 256.0;
    private static final float[] WHITE = {1f, 1f, 1f};
    private static final float[] QUASAR = {0.75f, 0.86f, 1f};
    private static final float[] DUST = {0.56f, 0.5f, 0.43f};
    /**
     * How far under its middle the lobe's skin reaches: almost a whole ball. The landing crater is about as deep as the
     * lobe is wide (since 2026-11-17), so the lobe sits in it; the land hides whatever part is under the ground.
     */
    private static final double BALL = -1.45;

    static final Matrix4f skyView = new Matrix4f();
    static final Matrix4f skyProj = new Matrix4f();
    static boolean skyKnown;
    /** Where the second press would land right now (a star is locked, the player looks at the ground), or null. */
    @Nullable
    static Vec3 aimedMark;

    /** How much of each named star is seen (0 behind land, 1 in the open), eased; and when the last frame was. */
    private static final float[] SEEN = new float[QuasarStars.ALL.size()];
    private static long starsFrame;
    private static boolean failed;
    @Nullable
    private static QuasarCast particlesFor;
    private static long particlesDone;

    static {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> particles());
        NeoForge.EVENT_BUS.addListener(ViewportEvent.ComputeCameraAngles.class, QuasarRender::shake);
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Pre.class, QuasarRender::paint);
    }

    private QuasarRender() {
    }

    /** What a drawing pass needs from the frame: the partial tick, the camera and the level's matrices. */
    record Frame(float partial, Camera camera, Matrix4f view, Matrix4f proj) {
        static Frame of(RenderLevelStageEvent event) {
            return new Frame(event.getPartialTick().getGameTimeDeltaPartialTick(true), event.getCamera(),
                new Matrix4f(event.getModelViewMatrix()), new Matrix4f(event.getProjectionMatrix()));
        }
    }

    static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (failed || level == null || level.dimension() != Level.OVERWORLD) {
            return;
        }
        try {
            // With a shader pack on (Iris), the pack draws the whole level again in its own passes and shows nothing our
            // shader draws in the level's stages (Adam, 2026-11-17: "when your shaders are on ... you can see stuff like
            // the stars in the sky without them looking out of place"): then everything is drawn after the particles,
            // from the looks painted before the frame (QuasarAtlas), as the SA Series does it.
            // The sky's things (the named stars too) are drawn with the sky, so the land and the clouds are drawn over
            // them; their names are the HUD's, over everything (Adam, 2026-11-17: "make them display under clouds but the
            // text above clouds", then "with and without shaders"). Without a pack: right after the sky, with our shader.
            // With one: from inside the game's sky drawing (skyPass), the only place a pack keeps them in its sky (drawn
            // right after it, as first tried, Complementary painted its sky over them).
            boolean pack = QuasarGl.shaderPack();
            RenderLevelStageEvent.Stage stage = event.getStage();
            if (stage == RenderLevelStageEvent.Stage.AFTER_SKY) {
                skyView.set(event.getModelViewMatrix());
                skyProj.set(event.getProjectionMatrix());
                skyKnown = true;
                if (!pack && QuasarGl.ready()) {
                    Frame frame = Frame.of(event);
                    sky(frame, level);
                    stars(frame, level);
                }
            } else if (stage == RenderLevelStageEvent.Stage.AFTER_WEATHER && !pack && QuasarGl.ready()) {
                world(Frame.of(event), mc);
            } else if (stage == RenderLevelStageEvent.Stage.AFTER_PARTICLES && pack && QuasarGl.ready() && QuasarAtlas.ready()) {
                Frame frame = Frame.of(event);
                packed(QuasarAtlas.PACK, () -> world(frame, mc));
            }
        } catch (RuntimeException broken) {
            // a strike whose light can't be drawn mustn't take the game down
            failed = true;
            QuasarGl.packing = null;
            Ton618Addition.LOG.error("The Quasar Jets world effects stopped drawing", broken);
            Tesselator.getInstance().clear();
            QuasarGl.end();
        }
    }

    /**
     * With a shader pack on, from inside the game's sky drawing ({@code LevelRendererMixin}): the sky's things, drawn
     * there so the pack keeps them in its sky (its clouds and the land are drawn over them). {@code pose}: the sky's
     * camera turn.
     */
    public static void skyPass(Matrix4f pose, float partial, Camera camera) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (failed || level == null || level.dimension() != Level.OVERWORLD || !QuasarGl.ready() || !QuasarAtlas.ready()
            || !QuasarGl.shaderPack()) {
            return;
        }
        try {
            Frame frame = new Frame(partial, camera, new Matrix4f(pose), new Matrix4f());
            QuasarAtlas.drawSky(frame.view(), () -> {
                sky(frame, level);
                stars(frame, level);
            });
        } catch (RuntimeException broken) {
            failed = true;
            QuasarGl.packing = null;
            Ton618Addition.LOG.error("The Quasar Jets sky stopped drawing (shader pack)", broken);
            Tesselator.getInstance().clear();
        }
    }

    /** With a shader pack on: what {@code draws} draws, gathered and drawn in one go from the painted looks as {@code type}. */
    private static void packed(RenderType type, Runnable draws) {
        BufferBuilder out = QuasarAtlas.packBuilder();
        QuasarGl.packing = out;
        try {
            draws.run();
        } finally {
            QuasarGl.packing = null;
        }
        MeshData mesh = out.build();
        if (mesh != null) {
            type.draw(mesh);
        }
    }

    /**
     * Before each frame, with a shader pack on: the looks painted at this frame's clock while our shader can still run
     * (the mark's ring, the lobe's size and the fireball's age come from the strike, if there is one).
     */
    private static void paint(RenderFrameEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (failed || level == null || level.dimension() != Level.OVERWORLD || !QuasarGl.ready() || !QuasarGl.shaderPack()) {
            return;
        }
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        QuasarCast cast = QuasarClient.current();
        if (QuasarAtlas.ready() && cast == null && !QuasarClient.holdingQuasarJets() && QuasarClient.secondSun(partial) <= 0.01f) {
            return;
        }
        float seconds = (level.getGameTime() % 24000L + partial) / 20f;
        float t;
        float pulse = 0f;
        float domeRadius = 4f;
        float domeWhite = 0f;
        float fireAge = 0f;
        float fireRadius = 4f;
        if (cast == null) {
            t = level.getGameTime() % 24000L + partial;
        } else {
            t = cast.tick(partial);
            pulse = markPulse(t);
            QuasarStars.Type type = cast.star().type();
            float lobe = QuasarClock.lobe(t);
            float p0 = t - QuasarClock.POP;
            if (lobe > 0f) {
                domeRadius = (float) type.lobe * lobe;
            } else if (p0 >= 0f && p0 < 7f) {
                domeRadius = (float) type.lobe * (1f + 0.35f * p0 / 7f);
                domeWhite = Math.min(1f, p0 / 3f);
            }
            float b0 = t - QuasarClock.LANDS;
            if (b0 >= 0f && b0 < 44f) {
                fireAge = b0 / 44f;
                fireRadius = (float) type.blast * (0.25f + 0.7f * easeOut(Math.min(1f, b0 / 10f)) + 0.2f * fireAge);
            }
        }
        try {
            QuasarAtlas.paint(t, seconds, pulse, domeRadius, domeWhite, fireAge, fireRadius);
        } catch (RuntimeException broken) {
            failed = true;
            Ton618Addition.LOG.error("The Quasar Jets world effects stopped drawing (shader pack)", broken);
        }
    }

    /**
     * Where the landing marker's running ring is (0-1 from its middle out, 0 for none): one as the mark is set, then one
     * every 3 seconds, coming faster and faster until one every 0.8 s as the jet lands. Counted as a phase, so the ring
     * never jumps when the beat quickens.
     */
    static float markPulse(float t) {
        if (t < 0f || t >= QuasarClock.LANDS) {
            return 0f;
        }
        if (t < 24f) {
            return Math.max(0.001f, t / 24f);
        }
        float from = QuasarClock.SWEEP - 160f;
        float span = QuasarClock.LANDS - from;
        double phase = (t - 24f) / 60.0;
        if (t > from) {
            double in = t - from;
            phase += (1.0 / 16.0 - 1.0 / 60.0) * in * in / (2.0 * span);
        }
        return Math.max(0.001f, (float) (phase - Math.floor(phase)));
    }

    // ---- the sky -------------------------------------------------------------------------------------------------------

    private static void sky(Frame frame, ClientLevel level) {
        float partial = frame.partial();
        Camera camera = frame.camera();
        Vector3f right = new Vector3f(camera.getLeftVector()).negate();
        Vector3f up = new Vector3f(camera.getUpVector());
        float night = level.getStarBrightness(partial) * (1f - level.getRainLevel(partial));
        QuasarCast cast = QuasarClient.current();
        float t = cast == null ? -1f : cast.tick(partial);
        float sun = QuasarClient.secondSun(partial);
        boolean strike = cast != null && t < QuasarClock.NIGHT + 30;
        if (sun <= 0.01f && !strike) {
            return;
        }
        Matrix4f view = new Matrix4f(frame.view());
        Matrix4f proj = new Matrix4f(frame.proj());
        float seconds = (level.getGameTime() % 24000L + partial) / 20f;
        QuasarGl.light(false);

        // the jet beyond what the world can draw: on its way in from the second sun, and standing there afterwards
        if (cast != null && t >= QuasarClock.SWEEP && t < QuasarClock.POP + 6) {
            float fade = t > QuasarClock.POP ? 1f - (t - QuasarClock.POP) / 6f : 1f;
            float head = (float) QuasarClock.beamHead(t);
            float far = (float) Math.min(QuasarClock.BEAM_FAR, Minecraft.getInstance().gameRenderer.getDepthFar() * 0.95);
            float from = Math.max(head, far * 0.55f);
            Vec3 eye = camera.getPosition();
            Vector3f mark = new Vector3f((float) (cast.mark().x - eye.x), (float) (cast.mark().y - eye.y), (float) (cast.mark().z - eye.z));
            QuasarStars.Type type = cast.star().type();
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.farBand(b, mark, toVector(cast.quasarDirection()), from, 12000f, 0.9f + 1.3f * type.size, SKY, far * 0.55f, far,
                QuasarGl.rgb(type.color), fade);
            QuasarGl.draw(b, view, proj, QuasarGl.BEAM, t, from, 0.13f, 1.0e9f, t >= QuasarClock.LANDS ? 1f : 0f);
        }

        if (cast != null) {
            Vector3f at = toVector(cast.quasarDirection()).mul(SKY);
            // TON618, fed: a new point of light where there was none, growing as the jets come on
            if (t >= QuasarClock.IGNITE && t < QuasarClock.NIGHT + 30 && sun < 0.6f) {
                float lit = (0.35f * QuasarClock.ramp(t, QuasarClock.IGNITE, QuasarClock.IGNITE + 30)
                    + 0.65f * QuasarClock.ramp(t, QuasarClock.JETS, QuasarClock.DAY)) * (1f - QuasarClock.ramp(t, QuasarClock.NIGHT, QuasarClock.NIGHT + 30));
                float half = 8f + 12f * QuasarClock.ramp(t, QuasarClock.JETS, QuasarClock.DAY);
                BufferBuilder b = QuasarGl.begin();
                QuasarGl.billboard(b, at, right, up, half, QUASAR, lit * (1f - sun));
                QuasarGl.draw(b, view, proj, QuasarGl.STAR, seconds, 6.18f, 0f, 0.35f);
            }
            // the second sun: TON618 blazing as a quasar, low in the sky
            if (sun > 0.01f) {
                float flare = 1f + 0.9f * (float) Math.exp(-Math.max(0f, t - QuasarClock.DAY) / 10f);
                BufferBuilder b = QuasarGl.begin();
                QuasarGl.billboard(b, at, right, up, 95f, new float[]{0.4f, 0.58f, 1f}, 0.14f * sun);
                QuasarGl.draw(b, view, proj, QuasarGl.GLOW, 2.2f, 0.2f, 0f, 0f);
                b = QuasarGl.begin();
                QuasarGl.billboard(b, at, right, up, 58f * flare, WHITE, Math.min(1f, sun * 1.15f));
                QuasarGl.draw(b, view, proj, QuasarGl.SUN, seconds, 0f, 0f, 0f);
            }
        }
        QuasarGl.end();
    }

    /**
     * Our 8 named stars: only while holding TON 618's item or the Stellar Remote with TA-02 picked (Adam, 2026-11-16).
     * The one being launched stays (for the film's viewers too) until it leaves the sky. They are drawn with the sky
     * (see render and skyPass), so the land and the clouds (the game's, or a shader pack's) are drawn over them, while
     * their names stay on top (the HUD's, {@link QuasarReticle}). A ray from the eye also fades each one out when land
     * stands in its way.
     */
    private static void stars(Frame frame, ClientLevel level) {
        float partial = frame.partial();
        float night = level.getStarBrightness(partial) * (1f - level.getRainLevel(partial));
        QuasarCast cast = QuasarClient.current();
        float t = cast == null ? -1f : cast.tick(partial);
        boolean holding = QuasarClient.holdingQuasarJets();
        boolean launching = cast != null && t < 92f && (cast.film() || holding);
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - starsFrame) / 1.0e9f);
        starsFrame = now;
        if (!launching && (!holding || night <= 0.01f)) {
            return;
        }
        Camera camera = frame.camera();
        Vec3 eye = camera.getPosition();
        Vector3f look = camera.getLookVector();
        Vector3f right = new Vector3f(camera.getLeftVector()).negate();
        Vector3f up = new Vector3f(camera.getUpVector());
        Matrix4f view = new Matrix4f(frame.view());
        Matrix4f proj = new Matrix4f(frame.proj());
        float seconds = (level.getGameTime() % 24000L + partial) / 20f;
        float strength = Math.min(1f, night * 2.2f);
        QuasarGl.light(false);
        for (int i = 0; i < QuasarStars.ALL.size(); i++) {
            QuasarStars.Star star = QuasarStars.ALL.get(i);
            boolean launched = cast != null && cast.starIndex() == i && t < 92f;
            float a;
            if (launched) {
                a = launching ? Math.max(strength, 0.85f) * (1f - QuasarClock.ramp(t, 72f, 90f)) : 0f;
            } else {
                a = holding && QuasarClient.starShown(i) ? strength : 0f;
            }
            Vec3 dir = star.direction();
            if (a <= 0.01f || dir.x * look.x() + dir.y * look.y() + dir.z * look.z() < 0.2) {
                continue;
            }
            boolean open = level.clip(new ClipContext(eye, eye.add(dir.scale(192.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                CollisionContext.empty())).getType() == HitResult.Type.MISS;
            SEEN[i] += ((open ? 1f : 0f) - SEEN[i]) * Math.min(1f, dt * 10f);
            a *= SEEN[i];
            if (a <= 0.01f) {
                continue;
            }
            boolean locked = i == QuasarClient.lockedStar() || launched;
            // the film zooms in on the launched star: it swells to meet the cut
            float zoom = launched ? QuasarClock.ramp(t, 56f, 80f) : 0f;
            float half = (5.5f + 2.6f * star.type().size) * (locked ? 1.15f : 1f) * (1f + 2.2f * zoom * zoom);
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.billboard(b, toVector(dir).mul(SKY), right, up, half, QuasarGl.rgb(star.type().color), a);
            QuasarGl.draw(b, view, proj, QuasarGl.STAR, seconds, i * 1.37f + 0.5f, locked && !launched ? 1f : 0f, 0.2f);
        }
        QuasarGl.end();
    }

    // ---- in the world --------------------------------------------------------------------------------------------------

    private static void world(Frame frame, Minecraft mc) {
        float partial = frame.partial();
        Camera camera = frame.camera();
        Vec3 eye = camera.getPosition();
        Matrix4f view = new Matrix4f(frame.view());
        Matrix4f proj = new Matrix4f(frame.proj());
        QuasarCast cast = QuasarClient.current();
        aimedMark = null;
        if (cast == null) {
            aim(mc, eye, partial, view, proj);
            return;
        }
        float t = cast.tick(partial);
        if (t > QuasarClock.POP + 40) {
            return;
        }
        QuasarStars.Type type = cast.star().type();
        float[] col = QuasarGl.rgb(type.color);
        float[] pale = mix(col, WHITE, 0.6f);
        Vector3f mark = new Vector3f((float) (cast.mark().x - eye.x), (float) (cast.mark().y - eye.y), (float) (cast.mark().z - eye.z));
        Vector3f right = new Vector3f(camera.getLeftVector()).negate();
        Vector3f up = new Vector3f(camera.getUpVector());
        float blast = (float) type.blast;
        float lobeRadius = (float) type.lobe;
        float b0 = t - QuasarClock.LANDS;
        QuasarGl.light(true);

        // ---- the landing marker, until the jet lands on it (Adam, 2026-11-17: "one of those 3rd person things where it
        // shows where the thing will land in a cool way just like railgun and halley and luna"): a target as wide as the
        // crater (its outer ring on the crater's edge), a pillar of light standing on its middle, and a ring running
        // out from the middle, faster and faster as the landing nears. Seen by everyone who sees the strike.
        if (t < QuasarClock.LANDS + 2) {
            float gone = 1f - QuasarClock.ramp(t, QuasarClock.LANDS - 3, QuasarClock.LANDS + 2);
            float on = QuasarClock.ramp(t, 0f, 20f);
            float near = smooth(QuasarClock.SWEEP - 160f, QuasarClock.LANDS, t);
            float pulse = markPulse(t);
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.flat(b, new Vector3f(mark).add(0f, 0.14f, 0f), blast / 0.86f, col, 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.MARK, t, (0.5f + 0.5f * near) * on * gone, pulse, 0f);
            b = QuasarGl.begin();
            QuasarGl.flat(b, new Vector3f(mark).add(0f, 0.18f, 0f), 2.6f + 0.4f * type.size, col, 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.MARK, t, 0.95f * gone, pulse, 0f);
            float pillar = on * gone * (0.55f + 0.45f * near);
            if (pillar > 0.01f) {
                float tall = 170f;
                b = QuasarGl.begin();
                QuasarGl.band(b, new Vector3f(mark).add(0f, 0.2f, 0f), new Vector3f(0f, 1f, 0f), 0f, tall, 0.45f + 0.3f * type.size, col,
                    pillar);
                QuasarGl.draw(b, view, proj, QuasarGl.BEAM, t, 0f, 0.13f, tall, 0f);
                b = QuasarGl.begin();
                QuasarGl.billboard(b, new Vector3f(mark).add(0f, 1.2f, 0f), right, up, 4f + 2f * type.size, pale, 0.55f * pillar);
                QuasarGl.draw(b, view, proj, QuasarGl.GLOW, 6f, 0.5f, 0f, 0f);
            }
        }

        // ---- the landing blast (LANDS): a flash, a shock wave and a wall of dust racing out, a fireball
        if (b0 >= 0f && b0 < 16f) {
            float u = 1f - b0 / 16f;
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.billboard(b, new Vector3f(mark).add(0f, blast * 0.3f, 0f), right, up, Math.min(blast * 4f, 900f), pale, u * u);
            QuasarGl.draw(b, view, proj, QuasarGl.GLOW, 14f, 0.6f, 0f, 0f);
        }
        if (b0 >= 0f && b0 < 32f) {
            float u = b0 / 32f;
            // (out to 3 crater radii: the crater is five times wider since 2026-11-17)
            float r = blast * (1f + 2f * easeOut(u));
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.ring(b, new Vector3f(mark).add(0f, 0.35f, 0f), r * 0.86f, r, pale, 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.SHOCK, t, (float) Math.pow(1f - u, 1.3) * 1.2f, 24f, 0f);
        }
        if (b0 >= 0f && b0 < 46f) {
            float u = b0 / 46f;
            float r = blast * (0.9f + 1.2f * easeOut(Math.min(1f, b0 / 34f)));
            BufferBuilder b = QuasarGl.begin();
            // (never taller than a tree, however big the crater: a tall one stands like a wall of fog)
            QuasarGl.wall(b, new Vector3f(mark).add(0f, -0.5f, 0f), r, Math.min(blast * (0.16f + 0.26f * u), 5f + 9f * u), mix(DUST, col, 0.15f), 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.DUST, t, 0.5f * (1f - u) * Math.min(1f, b0 / 3f), Math.max(24f, (float) Math.floor(r * 0.7f)), 0f);
        }
        // the dust hanging over the crater until the lobe blows it away
        if (b0 >= 6f && t < QuasarClock.LOBE + 12) {
            float shown = 0.22f * QuasarClock.ramp(b0, 6f, 14f) * (1f - QuasarClock.ramp(t, QuasarClock.LOBE - 4, QuasarClock.LOBE + 12));
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.wall(b, new Vector3f(mark).add(0f, -1f, 0f), blast * 1.15f, Math.min(blast * 0.9f, 14f), mix(DUST, col, 0.1f), 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.DUST, t * 0.6f, shown, Math.max(20f, (float) Math.floor(blast * 0.9f)), 0f);
        }
        if (b0 >= 0f && b0 < 44f) {
            // a lumpy ball of fire that swells, rises and is gone in two seconds: the game's own smoke and the dust are
            // what is left
            float age = b0 / 44f;
            float r = blast * (0.25f + 0.7f * easeOut(Math.min(1f, b0 / 10f)) + 0.2f * age);
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.blob(b, new Vector3f(mark).add(0f, blast * 0.55f * age, 0f), r, col, t, 0.16f + 0.14f * age);
            QuasarGl.draw(b, view, proj, QuasarGl.FIREBALL, t, age, r, 1f - smooth(0.4f, 0.85f, age));
        }

        // ---- the jet: from the horizon to its head, then standing on the mark (SWEEP..POP+6)
        if (t >= QuasarClock.SWEEP && t < QuasarClock.POP + 6) {
            float fade = t > QuasarClock.POP ? 1f - (t - QuasarClock.POP) / 6f : 1f;
            float head = (float) QuasarClock.beamHead(t);
            float far = (float) Math.min(QuasarClock.BEAM_FAR, mc.gameRenderer.getDepthFar() * 0.95);
            boolean landed = t >= QuasarClock.LANDS;
            if (head < far) {
                Vector3f dir = toVector(cast.quasarDirection());
                float width = 0.9f + 1.3f * type.size;
                // once it has landed it runs on into the crater (the land hides what is under the ground)
                float start = landed ? -blast * 1.3f : head;
                if (!landed) {
                    BufferBuilder g = QuasarGl.begin();
                    QuasarGl.billboard(g, new Vector3f(dir).mul(head).add(mark), right, up, width * 5f, pale, fade);
                    QuasarGl.draw(g, view, proj, QuasarGl.GLOW, 40f, 0.45f, 0f, 0f);
                }
                BufferBuilder b = QuasarGl.begin();
                QuasarGl.band(b, mark, dir, start, far, width, col, fade);
                QuasarGl.draw(b, view, proj, QuasarGl.BEAM, t, start, 0.13f, far, landed ? 1f : 0f);
                if (landed) {
                    // where it stands on the ground: a hot, restless glow
                    float flicker = 0.85f + 0.15f * (float) Math.sin(t * 1.7f);
                    BufferBuilder g = QuasarGl.begin();
                    QuasarGl.billboard(g, new Vector3f(mark).add(0f, 0.6f, 0f), right, up, 5f + width * 2.4f, mix(col, WHITE, 0.65f),
                        0.9f * fade * flicker);
                    QuasarGl.draw(g, view, proj, QuasarGl.GLOW, 30f, 0.5f, 0f, 0f);
                }
            }
        }

        // ---- the radio lobe (LOBE..POP), then its pop: the dome jumps and is gone, a flash, two rings run out
        float lobe = QuasarClock.lobe(t);
        if (lobe > 0f) {
            float r = lobeRadius * lobe;
            BufferBuilder g = QuasarGl.begin();
            QuasarGl.billboard(g, new Vector3f(mark).add(0f, r * 0.3f, 0f), right, up, r * 1.35f, col, 0.24f);
            QuasarGl.draw(g, view, proj, QuasarGl.GLOW, 3.5f, 0.3f, 0f, 0f);
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.dome(b, mark, r, col, BALL);
            QuasarGl.draw(b, view, proj, QuasarGl.DOME, t, 0.95f, r, 0f);
        }
        float p0 = t - QuasarClock.POP;
        if (p0 >= 0f && p0 < 7f) {
            float u = p0 / 7f;
            float r = lobeRadius * (1f + 0.35f * u);
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.dome(b, mark, r, col, BALL);
            QuasarGl.draw(b, view, proj, QuasarGl.DOME, t, 1.4f * (1f - u), r, Math.min(1f, p0 / 3f));
        }
        if (p0 >= 0f && p0 < 14f) {
            float u = 1f - p0 / 14f;
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.billboard(b, new Vector3f(mark).add(0f, lobeRadius * 0.3f, 0f), right, up, lobeRadius * 5f, pale, u * u);
            QuasarGl.draw(b, view, proj, QuasarGl.GLOW, 10f, 0.6f, 0f, 0f);
        }
        if (p0 >= 0f && p0 < 30f) {
            float u = p0 / 30f;
            float r = lobeRadius * (1f + 4.2f * easeOut(u));
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.ring(b, new Vector3f(mark).add(0f, 0.5f, 0f), r * 0.78f, r, col, 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.SHOCK, t, (1f - u) * 1.1f, 28f, 0f);
        }
        if (p0 >= 0f && p0 < 16f) {
            float u = p0 / 16f;
            float r = lobeRadius * (1f + 6f * easeOut(u));
            BufferBuilder b = QuasarGl.begin();
            QuasarGl.ring(b, new Vector3f(mark).add(0f, 0.8f, 0f), r * 0.9f, r, WHITE, 1f);
            QuasarGl.draw(b, view, proj, QuasarGl.SHOCK, t, (1f - u) * 0.8f, 36f, 0f);
        }
        QuasarGl.end();
    }

    /**
     * Before the second press: a star is locked and the player looks at the ground, so the mark is shown where the
     * strike would land (dim; {@link QuasarReticle} says how far it is, or that it is too close).
     */
    private static void aim(Minecraft mc, Vec3 eye, float partial, Matrix4f view, Matrix4f proj) {
        int locked = QuasarClient.lockedStar();
        if (locked < 0 || mc.player == null || !QuasarClient.holdingQuasarJets()) {
            return;
        }
        HitResult hit = mc.player.pick(MARK_RANGE, partial, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        // as the server's second press: down through trees and plants to the ground (QuasarJets.groundBelow)
        BlockPos.MutableBlockPos ground = block.getBlockPos().mutable();
        while (mc.level != null && ground.getY() > mc.level.getMinBuildHeight()) {
            BlockState state = mc.level.getBlockState(ground);
            if (state.isAir() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.canBeReplaced() && state.getFluidState().isEmpty()) {
                ground.move(0, -1, 0);
            } else {
                break;
            }
        }
        Vec3 at = new Vec3(ground.getX() + 0.5, ground.getY() + 1.0, ground.getZ() + 0.5);
        aimedMark = at;
        QuasarStars.Type type = QuasarStars.ALL.get(locked).type();
        boolean tooClose = at.distanceTo(mc.player.position()) < tooClose(type);
        float ticks = (mc.level == null ? 0L : mc.level.getGameTime() % 24000L) + partial;
        float[] tint = tooClose ? new float[]{1f, 0.25f, 0.2f} : QuasarGl.rgb(type.color);
        Vector3f where = new Vector3f((float) (at.x - eye.x), (float) (at.y - eye.y), (float) (at.z - eye.z));
        QuasarGl.light(true);
        // how wide the crater will be (its outer ring on the crater's edge), and the middle
        BufferBuilder b = QuasarGl.begin();
        QuasarGl.flat(b, new Vector3f(where).add(0f, 0.12f, 0f), (float) type.blast / 0.86f, tint, 1f);
        QuasarGl.draw(b, view, proj, QuasarGl.MARK, ticks, tooClose ? 0.25f : 0.3f, 0f, 0f);
        b = QuasarGl.begin();
        QuasarGl.flat(b, new Vector3f(where).add(0f, 0.16f, 0f), 2.6f + 0.4f * type.size, tint, 1f);
        QuasarGl.draw(b, view, proj, QuasarGl.MARK, ticks, tooClose ? 0.4f : 0.6f, 0f, 0f);
        QuasarGl.end();
    }

    /** Nearer than this the server refuses the second press (QuasarJets.cast: the bigger of lobe and crater, plus 6). */
    static double tooClose(QuasarStars.Type type) {
        return QuasarStars.safeDistance(type);
    }

    // ---- the game's own particles: smoke, sparks and embers for the blast, the standing jet, the lobe and its pop --------

    private static void particles() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        QuasarCast cast = QuasarClient.current();
        if (level == null || cast == null || level.dimension() != Level.OVERWORLD) {
            particlesFor = null;
            return;
        }
        if (mc.isPaused()) {
            return;
        }
        long t = cast.tick();
        if (particlesFor != cast) {
            // a strike this client has only just heard of: nothing of its past is played back
            particlesFor = cast;
            particlesDone = t - 1;
        }
        if (t <= particlesDone) {
            return;
        }
        long from = Math.max(particlesDone + 1, t - 4);
        particlesDone = t;
        if (mc.gameRenderer.getMainCamera().getPosition().distanceTo(cast.mark()) > 320.0) {
            return;
        }
        try {
            for (long k = from; k <= t; k++) {
                emit(level, cast, k);
            }
        } catch (RuntimeException broken) {
            Ton618Addition.LOG.error("The Quasar Jets particles failed", broken);
            particlesDone = Long.MAX_VALUE;
        }
    }

    private static void emit(ClientLevel level, QuasarCast cast, long k) {
        QuasarStars.Type type = cast.star().type();
        RandomSource r = level.random;
        Vec3 m = cast.mark();
        double blast = type.blast;
        double lobe = type.lobe;
        if (k == QuasarClock.LANDS) {
            add(level, ParticleTypes.FLASH, m.x, m.y + 1.5, m.z, 0, 0, 0);
            int bursts = 2 + Math.round(type.size * 2.5f);
            for (int i = 0; i < bursts; i++) {
                add(level, ParticleTypes.EXPLOSION_EMITTER, m.x + (r.nextDouble() - 0.5) * blast, m.y + r.nextDouble() * 2.0,
                    m.z + (r.nextDouble() - 0.5) * blast, 0, 0, 0);
            }
            // dust kicked out along the ground, sparks and embers thrown up
            int dust = 40 + (int) (Math.min(blast, 60.0) * 4.0);
            for (int i = 0; i < dust; i++) {
                double a = r.nextDouble() * Math.PI * 2.0;
                double speed = 0.5 + r.nextDouble() * 0.9;
                add(level, r.nextInt(3) == 0 ? ParticleTypes.CAMPFIRE_COSY_SMOKE : ParticleTypes.CLOUD, m.x + Math.cos(a) * blast * 0.5,
                    m.y + 0.3 + r.nextDouble(), m.z + Math.sin(a) * blast * 0.5, Math.cos(a) * speed, 0.03 + r.nextDouble() * 0.15,
                    Math.sin(a) * speed);
            }
            int sparks = 30 + (int) (Math.min(blast, 60.0) * 3.0);
            for (int i = 0; i < sparks; i++) {
                double a = r.nextDouble() * Math.PI * 2.0;
                double out = 0.2 + r.nextDouble() * 0.9;
                add(level, ParticleTypes.FIREWORK, m.x, m.y + 0.5, m.z, Math.cos(a) * out, 0.4 + r.nextDouble() * 1.1, Math.sin(a) * out);
            }
            for (int i = 0; i < 10 + (int) (Math.min(blast, 60.0) * 2.0); i++) {
                add(level, ParticleTypes.LAVA, m.x + (r.nextDouble() - 0.5) * blast * 1.4, m.y + 0.2, m.z + (r.nextDouble() - 0.5) * blast * 1.4,
                    0, 0, 0);
            }
            for (int i = 0; i < 30; i++) {
                add(level, ParticleTypes.LARGE_SMOKE, m.x + (r.nextDouble() - 0.5) * blast, m.y + r.nextDouble() * 2.0,
                    m.z + (r.nextDouble() - 0.5) * blast, (r.nextDouble() - 0.5) * 0.2, 0.1 + r.nextDouble() * 0.4, (r.nextDouble() - 0.5) * 0.2);
            }
        }
        // smoke rising out of the crater until the lobe swells over it
        if (k > QuasarClock.LANDS + 2 && k < QuasarClock.LOBE && k % 2 == 0) {
            for (int i = 0; i < 1 + Math.round(type.size); i++) {
                add(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, m.x + (r.nextDouble() - 0.5) * blast * 1.2, m.y - type.blastDepth * 0.3,
                    m.z + (r.nextDouble() - 0.5) * blast * 1.2, 0, 0.06 + r.nextDouble() * 0.05, 0);
            }
        }
        // the jet standing on the ground: sparks spitting from its foot
        if (k >= QuasarClock.LANDS && k < QuasarClock.POP) {
            for (int i = 0; i < 3; i++) {
                double a = r.nextDouble() * Math.PI * 2.0;
                double out = 0.25 + r.nextDouble() * 0.6;
                add(level, ParticleTypes.ELECTRIC_SPARK, m.x, m.y + 0.4, m.z, Math.cos(a) * out, 0.1 + r.nextDouble() * 0.6, Math.sin(a) * out);
            }
        }
        // the lobe's skin crackles as it swells
        float swell = QuasarClock.lobe(k);
        if (swell > 0.05f) {
            for (int i = 0; i < 2 + Math.round(type.size * 2f); i++) {
                Vec3 n = onDome(r);
                add(level, ParticleTypes.ELECTRIC_SPARK, m.x + n.x * lobe * swell, m.y + n.y * lobe * swell, m.z + n.z * lobe * swell,
                    (r.nextDouble() - 0.5) * 0.3, (r.nextDouble() - 0.5) * 0.3, (r.nextDouble() - 0.5) * 0.3);
            }
        }
        if (k == QuasarClock.POP) {
            add(level, ParticleTypes.FLASH, m.x, m.y + lobe * 0.3, m.z, 0, 0, 0);
            int count = 60 + (int) (lobe * 3.0);
            for (int i = 0; i < count; i++) {
                Vec3 n = onDome(r);
                double speed = 0.4 + r.nextDouble() * 0.7;
                add(level, i % 2 == 0 ? ParticleTypes.END_ROD : ParticleTypes.ELECTRIC_SPARK, m.x + n.x * lobe, m.y + n.y * lobe, m.z + n.z * lobe,
                    n.x * speed, n.y * speed * 0.6, n.z * speed);
            }
        }
        // glints over the bowl as the crystals grow
        if (k > QuasarClock.POP && k < QuasarClock.POP + 30) {
            for (int i = 0; i < 2; i++) {
                add(level, ParticleTypes.END_ROD, m.x + (r.nextDouble() - 0.5) * lobe * 1.2, m.y - r.nextDouble() * type.bowl,
                    m.z + (r.nextDouble() - 0.5) * lobe * 1.2, 0, 0.02 + r.nextDouble() * 0.05, 0);
            }
        }
    }

    /** A random point of the unit dome (the upper half of a ball). */
    private static Vec3 onDome(RandomSource r) {
        double y = r.nextDouble();
        double a = r.nextDouble() * Math.PI * 2.0;
        double q = Math.sqrt(1.0 - y * y);
        return new Vec3(Math.cos(a) * q, y, Math.sin(a) * q);
    }

    /** One of the game's particles, shown however far away it is. */
    private static void add(ClientLevel level, ParticleOptions particle, double x, double y, double z, double vx, double vy, double vz) {
        level.addAlwaysVisibleParticle(particle, true, x, y, z, vx, vy, vz);
    }

    // ---- the shake of the view, for players near the strike who aren't watching the film ----------------------------------

    private static void shake(ViewportEvent.ComputeCameraAngles event) {
        QuasarCast cast = QuasarClient.current();
        Minecraft mc = Minecraft.getInstance();
        if (cast == null || mc.level == null || mc.level.dimension() != Level.OVERWORLD || QuasarJetsFilm.hasCamera()) {
            return;
        }
        float t = cast.tick((float) event.getPartialTick());
        if (t < QuasarClock.LANDS || t > QuasarClock.POP + 40) {
            return;
        }
        QuasarStars.Type type = cast.star().type();
        double d = event.getCamera().getPosition().distanceTo(cast.mark());
        float b0 = t - QuasarClock.LANDS;
        float p0 = t - QuasarClock.POP;
        float amp = 3f * near(d, type.blast * 12.0) * (float) Math.exp(-b0 / 9f);
        if (t < QuasarClock.POP) {
            amp += 0.12f * near(d, type.blast * 6.0) + 0.25f * near(d, type.lobe * 3.0) * smooth(835f, 888f, t);
        }
        if (p0 >= 0f) {
            amp += 2.2f * near(d, type.lobe * 6.0) * (float) Math.exp(-p0 / 7f);
        }
        amp *= mc.options.screenEffectScale().get().floatValue();
        if (amp < 0.01f) {
            return;
        }
        event.setPitch(event.getPitch() + amp * (float) (Math.sin(t * 7.13) + Math.sin(t * 12.7 + 1.3)) * 0.5f);
        event.setYaw(event.getYaw() + amp * 0.7f * (float) (Math.sin(t * 8.31 + 2.0) + Math.sin(t * 14.9)) * 0.5f);
        event.setRoll(event.getRoll() + amp * 0.5f * (float) Math.sin(t * 9.7 + 0.7));
    }

    /** 1 at the middle, 0 from {@code reach} blocks away on. */
    private static float near(double distance, double reach) {
        return (float) Math.max(0.0, 1.0 - distance / reach);
    }

    // ---- small maths ---------------------------------------------------------------------------------------------------

    private static float easeOut(float u) {
        float v = 1f - Math.min(Math.max(u, 0f), 1f);
        return 1f - v * v * v;
    }

    private static float smooth(float from, float to, float x) {
        float u = QuasarClock.ramp(x, from, to);
        return u * u * (3f - 2f * u);
    }

    private static float[] mix(float[] a, float[] b, float u) {
        return new float[]{a[0] + (b[0] - a[0]) * u, a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u};
    }

    static Vector3f toVector(Vec3 v) {
        return new Vector3f((float) v.x, (float) v.y, (float) v.z);
    }
}
