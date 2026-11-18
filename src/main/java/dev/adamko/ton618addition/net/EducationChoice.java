package dev.adamko.ton618addition.net;

import cyou.rimuru.rimulib.mc1201.api.NetApi;
import cyou.rimuru.rimulib.mc1201.net.RimuPacket;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.network.FriendlyByteBuf;

/** Client to server: this player's educational mode (sent once in a world and whenever it is switched). */
public record EducationChoice(boolean on) {
    public static final RimuPacket<EducationChoice> PACKET = NetApi.packet(Ton618Addition.id("education"),
        EducationChoice.class, EducationChoice::write, EducationChoice::read);

    private void write(FriendlyByteBuf buf) {
        buf.writeBoolean(this.on);
    }

    private static EducationChoice read(FriendlyByteBuf buf) {
        return new EducationChoice(buf.readBoolean());
    }
}
