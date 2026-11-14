package dev.adamko.ton618addition;

import com.mojang.logging.LogUtils;
import dev.adamko.ton618addition.lastlight.LastLight;
import dev.adamko.ton618addition.link.Ton618Link;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * TON 618 Addition: an unofficial addon for rimuru_dev's TON 618 (this branch: Fabric 1.21.1).
 *
 * <ul>
 *   <li>{@link Ton618Link}: the one place that joins TON 618 (and, when installed, The Shooting Star's Stellar Remote),
 *       through RimuLib's public API, TON 618's command storage and its own commands only.</li>
 *   <li>{@link LastLight}: TA-01 · Last Light on the server (its checks, its clock, bringing the world back).</li>
 *   <li>{@link Ton618AdditionConfig}: the server settings; the client's are in {@code client/}.</li>
 *   <li>{@code client/film/}: the film, made by Fable (see CLAUDE.md, "For Fable: the film").</li>
 * </ul>
 *
 * <p>Fabric runs main entrypoints in no fixed order, so joining TON 618 waits until every mod's main entrypoint has run:
 * just before RimuLib's client starts ({@code client/mixin/RimulibClientMixin}, before it makes the skills' keys) or,
 * on a dedicated server, in {@link Ton618AdditionServer} (as the SA Series does). {@link Ton618Link#install} runs once.
 */
public final class Ton618Addition implements ModInitializer {
    public static final String MOD_ID = "ton618_addition";
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        Ton618AdditionConfig.SPEC.load();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            Ton618AdditionConfig.SPEC.load();
            Ton618Link.install();
        });
        ServerTickEvents.END_SERVER_TICK.register(LastLight::tick);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LastLight.clear());
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> LastLight.arrived(player));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> LastLight.arrived(handler.player));
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
