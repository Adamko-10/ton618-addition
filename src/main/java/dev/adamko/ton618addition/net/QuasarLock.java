package dev.adamko.ton618addition.net;

import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * TA-02's first press: the star this player has locked (an index into {@code QuasarStars.ALL}), or -1 when the lock is
 * gone (cast, timed out, day came). For the reticle's "LOCKED" and the hint to aim at the ground.
 */
public record QuasarLock(int star) implements CustomPacketPayload {
    public static final Type<QuasarLock> TYPE = new Type<>(Ton618Addition.id("quasar_lock"));
    public static final StreamCodec<RegistryFriendlyByteBuf, QuasarLock> CODEC = StreamCodec.of(
        (buf, value) -> value.write(buf), QuasarLock::read);

    @Override
    public Type<QuasarLock> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.star + 1);
    }

    private static QuasarLock read(FriendlyByteBuf buf) {
        return new QuasarLock(buf.readVarInt() - 1);
    }
}
