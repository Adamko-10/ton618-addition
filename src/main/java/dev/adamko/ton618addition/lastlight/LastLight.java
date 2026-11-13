package dev.adamko.ton618addition.lastlight;

import cyou.rimuru.rimulib.mc1211.api.NetApi;
import cyou.rimuru.rimulib.mc1211.api.SkillApi;
import cyou.rimuru.rimulib.mc1211.skill.SkillHolder;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.Ton618AdditionConfig;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.LastLightFx;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * TA-01 · Last Light on the server (agreed with Adam 2026-11-13). Only in TON 618's dark, after a no-limit TON 618 has
 * devoured the world: time is fast-forwarded until TON618 evaporates by Hawking radiation, and at the final burst the
 * world comes back for everyone (TON 618's own {@code ton618 restore}, run by the server).
 *
 * <p>The film (client side, Fable's) shows all of it; the server only checks the cast, tells everyone in the dark, and
 * at {@link LastLightClock#RESTORE} brings the world back (with a sunrise, {@code morning_on_return}). One Last Light at a
 * time. It keeps running if the caster leaves: the hole's fate is the world's, not theirs.
 */
public final class LastLight {
    /** The one running, or null. */
    private static Running running;

    private LastLight() {
    }

    private static final class Running {
        final LastLightFx fx;
        boolean restored;

        Running(LastLightFx fx) {
            this.fx = fx;
        }

        long tick(MinecraftServer server) {
            return server.overworld().getGameTime() - this.fx.castGameTime();
        }
    }

    /** The cast, from either menu. True when it started (the menu then starts its cooldown). */
    public static boolean cast(ServerPlayer player, LastLightSkill skill) {
        MinecraftServer server = player.server;
        if (!Ton618AdditionConfig.enabled()) {
            return deny(player, LastLightInfo.TITLE + " is turned off on this server");
        }
        if (Ton618AdditionConfig.operatorsOnly() && !player.hasPermissions(2)) {
            return deny(player, "Only operators can cast " + LastLightInfo.NAME + " on this server");
        }
        if (running != null) {
            return deny(player, LastLightInfo.NAME + " is already burning");
        }
        if (!Ton618Link.devoured(server)) {
            return deny(player, "Nothing to evaporate yet: " + LastLightInfo.NAME + " works only after TON 618 has devoured the world");
        }
        if (!Ton618Link.inDark(player)) {
            return deny(player, "Cast " + LastLightInfo.NAME + " in the dark, beside TON618");
        }
        boolean offhand = !(player.getMainHandItem().getItem() instanceof SkillHolder)
            && player.getOffhandItem().getItem() instanceof SkillHolder;
        LastLightFx fx = new LastLightFx(player.getUUID(), player.getId(), server.overworld().getGameTime(), player.position(),
            player.getYRot(), player.getXRot(), offhand, skill.onRemote());
        running = new Running(fx);
        int told = 0;
        ServerLevel dark = server.getLevel(Ton618Link.DARK);
        if (dark != null) {
            for (ServerPlayer watcher : dark.players()) {
                told += send(watcher, fx) ? 1 : 0;
            }
        }
        Ton618Addition.LOG.info("{} cast {} from {} in TON 618's dark; {} player(s) see it. The world comes back in {} s.",
            player.getGameProfile().getName(), LastLightInfo.TITLE, skill.onRemote() ? "the Stellar Remote" : "TON 618",
            told, LastLightClock.RESTORE / 20.0);
        return true;
    }

    /** Every server tick. */
    public static void tick(MinecraftServer server) {
        if (running == null) {
            return;
        }
        long tick = running.tick(server);
        if (!running.restored && tick >= LastLightClock.RESTORE) {
            running.restored = true;
            bringBack(server);
        }
        if (tick >= LastLightClock.END) {
            running = null;
        }
    }

    /**
     * A player arrived in a dimension or logged in: if they are in the dark while Last Light runs (and before the
     * burst), they see the rest of the film (its clock is the game time, so they join it where it is).
     */
    public static void arrived(ServerPlayer player) {
        if (running != null && !running.restored && Ton618Link.inDark(player)
            && running.tick(player.server) < LastLightClock.BURST) {
            send(player, running.fx);
        }
    }

    public static void clear() {
        running = null;
    }

    private static void bringBack(MinecraftServer server) {
        if (!Ton618Link.devoured(server)) {
            Ton618Addition.LOG.info("{}: the world was already back (someone ran /ton618 restore)", LastLightInfo.TITLE);
            return;
        }
        if (Ton618AdditionConfig.morning()) {
            ServerLevel overworld = server.overworld();
            // the next sunrise (day time 0 of the next day): the film's return is always a morning
            overworld.setDayTime((Math.floorDiv(overworld.getDayTime(), 24000L) + 1L) * 24000L);
        }
        if (Ton618Link.restore(server)) {
            Ton618Addition.LOG.info("{}: TON618 has evaporated; the world is back", LastLightInfo.TITLE);
        } else {
            Ton618Addition.LOG.error("{}: TON 618's /ton618 restore didn't bring the world back (has TON 618 changed?)",
                LastLightInfo.TITLE);
            ServerLevel dark = server.getLevel(Ton618Link.DARK);
            if (dark != null) {
                for (ServerPlayer player : dark.players()) {
                    player.sendSystemMessage(Component.literal("[TON 618 Addition] " + LastLightInfo.NAME
                        + " couldn't bring the world back. An operator can run /ton618 restore.").withStyle(ChatFormatting.RED));
                }
            }
        }
    }

    private static boolean send(ServerPlayer player, LastLightFx fx) {
        if (!NetApi.canSend(player, LastLightFx.PACKET)) {
            return false;
        }
        NetApi.send(player, fx);
        return true;
    }

    private static boolean deny(ServerPlayer player, String message) {
        SkillApi.deny(player, Component.literal(message).withStyle(ChatFormatting.DARK_PURPLE));
        return false;
    }
}
