package dev.adamko.ton618addition.client;

import dev.adamko.ton618addition.ReadingTime;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import dev.adamko.ton618addition.lastlight.LastLightReading;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.LastLightFx;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * One Last Light cast as the film sees it: everything the film may read (CLAUDE.md, "For Fable: the film"). Owned by
 * the Opus session; Fable asks for more in {@code reviews/fable/PROPOSALS.md}.
 *
 * <p>The clock ({@link #tick}) counts game ticks from the cast. It follows the server's game time, which the dark and
 * the overworld share, and counts on by itself while there is no level (the trip back to the overworld at
 * {@link LastLightClock#RESTORE}), so it never stops or jumps through the dimension change.
 */
public final class LastLightCast {
    private final LastLightFx fx;
    private long counted;

    LastLightCast(LastLightFx fx) {
        this.fx = fx;
        Minecraft mc = Minecraft.getInstance();
        this.counted = mc.level == null ? 0L : Math.max(0L, mc.level.getGameTime() - fx.castGameTime());
    }

    /** Once per client tick (not while the game is paused). */
    void clientTick() {
        this.counted++;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            long fromServer = mc.level.getGameTime() - this.fx.castGameTime();
            // follow the server's clock when it's sane (a new level's time is 0 until the server sends it)
            if (Math.abs(fromServer - this.counted) <= 40L) {
                this.counted = fromServer;
            }
        }
    }

    /**
     * Story ticks of {@link LastLightClock}, with the frame's partial tick: game ticks since the cast, slowed in the
     * reading windows ({@code LastLightReading}) when the caster had educational mode on, as on the server.
     */
    public float tick(float partialTick) {
        return story(this.counted + partialTick);
    }

    /** Whole story ticks. */
    public long tick() {
        return (long) story(this.counted);
    }

    /** Game ticks since the cast, unslowed (for anything that must follow the real clock, e.g. a soundtrack). */
    public float realTick(float partialTick) {
        return this.counted + partialTick;
    }

    private float story(float real) {
        return this.fx.educational() ? ReadingTime.story(LastLightReading.WINDOWS, real) : real;
    }

    /** The packet as it came (the raw data, for anything not wrapped below). */
    public LastLightFx fx() {
        return this.fx;
    }

    public UUID casterUuid() {
        return this.fx.caster();
    }

    /** The caster now, in this client's level (null if they're not in it, e.g. while changing dimension). */
    @Nullable
    public Player caster() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? null : mc.level.getPlayerByUUID(this.fx.caster());
    }

    /** This client is the caster. */
    public boolean isCaster() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && mc.player.getUUID().equals(this.fx.caster());
    }

    /** Where the caster stood when they cast (in the dark), and where they looked. */
    public Vec3 castPosition() {
        return this.fx.position();
    }

    public float castYaw() {
        return this.fx.yaw();
    }

    public float castPitch() {
        return this.fx.pitch();
    }

    /** The item that fired it is in the caster's off hand. */
    public boolean offhand() {
        return this.fx.offhand();
    }

    /** It was fired from The Shooting Star's Stellar Remote (otherwise TON 618's own item). */
    public boolean viaStellarRemote() {
        return this.fx.remote();
    }

    /** This client is in TON 618's dark (until the world comes back at RESTORE). */
    public boolean inDark() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level != null && mc.level.dimension() == Ton618Link.DARK;
    }

    /** TON618 in the dark's sky: its direction from the camera (unit vector), as TON 618 draws it. */
    public static Vec3 holeDirection() {
        return Ton618Link.HOLE_DIRECTION;
    }

    /** How far from the camera TON 618 draws it (blocks; it moves with the camera, like the sun). */
    public static double holeDistance() {
        return Ton618Link.HOLE_DISTANCE;
    }

    /** The radius of its black shadow at {@link #holeDistance()} (blocks); its disk runs from 2.9 to 11 of these. */
    public static double holeShadowRadius() {
        return Ton618Link.HOLE_SHADOW_RADIUS;
    }

    /** Educational mode is on (the player's setting, off by default). */
    public boolean educational() {
        return Ton618AdditionClientConfig.educational();
    }

    /** This strike runs its reading windows slower (its caster had educational mode on; the same for every viewer). */
    public boolean slowed() {
        return this.fx.educational();
    }
}
