package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarClient;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TA-02 · Quasar Jets on Fabric: the fog takes the second sun's blue-white (NeoForge's ViewportEvent.ComputeFogColor).
 * Minecraft 1.21.11 returns the colour it computes (as the SA Series' 1.21.11 fog mixin).
 */
@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Inject(method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IF)Lorg/joml/Vector4f;",
        at = @At("RETURN"))
    private void ton618Addition$fogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount,
                                        CallbackInfoReturnable<Vector4f> cir) {
        Vector4f fog = cir.getReturnValue();
        float[] rgb = QuasarClient.fogColor(partialTicks, fog.x, fog.y, fog.z);
        if (rgb != null) {
            fog.x = rgb[0];
            fog.y = rgb[1];
            fog.z = rgb[2];
        }
    }
}
