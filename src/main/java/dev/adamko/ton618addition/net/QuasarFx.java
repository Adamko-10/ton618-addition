package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.mc1201.api.NetApi;
import cyou.rimuru.rimulib.mc1201.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * A TA-02 · Quasar Jets strike, sent to everyone in the overworld (the second sun is everyone's, the beam and the lobe
 * whoever is near) and to anyone who arrives there while it runs. The clock is {@code game time - castGameTime}.
 *
 * @param caster       the caster's UUID
 * @param casterId     the caster's entity id when they cast
 * @param castGameTime the game time of the second press (tick 0 of {@code QuasarClock})
 * @param mark         where the jet lands (the strike point, on the ground)
 * @param star         which named star (index into {@code QuasarStars.ALL})
 * @param quasar       the quasar's direction in the sky (unit vector; the jet comes in along it)
 * @param position     where the caster stood
 * @param yaw          where they looked (degrees)
 * @param pitch        ditto
 * @param offhand      the item that fired it is in their off hand
 * @param remote       it was fired from the Stellar Remote (otherwise TON 618's own item)
 * @param film         this player gets the film (the caster and players within 128 blocks); the rest see the world only
 * @param secondSun    the server allows the second sun
 * @param educational  the caster had educational mode on: the reading windows run slower ({@code ReadingTime})
 * @param power        the Power slider: the blast's size, times the star's own (0.1 to 3)
 * @param damage       the Damage slider: the hit at the blast's middle (infinite: it kills)
 */
public record QuasarFx(UUID caster, int casterId, long castGameTime, Vec3 mark, int star, Vec3 quasar, Vec3 position,
                       float yaw, float pitch, boolean offhand, boolean remote, boolean film, boolean secondSun,
                       boolean educational, float power, float damage) {
    public static final RimuPacket<QuasarFx> PACKET = NetApi.packet(Ton618Addition.id("quasar_jets"), QuasarFx.class,
        QuasarFx::write, QuasarFx::read);

    public QuasarFx withFilm(boolean film) {
        return new QuasarFx(this.caster, this.casterId, this.castGameTime, this.mark, this.star, this.quasar,
            this.position, this.yaw, this.pitch, this.offhand, this.remote, film, this.secondSun, this.educational, this.power,
            this.damage);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeUUID(this.caster);
        buf.writeVarInt(this.casterId);
        buf.writeLong(this.castGameTime);
        writeVec(buf, this.mark);
        buf.writeVarInt(this.star);
        writeVec(buf, this.quasar);
        writeVec(buf, this.position);
        buf.writeFloat(this.yaw);
        buf.writeFloat(this.pitch);
        buf.writeBoolean(this.offhand);
        buf.writeBoolean(this.remote);
        buf.writeBoolean(this.film);
        buf.writeBoolean(this.secondSun);
        buf.writeBoolean(this.educational);
        buf.writeFloat(this.power);
        buf.writeFloat(this.damage);
    }

    private static QuasarFx read(FriendlyByteBuf buf) {
        return new QuasarFx(buf.readUUID(), buf.readVarInt(), buf.readLong(), readVec(buf), buf.readVarInt(), readVec(buf),
            readVec(buf), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
            buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    // three doubles, not writeVec3: 1.20.1's FriendlyByteBuf has none (the ports share this file)
    private static void writeVec(FriendlyByteBuf buf, Vec3 v) {
        buf.writeDouble(v.x);
        buf.writeDouble(v.y);
        buf.writeDouble(v.z);
    }

    private static Vec3 readVec(FriendlyByteBuf buf) {
        return new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }
}
