package dev.adamko.ton618addition.client.quasar;

import com.mojang.math.Axis;
import cyou.rimuru.rimulib.mc1211.client.api.UiApi;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

/**
 * <b>FABLE'S FILE</b> (since the quality redo, 2026-11-16). TA-02's own HUD, outside the film, in the film's lettering
 * (RimuLib's overlay: one size at any GUI scale). Only while the player holds TON 618's item or the Stellar Remote with
 * TA-02 picked, at night in the overworld (Adam, 2026-11-16):
 * <ul>
 *   <li>a small name under each of our named stars in view;</li>
 *   <li>on the one nearest the crosshair, a reticle that closes as the aim gets good enough to lock, with a card: its
 *       name, type, distance, size, mass, and what a press would do;</li>
 *   <li>once a star is locked, a line at the bottom saying what to do next and how far away the strike would land
 *       ({@link QuasarRender} draws the mark there).</li>
 * </ul>
 * Also the landing blast and the lobe's pop as a short flash of the whole screen for players near them.
 */
final class QuasarReticle {
    private static final double SHOW_DEGREES = 9.0;
    private static final int INK = 0xE6EBF2;
    private static final int DIM = 0x8590A3;
    private static final int PLATE = 0x03090C;
    private static final int WARN = 0xFF5A4D;

    /** How far the reticle has closed (0 wide open, 1 shut on a locked star), eased over a few frames. */
    private static float closed;
    private static int closedOn = -1;
    private static long lastFrame;

    private QuasarReticle() {
    }

    static void draw(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        flash(g, mc, partial);
        if (mc.level.dimension() != Level.OVERWORLD || QuasarClient.current() != null || QuasarJetsFilm.hasCamera()
            || !QuasarClient.holdingQuasarJets()) {
            return;
        }
        UiApi.overlay(g, () -> hud(g, mc, partial));
    }

    private static void hud(GuiGraphics g, Minecraft mc, float partial) {
        float w = UiApi.width(g);
        float h = UiApi.height(g);
        float cq = w / 100f;
        int locked = QuasarClient.lockedStar();
        float night = mc.level.getStarBrightness(partial) * (1f - mc.level.getRainLevel(partial));
        float ticks = mc.level.getGameTime() % 24000L + partial;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastFrame) / 1.0e9f);
        lastFrame = now;
        if (night > 0.15f && QuasarRender.skyKnown) {
            Vec3 look = mc.player.getViewVector(partial);
            int focus = QuasarStars.looking(look, SHOW_DEGREES);
            if (focus >= 0 && !QuasarClient.starShown(focus)) {
                focus = -1;
            }
            // a small name under each of the others, so they can be found
            for (int i = 0; i < QuasarStars.ALL.size(); i++) {
                if (i == focus || !QuasarClient.starShown(i)) {
                    continue;
                }
                QuasarStars.Star s = QuasarStars.ALL.get(i);
                float[] at = project(s.direction());
                if (at == null) {
                    continue;
                }
                float em = 0.8f * cq;
                String name = s.name().toUpperCase(Locale.ROOT);
                float x = at[0] * w;
                float y = at[1] * h + (1.5f + 0.5f * s.type().size) * cq;
                box(g, x - 0.5f, y - 0.6f * cq, 1f, 0.4f * cq, s.type().color, 0.5f);
                tracked(g, UiApi.Font.LABEL, name, x, y + em, em, em * 0.22f, i == locked ? s.type().color : INK, i == locked ? 0.95f : 0.5f);
            }
            if (focus >= 0) {
                QuasarStars.Star s = QuasarStars.ALL.get(focus);
                float[] at = project(s.direction());
                if (at != null) {
                    boolean isLocked = focus == locked;
                    boolean close = QuasarStars.looking(look, QuasarStars.PICK_DEGREES) == focus;
                    float target = isLocked ? 1f : close ? 0.62f : 0f;
                    if (closedOn != focus) {
                        closedOn = focus;
                        closed = 0f;
                    }
                    closed += (target - closed) * Math.min(1f, dt * 12f);
                    reticle(g, at[0] * w, at[1] * h, s, isLocked, close, ticks, w, cq);
                }
            } else {
                closedOn = -1;
            }
        }
        if (locked >= 0) {
            hint(g, mc, QuasarStars.ALL.get(locked), w, h, cq);
        }
    }

    // ---- the reticle and its card ------------------------------------------------------------------------------------------

    private static void reticle(GuiGraphics g, float x, float y, QuasarStars.Star s, boolean locked, boolean close, float ticks, float w,
                                float cq) {
        int rgb = s.type().color;
        float half = (3.7f - 1.6f * closed) * cq;
        float arm = half * 0.42f;
        float thick = Math.max(1.2f, 0.14f * cq);
        float a = 0.55f + 0.45f * closed;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sy = -1; sy <= 1; sy += 2) {
                box(g, sx < 0 ? x - half : x + half - arm, sy < 0 ? y - half : y + half - thick, arm, thick, rgb, a);
                box(g, sx < 0 ? x - half : x + half - thick, sy < 0 ? y - half : y + half - arm, thick, arm, rgb, a);
            }
        }
        // a ring of ticks turning round it: quick while it is still searching, slow and whole once it has the star
        float turn = ticks * (locked ? 0.012f : close ? 0.03f : 0.07f);
        int marks = 24;
        for (int i = 0; i < marks; i++) {
            if (!locked && i % 3 == 2) {
                continue;
            }
            float angle = turn + i * (float) (Math.PI * 2.0 / marks);
            boolean big = i % 6 == 0;
            g.pose().pushPose();
            g.pose().translate(x, y, 0f);
            g.pose().mulPose(Axis.ZP.rotation(angle));
            UiApi.box(g, half * 1.5f, -thick * 0.4f, (big ? 0.7f : 0.35f) * cq, thick * 0.8f, argb(rgb, a * (big ? 0.9f : 0.5f)));
            g.pose().popPose();
        }

        // the card: to the right of it, or to the left near the screen's edge
        String name = s.name().toUpperCase(Locale.ROOT);
        String line1 = s.type().label + " · " + s.distance();
        String line2 = s.size() + " · " + s.mass();
        String status = locked ? "LOCKED" : close ? "PRESS TO LOCK" : "ACQUIRING";
        float nameEm = 1.3f * cq;
        float em = 0.95f * cq;
        float smallEm = 0.82f * cq;
        float pad = 0.8f * cq;
        float textW = Math.max(Math.max(trackedWidth(g, UiApi.Font.LABEL, name, nameEm, nameEm * 0.3f), width(g, line1, em)),
            Math.max(width(g, line2, smallEm), trackedWidth(g, UiApi.Font.LABEL, status, smallEm, smallEm * 0.34f) + smallEm * 1.4f));
        float cardW = textW + pad * 2f;
        float cardH = pad * 1.6f + nameEm + em * 1.5f + smallEm * 1.5f + smallEm * 1.9f;
        float gap = half * 1.5f + 1.6f * cq;
        boolean left = x + gap + cardW > w * 0.98f;
        float cx = left ? x - gap - cardW : x + gap;
        float cy = y - cardH * 0.5f;
        box(g, left ? cx + cardW : x + half * 1.5f + 0.9f * cq, y - 0.5f, left ? x - half * 1.5f - 0.9f * cq - cx - cardW : cx - x - half * 1.5f - 0.9f * cq,
            1f, rgb, 0.6f);
        box(g, cx, cy, cardW, cardH, PLATE, 0.74f);
        box(g, left ? cx + cardW - 2f : cx, cy, 2f, cardH, rgb, 1f);
        float base = cy + pad * 0.8f + nameEm * 0.85f;
        tracked(g, UiApi.Font.LABEL, name, cx + pad + trackedWidth(g, UiApi.Font.LABEL, name, nameEm, nameEm * 0.3f) * 0.5f, base, nameEm,
            nameEm * 0.3f, rgb, 1f);
        base += em * 1.5f;
        text(g, line1, cx + pad, base, em, INK, 0.95f);
        base += smallEm * 1.5f;
        text(g, line2, cx + pad, base, smallEm, DIM, 0.95f);
        base += smallEm * 1.9f;
        // a small light: steady when locked, blinking while it can be locked
        float blink = locked ? 1f : close ? 0.5f + 0.5f * (float) Math.sin(ticks * 0.5f) : 0.35f;
        box(g, cx + pad, base - smallEm * 0.72f, smallEm * 0.7f, smallEm * 0.7f, locked || close ? rgb : DIM, blink);
        tracked(g, UiApi.Font.LABEL, status, cx + pad + smallEm * 1.4f + trackedWidth(g, UiApi.Font.LABEL, status, smallEm, smallEm * 0.34f) * 0.5f,
            base, smallEm, smallEm * 0.34f, locked || close ? rgb : DIM, 1f);
    }

    /** A star is locked: what to do next, and how far away the strike would land. */
    private static void hint(GuiGraphics g, Minecraft mc, QuasarStars.Star s, float w, float h, float cq) {
        int rgb = s.type().color;
        String title = s.name().toUpperCase(Locale.ROOT) + " LOCKED";
        Vec3 aimed = QuasarRender.aimedMark;
        String detail;
        int detailRgb = INK;
        if (aimed == null) {
            detail = "aim at the ground and cast again";
        } else {
            double away = aimed.distanceTo(mc.player.position());
            double safe = s.type().lobe + 4.0;
            if (away < safe) {
                detail = "too close: aim at least " + (int) Math.ceil(safe) + " blocks away";
                detailRgb = WARN;
            } else {
                detail = "strike point " + Math.round(away) + " blocks away · cast again to launch";
            }
        }
        float titleEm = 1.1f * cq;
        float em = 0.95f * cq;
        float pad = 0.8f * cq;
        float titleW = trackedWidth(g, UiApi.Font.LABEL, title, titleEm, titleEm * 0.36f);
        float cardW = Math.max(titleW, width(g, detail, em)) + pad * 2.4f;
        float cardH = pad * 1.6f + titleEm + em * 1.6f;
        float x = (w - cardW) * 0.5f;
        // above the game's own line with the held item's name (Adam, 2026-11-16: "text is over itself")
        float y = h * 0.585f;
        box(g, x, y, cardW, cardH, PLATE, 0.74f);
        box(g, x, y + cardH - 2f, cardW, 2f, rgb, 1f);
        tracked(g, UiApi.Font.LABEL, title, w * 0.5f, y + pad * 0.8f + titleEm * 0.85f, titleEm, titleEm * 0.36f, rgb, 1f);
        text(g, detail, (w - width(g, detail, em)) * 0.5f, y + pad * 0.8f + titleEm * 0.85f + em * 1.6f, em, detailRgb, 0.95f);
    }

    /** Where a sky direction is on the screen (0-1 across and down), or null when it is behind the view or off it. */
    @Nullable
    private static float[] project(Vec3 dir) {
        Vector4f p = new Vector4f((float) dir.x * 100f, (float) dir.y * 100f, (float) dir.z * 100f, 1f);
        QuasarRender.skyView.transform(p);
        QuasarRender.skyProj.transform(p);
        if (p.w <= 0.01f) {
            return null;
        }
        float x = p.x / p.w * 0.5f + 0.5f;
        float y = 1f - (p.y / p.w * 0.5f + 0.5f);
        return x < -0.02f || y < -0.02f || x > 1.02f || y > 1.02f ? null : new float[]{x, y};
    }

    // ---- the flashes: the landing blast and the lobe's pop, strong near them and gone a way off ----------------------------

    private static void flash(GuiGraphics g, Minecraft mc, float partial) {
        QuasarCast cast = QuasarClient.current();
        if (cast == null || mc.level.dimension() != Level.OVERWORLD) {
            return;
        }
        float t = cast.tick(partial);
        if (t < QuasarClock.LANDS || t > QuasarClock.POP + 12) {
            return;
        }
        QuasarStars.Type type = cast.star().type();
        double d = mc.gameRenderer.getMainCamera().getPosition().distanceTo(cast.mark());
        float b0 = t - QuasarClock.LANDS;
        float p0 = t - QuasarClock.POP;
        float a = 0f;
        if (b0 < 14f) {
            float u = 1f - b0 / 14f;
            a = (float) Math.max(0.0, 1.0 - d / (type.blast * 10.0)) * u * u * 0.9f;
        }
        if (p0 >= 0f) {
            a = Math.max(a, (float) Math.max(0.0, 1.0 - d / (type.lobe * 3.0)) * (1f - p0 / 12f) * 0.8f);
        }
        // softer for players who asked the game to hide lightning flashes
        a *= mc.options.hideLightningFlash().get() ? 0.35f : 1f;
        if (a > 0.01f) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (Math.min(a, 1f) * 255f) << 24) | 0xF4F8FF);
        }
    }

    // ---- drawing -----------------------------------------------------------------------------------------------------------

    private static int argb(int rgb, float alpha) {
        float a = alpha < 0f ? 0f : Math.min(alpha, 1f);
        return Math.round(a * 255f) << 24 | rgb & 0xFFFFFF;
    }

    private static void box(GuiGraphics g, float x, float y, float w, float h, int rgb, float alpha) {
        if (alpha > 0.004f && w > 0f && h > 0f) {
            UiApi.box(g, x, y, w, h, argb(rgb, alpha));
        }
    }

    /** Plain text, its left end at {@code x}, on the baseline {@code y}, {@code em} high. */
    private static void text(GuiGraphics g, String text, float x, float y, float em, int rgb, float alpha) {
        UiApi.text(g, UiApi.Font.MONO, text, x, y, 0f, em / 9.5f, argb(rgb, alpha));
    }

    private static float width(GuiGraphics g, String text, float em) {
        return UiApi.measure(g, UiApi.Font.MONO, text, em / 9.5f);
    }

    private static float trackedWidth(GuiGraphics g, UiApi.Font font, String text, float em, float tracking) {
        float scale = em / 9.5f;
        float width = 0f;
        for (int i = 0; i < text.length(); i++) {
            width += UiApi.measure(g, font, text.substring(i, i + 1), scale) + (i > 0 ? tracking : 0f);
        }
        return width;
    }

    /** Letters set wide apart, centred on {@code cx}, on the baseline {@code y}. */
    private static void tracked(GuiGraphics g, UiApi.Font font, String text, float cx, float y, float em, float tracking, int rgb, float alpha) {
        if (alpha <= 0.02f) {
            return;
        }
        float scale = em / 9.5f;
        float x = cx - trackedWidth(g, font, text, em, tracking) * 0.5f;
        for (int i = 0; i < text.length(); i++) {
            String letter = text.substring(i, i + 1);
            if (!letter.isBlank()) {
                UiApi.text(g, font, letter, x, y, 0f, scale, argb(rgb, alpha));
            }
            x += UiApi.measure(g, font, letter, scale) + tracking;
        }
    }
}
