package dev.adamko.ton618addition.link;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerPlayer;

/**
 * Which players have educational mode on (their client setting, sent with {@code net/EducationChoice}). A strike takes
 * its caster's: with it on, the strike runs its reading windows slower for everyone watching ({@code ReadingTime}).
 */
public final class Education {
    private static final Set<UUID> ON = ConcurrentHashMap.newKeySet();

    private Education() {
    }

    public static boolean on(ServerPlayer player) {
        return ON.contains(player.getUUID());
    }

    public static void set(UUID player, boolean on) {
        if (on) {
            ON.add(player);
        } else {
            ON.remove(player);
        }
    }

    public static void forget(UUID player) {
        ON.remove(player);
    }
}
