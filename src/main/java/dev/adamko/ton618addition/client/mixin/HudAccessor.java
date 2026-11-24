package dev.adamko.ton618addition.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The action bar's drawing, for {@link dev.adamko.ton618addition.client.HudRescue} in TON 618's dark. */
@Mixin(Hud.class)
public interface HudAccessor {
    @Invoker("extractOverlayMessage")
    void ton618Addition$extractOverlayMessage(GuiGraphicsExtractor graphics, DeltaTracker delta);
}
