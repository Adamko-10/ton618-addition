package dev.adamko.ton618addition.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Each player's own settings ({@code config/ton618_addition-client.toml}, also in game: Mods, TON 618 Addition,
 * Config).
 */
public final class Ton618AdditionClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue EDUCATIONAL;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("film");
        EDUCATIONAL = b.comment(
                "Educational mode: small labels in the Last Light film pointing at the real physics (Hawking radiation,",
                "the eras of the universe, the information paradox). Off by default.")
            .define("educational_mode", false);
        b.pop();
        SPEC = b.build();
    }

    private Ton618AdditionClientConfig() {
    }

    /** Educational mode is on: the film shows its physics labels. */
    public static boolean educational() {
        return SPEC.isLoaded() && EDUCATIONAL.get();
    }
}
