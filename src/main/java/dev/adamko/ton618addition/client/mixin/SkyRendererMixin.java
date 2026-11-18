package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets on Fabric 26.3: the named stars, the second sun and the far jet, drawn right after the vanilla sky
 * onto its target (the land and the clouds still cover them), as the SA Series' 26.3 sky dome is.
 */
@Mixin(SkyRenderer.class)
abstract class SkyRendererMixin {
    @Shadow
    @Final
    private RenderTarget renderTarget;

    @Inject(method = "render(Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lnet/minecraft/client/renderer/state/level/SkyRenderState;)V",
        at = @At("TAIL"))
    private void ton618Addition$afterSky(GpuBufferSlice skyFog, SkyRenderState state, CallbackInfo ci) {
        QuasarRender.skyStage(this.renderTarget);
    }
}
