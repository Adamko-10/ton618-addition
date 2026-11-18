package dev.adamko.ton618addition.quasar;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Which launched stars are still missing from the sky (Adam, 2026-11-15: "For a few nights"): saved with the overworld.
 * For each named star, the overworld day ({@code dayTime / 24000}) from which it is back.
 */
public final class QuasarSky extends SavedData {
    private static final String NAME = "ton618_addition_quasar_sky";

    private final long[] backOnDay = new long[QuasarStars.ALL.size()];

    public static QuasarSky of(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(QuasarSky::load, QuasarSky::new, NAME);
    }

    private static QuasarSky load(CompoundTag tag) {
        QuasarSky sky = new QuasarSky();
        long[] saved = tag.getLongArray("back_on_day");
        System.arraycopy(saved, 0, sky.backOnDay, 0, Math.min(saved.length, sky.backOnDay.length));
        return sky;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("back_on_day", this.backOnDay);
        return tag;
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
