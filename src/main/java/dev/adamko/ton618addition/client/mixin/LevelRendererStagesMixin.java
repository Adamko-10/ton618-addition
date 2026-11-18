package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.adamko.ton618addition.client.quasar.QuasarRender;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * TA-02 · Quasar Jets, Minecraft 26.3 (Fabric 26.3 branch): the level's camera turn and projection for the frame, from
 * the camera's render state, as the level is about to be drawn. The star names on the HUD and the film's labels need
 * them; the drawing itself hangs on Fabric's START_MAIN (right after the sky), COLLECT_SUBMITS (with a shader pack) and
 * END_MAIN (see QuasarClient).
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererStagesMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void ton618Addition$frame(GraphicsResourceAllocator allocator, boolean renderBlockOutline, CameraRenderState state, GpuBufferSlice fog,
                                      Vector4f fogColor, boolean renderSky, boolean other, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.mainCamera();
        if (state.viewRotationMatrix == null || state.projectionMatrix == null) {
            return;
        }
        QuasarRender.frame(mc.getDeltaTracker().getGameTimeDeltaPartialTick(true), camera, state.viewRotationMatrix, state.projectionMatrix);
        QuasarJetsFilm.afterSky(state.viewRotationMatrix, state.projectionMatrix, camera);
    }
}
