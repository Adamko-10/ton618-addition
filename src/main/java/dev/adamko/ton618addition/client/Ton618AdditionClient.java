package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.client.api.FilmApi;
import cyou.rimuru.rimulib.client.api.MenuApi;
import cyou.rimuru.rimulib.client.api.NetClientApi;
import cyou.rimuru.rimulib.client.api.UiApi;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.client.quasar.QuasarClient;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import dev.adamko.ton618addition.lastlight.LastLightInfo;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.EducationChoice;
import dev.adamko.ton618addition.net.LastLightEnd;
import dev.adamko.ton618addition.net.LastLightFx;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * The client side: the player's settings, the Last Light packet, and handing each cast to the film
 * ({@link LastLightFilm}, Fable's). Holds the running cast so the film can ask for it at any time.
 */
public final class Ton618AdditionClient implements ClientModInitializer {
    @Nullable
    private static LastLightCast current;
    /** RimuLib's films were on last tick (to notice them being switched on mid-cast). */
    private static boolean filmsWereOn;
    /** The educational mode last told to this server, or null when not told yet in this world. */
    @Nullable
    private static Boolean sentEducation;

    private static boolean filmsOn() {
        try {
            return FilmApi.automatic();
        } catch (RuntimeException | LinkageError unsure) {
            return true;
        }
    }

    /** Fabric's client entrypoint (joining TON 618 itself waits for RimuLib's client: client/mixin/RimulibClientMixin). */
    @Override
    public void onInitializeClient() {
        Ton618AdditionClientConfig.SPEC.load();
        LastLightFilm.init();
        QuasarClient.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> stop());
    }

    /** Just before RimuLib's client starts (with {@link Ton618Link#install}; client/mixin/RimulibClientMixin). */
    public static void afterConstruction() {
        NetClientApi.receive(LastLightFx.PACKET, (fx, context) -> Minecraft.getInstance().execute(() -> begin(fx)));
        QuasarClient.afterConstruction();
        // the server dropped the cast before its end (the world came back early, or the restore failed)
        NetClientApi.receive(LastLightEnd.PACKET, (end, context) -> Minecraft.getInstance().execute(() -> {
            if (current != null && current.fx().castGameTime() == end.castGameTime()) {
                stop();
            }
        }));
        // the educational-mode plate in the H menu and its key, through RimuLib's public API
        try {
            UiApi.onMenuDraw((graphics, partial) -> EducationPlate.draw(graphics));
            MenuApi.onKey(EducationPlate::key);
        } catch (RuntimeException | LinkageError failed) {
            Ton618Addition.LOG.warn("Couldn't add the educational-mode switch to the H menu: {}", failed.toString());
        }
        // the menu's warning when Last Light is picked anywhere but in TON 618's dark. None while there is no world: RimuLib
        // makes the skill keys once at start and gives none to a skill with a warning then (its default keys ; and '
        // never existed before 2026-11-20); a key only picks the skill, the server still says why it can't be cast
        LastLightInfo.selectWarning = () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.level == null || mc.level.dimension() == Ton618Link.DARK ? null
                // short enough for the menu's warning line (Adam, 2026-11-17)
                : "Not yet: set TON618's Scale to ∞ and use it first";
        };
    }

    /** The Last Light running now (its film's data and clock), or null. */
    @Nullable
    public static LastLightCast current() {
        return current;
    }

    private static void begin(LastLightFx fx) {
        if (current != null && current.fx().castGameTime() == fx.castGameTime()) {
            return;
        }
        current = new LastLightCast(fx);
        boolean filmsOn = true;
        boolean other = false;
        try {
            filmsOn = FilmApi.automatic();
            other = FilmApi.active();
            // RimuLib plays one film at a time and queues a new one behind any film still running, whatever its
            // priority: TON 618's own film runs on for a while in the dark, so Last Light's would wait unseen
            if (other) {
                FilmApi.skip();
            }
        } catch (RuntimeException | LinkageError unsure) {
            // the film starts or queues as RimuLib decides
        }
        filmsWereOn = filmsOn;
        Ton618Addition.LOG.info("{} reached this game at film tick {} (films {}{}).", LastLightInfo.TITLE, current.tick(),
            filmsOn ? "on" : "off: RimuLib's film key turns them on", other ? "; another film was playing and was skipped" : "");
        try {
            LastLightFilm.start(current);
        } catch (RuntimeException failed) {
            Ton618Addition.LOG.error("The Last Light film failed to start", failed);
        }
    }

    private static void tick() {
        sendEducation();
        if (current == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        current.clientTick();
        // films switched on mid-cast (RimuLib's film key, as the notice says): start the film where the cast is
        boolean filmsOn = filmsOn();
        if (filmsOn && !filmsWereOn && current.tick() < LastLightClock.BURST - 20) {
            try {
                LastLightFilm.start(current);
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("The Last Light film failed to start", failed);
            }
        }
        filmsWereOn = filmsOn;
        if (current.tick() > LastLightClock.END + 40) {
            current = null;
            if (LastLightFilm.active()) {
                LastLightFilm.stop();
            }
        }
    }

    /**
     * Tells the server this player's educational mode once in each world and whenever it changes (the H-menu switch or
     * the config screen): a strike they cast then runs its reading windows slower ({@code ReadingTime}).
     */
    private static void sendEducation() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            sentEducation = null;
            return;
        }
        boolean on = Ton618AdditionClientConfig.educational();
        if (sentEducation != null && sentEducation == on) {
            return;
        }
        try {
            if (NetClientApi.canSend(EducationChoice.PACKET)) {
                NetClientApi.send(new EducationChoice(on));
                sentEducation = on;
            }
        } catch (RuntimeException | LinkageError unsure) {
            sentEducation = on;
        }
    }

    /**
     * Leaving the server, or the server dropped the cast early ({@link LastLightEnd}). A dimension change is not this:
     * the film runs on through the trip back.
     */
    private static void stop() {
        if (current != null) {
            current = null;
            try {
                LastLightFilm.stop();
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("The Last Light film failed to stop", failed);
            }
        }
    }
}
