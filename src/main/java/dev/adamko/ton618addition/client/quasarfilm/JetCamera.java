package dev.adamko.ton618addition.client.quasarfilm;

import static dev.adamko.ton618addition.client.quasarfilm.JetScript.kick;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lerp;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lin;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.ss;

import cyou.rimuru.rimulib.client.api.FilmApi;
import cyou.rimuru.rimulib.client.cinematic.CameraPose;
import cyou.rimuru.rimulib.client.cinematic.Cutscene;
import cyou.rimuru.rimulib.client.cinematic.Subject;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

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
    /**
     * Where the world shots stand, chosen once a film the first time each is needed (so the camera doesn't jump about):
     * a spot with a clear view of what the shot is about, not one inside a tree or behind a hill.
     */
    private static double sunLift = Double.NaN;
    @Nullable
    private static Vec3 sweepFrom;
    @Nullable
    private static Vec3 lobeFrom;
    @Nullable
    private static Vec3 bowlFrom;

    private JetCamera() {
    }

    /** The cutscene for {@code cast}: one shot whose camera reads the strike's clock itself. */
    static Cutscene cutscene(QuasarCast cast) {
        lastFeet = cast.castPosition();
        sunLift = Double.NaN;
        sweepFrom = null;
        lobeFrom = null;
        bowlFrom = null;
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
        if (t < 830F) {
            return sweptAlong(cast, t);
        }
        // the cut from the wide shot of the lobe to the bowl it leaves is hidden by the pop's flash
        return t < 891F ? radioLobe(cast, feet, t) : bowl(cast, feet, t);
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
        // low behind the caster on the line to the new light, creeping in; higher if trees or hills hide that light
        if (Double.isNaN(sunLift)) {
            sunLift = 0.0;
            for (double lift : new double[] {0.0, 3.0, 7.0, 12.0, 18.0, 26.0}) {
                Vec3 from = at(feet, fw, rt, -7.2, -1.7, 1.9 + lift);
                sunLift = lift;
                if (clear(from, from.add(cast.quasarDirection().scale(72.0)))) {
                    break;
                }
            }
        }
        float u = lin(680F, 745F, t);
        Vec3 pos = lifted(at(feet, fw, rt, lerp(-7.2F, -6.2F, u), -1.7, lerp(1.9F, 2.2F, u) + sunLift), 0.9);
        // toward the light; from higher up the camera also looks down a little, to keep the caster in the picture
        Vec3 dir = at(feet, fw, rt, 40.0, 0.0, 4.2 + sunLift * 0.72).subtract(pos).normalize();
        return aim(pos, dir, feet, 52F);
    }

    /** Nothing solid stands between the two points (leaves count as solid: they hide the picture). */
    private static boolean clear(Vec3 from, Vec3 to) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return true;
        }
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (net.minecraft.world.entity.Entity) null))
            .getType() == HitResult.Type.MISS;
    }

    /** {@code offset} turned by {@code degrees} round the upright. */
    private static Vec3 turned(Vec3 offset, double degrees) {
        double a = Math.toRadians(degrees);
        return new Vec3(offset.x * Math.cos(a) - offset.z * Math.sin(a), offset.y, offset.x * Math.sin(a) + offset.z * Math.cos(a));
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
        // high to the side of the jet's path, turning with the jet from the horizon to the mark, then easing in; far
        // enough back for the whole landing blast (the crater, the fireball over it, the dust racing out)
        QuasarStars.Type type = cast.star().type();
        float blast = (float) cast.blast();
        // (the craters are three times the size since 2026-11-17: about two crater radii off, 38 to 150 blocks, and
        // never up in the clouds)
        float away = Mth.clamp(blast * 1.42F, 27F, 106F);
        float u = ss(752F, 800F, t);
        float v = ss(806F, 830F, t);
        if (sweepFrom == null) {
            // the first spot from which both the mark and the jet's way in can be seen: round to either side, then
            // higher (in a forest the plain spot is inside the leaves); failing all, the highest
            Vec3 base = new Vec3(fw.x * away + rt.x * away, 0.0, fw.z * away + rt.z * away);
            Vec3 seen = mark.add(0.0, 2.5, 0.0);
            Vec3 wayIn = mark.add(cast.quasarDirection().scale(34.0)).add(0.0, 1.0, 0.0);
            Vec3 fallback = null;
            search:
            for (double high : new double[] {1.0, 1.5, 2.1, 2.8}) {
                for (double round : new double[] {0.0, 20.0, -20.0, 40.0, -40.0}) {
                    Vec3 spot = capped(mark.add(turned(base, round)).add(0.0, away * 0.3 * high, 0.0), 3.0);
                    if (clear(spot, seen)) {
                        if (clear(spot, wayIn)) {
                            sweepFrom = spot;
                            break search;
                        }
                        if (fallback == null) {
                            fallback = spot;
                        }
                    }
                }
            }
            if (sweepFrom == null) {
                sweepFrom = fallback != null ? fallback : capped(mark.add(base).add(0.0, away * 0.3 * 2.8, 0.0), 3.0);
            }
        }
        // easing in a little toward the mark after the landing
        Vec3 pos = mark.add(sweepFrom.subtract(mark).scale(lerp(1F, 0.88F, v)));
        Vec3 dir = at(mark, fw, rt, lerp(95F, 4F, u), 0.0, lerp(15F, 2F + blast * 0.12F, u)).subtract(pos).normalize();
        float jet = JetScript.jet(type);
        // the landing: one hard kick that dies away, and a second when the shock wave reaches the camera (the wave's
        // front is blast x (1 + 2 x easeOut(b / 32)) from the mark, as QuasarRender draws it)
        float reach = ((float) pos.distanceTo(mark) / blast - 1F) / 2F;
        float waveAt = reach >= 1F ? Float.MAX_VALUE : 795F + 32F * (1F - (float) Math.cbrt(1F - Math.max(0F, reach)));
        float shake = 0.004F * lin(760F, 795F, t) * jet + 0.05F * Math.min(1.3F, 0.6F + blast / 20F) * kick(t, 795F, 8F)
            + 0.02F * kick(t, waveAt, 6F) + 0.003F * lin(795F, 800F, t);
        return aim(pos, shaken(dir, t, shake), mark, 55F + 6F * kick(t, 795F, 5F));
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
        if (lobeFrom == null) {
            // the first spot that sees the middle of the dome: to either side, then higher; failing all, the highest
            Vec3 seen = mark.add(0.0, lobe * 0.5, 0.0);
            Vec3 spot = feet;
            search:
            for (double high : new double[] {1.0, 1.8, 2.8, 4.0, 5.5}) {
                for (double side : new double[] {3.5, -9.0, 14.0, -20.0, 26.0}) {
                    spot = at(mark, fw, rt, -away, side, height * high);
                    spot = capped(new Vec3(spot.x, Math.max(spot.y, feet.y + 3.2), spot.z), 2.0);
                    if (clear(spot, seen)) {
                        break search;
                    }
                }
            }
            lobeFrom = spot;
        }
        Vec3 pos = lobeFrom;
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

    // ---- 10b. The bowl: what the lobe leaves, then up to the gap in the sky ---------------------------------------------

    private static CameraPose bowl(QuasarCast cast, Vec3 feet, float t) {
        Vec3 mark = cast.mark();
        QuasarStars.Type type = cast.star().type();
        float lobe = (float) type.lobe;
        if (bowlFrom == null) {
            // over the bowl's rim on the caster's side, looking down into it; round to either side or higher if
            // something is in the way
            Vec3 back = flat(feet.subtract(mark));
            if (back == null) {
                back = toMark(cast).scale(-1.0);
            }
            Vec3 base = back.scale(lobe * 1.75);
            Vec3 seen = mark.add(0.0, 1.0, 0.0);
            Vec3 spot = feet;
            search:
            for (double high : new double[] {0.8, 1.2, 1.7, 2.4}) {
                for (double round : new double[] {0.0, 35.0, -35.0, 70.0, -70.0, 110.0, -110.0}) {
                    spot = capped(mark.add(turned(base, round)).add(0.0, lobe * high, 0.0), 3.0);
                    if (clear(spot, seen)) {
                        break search;
                    }
                }
            }
            bowlFrom = spot;
        }
        // drifting in over the bowl while the crystals grow, then up to where the star was
        Vec3 pos = mark.add(bowlFrom.subtract(mark).scale(lerp(1F, 0.86F, ss(892F, 930F, t))));
        float up = ss(JetScript.TILT_FROM, JetScript.TILT_TO, t);
        Vec3 dir = turn(mark.add(0.0, -1.0, 0.0).subtract(pos).normalize(), cast.star().direction(), up);
        float swell = Math.min(lobe / 22F, 1.5F);
        float shake = Math.min(0.045F, 0.03F * (0.4F + swell)) * kick(t, 890F, 7F);
        return aim(pos, shaken(dir, t, shake), mark, lerp(50F, 46F, up));
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

    /**
     * {@code pos} for a camera: no higher than 12 blocks under the cloud layer (a shot from above the clouds shows
     * clouds), and at least {@code clear} blocks over the ground.
     */
    private static Vec3 capped(Vec3 pos, double clear) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return pos;
        }
        float clouds = level.effects().getCloudHeight();
        Vec3 under = Float.isNaN(clouds) ? pos : new Vec3(pos.x, Math.min(pos.y, clouds - 12.0), pos.z);
        return lifted(under, clear);
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
