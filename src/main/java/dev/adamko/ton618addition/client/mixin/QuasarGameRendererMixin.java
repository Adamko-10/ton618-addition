package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02's two frame hooks Fabric API has no event for: the frame's start (NeoForge's RenderFrameEvent.Pre: the looks
 * painted for a shader pack) and the camera's shake (NeoForge's ViewportEvent.ComputeCameraAngles: right after the
 * camera is set up, before the view is built from it).
 */
@Mixin(GameRenderer.class)
abstract class QuasarGameRendererMixin {
    private static final float DEG = (float) (Math.PI / 180.0);

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void ton618Addition$frameStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        QuasarRender.paint(deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$shake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = ((GameRenderer) (Object) this).getMainCamera();
        float[] add = QuasarRender.shake(camera, deltaTracker.getGameTimeDeltaPartialTick(true));
        if (add == null) {
            return;
        }
        float pitch = camera.getXRot() + add[0];
        float yaw = camera.getYRot() + add[1];
        ((CameraAccessor) camera).ton618Addition$setRotation(yaw, pitch);
        if (add[2] != 0F) {
            // the roll, as NeoForge's camera turns it in (the view is built from this rotation right after)
            camera.rotation().rotationYXZ((float) Math.PI - yaw * DEG, -pitch * DEG, -add[2] * DEG);
        }
    }
}
