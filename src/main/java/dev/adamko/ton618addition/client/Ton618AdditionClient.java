package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.mc1201.client.api.FilmApi;
import cyou.rimuru.rimulib.mc1201.client.api.MenuApi;
import cyou.rimuru.rimulib.mc1201.client.api.NetClientApi;
import cyou.rimuru.rimulib.mc1201.client.api.UiApi;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import dev.adamko.ton618addition.lastlight.LastLightInfo;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.LastLightEnd;
import dev.adamko.ton618addition.net.LastLightFx;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * The client side: the player's settings, the Last Light packet, and handing each cast to the film
 * ({@link LastLightFilm}, Fable's). Holds the running cast so the film can ask for it at any time.
 */
public final class Ton618AdditionClient {
    @Nullable
    private static LastLightCast current;
    /** RimuLib's films were on last tick (to notice them being switched on mid-cast). */
    private static boolean filmsWereOn;

    private static boolean filmsOn() {
        try {
            return FilmApi.automatic();
        } catch (RuntimeException | LinkageError unsure) {
            return true;
        }
    }

    private Ton618AdditionClient() {
    }

    /** From the mod's constructor, on clients only. */
    public static void init(IEventBus modBus, FMLJavaModLoadingContext context) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Ton618AdditionClientConfig.SPEC);
        // (Forge has no generic config screen like NeoForge's: the H-menu plate and the toml hold the settings)
        LastLightFilm.init(modBus);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                tick();
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> stop());
    }

    /** After every mod is constructed (with {@link Ton618Link#install}). */
    public static void afterConstruction() {
        NetClientApi.receive(LastLightFx.PACKET, (fx, context) -> Minecraft.getInstance().execute(() -> begin(fx)));
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
        // the menu's warning when Last Light is picked anywhere but in TON 618's dark
        LastLightInfo.selectWarning = () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.level != null && mc.level.dimension() == Ton618Link.DARK ? null
                : "Locked: set TON618's Scale to ∞ and use it, then cast Last Light in the dark";
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
