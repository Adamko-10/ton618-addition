package dev.adamko.ton618addition.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.adamko.ton618addition.client.HudRescue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Right after the HUD (Fabric 26.3 branch): the film's screen layer and Last Light's notice, whether the HUD is hidden
 * (F1) or not, as on the other branches. Optional: without it, {@link HudRescue}'s HUD element draws them instead.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @WrapOperation(method = "extractRenderState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Hud;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void ton618_addition$afterHud(Hud hud, GuiGraphicsExtractor graphics, DeltaTracker delta, Operation<Void> original) {
        HudRescue.viaMixin();
        original.call(hud, graphics, delta);
        HudRescue.afterHud(graphics, delta);
    }
}
