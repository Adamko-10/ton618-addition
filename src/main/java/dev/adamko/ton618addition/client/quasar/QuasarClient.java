package dev.adamko.ton618addition.client.quasar;

import cyou.rimuru.rimulib.mc1211.client.api.NetClientApi;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.net.QuasarFx;
import dev.adamko.ton618addition.net.QuasarLock;
import dev.adamko.ton618addition.net.QuasarMissing;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarInfo;
import dev.adamko.ton618addition.quasar.QuasarSky;
import dev.adamko.ton618addition.quasar.QuasarStars;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

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

    public static void init(IEventBus modBus) {
        QuasarJetsFilm.init(modBus);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> reset());
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, QuasarRender::render);
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event -> QuasarReticle.draw(event.getGuiGraphics(),
            event.getPartialTick().getGameTimeDeltaPartialTick(true)));
        NeoForge.EVENT_BUS.addListener(ViewportEvent.ComputeFogColor.class, QuasarClient::fog);
    }

    /** After every mod is constructed (the packets are declared by then). */
    public static void afterConstruction() {
        NetClientApi.receive(QuasarFx.PACKET, (fx, context) -> Minecraft.getInstance().execute(() -> begin(fx)));
        NetClientApi.receive(QuasarLock.PACKET, (lock, context) -> Minecraft.getInstance().execute(() -> lockedStar = lock.star()));
        NetClientApi.receive(QuasarMissing.PACKET, (missing, context) -> Minecraft.getInstance().execute(() -> {
            long[] days = new long[QuasarStars.ALL.size()];
            System.arraycopy(missing.backOnDay(), 0, days, 0, Math.min(days.length, missing.backOnDay().length));
            backOnDay = days;
        }));
        QuasarInfo.selectWarning = () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.level != null && mc.level.dimension() == Level.OVERWORLD && mc.level.isNight() ? null
                : "Night only, in the overworld: cast at a named star, then at the ground";
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

    /** The named star is in the sky today (not launched lately). */
    public static boolean starShown(int star) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        return QuasarSky.day(mc.level.getDayTime()) >= backOnDay[star];
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
    private static void fog(ViewportEvent.ComputeFogColor event) {
        float d = secondSun((float) event.getPartialTick());
        if (d <= 0f) {
            return;
        }
        event.setRed(Mth.lerp(d, event.getRed(), 0.66f));
        event.setGreen(Mth.lerp(d, event.getGreen(), 0.80f));
        event.setBlue(Mth.lerp(d, event.getBlue(), 1.0f));
    }
}
