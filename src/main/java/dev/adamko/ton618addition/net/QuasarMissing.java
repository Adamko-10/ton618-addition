package dev.adamko.ton618addition.net;

import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The launched stars still missing from the sky: for each named star, the overworld day ({@code dayTime / 24000}) from
 * which it is back, or 0 if it was never launched. Sent on login and whenever one is launched.
 */
public record QuasarMissing(long[] backOnDay) implements CustomPacketPayload {
    public static final Type<QuasarMissing> TYPE = new Type<>(Ton618Addition.id("quasar_missing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuasarMissing> CODEC = StreamCodec.of(
        (buf, value) -> value.write(buf), QuasarMissing::read);

    @Override
    public Type<QuasarMissing> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.backOnDay.length);
        for (long day : this.backOnDay) {
            buf.writeVarLong(day);
        }
    }

    private static QuasarMissing read(FriendlyByteBuf buf) {
        int n = Math.min(buf.readVarInt(), 64);
        long[] days = new long[n];
        for (int i = 0; i < n; i++) {
            days[i] = buf.readVarLong();
        }
        return new QuasarMissing(days);
    }
}
