package dev.adamko.ton618addition.client.quasarfilm;

import static dev.adamko.ton618addition.client.quasarfilm.JetScript.kick;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lerp;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.lin;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.sat;
import static dev.adamko.ton618addition.client.quasarfilm.JetScript.ss;

import com.mojang.math.Axis;
import cyou.rimuru.rimulib.mc1211.client.api.UiApi;
import dev.adamko.ton618addition.client.quasar.QuasarCast;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * <b>FABLE'S FILE.</b> The film's words and veils over the picture, as the previz lays them out: the letterbox bars,
 * the captions over the lower bar, the reticle closing on the locked star (and, at the end, on the gap it left), the
 * year counter with "1 SECOND = ... YEARS" under it, the readouts on the right, the title card, the educational labels
 * (only when the player turned them on), the flashes of the cuts and the blue-white flood. Drawn in RimuLib's overlay
 * units (one size at any GUI scale) with its lettering, like Last Light's.
 */
final class JetHud {
    private static final int INK = 0xE6EBF2;
    private static final int DIM = 0x8590A3;
    private static final int COLD = 0x8FF0E0;
    private static final int JET = 0xCFE6FF;
    private static final int FLOOD = 0xEEF6FF;
    private static final int TITLE = 0xD6E9FF;
    private static final String TITLE_TEXT = "TA-02 · QUASAR JETS";

    /** The world camera of this frame (kept by the film from the level's own drawing), for things placed in the world. */
    private static final Matrix4f WORLD_VIEW = new Matrix4f();
    private static final Matrix4f WORLD_PROJ = new Matrix4f();
    private static Vec3 worldEye = Vec3.ZERO;
    private static boolean worldKnown;

    private JetHud() {
    }

    static void world(Matrix4f view, Matrix4f projection, Vec3 eye) {
        WORLD_VIEW.set(view);
        WORLD_PROJ.set(projection);
        worldEye = eye;
        worldKnown = true;
    }

    static void forget() {
        worldKnown = false;
    }

    /** Everything, at tick {@code t}; {@code popFlash}: the lobe's pop flashes the picture (nobody else draws it this frame). */
    static void draw(GuiGraphics g, float t, QuasarCast cast, boolean popFlash) {
        QuasarStars.Star star = cast.star();
        int hot = star.type().color;
        UiApi.overlay(g, () -> {
            float w = UiApi.width(g);
            float h = UiApi.height(g);
            float cq = w / 100F;

            // the flashes of the cuts (softer for players who asked the game to hide lightning flashes), then the flood
            float soften = Minecraft.getInstance().options.hideLightningFlash().get() ? 0.35F : 1F;
            if (t >= 70F && t < 92F) {
                float u = lin(70F, 80F, t);
                box(g, 0F, 0F, w, h, mix(hot, 0xFFFFFF, 0.35F), (t < 80F ? u * u : 0.95F * (1F - lin(80F, 92F, t))));
            } else if (t >= 326F && t < 336F) {
                box(g, 0F, 0F, w, h, JET, 0.5F * (1F - Math.abs(t - 330F) / 5F) * soften);
            } else if (t >= 438F && t < 456F) {
                box(g, 0F, 0F, w, h, JET, (t < 441F ? lin(438F, 441F, t) * 0.6F : 0.6F * (1F - lin(441F, 456F, t))) * soften);
            } else if (popFlash && t >= 889F && t < 906F) {
                box(g, 0F, 0F, w, h, mix(hot, 0xFFFFFF, 0.5F), 0.8F * (1F - lin(889F, 906F, t)) * soften);
            }
            box(g, 0F, 0F, w, h, FLOOD, JetScript.white(t));

            // the bars: in over the first ticks, away at the end
            float bars = ss(0F, 10F, t) * (1F - ss(940F, 958F, t));
            float bar = h * 0.11F;
            box(g, 0F, -bar * (1F - bars), w, bar, 0x000000, 1F);
            box(g, 0F, h - bar * bars, w, bar, 0x000000, 1F);

            reticle(g, t, star, w, h, cq);
            if (cast.educational()) {
                labels(g, t, cast, w, h, cq);
            }
            counters(g, t, hot, w, h, cq);
            readouts(g, t, star, w, h, cq);
            title(g, t, w, h, cq);
            caption(g, t, star, w, h, cq, bars);
        });
    }

    // ---- the reticle: on the star in shot 1, on the gap it left at the end ------------------------------------------------

    private static void reticle(GuiGraphics g, float t, QuasarStars.Star star, float w, float h, float cq) {
        float shown;
        float size;
        String name = star.name().toUpperCase(Locale.ROOT);
        String small;
        if (t < 80F) {
            shown = ss(24F, 34F, t) * (1F - lin(68F, 77F, t));
            size = lerp(17F, 4.6F, ss(28F, 46F, t)) + 1.4F * kick(t, 46F, 3F);
            small = t >= 40F ? "LOCKED" : "ACQUIRING";
            if (t < 40F) {
                name = "";
            }
        } else if (t >= 930F) {
            shown = ss(932F, 942F, t) * (1F - lin(952F, 958F, t)) * 0.85F;
            size = 4.6F;
            small = "MISSING · gone from the sky for a few nights";
        } else {
            return;
        }
        float[] at = shown > 0.01F ? worldDirection(star.direction()) : null;
        if (at == null) {
            return;
        }
        float x = at[0] * w / 100F;
        float y = at[1] * h / 100F;
        float half = size * cq * 0.5F;
        float arm = size * cq * 0.28F;
        int rgb = star.type().color;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                box(g, sx < 0 ? x - half : x + half - arm, sy < 0 ? y - half : y + half - 2F, arm, 2F, rgb, shown);
                box(g, sx < 0 ? x - half : x + half - 2F, sy < 0 ? y - half : y + half - arm, 2F, arm, rgb, shown);
            }
        }
        float em = 1.05F * cq;
        float base = y + half + 0.8F * cq + em * 0.85F;
        if (!name.isEmpty()) {
            tracked(g, UiApi.Font.MONO, name, x, base, em, em * 0.2F, rgb, shown, -1F, 0F);
            base += em * 1.25F;
        }
        float smallEm = em * 0.8F;
        tracked(g, UiApi.Font.MONO, small, x, base, smallEm, smallEm * 0.12F, INK, shown, -1F, 0F);
    }

    // ---- the year counter and how fast time runs ---------------------------------------------------------------------------

    private static void counters(GuiGraphics g, float t, int hot, float w, float h, float cq) {
        float shown = lin(150F, 156F, t) * (1F - lin(332F, 346F, t));
        if (shown > 0.01F) {
            float em = 1.55F * cq;
            String number = JetScript.years(JetScript.logYears(t));
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
        float rate = lin(154F, 160F, t) * (1F - lin(326F, 334F, t));
        if (rate > 0.01F) {
            double perSecond = (Math.pow(10.0, JetScript.logYears(t + 0.5F)) - Math.pow(10.0, JetScript.logYears(t - 0.5F))) * 20.0;
            String years = perSecond > 0.0 ? JetScript.years((float) Math.log10(Math.max(perSecond, 1.0))) : "0";
            float em = 0.88F * cq;
            String text = "1 SECOND = " + years + " YEARS";
            float width = rich(g, UiApi.Font.MONO, text, 0F, 0F, em, hot, 0F);
            rich(g, UiApi.Font.MONO, text, (w - width) * 0.5F, h * 0.069F + em * 0.85F, em, hot, rate);
        }
    }

    // ---- the readouts on the right -----------------------------------------------------------------------------------------

    private static void readouts(GuiGraphics g, float t, QuasarStars.Star star, float w, float h, float cq) {
        float em = 1.05F * cq;
        float right = w * 0.97F;
        float top = h * 0.15F + em * 0.85F;
        float line = em * 1.75F;
        int hot = star.type().color;
        if (t >= 42F && t < 98F) {
            float shown = Math.min(lin(42F, 48F, t), 1F - lin(92F, 98F, t));
            readout(g, "TYPE", star.type().label.toUpperCase(Locale.ROOT), right, top, em, hot, shown);
            readout(g, "DISTANCE", star.distance(), right, top + line, em, INK, shown);
            readout(g, "SIZE", star.size(), right, top + line * 2F, em, INK, shown);
            readout(g, "MASS", star.mass(), right, top + line * 3F, em, INK, shown);
        } else if (t >= 204F && t < 330F) {
            float shown = Math.min(lin(204F, 210F, t), 1F - lin(324F, 330F, t));
            readout(g, "TO TON618", JetScript.lightYears(JetScript.distanceLy(t)) + " light years", right, top, em, INK, shown);
        } else if (t >= 352F && t < 438F) {
            float shown = Math.min(lin(352F, 358F, t), 1F - lin(432F, 438F, t));
            float mass = JetScript.mass(star);
            String fed = String.format(Locale.ROOT, mass < 2F ? "%.2f Suns" : "%.1f Suns", mass * sat((t - 384F) / 66F));
            readout(g, "FED TO TON618", fed, right, top, em, hot, shown);
            readout(g, "OF", star.mass(), right, top + line, em, INK, shown);
        } else if (t >= 446F && t < 528F) {
            float shown = Math.min(lin(446F, 452F, t), 1F - lin(522F, 528F, t));
            readout(g, "BRIGHTNESS", "140 trillion Suns", right, top, em, hot, shown);
        } else if (t >= 534F && t < 662F) {
            float shown = Math.min(lin(534F, 540F, t), 1F - lin(654F, 662F, t));
            float grown = lin(530F, 600F, t);
            readout(g, "JET SPEED", "99.9% of light", right, top, em, hot, shown);
            readout(g, "LENGTH", String.format(Locale.ROOT, "%,d light years", Math.round(500F * grown * grown) * 1000), right,
                top + line, em, INK, shown);
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

    // ---- the title card, under the lit quasar ----------------------------------------------------------------------------------

    private static void title(GuiGraphics g, float t, float w, float h, float cq) {
        if (t < 452F || t > 522F) {
            return;
        }
        float out = 1F - lin(512F, 522F, t);
        float smallEm = 0.82F * cq;
        float smallBase = h * 0.63F + smallEm * 0.85F;
        float eyebrow = out * lin(452F, 458F, t);
        tracked(g, UiApi.Font.LABEL, "TON 618 ADDITION", w * 0.5F + 0.1F * cq, smallBase + 0.1F * cq, smallEm, smallEm * 0.9F, 0x000000, eyebrow * 0.8F, -1F, 0F);
        tracked(g, UiApi.Font.LABEL, "TON 618 ADDITION", w * 0.5F, smallBase, smallEm, smallEm * 0.9F, INK, eyebrow, -1F, 0F);
        float em = 2.9F * cq;
        float base = smallBase + 1.3F * cq + em * 0.85F;
        // letter by letter, over its own shadow so it reads against the disc
        tracked(g, UiApi.Font.DISPLAY, TITLE_TEXT, w * 0.5F + 0.16F * cq, base + 0.16F * cq, em, em * 0.28F, 0x000A1E, out * 0.85F, t - 454F, 1.1F);
        tracked(g, UiApi.Font.DISPLAY, TITLE_TEXT, w * 0.5F, base, em, em * 0.28F, TITLE, out, t - 454F, 1.1F);
    }

    // ---- the caption over the lower bar --------------------------------------------------------------------------------------

    private static void caption(GuiGraphics g, float t, QuasarStars.Star star, float w, float h, float cq, float bars) {
        for (JetScript.Caption caption : JetScript.CAPTIONS) {
            if (t >= caption.from() && t <= caption.to()) {
                float shown = Math.min(lin(caption.from(), caption.from() + 4F, t), 1F - lin(caption.to() - 4F, caption.to(), t)) * bars;
                String title = caption.title().replace("%STAR%", star.name().toUpperCase(Locale.ROOT));
                String detail = caption.detail().replace("%STAR%", star.name())
                    .replace("%TYPE%", star.type().label.toLowerCase(Locale.ROOT) + " · " + star.distance() + " away");
                float em = 1.2F * cq;
                tracked(g, UiApi.Font.LABEL, title, w * 0.5F, h * 0.884F + em * 0.85F, em, em * 0.42F, star.type().color, shown, -1F, 0F);
                float detailEm = 1.02F * cq;
                float width = rich(g, UiApi.Font.MONO, detail, 0F, 0F, detailEm, INK, 0F);
                rich(g, UiApi.Font.MONO, detail, (w - width) * 0.5F, h * 0.933F + detailEm * 0.85F, detailEm, INK, shown * 0.8F);
                return;
            }
        }
    }

    // ---- the educational labels ----------------------------------------------------------------------------------------------

    /** Where on the screen (hundredths) the thing a label is about is right now; the middle if it can't be told. */
    private static float[] anchor(JetScript.Anchor anchor, float t, QuasarCast cast) {
        QuasarStars.Type type = cast.star().type();
        float[] at;
        switch (anchor) {
            case STAR:
                at = t < JetScript.SPACE_FROM ? worldDirection(cast.star().direction()) : JetSpace.anchor(anchor, t, type.size);
                break;
            case SECOND_SUN:
                at = worldDirection(cast.quasarDirection());
                break;
            case BEAM:
                at = worldPoint(cast.mark().add(0.0, 1.0, 0.0).add(cast.quasarDirection().scale(16.0)));
                break;
            case DOME:
                at = worldPoint(cast.mark().add(0.0, type.lobe * 0.85, 0.0));
                break;
            case CRYSTALS:
                at = worldPoint(cast.mark().add(0.0, 0.5, 0.0));
                break;
            case RIBBON:
            case DISC:
                at = JetSpace.anchor(anchor, t, type.size * 2.2F);
                break;
            default:
                at = JetSpace.anchor(anchor, t, type.size);
        }
        return at != null ? at : new float[] {50F, 50F};
    }

    private static void labels(GuiGraphics g, float t, QuasarCast cast, float w, float h, float cq) {
        for (JetScript.Label label : JetScript.LABELS) {
            if (t < label.from() || t > label.to()) {
                continue;
            }
            float shown = Math.min(lin(label.from(), label.from() + 4F, t), 1F - lin(label.to() - 4F, label.to(), t));
            if (shown < 0.01F) {
                continue;
            }
            float[] anchor = anchor(label.anchor(), t, cast);
            float ax = Math.max(3F, Math.min(anchor[0], 97F)) * w / 100F;
            float ay = Math.max(14F, Math.min(anchor[1], 85F)) * h / 100F;
            String text = label.text().replace("%TYPE_FACT%", JetScript.typeFact(cast.star().type()))
                .replace("%TIDAL%", JetScript.tidal(cast.star().type()));
            float em = 1.08F * cq;
            float pad = 0.8F * cq;
            List<String> lines = wrap(g, text, em, w * 0.31F - pad * 2F);
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
            box(g, x, y, boxW, boxH, 0x03090C, shown * 0.82F);
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

    // ---- things in the world, on the screen ------------------------------------------------------------------------------------

    /** Where a far-off direction of the sky is on the screen (hundredths), or null when it is behind the view. */
    @Nullable
    private static float[] worldDirection(Vec3 direction) {
        return worldKnown ? onScreen((float) direction.x * 100F, (float) direction.y * 100F, (float) direction.z * 100F) : null;
    }

    @Nullable
    private static float[] worldPoint(Vec3 point) {
        return worldKnown ? onScreen((float) (point.x - worldEye.x), (float) (point.y - worldEye.y), (float) (point.z - worldEye.z)) : null;
    }

    /** A point given from the camera, through the level's own view and projection of this frame. */
    @Nullable
    private static float[] onScreen(float x, float y, float z) {
        Vector4f p = new Vector4f(x, y, z, 1F);
        WORLD_VIEW.transform(p);
        WORLD_PROJ.transform(p);
        if (p.w <= 0.01F) {
            return null;
        }
        return new float[] {(p.x / p.w * 0.5F + 0.5F) * 100F, (1F - (p.y / p.w * 0.5F + 0.5F)) * 100F};
    }

    // ---- drawing ---------------------------------------------------------------------------------------------------------------

    private static int argb(int rgb, float alpha) {
        return Math.round(sat(alpha) * 255F) << 24 | rgb & 0xFFFFFF;
    }

    /** {@code a} with {@code u} of {@code b} mixed in. */
    private static int mix(int a, int b, float u) {
        int r = Math.round(lerp(a >> 16 & 255, b >> 16 & 255, u));
        int gr = Math.round(lerp(a >> 8 & 255, b >> 8 & 255, u));
        int bl = Math.round(lerp(a & 255, b & 255, u));
        return r << 16 | gr << 8 | bl;
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
     * Text with the film's raised powers ({@code 10^{11}}), its left end at {@code x}, on the baseline {@code y},
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
