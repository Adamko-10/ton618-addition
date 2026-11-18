package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.mc1201.api.NetApi;
import cyou.rimuru.rimulib.mc1201.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;

/**
 * TA-02's first press: the star this player has locked (an index into {@code QuasarStars.ALL}), or -1 when the lock is
 * gone (cast, timed out, day came). For the reticle's "LOCKED" and the hint to aim at the ground.
 */
public record QuasarLock(int star) {
    public static final RimuPacket<QuasarLock> PACKET = NetApi.packet(Ton618Addition.id("quasar_lock"), QuasarLock.class,
        QuasarLock::write, QuasarLock::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.star + 1);
    }

    private static QuasarLock read(FriendlyByteBuf buf) {
        return new QuasarLock(buf.readVarInt() - 1);
    }
}
