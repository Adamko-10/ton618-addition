package dev.adamko.ton618addition.client.film;

import cyou.rimuru.rimulib.mc1201.client.api.FilmApi;
import cyou.rimuru.rimulib.mc1201.client.cinematic.Cutscene;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.LastLightCast;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.ToastAddEvent;

/**
 * <b>FABLE'S FILE.</b> The Last Light film, built from the previz Adam approved on 2026-11-13 ({@code previz/NOTES.md},
 * v2: 850 ticks, 10 shots). This package ({@code client/film}) and the film's shader folder belong to the Fable
 * session (CLAUDE.md, "For Fable: the film").
 *
 * <p><b>How it is put together</b> (each part a pure function of the cast's clock, so nothing is simulated and a
 * late arrival simply joins mid-film):
 * <ul>
 *   <li>{@link FilmScript}: the timeline and the numbers on screen;</li>
 *   <li>{@link FilmCamera}: the camera in the world, one cutscene handed to RimuLib's film director on the cast's
 *       clock (the dark, then the overworld);</li>
 *   <li>{@link FilmWorld}: what is drawn in the world (the hand's glow, the time wave in the dark, the returning wave
 *       in the overworld);</li>
 *   <li>{@link FilmSpace}: the film's own space scene over the whole screen (game ticks 200 to 684);</li>
 *   <li>{@link FilmHud}: bars, captions, counters, the title, the educational labels, the white;</li>
 *   <li>{@link FilmSound}: the soundtrack, made by code;</li>
 *   <li>{@link FilmGl}: the one shader program and the drawing helpers.</li>
 * </ul>
 *
 * <p><b>Who sees what.</b> Everyone the cast reaches gets the whole film, unless they have RimuLib's films switched
 * off or skip it with RimuLib's film key: then only the white of the burst is shown, so the trip back to the
 * overworld is still hidden.
 *
 * <p><b>Toasts.</b> The game draws its toasts (tutorial hints, advancements, recipes) over everything, the film too.
 * While this film has the camera they are kept off the screen: the ones showing when it starts are cleared, and the ones
 * that arrive while it plays are held back and shown when it ends.
 *
 * <p><b>The contract</b> (what the rest of the mod calls): {@link #init}, {@link #start}, {@link #stop},
 * {@link #active}.
 */
public final class LastLightFilm {
    @Nullable
    private static LastLightCast cast;
    /** The film's camera with RimuLib's director; null while only the white is shown. */
    @Nullable
    private static Cutscene film;
    @Nullable
    private static FilmSound.Track track;
    /** Ticks since the camera was last handed to the director, and since the soundtrack was last started. */
    private static int sinceFilm;
    private static int sinceSound;
    private static boolean pressed;
    /** Toasts that arrived while the film had the camera: shown when it lets go. */
    private static final List<Toast> HELD_TOASTS = new ArrayList<>();

    private LastLightFilm() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(EventPriority.NORMAL, false, RegisterShadersEvent.class, FilmGl::register);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                tick();
            }
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderLevelStageEvent.class, LastLightFilm::world);
        // over the game's own HUD: Forge 1.21.1 has no RenderGuiEvent, so client/HudRescue calls screen(...) right
        // after the HUD every frame (also when TON 618 hides the HUD)
        // the game's "loading terrain" screen during the trip back: kept white
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ScreenEvent.Render.Post.class, LastLightFilm::loading);
        // the game's toasts would sit on top of the film: held back while it has the camera
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ToastAddEvent.class, LastLightFilm::toast);
    }

    public static void start(LastLightCast newCast) {
        end();
        cast = newCast;
        pressed = newCast.tick() > LastLightClock.PRESS;
        sinceFilm = 0;
        sinceSound = 1000;
        FilmSpace.forget();
        FilmWorld.forget();
        if (FilmApi.automatic()) {
            film = FilmCamera.cutscene(newCast);
            FilmApi.play(film);
            FilmSound.prepare(newCast.slowed());
            Minecraft.getInstance().getMusicManager().stopPlaying();
            // toasts already on screen (a new game's "Move with W, A, S and D" stays up until the player moves)
            Minecraft.getInstance().getToasts().clear();
        }
    }

    public static void stop() {
        end();
    }

    public static boolean active() {
        return cast != null;
    }

    /**
     * RimuLib's director is showing this film right now: not just a cast running (with films off, or skipped, there is
     * none). For the HUD rescue (Codex's review, 2026-11-24).
     */
    public static boolean showingNow() {
        return cast != null && showing(0F);
    }

    private static void end() {
        cast = null;
        if (film != null) {
            FilmApi.stop(film.subject);
            film = null;
        }
        if (track != null) {
            track.end();
            track = null;
        }
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
        LastLightCast now = cast;
        Minecraft mc = Minecraft.getInstance();
        if (now == null || mc.isPaused()) {
            return;
        }
        long g = now.tick();
        if (g > LastLightClock.END) {
            end();
            return;
        }
        if (film == null) {
            return;
        }
        if (film.skipped) {
            // skipped with RimuLib's film key: only the white is left
            Cutscene skipped = film;
            film = null;
            FilmApi.stop(skipped.subject);
            if (track != null) {
                track.end();
                track = null;
            }
            showHeldToasts();
            return;
        }
        // the director let go of it (another film took over, or it was cleared): hand it back
        if (!showing(0F) && g < FilmScript.LAST_SHOT_END - 5 && ++sinceFilm > 10) {
            sinceFilm = 0;
            FilmApi.play(film);
        }
        // the soundtrack: started here, started again after the game stops every sound for the trip back, and
        // whenever it has drifted too far from the film's clock to nudge back
        sinceSound++;
        boolean playing = track != null && mc.getSoundManager().isActive(track);
        if (g < LastLightClock.END - 10 && sinceSound > 20 && (!playing || Math.abs(track.drift()) > 0.4)) {
            if (track != null) {
                track.end();
            }
            sinceSound = 0;
            try {
                // the track has the cast's slow-downs in it already (educational mode), so it runs on the real clock
                track = FilmSound.play(now.slowed(), () -> now.realTick(0F), () -> cast != now);
            } catch (RuntimeException failed) {
                Ton618Addition.LOG.error("Last Light's soundtrack couldn't start", failed);
                sinceSound = -100000;
            }
        }
        // the press: the caster's arm swings (on this client only; nothing is sent)
        if (!pressed && g >= LastLightClock.PRESS) {
            pressed = true;
            Player caster = now.caster();
            if (caster != null && g < LastLightClock.PRESS + 10) {
                caster.swing(now.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, false);
            }
        }
    }

    /** In the world: the waves, the hand's glow, the motes. */
    private static void world(RenderLevelStageEvent event) {
        LastLightCast now = cast;
        // in the dark the film draws after the level is finished, in the overworld after the weather (see FilmWorld)
        if (now == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER
                && event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        float partial = event.getPartialTick();
        if (!showing(partial)) {
            return;
        }
        try {
            FilmWorld.render(event, now, FilmScript.toSb(now.tick(partial)));
        } catch (RuntimeException failed) {
            fail(failed);
        }
    }

    /** Over the screen: the space scene, then the words and veils (from client/HudRescue, after the HUD). */
    public static void screen(GuiGraphics graphics, float partial) {
        LastLightCast now = cast;
        if (now == null) {
            return;
        }
        float g = now.tick(partial);
        if (g > LastLightClock.END) {
            return;
        }
        if (!showing(partial)) {
            FilmHud.white(graphics, g);
            return;
        }
        try {
            float t = FilmScript.toSb(g);
            // whatever the HUD has drawn so far goes first, then the scene covers it
            graphics.flush();
            if (FilmSpace.covers(t)) {
                FilmSpace.draw(t);
            }
            FilmHud.draw(graphics, g, t, now.educational());
        } catch (RuntimeException failed) {
            fail(failed);
        }
    }

    /** The "loading terrain" screen of the trip back, under the film's white. */
    private static void loading(ScreenEvent.Render.Post event) {
        LastLightCast now = cast;
        if (now == null || !(event.getScreen() instanceof ReceivingLevelScreen)) {
            return;
        }
        float g = now.tick(0F);
        if (g >= 677F && g < 735F) {
            GuiGraphics graphics = event.getGuiGraphics();
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xFFFFFFFF);
        }
    }

    /** A film that can't be drawn mustn't take the game down with it: only the white is left. */
    private static void fail(RuntimeException failed) {
        Ton618Addition.LOG.error("The Last Light film stopped drawing; only its white is shown from here", failed);
        if (film != null) {
            FilmApi.stop(film.subject);
            film = null;
        }
        if (track != null) {
            track.end();
            track = null;
        }
        showHeldToasts();
        FilmGl.end();
    }
}
