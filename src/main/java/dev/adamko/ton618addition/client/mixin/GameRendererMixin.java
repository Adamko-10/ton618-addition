package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.HudRescue;
import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Around the game's HUD call: draws what TON 618's hidden HUD skipped (see {@link HudRescue}). Plain injections, no
 * MixinExtras, so every loader's branch shares it; the frame's projection is still the HUD's right after the call, so
 * a fresh {@link GuiGraphics} on the game's buffers draws in the same place.
 */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    private static final String GUI_RENDER =
        "Lnet/minecraft/client/gui/Gui;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V";

    @Inject(method = "render", at = @At(value = "INVOKE", target = GUI_RENDER))
    private void ton618Addition$beforeHud(DeltaTracker delta, boolean renderLevel, CallbackInfo ci) {
        // TA-02's world effects, when a shader pack painted over them in the level's passes
        QuasarRender.late();
        HudRescue.beforeGui();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = GUI_RENDER, shift = At.Shift.AFTER))
    private void ton618Addition$afterHud(DeltaTracker delta, boolean renderLevel, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        HudRescue.afterGui(new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource()), delta);
    }
}
