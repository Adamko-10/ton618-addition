package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.HudRescue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Notes that the game's HUD ran to its end this frame (see {@link HudRescue}). */
@Mixin(Gui.class)
abstract class GuiMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void ton618Addition$reachedEnd(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        HudRescue.guiReachedEnd();
    }
}
