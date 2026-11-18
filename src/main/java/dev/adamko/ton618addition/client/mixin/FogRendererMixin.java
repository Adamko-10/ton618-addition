package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.adamko.ton618addition.client.quasar.QuasarClient;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Under TA-02's second sun the fog turns to its sky (NeoForge's ViewportEvent.ComputeFogColor): the colour Minecraft
 * has just worked out is changed, and the screen is cleared to the new one (it has already been cleared to the old).
 */
@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void ton618Addition$fogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistanceChunks,
                                               float bossColorModifier, CallbackInfo ci) {
        float[] rgb = QuasarClient.fogColor(partialTicks, fogRed, fogGreen, fogBlue);
        if (rgb != null) {
            fogRed = rgb[0];
            fogGreen = rgb[1];
            fogBlue = rgb[2];
            RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
        }
    }
}
