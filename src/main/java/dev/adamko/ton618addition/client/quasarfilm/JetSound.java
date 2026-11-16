package dev.adamko.ton618addition.client.quasarfilm;


import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.quasar.QuasarReading;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.DoubleSupplier;
import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.SoundBufferLibrary;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import org.lwjgl.BufferUtils;

/**
 * <b>FABLE'S FILE.</b> The film's soundtrack: one continuous stereo track for the whole film (about 50 s), <b>made by this
 * code</b> the first time it is needed (plain oscillators and filtered noise, the previz's "sound sketch" grown up; the same way Last Light's is made and played) and
 * played through the game's own sound engine on the Players volume slider, as the SA Series plays its films'
 * soundtracks. No sound file is used, ours or anyone's.
 *
 * <p>The game stops every sound when the world changes (a trip to another dimension), so the film
 * starts the track again from wherever its clock is ({@link #play}); the same serves a player who arrives mid-film.
 */
final class JetSound {
    private static final int RATE = JetSynth.RATE;
    private static final ResourceLocation ID = Ton618Addition.id("film/quasar_jets");
    private static final Sound SOUND = new Sound(ID, ConstantFloat.of(1F), ConstantFloat.of(1F), 1, Sound.Type.FILE, true, false, 16);
    private static final WeighedSoundEvents EVENTS = new WeighedSoundEvents(ID, null);
    @Nullable
    private static CompletableFuture<short[]> rendered;
    /** What the track in {@link #rendered} was made for: slowed or not, and which cues had sound files. */
    private static String renderedFor = "";
    private static final float[][] NO_WINDOWS = {};

    static {
        EVENTS.addSound(SOUND);
    }

    private JetSound() {
    }

    /**
     * Starts making the track (off the game's thread) for a strike that is {@code slowed} by educational mode's reading
     * windows or not, unless that very track is made already. Call it on the game's thread: it asks which cues have
     * sound files.
     */
    static synchronized CompletableFuture<short[]> prepare(boolean slowed) {
        float[][] windows = slowed ? QuasarReading.WINDOWS : NO_WINDOWS;
        Set<String> files = new HashSet<>();
        StringBuilder key = new StringBuilder(slowed ? "slowed" : "plain");
        for (String name : JetSynth.FILES) {
            if (JetCues.has(name)) {
                files.add(name);
                key.append('|').append(name);
            }
        }
        if (rendered == null || !renderedFor.contentEquals(key)) {
            renderedFor = key.toString();
            rendered = CompletableFuture.supplyAsync(() -> {
                try {
                    long began = System.nanoTime();
                    short[] pcm = JetSynth.render(windows, files::contains);
                    Ton618Addition.LOG.debug("The Quasar Jets soundtrack made in {} ms", (System.nanoTime() - began) / 1000000L);
                    return pcm;
                } catch (RuntimeException failed) {
                    Ton618Addition.LOG.error("The Quasar Jets soundtrack couldn't be made, so the film is silent", failed);
                    return new short[0];
                }
            });
        }
        return rendered;
    }

    /**
     * Plays the track from wherever {@code clock} is: <b>real</b> game ticks from the cast (the track already has the
     * strike's slow-downs in it); {@code over}: the film has ended.
     */
    static Track play(boolean slowed, DoubleSupplier clock, java.util.function.BooleanSupplier over) {
        Track track = new Track(clock, over, prepare(slowed));
        Minecraft.getInstance().getSoundManager().play(track);
        return track;
    }

    /** One playing of the track, kept in step with the film's clock. */
    static final class Track extends AbstractTickableSoundInstance {
        private final DoubleSupplier clock;
        private final java.util.function.BooleanSupplier over;
        private final CompletableFuture<short[]> made;
        /** The film's second at which this playing started, and how much of it has played since. */
        private double startedAt = Double.NaN;
        private double played;
        private long last;

        private Track(DoubleSupplier clock, java.util.function.BooleanSupplier over, CompletableFuture<short[]> made) {
            super(SoundEvent.createVariableRangeEvent(ID), SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.clock = clock;
            this.over = over;
            this.made = made;
            this.volume = 1F;
            this.pitch = 1F;
            this.relative = true;
            this.attenuation = SoundInstance.Attenuation.NONE;
            this.looping = false;
        }

        /** The track is no sound file: it stands for itself. */
        @Override
        public WeighedSoundEvents resolve(SoundManager manager) {
            this.sound = SOUND;
            return EVENTS;
        }

        @Override
        public CompletableFuture<AudioStream> getStream(SoundBufferLibrary buffers, Sound sound, boolean looping) {
            return this.made.thenApply(pcm -> {
                this.startedAt = this.clock.getAsDouble() / 20.0;
                this.played = 0.0;
                this.last = System.nanoTime();
                return new Stream(pcm, (int) (this.startedAt * RATE));
            });
        }

        void end() {
            this.stop();
        }

        /** How far this playing has drifted from the film's clock, in seconds (0 until it has started). */
        double drift() {
            return Double.isNaN(this.startedAt) ? 0.0 : this.startedAt + this.played - this.clock.getAsDouble() / 20.0;
        }

        @Override
        public void tick() {
            if (this.over.getAsBoolean()) {
                this.stop();
                return;
            }
            if (Double.isNaN(this.startedAt)) {
                return;
            }
            long now = System.nanoTime();
            double dt = (now - this.last) / 1.0E9;
            this.last = now;
            // a long gap: the game was paused, and the track with it
            this.played += (dt > 1.0 ? 0.05 : dt) * this.pitch;
            // the game's clock and the sound card's drift apart: nudge the speed by up to 4% until they agree
            this.pitch = (float) (1.0 - Math.max(-0.04, Math.min(0.04, this.drift() * 0.6)));
        }
    }

    /** The track's samples from one point on, a second at a time. */
    private static final class Stream implements AudioStream {
        private static final AudioFormat FORMAT = new AudioFormat(RATE, 16, 2, true, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
        private final short[] pcm;
        private int at;

        private Stream(short[] pcm, int fromFrame) {
            this.pcm = pcm;
            this.at = Math.max(0, fromFrame) * 2;
        }

        @Override
        public AudioFormat getFormat() {
            return FORMAT;
        }

        @Override
        @Nullable
        public ByteBuffer read(int size) {
            int count = Math.min(size / 2 & ~1, this.pcm.length - this.at);
            if (count <= 0) {
                return null;
            }
            ByteBuffer out = BufferUtils.createByteBuffer(count * 2);
            out.asShortBuffer().put(this.pcm, this.at, count);
            this.at += count;
            return out;
        }

        @Override
        public void close() {
        }
    }

}
