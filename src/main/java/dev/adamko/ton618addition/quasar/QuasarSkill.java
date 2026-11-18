package dev.adamko.ton618addition.quasar;

import cyou.rimuru.rimulib.skill.Power;
import cyou.rimuru.rimulib.skill.Skill;

/**
 * TA-02 · Quasar Jets as a skill in a RimuLib skill menu: one for TON 618's own item, one for the Stellar Remote; both
 * cast {@link QuasarJets#cast}. The star you pick sets the size; the Power slider (0.1x to 3x of it, a warning above 2x)
 * and the Damage slider (the hit at the blast's middle; ∞ kills everything in its reach) work as on The Shooting Star's
 * own skills (Adam, 2026-11-17: "a slider to select the power of the explosion just like the shooting star addition
 * and the original shooting star"; his pick: 0.1x to 3x). Without SS-21 Quasar it is in the menus but locked.
 */
public final class QuasarSkill implements Skill {
    private final int defaultKey;
    private final boolean onRemote;

    public QuasarSkill(int defaultKey, boolean onRemote) {
        this.defaultKey = defaultKey;
        this.onRemote = onRemote;
    }

    public boolean onRemote() {
        return this.onRemote;
    }

    @Override
    public String id() {
        return QuasarInfo.ID;
    }

    @Override
    public String tier() {
        return QuasarInfo.TIER;
    }

    @Override
    public String title() {
        return QuasarInfo.TITLE;
    }

    @Override
    public int color() {
        return QuasarInfo.COLOR;
    }

    @Override
    public int cooldown() {
        return QuasarInfo.COOLDOWN;
    }

    @Override
    public int duration() {
        return QuasarClock.END;
    }

    @Override
    public int defaultKey() {
        return this.defaultKey;
    }

    /** The Power slider: 0.1x to 3x the star's own blast; the menu shows it as N× and the crater's width in blocks. */
    @Override
    public Power power() {
        return new Power(0.1F, 3F, 1F, true, QuasarInfo.craterAcross.get());
    }

    @Override
    public float warnAbove() {
        return 2F;
    }

    @Override
    public boolean powerSliders() {
        return true;
    }

    @Override
    public boolean speedSlider() {
        return false;
    }

    @Override
    public String selectWarning() {
        return QuasarInfo.selectWarning.get();
    }

    @Override
    public String toString() {
        return QuasarInfo.TITLE;
    }
}
