package dev.adamko.ton618addition;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * The server's settings ({@code config/ton618_addition-common.toml}). Read on the server when a cast comes in, so a
 * change to the file takes effect at the next cast.
 */
public final class Ton618AdditionConfig {
    /** Who may cast Last Light (Adam, 2026-11-13: a setting, everyone by default). */
    public enum Who { EVERYONE, OPERATORS }

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.EnumValue<Who> WHO_CAN_CAST;
    private static final ForgeConfigSpec.BooleanValue MORNING;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("last_light");
        ENABLED = b.comment("TA-01 Last Light can be cast on this server.")
            .define("enabled", true);
        WHO_CAN_CAST = b.comment(
                "Who may cast Last Light. It brings the world back for everyone in TON 618's dark, which TON 618 itself",
                "leaves to operators (/ton618 restore): EVERYONE or OPERATORS (permission level 2).")
            .defineEnum("who_can_cast", Who.EVERYONE);
        MORNING = b.comment("When the world comes back, the overworld's time jumps to the next sunrise (the film shows a morning).")
            .define("morning_on_return", true);
        b.pop();
        SPEC = b.build();
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
