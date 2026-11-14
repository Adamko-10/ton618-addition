package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.mc1211.client.api.NetClientApi;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import dev.adamko.ton618addition.lastlight.LastLightInfo;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.LastLightEnd;
import dev.adamko.ton618addition.net.LastLightFx;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The client side: the player's settings, the Last Light packet, and handing each cast to the film
 * ({@link LastLightFilm}, Fable's). Holds the running cast so the film can ask for it at any time.
 */
public final class Ton618AdditionClient {
    @Nullable
    private static LastLightCast current;

    private Ton618AdditionClient() {
    }

    /** From the mod's constructor, on clients only. */
    public static void init(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, Ton618AdditionClientConfig.SPEC);
        // Mods, TON 618 Addition, Config: NeoForge's own screen for our settings
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        LastLightFilm.init(modBus);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> stop());
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
        // the menu's warning when Last Light is picked anywhere but in TON 618's dark
        LastLightInfo.selectWarning = () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.level != null && mc.level.dimension() == Ton618Link.DARK ? null
                : "Only in the dark, after TON 618 has devoured the world";
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
