package dev.adamko.ton618addition.net;

import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Client to server: this player's educational mode (sent once in a world and whenever it is switched). */
public record EducationChoice(boolean on) implements CustomPacketPayload {
    public static final Type<EducationChoice> TYPE = new Type<>(Ton618Addition.id("education"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EducationChoice> CODEC = StreamCodec.of(
        (buf, value) -> value.write(buf), EducationChoice::read);

    @Override
    public Type<EducationChoice> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.on);
    }

    private static EducationChoice read(FriendlyByteBuf buf) {
        return new EducationChoice(buf.readBoolean());
    }
}
