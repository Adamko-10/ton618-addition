package dev.adamko.ton618addition.client.quasar;

import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarStars;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

/**
 * TA-02's own HUD, outside the film: while TA-02 is the picked skill at night in the overworld, a reticle on the named
 * star nearest the crosshair (its name, type and distance; "LOCKED" once press 1 took it), and the hint to aim at the
 * ground for press 2. Also the radio lobe's pop as a short flash for players near it.
 */
final class QuasarReticle {
    private static final double SHOW_DEGREES = 9.0;

    private QuasarReticle() {
    }

    static void draw(GuiGraphics g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        flash(g, mc, partial);
        if (mc.level.dimension() != Level.OVERWORLD || !QuasarClient.holdingQuasarJets()) {
            return;
        }
        Font font = mc.font;
        int w = g.guiWidth();
        int h = g.guiHeight();
        int locked = QuasarClient.lockedStar();
        float night = mc.level.getStarBrightness(partial) * (1f - mc.level.getRainLevel(partial));
        if (night > 0.15f && QuasarRender.skyKnown) {
            Vec3 look = mc.player.getViewVector(partial);
            int star = QuasarStars.looking(look, SHOW_DEGREES);
            if (star >= 0 && QuasarClient.starShown(star)) {
                QuasarStars.Star s = QuasarStars.ALL.get(star);
                float[] at = project(s.direction(), w, h);
                if (at != null) {
                    boolean isLocked = star == locked;
                    boolean close = QuasarStars.looking(look, QuasarStars.PICK_DEGREES) == star;
                    int color = 0xFF000000 | s.type().color;
                    bracket(g, (int) at[0], (int) at[1], isLocked ? 9 : close ? 11 : 15, color);
                    String status = isLocked ? "LOCKED" : close ? "cast to lock" : "ACQUIRING";
                    g.drawString(font, s.name().toUpperCase(java.util.Locale.ROOT), (int) at[0] + 18, (int) at[1] - 10, color);
                    g.drawString(font, s.type().label + " · " + s.distance(), (int) at[0] + 18, (int) at[1], 0xFFD8E2EE);
                    g.drawString(font, status, (int) at[0] + 18, (int) at[1] + 10, isLocked ? color : 0xFF8FA0B4);
                }
            }
        }
        if (locked >= 0) {
            String hint = QuasarStars.ALL.get(locked).name() + " locked: aim at the ground and cast again";
            g.drawCenteredString(font, hint, w / 2, h - 72, 0xFF000000 | QuasarStars.ALL.get(locked).type().color);
        }
    }

    /** TA-02 is the picked skill of what the player holds (RimuLib's client state; nothing shown if it can't tell). */

    /** Screen position of a sky direction, or null when it is behind the view. */
    private static float[] project(Vec3 dir, int w, int h) {
        Vector4f p = new Vector4f((float) dir.x * 100f, (float) dir.y * 100f, (float) dir.z * 100f, 1f);
        QuasarRender.skyView.transform(p);
        QuasarRender.skyProj.transform(p);
        if (p.w <= 0.01f) {
            return null;
        }
        float x = (p.x / p.w * 0.5f + 0.5f) * w;
        float y = (1f - (p.y / p.w * 0.5f + 0.5f)) * h;
        if (x < -20 || y < -20 || x > w + 20 || y > h + 20) {
            return null;
        }
        return new float[]{x, y};
    }

    private static void bracket(GuiGraphics g, int x, int y, int r, int color) {
        int l = Math.max(3, r / 2);
        g.fill(x - r, y - r, x - r + l, y - r + 1, color);
        g.fill(x - r, y - r, x - r + 1, y - r + l, color);
        g.fill(x + r - l, y - r, x + r, y - r + 1, color);
        g.fill(x + r - 1, y - r, x + r, y - r + l, color);
        g.fill(x - r, y + r - 1, x - r + l, y + r, color);
        g.fill(x - r, y + r - l, x - r + 1, y + r, color);
        g.fill(x + r - l, y + r - 1, x + r, y + r, color);
        g.fill(x + r - 1, y + r - l, x + r, y + r, color);
    }

    /** The lobe's pop: a short white flash, strong near it and gone three lobe radii away. */
    private static void flash(GuiGraphics g, Minecraft mc, float partial) {
        QuasarCast cast = QuasarClient.current();
        if (cast == null) {
            return;
        }
        float t = cast.tick(partial);
        if (t < QuasarClock.POP || t > QuasarClock.POP + 12) {
            return;
        }
        double reach = cast.star().type().lobe * 3.0;
        double d = mc.player.position().distanceTo(cast.mark());
        float a = (float) Math.max(0.0, 1.0 - d / reach) * (1f - (t - QuasarClock.POP) / 12f) * 0.8f;
        if (a > 0.01f) {
            g.fill(0, 0, g.guiWidth(), g.guiHeight(), ((int) (a * 255f) << 24) | 0xF4F8FF);
        }
    }
}
