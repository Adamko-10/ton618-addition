package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import dev.adamko.ton618addition.client.quasar.QuasarRender;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets, Minecraft 1.21.11 (Fabric 1.21.11 branch): the level's camera turn and projection for the frame,
 * as the level is about to be drawn. Fabric's world events on 1.21.11 don't hand them over, and the star names on the
 * HUD and the film's labels need them. The drawing itself hangs on Fabric's START_MAIN (right after the sky),
 * AFTER_ENTITIES (with a shader pack) and END_MAIN (see QuasarClient).
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererStagesMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void ton618Addition$frame(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera,
                                      Matrix4f view, Matrix4f projection, Matrix4f cullingProjection, GpuBufferSlice fog, Vector4f fogColor,
                                      boolean renderSky, CallbackInfo ci) {
        QuasarRender.frame(deltaTracker.getGameTimeDeltaPartialTick(true), camera, view, projection);
        QuasarJetsFilm.afterSky(view, projection, camera);
    }
}
