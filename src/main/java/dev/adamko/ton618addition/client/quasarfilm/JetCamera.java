package dev.adamko.ton618addition.client.quasarfilm;

import static dev.adamko.ton618addition.client.quasarfilm.JetScript.kick;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lerp;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lin;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.ss;

import cyou.rimuru.rimulib.mc1211.client.api.FilmApi;
import cyou.rimuru.rimulib.mc1211.client.cinematic.CameraPose;
import cyou.rimuru.rimulib.mc1211.client.cinematic.Cutscene;
import cyou.rimuru.rimulib.mc1211.client.cinematic.Subject;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * <b>FABLE'S FILE.</b> The film's camera in the world, handed to RimuLib's film director as one cutscene on the
 * strike's own clock: the previz's shot 1 (the mark, then up to the locked star and into it), waiting while the space
 * scene covers the screen, then shots 8 (the second sun), 9 (the jet coming in) and 10 (the radio lobe, then up to the
 * gap in the sky). The world effects in those shots are not the film's: the camera only frames what
 * {@code client/quasar/QuasarRender} draws for everyone.
 *
 * <p>Shot 10 is framed for the lobe as it is built (radius 9 to 33 blocks by star type, half as big again as the
 * previz's): the camera stands back far enough from the mark for the whole dome and its pop.
 */
final class JetCamera {
    private static final Vec3 UP = new Vec3(0.0, 1.0, 0.0);
    /** Where the caster was last seen, for the frames in which they aren't in this client's level. */
    private static Vec3 lastFeet = Vec3.ZERO;

    private JetCamera() {
    }

    /** The cutscene for {@code cast}: one shot whose camera reads the strike's clock itself. */
    static Cutscene cutscene(QuasarCast cast) {
        lastFeet = cast.castPosition();
        Subject subject = new Subject() {
            @Override
            public Vec3 feet(float partial) {
                return JetCamera.feet(cast, partial);
            }

            @Override
            public Vec3 target() {
                return JetCamera.feet(cast, 1F);
            }

            @Override
            public Vec3 forward() {
                return toMark(cast);
            }
        };
        Cutscene film = FilmApi.film(subject, "TA-02", "Quasar Jets", cast.star().type().color,
            List.of(FilmApi.cut(0, JetScript.LAST_SHOT_END, (x, partial, k) -> pose(cast, partial))));
        // the film's own bars and words (JetHud), no fade in, its own clock
        return film.clock(cast::tick).hardCut().fullFrame().priority(2);
    }

    /** The caster's feet now: the caster in this level, else where they were last seen. */
    static Vec3 feet(QuasarCast cast, float partial) {
        Player caster = cast.caster();
        if (caster != null) {
            lastFeet = caster.getPosition(partial);
        }
        return lastFeet;
    }

    /** The flat way from where the caster stood to the mark (the way they faced, if the mark is at their feet). */
    private static Vec3 toMark(QuasarCast cast) {
        Vec3 way = flat(cast.mark().subtract(cast.castPosition()));
        if (way != null) {
            return way;
        }
        double yaw = Math.toRadians(cast.castYaw());
        return new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
    }

    private static Vec3 flat(Vec3 v) {
        Vec3 f = new Vec3(v.x, 0.0, v.z);
        return f.lengthSqr() < 1.0 ? null : f.normalize();
    }

    private static Vec3 right(Vec3 forward) {
        return new Vec3(-forward.z, 0.0, forward.x);
    }

    private static CameraPose pose(QuasarCast cast, float partial) {
        float t = cast.tick(partial);
        Vec3 feet = feet(cast, partial);
        if (t < 640F) {
            // shot 1; its last frame is held while the space scene covers the screen
            return lockOn(cast, feet, Math.min(t, JetScript.SPACE_FROM));
        }
        if (t < 745F) {
            // shot 8 (set up a little early, under the space scene and the flood's white)
            return secondSun(cast, feet, Math.max(t, 680F));
        }
        return t < 830F ? sweptAlong(cast, t) : radioLobe(cast, feet, t);
    }

    // ---- 1. Lock-on -----------------------------------------------------------------------------------------------------

    private static CameraPose lockOn(QuasarCast cast, Vec3 feet, float t) {
        Vec3 fw = toMark(cast);
        Vec3 rt = right(fw);
        // behind the caster's right shoulder on the mark, then swinging up to the locked star and moving in
        float u = ss(18F, 46F, t);
        Vec3 pos = at(feet, fw, rt, lerp(-4.4F, -2.6F, u), lerp(1.7F, 1.3F, u), lerp(2.7F, 1.9F, u));
        Vec3 dir = turn(cast.mark().subtract(pos).normalize(), cast.star().direction(), u);
        // the zoom into the star (the flash cut covers its end)
        float fov = lerp(50F, 5F, (float) Math.pow(lin(52F, 80F, t), 1.6));
        return aim(pos, shaken(dir, t, 0.004F * kick(t, 0F, 6F)), feet, fov);
    }

    // ---- 8. Second Sun ---------------------------------------------------------------------------------------------------

    private static CameraPose secondSun(QuasarCast cast, Vec3 feet, float t) {
        Vec3 fw = toQuasar(cast);
        Vec3 rt = right(fw);
        // low behind the caster on the line to the new light, creeping in
        float u = lin(680F, 745F, t);
        Vec3 pos = lifted(at(feet, fw, rt, lerp(-7.2F, -6.2F, u), -1.7, lerp(1.9F, 2.2F, u)), 0.9);
        Vec3 dir = at(feet, fw, rt, 40.0, 0.0, 4.2).subtract(pos).normalize();
        return aim(pos, dir, feet, 52F);
    }

    /** The flat way to the second sun. */
    private static Vec3 toQuasar(QuasarCast cast) {
        Vec3 way = flat(cast.quasarDirection().scale(100.0));
        return way != null ? way : new Vec3(0.0, 0.0, 1.0);
    }

    // ---- 9. Swept Along --------------------------------------------------------------------------------------------------

    private static CameraPose sweptAlong(QuasarCast cast, float t) {
        Vec3 mark = cast.mark();
        Vec3 fw = toQuasar(cast);
        Vec3 rt = right(fw);
        // high to the side of the jet's path, turning with the jet from the horizon to the mark, then easing in
        float u = ss(752F, 800F, t);
        float v = ss(800F, 830F, t);
        Vec3 pos = lifted(at(mark, fw, rt, lerp(27F, 22F, v), lerp(27F, 23F, v), lerp(8F, 6.5F, v)), 3.0);
        Vec3 dir = at(mark, fw, rt, lerp(95F, 6F, u), 0.0, lerp(15F, 2.5F, u)).subtract(pos).normalize();
        float jet = JetScript.jet(cast.star().type());
        return aim(pos, shaken(dir, t, 0.004F * lin(760F, 800F, t) * jet), mark, 55F);
    }

    // ---- 10. Radio Lobe --------------------------------------------------------------------------------------------------

    /** How far from the mark shot 10's camera stands: far enough for the whole dome and its pop, and behind the caster. */
    private static double lobeDistance(QuasarCast cast, Vec3 feet) {
        Vec3 mark = cast.mark();
        double casterAway = Math.sqrt((feet.x - mark.x) * (feet.x - mark.x) + (feet.z - mark.z) * (feet.z - mark.z));
        return Math.max(casterAway + 8.0, cast.star().type().lobe * 2.3);
    }

    private static CameraPose radioLobe(QuasarCast cast, Vec3 feet, float t) {
        Vec3 mark = cast.mark();
        QuasarStars.Type type = cast.star().type();
        float lobe = (float) type.lobe;
        // wide from behind the caster, on the far side of them from the mark
        Vec3 back = flat(feet.subtract(mark));
        if (back == null) {
            back = toMark(cast).scale(-1.0);
        }
        Vec3 fw = back.scale(-1.0);
        Vec3 rt = right(fw);
        double away = lobeDistance(cast, feet);
        double height = Math.max(6.0, away * 0.15);
        Vec3 pos = at(mark, fw, rt, -away, 3.5, height);
        pos = lifted(new Vec3(pos.x, Math.max(pos.y, feet.y + 3.2), pos.z), 2.0);
        // 922 on in the previz: up to the sky, to the gap where the star was (a little later here, for the last label)
        float up = ss(JetScript.TILT_FROM, JetScript.TILT_TO, t);
        Vec3 dir = turn(mark.add(0.0, lobe * 0.3, 0.0).subtract(pos).normalize(), cast.star().direction(), up);
        // wide enough for the dome; narrower when the caster marked a spot far off, so the dome isn't lost in the picture
        float wide = Mth.clamp((float) Math.toDegrees(2.0 * Math.atan(1.45 * lobe / away)), 34F, 58F);
        float fov = lerp(wide, Math.min(wide, 46F), up);
        float jet = JetScript.jet(type);
        float swell = Math.min(lobe / 22F, 1.5F);
        float shake = 0.004F * (1F - lin(884F, 892F, t)) * jet + 0.006F * ss(835F, 888F, t) * (1F - lin(888F, 892F, t)) * swell
            + Math.min(0.045F, 0.03F * (0.4F + swell)) * kick(t, 890F, 7F);
        return aim(pos, shaken(dir, t, shake), feet, fov);
    }

    // ---- shared ----------------------------------------------------------------------------------------------------------

    /** {@code forward} blocks ahead of the anchor, {@code right} to its right, {@code up} above it. */
    private static Vec3 at(Vec3 anchor, Vec3 fw, Vec3 rt, double forward, double right, double up) {
        return anchor.add(fw.x * forward + rt.x * right, up, fw.z * forward + rt.z * right);
    }

    /** {@code pos}, raised if the land there is higher: at least {@code clear} blocks over the ground. */
    private static Vec3 lifted(Vec3 pos, double clear) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return pos;
        }
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(pos.x), Mth.floor(pos.z));
        // an unloaded column says "the bottom of the world": then the shot's own height stands
        return ground > level.getMinBuildHeight() && pos.y < ground + clear ? new Vec3(pos.x, ground + clear, pos.z) : pos;
    }

    /** From looking along {@code from} to looking along {@code to}, {@code u} of the way round (both unit vectors). */
    private static Vec3 turn(Vec3 from, Vec3 to, float u) {
        if (u <= 0F) {
            return from;
        }
        if (u >= 1F) {
            return to;
        }
        double cos = Mth.clamp(from.dot(to), -1.0, 1.0);
        double angle = Math.acos(cos);
        if (angle < 1.0E-3) {
            return to;
        }
        Vec3 axis = from.cross(to);
        if (axis.lengthSqr() < 1.0E-8) {
            // straight behind: go over the top
            axis = from.cross(UP);
            if (axis.lengthSqr() < 1.0E-8) {
                axis = new Vec3(1.0, 0.0, 0.0);
            }
        }
        axis = axis.normalize();
        double a = angle * u;
        // Rodrigues: from, turned by a round the axis (which is square to it)
        return from.scale(Math.cos(a)).add(axis.cross(from).scale(Math.sin(a))).normalize();
    }

    /** {@code dir} with a small tremble of {@code amp} radians. */
    private static Vec3 shaken(Vec3 dir, float t, float amp) {
        if (amp <= 0F) {
            return dir;
        }
        Vec3 side = dir.cross(UP);
        if (side.lengthSqr() < 1.0E-6) {
            return dir;
        }
        side = side.normalize();
        Vec3 up = side.cross(dir);
        double ax = amp * (Math.sin(t * 7.13) + Math.sin(t * 12.7 + 1.3)) * 0.5;
        double ay = amp * (Math.sin(t * 8.31 + 2.0) + Math.sin(t * 14.9)) * 0.5;
        return dir.add(up.scale(ax)).add(side.scale(ay)).normalize();
    }

    /**
     * A pose looking along {@code dir}. RimuLib keeps the camera out of blocks by checking the line from where it looks
     * to where it stands, so the point looked at is put about as far off as {@code anchor}: the camera then never ends
     * up behind a wall from it.
     */
    private static CameraPose aim(Vec3 pos, Vec3 dir, Vec3 anchor, float fov) {
        double reach = Mth.clamp(pos.distanceTo(anchor.add(0.0, 1.0, 0.0)), 2.0, 24.0);
        return FilmApi.pose(pos, pos.add(dir.scale(reach)), fov);
    }
}
