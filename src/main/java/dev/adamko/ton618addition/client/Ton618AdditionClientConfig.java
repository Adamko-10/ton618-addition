package dev.adamko.ton618addition.client;

import dev.adamko.ton618addition.config.ConfigFile;

/**
 * Each player's own settings ({@code config/ton618_addition-client.toml}, the same file as on NeoForge), switched in the
 * H menu's plate ({@link EducationPlate}).
 */
public final class Ton618AdditionClientConfig {
    public static final ConfigFile SPEC = new ConfigFile("ton618_addition-client.toml");
    private static final ConfigFile.BoolValue EDUCATIONAL;

    static {
        SPEC.section("film");
        EDUCATIONAL = SPEC.define("educational_mode", false,
            "Educational mode: small labels in the Last Light film pointing at the real physics (Hawking radiation,",
            "the eras of the universe, the information paradox). Off by default.");
    }

    private Ton618AdditionClientConfig() {
    }

    /** Educational mode is on: the film shows its physics labels. */
    public static boolean educational() {
        return SPEC.isLoaded() && EDUCATIONAL.get();
    }

    /** Sets and saves educational mode (from the H-menu plate, {@link EducationPlate}). */
    public static void setEducational(boolean on) {
        if (!SPEC.isLoaded()) {
            return;
        }
        EDUCATIONAL.set(on);
        SPEC.save();
    }
}
