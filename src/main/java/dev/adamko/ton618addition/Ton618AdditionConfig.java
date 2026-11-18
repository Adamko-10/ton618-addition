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
    private static final ConfigFile.BoolValue QUASAR_ENABLED;
    private static final ConfigFile.EnumValue<Who> QUASAR_WHO;
    private static final ConfigFile.BoolValue QUASAR_TERRAIN;
    private static final ConfigFile.BoolValue QUASAR_SECOND_SUN;
    private static final ConfigFile.BoolValue QUASAR_CARRY_PLAYERS;
    private static final ConfigFile.IntValue QUASAR_MAX_CARRIED;
    private static final ConfigFile.IntValue QUASAR_MISSING_NIGHTS;
    private static final ConfigFile.DoubleValue QUASAR_MAX_POWER;
    private static final ConfigFile.DoubleValue QUASAR_MAX_DAMAGE;

    static {
        SPEC.section("last_light");
        ENABLED = SPEC.define("enabled", true, "TA-01 Last Light can be cast on this server.");
        WHO_CAN_CAST = SPEC.defineEnum("who_can_cast", Who.EVERYONE,
            "Who may cast Last Light. It brings the world back for everyone in TON 618's dark, which TON 618 itself",
            "leaves to operators (/ton618 restore): EVERYONE or OPERATORS (permission level 2).");
        MORNING = SPEC.define("morning_on_return", true,
            "When the world comes back, the overworld's time jumps to the next sunrise (the film shows a morning).");
        SPEC.section("quasar_jets");
        QUASAR_ENABLED = SPEC.define("enabled", true,
            "TA-02 Quasar Jets can be cast on this server (it stays locked without SS-21 Quasar installed).");
        QUASAR_WHO = SPEC.defineEnum("who_can_cast", Who.EVERYONE,
            "Who may cast Quasar Jets: EVERYONE or OPERATORS (permission level 2).");
        QUASAR_TERRAIN = SPEC.define("terrain", true,
            "The landing blasts a crater, the radio lobe melts a glassy bowl and leaves frozen-light crystals,",
            "and the jet lifts loose blocks. false: no blocks are changed (the light show, the carrying and the damage stay).");
        QUASAR_SECOND_SUN = SPEC.define("second_sun", true,
            "The second sun: the night turns to day for everyone in the overworld while the strike runs.");
        QUASAR_CARRY_PLAYERS = SPEC.define("carry_players", false,
            "Players in the jet's path are carried along like mobs and items (the caster never is).");
        QUASAR_MAX_CARRIED = SPEC.defineInRange("max_carried", 40, 0, 200,
            "At most this many mobs, items and blocks are carried by the jet.");
        QUASAR_MISSING_NIGHTS = SPEC.defineInRange("missing_nights", 3, 0, 100,
            "A launched star is missing from the night sky for this many nights (0: back at once).");
        QUASAR_MAX_POWER = SPEC.defineInRange("max_power", 3.0, 0.1, 3.0,
            "The most Power a player may pick on Quasar Jets' slider (0.1 to 3, as the slider goes).",
            "More Power cuts a wider crater (up to 1800 blocks across at 3), which can keep a server busy for a while.",
            "A cast above it is held to it, and the caster is told.");
        QUASAR_MAX_DAMAGE = SPEC.defineInRange("max_damage", 0.0, 0.0, 100000.0,
            "The most Damage Quasar Jets' landing may do at its middle, in health points. 0: no cap, so the",
            "slider's ∞ (its default) kills everything in the blast's reach. Above 0, ∞ becomes this number.");
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

    public static float quasarMaxPower() {
        return SPEC.isLoaded() ? QUASAR_MAX_POWER.get().floatValue() : 3F;
    }

    /** 0: no cap. */
    public static float quasarMaxDamage() {
        return SPEC.isLoaded() ? QUASAR_MAX_DAMAGE.get().floatValue() : 0F;
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
