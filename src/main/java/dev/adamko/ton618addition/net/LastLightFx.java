package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.api.NetApi;
import cyou.rimuru.rimulib.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
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
                          boolean offhand, boolean remote) {
    public static final RimuPacket<LastLightFx> PACKET = NetApi.packet(Ton618Addition.id("last_light"), LastLightFx.class,
        LastLightFx::write, LastLightFx::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.caster);
        buf.writeVarInt(this.casterId);
        buf.writeLong(this.castGameTime);
        buf.writeVec3(this.position);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeBoolean(this.offhand);
        buf.writeBoolean(this.remote);
    }

    private static LastLightFx read(FriendlyByteBuf buf) {
        return new LastLightFx(buf.readUUID(), buf.readVarInt(), buf.readLong(), buf.readVec3(), buf.readFloat(),
            buf.readFloat(), buf.readBoolean(), buf.readBoolean());
    }
}
