package dev.adamko.ton618addition.quasar;

import cyou.rimuru.rimulib.mc1211.api.NetApi;
import cyou.rimuru.rimulib.mc1211.api.SkillApi;
import cyou.rimuru.rimulib.mc1211.skill.SkillHolder;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.Ton618AdditionConfig;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        /** The last tick {@link #sounds} has played up to. */
        long soundsDone = -1;
        long lastLobeHit = Long.MIN_VALUE;

        Running(QuasarFx fx, QuasarStars.Star star) {
            this.fx = fx;
            this.star = star;
            this.caster = fx.caster();
            this.mark = fx.mark();
            this.quasar = fx.quasar();
            this.random = RandomSource.create(fx.castGameTime());
        }

        long tick(MinecraftServer server) {
            return server.overworld().getGameTime() - this.fx.castGameTime();
        }
    }

    // ---- casting --------------------------------------------------------------------------------------------------

    /** Both presses, from either menu. True only when the strike started (the menu then starts the cooldown). */
    public static boolean cast(ServerPlayer player, QuasarSkill skill) {
        if (!Ton618AdditionConfig.quasarEnabled()) {
            return deny(player, QuasarInfo.TITLE + " is turned off on this server");
        }
        if (Ton618AdditionConfig.quasarOperatorsOnly() && !player.hasPermissions(2)) {
            return deny(player, "Only operators can cast " + QuasarInfo.NAME + " here");
        }
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) {
            return deny(player, QuasarInfo.NAME + ": only in the overworld, at night");
        }
        long now = level.getGameTime();
        long today = QuasarSky.day(level.getDayTime());
        Lock lock = LOCKS.get(player.getUUID());
        if (lock != null && (now - lock.gameTime() > LOCK_TIMEOUT || !level.isNight())) {
            unlock(player);
            lock = null;
        }
        int star = QuasarStars.looking(player.getLookAngle(), QuasarStars.PICK_DEGREES);
        if (star >= 0) {
            // press 1 (or picking another star): lock it
            if (!level.isNight()) {
                return deny(player, "No stars to launch: wait for night");
            }
            if (level.isRaining()) {
                return deny(player, "Too cloudy: no stars to launch");
            }
            QuasarSky sky = QuasarSky.of(player.server);
            QuasarStars.Star picked = QuasarStars.ALL.get(star);
            if (sky.missing(star, today)) {
                long nights = sky.backOn(star) - today;
                return deny(player, picked.name() + " is gone: back in " + nights + (nights == 1 ? " night" : " nights"));
            }
            LOCKS.put(player.getUUID(), new Lock(star, now));
            // the HUD hint (QuasarReticle) says "locked: aim at the ground"; no action-bar line on top of it (Adam, 2026-11-16)
            send(player, new QuasarLock(star));
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
        BlockPos ground = block.getBlockPos();
        Vec3 mark = new Vec3(ground.getX() + 0.5, ground.getY() + 1.0, ground.getZ() + 0.5);
        double safe = picked.type().lobe + 4.0;
        if (mark.distanceTo(player.position()) < safe) {
            return deny(player, "Too close: aim at least " + (int) Math.ceil(safe) + " blocks away");
        }
        boolean offhand = !(player.getMainHandItem().getItem() instanceof SkillHolder)
            && player.getOffhandItem().getItem() instanceof SkillHolder;
        QuasarFx fx = new QuasarFx(player.getUUID(), player.getId(), now, mark, lock.star(),
            QuasarStars.quasarDirection(player.getYRot()), player.position(), player.getYRot(), player.getXRot(), offhand,
            skill.onRemote(), true, Ton618AdditionConfig.quasarSecondSun());
        unlock(player);
        running = new Running(fx, picked);
        QuasarSky sky = QuasarSky.of(player.server);
        sky.launch(lock.star(), today, Ton618AdditionConfig.quasarMissingNights());
        QuasarMissing missing = new QuasarMissing(sky.copy());
        int films = 0;
        for (ServerPlayer other : player.server.getPlayerList().getPlayers()) {
            send(other, missing);
            if (other.level().dimension() == Level.OVERWORLD) {
                films += show(other) ? 1 : 0;
            }
        }
        Ton618Addition.LOG.info("{} cast {} from {}: {} ({}) into TON618, the jet lands at {}; {} player(s) get the film.",
            player.getGameProfile().getName(), QuasarInfo.TITLE, skill.onRemote() ? "the Stellar Remote" : "TON 618",
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
        send(player, new QuasarMissing(QuasarSky.of(player.server).copy()));
        Running run = running;
        if (run != null && player.level().dimension() == Level.OVERWORLD && run.tick(player.server) < QuasarClock.NIGHT) {
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
            dropAll(run);
            running = null;
            return;
        }
        if (t >= QuasarClock.END) {
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
            if (now - entry.getValue().gameTime() > LOCK_TIMEOUT || !overworld.isNight()) {
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
            entity.moveTo(target.x, target.y, target.z, entity.getYRot(), entity.getXRot());
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

    private static boolean movable(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity()
            && state.getDestroySpeed(level, pos) >= 0f && state.isCollisionShapeFullBlock(level, pos)
            && !state.is(Blocks.BEDROCK);
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
        if (due(from, t, QuasarClock.SWEEP)) {
            play(level, run, SoundEvents.BEACON_ACTIVATE, 6F, 0.5F);
        }
        if (due(from, t, QuasarClock.SWEEP + 22)) {
            play(level, run, SoundEvents.WARDEN_SONIC_CHARGE, 6F, 0.6F);
        }
        if (due(from, t, QuasarClock.LANDS)) {
            play(level, run, SoundEvents.WARDEN_SONIC_BOOM, 8F, 0.6F);
            play(level, run, SoundEvents.GENERIC_EXPLODE.value(), 4F, 0.5F);
        }
        for (int i = 0; i < 4; i++) {
            if (due(from, t, QuasarClock.LOBE + i * 15)) {
                play(level, run, SoundEvents.RESPAWN_ANCHOR_CHARGE, 4F, 0.5F + i * 0.2F);
            }
        }
        if (due(from, t, QuasarClock.POP)) {
            play(level, run, SoundEvents.GENERIC_EXPLODE.value(), 8F, 0.7F);
            play(level, run, SoundEvents.LIGHTNING_BOLT_THUNDER, 6F, 0.8F);
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
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
            .getHolderOrThrow(QuasarContent.RADIO_LOBE));
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
        while (groundY > level.getMinBuildHeight()) {
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
                level.playSound(null, on, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 1.5F,
                    0.7F + run.random.nextFloat() * 0.8F);
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
    private static void send(ServerPlayer player, Object message) {
        cyou.rimuru.rimulib.mc1211.net.RimuPacket<?> packet = message instanceof QuasarFx ? QuasarFx.PACKET
            : message instanceof QuasarLock ? QuasarLock.PACKET : QuasarMissing.PACKET;
        if (NetApi.canSend(player, packet)) {
            NetApi.send(player, message);
        }
    }

    private static boolean deny(ServerPlayer player, String message) {
        SkillApi.deny(player, Component.literal(message).withStyle(ChatFormatting.DARK_PURPLE));
        return false;
    }
}
