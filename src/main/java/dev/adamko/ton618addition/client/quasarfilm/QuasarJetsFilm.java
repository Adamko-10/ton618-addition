package dev.adamko.ton618addition.client.quasarfilm;

import cyou.rimuru.rimulib.mc1211.client.api.FilmApi;
import cyou.rimuru.rimulib.mc1211.client.cinematic.Cutscene;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ToastAddEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * <b>FABLE'S FILE.</b> TA-02 · Quasar Jets' film, built from the previz Adam approved on 2026-11-15
 * ({@code previz/quasar-jets/NOTES.md}, v1: 960 ticks, 10 shots). This package ({@code client/quasarfilm}) and the
 * film's shader folder ({@code shaders/quasarfilm}) belong to the Fable session (CLAUDE.md, "For Fable: TA-02's film").
 *
 * <p><b>How it is put together</b> (each part a pure function of the strike's clock, so nothing is simulated):
 * <ul>
 *   <li>{@link JetScript}: the timeline, the numbers on screen, the words;</li>
 *   <li>{@link JetCamera}: the camera in the world, one cutscene handed to RimuLib's film director on the strike's
 *       clock (shot 1, then shots 8 to 10);</li>
 *   <li>{@link JetSpace}: the film's own space scene over the whole screen (ticks 80 to 680: the star engine, the
 *       flight, the meal, the ignition, the jets, the blazar turn);</li>
 *   <li>{@link JetHud}: bars, captions, the reticle, counters, readouts, the title, the educational labels, the
 *       flashes and the flood;</li>
 *   <li>{@link JetSound} and {@link JetSynth}: the soundtrack, made by code;</li>
 *   <li>{@link JetGl}: the one shader program and the drawing helpers.</li>
 * </ul>
 *
 * <p><b>What it does not draw.</b> Everything in the world from tick 680 on (the second sun and its day, the jet, the
 * radio lobe and its pop, the mark, our named stars) is {@code client/quasar/QuasarRender}'s, for everyone; the film
 * only points its camera at it. A player who has RimuLib's films switched off, or skips this one with RimuLib's film
 * key, sees those world effects from where they stand and nothing of the film.
 *
 * <p><b>The contract</b> (what the rest of the mod calls): {@link #init}, {@link #start}, {@link #stop},
 * {@link #active}.
 */
public final class QuasarJetsFilm {
    @Nullable
    private static QuasarCast cast;
    /** The film's camera with RimuLib's director; null when this player isn't shown the film (off, skipped, failed). */
    @Nullable
    private static Cutscene film;
    @Nullable
    private static JetSound.Track track;
    /** Ticks since the camera was last handed to the director, and since the soundtrack was last started. */
    private static int sinceFilm;
    private static int sinceSound;
    /** Toasts that arrived while the film had the camera: shown when it lets go. */
    private static final List<Toast> HELD_TOASTS = new ArrayList<>();

    private QuasarJetsFilm() {
    }

    /** Once, from {@code QuasarClient.init}, on clients only. */
    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterShadersEvent.class, JetGl::register);
        modBus.addListener(RegisterShadersEvent.class, JetPost::register);
        // the world effects' shader too (client/quasar is Fable's since the quality redo; this is where the mod bus is)
        modBus.addListener(RegisterShadersEvent.class, dev.adamko.ton618addition.client.quasar.QuasarGl::register);
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, QuasarJetsFilm::world);
        // over the game's own HUD; and when TON 618 calls the HUD off for a film (it does), drawn all the same:
        // client/HudRescue then posts a cancelled Pre for exactly this
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class,
            event -> screen(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(true), false));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, RenderGuiEvent.Pre.class, event -> {
            if (event.isCanceled()) {
                screen(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(true), true);
            }
        });
        // the game's toasts would sit on top of the film: held back while it has the camera
        NeoForge.EVENT_BUS.addListener(ToastAddEvent.class, QuasarJetsFilm::toast);
    }

    /** A strike reached this client and it gets the film (the caster and players within ~128 blocks). */
    public static void start(QuasarCast newCast) {
        end();
        cast = newCast;
        sinceFilm = 0;
        sinceSound = 1000;
        JetSpace.forget();
        JetHud.forget();
        if (FilmApi.automatic()) {
            film = JetCamera.cutscene(newCast);
            FilmApi.play(film);
            JetSound.prepare(newCast.slowed());
            JetCues.start();
            Minecraft.getInstance().getMusicManager().stopPlaying();
            // toasts already on screen (a new game's "Move with W, A, S and D" stays up until the player moves)
            Minecraft.getInstance().getToasts().clear();
        }
    }

    /** Leaving the server, or the strike ended. */
    public static void stop() {
        end();
    }

    public static boolean active() {
        return cast != null;
    }

    /** The film has this player's camera right now (so the world effects leave the shaking of the view to it). */
    public static boolean hasCamera() {
        return cast != null && film != null && !film.skipped;
    }

    private static void end() {
        cast = null;
        letGo();
    }

    /** The film lets go of the camera and falls silent; the strike itself (the world's part) runs on. */
    private static void letGo() {
        if (film != null) {
            Cutscene old = film;
            film = null;
            FilmApi.stop(old.subject);
        }
        if (track != null) {
            track.end();
            track = null;
        }
        JetCues.stop();
        showHeldToasts();
    }

    /** A toast the game wants to show: not over the film. It is kept (a handful at most) for when the film ends. */
    private static void toast(ToastAddEvent event) {
        if (film != null) {
            event.setCanceled(true);
            if (HELD_TOASTS.size() < 8) {
                HELD_TOASTS.add(event.getToast());
            }
        }
    }

    /** The film has let go of the camera ({@code film} is null by now): the toasts held back are shown. */
    private static void showHeldToasts() {
        if (HELD_TOASTS.isEmpty() || film != null) {
            return;
        }
        List<Toast> held = new ArrayList<>(HELD_TOASTS);
        HELD_TOASTS.clear();
        ToastComponent toasts = Minecraft.getInstance().getToasts();
        for (Toast toast : held) {
            toasts.addToast(toast);
        }
    }

    /** RimuLib's director is showing this film's camera right now. */
    private static boolean showing(float partial) {
        return film != null && !film.skipped && !Float.isNaN(FilmApi.time(film.subject, partial));
    }

    private static void tick() {
        QuasarCast now = cast;
        Minecraft mc = Minecraft.getInstance();
        if (now == null || mc.isPaused()) {
            return;
        }
        long g = now.tick();
        if (g > JetScript.END) {
            end();
            return;
        }
        if (film == null) {
            return;
        }
        if (film.skipped) {
            // skipped with RimuLib's film key: the world effects are all that is left
            letGo();
            return;
        }
        // the director let go of it (another film took over, or it was cleared): hand it back
        if (!showing(0F) && g < JetScript.LAST_SHOT_END - 5 && ++sinceFilm > 10) {
            sinceFilm = 0;
            FilmApi.play(film);
        }
        // the cues that have sound files of their own (previz/quasar-jets/SOUND-CUES.md)
        JetCues.tick(now.tick(0F), () -> now.tick(0F), () -> cast != now || film == null);
        // the soundtrack: started here, and again whenever it has stopped or drifted too far from the film's clock
        sinceSound++;
        boolean playing = track != null && mc.getSoundManager().isActive(track);
        if (g < JetScript.END - 10 && sinceSound > 20 && (!playing || Math.abs(track.drift()) > 0.4)) {
            if (track != null) {
                track.end();
            }
            sinceSound = 0;
            try {
                // the track has the strike's slow-downs in it already, so it runs on the real clock
                track = JetSound.play(now.slowed(), () -> now.realTick(0F), () -> cast != now || film == null);
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("The Quasar Jets soundtrack couldn't start", failed);
                sinceSound = -100000;
            }
        }
    }

    /** In the level's own drawing: nothing of ours is drawn, the frame's camera is only noted for the reticle and labels. */
    private static void world(RenderLevelStageEvent event) {
        if (cast != null && film != null && event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY) {
            JetHud.world(event.getModelViewMatrix(), event.getProjectionMatrix(), event.getCamera().getPosition());
        }
    }

    /**
     * Over the screen: the space scene, then the words and veils. {@code hudCalledOff}: the game's HUD was cancelled
     * this frame, so nothing else of the mod's HUD (the pop's flash among it) is drawn.
     */
    private static void screen(GuiGraphics graphics, float partial, boolean hudCalledOff) {
        QuasarCast now = cast;
        if (now == null || film == null || film.skipped) {
            return;
        }
        float t = now.tick(partial);
        // while the director shows the film; and through its last ticks, as the camera goes back to the player and the
        // bars slide away
        if (t > JetScript.END || !showing(partial) && t < JetScript.LAST_SHOT_END - 2F) {
            return;
        }
        try {
            // whatever the HUD has drawn so far goes first, then the scene covers it
            graphics.flush();
            if (JetSpace.covers(t)) {
                JetSpace.draw(t, now.star().type());
            }
            JetHud.draw(graphics, t, now, hudCalledOff);
        } catch (RuntimeException failed) {
            fail(failed);
        }
    }

    /** A film that can't be drawn mustn't take the game down with it: the player gets their own view back. */
    private static void fail(RuntimeException failed) {
        Ton618Addition.LOG.error("The Quasar Jets film stopped drawing; the strike goes on without it", failed);
        letGo();
        JetGl.end();
    }
}
