package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets on Fabric 26.3: the camera's shake at the landing (NeoForge's ViewportEvent.ComputeCameraAngles),
 * right after the camera is set up for the frame and before its render state is taken from it.
 */
@Mixin(Camera.class)
abstract class CameraShakeMixin {
    private static final float DEG = (float) (Math.PI / 180.0);

    @Inject(method = "update(Lnet/minecraft/client/DeltaTracker;)V", at = @At("TAIL"))
    private void ton618Addition$shake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = (Camera) (Object) this;
        float[] add = QuasarRender.shake(camera, deltaTracker.getGameTimeDeltaPartialTick(true));
        if (add == null) {
            return;
        }
        float pitch = camera.xRot() + add[0];
        float yaw = camera.yRot() + add[1];
        ((CameraAccessor) camera).ton618Addition$setRotation(yaw, pitch);
        if (add[2] != 0F) {
            // the roll, as NeoForge's camera turns it in (the view is built from this rotation right after)
            camera.rotation().rotationYXZ((float) Math.PI - yaw * DEG, -pitch * DEG, -add[2] * DEG);
        }
    }
}
