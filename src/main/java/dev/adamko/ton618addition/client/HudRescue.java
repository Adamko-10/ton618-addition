package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.mc1201.client.magic.WhiteSpace;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.client.gui.GuiGraphics;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.client.quasar.QuasarClient;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;

/**
 * Puts back what TON 618 hides with the HUD (Adam's test, 2026-11-14: "when ton 618 eats your world you cant open the
 * ui you just dont see it").
 *
 * <p>TON 618 1.1.0 cancels the whole of {@code Gui.render} at its start whenever the player is in its dark
 * ({@code ton618:devoured}) or a RimuLib film hides the HUD, and draws only its own overlay. That also skips what is
 * drawn at the end of {@code Gui.render}: RimuLib's H menu ({@code WhiteSpace.render}). So right after the HUD call
 * (the same layer, before screens):
 * <ol>
 *   <li>when {@code Gui.render} didn't reach its end this frame, RimuLib's menu is drawn as RimuLib would at the end of
 *       the HUD (RimuLib internals, the only way: if they change, a warning in the log and the menu stays hidden);</li>
 *   <li>every frame, the Last Light film's screen layer ({@code LastLightFilm.screen}: words, space scene, white).
 *       Forge 1.21.1 has no {@code RenderGuiEvent} (NeoForge's branch draws the film there), so it's drawn here;</li>
 *   <li>every frame, Last Light's fallback notice.</li>
 * </ol>
 * The vanilla HUD (hotbar, chat) stays hidden in the dark, as TON 618 wants. Driven by {@code client/mixin/GuiMixin}
 * and {@code GameRendererMixin}.
 */
public final class HudRescue {
    private static boolean reached;
    private static boolean warned;

    private HudRescue() {
    }

    /** Just before the game draws its HUD. */
    public static void beforeGui() {
        reached = false;
    }

    /** {@code Gui.render} reached its end: nothing was cancelled. */
    public static void guiReachedEnd() {
        reached = true;
    }

    /**
     * Just after the game's HUD call: if it was cut short, draw what it skipped; then, every frame, Last Light's
     * fallback notice ({@link LastLightNotice}: only while it runs without its film).
     */
    public static void afterGui(GuiGraphics graphics, float partial) {
        boolean calledOff = !reached;
        if (calledOff) {
            rescue(graphics, partial);
        }
        // Forge 1.21.1 has no RenderGuiEvent (its HUD is layers): the film's screen layer is drawn from here, every
        // frame, whether or not TON 618 hid the HUD
        try {
            LastLightFilm.screen(graphics, partial);
            graphics.flush();
        } catch (RuntimeException failed) {
            warnOnce("the film's layer", failed);
        }
        // TA-02 · Quasar Jets: its film's screen layer every frame (told when the HUD was called off, as NeoForge's
        // cancelled RenderGuiEvent.Pre does), and its reticle and readout only when the HUD was drawn
        try {
            QuasarJetsFilm.screen(graphics, partial, calledOff);
            if (!calledOff) {
                QuasarClient.hud(graphics, partial);
            }
            graphics.flush();
        } catch (RuntimeException failed) {
            warnOnce("Quasar Jets' layer", failed);
        }
        try {
            LastLightNotice.draw(graphics, partial);
            graphics.flush();
        } catch (RuntimeException failed) {
            warnOnce("Last Light's notice", failed);
        }
    }

    private static void rescue(GuiGraphics graphics, float partial) {
        reached = true;
        try {
            WhiteSpace.beforeHud();
            WhiteSpace.render(graphics, partial);
        } catch (RuntimeException | LinkageError failed) {
            warnOnce("RimuLib's menu", failed);
        }
        graphics.flush();
    }

    private static void warnOnce(String what, Throwable failed) {
        if (!warned) {
            warned = true;
            Ton618Addition.LOG.warn("Couldn't draw {} over TON 618's hidden HUD: {}", what, failed.toString());
        }
    }
}
