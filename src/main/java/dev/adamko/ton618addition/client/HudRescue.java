package dev.adamko.ton618addition.client;

import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.film.LastLightFilm;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import dev.adamko.ton618addition.client.quasar.QuasarClient;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;

/**
 * The film's screen layer and Last Light's fallback notice, drawn right after the HUD (Fabric 26.3 branch).
 *
 * <p>On the other branches this class puts back what TON 618 hides when it cancels the HUD in its dark (RimuLib's H
 * menu, the film's layer). TON 618's 26.3 build has no such HUD mixin, and RimuLib 26.3 draws its menu as a Fabric HUD
 * element and never hides the HUD, so here there is nothing to rescue. What is left: drawing the film's words, space
 * scene and white, and the notice, after the HUD in every frame, also when the player hid the HUD (F1), as the other
 * branches do. {@code client/mixin/GuiMixin} calls {@link #afterHud} for that; if that optional mixin didn't apply,
 * a Fabric HUD element of our own draws them instead (then F1 hides them).
 */
public final class HudRescue {
    private static boolean viaMixin;
    private static boolean warned;

    private HudRescue() {
    }

    /** From the client entrypoint. */
    public static void register() {
        HudElementRegistry.addLast(Ton618Addition.id("last_light"), (graphics, delta) -> {
            if (!viaMixin) {
                draw(graphics, delta);
            }
        });
    }

    /** {@code GuiMixin} is working: the HUD element stands down. */
    public static void viaMixin() {
        viaMixin = true;
    }

    /** From {@code GuiMixin}, right after the HUD. */
    public static void afterHud(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        draw(graphics, delta);
    }

    private static void draw(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        float partial = delta.getGameTimeDeltaPartialTick(false);
        try {
            LastLightFilm.screen(graphics, partial);
        } catch (RuntimeException failed) {
            warnOnce("the film's layer", failed);
        }
        // TA-02 · Quasar Jets: its film's screen layer every frame, and its reticle and readout only when the HUD is shown.
        // On 26.3 nothing calls the HUD off (TON 618 and RimuLib leave it alone): only F1 hides it
        boolean calledOff = Minecraft.getInstance().gui.hud.isHidden();
        float gamePartial = delta.getGameTimeDeltaPartialTick(true);
        try {
            QuasarJetsFilm.screen(graphics, gamePartial, calledOff);
            if (!calledOff) {
                QuasarClient.hud(graphics, gamePartial);
            }
        } catch (RuntimeException failed) {
            warnOnce("Quasar Jets' layer", failed);
        }
        try {
            LastLightNotice.draw(graphics, partial);
        } catch (RuntimeException failed) {
            warnOnce("Last Light's notice", failed);
        }
    }

    private static void warnOnce(String what, Throwable failed) {
        if (!warned) {
            warned = true;
            Ton618Addition.LOG.warn("Couldn't draw {}: {}", what, failed.toString());
        }
    }
}
