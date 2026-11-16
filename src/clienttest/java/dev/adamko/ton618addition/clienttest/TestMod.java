package dev.adamko.ton618addition.clienttest;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dev-only mod for the {@code runClientTest} run (never in the release jar): {@link Harness} makes a world, casts TA-02 ·
 * Quasar Jets or TA-01 · Last Light by itself and saves screenshots by story tick, for Fable and Opus to look at the
 * effects without Adam. Does nothing unless {@code -Dton618test=true} (the run sets it).
 */
@Mod(TestMod.MOD_ID)
public final class TestMod {
    public static final String MOD_ID = "ton618_clienttest";

    public TestMod(Dist dist) {
        if (dist != Dist.CLIENT || !Boolean.getBoolean("ton618test")) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> Harness.tick());
        // the screenshots are the last thing a frame does: registered at the lowest priority once the game runs
        boolean[] armed = {false};
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            if (!armed[0]) {
                armed[0] = true;
                NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderFrameEvent.Post.class, event2 -> Harness.frame());
            }
        });
        Harness.log("armed: " + Harness.describe());
    }
}
