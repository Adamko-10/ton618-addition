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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * TON 618 Addition: an unofficial addon for rimuru_dev's TON 618 (this branch: Forge 1.20.1, also NeoForge 1.20.1).
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

    public Ton618Addition() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modBus = context.getModEventBus();
        boolean client = FMLEnvironment.dist.isClient();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Ton618AdditionConfig.SPEC);
        // Mods are constructed in parallel: join TON 618 once every mod has been constructed (the queue runs on the main
        // thread), still before RimuLib registers its packets and makes the skills' keys (as the SA Series does).
        modBus.addListener((FMLConstructModEvent event) -> event.enqueueWork(() -> {
            Ton618Link.install();
            if (client) {
                Ton618AdditionClient.afterConstruction();
            }
        }));

        if (client) {
            Ton618AdditionClient.init(modBus, context);
        }

        QuasarContent.register(modBus);

        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                LastLight.tick(event.getServer());
                QuasarJets.tick(event.getServer());
            }
        });
        MinecraftForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            LastLight.clear();
            QuasarJets.clear();
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                LastLight.arrived(player);
                QuasarJets.arrived(player);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                LastLight.arrived(player);
                QuasarJets.arrived(player);
            }
        });
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                QuasarJets.left(player);
                Education.forget(player.getUUID());
            }
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
