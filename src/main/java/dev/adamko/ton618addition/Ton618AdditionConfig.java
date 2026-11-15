package dev.adamko.ton618addition;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The server's settings ({@code config/ton618_addition-common.toml}). Read on the server when a cast comes in, so a
 * change to the file takes effect at the next cast.
 */
public final class Ton618AdditionConfig {
    /** Who may cast Last Light (Adam, 2026-11-13: a setting, everyone by default). */
    public enum Who { EVERYONE, OPERATORS }

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.EnumValue<Who> WHO_CAN_CAST;
    private static final ModConfigSpec.BooleanValue MORNING;
    private static final ModConfigSpec.BooleanValue QUASAR_ENABLED;
    private static final ModConfigSpec.EnumValue<Who> QUASAR_WHO;
    private static final ModConfigSpec.BooleanValue QUASAR_TERRAIN;
    private static final ModConfigSpec.BooleanValue QUASAR_SECOND_SUN;
    private static final ModConfigSpec.BooleanValue QUASAR_CARRY_PLAYERS;
    private static final ModConfigSpec.IntValue QUASAR_MAX_CARRIED;
    private static final ModConfigSpec.IntValue QUASAR_MISSING_NIGHTS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
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
        b.push("quasar_jets");
        QUASAR_ENABLED = b.comment("TA-02 Quasar Jets can be cast on this server (it only appears with SS-21 Quasar installed).")
            .define("enabled", true);
        QUASAR_WHO = b.comment("Who may cast Quasar Jets: EVERYONE or OPERATORS (permission level 2).")
            .defineEnum("who_can_cast", Who.EVERYONE);
        QUASAR_TERRAIN = b.comment("The radio lobe melts a glassy bowl, leaves frozen-light crystals and the jet lifts loose blocks.",
                "false: no blocks are changed (the light show, the carrying and the damage stay).")
            .define("terrain", true);
        QUASAR_SECOND_SUN = b.comment("The second sun: the night turns to day for everyone in the overworld while the strike runs.")
            .define("second_sun", true);
        QUASAR_CARRY_PLAYERS = b.comment("Players in the jet's path are carried along like mobs and items (the caster never is).")
            .define("carry_players", false);
        QUASAR_MAX_CARRIED = b.comment("At most this many mobs, items and blocks are carried by the jet.")
            .defineInRange("max_carried", 40, 0, 200);
        QUASAR_MISSING_NIGHTS = b.comment("A launched star is missing from the night sky for this many nights (0: back at once).")
            .defineInRange("missing_nights", 3, 0, 100);
        b.pop();
        SPEC = b.build();
    }

    public static boolean quasarEnabled() {
        return !SPEC.isLoaded() || QUASAR_ENABLED.get();
    }

    public static boolean quasarOperatorsOnly() {
        return SPEC.isLoaded() && QUASAR_WHO.get() == Who.OPERATORS;
    }

    public static boolean quasarTerrain() {
        return !SPEC.isLoaded() || QUASAR_TERRAIN.get();
    }

    public static boolean quasarSecondSun() {
        return !SPEC.isLoaded() || QUASAR_SECOND_SUN.get();
    }

    public static boolean quasarCarryPlayers() {
        return SPEC.isLoaded() && QUASAR_CARRY_PLAYERS.get();
    }

    public static int quasarMaxCarried() {
        return SPEC.isLoaded() ? QUASAR_MAX_CARRIED.get() : 40;
    }

    public static int quasarMissingNights() {
        return SPEC.isLoaded() ? QUASAR_MISSING_NIGHTS.get() : 3;
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
