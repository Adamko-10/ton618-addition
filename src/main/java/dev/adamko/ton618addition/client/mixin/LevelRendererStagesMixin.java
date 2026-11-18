package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarRender;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The two points of the level's drawing TA-02 draws at that Fabric API has no event for (NeoForge's AFTER_SKY and
 * AFTER_PARTICLES, as the SA Series' Fabric branches do it): right after the vanilla sky (the sky's things without a
 * shader pack, the film's star labels) and right after the particles (everything, from the painted looks, with a pack).
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererStagesMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$afterSky(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera,
                                         GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        Matrix4f view = new Matrix4f(poseStack.last().pose());
        QuasarRender.stage(QuasarRender.Stage.AFTER_SKY, partialTick, camera, view, projectionMatrix);
        QuasarJetsFilm.afterSky(view, projectionMatrix, camera);
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V",
        shift = At.Shift.AFTER))
    private void ton618Addition$afterParticles(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera,
                                               GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        QuasarRender.stage(QuasarRender.Stage.AFTER_PARTICLES, partialTick, camera, new Matrix4f(poseStack.last().pose()), projectionMatrix);
    }
}
