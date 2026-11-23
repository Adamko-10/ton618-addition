package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.mc1201.client.magic.WhiteSpace;
import cyou.rimuru.rimulib.mc1201.client.cinematic.CutsceneDirector;
import dev.adamko.ton618addition.link.Ton618Link;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
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
 *   <li>when {@code Gui.render} didn't reach its end this frame and the player is in the dark (not during a film):
 *       the action bar, so the reason a skill can't be cast there shows (a player's report, 2026-11-20: "the quasar
 *       addition doesnt work too": TA-02's "only in the overworld, at night", SS-21 Quasar's and RimuLib's own
 *       refusals all go to the action bar, which the dark hid);</li>
 *   <li>when {@code Gui.render} didn't reach its end this frame, RimuLib's menu is drawn as RimuLib would at the end of
 *       the HUD (RimuLib internals, the only way: if they change, a warning in the log and the menu stays hidden);</li>
 *   <li>every frame, the Last Light film's screen layer ({@code LastLightFilm.screen}: words, space scene, white).
 *       Forge 1.21.1 has no {@code RenderGuiEvent} (NeoForge's branch draws the film there), so it's drawn here;</li>
 *   <li>every frame, Last Light's fallback notice.</li>
 * </ol>
 * The rest of the vanilla HUD (hotbar, chat) stays hidden in the dark, as TON 618 wants. Driven by
 * {@code client/mixin/GuiMixin} and {@code GameRendererMixin}.
 */
public final class HudRescue {
    private static boolean reached;
    private static boolean warned;
    private static boolean toldRescue;

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
        Minecraft mc = Minecraft.getInstance();
        if (!toldRescue) {
            toldRescue = true;
            Ton618Addition.LOG.info("TON 618 hid the game's HUD (its dark or a film): TON 618 Addition draws the H menu "
                + "over it{}.", inDark(mc) ? ", and the action bar in the dark" : "");
        }
        if (inDark(mc) && !filmHidesHud() && !LastLightFilm.active() && !QuasarJetsFilm.active()) {
            try {
                // Forge 47 draws it as a HUD overlay (ForgeGui.renderRecordOverlay, protected): its public entry
                VanillaGuiOverlay.RECORD_OVERLAY.type().overlay().render((ForgeGui) mc.gui, graphics, partial,
                    graphics.guiWidth(), graphics.guiHeight());
            } catch (RuntimeException | LinkageError failed) {
                warnOnce("the action bar", failed);
            }
        }
        try {
            WhiteSpace.beforeHud();
            WhiteSpace.render(graphics, partial);
        } catch (RuntimeException | LinkageError failed) {
            warnOnce("RimuLib's menu", failed);
        }
        graphics.flush();
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
