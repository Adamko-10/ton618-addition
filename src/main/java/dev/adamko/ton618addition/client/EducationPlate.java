package dev.adamko.ton618addition.client;

import com.mojang.blaze3d.platform.InputConstants;
import cyou.rimuru.rimulib.mc1211.api.SkillApi;
import cyou.rimuru.rimulib.mc1211.client.api.MenuApi;
import cyou.rimuru.rimulib.mc1211.client.magic.ClientSkills;
import cyou.rimuru.rimulib.mc1211.skill.Skill;
import cyou.rimuru.rimulib.mc1211.skill.SkillSet;
import dev.adamko.ton618addition.link.Ton618Link;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * The addon's plate in the H menu (Adam, 2026-11-14: "Switch in the H menu"): the educational-mode switch, flipped
 * with its key ({@link #KEY}, "\") because the menu holds the mouse. Shown in the menu's top right corner (Adam,
 * 2026-11-14) while it is open for TON 618's item, or for the Stellar Remote with Last Light picked. Drawn and keyed
 * through RimuLib's public API ({@code UiApi.onMenuDraw}, {@code MenuApi.onKey}), in the same style as the SA Series'
 * plates.
 */
public final class EducationPlate {
    private static final int PLATE = 0xFBFAFC;
    private static final int INK = 0x3B4048;
    private static final int GREY = 0x8A8F99;
    private static final int RULE = 0xE4E6EB;
    /** The blue-white of Last Light's burst, a little deeper so it reads on white. */
    private static final int ACCENT = 0x5E9BD6;
    /**
     * "\": no letter is free in the menu (the SA Series' plates use "[", "-" and "="). It was "]" at first, but in
     * Minecraft's font that key cap read as "J", the films' key (Adam, 2026-11-14). GLFW key code.
     */
    public static final int KEY = 92;
    private static final int WIDTH = 214;
    private static final int HEIGHT = 58;
    private static final float SLIDE_MILLIS = 160.0F;

    private static long flippedAt;

    private EducationPlate() {
    }

    /**
     * The menu is open for TON 618's item, or for the Stellar Remote with Last Light picked (the SA Series' Stellar
     * Convergence plate takes the same top right corner when its skill is picked there).
     */
    static boolean shown() {
        if (!MenuApi.capturesInput()) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }
        SkillSet ton618 = SkillApi.setOf(Ton618Link.ON_TON618);
        if (ton618 != null && SkillApi.holds(player, ton618)) {
            return true;
        }
        SkillSet remote = SkillApi.setOf(Ton618Link.ON_REMOTE);
        return remote != null && SkillApi.holds(player, remote) && lastLightPicked(remote);
    }

    /** Last Light is the picked skill in that menu (RimuLib internals: if they change, the plate just shows). */
    private static boolean lastLightPicked(SkillSet remote) {
        try {
            return ClientSkills.selected(remote) == Ton618Link.ON_REMOTE;
        } catch (RuntimeException | LinkageError unsure) {
            return true;
        }
    }

    /** A key pressed while the menu is open: true if it was ours (never one the menu itself uses). */
    static boolean key(int key) {
        if (key != KEY || !shown() || menuUses(key)) {
            return false;
        }
        Ton618AdditionClientConfig.setEducational(!Ton618AdditionClientConfig.educational());
        flippedAt = Util.getMillis();
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 0.5F));
        return true;
    }

    /** The menu is rebinding a key, or the key is the menu's, the films', or a skill's in the shown menus. */
    private static boolean menuUses(int key) {
        try {
            if (MenuApi.binding() != null || matches(MenuApi.menuKey(), key) || matches(MenuApi.filmKey(), key)) {
                return true;
            }
            for (Skill ours : new Skill[]{Ton618Link.ON_TON618, Ton618Link.ON_REMOTE}) {
                SkillSet set = SkillApi.setOf(ours);
                if (set != null) {
                    for (Skill skill : set.skills) {
                        if (matches(MenuApi.key(skill), key)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (RuntimeException | LinkageError unsure) {
            return true;
        }
    }

    private static boolean matches(KeyMapping mapping, int key) {
        return mapping != null && mapping.matches(key, 0);
    }

    static void draw(GuiGraphics g) {
        if (!shown()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        boolean on = Ton618AdditionClientConfig.educational();
        Component title = Component.translatable("gui.ton618_addition.panel").withStyle(ChatFormatting.BOLD);
        Component label = Component.translatable("gui.ton618_addition.educational").withStyle(ChatFormatting.BOLD);
        Component hint = Component.translatable("gui.ton618_addition.educational.hint");
        // how Last Light unlocks, while it is still locked (Adam, 2026-11-14: "a little note ... in the UI"); in the
        // dark, one line saying it's ready
        boolean ready = minecraft.level != null && minecraft.level.dimension() == Ton618Link.DARK;
        Component[] unlock = ready
            ? new Component[]{Component.translatable("gui.ton618_addition.unlock.ready").withStyle(ChatFormatting.BOLD)}
            : new Component[]{Component.translatable("gui.ton618_addition.unlock.title").withStyle(ChatFormatting.BOLD),
                Component.translatable("gui.ton618_addition.unlock.1"), Component.translatable("gui.ton618_addition.unlock.2"),
                Component.translatable("gui.ton618_addition.unlock.3")};
        int w = Math.max(WIDTH, Math.max(font.width(title), font.width(hint)) + 22);
        for (Component line : unlock) {
            w = Math.max(w, font.width(line) + 22);
        }
        int height = HEIGHT + 4 + unlock.length * 11;
        // never below one screen pixel per font pixel; a little smaller on small screens (as the SA Series' plates)
        float scale = Math.max(Mth.clamp(g.guiWidth() / 860.0F, 0.5F, 0.75F) * 0.82F, 1.0F / (float) minecraft.getWindow().getGuiScale());
        g.pose().pushPose();
        // over the menu, in its top right corner (Adam, 2026-11-14; the SA Series' first plate is top left)
        g.pose().translate(g.guiWidth() - 8.0F - w * scale, 8.0F, 400.0F);
        g.pose().scale(scale, scale, 1.0F);
        plate(g, 2, 3, w + 2, height + 3, 0x26000000);
        plate(g, 0, 0, w, height, 0xF4000000 | PLATE);
        g.fill(w - 3, 2, w, height - 2, 0xFF000000 | ACCENT);
        // the unlock note, under the switch
        g.fill(11, HEIGHT - 5, w - 10, HEIGHT - 4, 0xFF000000 | RULE);
        for (int i = 0; i < unlock.length; i++) {
            int color = ready ? ACCENT : i == 0 ? INK : GREY;
            g.drawString(font, unlock[i], 11, HEIGHT + 1 + i * 11, 0xFF000000 | color, false);
        }
        g.drawString(font, title, 11, 7, 0xFF000000 | INK, false);
        g.fill(11, 19, w - 10, 20, 0xFF000000 | RULE);
        // the row: label, key cap, state, switch
        int y = 26;
        g.drawString(font, label, 11, y + 1, 0xFF000000 | INK, false);
        int sw = 26;
        int sx = w - 12 - sw;
        String keyName = keyName(KEY);
        plate(g, sx - 18, y - 1, sx - 6, y + 11, 0xFF000000 | INK);
        plate(g, sx - 17, y, sx - 7, y + 10, 0xFF000000 | PLATE);
        g.drawString(font, keyName, sx - 12 - font.width(keyName) / 2, y + 2, 0xFF000000 | INK, false);
        float k = Mth.clamp((Util.getMillis() - flippedAt) / SLIDE_MILLIS, 0.0F, 1.0F);
        k = k * k * (3.0F - 2.0F * k);
        float at = on ? k : 1.0F - k;
        plate(g, sx, y, sx + sw, y + 10, 0xFF000000 | lerp(0xC9CDD4, ACCENT, at));
        int knob = sx + 1 + Math.round((sw - 10) * at);
        plate(g, knob, y + 1, knob + 8, y + 9, 0xFFFFFFFF);
        Component state = Component.translatable(on ? "gui.ton618_addition.on" : "gui.ton618_addition.off");
        g.drawString(font, state, sx - 22 - font.width(state), y + 1, 0xFF000000 | (on ? ACCENT : GREY), false);
        g.drawString(font, hint, 11, y + 14, 0xFF000000 | GREY, false);
        g.pose().popPose();
    }

    /** A symbol key as the keyboard layout prints it. */
    private static String keyName(int key) {
        String name = InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
        return name.length() == 1 ? name.toUpperCase(Locale.ROOT) : name;
    }

    /** A rectangle with its corners cut by a pixel (the menu's own plates are rounded). */
    private static void plate(GuiGraphics g, int x0, int y0, int x1, int y1, int argb) {
        g.fill(x0 + 1, y0, x1 - 1, y1, argb);
        g.fill(x0, y0 + 1, x0 + 1, y1 - 1, argb);
        g.fill(x1 - 1, y0 + 1, x1, y1 - 1, argb);
    }

    private static int lerp(int a, int b, float k) {
        int r = Math.round(Mth.lerp(k, a >> 16 & 0xFF, b >> 16 & 0xFF));
        int gr = Math.round(Mth.lerp(k, a >> 8 & 0xFF, b >> 8 & 0xFF));
        int bl = Math.round(Mth.lerp(k, a & 0xFF, b & 0xFF));
        return r << 16 | gr << 8 | bl;
    }
}
