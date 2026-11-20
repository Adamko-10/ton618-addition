package dev.adamko.ton618addition.clienttest;

import cyou.rimuru.rimulib.mc1211.client.magic.ClientSkills;
import cyou.rimuru.rimulib.mc1211.client.magic.WhiteSpace;
import dev.adamko.ton618addition.client.LastLightCast;
import dev.adamko.ton618addition.client.Ton618AdditionClient;
import dev.adamko.ton618addition.client.Ton618AdditionClientConfig;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import dev.adamko.ton618addition.client.quasar.QuasarClient;
import dev.adamko.ton618addition.lastlight.LastLight;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import cyou.rimuru.rimulib.mc1211.skill.Skill;
import cyou.rimuru.rimulib.mc1211.skill.SkillSet;
import cyou.rimuru.rimulib.mc1211.skill.Tuning;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.quasar.QuasarJets;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;

/**
 * The test harness's state machine (client thread; world actions are handed to the integrated server's thread).
 *
 * <p>Parameters ({@code -Dton618test.<name>=...}, forwarded by the {@code runClientTest} Gradle run):
 * <ul>
 *   <li>{@code skill}: {@code quasar} (default), {@code lastlight}, or {@code ss21} (SS-21 Quasar's own strike, shots
 *       by ticks from its cast);</li>
 *   <li>{@code star}: TA-02's star by name or index (default Antares);</li>
 *   <li>{@code distance}: how far from the caster the jet lands, blocks east (default 110: the biggest crater needs 66);</li>
 *   <li>{@code perch}: how high above the trees the player stands (default 35, so the aim clears the canopy);</li>
 *   <li>{@code shots}: story ticks to screenshot, comma separated, or {@code from-to/step} (default every 20 to the end);</li>
 *   <li>{@code edu}: educational mode on for this run (default false);</li>
 *   <li>{@code power}: TA-02's Power slider (default 1);</li>
 *   <li>{@code world}: {@code normal} (default, fixed seed) or {@code flat};</li>
 *   <li>{@code seed}: the world seed (default 618);</li>
 *   <li>{@code menu}: {@code true} = no strike: open the H menu (holding TON 618) in the overworld, then again in TON
 *       618's dark, a screenshot of each ({@code menu_overworld.png}, {@code menu_dark.png}; a player's report,
 *       2026-11-20: "u cant open the H menu when ton618 devoured the world"), then TA-02 tried in the dark
 *       ({@code deny_dark.png}: its refusal on the action bar);</li>
 *   <li>{@code nohudhook}: {@code true} = TON 618 Addition's hook after the HUD call does nothing (as if another mod
 *       had changed that call), to test the end-of-frame fallback in {@code HudRescue}.</li>
 * </ul>
 * Screenshots go to {@code run/clienttest/screenshots/}: {@code sky.png} (TA-02's named stars before the cast),
 * {@code locked.png} (after press 1), then {@code <skill>_<story tick>.png}. The game closes after the last one.
 */
final class Harness {
    private static final String LEVEL = "ton618test";
    static final String SKILL = prop("skill", "quasar");
    static final String STAR = prop("star", "Antares");
    static final int DISTANCE = Integer.parseInt(prop("distance", "110"));
    static final int PERCH = Integer.parseInt(prop("perch", "35"));
    static final boolean EDU = Boolean.parseBoolean(prop("edu", "false"));
    /** TA-02's Power slider for the cast (Damage stays ∞, as the menu starts it). */
    static final float POWER = Float.parseFloat(prop("power", "1"));
    static final String WORLD = prop("world", "normal");
    static final long SEED = Long.parseLong(prop("seed", "618"));
    static final boolean MENU = Boolean.parseBoolean(prop("menu", "false"));
    static final float[] SHOTS = shots(prop("shots", ""), "lastlight".equals(SKILL) ? LastLightClock.END + 20 : 1000);

    private enum Step { WORLD, SETUP, SETTLE, SKY, LOCK, LOCKED, CAST, FILM, MENU_SHOT, MENU_TO_DARK, MENU_OPEN, MENU_DENY, MENU_DENY_SHOT, MENU_END, SS21,
        DONE }

    private static boolean menuInDark;
    private static long ss21At;

    private static Step step = Step.WORLD;
    private static long waitUntil;
    private static int nextShot;
    private static String pendingShot;
    private static boolean started;
    private static long startedAt;

    private Harness() {
    }

    static String describe() {
        return "skill=" + SKILL + " star=" + STAR + " distance=" + DISTANCE + " edu=" + EDU + " world=" + WORLD + " seed="
            + SEED + " shots=" + SHOTS.length;
    }

    static void log(String message) {
        System.out.println("[TON618TEST] " + message);
    }

    // ---- the slow part: the world, the setup, the presses ------------------------------------------------------------

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.tutorialStep = TutorialSteps.NONE;
        mc.options.pauseOnLostFocus = false;
        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(null);
        }
        if (step == Step.WORLD) {
            if (mc.level == null && mc.getOverlay() == null && mc.screen != null) {
                step = Step.SETUP;
                createWorld(mc);
            }
            return;
        }
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.level == null || mc.player == null || server == null || mc.screen != null) {
            return;
        }
        long now = mc.level.getGameTime();
        if (now < waitUntil) {
            return;
        }
        switch (step) {
            case SETUP -> {
                Ton618AdditionClientConfig.setEducational(EDU);
                onServer(server, Harness::setUp);
                step = Step.SETTLE;
                waitUntil = now + 120;
            }
            case SETTLE -> {
                mc.player.getInventory().selected = 0;
                if (MENU) {
                    openMenu(mc);
                    step = Step.MENU_SHOT;
                    waitUntil = now + 60;
                } else if ("ss21".equals(SKILL)) {
                    // SS-21 Quasar's own strike (Aether21's addon), fired as the Stellar Remote would: does it run?
                    onServer(server, Harness::castSs21);
                    ss21At = now;
                    step = Step.SS21;
                } else if (!"lastlight".equals(SKILL)) {
                    ClientSkills.select(Ton618Link.QUASAR_ON_TON618);
                    onServer(server, player -> look(player, star().direction()));
                    step = Step.SKY;
                } else {
                    onServer(server, Harness::castLastLight);
                    step = Step.FILM;
                }
                waitUntil = now + 20;
            }
            case SKY -> {
                pendingShot = "sky";
                step = Step.LOCK;
                waitUntil = now + 5;
            }
            case LOCK -> {
                onServer(server, player -> {
                    look(player, star().direction());
                    QuasarJets.cast(player, Ton618Link.QUASAR_ON_TON618, new Tuning(POWER, Tuning.INFINITE, 1F));
                });
                step = Step.LOCKED;
                waitUntil = now + 10;
            }
            case LOCKED -> {
                pendingShot = "locked";
                step = Step.CAST;
                waitUntil = now + 5;
            }
            case CAST -> {
                onServer(server, Harness::castQuasar);
                step = Step.FILM;
            }
            case MENU_SHOT -> {
                log("menu " + (menuInDark ? "in the dark" : "in the overworld") + ": state=" + WhiteSpace.state()
                    + " active=" + WhiteSpace.active() + " dimension=" + mc.level.dimension().location());
                pendingShot = menuInDark ? "menu_dark" : "menu_overworld";
                step = menuInDark ? Step.MENU_DENY : Step.MENU_TO_DARK;
                waitUntil = now + 10;
            }
            case MENU_DENY -> {
                // TA-02 can't be cast in the dark: its refusal goes to the action bar, which the dark hides
                WhiteSpace.clear();
                onServer(server, player -> log("TA-02 in the dark: "
                    + (QuasarJets.cast(player, Ton618Link.QUASAR_ON_TON618, null) ? "cast" : "refused")));
                step = Step.MENU_DENY_SHOT;
                waitUntil = now + 15;
            }
            case MENU_DENY_SHOT -> {
                pendingShot = "deny_dark";
                step = Step.MENU_END;
                waitUntil = now + 10;
            }
            case MENU_TO_DARK -> {
                WhiteSpace.clear();
                menuInDark = true;
                onServer(server, player -> {
                    String name = player.getGameProfile().getName();
                    run(player.server, "data modify storage ton618:ton618 devoured set value 1b");
                    run(player.server, "execute in ton618:devoured run tp " + name + " 0.5 1 0.5 0 0");
                });
                step = Step.MENU_OPEN;
                waitUntil = now + 100;
            }
            case MENU_OPEN -> {
                pendingShot = "dark";
                mc.player.getInventory().selected = 0;
                openMenu(mc);
                step = Step.MENU_SHOT;
                waitUntil = now + 60;
            }
            case MENU_END -> finish(mc);
            case SS21 -> {
                // story ticks from the cast (its jet touches the ground at 758, bursts at 998)
                while (nextShot < SHOTS.length && now - ss21At >= SHOTS[nextShot]) {
                    pendingShot = String.format(Locale.ROOT, "ss21_%04d", (int) SHOTS[nextShot]);
                    nextShot++;
                    return;
                }
                if (nextShot >= SHOTS.length) {
                    step = Step.MENU_END;
                    waitUntil = now + 5;
                }
            }
            case FILM -> {
                if (!started && now - waitUntil > 200) {
                    log("FAILED: the strike never reached this client (see the log for the server's refusal)");
                    finish(mc);
                }
            }
            default -> {
            }
        }
    }

    private static void createWorld(Minecraft mc) {
        try {
            deleteRecursive(mc.gameDirectory.toPath().resolve("saves").resolve(LEVEL));
        } catch (IOException e) {
            log("couldn't delete the old test world: " + e);
        }
        LevelSettings settings = new LevelSettings(LEVEL, GameType.CREATIVE, false, Difficulty.PEACEFUL, true,
            new GameRules(), WorldDataConfiguration.DEFAULT);
        WorldOptions options = new WorldOptions(SEED, true, false);
        Function<RegistryAccess, WorldDimensions> dimensions = access -> access.registryOrThrow(Registries.WORLD_PRESET)
            .getOrThrow("flat".equals(WORLD) ? WorldPresets.FLAT : WorldPresets.NORMAL).createWorldDimensions();
        Screen parent = new TitleScreen();
        log("creating the test world");
        mc.createWorldOpenFlows().createFreshLevel(LEVEL, settings, options, dimensions, parent);
    }

    private static void setUp(ServerPlayer player) {
        String name = player.getGameProfile().getName();
        for (String command : new String[]{"gamerule doDaylightCycle false", "gamerule doWeatherCycle false",
            "gamerule doMobSpawning false", "weather clear 1000000", "time set 18000", "give " + name + " ton618:ton_618"}) {
            run(player.server, command);
        }
        if ("lastlight".equals(SKILL)) {
            run(player.server, "data modify storage ton618:ton618 devoured set value 1b");
            run(player.server, "execute in ton618:devoured run tp " + name + " 0.5 1 0.5 0 0");
            return;
        }
        // a glass perch high above the trees, so both presses see open sky and the ground far off (spawn can be in a forest)
        ServerLevel level = player.serverLevel();
        int x = Mth.floor(player.getX());
        int z = Mth.floor(player.getZ());
        int top = 0;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                top = Math.max(top, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x + dx, z + dz));
            }
        }
        int y = top + PERCH;
        run(player.server, "fill " + (x - 1) + " " + y + " " + (z - 1) + " " + (x + 1) + " " + y + " " + (z + 1) + " glass");
        player.teleportTo(level, x + 0.5, y + 1, z + 0.5, player.getYRot(), player.getXRot());
    }

    private static void castQuasar(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int x = Mth.floor(player.getX()) + DISTANCE;
        int z = Mth.floor(player.getZ());
        // (the land there loaded first: unloaded, its height reads as the bottom of the world and press 2 is refused)
        level.getChunk(x >> 4, z >> 4);
        Vec3 target = new Vec3(x + 0.5, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 0.5, z + 0.5);
        look(player, target.subtract(player.getEyePosition()));
        boolean cast = QuasarJets.cast(player, Ton618Link.QUASAR_ON_TON618, new Tuning(POWER, Tuning.INFINITE, 1F));
        log("press 2 at " + target + ": " + (cast ? "the strike started" : "REFUSED"));
    }

    /** RimuLib's H menu, as the H key opens it (the held item, slot 0, is TON 618). */
    private static void openMenu(Minecraft mc) {
        try {
            WhiteSpace.openMenu(mc);
            log("pressed H: state=" + WhiteSpace.state());
        } catch (RuntimeException | LinkageError e) {
            log("couldn't open the H menu: " + e);
        }
    }

    /** SS-21 Quasar's skill (id {@code quasar}) on the Stellar Remote, cast at the ground {@link #DISTANCE} blocks east. */
    private static void castSs21(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int x = Mth.floor(player.getX()) + DISTANCE;
        int z = Mth.floor(player.getZ());
        level.getChunk(x >> 4, z >> 4);
        Vec3 target = new Vec3(x + 0.5, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 0.5, z + 0.5);
        look(player, target.subtract(player.getEyePosition()));
        for (SkillSet set : SkillSet.all()) {
            for (Skill skill : set.skills) {
                if (skill.getClass().getName().startsWith("dev.ss21.")) {
                    boolean cast = set.cast(player, skill, Tuning.initial(skill));
                    log("SS-21 Quasar (" + skill.id() + " in " + set.id + ") at " + target + ": " + (cast ? "cast" : "REFUSED"));
                    return;
                }
            }
        }
        log("FAILED: SS-21 Quasar's skill isn't in any skill set");
    }

    private static void castLastLight(ServerPlayer player) {
        boolean cast = LastLight.cast(player, Ton618Link.ON_TON618);
        log("Last Light: " + (cast ? "cast" : "REFUSED"));
    }

    /** Turns the player to look along {@code direction} (server side; the client follows). */
    private static void look(ServerPlayer player, Vec3 direction) {
        Vec3 d = direction.normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        float pitch = (float) Math.toDegrees(-Math.asin(Mth.clamp(d.y, -1.0, 1.0)));
        player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), yaw, pitch);
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.setYHeadRot(yaw);
    }

    private static QuasarStars.Star star() {
        for (QuasarStars.Star s : QuasarStars.ALL) {
            if (s.name().equalsIgnoreCase(STAR)) {
                return s;
            }
        }
        try {
            return QuasarStars.ALL.get(Integer.parseInt(STAR));
        } catch (RuntimeException notIndex) {
            return QuasarStars.ALL.get(QuasarStars.ALL.size() - 1);
        }
    }

    // ---- the screenshots, at the end of a drawn frame -----------------------------------------------------------------

    static void frame() {
        Minecraft mc = Minecraft.getInstance();
        if (step == Step.DONE || mc.level == null) {
            return;
        }
        if (pendingShot != null) {
            grab(mc, pendingShot);
            pendingShot = null;
        }
        if (step != Step.FILM) {
            return;
        }
        float partial = mc.getTimer().getGameTimeDeltaPartialTick(true);
        float t;
        if ("lastlight".equals(SKILL)) {
            LastLightCast cast = Ton618AdditionClient.current();
            t = cast == null ? Float.NaN : cast.tick(partial);
        } else {
            QuasarCast cast = QuasarClient.current();
            t = cast == null ? Float.NaN : cast.tick(partial);
        }
        if (Float.isNaN(t)) {
            if (started && mc.level.getGameTime() - startedAt > 40) {
                log("the strike is over");
                finish(mc);
            }
            return;
        }
        if (!started) {
            started = true;
            log("the strike reached this client");
        }
        startedAt = mc.level.getGameTime();
        while (nextShot < SHOTS.length && t >= SHOTS[nextShot]) {
            grab(mc, String.format(Locale.ROOT, "%s_%04d", SKILL, (int) SHOTS[nextShot]));
            nextShot++;
        }
        if (nextShot >= SHOTS.length) {
            finish(mc);
        }
    }

    private static void grab(Minecraft mc, String name) {
        Screenshot.grab(mc.gameDirectory, name + ".png", mc.getMainRenderTarget(), message -> {
        });
        log("shot " + name);
    }

    private static void finish(Minecraft mc) {
        step = Step.DONE;
        log("done");
        mc.stop();
    }

    // ---- helpers -----------------------------------------------------------------------------------------------------

    private interface PlayerAction {
        void run(ServerPlayer player);
    }

    private static void onServer(MinecraftServer server, PlayerAction action) {
        server.execute(() -> {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                action.run(players.get(0));
            }
        });
    }

    private static void run(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
    }

    private static String prop(String name, String fallback) {
        String value = System.getProperty("ton618test." + name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** "300,420,795" or "0-1000/20"; empty = every 20 ticks up to {@code end}. */
    private static float[] shots(String text, int end) {
        List<Float> out = new ArrayList<>();
        if (text.isEmpty()) {
            text = "0-" + end + "/20";
        }
        for (String part : text.split(",")) {
            part = part.trim();
            if (part.contains("-")) {
                String[] range = part.split("[-/]");
                float from = Float.parseFloat(range[0]);
                float to = Float.parseFloat(range[1]);
                float stepBy = range.length > 2 ? Float.parseFloat(range[2]) : 20F;
                for (float s = from; s <= to; s += stepBy) {
                    out.add(s);
                }
            } else if (!part.isEmpty()) {
                out.add(Float.parseFloat(part));
            }
        }
        out.sort(Comparator.naturalOrder());
        float[] result = new float[out.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = out.get(i);
        }
        return result;
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }
}
