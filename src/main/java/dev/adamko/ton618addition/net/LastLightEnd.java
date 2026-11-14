package dev.adamko.ton618addition.net;

import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * The server dropped a running Last Light before its end (Fable's proposal P1, Adam picked it 2026-11-14): the world
 * came back early (an operator's {@code /ton618 restore} mid-film) or the restore failed. Sent to everyone who was
 * shown the cast; their film stops.
 *
 * @param castGameTime which cast ({@link LastLightFx#castGameTime()})
 */
public record LastLightEnd(long castGameTime) implements CustomPacketPayload {
    public static final Type<LastLightEnd> TYPE = new Type<>(Ton618Addition.id("last_light_end"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LastLightEnd> CODEC = StreamCodec.of(
        (buf, value) -> value.write(buf), LastLightEnd::read);

    @Override
    public Type<LastLightEnd> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeLong(this.castGameTime);
    }

    private static LastLightEnd read(FriendlyByteBuf buf) {
        return new LastLightEnd(buf.readLong());
    }
}
