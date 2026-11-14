package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.mc1211.client.magic.WhiteSpace;
import dev.adamko.ton618addition.Ton618Addition;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

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
 *   <li>RimuLib's menu is drawn as RimuLib would at the end of the HUD (RimuLib internals, the only way: if they
 *       change, a warning in the log and the menu stays hidden).</li>
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

    /** Just after the game's HUD call: if it was cut short, draw what it skipped. */
    public static void afterGui(GuiGraphics graphics, DeltaTracker delta) {
        if (reached) {
            return;
        }
        reached = true;
        try {
            RenderGuiEvent.Pre calledOff = new RenderGuiEvent.Pre(graphics, delta);
            calledOff.setCanceled(true);
            NeoForge.EVENT_BUS.post(calledOff);
        } catch (RuntimeException failed) {
            warnOnce("the film's layer", failed);
        }
        try {
            WhiteSpace.beforeHud();
            WhiteSpace.render(graphics, delta.getGameTimeDeltaPartialTick(true));
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
