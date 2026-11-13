package dev.adamko.ton618addition.client.film;

import dev.adamko.ton618addition.client.LastLightCast;
import dev.adamko.ton618addition.lastlight.LastLightClock;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * <b>FABLE'S FILE.</b> The Last Light film. This package ({@code client/film}) and the film's shader, texture and sound
 * folders belong to the Fable session (CLAUDE.md, "For Fable: the film"): replace everything here freely.
 *
 * <p>What is here now is a <b>placeholder</b> by the Opus session, only so the skill can be tested before the real film
 * exists: bars, the year counter, the title, the white at the burst and the educational labels as plain text, timed
 * from the approved previz ({@code previz/NOTES.md}).
 *
 * <p><b>The contract</b> (what the rest of the mod calls; keep these four, or propose a change):
 * <ul>
 *   <li>{@link #init(IEventBus)}: once, from the mod's constructor on clients (the mod bus: register shaders, sounds,
 *       renderers here);</li>
 *   <li>{@link #start(LastLightCast)}: a cast reached this client (everyone in the dark gets it; a late arrival gets it
 *       mid-way: start from {@code cast.tick(...)});</li>
 *   <li>{@link #stop()}: cancel (the player left the server). Never called for the trip back to the overworld;</li>
 *   <li>{@link #active()}: the film is running.</li>
 * </ul>
 */
public final class LastLightFilm {
    @Nullable
    private static LastLightCast cast;

    private LastLightFilm() {
    }

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event -> draw(event.getGuiGraphics(),
            event.getPartialTick().getGameTimeDeltaPartialTick(false)));
    }

    public static void start(LastLightCast newCast) {
        cast = newCast;
    }

    public static void stop() {
        cast = null;
    }

    public static boolean active() {
        return cast != null;
    }

    // ---- the placeholder ------------------------------------------------------------------------------------------

    /** log10(years) at these ticks (previz/NOTES.md, "The numbers on screen"), straight lines between. */
    private static final float[][] YEARS = {
        {118, 0}, {159, 3.5F}, {200, 9}, {249, 11}, {270, 12}, {330, 12.6F}, {377, 14}, {440, 40}, {540, 90}, {593, 99},
        {625, 99.9F}, {680, 100}};

    /** The educational labels: from, to, text (previz/NOTES.md, "Educational labels"). */
    private static final Object[][] LABELS = {
        {10, 62, "A real black hole: 40 to 66 billion Suns."},
        {206, 324, "Hawking temperature: about 10^-18 K. Space is 2.7 K today, so it still grows. It shrinks only once space is colder."},
        {335, 382, "About 10^14 years: the last red dwarf stars burn out."},
        {388, 437, "Left over: white dwarfs, neutron stars, black holes."},
        {444, 538, "Hawking radiation: quantum particle pairs appear at the horizon. One falls in, one escapes, and the hole loses mass."},
        {544, 623, "Smaller means hotter means faster: temperature goes as 1 / mass."},
        {629, 676, "About 10^100 years. The end is a burst of gamma rays."},
        {735, 831, "The information paradox: does what fell in come back out in the radiation? Still debated. Here, it does."}};

    private static void draw(GuiGraphics g, float partial) {
        LastLightCast now = cast;
        if (now == null) {
            return;
        }
        float t = now.tick(partial);
        if (t > LastLightClock.END) {
            cast = null;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        // letterbox bars, in 0-13 and out 825-846
        float bars = Math.min(clamp(t / 13F), 1F - clamp((t - 825F) / 21F));
        int bar = Math.round(h * 0.11F * bars);
        g.fill(0, 0, w, bar, 0xFF000000);
        g.fill(0, h - bar, w, h, 0xFF000000);
        // the year counter, RACE to 730
        if (t >= LastLightClock.RACE && t < 730) {
            g.drawCenteredString(font, "YEAR " + years(t), w / 2, Math.max(4, bar / 2 - 4), 0xFFE8F2FF);
        }
        // the title, 132-197
        if (t >= 132 && t < 197) {
            g.drawCenteredString(font, "TON 618 ADDITION", w / 2, h / 2 - 12, 0xFFBFE0FF);
            g.drawCenteredString(font, "TA-01 · LAST LIGHT", w / 2, h / 2, 0xFFFFFFFF);
        }
        // the burst: white from 677, solid 681-720, gone by 735
        float white = Math.min(clamp((t - 677F) / 4F), 1F - clamp((t - LastLightClock.CLEAR) / 15F));
        if (white > 0F) {
            g.fill(0, 0, w, h, ((int) (white * 255F) << 24) | 0xFFFFFF);
        }
        if (now.educational()) {
            for (Object[] label : LABELS) {
                if (t >= ((Number) label[0]).floatValue() && t < ((Number) label[1]).floatValue()) {
                    g.drawWordWrap(font, net.minecraft.network.chat.Component.literal("PHYSICS: " + label[2]), 12,
                        h - bar - 34, w / 2, 0xFF9FD4FF);
                }
            }
        }
        g.drawString(font, "placeholder film (Fable's film replaces it)", 4, h - 10, 0x80FFFFFF);
    }

    private static String years(float t) {
        float log = YEARS[YEARS.length - 1][1];
        for (int i = 1; i < YEARS.length; i++) {
            if (t < YEARS[i][0]) {
                float u = (t - YEARS[i - 1][0]) / (YEARS[i][0] - YEARS[i - 1][0]);
                log = YEARS[i - 1][1] + (YEARS[i][1] - YEARS[i - 1][1]) * u;
                break;
            }
        }
        if (log < 6F) {
            return String.format("%,d", (long) Math.pow(10.0, log));
        }
        int exponent = (int) Math.floor(log);
        return String.format("%.1f × 10^%d", Math.pow(10.0, log - exponent), exponent);
    }

    private static float clamp(float v) {
        return v < 0F ? 0F : Math.min(v, 1F);
    }
}
