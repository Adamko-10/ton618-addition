package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inside the game's sky drawing, where its own stars are drawn: TA-02's sky things when a shader pack is on
 * ({@link QuasarRender#skyPass}). A pack (Iris/Oculus) only treats what is drawn here as part of its sky, which it keeps
 * and draws its clouds over; drawn anywhere else it paints its sky over them. Right after the star brightness is asked
 * for, the pack has already moved its stage to the stars. On 1.20.1 the sky is drawn on a PoseStack that has been
 * turned for the sun by then: the camera's own pose is taken as the method starts.
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Unique
    private static final Matrix4f ton618Addition$view = new Matrix4f();

    @Inject(method = "renderSky", at = @At("HEAD"))
    private void ton618Addition$skyStart(PoseStack poseStack, Matrix4f projection, float partial, Camera camera, boolean foggy,
                                        Runnable fogSetup, CallbackInfo ci) {
        ton618Addition$view.set(poseStack.last().pose());
    }

    @Inject(method = "renderSky", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/multiplayer/ClientLevel;getStarBrightness(F)F", shift = At.Shift.AFTER))
    private void ton618Addition$skyPass(PoseStack poseStack, Matrix4f projection, float partial, Camera camera, boolean foggy,
                                       Runnable fogSetup, CallbackInfo ci) {
        QuasarRender.skyPass(new Matrix4f(ton618Addition$view), partial, camera);
    }
}
