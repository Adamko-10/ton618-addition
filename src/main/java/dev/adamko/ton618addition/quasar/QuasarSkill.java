package dev.adamko.ton618addition.quasar;

import cyou.rimuru.rimulib.mc1211.skill.Power;
import cyou.rimuru.rimulib.mc1211.skill.Skill;

/**
 * TA-02 · Quasar Jets as a skill in a RimuLib skill menu: one for TON 618's own item, one for the Stellar Remote; both
 * cast {@link QuasarJets#cast}. No sliders: the star you pick sets the size.
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

    @Override
    public Power power() {
        return null;
    }

    @Override
    public boolean powerSliders() {
        return false;
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
