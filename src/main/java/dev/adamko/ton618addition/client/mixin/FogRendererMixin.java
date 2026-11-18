package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarClient;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets on Fabric: the fog takes the second sun's blue-white (NeoForge's ViewportEvent.ComputeFogColor).
 * Minecraft 26.3 fills in a colour it is handed (as the SA Series' 26.3 fog mixin).
 */
@Mixin(FogRenderer.class)
abstract class FogRendererMixin {
    @Inject(method = "computeFogColor(Lnet/minecraft/client/Camera;FLnet/minecraft/client/multiplayer/ClientLevel;IFLorg/joml/Vector4f;)V",
        at = @At("TAIL"))
    private void ton618Addition$fogColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float darkenWorldAmount,
                                        Vector4f fog, CallbackInfo ci) {
        float[] rgb = QuasarClient.fogColor(partialTicks, fog.x, fog.y, fog.z);
        if (rgb != null) {
            fog.x = rgb[0];
            fog.y = rgb[1];
            fog.z = rgb[2];
        }
    }
}
