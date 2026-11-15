package dev.adamko.ton618addition.client.quasarfilm;

import cyou.rimuru.rimulib.mc1211.client.api.FilmApi;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import dev.adamko.ton618addition.quasar.QuasarClock;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * <b>FABLE'S PACKAGE ({@code client/quasarfilm/}).</b> TA-02 · Quasar Jets' film. This is a <b>placeholder by Opus</b>
 * (letterbox bars, each shot's words, the title card and the blue-white flood, timed by {@link QuasarClock}) so the
 * skill can be tested before the real film exists; Fable replaces it freely, keeping the four entry points below
 * (CLAUDE.md, "For Fable: TA-02's film"). The world effects everyone sees (the second sun, the jet, the lobe) are not
 * the film's: they are {@code client/quasar/QuasarRender}'s.
 */
public final class QuasarJetsFilm {
    /** Shot words: from tick, to tick, caption, line under it (previz v1's shot table). */
    private static final Object[][] WORDS = {
        {6, 36, "QUASAR JETS", "strike point marked"},
        {42, 78, "%STAR%", "%TYPE%"},
        {86, 146, "THE STAR ENGINE", "a mirror to catch its own light"},
        {152, 196, "IT MOVES", "all its light now pours out behind it"},
        {206, 262, "ACROSS THE UNIVERSE", "18 billion light years to TON618"},
        {270, 326, "TON618", "66 billion Suns"},
        {336, 384, "CAUGHT", "the mirror falls in first"},
        {390, 436, "TIDAL DISRUPTION", "the star is pulled into a ribbon"},
        {534, 606, "THE JETS", "out of both poles, at nearly the speed of light"},
        {614, 662, "BLAZAR", "one jet swings to face Earth"},
        {692, 741, "SECOND SUN", "night turns to day"},
        {749, 826, "SWEPT ALONG", "the jet carries what it touches to the mark"},
        {834, 886, "RADIO LOBE", "a bubble of plasma where the jet ends"},
        {896, 926, "FROZEN LIGHT", "what is left of %STAR%"},
    };

    @Nullable
    private static QuasarCast cast;

    private QuasarJetsFilm() {
    }

    /** Once, from the mod's constructor, on clients only. */
    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event -> draw(event.getGuiGraphics(),
            event.getPartialTick().getGameTimeDeltaPartialTick(true)));
    }

    /** A strike reached this client and it gets the film (the caster and players within ~128 blocks). */
    public static void start(QuasarCast newCast) {
        cast = newCast;
    }

    /** Leaving the server, or the strike ended. */
    public static void stop() {
        cast = null;
    }

    public static boolean active() {
        return cast != null;
    }

    private static boolean filmsOn() {
        try {
            return FilmApi.automatic();
        } catch (RuntimeException | LinkageError unsure) {
            return true;
        }
    }

    private static void draw(GuiGraphics g, float partial) {
        QuasarCast now = cast;
        if (now == null || !filmsOn()) {
            return;
        }
        float t = now.tick(partial);
        if (t > QuasarClock.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        // the bars slide in (0 to 10) and away (938 to 958)
        float bars = Math.min(QuasarClock.ramp(t, 0, 10), 1f - QuasarClock.ramp(t, 938, 958));
        int bar = (int) (h * 0.11f * bars);
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
        String star = now.star().name().toUpperCase(java.util.Locale.ROOT);
        String type = now.star().type().label.toLowerCase(java.util.Locale.ROOT) + " · " + now.star().distance() + " away";
        int color = 0xFF000000 | now.star().type().color;
        for (Object[] word : WORDS) {
            int from = (Integer) word[0];
            int to = (Integer) word[1];
            if (t < from || t > to) {
                continue;
            }
            float a = Math.min(QuasarClock.ramp(t, from, from + 6), 1f - QuasarClock.ramp(t, to - 6, to));
            int alpha = Mth.clamp((int) (a * 255f), 4, 255) << 24;
            String title = ((String) word[2]).replace("%STAR%", star);
            String line = ((String) word[3]).replace("%STAR%", now.star().name()).replace("%TYPE%", type);
            g.drawCenteredString(font, title, w / 2, h - bar - 26, alpha | (color & 0xFFFFFF));
            g.drawCenteredString(font, line, w / 2, h - bar - 14, alpha | 0xD8E2EE);
        }
        if (t >= 452 && t <= 522) {
            float a = Math.min(QuasarClock.ramp(t, 452, 466), 1f - QuasarClock.ramp(t, 510, 522));
            int alpha = Mth.clamp((int) (a * 255f), 4, 255) << 24;
            g.drawCenteredString(font, "TON 618 ADDITION", w / 2, h / 2 - 14, alpha | 0x9AA6B5);
            g.pose().pushPose();
            g.pose().translate(w / 2f, h / 2f, 0);
            g.pose().scale(2f, 2f, 1f);
            g.drawCenteredString(font, "TA-02 · QUASAR JETS", 0, 0, alpha | 0xE6F0FF);
            g.pose().popPose();
        }
        // the blazar's blue-white flood (640 to 668), held to 684, clearing onto the second sun by 704
        float flood = t < QuasarClock.FLOOD + 3 ? QuasarClock.ramp(t, 640, 668) : 1f - QuasarClock.ramp(t, 684, 704);
        if (flood > 0.01f) {
            g.fill(0, 0, w, h, ((int) (Math.min(1f, flood) * 255f) << 24) | 0xE6F0FF);
        }
    }
}
