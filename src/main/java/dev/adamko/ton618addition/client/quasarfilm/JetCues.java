package dev.adamko.ton618addition.client.quasarfilm;

import dev.adamko.ton618addition.Ton618Addition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * <b>FABLE'S FILE.</b> The film's sounds from sound files (Adam, 2026-11-16: "severe lack of sfx"; his pick: the Sonniss
 * GDC 2026 library). The cue sheet is {@code previz/quasar-jets/SOUND-CUES.md}: the Opus session cuts each sound from
 * the library and lists it in {@code sounds.json} under the name given there.
 *
 * <p><b>Nothing here needs changing when a file arrives.</b> Each cue is looked up by name when it is due: if
 * {@code sounds.json} has it, it is played (not positioned, on the Players slider, like the film's track); if not,
 * nothing is played and the film's code-made track ({@link JetSynth}) keeps its own version of that cue, as
 * {@link #has} tells it. One-shot cues start at a story tick; held ones are loops, started inside their span and
 * stopped at its end (so educational mode's slow-down just holds them longer).
 */
final class JetCues {
    /** A cue: its name in {@code sounds.json}, the story tick it starts at, where a loop ends (-1: one shot), how loud. */
    record Cue(String name, float from, float to, float volume) {
    }

    static final Cue[] CUES = {
        new Cue("quasar_film_reticle", 24, -1, 0.8F),
        new Cue("quasar_film_lock", 40, -1, 0.9F),
        new Cue("quasar_film_zoom", 58, -1, 1F),
        new Cue("quasar_film_star_loop", 80, 334, 0.55F),
        new Cue("quasar_film_mirror", 92, -1, 0.9F),
        new Cue("quasar_film_push", 150, -1, 1F),
        new Cue("quasar_film_time_loop", 152, 330, 0.6F),
        new Cue("quasar_film_wind_loop", 200, 330, 0.7F),
        new Cue("quasar_film_arrive", 330, -1, 1F),
        new Cue("quasar_film_mirror_break", 336, -1, 0.8F),
        new Cue("quasar_film_stretch", 350, -1, 0.9F),
        new Cue("quasar_film_ribbon_loop", 362, 440, 0.5F),
        new Cue("quasar_film_ignite", 440, -1, 1F),
        new Cue("quasar_film_title", 455, -1, 0.9F),
        new Cue("quasar_film_quasar_loop", 442, 666, 0.55F),
        new Cue("quasar_film_jets", 530, -1, 1F),
        new Cue("quasar_film_jet_loop", 534, 666, 0.6F),
        new Cue("quasar_film_turn", 620, -1, 1F),
        new Cue("quasar_film_flood", 665, -1, 1F),
        new Cue("quasar_film_birds", 700, -1, 0.5F),
        new Cue("quasar_film_impact_sub", 795, -1, 1F),
        new Cue("quasar_film_pop_sub", 890, -1, 1F)};

    private static final List<SoundInstance> PLAYING = new ArrayList<>();
    private static final boolean[] LOOPING = new boolean[CUES.length];
    private static float last = Float.NaN;
    /** Counts up each film, so loops left over from an earlier one know to stop. */
    private static int film;

    private JetCues() {
    }

    static Identifier id(String name) {
        return Ton618Addition.id(name);
    }

    /** {@code sounds.json} has a sound of this name (ours, {@code ton618_addition:<name>}). */
    static boolean has(String name) {
        try {
            return Minecraft.getInstance().getSoundManager().getSoundEvent(id(name)) != null;
        } catch (RuntimeException unsure) {
            return false;
        }
    }

    /** A film starts: nothing is playing yet. */
    static void start() {
        stop();
        film++;
        last = Float.NaN;
    }

    /** The film lets go: everything it started falls silent. */
    static void stop() {
        film++;
        for (SoundInstance sound : PLAYING) {
            Minecraft.getInstance().getSoundManager().stop(sound);
        }
        PLAYING.clear();
        java.util.Arrays.fill(LOOPING, false);
    }

    /**
     * Once a game tick while the film plays: {@code story} is the film's story tick now ({@code clock} gives it to the
     * loops later), {@code over} says when the film has ended.
     */
    static void tick(float story, DoubleSupplier clock, BooleanSupplier over) {
        float before = Float.isNaN(last) ? story - 1F : last;
        last = story;
        int mine = film;
        for (int i = 0; i < CUES.length; i++) {
            Cue cue = CUES[i];
            boolean loop = cue.to() >= 0F;
            boolean due = loop ? story >= cue.from() && story < cue.to() - 4F && !LOOPING[i] : before < cue.from() && story >= cue.from();
            if (!due || !has(cue.name())) {
                continue;
            }
            try {
                SoundInstance sound;
                if (loop) {
                    LOOPING[i] = true;
                    sound = new Loop(cue, clock, () -> over.getAsBoolean() || film != mine);
                } else {
                    sound = new SimpleSoundInstance(id(cue.name()), SoundSource.PLAYERS, cue.volume(), 1F, SoundInstance.createUnseededRandom(),
                        false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
                }
                Minecraft.getInstance().getSoundManager().play(sound);
                PLAYING.add(sound);
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("The Quasar Jets film couldn't play {}", cue.name(), failed);
            }
        }
    }

    /** A held sound: in over a few ticks, out over its span's last few, gone when the film is. */
    private static final class Loop extends AbstractTickableSoundInstance {
        private final Cue cue;
        private final DoubleSupplier clock;
        private final BooleanSupplier over;

        private Loop(Cue cue, DoubleSupplier clock, BooleanSupplier over) {
            super(SoundEvent.createVariableRangeEvent(id(cue.name())), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.cue = cue;
            this.clock = clock;
            this.over = over;
            this.looping = true;
            this.delay = 0;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.volume = 0.01F;
        }

        @Override
        public void tick() {
            float story = (float) this.clock.getAsDouble();
            if (this.over.getAsBoolean() || story >= this.cue.to()) {
                this.stop();
                return;
            }
            float in = Math.min(1F, (story - this.cue.from() + 1F) / 5F);
            float out = Math.min(1F, (this.cue.to() - story) / 6F);
            this.volume = Math.max(0.01F, this.cue.volume() * Math.max(0F, Math.min(in, out)));
        }
    }
}
