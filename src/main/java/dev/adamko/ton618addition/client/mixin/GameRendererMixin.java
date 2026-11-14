package dev.adamko.ton618addition.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.adamko.ton618addition.client.HudRescue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Around the game's HUD call: draws what TON 618's hidden HUD skipped (see {@link HudRescue}). */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @WrapOperation(method = "render", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void ton618Addition$hud(Gui gui, GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        HudRescue.beforeGui();
        original.call(gui, graphics, delta);
        HudRescue.afterGui(graphics, delta);
    }
}
