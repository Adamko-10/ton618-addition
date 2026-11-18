package dev.adamko.ton618addition.client.quasar;

import dev.adamko.ton618addition.ReadingTime;
import dev.adamko.ton618addition.client.Ton618AdditionClientConfig;
import dev.adamko.ton618addition.net.QuasarFx;
import dev.adamko.ton618addition.quasar.QuasarReading;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * One TA-02 strike as this client sees it: everything the film may read (CLAUDE.md, "For Fable: TA-02's film"). Owned
 * by the Opus session; Fable asks for more in {@code reviews/fable/PROPOSALS.md}. The clock counts game ticks from the
 * second press ({@code QuasarClock}).
 */
public final class QuasarCast {
    private final QuasarFx fx;
    private long counted;

    QuasarCast(QuasarFx fx) {
        this.fx = fx;
        Minecraft mc = Minecraft.getInstance();
        this.counted = mc.level == null ? 0L : Math.max(0L, mc.level.getGameTime() - fx.castGameTime());
    }

    void clientTick() {
        this.counted++;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            long fromServer = mc.level.getGameTime() - this.fx.castGameTime();
            if (Math.abs(fromServer - this.counted) <= 40L) {
                this.counted = fromServer;
            }
        }
    }

    /**
     * Story ticks of {@code QuasarClock}, with the frame's partial tick: game ticks since the second press, slowed in the
     * reading windows ({@code QuasarReading}) when the caster had educational mode on, as on the server.
     */
    public float tick(float partialTick) {
        return story(this.counted + partialTick);
    }

    /** Whole story ticks. */
    public long tick() {
        return (long) story(this.counted);
    }

    /** Game ticks since the second press, unslowed (for anything that must follow the real clock, e.g. a soundtrack). */
    public float realTick(float partialTick) {
        return this.counted + partialTick;
    }

    private float story(float real) {
        return this.fx.educational() ? ReadingTime.story(QuasarReading.WINDOWS, real) : real;
    }

    public QuasarFx fx() {
        return this.fx;
    }

    /** This client gets the film (the caster and players within ~128 blocks); the rest only see the world effects. */
    public boolean film() {
        return this.fx.film();
    }

    /** The launched star and its type (what the type changes: {@link QuasarStars.Type}). */
    /** The landing blast's radius: the star's, times the Power slider it was cast with. */
    public double blast() {
        return star().type().blast * this.fx.power();
    }

    public QuasarStars.Star star() {
        return QuasarStars.ALL.get(Math.floorMod(this.fx.star(), QuasarStars.ALL.size()));
    }

    public int starIndex() {
        return this.fx.star();
    }

    /** Where the jet lands (the ground mark, the radio lobe's middle). */
    public Vec3 mark() {
        return this.fx.mark();
    }

    /** The second sun's direction in the sky (unit vector); the jet comes in along it. */
    public Vec3 quasarDirection() {
        return this.fx.quasar();
    }

    public UUID casterUuid() {
        return this.fx.caster();
    }

    @Nullable
    public Player caster() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.getPlayerByUUID(this.fx.caster());
    }

    public boolean isCaster() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(this.fx.caster());
    }

    public Vec3 castPosition() {
        return this.fx.position();
    }

    public float castYaw() {
        return this.fx.yaw();
    }

    public float castPitch() {
        return this.fx.pitch();
    }

    public boolean offhand() {
        return this.fx.offhand();
    }

    public boolean viaStellarRemote() {
        return this.fx.remote();
    }

    /** The server allows the second sun. */
    public boolean secondSun() {
        return this.fx.secondSun();
    }

    public boolean educational() {
        return Ton618AdditionClientConfig.educational();
    }

    /** This strike runs its reading windows slower (its caster had educational mode on; the same for every viewer). */
    public boolean slowed() {
        return this.fx.educational();
    }
}
