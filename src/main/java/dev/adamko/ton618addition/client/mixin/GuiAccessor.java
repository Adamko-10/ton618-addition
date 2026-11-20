package dev.adamko.ton618addition.client.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The action bar's drawing, for {@link dev.adamko.ton618addition.client.HudRescue} in TON 618's dark. */
@Mixin(Gui.class)
public interface GuiAccessor {
    @Invoker("renderOverlayMessage")
    void ton618Addition$renderOverlayMessage(GuiGraphics graphics, DeltaTracker delta);
}
