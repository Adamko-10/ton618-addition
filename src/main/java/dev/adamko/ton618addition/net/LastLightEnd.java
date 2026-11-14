package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.mc1201.api.NetApi;
import cyou.rimuru.rimulib.mc1201.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;

/**
 * The server dropped a running Last Light before its end (Fable's proposal P1, Adam picked it 2026-11-14): the world
 * came back early (an operator's {@code /ton618 restore} mid-film) or the restore failed. Sent to everyone who was
 * shown the cast; their film stops.
 *
 * @param castGameTime which cast ({@link LastLightFx#castGameTime()})
 */
public record LastLightEnd(long castGameTime) {
    public static final RimuPacket<LastLightEnd> PACKET = NetApi.packet(Ton618Addition.id("last_light_end"), LastLightEnd.class,
        LastLightEnd::write, LastLightEnd::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeLong(this.castGameTime);
    }

    private static LastLightEnd read(FriendlyByteBuf buf) {
        return new LastLightEnd(buf.readLong());
    }
}
