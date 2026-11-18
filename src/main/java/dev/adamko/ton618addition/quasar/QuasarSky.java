package dev.adamko.ton618addition.quasar;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Which launched stars are still missing from the sky (Adam, 2026-11-15: "For a few nights"): saved with the overworld.
 * For each named star, the overworld day ({@code dayTime / 24000}) from which it is back.
 */
public final class QuasarSky extends SavedData {
    /** (26.3: saved data is named by an id; the file stays data/ton618_addition/quasar_sky.dat-like under the world.) */
    private static final net.minecraft.resources.Identifier NAME = dev.adamko.ton618addition.Ton618Addition.id("quasar_sky");
    /** Minecraft 1.21.11: saved data is written through a codec (the same key, back_on_day, as on the other branches). */
    private static final Codec<QuasarSky> CODEC = Codec.LONG.listOf().optionalFieldOf("back_on_day", List.of())
        .xmap(QuasarSky::load, sky -> java.util.Arrays.stream(sky.backOnDay).boxed().toList()).codec();
    private static final SavedDataType<QuasarSky> TYPE = new SavedDataType<>(NAME, QuasarSky::new, CODEC, null);

    private final long[] backOnDay = new long[QuasarStars.ALL.size()];

    public static QuasarSky of(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    private static QuasarSky load(List<Long> saved) {
        QuasarSky sky = new QuasarSky();
        for (int i = 0; i < Math.min(saved.size(), sky.backOnDay.length); i++) {
            sky.backOnDay[i] = saved.get(i);
        }
        return sky;
    }

    /** The day of {@code dayTime} (day 0 is the world's first). */
    public static long day(long dayTime) {
        return Math.floorDiv(dayTime, 24000L);
    }

    /** The star is gone from the sky on {@code today}. */
    public boolean missing(int star, long today) {
        return today < this.backOnDay[star];
    }

    public long backOn(int star) {
        return this.backOnDay[star];
    }

    /** Launched now: gone for {@code nights} nights (tonight counts as the first). */
    public void launch(int star, long today, int nights) {
        this.backOnDay[star] = today + Math.max(0, nights);
        this.setDirty();
    }

    public long[] copy() {
        return this.backOnDay.clone();
    }
}
