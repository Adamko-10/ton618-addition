package dev.adamko.ton618addition.client;

import com.mojang.blaze3d.platform.InputConstants;
import cyou.rimuru.rimulib.client.magic.SkillKeys;
import cyou.rimuru.rimulib.skill.Skill;
import dev.adamko.ton618addition.Ton618Addition;
import dev.adamko.ton618addition.client.mixin.KeyMappingAccessor;
import dev.adamko.ton618addition.link.Ton618Link;
import net.minecraft.client.KeyMapping;

/**
 * Fabric 26.3 only: TA-01's and TA-02's default keys (; ' , .).
 *
 * <p>RimuLib 26.3 makes a skill's key with its default only for the letters A-Z (26.3's key codes are SDL scancodes; it
 * turns GLFW's 65-90 into them and makes every other key unbound), so our four keys came out "Not Bound" with no
 * default to reset to (Codex's test, 2026-11-24). They get their defaults here, right after RimuLib made them
 * ({@code client/mixin/RimulibClientMixin}, the end of RimuLib's client start) and before the game reads options.txt,
 * so a key saved there still wins. A key RimuLib already gave a default is left alone, as is a skill with no key
 * (TA-02 without SS-21 Quasar). RimuLib internals ({@code SkillKeys}): if they change, one warning and the keys stay
 * as RimuLib made them.
 */
public final class SkillKeyDefaults {
    private SkillKeyDefaults() {
    }

    public static void give() {
        try {
            int given = give(Ton618Link.ON_TON618, InputConstants.KEY_SEMICOLON)
                + give(Ton618Link.ON_REMOTE, InputConstants.KEY_APOSTROPHE)
                + give(Ton618Link.QUASAR_ON_TON618, InputConstants.KEY_COMMA)
                + give(Ton618Link.QUASAR_ON_REMOTE, InputConstants.KEY_PERIOD);
            if (given > 0) {
                KeyMapping.resetMapping();
                Ton618Addition.LOG.info("Gave {} of TA-01's and TA-02's keys their defaults (; ' , .): RimuLib 26.3 "
                    + "makes defaults only for letter keys.", given);
            }
        } catch (RuntimeException | LinkageError failed) {
            Ton618Addition.LOG.warn("Couldn't give TA-01's and TA-02's keys their defaults (bind them in Controls): {}",
                failed.toString());
        }
    }

    private static int give(Skill skill, int code) {
        KeyMapping key = SkillKeys.of(skill);
        if (key == null || key.getDefaultKey().getValue() != InputConstants.UNKNOWN.getValue()) {
            return 0;
        }
        InputConstants.Key wanted = InputConstants.Type.KEYBOARD.getOrCreate(code);
        ((KeyMappingAccessor) key).ton618Addition$setDefaultKey(wanted);
        key.setKey(wanted);
        return 1;
    }
}
