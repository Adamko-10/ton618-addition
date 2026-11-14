package dev.adamko.ton618addition.net;

import dev.adamko.ton618addition.Ton618Addition;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

/**
 * A Last Light cast, sent to every player in TON 618's dark (Adam, 2026-11-13: everyone in the dark sees the whole
 * film), and to anyone who arrives there while it runs. The film's clock is {@code level game time - castGameTime}:
 * the dark and the overworld share the game time, so it keeps counting through the trip back.
 *
 * @param caster        the caster's UUID (their entity id can change when they change dimension)
 * @param casterId      the caster's entity id when they cast
 * @param castGameTime  the game time of the cast (tick 0 of {@code LastLightClock})
 * @param position      where the caster stood
 * @param yaw           where they looked (degrees)
 * @param pitch         ditto
 * @param offhand       the item that fired it is in their off hand
 * @param remote        it was fired from The Shooting Star's Stellar Remote (otherwise TON 618's own item)
 */
public record LastLightFx(UUID caster, int casterId, long castGameTime, Vec3 position, float yaw, float pitch,
                          boolean offhand, boolean remote) implements CustomPacketPayload {
    public static final Type<LastLightFx> TYPE = new Type<>(Ton618Addition.id("last_light"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LastLightFx> CODEC = StreamCodec.of(
        (buf, value) -> value.write(buf), LastLightFx::read);

    @Override
    public Type<LastLightFx> type() {
        return TYPE;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.caster);
        buf.writeVarInt(this.casterId);
        buf.writeLong(this.castGameTime);
        // 26.3's FriendlyByteBuf has no writeVec3/readVec3: three doubles
        buf.writeDouble(this.position.x);
        buf.writeDouble(this.position.y);
        buf.writeDouble(this.position.z);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeBoolean(this.offhand);
        buf.writeBoolean(this.remote);
    }

    private static LastLightFx read(FriendlyByteBuf buf) {
        return new LastLightFx(buf.readUUID(), buf.readVarInt(), buf.readLong(), new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()), buf.readFloat(),
            buf.readFloat(), buf.readBoolean(), buf.readBoolean());
    }
}
