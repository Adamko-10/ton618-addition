package dev.adamko.ton618addition.client.film;

import static dev.adamko.ton618addition.client.film.FilmScript.lerp;
import static dev.adamko.ton618addition.client.film.FilmScript.lin;
import static dev.adamko.ton618addition.client.film.FilmScript.ss;

import cyou.rimuru.rimulib.mc1211.client.api.FilmApi;
import cyou.rimuru.rimulib.mc1211.client.cinematic.CameraPose;
import cyou.rimuru.rimulib.mc1211.client.cinematic.Cutscene;
import cyou.rimuru.rimulib.mc1211.client.cinematic.Subject;
import dev.adamko.ton618addition.client.LastLightCast;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * <b>FABLE'S FILE.</b> The film's camera in the world, handed to RimuLib's film director as one cutscene on the
 * cast's own clock (so it starts mid-way for a late arrival and runs on through the trip back to the overworld):
 * the dark (the previz's shots 1 to 3), waiting while the space scene covers the screen, then the overworld (shots 9
 * and 10). In the dark everything is placed along the way to TON618 in the sky, so the caster and the hole are both in
 * the picture whichever way the caster faces; in the overworld along the way to the sunrise (east).
 */
final class FilmCamera {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    /** The way to the sunrise, flat, and its right. */
    private static final Vec3 EAST = new Vec3(1.0, 0.0, 0.0);
    private static final Vec3 SOUTH = new Vec3(0.0, 0.0, 1.0);
    /** Where the caster was last seen, for the frames in which they aren't in this client's level. */
    private static Vec3 lastFeet = Vec3.ZERO;

    private FilmCamera() {
    }

    /** The cutscene for {@code cast}: one shot whose camera reads the cast's clock itself. */
    static Cutscene cutscene(LastLightCast cast) {
        lastFeet = cast.castPosition();
        Subject subject = new Subject() {
            @Override
            public Vec3 feet(float partial) {
                return FilmCamera.feet(cast, partial);
            }

            @Override
            public Vec3 target() {
                return FilmCamera.feet(cast, 1F);
            }

            @Override
            public Vec3 forward() {
                return cast.inDark() ? flat(LastLightCast.holeDirection()) : EAST;
            }
        };
        Cutscene film = FilmApi.film(subject, "TA-01", "Last Light", 0xBFE0FF,
            List.of(FilmApi.cut(0, FilmScript.LAST_SHOT_END, (x, partial, k) -> pose(cast, partial))));
        // the film's own bars and words (FilmHud), no fade in (a late arrival joins mid-film), its own clock
        return film.clock(cast::tick).hardCut().fullFrame().priority(2);
    }

    /** The caster's feet now: the caster in this level, else this client's own player, else where they were last seen. */
    static Vec3 feet(LastLightCast cast, float partial) {
        Player caster = cast.caster();
        if (caster == null && !cast.inDark()) {
            // back in the overworld everyone stands at the spawn: this client's player is as good until the caster shows
            caster = Minecraft.getInstance().player;
        }
        if (caster != null) {
            lastFeet = caster.getPosition(partial);
        }
        return lastFeet;
    }

    private static Vec3 flat(Vec3 v) {
        Vec3 f = new Vec3(v.x, 0.0, v.z);
        return f.lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, -1.0) : f.normalize();
    }

    private static CameraPose pose(LastLightCast cast, float partial) {
        float t = FilmScript.toSb(cast.tick(partial));
        Vec3 feet = feet(cast, partial);
        return cast.inDark() ? dark(feet, t) : overworld(feet, Math.max(t, FilmScript.S_WORLD));
    }

    // ---- the dark: shots 1 to 3, then waiting under the space scene --------------------------------------------------------

    private static CameraPose dark(Vec3 feet, float t) {
        Vec3 hole = LastLightCast.holeDirection();
        Vec3 fw = flat(hole);
        Vec3 rt = new Vec3(-fw.z, 0.0, fw.x);
        Vec3 pos;
        Vec3 dir;
        float fov;
        if (t < 50F) {
            // 1. The Dark: behind the caster's right shoulder, low, a slow push in, looking a little under TON618
            float u = t / 50F;
            pos = at(feet, fw, rt, lerp(-5.4F, -4.4F, u), 1.9, lerp(1.45F, 1.7F, u));
            dir = at(feet, fw, rt, 40.0, 3.0, 6.5).subtract(pos).normalize();
            fov = 46F;
        } else if (t < FilmScript.S_RACE) {
            // 2. The Press: tight over the shoulder, the caster in front, TON618 beyond
            float u = ss(50F, 90F, t);
            pos = at(feet, fw, rt, lerp(-1.6F, -1.4F, u), 0.7, lerp(1.48F, 1.54F, u));
            dir = hole.scale(100.0).add(rt.scale(-9.0)).add(0.0, -13.0, 0.0).normalize();
            fov = 40F;
        } else {
            // 3. Time Lets Go: wide as the wave leaves, then up past the caster and in on TON618 until its shadow fills
            //    the frame; it waits there while the space scene covers the screen
            float m = ss(98F, 150F, t);
            pos = at(feet, fw, rt, lerp(-3.6F, 7F, m), lerp(1.3F, 0.2F, m), lerp(2.2F, 9F, m));
            Vec3 wide = at(feet, fw, rt, 40.0, 1.0, 5.0).subtract(pos).normalize();
            dir = wide.lerp(hole, ss(100F, 132F, t)).normalize();
            fov = lerp(50F, 11F, (float) Math.pow(lin(100F, 150F, t), 1.5));
        }
        float press = t >= FilmScript.S_PRESS ? (float) Math.exp(-(t - FilmScript.S_PRESS) / 7F) : 0F;
        float release = t >= FilmScript.S_RACE ? (float) Math.exp(-(t - FilmScript.S_RACE) / 8F) : 0F;
        float shake = 0.006F * press + 0.014F * release + 0.0035F * lin(112F, 150F, t);
        return aim(pos, shaken(dir, t, t < FilmScript.S_COSMOS ? shake : 0F), feet, fov);
    }

    // ---- the overworld: shots 9 and 10 ----------------------------------------------------------------------------------

    private static CameraPose overworld(Vec3 feet, float t) {
        float a = ss(FilmScript.S_CLEAR, 612F, t);
        float b = ss(612F, 660F, t);
        // close behind the caster facing the low sun, rising and pulling back, then settling behind them
        double back = lerp(lerp(3.2F, 10F, a), 4.6F, b);
        double up = lerp(lerp(2.4F, 4.6F, a), 2.7F, b);
        double side = lerp(lerp(1.2F, 3F, a), 1.1F, b);
        Vec3 pos = at(feet, EAST, SOUTH, -back, side, up);
        Vec3 dir = at(feet, EAST, SOUTH, 30.0, 0.0, lerp(lerp(2.0F, -0.3F, a), 1.6F, b)).subtract(pos).normalize();
        float shake = t >= FilmScript.S_CLEAR ? 0.012F * (float) Math.exp(-(t - FilmScript.S_CLEAR) / 9F) : 0F;
        return aim(pos, shaken(dir, t, shake), feet, 58F);
    }

    // ---- shared ----------------------------------------------------------------------------------------------------------

    /** {@code forward} blocks ahead of the anchor, {@code right} to its right, {@code up} above it. */
    private static Vec3 at(Vec3 anchor, Vec3 fw, Vec3 rt, double forward, double right, double up) {
        return anchor.add(fw.x * forward + rt.x * right, up, fw.z * forward + rt.z * right);
    }

    /** {@code dir} with a small tremble of {@code amp} radians. */
    private static Vec3 shaken(Vec3 dir, float t, float amp) {
        if (amp <= 0F) {
            return dir;
        }
        Vec3 right = dir.cross(UP).normalize();
        Vec3 up = right.cross(dir);
        double ax = amp * (Math.sin(t * 7.13) + Math.sin(t * 12.7 + 1.3)) * 0.5;
        double ay = amp * (Math.sin(t * 8.31 + 2.0) + Math.sin(t * 14.9)) * 0.5;
        return dir.add(up.scale(ax)).add(right.scale(ay)).normalize();
    }

    /**
     * A pose looking along {@code dir}. RimuLib keeps the camera out of blocks by checking the line from where it looks
     * to where it stands, so the point looked at is put about as far off as the caster: the camera then never ends up
     * behind a wall from them.
     */
    private static CameraPose aim(Vec3 pos, Vec3 dir, Vec3 feet, float fov) {
        double reach = Math.max(2.0, pos.distanceTo(feet.add(0.0, 1.0, 0.0)));
        return FilmApi.pose(pos, pos.add(dir.scale(reach)), fov);
    }
}
