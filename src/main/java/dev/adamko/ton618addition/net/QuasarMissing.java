package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.mc1201.api.NetApi;
import cyou.rimuru.rimulib.mc1201.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;

/**
 * The launched stars still missing from the sky: for each named star, the overworld day ({@code dayTime / 24000}) from
 * which it is back, or 0 if it was never launched. Sent on login and whenever one is launched.
 */
public record QuasarMissing(long[] backOnDay) {
    public static final RimuPacket<QuasarMissing> PACKET = NetApi.packet(Ton618Addition.id("quasar_missing"),
        QuasarMissing.class, QuasarMissing::write, QuasarMissing::read);

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
