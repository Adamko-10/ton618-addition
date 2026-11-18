package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
    @Inject(method = "render(FJZ)V", at = @At("HEAD"))
    private void ton618Addition$frameStart(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        QuasarRender.paint(partialTicks);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/world/entity/Entity;ZZF)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$shake(float partialTicks, long finishTimeNano, PoseStack poseStack, CallbackInfo ci) {
        Camera camera = ((GameRenderer) (Object) this).getMainCamera();
        float[] add = QuasarRender.shake(camera, partialTicks);
        if (add == null) {
            return;
        }
        float pitch = camera.getXRot() + add[0];
        float yaw = camera.getYRot() + add[1];
        ((CameraAccessor) camera).ton618Addition$setRotation(yaw, pitch);
        if (add[2] != 0F) {
            // the roll, before the view's pitch and yaw are put on the pose (as Forge's ComputeCameraAngles does on 1.20.1)
            poseStack.mulPose(Axis.ZP.rotationDegrees(add[2]));
        }
    }
}
