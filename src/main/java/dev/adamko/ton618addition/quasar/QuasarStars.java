package dev.adamko.ton618addition.quasar;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.phys.Vec3;

/**
 * The 8 named real stars TA-02 can launch (Adam, 2026-11-15: "8: two per type"), drawn by us in the overworld's night
 * sky at fixed directions, and what each type does to the strike. Numbers from Fable's previz table
 * ({@code previz/quasar-jets/NOTES.md}); the radio lobe, bowl and crystals are 1.5 times the previz's (Adam on v1: "make
 * the explosion thing bigger"). Every number is a first guess to tune in the game.
 */
public final class QuasarStars {
    /** What the star's type changes. Lengths in blocks. */
    public enum Type {
        RED_DWARF("Red dwarf", 0xFF7A4D, 0.7f, 1.5, 9, 3, 12, 1f, 4f, 0.8f, 20, 10, 14f, 60),
        SUN_LIKE("Sun-like", 0xFFE2A0, 1.0f, 2.6, 15, 4, 20, 2f, 6f, 0.9f, 30, 15, 22f, 90),
        BLUE_GIANT("Blue giant", 0xA8C8FF, 1.6f, 4.2, 24, 6, 32, 3f, 9f, 1f, 45, 22, 32f, 130),
        RED_SUPERGIANT("Red supergiant", 0xFF9450, 2.3f, 6.1, 33, 8, 44, 4f, 12f, 1f, 60, 30, 45f, 180);

        public final String label;
        /** Its colour: the star, the jet's sheath, the lobe, the crystals, the reticle. */
        public final int color;
        /** Its size in our sky and in the film (Sun-like = 1). */
        public final float size;
        /** Things within this distance of the jet's line are picked up. */
        public final double swept;
        /** The radio lobe's radius (the bowl's too). */
        public final double lobe;
        /** The bowl's depth at its middle. */
        public final int bowl;
        /** Frozen-light crystals left in the bowl. */
        public final int crystals;
        /** Damage every half second inside the swelling lobe, and at its pop. */
        public final float lobeDamage;
        public final float popDamage;
        /** How bright the second sun's day is (1 = noon). */
        public final float day;
        /**
         * The jet's landing blast at {@code QuasarClock.LANDS} (Adam, 2026-11-16: "a big impact and a proper explosion"):
         * the crater's radius and its depth at the middle, the hit at the middle (less further out, to 1.3 x the radius),
         * and how many blocks are thrown out as debris. Around the crater, out to {@code QuasarJets.RING} x the radius,
         * the blast flattens trees and plants and scorches the ground. Tripled on 2026-11-17 (Adam: "make it actually
         * destructive").
         */
        public final double blast;
        public final int blastDepth;
        public final float blastDamage;
        public final int debris;

        Type(String label, int color, float size, double swept, double lobe, int bowl, int crystals, float lobeDamage,
             float popDamage, float day, double blast, int blastDepth, float blastDamage, int debris) {
            this.label = label;
            this.color = color;
            this.size = size;
            this.swept = swept;
            this.lobe = lobe;
            this.bowl = bowl;
            this.crystals = crystals;
            this.lobeDamage = lobeDamage;
            this.popDamage = popDamage;
            this.day = day;
            this.blast = blast;
            this.blastDepth = blastDepth;
            this.blastDamage = blastDamage;
            this.debris = debris;
        }
    }

    /**
     * One named star.
     *
     * @param direction unit vector from the viewer (fixed in the world: it doesn't wheel with the vanilla sky)
     */
    public record Star(String name, Type type, String distance, String size, String mass, Vec3 direction) {
    }

    /** In this order everywhere (the packets send the index). */
    public static final List<Star> ALL = List.of(
        star("Barnard's Star", Type.RED_DWARF, "6 light years", "0.19 Suns wide", "0.16 Suns", 20, 40),
        star("Wolf 359", Type.RED_DWARF, "7.9 light years", "0.14 Suns wide", "0.11 Suns", 200, 55),
        star("Alpha Centauri A", Type.SUN_LIKE, "4.4 light years", "1.2 Suns wide", "1.1 Suns", 110, 30),
        star("Tau Ceti", Type.SUN_LIKE, "11.9 light years", "0.79 Suns wide", "0.78 Suns", 290, 35),
        star("Rigel", Type.BLUE_GIANT, "860 light years", "79 Suns wide", "21 Suns", 70, 60),
        star("Spica", Type.BLUE_GIANT, "250 light years", "7.5 Suns wide", "11 Suns", 250, 25),
        star("Betelgeuse", Type.RED_SUPERGIANT, "550 light years", "760 Suns wide", "17 Suns", 160, 45),
        star("Antares", Type.RED_SUPERGIANT, "550 light years", "680 Suns wide", "13 Suns", 330, 65));

    /** How close (degrees) the look must be to a star to pick it. */
    public static final double PICK_DEGREES = 5.0;

    private QuasarStars() {
    }

    /** A star at {@code azimuth} degrees from north, clockwise, {@code elevation} degrees over the horizon. */
    private static Star star(String name, Type type, String distance, String size, String mass, double azimuth,
                             double elevation) {
        double az = Math.toRadians(azimuth);
        double el = Math.toRadians(elevation);
        // north is -Z, east is +X
        Vec3 direction = new Vec3(Math.sin(az) * Math.cos(el), Math.sin(el), -Math.cos(az) * Math.cos(el));
        return new Star(name, type, distance, size, mass, direction.normalize());
    }

    @Nullable
    public static Star get(int index) {
        return index >= 0 && index < ALL.size() ? ALL.get(index) : null;
    }

    /** The star nearest the look (a unit vector) within {@code degrees}, or -1. */
    public static int looking(Vec3 look, double degrees) {
        double best = Math.cos(Math.toRadians(degrees));
        int found = -1;
        for (int i = 0; i < ALL.size(); i++) {
            double dot = ALL.get(i).direction().dot(look);
            if (dot > best) {
                best = dot;
                found = i;
            }
        }
        return found;
    }

    /**
     * Where the second sun (TON618 as a quasar) sits: 65 degrees to the right of where the caster faced and 8 degrees
     * over the horizon (previz shot 8), so the jet comes in low from the side.
     */
    public static Vec3 quasarDirection(float casterYaw) {
        double yaw = Math.toRadians(casterYaw + 65.0);
        double el = Math.toRadians(8.0);
        // Minecraft's yaw: 0 faces south (+Z), 90 faces west (-X)
        return new Vec3(-Math.sin(yaw) * Math.cos(el), Math.sin(el), Math.cos(yaw) * Math.cos(el)).normalize();
    }
}
