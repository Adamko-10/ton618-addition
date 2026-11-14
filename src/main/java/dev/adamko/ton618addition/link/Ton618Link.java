package dev.adamko.ton618addition.link;

import cyou.rimuru.rimulib.api.NetApi;
import cyou.rimuru.rimulib.api.RimulibApi;
import cyou.rimuru.rimulib.api.SkillApi;
import cyou.rimuru.rimulib.skill.Skill;
import cyou.rimuru.rimulib.skill.SkillSet;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.lastlight.LastLight;
import dev.adamko.ton618addition.lastlight.LastLightInfo;
import dev.adamko.ton618addition.lastlight.LastLightSkill;
import dev.adamko.ton618addition.net.LastLightEnd;
import dev.adamko.ton618addition.net.LastLightFx;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The one place the addon touches TON 618 (and The Shooting Star). TON 618's classes are obfuscated with new names
 * every release, so nothing here names them: only RimuLib's public API ({@code SkillApi}, {@code NetApi}), TON 618's
 * vanilla command storage and its own {@code /ton618} commands, and facts read from its 1.1.0 jar
 * ({@code docs/TON618-NOTES.md}). When TON 618 updates, check those facts here first.
 */
public final class Ton618Link {
    public static final String TON618 = "ton618";
    public static final String SHOOTING_STAR = "shooting_star_demo";

    /** TON 618's dark, where everyone goes when it devours the world. */
    public static final ResourceKey<Level> DARK = ResourceKey.create(Registries.DIMENSION,
        Identifier.fromNamespaceAndPath(TON618, "devoured"));
    /** Where TON 618 keeps whether the world is devoured ({@code devoured}, a boolean, and {@code at}, the game time). */
    private static final Identifier STORAGE = Identifier.fromNamespaceAndPath(TON618, "ton618");

    /**
     * TON618 in the dark's sky, as TON 618 1.1.0 draws it: always in this direction from the camera (like the sun),
     * {@link #HOLE_DISTANCE} away, its black shadow {@link #HOLE_SHADOW_RADIUS} across at that distance (about 3.3
     * degrees), its disk from 2.9 to 11 shadow radii. Read from its jar; for the film to line up with it.
     */
    public static final Vec3 HOLE_DIRECTION = new Vec3(0.34, 0.2, -1.0).normalize();
    public static final double HOLE_DISTANCE = 1000.0;
    public static final double HOLE_SHADOW_RADIUS = 1000.0 * Math.tan(Math.toRadians(8.5)) / 2.6;

    /** Last Light on TON 618's own item. */
    public static final LastLightSkill ON_TON618 = new LastLightSkill(LastLightInfo.KEY_ON_TON618, false);
    /** Last Light on The Shooting Star's Stellar Remote (when The Shooting Star is installed). */
    public static final LastLightSkill ON_REMOTE = new LastLightSkill(LastLightInfo.KEY_ON_REMOTE, true);

    private static boolean installed;

    private Ton618Link() {
    }

    /**
     * Joins RimuLib, declares our packet, and adds Last Light to TON 618's menu and (if The Shooting Star registers its
     * remote) the Stellar Remote's, through {@code SkillApi.whenRegistered} + {@code extend}: the official way, as the SA
     * Series does. Safe to call more than once.
     */
    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        RimulibApi.connect(Ton618Addition.MOD_ID, Ton618Addition.class, "1.2.3");
        NetApi.toClient(LastLightFx.TYPE, LastLightFx.CODEC);
        NetApi.toClient(LastLightEnd.TYPE, LastLightEnd.CODEC);
        SkillApi.whenRegistered(TON618, set -> {
            // TON 618's one set, ton618:ton_618, held by its item
            if (set.hidden || !"ton_618".equals(set.id.getPath()) || SkillApi.setOf(ON_TON618) != null) {
                return;
            }
            SkillApi.extend(set, new LastLightSkill[]{ON_TON618}, (player, skill, tuning) -> LastLight.cast(player, skill));
            logJoined(set, ON_TON618, "TON 618's menu");
        });
        SkillApi.whenRegistered(SHOOTING_STAR, set -> {
            // the Stellar Remote is The Shooting Star's set that holds its own skills (an enum of them)
            if (set.hidden || set.skills.stream().noneMatch(skill -> skill instanceof Enum<?>) || SkillApi.setOf(ON_REMOTE) != null) {
                return;
            }
            SkillApi.extend(set, new LastLightSkill[]{ON_REMOTE}, (player, skill, tuning) -> LastLight.cast(player, skill));
            logJoined(set, ON_REMOTE, "the Stellar Remote");
        });
    }

    private static void logJoined(SkillSet set, Skill skill, String where) {
        Ton618Addition.LOG.info("{} joined {} ({}) as skill #{} (key slot {}).", skill.title(), where, set.id,
            set.skills.indexOf(skill) + 1, SkillApi.index(skill));
    }

    /** TON 618 has devoured the world (its own flag; {@code /ton618 status} reads the same). */
    public static boolean devoured(MinecraftServer server) {
        return server.getCommandStorage().get(STORAGE).getBooleanOr("devoured", false);
    }

    public static boolean inDark(ServerPlayer player) {
        return player.level().dimension() == DARK;
    }

    /**
     * Brings the world back with TON 618's own {@code /ton618 restore}, run as the server: it clears its flag (and the
     * copy it keeps in memory) and sends everyone in the dark to the overworld's spawn. True when the world is back.
     */
    public static boolean restore(MinecraftServer server) {
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
        try {
            server.getCommands().performPrefixedCommand(source, "ton618 restore");
        } catch (RuntimeException failed) {
            Ton618Addition.LOG.error("/ton618 restore failed", failed);
        }
        return !devoured(server);
    }
}
