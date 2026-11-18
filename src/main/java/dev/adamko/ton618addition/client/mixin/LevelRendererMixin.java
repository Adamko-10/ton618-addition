package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inside the game's sky drawing, where its own stars are drawn: TA-02's sky things when a shader pack is on
 * ({@link QuasarRender#skyPass}). A pack (Iris) only treats what is drawn here as part of its sky, which it keeps and
 * draws its clouds over; drawn anywhere else it paints its sky over them. Right after the star brightness is asked for,
 * Iris has already moved its stage to the stars (not the sun or the moon, which a pack may hide).
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Inject(method = "renderSky", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F", shift = At.Shift.AFTER))
    private void ton618Addition$skyPass(Matrix4f frustum, Matrix4f projection, float partial, Camera camera, boolean foggy,
                                       Runnable fogSetup, CallbackInfo ci) {
        QuasarRender.skyPass(frustum, partial, camera);
    }
}
