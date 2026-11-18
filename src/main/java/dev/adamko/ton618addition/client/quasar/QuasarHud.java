package dev.adamko.ton618addition.client.quasar;

import cyou.rimuru.rimulib.client.api.UiApi;
import dev.adamko.ton618addition.client.Ton618AdditionClientConfig;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import dev.adamko.ton618addition.quasar.QuasarClock;
import dev.adamko.ton618addition.quasar.QuasarSky;
import dev.adamko.ton618addition.quasar.QuasarStars;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * TA-02's readout for whoever watches a strike without its film (third or first person: the film is off, or they are
 * further off than the film reaches), as the SA Series' strike readouts but saying what TA-02 is doing (Adam,
 * 2026-11-17: "a ui for 3rd person no cutscene ... make it similar to shooting star addition but make it make sense";
 * his pick of the mock-up: "Build it like this", for "Everyone near"):
 * <ul>
 *   <li>top left, a panel: the skill and the star, the phase, and what matters in it (the star's racing clock, the
 *       Earth's clock, time to impact, the mark's distance and direction, the blast's size and the Power it was cast
 *       with; then the crater, the lobe's pop, the frozen light, when the star is back); in educational mode one line
 *       on what is happening;</li>
 *   <li>along the top, beside the panel, the strike's phases with how far it has got;</li>
 *   <li>in the last 2.5 seconds before the landing, "JET INBOUND" and a countdown at the top.</li>
 * </ul>
 */
final class QuasarHud {
    /** Players further than this from the mark (and not the caster) get no readout. */
    private static final double RANGE = 2000.0;
    private static final int INK = 0xE6EBF2;
    private static final int DIM = 0x8A9BBD;
    private static final int PLATE = 0x060A16;
    private static final int WARN = 0xFF6B55;
    private static final int WARN_INK = 0xFFB7A6;

    /** The phases: the tick each starts at, the panel's name for it, and its line for educational mode. */
    private static final Object[][] PHASES = {
        {0, "STRIKE POINT MARKED", "the star is locked, the jet will land here"},
        {102, "THE STAR ENGINE", "a mirror catches the star's own light and pushes it"},
        {154, "FAST FORWARD", "the remote speeds up the star's own time"},
        {206, "IN FLIGHT TO TON 618", "billions of years for the star, seconds for us"},
        {270, "TON 618", "an ultramassive black hole at a galaxy's heart"},
        {336, "CAUGHT", "the star falls toward the black hole"},
        {388, "TIDAL DISRUPTION", "the star is pulled into a ribbon of gas"},
        {444, "IGNITION", "the gas falls in and TON 618 blazes as a quasar"},
        {534, "THE JETS", "plasma shot out of both poles at nearly light speed"},
        {612, "BLAZAR", "one jet swings round to face the Earth"},
        {692, "SECOND SUN", "the quasar's light turns night into day"},
        {749, "JET INBOUND", "the jet carries what it touches to the mark"},
        {797, "IMPACT", "the jet hits the ground at nearly the speed of light"},
        {834, "RADIO LOBE", "a bubble of plasma where the jet ends"},
        {892, "FROZEN LIGHT", "what is left of the star"}};
    /** The bottom bar: its labels and the tick each starts at. */
    private static final String[] BAR = {"MARK", "ENGINE", "FLIGHT", "TON 618", "IGNITION", "JETS", "2ND SUN", "IMPACT", "LOBE"};
    private static final int[] BAR_AT = {0, 102, 206, 270, 444, 534, 692, 797, 834};
    private static final String[] COMPASS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    private QuasarHud() {
    }

    static void draw(GuiGraphicsExtractor g, float partial) {
        Minecraft mc = Minecraft.getInstance();
        QuasarCast cast = QuasarClient.current();
        if (cast == null || mc.level == null || mc.player == null || mc.gui.hud.isHidden() || mc.level.dimension() != Level.OVERWORLD
            || QuasarJetsFilm.hasCamera()) {
            return;
        }
        float t = cast.tick(partial);
        if (t < 0f || t >= QuasarClock.END) {
            return;
        }
        Vec3 eye = mc.gameRenderer.mainCamera().position();
        if (!cast.isCaster() && eye.distanceTo(cast.mark()) > RANGE) {
            return;
        }
        UiApi.overlay(g, () -> hud(g, mc, cast, t, eye));
    }

    private static void hud(GuiGraphicsExtractor g, Minecraft mc, QuasarCast cast, float t, Vec3 eye) {
        float w = UiApi.width(g);
        float h = UiApi.height(g);
        float cq = w / 100f;
        QuasarStars.Star star = cast.star();
        boolean inbound = t >= QuasarClock.SWEEP && t < QuasarClock.LANDS;
        int accent = inbound ? WARN : star.type().color;
        int phase = 0;
        for (int i = 0; i < PHASES.length; i++) {
            if (t >= (Integer) PHASES[i][0]) {
                phase = i;
            }
        }

        // ---- the panel, top left
        float em = 1.05f * cq;
        float line = em * 1.55f;
        float x = 1.6f * cq;
        float y = 1.6f * cq;
        float pw = 26f * cq;
        String[][] rows = rows(mc, cast, t, eye);
        boolean edu = Ton618AdditionClientConfig.educational() || cast.fx().educational();
        int lines = 3 + rows.length + (edu ? 1 : 0);
        float ph = line * lines + em * 1.4f;
        box(g, x, y, pw, ph, PLATE, 0.72f);
        box(g, x, y, 0.22f * cq, ph, accent, 0.95f);
        float tx = x + 1.1f * cq;
        float base = y + em * 1.35f;
        text(g, "◆ TA-02 · QUASAR JETS", tx, base, em * 1.08f, accent, 0.98f);
        base += line;
        text(g, (star.name() + " · " + star.type().label).toUpperCase(Locale.ROOT), tx, base, em * 0.9f, DIM, 0.95f);
        base += line * 0.55f;
        box(g, tx, base, pw - 2.2f * cq, 0.08f * cq, INK, 0.18f);
        base += line * 0.95f;
        text(g, (String) PHASES[phase][1], tx, base, em, inbound ? WARN_INK : INK, 0.98f);
        for (String[] row : rows) {
            base += line;
            text(g, row[0], tx, base, em * 0.92f, DIM, 0.95f);
            String value = row[1];
            boolean warn = value.startsWith("!");
            if (warn) {
                value = value.substring(1);
            }
            float vw = width(g, value, em * 0.92f);
            text(g, value, x + pw - 1.1f * cq - vw, base, em * 0.92f, warn ? WARN_INK : INK, 0.98f);
        }
        if (edu) {
            base += line;
            text(g, (String) PHASES[phase][2], tx, base, em * 0.85f, 0xB9C7E6, 0.9f);
        }

        // ---- the phases, along the top beside the panel (at the bottom the hotbar hid them)
        float bx = x + pw + 1.6f * cq;
        float bw = w - bx - 1.6f * cq;
        float by = y + 0.4f * cq;
        float done = Math.min(1f, t / QuasarClock.END);
        box(g, bx - 0.8f * cq, y, bw + 1.6f * cq, 2.9f * cq, PLATE, 0.55f);
        box(g, bx, by, bw, 0.32f * cq, INK, 0.16f);
        box(g, bx, by, bw * done, 0.32f * cq, accent, 0.9f);
        int now = 0;
        for (int i = 0; i < BAR.length; i++) {
            if (t >= BAR_AT[i]) {
                now = i;
            }
        }
        float slot = bw / BAR.length;
        for (int i = 0; i < BAR.length; i++) {
            float lw = width(g, BAR[i], em * 0.8f);
            float lx = bx + slot * i + (slot - lw) * 0.5f;
            text(g, BAR[i], lx, by + 1.5f * cq, em * 0.8f, i == now ? INK : DIM, i == now ? 0.98f : 0.75f);
        }

        // ---- the jet inbound: a countdown at the top
        if (inbound) {
            float left = (QuasarClock.LANDS - t) / 20f;
            float pulse = 0.75f + 0.25f * (float) Math.sin(t * 1.2f);
            String head = "JET INBOUND";
            String count = String.format(Locale.ROOT, "%.1f s", left);
            float hw = width(g, head, em * 1.1f);
            float cw = width(g, count, em * 2.8f);
            // a plate behind it: the second sun's sky is bright
            float plate = Math.max(hw, cw) + 3f * cq;
            box(g, (w - plate) * 0.5f, 8f * cq - em * 1.6f, plate, em * 5.4f, PLATE, 0.7f);
            box(g, (w - plate) * 0.5f, 8f * cq - em * 1.6f, plate, 0.22f * cq, WARN, 0.95f);
            text(g, head, (w - hw) * 0.5f, 8f * cq, em * 1.1f, WARN_INK, pulse);
            text(g, count, (w - cw) * 0.5f, 8f * cq + em * 3.2f, em * 2.8f, INK, 0.98f);
        }
    }

    /** The panel's rows for this moment: {label, value}; a value starting with "!" is a warning. */
    private static String[][] rows(Minecraft mc, QuasarCast cast, float t, Vec3 eye) {
        double blast = cast.blast();
        String mark = Math.round(eye.distanceTo(cast.mark())) + " m · " + compass(eye, cast.mark());
        String size = Math.round(blast * 2.0) + " m · " + String.format(Locale.ROOT, "%.1f×", cast.fx().power());
        String toImpact = clock((QuasarClock.LANDS - t) / 20f, false);
        if (t < 154f) {
            return new String[][]{{"impact in", toImpact}, {"mark", mark}, {"blast", size}};
        }
        if (t < QuasarClock.ARRIVE) {
            return new String[][]{{"star's clock", "+" + years(t)}, {"earth clock", clock(cast.realTick(0f) / 20f, false)},
                {"impact in", toImpact}, {"mark", mark}, {"blast", size}};
        }
        if (t < QuasarClock.SWEEP) {
            return new String[][]{{"impact in", toImpact}, {"mark", mark}, {"blast", size}};
        }
        if (t < QuasarClock.LANDS) {
            boolean inside = eye.distanceTo(cast.mark()) < blast * 1.3;
            return new String[][]{{"impact in", clock((QuasarClock.LANDS - t) / 20f, true)}, {"mark", mark},
                {"you are", inside ? "!inside the blast" : "outside the blast"}};
        }
        String crater = Math.round(blast * 2.0) + " m across";
        if (t < QuasarClock.POP) {
            return new String[][]{{"crater", crater}, {"lobe pops in", clock((QuasarClock.POP - t) / 20f, true)}, {"mark", mark}};
        }
        String back = "-";
        if (mc.level != null) {
            long nights = QuasarClient.backOnDay(cast.starIndex()) - QuasarSky.day(mc.level.getDefaultClockTime());
            back = nights <= 0 ? "tonight" : nights == 1 ? "1 night" : nights + " nights";
        }
        return new String[][]{{"crater", crater}, {"frozen light", cast.star().type().crystals + " crystals"},
            {cast.star().name().toLowerCase(Locale.ROOT) + " back in", back}};
    }

    /** The star's own clock while the remote races it: up to about 18 billion years by its arrival. */
    private static String years(float t) {
        float u = QuasarClock.ramp(t, 154f, QuasarClock.ARRIVE);
        double y = 18.0e9 * Math.pow(u, 3.0);
        if (y >= 1.0e9) {
            return String.format(Locale.ROOT, "%.1f billion yr", y / 1.0e9);
        }
        if (y >= 1.0e6) {
            return String.format(Locale.ROOT, "%.0f million yr", y / 1.0e6);
        }
        return String.format(Locale.ROOT, "%,d yr", Math.round(y));
    }

    private static String clock(float seconds, boolean tenths) {
        float s = Math.max(0f, seconds);
        int m = (int) (s / 60f);
        float rest = s - m * 60f;
        return tenths ? String.format(Locale.ROOT, "%d:%04.1f", m, rest) : String.format(Locale.ROOT, "%d:%02d", m, (int) rest);
    }

    private static String compass(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        // north is -z, east is +x
        double deg = Math.toDegrees(Math.atan2(dx, -dz));
        int i = (int) Math.floorMod(Math.round(deg / 45.0), 8L);
        return COMPASS[i];
    }

    private static int argb(int rgb, float alpha) {
        float a = alpha < 0f ? 0f : Math.min(alpha, 1f);
        return Math.round(a * 255f) << 24 | rgb & 0xFFFFFF;
    }

    private static void box(GuiGraphicsExtractor g, float x, float y, float w, float h, int rgb, float alpha) {
        if (alpha > 0.004f && w > 0f && h > 0f) {
            UiApi.box(g, x, y, w, h, argb(rgb, alpha));
        }
    }

    private static void text(GuiGraphicsExtractor g, String text, float x, float y, float em, int rgb, float alpha) {
        UiApi.text(g, UiApi.Font.MONO, text, x, y, 0f, em / 9.5f, argb(rgb, alpha));
    }

    private static float width(GuiGraphicsExtractor g, String text, float em) {
        return UiApi.measure(g, UiApi.Font.MONO, text, em / 9.5f);
    }
}
