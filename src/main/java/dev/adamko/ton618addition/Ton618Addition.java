package dev.adamko.ton618addition;

import com.mojang.logging.LogUtils;
import dev.adamko.ton618addition.client.Ton618AdditionClient;
import dev.adamko.ton618addition.lastlight.LastLight;
import dev.adamko.ton618addition.link.Education;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.quasar.QuasarContent;
import dev.adamko.ton618addition.quasar.QuasarJets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * TON 618 Addition: an unofficial addon for rimuru_dev's TON 618.
 *
 * <ul>
 *   <li>{@link Ton618Link}: the one place that joins TON 618 (and, when installed, The Shooting Star's Stellar Remote),
 *       through RimuLib's public API, TON 618's command storage and its own commands only.</li>
 *   <li>{@link LastLight}: TA-01 · Last Light on the server (its checks, its clock, bringing the world back).</li>
 *   <li>{@link QuasarJets}: TA-02 · Quasar Jets on the server (with SS-21 Quasar installed only); {@link QuasarContent}
 *       its frozen-light crystals.</li>
 *   <li>{@link Ton618AdditionConfig}: the server settings; the client's are in {@code client/}.</li>
 *   <li>{@code client/film/}: the film, made by Fable (see CLAUDE.md, "For Fable: the film").</li>
 * </ul>
 */
@Mod(Ton618Addition.MOD_ID)
public final class Ton618Addition {
    public static final String MOD_ID = "ton618_addition";
    public static final Logger LOG = LogUtils.getLogger();

    public Ton618Addition(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.COMMON, Ton618AdditionConfig.SPEC);
        // Mods are constructed in parallel: join TON 618 once every mod has been constructed (the queue runs on the main
        // thread), still before RimuLib registers its packets and makes the skills' keys (as the SA Series does).
        modBus.addListener(FMLConstructModEvent.class, event -> event.enqueueWork(() -> {
            Ton618Link.install();
            if (dist.isClient()) {
                Ton618AdditionClient.afterConstruction();
            }
        }));

        if (dist.isClient()) {
            Ton618AdditionClient.init(modBus, container);
        }

        QuasarContent.register(modBus);

        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> {
            LastLight.tick(event.getServer());
            QuasarJets.tick(event.getServer());
        });
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, event -> {
            LastLight.clear();
            QuasarJets.clear();
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                LastLight.arrived(player);
                QuasarJets.arrived(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                LastLight.arrived(player);
                QuasarJets.arrived(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                QuasarJets.left(player);
                Education.forget(player.getUUID());
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
