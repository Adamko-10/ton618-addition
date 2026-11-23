package dev.adamko.ton618addition.client.quasar;

import cyou.rimuru.rimulib.client.api.NetClientApi;
import cyou.rimuru.rimulib.client.magic.ClientSkills;
import cyou.rimuru.rimulib.skill.SkillHolder;
import cyou.rimuru.rimulib.client.magic.SkillTuning;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.net.QuasarFx;
import dev.adamko.ton618addition.net.QuasarLock;
import dev.adamko.ton618addition.net.QuasarMissing;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarInfo;
import dev.adamko.ton618addition.quasar.QuasarSkill;
import dev.adamko.ton618addition.quasar.QuasarSky;
import dev.adamko.ton618addition.quasar.QuasarStars;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

/**
 * TA-02 · Quasar Jets on the client: its packets, the running strike, the star this player has locked, the stars
 * missing from the sky, and the second sun's strength (read by {@code client/mixin/ClientLevelMixin}). Hands the strike
 * to the film ({@link QuasarJetsFilm}, Fable's) for players who get it; the world effects are {@link QuasarRender}'s.
 */
public final class QuasarClient {
    @Nullable
    private static QuasarCast current;
    private static int lockedStar = -1;
    private static long[] backOnDay = new long[QuasarStars.ALL.size()];

    private QuasarClient() {
    }

    public static void init() {
        QuasarJetsFilm.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> reset());
        // the level's stages on Minecraft 1.21.11 (the frame's matrices from client/mixin/LevelRendererStagesMixin): right
        // after the sky = the start of the main pass (the land and the clouds are drawn over), the plain world effects at
        // its end, and with a shader pack everything as glowing-eyes geometry with the entities
        WorldRenderEvents.START_MAIN.register(context -> QuasarRender.stage(QuasarRender.Stage.AFTER_SKY));
        WorldRenderEvents.END_MAIN.register(context -> QuasarRender.stage(QuasarRender.Stage.AFTER_WEATHER));
        WorldRenderEvents.AFTER_ENTITIES.register(QuasarRender::stage);
        // the aim reticle and the readout: client/HudRescue calls hud(...) right after the HUD; the fog's colour:
        // client/mixin/FogRendererMixin calls fogColor(...)
    }

    /**
     * Right after the game's HUD, from {@code client/HudRescue}: the aim reticle, then the readout for whoever watches
     * a strike without its film. Not when TON 618 called the HUD off (as on NeoForge, where they hang on
     * RenderGuiEvent.Post).
     */
    public static void hud(net.minecraft.client.gui.GuiGraphics graphics, float partial) {
        QuasarReticle.draw(graphics, partial);
        QuasarHud.draw(graphics, partial);
    }

    /** After every mod is constructed (the packets are declared by then). */
    public static void afterConstruction() {
        NetClientApi.receive(QuasarFx.TYPE, (fx, context) -> Minecraft.getInstance().execute(() -> begin(fx)));
        NetClientApi.receive(QuasarLock.TYPE, (lock, context) -> Minecraft.getInstance().execute(() -> lockedStar = lock.star()));
        NetClientApi.receive(QuasarMissing.TYPE, (missing, context) -> Minecraft.getInstance().execute(() -> {
            long[] days = new long[QuasarStars.ALL.size()];
            System.arraycopy(missing.backOnDay(), 0, days, 0, Math.min(days.length, missing.backOnDay().length));
            backOnDay = days;
        }));
        QuasarInfo.selectWarning = () -> {
            if (!Ton618Link.ss21Installed()) {
                return "Locked: needs the SS-21 Quasar addon (The Shooting Star: SS-21 Quasar)";
            }
            // none while there is no world (unlocked only), so RimuLib makes its key (, and .) at start, as Last Light's
            Minecraft mc = Minecraft.getInstance();
            return mc.level == null || mc.level.dimension() == Level.OVERWORLD && mc.level.isDarkOutside() ? null
                : "Night only, in the overworld: cast at a named star, then at the ground";
        };
        // the menu's Power line: the locked star's crater width at 1x (else a Sun-like star's)
        QuasarInfo.craterAcross = () -> {
            int locked = lockedStar;
            QuasarStars.Type type = locked >= 0 && locked < QuasarStars.ALL.size() ? QuasarStars.ALL.get(locked).type()
                : QuasarStars.Type.SUN_LIKE;
            return (float) (type.blast * 2.0);
        };
    }

    @Nullable
    public static QuasarCast current() {
        return current;
    }

    /** The star this player has locked (press 1), or -1. */
    public static int lockedStar() {
        return lockedStar;
    }

    /**
     * This player holds TON 618's item or the Stellar Remote with TA-02 picked: only then are the named stars and the
     * reticle shown (Adam, 2026-11-16). RimuLib internals: if they change, false.
     */
    public static boolean holdingQuasarJets() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        // (locked without SS-21 Quasar: no stars)
        if (!Ton618Link.ss21Installed()) {
            return false;
        }
        try {
            SkillHolder holder = SkillHolder.held(mc.player);
            return holder != null && holder.skillSet() != null && ClientSkills.selected(holder.skillSet()) instanceof QuasarSkill;
        } catch (RuntimeException | LinkageError unsure) {
            return false;
        }
    }

    /** The Power slider of the TA-02 the player holds (1 when unsure): sizes the aim's crater preview and safe distance. */
    public static float aimPower() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return 1F;
        }
        try {
            SkillHolder holder = SkillHolder.held(mc.player);
            if (holder != null && holder.skillSet() != null && ClientSkills.selected(holder.skillSet()) instanceof QuasarSkill skill) {
                return Math.max(0.1F, Math.min(3F, SkillTuning.power(skill)));
            }
        } catch (RuntimeException | LinkageError unsure) {
            // the menu's internals moved: the preview at the star's own size
        }
        return 1F;
    }

    /** The named star is in the sky today (not launched lately). */
    public static boolean starShown(int star) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        return QuasarSky.day(mc.level.getDayTime()) >= backOnDay[star];
    }

    /**
     * The overworld day ({@link QuasarSky#day}) from which the named star is back in the sky; 0 when it never left.
     * For the film's "back in N nights" (Fable's proposal P4): {@code backOnDay(star) - QuasarSky.day(level.getDayTime())}.
     */
    public static long backOnDay(int star) {
        return star >= 0 && star < backOnDay.length ? backOnDay[star] : 0L;
    }

    /** The second sun, 0 (none) to 1 (full day), for this client right now (in the overworld only). */
    public static float secondSun(float partial) {
        QuasarCast cast = current;
        Minecraft mc = Minecraft.getInstance();
        if (cast == null || !cast.secondSun() || mc.level == null || mc.level.dimension() != Level.OVERWORLD) {
            return 0f;
        }
        return QuasarClock.day(cast.tick(partial)) * cast.star().type().day;
    }

    private static void begin(QuasarFx fx) {
        if (current != null && current.fx().castGameTime() == fx.castGameTime()) {
            return;
        }
        current = new QuasarCast(fx);
        Ton618Addition.LOG.info("{} reached this game at tick {} ({}, {}).", QuasarInfo.TITLE, current.tick(),
            current.star().name(), fx.film() ? "with its film" : "world effects only");
        if (fx.film()) {
            try {
                QuasarJetsFilm.start(current);
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("The Quasar Jets film failed to start", failed);
            }
        }
    }

    private static void tick() {
        if (current == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        current.clientTick();
        if (current.tick() > QuasarClock.END + 40) {
            current = null;
            if (QuasarJetsFilm.active()) {
                QuasarJetsFilm.stop();
            }
        }
    }

    private static void reset() {
        current = null;
        lockedStar = -1;
        backOnDay = new long[QuasarStars.ALL.size()];
        try {
            QuasarJetsFilm.stop();
        } catch (RuntimeException failed) {
            Ton618Addition.LOG.error("The Quasar Jets film failed to stop", failed);
        }
    }

    /** The second sun lifts the night's fog toward a cold day blue. */
    /** The fog's colour under the second sun, or null to leave it (from client/mixin/FogRendererMixin). */
    @Nullable
    public static float[] fogColor(float partial, float red, float green, float blue) {
        float d = secondSun(partial);
        if (d <= 0f) {
            return null;
        }
        return new float[] {Mth.lerp(d, red, 0.66f), Mth.lerp(d, green, 0.80f), Mth.lerp(d, blue, 1.0f)};
    }
}
