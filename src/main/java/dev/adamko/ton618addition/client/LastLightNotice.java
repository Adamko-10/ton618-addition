package dev.adamko.ton618addition.client;

import cyou.rimuru.rimulib.client.api.FilmApi;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * What a player sees while Last Light runs <b>without its film</b> (Adam's test, 2026-11-14: "i wasnt able to cast
 * it": the cast had worked, but nothing showed). That happens when RimuLib's films are off (its film key J toggles
 * them, silently in TON 618's dark, which hides the action bar), when the film was skipped, or while it waits behind
 * another film. A small card at the top: the skill, how long until the world comes back, and why there's no film.
 * Drawn after the HUD by {@link HudRescue}, so it shows in the dark too. Not the film (Fable's): only a fallback.
 */
public final class LastLightNotice {
    private static final int PLATE = 0xE0101418;
    private static final int TITLE = 0xFFBFE0FF;
    private static final int TEXT = 0xFFE8EEF6;
    private static final int DIM = 0xFF9AA6B5;

    private LastLightNotice() {
    }

    static void draw(GuiGraphicsExtractor g, float partial) {
        LastLightCast cast = Ton618AdditionClient.current();
        if (cast == null) {
            return;
        }
        float tick = cast.tick(partial);
        boolean filmsOn;
        boolean filmShowing;
        try {
            filmsOn = FilmApi.automatic();
            filmShowing = FilmApi.active();
        } catch (RuntimeException | LinkageError unsure) {
            return;
        }
        if (filmShowing || tick >= LastLightClock.BURST - 5) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int seconds = Math.max(0, Mth.ceil((LastLightClock.RESTORE - tick) / 20.0F));
        Component title = Component.translatable("gui.ton618_addition.notice.title").withStyle(ChatFormatting.BOLD);
        Component line = Component.translatable("gui.ton618_addition.notice.countdown", seconds);
        Component why = Component.translatable(filmsOn ? "gui.ton618_addition.notice.no_film" : "gui.ton618_addition.notice.films_off");
        int w = Math.max(font.width(title), Math.max(font.width(line), font.width(why))) + 20;
        int h = 42;
        float scale = Math.max(Mth.clamp(g.guiWidth() / 640.0F, 0.75F, 1.25F), 1.0F / (float) minecraft.getWindow().getGuiScale());
        g.pose().pushMatrix();
        g.pose().translate(g.guiWidth() / 2.0F - w * scale / 2.0F, g.guiHeight() * 0.16F);
        g.pose().scale(scale, scale);
        g.fill(1, 0, w - 1, h, PLATE);
        g.fill(0, 1, 1, h - 1, PLATE);
        g.fill(w - 1, 1, w, h - 1, PLATE);
        g.fill(0, 0, 3, h, TITLE);
        g.text(font, title, (w - font.width(title)) / 2, 6, TITLE, false);
        g.text(font, line, (w - font.width(line)) / 2, 18, TEXT, false);
        g.text(font, why, (w - font.width(why)) / 2, 29, DIM, false);
        g.pose().popMatrix();
    }
}
