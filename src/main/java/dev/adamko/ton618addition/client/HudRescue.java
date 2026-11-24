package dev.adamko.ton618addition.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import cyou.rimuru.rimulib.mc1211.client.cinematic.CutsceneDirector;
import cyou.rimuru.rimulib.mc1211.client.magic.WhiteSpace;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.client.mixin.GuiAccessor;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.link.Ton618Link;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

/**
 * Puts back what TON 618 hides with the HUD (Adam's test, 2026-11-14: "when ton 618 eats your world you cant open the
 * ui you just dont see it").
 *
 * <p>TON 618 1.1.0 cancels the whole of {@code Gui.render} at its start whenever the player is in its dark
 * ({@code ton618:devoured}) or a RimuLib film hides the HUD, and draws only its own overlay. That also skips what is
 * drawn inside or at the end of {@code Gui.render}: RimuLib's H menu ({@code WhiteSpace.render}, at its end) and
 * NeoForge's {@link RenderGuiEvent}s (the Last Light film's words, space scene and white). So when {@code Gui.render}
 * didn't reach its end this frame, right after it (the same layer, before screens):
 * <ol>
 *   <li>a <b>cancelled</b> {@link RenderGuiEvent.Pre} is posted: "the HUD was called off this frame". Only listeners
 *       that ask for cancelled events hear it (the film does, by design), so other mods' HUDs stay hidden;</li>
 *   <li>in the dark (not during a film): the action bar, so the reason a skill can't be cast there shows (a player's
 *       report, 2026-11-20: "the quasar addition doesnt work too": TA-02's "only in the overworld, at night", SS-21
 *       Quasar's and RimuLib's own refusals all go to the action bar, which the dark hid);</li>
 *   <li>RimuLib's menu is drawn as RimuLib would at the end of the HUD (RimuLib internals, the only way: if they
 *       change, a warning in the log and the menu stays hidden).</li>
 * </ol>
 * The rest of the vanilla HUD (hotbar, chat) stays hidden in the dark, as TON 618 wants. Driven by
 * {@code client/mixin/GuiMixin} and {@code GameRendererMixin}; if another mod changes the game's HUD call so that the
 * second never runs, the same is drawn at the end of the frame instead ({@link #frameEnd}), and the log says so once.
 */
public final class HudRescue {
    /** Dev-only: the harness turns the HUD-call hook off, to test the end-of-frame fallback. */
    private static final boolean NO_HOOK_FOR_TEST = Boolean.getBoolean("ton618test.nohudhook");

    private static boolean reached;
    /** This frame: the HUD-call hook ran / {@code Gui.render} reached its end (reset at each frame's start). */
    private static boolean hookRan;
    private static boolean guiEnded;
    private static boolean warned;
    private static boolean toldRescue;
    private static boolean toldFallback;

    private HudRescue() {
    }

    /** From the client's init: the frame's start and end, for the fallback. */
    public static void init() {
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Pre.class, event -> {
            hookRan = false;
            guiEnded = false;
        });
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Post.class, HudRescue::frameEnd);
    }

    /** Just before the game draws its HUD. */
    public static void beforeGui() {
        if (NO_HOOK_FOR_TEST) {
            return;
        }
        reached = false;
    }

    /** {@code Gui.render} reached its end: nothing was cancelled. */
    public static void guiReachedEnd() {
        reached = true;
        guiEnded = true;
    }

    /**
     * Just after the game's HUD call: if it was cut short, draw what it skipped; then, every frame, Last Light's
     * fallback notice ({@link LastLightNotice}: only while it runs without its film).
     */
    public static void afterGui(GuiGraphics graphics, DeltaTracker delta) {
        if (NO_HOOK_FOR_TEST) {
            return;
        }
        hookRan = true;
        if (!reached) {
            rescue(graphics, delta);
        }
        notice(graphics, delta);
    }

    /**
     * The end of the frame: if the HUD was cut short and our hook after the HUD call never ran (another mod changed
     * that call), draw the same here, set up as the game sets up its HUD. Only with no screen open (a screen is drawn
     * after the HUD, and the menu must stay under it).
     */
    private static void frameEnd(RenderFrameEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (hookRan || guiEnded || mc.level == null || mc.player == null || mc.screen != null || mc.getOverlay() != null
            || !(inDark(mc) || filmHidesHud())) {
            return;
        }
        if (!toldFallback) {
            toldFallback = true;
            Ton618Addition.LOG.warn("The game's HUD call didn't reach TON 618 Addition's hook (another mod changes "
                + "GameRenderer.render): drawing the H menu over TON 618's hidden HUD at the end of the frame instead.");
        }
        Window window = mc.getWindow();
        float far = ClientHooks.getGuiFarPlane();
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        VertexSorting sorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, (float) (window.getWidth() / window.getGuiScale()),
            (float) (window.getHeight() / window.getGuiScale()), 0.0F, 1000.0F, far), VertexSorting.ORTHOGRAPHIC_Z);
        Matrix4fStack view = RenderSystem.getModelViewStack();
        view.pushMatrix();
        view.translation(0.0F, 0.0F, 10000F - far);
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.clear(256, Minecraft.ON_OSX);
        try {
            GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
            rescue(graphics, mc.getTimer());
            notice(graphics, mc.getTimer());
        } finally {
            view.popMatrix();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setProjectionMatrix(projection, sorting);
        }
    }

    private static void rescue(GuiGraphics graphics, DeltaTracker delta) {
        reached = true;
        Minecraft mc = Minecraft.getInstance();
        if (!toldRescue) {
            toldRescue = true;
            Ton618Addition.LOG.info("TON 618 hid the game's HUD (its dark or a film): TON 618 Addition draws the H menu "
                + "over it{}.", inDark(mc) ? ", and the action bar in the dark" : "");
        }
        try {
            RenderGuiEvent.Pre calledOff = new RenderGuiEvent.Pre(graphics, delta);
            calledOff.setCanceled(true);
            NeoForge.EVENT_BUS.post(calledOff);
        } catch (RuntimeException failed) {
            warnOnce("the film's layer", failed);
        }
        if (inDark(mc) && !filmHidesHud() && !LastLightFilm.showingNow() && !QuasarJetsFilm.hasCamera()) {
            try {
                ((GuiAccessor) mc.gui).ton618Addition$renderOverlayMessage(graphics, delta);
            } catch (RuntimeException | LinkageError failed) {
                warnOnce("the action bar", failed);
            }
        }
        try {
            WhiteSpace.beforeHud();
            WhiteSpace.render(graphics, delta.getGameTimeDeltaPartialTick(true));
        } catch (RuntimeException | LinkageError failed) {
            warnOnce("RimuLib's menu", failed);
        }
        graphics.flush();
    }

    private static void notice(GuiGraphics graphics, DeltaTracker delta) {
        try {
            LastLightNotice.draw(graphics, delta.getGameTimeDeltaPartialTick(false));
            graphics.flush();
        } catch (RuntimeException failed) {
            warnOnce("Last Light's notice", failed);
        }
    }

    private static boolean inDark(Minecraft mc) {
        return mc.level != null && mc.level.dimension() == Ton618Link.DARK;
    }

    /** A RimuLib film hides the HUD (TON 618 cuts the HUD then too). RimuLib internals: false if they change. */
    private static boolean filmHidesHud() {
        try {
            return CutsceneDirector.hideHud();
        } catch (RuntimeException | LinkageError unsure) {
            return false;
        }
    }

    private static void warnOnce(String what, Throwable failed) {
        if (!warned) {
            warned = true;
            Ton618Addition.LOG.warn("Couldn't draw {} over TON 618's hidden HUD: {}", what, failed.toString());
        }
    }
}
