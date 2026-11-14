package dev.adamko.ton618addition.client.film;

import static dev.adamko.ton618addition.client.film.FilmScript.lin;
import static dev.adamko.ton618addition.client.film.FilmScript.ss;

import com.mojang.math.Axis;
import cyou.rimuru.rimulib.mc1211.client.api.UiApi;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

/**
 * <b>FABLE'S FILE.</b> The film's words and veils over the picture, as the previz lays them out: the letterbox bars,
 * the captions over the lower bar, the year counter with "1 SECOND = ... YEARS" under it, the readouts on the right,
 * the title card, the educational labels (only when the player turned them on), and the flicker, the flash cut and the
 * white of the burst. Drawn in RimuLib's overlay units (one size at any GUI scale; 710 high) with its lettering.
 */
final class FilmHud {
    private static final int INK = 0xECE5D8;
    private static final int DIM = 0x978D80;
    private static final int EMBER = 0xFFA24A;
    private static final int COLD = 0x9BDCFF;
    private static final int TITLE = 0xFFF0D6;
    private static final String TITLE_TEXT = "TA-01 · LAST LIGHT";

    private FilmHud() {
    }

    /** Only the white of the burst (for a player who has films off, or skipped this one): the trip back stays hidden. */
    static void white(GuiGraphics g, float gameTick) {
        // up over 677-681, solid until CLEAR, gone by 735
        float white = Math.min(lin(677F, 681F, gameTick), 1F - lin(720F, 735F, gameTick));
        if (white > 0.004F) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), argb(0xFFFFFF, white));
        }
    }

    /** Everything, at game tick {@code gameTick} (storyboard time {@code t}); {@code educational}: the labels too. */
    static void draw(GuiGraphics g, float gameTick, float t, boolean educational) {
        UiApi.overlay(g, () -> {
            float w = UiApi.width(g);
            float h = UiApi.height(g);
            float cq = w / 100F;

            // the light flickering as time starts to race, the flash of the cut into space, the white of the burst
            if (t > 92F && t < 150F) {
                box(g, 0F, 0F, w, h, 0x000000, 0.10F * lin(92F, 120F, t) * (0.5F + 0.5F * (float) Math.sin(t * t * 0.045)));
            }
            float flash = t < 146F ? 0F : t < 150F ? lin(146F, 150F, t) * 0.9F : 0.9F * (1F - lin(150F, 158F, t));
            box(g, 0F, 0F, w, h, 0xFFDFA8, flash);
            box(g, 0F, 0F, w, h, 0xFFFFFF, FilmScript.white(t));

            // the bars: in over the first ticks, away at the end
            float bars = ss(0F, 10F, t) * (1F - ss(636F, 656F, t));
            float bar = h * 0.11F;
            box(g, 0F, -bar * (1F - bars), w, bar, 0x000000, 1F);
            box(g, 0F, h - bar * bars, w, bar, 0x000000, 1F);

            if (educational) {
                labels(g, t, w, h, cq);
            }
            counters(g, gameTick, t, w, h, cq);
            readouts(g, t, w, h, cq);
            title(g, t, w, h, cq);
            caption(g, t, w, h, cq, bars);
        });
    }

    // ---- the year counter and how fast time runs ---------------------------------------------------------------------------

    private static void counters(GuiGraphics g, float gameTick, float t, float w, float h, float cq) {
        float shown = lin(90F, 96F, t) * (1F - lin(536F, 548F, t));
        if (shown > 0.01F) {
            float em = 1.55F * cq;
            String number = t >= FilmScript.S_BURST ? "10^{100}" : FilmScript.years(FilmScript.logYears(t));
            float labelEm = em * 0.68F;
            float labelWidth = trackedWidth(g, UiApi.Font.LABEL, "YEARS", labelEm, labelEm * 0.34F);
            float gap = labelEm * 1.1F;
            float total = labelWidth + gap + rich(g, UiApi.Font.MONO, number, 0F, 0F, em, INK, 0F);
            float x = (w - total) * 0.5F;
            float base = h * 0.022F + em * 0.85F;
            tracked(g, UiApi.Font.LABEL, "YEARS", x + labelWidth * 0.5F, base, labelEm, labelEm * 0.34F, DIM, shown, -1F, 0F);
            rich(g, UiApi.Font.MONO, number, x + labelWidth + gap, base, em, INK, shown);
        }
        // the plain-words sign that time has sped up: how many years go by in one second of the film
        float rate = lin(92F, 98F, t) * (1F - lin(486F, 490F, t));
        if (rate > 0.01F) {
            double perSecond = (Math.pow(10.0, FilmScript.logYears(FilmScript.toSb(gameTick + 0.5F)))
                - Math.pow(10.0, FilmScript.logYears(FilmScript.toSb(gameTick - 0.5F)))) * 20.0;
            String years = perSecond > 0.0 ? FilmScript.years((float) Math.log10(Math.max(perSecond, 1.0))) : "0";
            float em = 0.88F * cq;
            String text = "1 SECOND = " + years + " YEARS";
            float width = rich(g, UiApi.Font.MONO, text, 0F, 0F, em, EMBER, 0F);
            rich(g, UiApi.Font.MONO, text, (w - width) * 0.5F, h * 0.069F + em * 0.85F, em, EMBER, rate);
        }
    }

    // ---- the readouts on the right -----------------------------------------------------------------------------------------

    private static void readouts(GuiGraphics g, float t, float w, float h, float cq) {
        float em = 1.05F * cq;
        float right = w * 0.97F;
        float top = h * 0.15F + em * 0.85F;
        float line = em * 1.75F;
        if (t >= 152F && t < 300F) {
            float shown = Math.min(lin(152F, 158F, t), 1F - lin(290F, 298F, t));
            float space = FilmScript.logSpaceK(t);
            String spaceText = t < 152.5F ? "2.7 K" : space < -29.9F ? "< 10^{-30} K" : FilmScript.sci(space) + " K";
            readout(g, "SPACE", spaceText, right, top, em, INK, shown);
            readout(g, "TON618", FilmScript.sci(FilmScript.logHoleK(t)) + " K", right, top + line, em, INK, shown);
            if (space < FilmScript.logHoleK(t)) {
                String note = "space is colder: it shrinks";
                float width = rich(g, UiApi.Font.MONO, note, 0F, 0F, em, EMBER, 0F);
                rich(g, UiApi.Font.MONO, note, right - width, top + line * 2F, em, EMBER, shown);
            }
        } else if (t >= 304F && t < 501F) {
            float shown = lin(304F, 310F, t);
            float mass = FilmScript.logMassKg(t);
            String massText = mass > FilmScript.LOG_SUN ? FilmScript.sci(mass - FilmScript.LOG_SUN) + " Suns"
                : mass > 0.5F ? FilmScript.sci(mass) + " kg" : "0";
            readout(g, "MASS", massText, right, top, em, INK, shown);
            readout(g, "TEMP", mass > 0.5F ? FilmScript.sci(FilmScript.logHoleK(t)) + " K" : "—", right, top + line, em,
                t >= 390F ? EMBER : INK, shown);
        }
    }

    /** A small dim label and its value, the value ending at {@code right}. */
    private static void readout(GuiGraphics g, String label, String value, float right, float base, float em, int rgb, float alpha) {
        float valueWidth = rich(g, UiApi.Font.MONO, value, 0F, 0F, em, rgb, 0F);
        rich(g, UiApi.Font.MONO, value, right - valueWidth, base, em, rgb, alpha);
        float labelEm = em * 0.78F;
        float labelWidth = trackedWidth(g, UiApi.Font.LABEL, label, labelEm, labelEm * 0.22F);
        tracked(g, UiApi.Font.LABEL, label, right - valueWidth - em * 0.9F - labelWidth * 0.5F, base, labelEm, labelEm * 0.22F, DIM, alpha,
            -1F, 0F);
    }

    // ---- the title card, inside the hole's shadow ---------------------------------------------------------------------------

    private static void title(GuiGraphics g, float t, float w, float h, float cq) {
        if (t < 100F || t > 148F) {
            return;
        }
        float out = 1F - lin(140F, 148F, t);
        float smallEm = 0.82F * cq;
        float smallBase = h * 0.43F + smallEm * 0.85F;
        tracked(g, UiApi.Font.LABEL, "TON 618 ADDITION", w * 0.5F, smallBase, smallEm, smallEm * 0.9F, DIM, out * lin(100F, 106F, t), -1F, 0F);
        float em = 2.9F * cq;
        // letter by letter
        tracked(g, UiApi.Font.DISPLAY, TITLE_TEXT, w * 0.5F, smallBase + 1.3F * cq + em * 0.85F, em, em * 0.28F, TITLE, out, t - 102F, 1.1F);
    }

    // ---- the caption over the lower bar --------------------------------------------------------------------------------------

    private static void caption(GuiGraphics g, float t, float w, float h, float cq, float bars) {
        for (FilmScript.Caption caption : FilmScript.CAPTIONS) {
            if (t >= caption.from() && t <= caption.to()) {
                float shown = Math.min(lin(caption.from(), caption.from() + 4F, t), 1F - lin(caption.to() - 4F, caption.to(), t)) * bars;
                float em = 1.2F * cq;
                tracked(g, UiApi.Font.LABEL, caption.title(), w * 0.5F, h * 0.884F + em * 0.85F, em, em * 0.42F, EMBER, shown, -1F, 0F);
                float detailEm = 1.02F * cq;
                float width = rich(g, UiApi.Font.MONO, caption.detail(), 0F, 0F, detailEm, INK, 0F);
                rich(g, UiApi.Font.MONO, caption.detail(), (w - width) * 0.5F, h * 0.933F + detailEm * 0.85F, detailEm, INK, shown * 0.8F);
                return;
            }
        }
    }

    // ---- the educational labels ----------------------------------------------------------------------------------------------

    private static void labels(GuiGraphics g, float t, float w, float h, float cq) {
        for (FilmScript.Label label : FilmScript.LABELS) {
            if (t < label.from() || t > label.to()) {
                continue;
            }
            float shown = Math.min(lin(label.from(), label.from() + 4F, t), 1F - lin(label.to() - 4F, label.to(), t));
            if (shown < 0.01F) {
                continue;
            }
            float[] anchor = label.anchor() == FilmScript.Anchor.HOLE ? FilmSpace.anchor(label.reach()) : new float[] {label.x(), label.y()};
            float ax = anchor[0] * w / 100F;
            float ay = anchor[1] * h / 100F;
            float em = 1.08F * cq;
            float pad = 0.8F * cq;
            List<String> lines = wrap(g, label.text(), em, w * 0.31F - pad * 2F);
            float textWidth = 0F;
            for (String line : lines) {
                textWidth = Math.max(textWidth, rich(g, UiApi.Font.MONO, line, 0F, 0F, em, COLD, 0F));
            }
            float headEm = em * 0.68F;
            float boxW = textWidth + pad * 2F;
            float boxH = pad * 1.2F + headEm * 1.5F + lines.size() * em * 1.45F;
            float offX = label.offX() * w / 100F;
            float x = offX >= 0F ? ax + offX : ax + offX - boxW;
            x = Math.max(w * 0.02F, Math.min(x, w * 0.98F - boxW));
            float y = ay + label.offY() * h / 100F - boxH * 0.5F;
            y = Math.max(h * 0.13F, Math.min(y, h * 0.86F - boxH));
            // the pointer: a thin line from what it is about to the box, a small mark on the spot
            float toX = x + boxW * 0.5F < ax ? x + boxW : x;
            float toY = y + boxH * 0.5F;
            line(g, ax, ay, toX, toY, COLD, shown * 0.7F);
            box(g, ax - 0.3F * cq, ay - 0.3F * cq, 0.6F * cq, 0.6F * cq, COLD, shown);
            box(g, x, y, boxW, boxH, 0x04080C, shown * 0.78F);
            box(g, x, y, 2F, boxH, COLD, shown);
            tracked(g, UiApi.Font.LABEL, "PHYSICS", x + pad + trackedWidth(g, UiApi.Font.LABEL, "PHYSICS", headEm, headEm * 0.34F) * 0.5F,
                y + pad * 0.6F + headEm, headEm, headEm * 0.34F, COLD, shown * 0.65F, -1F, 0F);
            float base = y + pad * 0.6F + headEm * 1.5F + em;
            for (String line : lines) {
                rich(g, UiApi.Font.MONO, line, x + pad, base, em, COLD, shown);
                base += em * 1.45F;
            }
        }
    }

    private static List<String> wrap(GuiGraphics g, String text, float em, float max) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String tried = line.isEmpty() ? word : line + " " + word;
            if (!line.isEmpty() && rich(g, UiApi.Font.MONO, tried, 0F, 0F, em, COLD, 0F) > max) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(tried);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    // ---- drawing ---------------------------------------------------------------------------------------------------------------

    private static int argb(int rgb, float alpha) {
        return Math.round(FilmScript.sat(alpha) * 255F) << 24 | rgb & 0xFFFFFF;
    }

    private static void box(GuiGraphics g, float x, float y, float w, float h, int rgb, float alpha) {
        if (alpha > 0.004F) {
            UiApi.box(g, x, y, w, h, argb(rgb, alpha));
        }
    }

    private static void line(GuiGraphics g, float x0, float y0, float x1, float y1, int rgb, float alpha) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length < 1F || alpha <= 0.004F) {
            return;
        }
        g.pose().pushPose();
        g.pose().translate(x0, y0, 0F);
        g.pose().mulPose(Axis.ZP.rotation((float) Math.atan2(dy, dx)));
        UiApi.box(g, 0F, -0.6F, length, 1.2F, argb(rgb, alpha));
        g.pose().popPose();
    }

    /** The lettering's size at scale 1 (RimuLib's faces). */
    private static float base(UiApi.Font font) {
        return font == UiApi.Font.DISPLAY ? 16F : 9.5F;
    }

    /**
     * Text with the film's raised powers ({@code 10^{14}}), its left end at {@code x}, on the baseline {@code y},
     * {@code em} high. Returns its width; with {@code alpha} 0 it only measures.
     */
    private static float rich(GuiGraphics g, UiApi.Font font, String text, float x, float y, float em, int rgb, float alpha) {
        float scale = em / base(font);
        boolean draw = alpha > 0.02F;
        int colour = argb(rgb, alpha);
        float width = 0F;
        int from = 0;
        while (from < text.length()) {
            int mark = text.indexOf("^{", from);
            String plain = mark < 0 ? text.substring(from) : text.substring(from, mark);
            if (!plain.isEmpty()) {
                if (draw) {
                    UiApi.text(g, font, plain, x + width, y, 0F, scale, colour);
                }
                width += UiApi.measure(g, font, plain, scale);
            }
            if (mark < 0) {
                break;
            }
            int end = text.indexOf('}', mark);
            if (end < 0) {
                break;
            }
            String raised = text.substring(mark + 2, end);
            if (draw) {
                UiApi.text(g, font, raised, x + width + em * 0.04F, y - em * 0.36F, 0F, scale * 0.62F, colour);
            }
            width += UiApi.measure(g, font, raised, scale * 0.62F) + em * 0.08F;
            from = end + 1;
        }
        return width;
    }

    private static float trackedWidth(GuiGraphics g, UiApi.Font font, String text, float em, float tracking) {
        float scale = em / base(font);
        float width = 0F;
        for (int i = 0; i < text.length(); i++) {
            width += UiApi.measure(g, font, text.substring(i, i + 1), scale) + (i > 0 ? tracking : 0F);
        }
        return width;
    }

    /**
     * Letters set wide apart, centred on {@code cx}, on the baseline {@code y}. With {@code per} above 0 they come in
     * one after another: letter i is fully in {@code per} x i + 5 ticks after {@code clock} passes 0.
     */
    private static void tracked(GuiGraphics g, UiApi.Font font, String text, float cx, float y, float em, float tracking, int rgb,
            float alpha, float clock, float per) {
        if (alpha <= 0.02F) {
            return;
        }
        float scale = em / base(font);
        float x = cx - trackedWidth(g, font, text, em, tracking) * 0.5F;
        for (int i = 0; i < text.length(); i++) {
            String letter = text.substring(i, i + 1);
            float a = per > 0F ? alpha * lin(i * per, i * per + 5F, clock) : alpha;
            if (a > 0.02F && !letter.isBlank()) {
                UiApi.text(g, font, letter, x, y, 0F, scale, argb(rgb, a));
            }
            x += UiApi.measure(g, font, letter, scale) + tracking;
        }
    }
}
