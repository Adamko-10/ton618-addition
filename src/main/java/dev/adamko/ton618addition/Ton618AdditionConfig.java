package dev.adamko.ton618addition;

import dev.adamko.ton618addition.config.ConfigFile;

/**
 * The server's settings ({@code config/ton618_addition-common.toml}, the same file and layout as on NeoForge; written by
 * the SA Series' small {@link ConfigFile} on Fabric). Loaded at start-up and again when a server starts.
 */
public final class Ton618AdditionConfig {
    /** Who may cast Last Light (Adam, 2026-11-13: a setting, everyone by default). */
    public enum Who { EVERYONE, OPERATORS }

    public static final ConfigFile SPEC = new ConfigFile("ton618_addition-common.toml");
    private static final ConfigFile.BoolValue ENABLED;
    private static final ConfigFile.EnumValue<Who> WHO_CAN_CAST;
    private static final ConfigFile.BoolValue MORNING;

    static {
        SPEC.section("last_light");
        ENABLED = SPEC.define("enabled", true, "TA-01 Last Light can be cast on this server.");
        WHO_CAN_CAST = SPEC.defineEnum("who_can_cast", Who.EVERYONE,
            "Who may cast Last Light. It brings the world back for everyone in TON 618's dark, which TON 618 itself",
            "leaves to operators (/ton618 restore): EVERYONE or OPERATORS (permission level 2).");
        MORNING = SPEC.define("morning_on_return", true,
            "When the world comes back, the overworld's time jumps to the next sunrise (the film shows a morning).");
    }

    private Ton618AdditionConfig() {
    }

    public static boolean enabled() {
        return !SPEC.isLoaded() || ENABLED.get();
    }

    public static boolean operatorsOnly() {
        return SPEC.isLoaded() && WHO_CAN_CAST.get() == Who.OPERATORS;
    }

    public static boolean morning() {
        return !SPEC.isLoaded() || MORNING.get();
    }
}
