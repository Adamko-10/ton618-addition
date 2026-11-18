package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets on Fabric 26.3: the frame's start (NeoForge's RenderFrameEvent.Pre: the shader-pack looks are
 * painted then; 26.3 gathers the frame in {@code GameRenderer.extract}), and Last Light's light in TON 618's dark right
 * after the level is drawn (RimuLib paints TON 618's dark picture at the end of LevelRenderer.render; Fable's proposal
 * P2 for Fabric).
 */
@Mixin(GameRenderer.class)
abstract class QuasarGameRendererMixin {
    @Inject(method = "extract(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void ton618Addition$frameStart(DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci) {
        QuasarRender.paint(deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "renderLevel()V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$afterLevel(CallbackInfo ci) {
        dev.adamko.ton618addition.client.film.LastLightFilm.afterLevel();
    }
}
