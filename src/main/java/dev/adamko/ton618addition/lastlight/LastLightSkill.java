package dev.adamko.ton618addition.lastlight;

import cyou.rimuru.rimulib.skill.Power;
import cyou.rimuru.rimulib.skill.Skill;

/**
 * TA-01 · Last Light as a skill in a RimuLib skill menu. RimuLib keeps one set per skill, so there is one of these for
 * TON 618's own item and one for The Shooting Star's Stellar Remote; both cast {@link LastLight#cast}.
 */
public final class LastLightSkill implements Skill {
    private final int defaultKey;
    private final boolean onRemote;

    public LastLightSkill(int defaultKey, boolean onRemote) {
        this.defaultKey = defaultKey;
        this.onRemote = onRemote;
    }

    /** True for the copy on The Shooting Star's Stellar Remote. */
    public boolean onRemote() {
        return this.onRemote;
    }

    @Override
    public String id() {
        return LastLightInfo.ID;
    }

    @Override
    public String tier() {
        return LastLightInfo.TIER;
    }

    @Override
    public String title() {
        return LastLightInfo.TITLE;
    }

    @Override
    public int color() {
        return LastLightInfo.COLOR;
    }

    @Override
    public int cooldown() {
        return LastLightInfo.COOLDOWN;
    }

    @Override
    public int duration() {
        return LastLightClock.END;
    }

    @Override
    public int defaultKey() {
        return this.defaultKey;
    }

    /** No sliders: the fast-forward always runs to the end. */
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
        return LastLightInfo.selectWarning.get();
    }

    @Override
    public String toString() {
        return LastLightInfo.TITLE;
    }
}
