package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.client.cinematic.CutsceneDirector;
import dev.adamko.ton618addition.client.mixin.HudAccessor;
import dev.adamko.ton618addition.link.Ton618Link;
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
    private static boolean toldDark;

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
        darkActionBar(graphics, delta);
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

    /**
     * The action bar in TON 618's dark: on 26.3 TON 618 doesn't call the HUD off, it wraps the game's HUD elements
     * and hides them all there, the action bar too, so every "can't cast here" reason was hidden (Codex's review,
     * 2026-11-24). Drawn here once a frame; not during one of our films or a RimuLib film that hides the HUD, not
     * under F1.
     */
    private static void darkActionBar(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui.hud.isHidden() || mc.level == null || mc.level.dimension() != Ton618Link.DARK || filmHidesHud()
            || LastLightFilm.showingNow() || QuasarJetsFilm.hasCamera()) {
            return;
        }
        if (!toldDark) {
            toldDark = true;
            Ton618Addition.LOG.info("TON 618 hides the game's HUD in its dark: TON 618 Addition draws the action bar there.");
        }
        try {
            ((HudAccessor) mc.gui.hud).ton618Addition$extractOverlayMessage(graphics, delta);
        } catch (RuntimeException | LinkageError failed) {
            warnOnce("the action bar", failed);
        }
    }

    /** A RimuLib film hides the HUD (TON 618 hides the HUD then too). RimuLib internals: false if they change. */
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
            Ton618Addition.LOG.warn("Couldn't draw {}: {}", what, failed.toString());
        }
    }
}
