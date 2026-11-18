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
 * TA-02 · Quasar Jets on Fabric: the frame's start (NeoForge's RenderFrameEvent.Pre: the shader-pack looks are painted
 * then) and the camera's shake at the landing (NeoForge's ViewportEvent.ComputeCameraAngles: right after the camera is
 * set up, before the view is built from it). Minecraft 1.21.11 sets the camera up in {@code updateCamera}, then copies it
 * into the level's render state.
 */
@Mixin(GameRenderer.class)
abstract class QuasarGameRendererMixin {
    private static final float DEG = (float) (Math.PI / 180.0);

    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void ton618Addition$frameStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        QuasarRender.paint(deltaTracker.getGameTimeDeltaPartialTick(true));
    }

    /**
     * Right after the level is drawn, RimuLib's screen effects included (TON 618's dark picture is painted at the end of
     * LevelRenderer.renderLevel): Last Light's light in the dark goes on top of it (Fable's proposal P2 for Fabric).
     */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$afterLevel(DeltaTracker deltaTracker, CallbackInfo ci) {
        dev.adamko.ton618addition.client.film.LastLightFilm.afterLevel();
    }

    @Inject(method = "updateCamera", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/Camera;setup(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;ZZF)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$shake(DeltaTracker deltaTracker, CallbackInfo ci) {
        Camera camera = ((GameRenderer) (Object) this).getMainCamera();
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
