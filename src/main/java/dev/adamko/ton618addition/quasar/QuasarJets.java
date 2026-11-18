package dev.adamko.ton618addition.quasar;

import cyou.rimuru.rimulib.api.CarveApi;
import cyou.rimuru.rimulib.api.NetApi;
import cyou.rimuru.rimulib.api.SkillApi;
import cyou.rimuru.rimulib.skill.SkillHolder;
import cyou.rimuru.rimulib.skill.Tuning;
import dev.adamko.ton618addition.ReadingTime;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.Ton618AdditionConfig;
import dev.adamko.ton618addition.link.Education;
import dev.adamko.ton618addition.link.Ton618Link;
import dev.adamko.ton618addition.net.QuasarFx;
import dev.adamko.ton618addition.net.QuasarLock;
import dev.adamko.ton618addition.net.QuasarMissing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * TA-02 · Quasar Jets on the server (planned with Adam 2026-11-15; Fable's approved previz v1 is the timing). At night,
 * in the overworld: press 1 at one of our named stars locks it; press 2 at the ground marks where the jet lands and
 * starts the strike. A star engine pushes the star into TON 618, which devours it and blazes as a quasar (the film's
 * part); then, for everyone in the overworld, the second sun; the jet comes in low from the horizon and carries what it
 * touches to the mark; a radio lobe swells there, shoves and hurts, melts a glassy bowl and pops, leaving frozen-light
 * crystals; the star stays missing from the sky for a few nights.
 *
 * <p>One strike at a time on a server. Everything in the world is timed by {@link QuasarClock} from the second press.
 */
public final class QuasarJets {
    /** A lock lasts a minute (then press 1 again). */
    private static final int LOCK_TIMEOUT = 1200;
    /** How long the world's loops ({@code quasar_beam_loop}, {@code quasar_lobe_loop}) are: 3 s. */
    private static final int LOOP_TICKS = 60;
    /** The blast ring reaches this many crater radii from the mark (1.8 until the crater was made five times wider). */
    static final double RING = 1.25;
    /** The ring's work a server tick, at most (it runs out with the shock wave over ~1.5 s, longer on a slow server). */
    private static final long RING_BUDGET_NS = 4_000_000L;
    /** How far the ground can be for press 2 (blocks). */
    private static final double MARK_RANGE = 256.0;
    /** Players this close to the caster or the mark get the film (Adam: "Caster + nearby players", ~128 blocks). */
    private static final double FILM_RADIUS = 128.0;
    /** Loose blocks the jet tears up near the mark, at most. */
    private static final int LOOSE_BLOCKS = 12;

    private static final Map<UUID, Lock> LOCKS = new HashMap<>();
    @Nullable
    private static Running running;

    private record Lock(int star, long gameTime) {
    }

    private QuasarJets() {
    }

    /** A thing the jet carries: it rides the line to the mark, circles there, and is thrown out by the lobe. */
    private static final class Carried {
        final Entity entity;
        final int slot;
        boolean orbiting;
        boolean released;

        Carried(Entity entity, int slot) {
            this.entity = entity;
            this.slot = slot;
        }
    }

    private static final class Running {
        final QuasarFx fx;
        final QuasarStars.Star star;
        final UUID caster;
        final Vec3 mark;
        final Vec3 quasar;
        final RandomSource random;
        final List<Carried> carried = new ArrayList<>();
        final Set<UUID> carriedIds = new HashSet<>();
        /** The bowl's columns, nearest the mark first, and how many are done. */
        final List<int[]> columns = new ArrayList<>();
        int columnsDone;
        int crystalsPlaced;
        boolean loosened;
        boolean popped;
        /** The landing blast has hit; the crater's columns, nearest the mark first, and how many are cut. */
        boolean blasted;
        /**
         * The crater, cut from the middle out with the shock wave ({@link #cutCrater}), then handed to RimuLib to finish
         * (land that wasn't loaded, as it loads).
         */
        @Nullable
        CarveApi.Cut crater;
        boolean craterHandedOver;
        /** The landing's debris still to throw, and how many more places may be tried for it. */
        int debrisLeft;
        int debrisTries;
        /** The blast ring round the crater: its columns, nearest the mark first, and how many are done. */
        List<int[]> ringColumns;
        int ringDone;
        /** The real tick (since the cast) the beam's and the lobe's loops are next replayed at. */
        long nextBeam;
        long nextLobe;
        /** The last tick {@link #sounds} has played up to. */
        long soundsDone = -1;
        /** The impact's sounds on their way to each player. */
        final List<Boom> booms = new ArrayList<>();
        long lastLobeHit = Long.MIN_VALUE;

        Running(QuasarFx fx, QuasarStars.Star star) {
            this.fx = fx;
            this.star = star;
            this.caster = fx.caster();
            this.mark = fx.mark();
            this.quasar = fx.quasar();
            this.random = RandomSource.create(fx.castGameTime());
        }

        /** The landing blast's radius: the star's, times the Power slider. */
        double blast() {
            return this.star.type().blast * this.fx.power();
        }

        /** The crater's depth at its middle (it grows slower than the width with Power). */
        double depth() {
            return this.star.type().blastDepth * Math.pow(this.fx.power(), 0.75);
        }

        /** Story ticks of {@link QuasarClock} (slower in the reading windows when the caster had educational mode on). */
        long tick(MinecraftServer server) {
            long real = server.overworld().getGameTime() - this.fx.castGameTime();
            return this.fx.educational() ? (long) ReadingTime.story(QuasarReading.WINDOWS, real) : real;
        }
    }

    // ---- casting --------------------------------------------------------------------------------------------------

    /**
     * Both presses, from either menu, with the menu's sliders ({@code tuning}; the second press uses Power and Damage).
     * True only when the strike started (the menu then starts the cooldown).
     */
    public static boolean cast(ServerPlayer player, QuasarSkill skill, @Nullable Tuning tuning) {
        if (!Ton618Link.ss21Installed()) {
            return deny(player, "Locked: " + QuasarInfo.NAME + " needs The Shooting Star: SS-21 Quasar installed");
        }
        if (!Ton618AdditionConfig.quasarEnabled()) {
            return deny(player, QuasarInfo.TITLE + " is turned off on this server");
        }
        if (Ton618AdditionConfig.quasarOperatorsOnly() && !player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return deny(player, "Only operators can cast " + QuasarInfo.NAME + " here");
        }
        ServerLevel level = player.level();
        if (level.dimension() != Level.OVERWORLD) {
            return deny(player, QuasarInfo.NAME + ": only in the overworld, at night");
        }
        long now = level.getGameTime();
        long today = QuasarSky.day(level.getDayTime());
        Lock lock = LOCKS.get(player.getUUID());
        if (lock != null && (now - lock.gameTime() > LOCK_TIMEOUT || !level.isDarkOutside())) {
            unlock(player);
            lock = null;
        }
        int star = QuasarStars.looking(player.getLookAngle(), QuasarStars.PICK_DEGREES);
        if (star >= 0) {
            // press 1 (or picking another star): lock it
            if (!level.isDarkOutside()) {
                return deny(player, "No stars to launch: wait for night");
            }
            if (level.isRaining()) {
                return deny(player, "Too cloudy: no stars to launch");
            }
            QuasarSky sky = QuasarSky.of(player.level().getServer());
            QuasarStars.Star picked = QuasarStars.ALL.get(star);
            if (sky.missing(star, today)) {
                long nights = sky.backOn(star) - today;
                return deny(player, picked.name() + " is gone: back in " + nights + (nights == 1 ? " night" : " nights"));
            }
            LOCKS.put(player.getUUID(), new Lock(star, now));
            // the HUD hint (QuasarReticle) says "locked: aim at the ground"; no action-bar line on top of it (Adam, 2026-11-16)
            send(player, new QuasarLock(star));
            notifySound(player, QuasarContent.LOCK, SoundSource.PLAYERS, 1F, 1F);
            return false;
        }
        if (lock == null) {
            return deny(player, "At night, aim at a named star first");
        }
        // press 2: the ground
        if (running != null) {
            return deny(player, QuasarInfo.NAME + " is already burning");
        }
        HitResult hit = player.pick(MARK_RANGE, 1f, false);
        if (!(hit instanceof BlockHitResult block) || hit.getType() != HitResult.Type.BLOCK) {
            return deny(player, "Aim at the ground, within 256 blocks");
        }
        QuasarStars.Star picked = QuasarStars.ALL.get(lock.star());
        // through trees and plants to the ground under them: the jet (and its crater) lands on the land, not a treetop
        BlockPos ground = groundBelow(player.level(), block.getBlockPos());
        Vec3 mark = new Vec3(ground.getX() + 0.5, ground.getY() + 1.0, ground.getZ() + 0.5);
        // clear of the lobe (the caster is never hurt, and the crater keeps the ground they stand on)
        Tuning tune = tuning == null ? Tuning.initial(skill) : tuning.clampFor(skill);
        float power = Mth.clamp(tune.power(), 0.1F, 3F);
        float damage = tune.erases() ? Float.POSITIVE_INFINITY : tune.damage();
        // the server's limits (config [quasar_jets] max_power, max_damage)
        float maxPower = Ton618AdditionConfig.quasarMaxPower();
        float maxDamage = Ton618AdditionConfig.quasarMaxDamage();
        if (power > maxPower || maxDamage > 0F && damage > maxDamage) {
            power = Math.min(power, maxPower);
            if (maxDamage > 0F) {
                damage = Math.min(damage, maxDamage);
            }
            player.sendSystemMessage(Component.literal(String.format(java.util.Locale.ROOT, "This server holds %s to Power %.1f×%s",
                QuasarInfo.NAME, power, maxDamage > 0F ? String.format(java.util.Locale.ROOT, " and Damage %.0f", maxDamage) : ""))
                .withStyle(ChatFormatting.GRAY));
        }
        double safe = QuasarStars.safeDistance(picked.type(), power);
        if (mark.distanceTo(player.position()) < safe) {
            return deny(player, "Too close: aim at least " + (int) Math.ceil(safe) + " blocks away");
        }
        boolean offhand = !(player.getMainHandItem().getItem() instanceof SkillHolder)
            && player.getOffhandItem().getItem() instanceof SkillHolder;
        QuasarFx fx = new QuasarFx(player.getUUID(), player.getId(), now, mark, lock.star(),
            QuasarStars.quasarDirection(player.getYRot()), player.position(), player.getYRot(), player.getXRot(), offhand,
            skill.onRemote(), true, Ton618AdditionConfig.quasarSecondSun(), Education.on(player), power, damage);
        unlock(player);
        running = new Running(fx, picked);
        QuasarSky sky = QuasarSky.of(player.level().getServer());
        sky.launch(lock.star(), today, Ton618AdditionConfig.quasarMissingNights());
        QuasarMissing missing = new QuasarMissing(sky.copy());
        int films = 0;
        for (ServerPlayer other : player.level().getServer().getPlayerList().getPlayers()) {
            send(other, missing);
            if (other.level().dimension() == Level.OVERWORLD) {
                films += show(other) ? 1 : 0;
            }
        }
        Ton618Addition.LOG.info("{} cast {} from {}: {} ({}) into TON618, the jet lands at {}; {} player(s) get the film.",
            player.getGameProfile().name(), QuasarInfo.TITLE, skill.onRemote() ? "the Stellar Remote" : "TON 618",
            picked.name(), picked.type().label, ground.above().toShortString(), films);
        return true;
    }

    /** Sends the running strike to one overworld player: the film if they are near, the world effects either way. */
    private static boolean show(ServerPlayer player) {
        Running run = running;
        if (run == null) {
            return false;
        }
        boolean film = player.getUUID().equals(run.caster)
            || player.position().distanceTo(run.fx.position()) <= FILM_RADIUS
            || player.position().distanceTo(run.mark) <= FILM_RADIUS;
        send(player, run.fx.withFilm(film));
        return film;
    }

    /** A player logged in or changed dimension: catch them up (the world effects; no film mid-way). */
    public static void arrived(ServerPlayer player) {
        send(player, new QuasarMissing(QuasarSky.of(player.level().getServer()).copy()));
        Running run = running;
        if (run != null && player.level().dimension() == Level.OVERWORLD && run.tick(player.level().getServer()) < QuasarClock.NIGHT) {
            send(player, run.fx.withFilm(false));
        }
    }

    public static void left(ServerPlayer player) {
        LOCKS.remove(player.getUUID());
    }

    public static void clear() {
        running = null;
        LOCKS.clear();
    }

    // ---- the strike, every server tick -----------------------------------------------------------------------------

    public static void tick(MinecraftServer server) {
        expireLocks(server);
        Running run = running;
        if (run == null) {
            return;
        }
        ServerLevel level = server.overworld();
        long t = run.tick(server);
        try {
            boolean terrain = Ton618AdditionConfig.quasarTerrain();
            sounds(level, run, t);
            if (!run.blasted && t >= QuasarClock.LANDS) {
                run.blasted = true;
                blast(level, run, terrain);
            }
            if (terrain && t >= QuasarClock.LANDS) {
                cutCrater(run, t);
                ring(level, run, t);
                if (run.blasted && run.debrisLeft > 0) {
                    throwDebris(level, run);
                }
            }
            if (terrain && !run.loosened && t >= QuasarClock.SWEEP + 30) {
                run.loosened = true;
                loosen(level, run);
            }
            if (t >= QuasarClock.SWEEP && t < QuasarClock.POP) {
                carry(level, run, t);
            }
            if (t >= QuasarClock.LOBE && t < QuasarClock.POP) {
                lobe(level, run, t);
            }
            if (terrain && t >= QuasarClock.MELT) {
                melt(level, run, t);
            }
            if (!run.popped && t >= QuasarClock.POP) {
                run.popped = true;
                pop(level, run);
            }
            if (terrain && t >= QuasarClock.POP) {
                crystals(level, run, t);
            }
        } catch (RuntimeException failed) {
            Ton618Addition.LOG.error("{} failed at tick {}; the strike stops", QuasarInfo.TITLE, t, failed);
            handOver(run);
            dropAll(run);
            running = null;
            return;
        }
        if (t >= QuasarClock.END) {
            handOver(run);
            dropAll(run);
            running = null;
        }
    }

    private static void expireLocks(MinecraftServer server) {
        if (LOCKS.isEmpty()) {
            return;
        }
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();
        Iterator<Map.Entry<UUID, Lock>> it = LOCKS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Lock> entry = it.next();
            if (now - entry.getValue().gameTime() > LOCK_TIMEOUT || !overworld.isDarkOutside()) {
                it.remove();
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    send(player, new QuasarLock(-1));
                }
            }
        }
    }

    /** The jet's line: from the mark out toward the quasar, the part from its head to the far end. */
    private static double distanceToBeam(Running run, Vec3 point, double head) {
        Vec3 rel = point.subtract(run.mark);
        double along = Mth.clamp(rel.dot(run.quasar), head, QuasarClock.BEAM_FAR);
        return rel.subtract(run.quasar.scale(along)).length();
    }

    /** SWEEP..POP: things in the jet's path ride it to the mark and circle there (Adam: "Lobe hurts, jet carries"). */
    private static void carry(ServerLevel level, Running run, long t) {
        double head = QuasarClock.beamHead(t);
        double swept = run.star.type().swept;
        int max = Ton618AdditionConfig.quasarMaxCarried();
        if (t < QuasarClock.THROW && run.carried.size() < max) {
            // near the ground the jet is only within reach close to the mark (it climbs 1 block in 7 going out)
            AABB area = new AABB(run.mark, run.mark).inflate(70, 30, 70);
            for (Entity entity : level.getEntities((Entity) null, area, e -> canCarry(run, e))) {
                if (run.carried.size() >= max) {
                    break;
                }
                if (!run.carriedIds.contains(entity.getUUID())
                    && distanceToBeam(run, entity.position().add(0, entity.getBbHeight() * 0.5, 0), head) <= swept) {
                    pickUp(run, entity);
                }
            }
        }
        for (Carried c : run.carried) {
            if (c.released || !c.entity.isAlive()) {
                continue;
            }
            if (t >= QuasarClock.THROW + (long) c.slot * 40L / Math.max(1, run.carried.size())) {
                throwOut(run, c);
                continue;
            }
            Vec3 pos = c.entity.position();
            Vec3 target;
            Vec3 rel = pos.subtract(run.mark);
            if (!c.orbiting && rel.dot(run.quasar) > 5.0) {
                // ride the line toward the mark at 1.7 blocks a tick, pulled onto its axis
                double along = Math.max(0.0, rel.dot(run.quasar) - 1.7);
                Vec3 onLine = run.mark.add(run.quasar.scale(along));
                target = pos.add(onLine.subtract(pos).scale(0.35)).add(run.quasar.scale(-1.7));
            } else {
                c.orbiting = true;
                double angle = t * 0.12 + c.slot * 2.39996;
                double radius = 3.0 + (c.slot % 3) * 1.3;
                Vec3 spot = run.mark.add(Math.cos(angle) * radius, 4.0 + (c.slot % 4), Math.sin(angle) * radius);
                target = pos.add(spot.subtract(pos).scale(0.3));
            }
            move(c.entity, target);
        }
    }

    private static boolean canCarry(Running run, Entity e) {
        if (!e.isAlive() || e.isSpectator() || e.getUUID().equals(run.caster)) {
            return false;
        }
        if (e instanceof Player player) {
            return Ton618AdditionConfig.quasarCarryPlayers() && !player.isCreative();
        }
        return e instanceof LivingEntity || e instanceof ItemEntity;
    }

    private static void pickUp(Running run, Entity entity) {
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), QuasarContent.LIFT.get(), SoundSource.BLOCKS,
            1F, 0.85F + run.random.nextFloat() * 0.3F);
        entity.setNoGravity(true);
        run.carriedIds.add(entity.getUUID());
        run.carried.add(new Carried(entity, run.carried.size()));
    }

    private static void move(Entity entity, Vec3 target) {
        Vec3 step = target.subtract(entity.position());
        if (entity instanceof ServerPlayer player) {
            player.setDeltaMovement(step);
            player.hurtMarked = true;
        } else {
            entity.setDeltaMovement(step);
            entity.snapTo(target.x, target.y, target.z, entity.getYRot(), entity.getXRot());
            entity.hurtMarked = true;
        }
        entity.fallDistance = 0f;
    }

    /** THROW..: the lobe throws the carried things outward on arcs, to land 1.15 to 1.65 lobe radii out. */
    private static void throwOut(Running run, Carried c) {
        c.released = true;
        Entity e = c.entity;
        e.setNoGravity(false);
        Vec3 out = e.position().subtract(run.mark).multiply(1, 0, 1);
        if (out.lengthSqr() < 1.0e-4) {
            out = new Vec3(Math.cos(c.slot), 0, Math.sin(c.slot));
        }
        out = out.normalize();
        double land = run.star.type().lobe * (1.15 + 0.5 * run.random.nextDouble());
        double flight = 30.0;
        double horizontal = Math.max(0.0, land - e.position().subtract(run.mark).multiply(1, 0, 1).length()) / flight;
        e.setDeltaMovement(out.x * horizontal, 0.75, out.z * horizontal);
        e.hurtMarked = true;
    }

    private static void dropAll(Running run) {
        for (Carried c : run.carried) {
            if (c.entity.isAlive()) {
                c.entity.setNoGravity(false);
            }
        }
        run.carried.clear();
    }

    /** Once, as the jet nears: a few loose blocks on top near its path are torn up and carried with the rest. */
    private static void loosen(ServerLevel level, Running run) {
        int max = Math.min(LOOSE_BLOCKS, Ton618AdditionConfig.quasarMaxCarried() - run.carried.size());
        double swept = run.star.type().swept;
        for (int tries = 0; tries < 60 && max > 0; tries++) {
            double along = 4.0 + run.random.nextDouble() * 32.0;
            double side = (run.random.nextDouble() * 2.0 - 1.0) * (swept + 2.0);
            Vec3 across = new Vec3(-run.quasar.z, 0, run.quasar.x).normalize();
            Vec3 p = run.mark.add(run.quasar.multiply(1, 0, 1).normalize().scale(along)).add(across.scale(side));
            int x = Mth.floor(p.x);
            int z = Mth.floor(p.z);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
                continue;
            }
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            BlockState state = level.getBlockState(top);
            if (!movable(level, top, state)) {
                continue;
            }
            FallingBlockEntity block = FallingBlockEntity.fall(level, top, state);
            pickUp(run, block);
            max--;
        }
    }

    /** The first block at or under {@code pos} that is ground: not a log, leaves, a plant or air. */
    static BlockPos groundBelow(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        while (p.getY() > level.getMinY()) {
            BlockState state = level.getBlockState(p);
            if (state.isAir() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)
                || state.canBeReplaced() && state.getFluidState().isEmpty()) {
                p.move(0, -1, 0);
            } else {
                break;
            }
        }
        return p.immutable();
    }

    private static boolean movable(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity()
            && state.getDestroySpeed(level, pos) >= 0f && state.isCollisionShapeFullBlock(level, pos)
            && !state.is(Blocks.BEDROCK);
    }

    /**
     * LANDS: the jet hits the ground (Adam, 2026-11-16: "make it feel like a big impact and a proper explosion"; 2026-11-17:
     * "make it actually destructive"). Everything near is hurt (most at the middle) and thrown out; with terrain on, the
     * crater is cut by RimuLib's carver (as The Shooting Star's own big craters: fast, spread over ticks, and land that
     * isn't loaded is cut when it loads), blocks fly out as debris, and the blast ring ({@link #ring}) flattens the land
     * round it. The caster, creative and spectator players, and what the jet carries are spared.
     */
    private static void blast(ServerLevel level, Running run, boolean terrain) {
        long started = System.nanoTime();
        QuasarStars.Type type = run.star.type();
        double reach = run.blast() * 1.3;
        // the Damage slider: the hit at the middle, less further out; infinite kills
        float damage = run.fx.damage();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(run.mark, run.mark).inflate(reach),
            e -> e.isAlive() && !e.getUUID().equals(run.caster) && !run.carriedIds.contains(e.getUUID()))) {
            Vec3 rel = e.position().subtract(run.mark);
            double d = rel.length();
            if (d > reach || e instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            double near = 1.0 - d / reach;
            e.hurt(lobeDamage(level), Float.isInfinite(damage) ? Float.MAX_VALUE : (float) (damage * (0.35 + 0.65 * near)));
            push(e, rel, 0.8 + 2.4 * near, 0.6 + 0.9 * near);
        }
        long hits = System.nanoTime();
        if (!terrain) {
            return;
        }
        // the debris, thrown over the next few ticks (all at once it held the server up at the landing)
        run.debrisLeft = (int) Math.round(type.debris * Math.min(run.fx.power(), 1.5));
        run.debrisTries = run.debrisLeft * 4;
        throwDebris(level, run);
        long debris = System.nanoTime();
        crater(level, run);
        long done = System.nanoTime();
        Ton618Addition.LOG.info("{}: the landing blast ({} blocks across): hits {} ms, the first debris {} ms, the crater set up {} ms",
            QuasarInfo.TITLE, (int) (run.blast() * 2), (hits - started) / 1_000_000, (debris - hits) / 1_000_000,
            (done - debris) / 1_000_000);
    }

    /** Debris thrown this tick: blocks, flung up and out, landing round the rim and beyond. */
    private static final int DEBRIS_A_TICK = 40;

    /**
     * LANDS..: up to {@link #DEBRIS_A_TICK} blocks of the surface the blast takes, flung up and out, landing round the rim
     * and beyond; until the strike's debris is all thrown (or it ran out of places to throw it from).
     */
    private static void throwDebris(ServerLevel level, Running run) {
        int thrown = 0;
        while (run.debrisLeft > 0 && run.debrisTries > 0 && thrown < DEBRIS_A_TICK) {
            run.debrisTries--;
            double a = run.random.nextDouble() * Math.PI * 2.0;
            double d = Math.sqrt(run.random.nextDouble()) * run.blast() * 0.9;
            int x = Mth.floor(run.mark.x + Math.cos(a) * d);
            int z = Mth.floor(run.mark.z + Math.sin(a) * d);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
                continue;
            }
            BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
            BlockState state = level.getBlockState(top);
            if (!movable(level, top, state)) {
                continue;
            }
            // its place emptied without block updates first (fall() would update every neighbour: in a jungle, bamboo and
            // plants breaking in chains, 1.5 s for 270 blocks; the crater takes that land anyway)
            level.setBlock(top, state.getFluidState().createLegacyBlock(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            FallingBlockEntity block = FallingBlockEntity.fall(level, top, state);
            block.dropItem = false;
            double speed = 0.8 + run.random.nextDouble() * 1.6;
            block.setDeltaMovement(Math.cos(a) * speed, 1.0 + run.random.nextDouble() * 1.4, Math.sin(a) * speed);
            block.hurtMarked = true;
            thrown++;
            run.debrisLeft--;
        }
    }

    /**
     * LANDS..: the crater cut from the middle out as the shock wave runs (RimuLib's carver, within its shared time budget
     * of each server tick; only land that is loaded). After two seconds, or once it is done, RimuLib finishes the rest
     * by itself, as the land loads ({@link #handOver}). Left to RimuLib from the start, the biggest craters (since they
     * were made five times wider, 2026-11-17) were cut in its own order and the middle could stay untouched for long.
     */
    private static void cutCrater(Running run, long t) {
        CarveApi.Cut cut = run.crater;
        if (cut == null || run.craterHandedOver) {
            return;
        }
        double r = run.blast();
        float u = QuasarClock.ramp(t, QuasarClock.LANDS, QuasarClock.LANDS + 30);
        try {
            cut.tick(4.0 + (r + 1.0) * u);
            if (cut.done() || t >= QuasarClock.LANDS + 40) {
                handOver(run);
            }
        } catch (RuntimeException | LinkageError failed) {
            Ton618Addition.LOG.error("{}: RimuLib couldn't cut the crater", QuasarInfo.TITLE, failed);
            run.crater = null;
        }
    }

    /** Whatever of the crater is left is RimuLib's to finish (as its land loads). */
    private static void handOver(Running run) {
        CarveApi.Cut cut = run.crater;
        if (cut == null || run.craterHandedOver) {
            return;
        }
        run.craterHandedOver = true;
        try {
            if (!cut.done()) {
                cut.start();
            }
        } catch (RuntimeException | LinkageError failed) {
            Ton618Addition.LOG.error("{}: RimuLib couldn't finish the crater", QuasarInfo.TITLE, failed);
        }
    }

    /**
     * The crater: a bowl {@code blastDepth} deep at the middle under the mark, everything above it gone, its floor
     * scorched (blackstone, basalt, glowing magma toward the middle). Pure maths in the floor and the surface: RimuLib may
     * work them out on its own threads.
     */
    private static void crater(ServerLevel level, Running run) {
        double r = run.blast();
        double depth = run.depth();
        double mx = run.mark.x;
        double mz = run.mark.z;
        int markY = Mth.floor(run.mark.y);
        int minY = level.getMinY() + 6;
        // the caster may stand inside it (QuasarStars.safeDistance): the 5 x 5 columns under their feet are kept
        Player caster = level.getPlayerByUUID(run.caster);
        Vec3 feet = caster != null && caster.level() == level ? caster.position() : run.fx.position();
        int footX = Mth.floor(feet.x);
        int footZ = Mth.floor(feet.z);
        CarveApi.Floor floor = (x, z) -> {
            if (Math.abs(x - footX) <= 2 && Math.abs(z - footZ) <= 2) {
                return CarveApi.UNTOUCHED;
            }
            double dx = x + 0.5 - mx;
            double dz = z + 0.5 - mz;
            double s2 = (dx * dx + dz * dz) / (r * r);
            if (s2 >= 1.0) {
                return CarveApi.UNTOUCHED;
            }
            return Math.max(minY, (int) Math.round(markY - depth * (1.0 - s2)));
        };
        CarveApi.Surface scorch = (x, y, z, state) -> {
            if (state.isAir() || !state.getFluidState().isEmpty()) {
                return state;
            }
            double dx = x + 0.5 - mx;
            double dz = z + 0.5 - mz;
            double s = Math.sqrt(dx * dx + dz * dz) / r;
            int roll = Math.floorMod((x * 73856093) ^ (z * 19349663) ^ (y * 83492791), 100);
            if (roll < 18 * (1.0 - s)) {
                return Blocks.MAGMA_BLOCK.defaultBlockState();
            }
            if (roll < 50) {
                return Blocks.BLACKSTONE.defaultBlockState();
            }
            return roll < 80 ? Blocks.BASALT.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
        };
        try {
            // (lazy: only land that is loaded is cut; the rest waits for its land, see handOver)
            run.crater = CarveApi.flatten(level, new Vec3(Math.floor(mx) + 0.5, markY, Math.floor(mz) + 0.5), r + 1.0, floor, scorch)
                .named(Ton618Addition.MOD_ID + ":quasar_crater").lazy();
        } catch (RuntimeException | LinkageError failed) {
            Ton618Addition.LOG.error("{}: RimuLib couldn't cut the crater", QuasarInfo.TITLE, failed);
        }
    }

    /**
     * LANDS..: the blast ring, from the crater's edge out to {@link #RING} x its radius, running out with the shock wave:
     * trees and plants flattened, grass and dirt scorched to coarse dirt, a few fires near the rim. Columns nearest the mark
     * first, {@link #RING_BUDGET_NS} of each server tick at most; only land that is loaded.
     */
    private static void ring(ServerLevel level, Running run, long t) {
        double r = run.blast();
        double outer = r * RING;
        if (run.ringColumns == null) {
            run.ringColumns = new ArrayList<>();
            int o = Mth.ceil(outer);
            for (int dx = -o; dx <= o; dx++) {
                for (int dz = -o; dz <= o; dz++) {
                    int d2 = dx * dx + dz * dz;
                    if (d2 > (r - 1) * (r - 1) && d2 <= outer * outer) {
                        run.ringColumns.add(new int[]{dx, dz});
                    }
                }
            }
            run.ringColumns.sort((a, b) -> Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]));
        }
        double reach = r + (outer - r) * QuasarClock.ramp(t, QuasarClock.LANDS, QuasarClock.LANDS + 30);
        int cx = Mth.floor(run.mark.x);
        int cz = Mth.floor(run.mark.z);
        long until = System.nanoTime() + RING_BUDGET_NS;
        while (run.ringDone < run.ringColumns.size() && System.nanoTime() < until) {
            int[] col = run.ringColumns.get(run.ringDone);
            double d = Math.sqrt(col[0] * col[0] + col[1] * col[1]);
            if (d > reach) {
                break;
            }
            run.ringDone++;
            ringColumn(level, cx + col[0], cz + col[1], (d - r) / (outer - r));
        }
    }

    private static void ringColumn(ServerLevel level, int x, int z, double share) {
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
            return;
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, top, z);
        int y = top;
        // trees and plants go, down to the ground
        while (y > level.getMinY()) {
            BlockState state = level.getBlockState(pos.setY(y));
            boolean plant = state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.BAMBOO) || state.is(Blocks.BAMBOO_SAPLING) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.CACTUS)
                || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM)
                || !state.isAir() && state.canBeReplaced() && state.getFluidState().isEmpty();
            if (plant && removable(level, pos, state)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
                y--;
            } else if (state.isAir()) {
                y--;
            } else {
                break;
            }
        }
        BlockState ground = level.getBlockState(pos.setY(y));
        int roll = Math.floorMod((x * 73856093) ^ (z * 19349663), 100);
        if ((ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.PODZOL) || ground.is(Blocks.MYCELIUM))
            && roll < 85 - share * 45) {
            level.setBlock(pos, Blocks.COARSE_DIRT.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            ground = Blocks.COARSE_DIRT.defaultBlockState();
        }
        // a few fires near the rim
        if (share < 0.35 && roll < 5 && ground.isSolid()) {
            BlockPos above = pos.above();
            if (level.getBlockState(above).isAir()) {
                level.setBlock(above, Blocks.FIRE.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /** LOBE..POP: the dome swells; what is inside is shoved out and hurt every half second (the caster never). */
    private static void lobe(ServerLevel level, Running run, long t) {
        if (t - run.lastLobeHit < 10) {
            return;
        }
        run.lastLobeHit = t;
        double r = run.star.type().lobe * QuasarClock.lobe(t);
        if (r < 1.0) {
            return;
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(run.mark, run.mark).inflate(r),
            e -> e.isAlive() && !e.getUUID().equals(run.caster) && !run.carriedIds.contains(e.getUUID()))) {
            Vec3 rel = e.position().subtract(run.mark);
            if (rel.length() > r || e instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            e.hurt(lobeDamage(level), run.star.type().lobeDamage);
            push(e, rel, 0.45, 0.25);
        }
    }

    /** POP: the dome bursts: a last hit inside it, a shove out to twice its size. */
    /**
     * The strike's world sounds, positioned at the mark, for everyone in range (Fable's proposal P3, Adam's yes,
     * 2026-11-16; the film's own track drops its copies of these): the jet coming down (SWEEP), its landing (LANDS), the
     * radio lobe charging up (LOBE..), its pop (POP). The crystals chime as each grows ({@link #crystals}). Vanilla sounds.
     */
    private static void sounds(ServerLevel level, Running run, long t) {
        long from = run.soundsDone;
        run.soundsDone = t;
        long real = level.getGameTime() - run.fx.castGameTime();
        if (due(from, t, QuasarClock.MARK)) {
            play(level, run, QuasarContent.MARK.get(), 2F, 1F);
        }
        // the second sun: everyone in the overworld hears it where they stand
        if (due(from, t, QuasarClock.DAY) && run.fx.secondSun()) {
            for (ServerPlayer player : level.players()) {
                notifySound(player, QuasarContent.SUN_IGNITE, SoundSource.AMBIENT, 1F, 1F);
            }
        }
        if (due(from, t, QuasarClock.SWEEP)) {
            play(level, run, QuasarContent.JET_INCOMING.get(), 16F, 1F);
        }
        if (due(from, t, QuasarClock.LANDS)) {
            impactSound(level, run, real);
        }
        booms(level, run, real);
        if (due(from, t, QuasarClock.LANDS + 6)) {
            play(level, run, QuasarContent.DEBRIS.get(), 8F, 1F);
        }
        // the jet standing on the ground and the swelling lobe: 3 s loops replayed by real time (educational mode slows
        // story time there), stopped at the pop
        if (t >= QuasarClock.LANDS && t < QuasarClock.POP && real >= run.nextBeam) {
            run.nextBeam = real + LOOP_TICKS;
            play(level, run, QuasarContent.BEAM_LOOP.get(), 8F, 1F);
        }
        if (t >= QuasarClock.LOBE && t < QuasarClock.POP && real >= run.nextLobe) {
            run.nextLobe = real + LOOP_TICKS;
            play(level, run, QuasarContent.LOBE_LOOP.get(), 8F, 1F);
        }
        if (due(from, t, QuasarClock.POP - 20)) {
            play(level, run, QuasarContent.LOBE_RISE.get(), 8F, 1F);
        }
        if (due(from, t, QuasarClock.POP)) {
            stopLoops(level);
            play(level, run, QuasarContent.POP.get(), 16F, 1F);
        }
        if (due(from, t, QuasarClock.NIGHT)) {
            play(level, run, QuasarContent.NIGHT.get(), 4F, 1F);
        }
    }

    /** Sound in the air: blocks a tick (343 m/s). */
    private static final double SOUND_SPEED = 17.15;
    /** The impact is heard up to this far. */
    private static final double IMPACT_HEARD = 2500.0;

    /** A player's impact sound on its way to them: when it arrives (real ticks since the cast) and how loud each half is. */
    private record Boom(UUID player, long due, float near, float far) {
    }

    /**
     * LANDS: the impact, for each player in the overworld their own mix of the near sound (the crack, the electric
     * blast) and the far one (the deep, muffled boom) by their distance, arriving after the time sound takes to get to
     * them (the flash first, then the boom), from the mark's direction. Players who get the film hear it at once (the
     * film shows the landing then). Pitched by the star: a red dwarf's higher, a giant's lower.
     */
    private static void impactSound(ServerLevel level, Running run, long real) {
        double blast = run.blast();
        for (ServerPlayer player : level.players()) {
            double d = player.position().distanceTo(run.mark);
            if (d > IMPACT_HEARD) {
                continue;
            }
            float near = (float) Mth.clamp(1.0 - d / (blast * 2.5), 0.0, 1.0);
            float far = (float) (Mth.clamp(1.0 - d / IMPACT_HEARD, 0.0, 1.0) * (0.5 + 0.5 * (1.0 - near)));
            boolean film = player.getUUID().equals(run.caster)
                || player.position().distanceTo(run.fx.position()) <= FILM_RADIUS || d <= FILM_RADIUS;
            long delay = film ? 0L : Math.min(60L, Math.round(d / SOUND_SPEED));
            run.booms.add(new Boom(player.getUUID(), real + delay, near, far));
        }
    }

    /** The impact sounds that have reached their players. */
    private static void booms(ServerLevel level, Running run, long real) {
        if (run.booms.isEmpty()) {
            return;
        }
        QuasarStars.Type type = run.star.type();
        float pitch = type == QuasarStars.Type.RED_DWARF ? 1.15F : type == QuasarStars.Type.SUN_LIKE ? 1F
            : type == QuasarStars.Type.BLUE_GIANT ? 0.92F : 0.85F;
        Iterator<Boom> it = run.booms.iterator();
        while (it.hasNext()) {
            Boom boom = it.next();
            if (boom.due() > real) {
                continue;
            }
            it.remove();
            if (!(level.getPlayerByUUID(boom.player()) instanceof ServerPlayer player)) {
                continue;
            }
            // three blocks from them toward the mark: from its direction, as loud as asked
            Vec3 eye = player.getEyePosition();
            Vec3 toward = run.mark.subtract(eye);
            Vec3 at = toward.lengthSqr() < 9.0 ? run.mark : eye.add(toward.normalize().scale(3.0));
            if (boom.near() > 0.02F) {
                player.connection.send(new ClientboundSoundPacket(QuasarContent.IMPACT_NEAR.holder(), SoundSource.BLOCKS, at.x, at.y, at.z, boom.near(),
                    pitch, level.random.nextLong()));
            }
            if (boom.far() > 0.02F) {
                player.connection.send(new ClientboundSoundPacket(QuasarContent.IMPACT_FAR.holder(), SoundSource.BLOCKS, at.x, at.y, at.z, boom.far(),
                    pitch, level.random.nextLong()));
            }
        }
    }

    /** The beam's and the lobe's loops end with the pop, for everyone who may hear them. */
    private static void stopLoops(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            player.connection.send(new ClientboundStopSoundPacket(QuasarContent.BEAM_LOOP.getId(), SoundSource.BLOCKS));
            player.connection.send(new ClientboundStopSoundPacket(QuasarContent.LOBE_LOOP.getId(), SoundSource.BLOCKS));
        }
    }

    /** {@code cue} falls after the last tick played and by this one (once each, even if a tick is skipped). */
    private static boolean due(long from, long t, long cue) {
        return from < cue && cue <= t;
    }

    /** Heard up to 16 x {@code volume} blocks from the mark. */
    private static void play(ServerLevel level, Running run, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, run.mark.x, run.mark.y, run.mark.z, sound, SoundSource.BLOCKS, volume, pitch);
    }

    private static void pop(ServerLevel level, Running run) {
        double r = run.star.type().lobe;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(run.mark, run.mark).inflate(r * 2.0),
            e -> e.isAlive() && !e.getUUID().equals(run.caster))) {
            Vec3 rel = e.position().subtract(run.mark);
            double d = rel.length();
            if (d > r * 2.0 || e instanceof Player p && (p.isCreative() || p.isSpectator())) {
                continue;
            }
            if (d <= r * 1.2) {
                e.hurt(lobeDamage(level), run.star.type().popDamage);
            }
            push(e, rel, 1.4 * (1.0 - d / (r * 2.0)) + 0.3, 0.6);
        }
        Ton618Addition.LOG.info("{}: the radio lobe of {} popped", QuasarInfo.TITLE, run.star.name());
    }

    private static void push(Entity e, Vec3 rel, double out, double up) {
        Vec3 dir = rel.multiply(1, 0, 1);
        dir = dir.lengthSqr() < 1.0e-4 ? new Vec3(1, 0, 0) : dir.normalize();
        e.setDeltaMovement(e.getDeltaMovement().add(dir.x * out, up, dir.z * out));
        e.hurtMarked = true;
    }

    private static DamageSource lobeDamage(ServerLevel level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
            .getOrThrow(QuasarContent.RADIO_LOBE));
    }

    /**
     * MELT..POP: the ground under the lobe sinks into a bowl ({@code depth × (1 − (d/R)²)}, whole blocks) of dark glass,
     * from the middle out. Trees and plants in it go too; water, block entities and unbreakable blocks are left alone.
     */
    private static void melt(ServerLevel level, Running run, long t) {
        double radius = run.star.type().lobe;
        if (run.columns.isEmpty() && run.columnsDone == 0) {
            int r = Mth.ceil(radius);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        run.columns.add(new int[]{dx, dz});
                    }
                }
            }
            run.columns.sort((a, b) -> Integer.compare(a[0] * a[0] + a[1] * a[1], b[0] * b[0] + b[1] * b[1]));
        }
        double reach = radius * QuasarClock.ramp(t, QuasarClock.MELT, QuasarClock.POP);
        int cx = Mth.floor(run.mark.x);
        int cz = Mth.floor(run.mark.z);
        while (run.columnsDone < run.columns.size()) {
            int[] col = run.columns.get(run.columnsDone);
            double d = Math.sqrt(col[0] * col[0] + col[1] * col[1]);
            if (d > reach) {
                break;
            }
            run.columnsDone++;
            meltColumn(level, run, cx + col[0], cz + col[1], d / radius);
        }
    }

    private static void meltColumn(ServerLevel level, Running run, int x, int z, double share) {
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
            return;
        }
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, topY, z);
        if (!level.getFluidState(pos).isEmpty()) {
            return;
        }
        // the ground: below trees, leaves and plants
        int groundY = topY;
        while (groundY > level.getMinY()) {
            BlockState state = level.getBlockState(pos.setY(groundY));
            if (state.isAir() || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.canBeReplaced()) {
                groundY--;
            } else {
                break;
            }
        }
        int depth = (int) Math.round(run.star.type().bowl * (1.0 - share * share));
        int floorY = groundY - depth;
        for (int y = topY; y > floorY; y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && removable(level, pos, state)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
        if (share < 0.88) {
            pos.setY(floorY);
            BlockState floor = level.getBlockState(pos);
            if (!floor.isAir() && removable(level, pos, floor)) {
                BlockState glass = run.random.nextFloat() < 0.75f ? Blocks.TINTED_GLASS.defaultBlockState()
                    : Blocks.OBSIDIAN.defaultBlockState();
                level.setBlock(pos, glass, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    private static boolean removable(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0f && state.getFluidState().isEmpty();
    }

    /** POP..: frozen-light crystals in the star's colour grow on the bowl's floor (890 to 918). */
    private static void crystals(ServerLevel level, Running run, long t) {
        int total = run.star.type().crystals;
        int due = (int) (total * QuasarClock.ramp(t, QuasarClock.POP, 918));
        BlockState crystal = QuasarContent.FROZEN_LIGHT.get(run.star.type()).get().defaultBlockState();
        double radius = run.star.type().lobe * 0.8;
        int tries = 0;
        while (run.crystalsPlaced < due && tries++ < 40) {
            double a = run.random.nextDouble() * Math.PI * 2.0;
            double d = Math.sqrt(run.random.nextDouble()) * radius;
            int x = Mth.floor(run.mark.x + Math.cos(a) * d);
            int z = Mth.floor(run.mark.z + Math.sin(a) * d);
            if (!level.hasChunkAt(new BlockPos(x, 0, z))) {
                run.crystalsPlaced++;
                continue;
            }
            BlockPos on = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
            if (level.getBlockState(on).isAir() && level.getBlockState(on.below()).isFaceSturdy(level, on.below(),
                net.minecraft.core.Direction.UP)) {
                level.setBlock(on, crystal, Block.UPDATE_ALL);
                level.playSound(null, on, QuasarContent.CRYSTAL.get(), SoundSource.BLOCKS, 1.5F,
                    0.8F + run.random.nextFloat() * 0.6F);
                run.crystalsPlaced++;
            }
        }
    }

    // ---- helpers -----------------------------------------------------------------------------------------------------

    private static void unlock(ServerPlayer player) {
        if (LOCKS.remove(player.getUUID()) != null) {
            send(player, new QuasarLock(-1));
        }
    }

    /** Sends one of our packets if the player's client has this addon (RimuLib's packets are optional). */
    private static void send(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload message) {
        if (NetApi.canSend(player, message.type())) {
            NetApi.send(player, message);
        }
    }

    /** A sound only this player hears, where they stand (1.21.1's playNotifySound, gone from 1.21.11's ServerPlayer). */
    private static void notifySound(ServerPlayer player, QuasarContent.Registered<net.minecraft.sounds.SoundEvent> sound, SoundSource source,
                                    float volume, float pitch) {
        player.connection.send(new ClientboundSoundPacket(sound.holder(), source, player.getX(), player.getY(), player.getZ(), volume, pitch,
            player.getRandom().nextLong()));
    }

    private static boolean deny(ServerPlayer player, String message) {
        SkillApi.deny(player, Component.literal(message).withStyle(ChatFormatting.DARK_PURPLE));
        return false;
    }
}
