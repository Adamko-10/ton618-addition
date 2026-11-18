package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TA-02's second sun on this client: while it shines, the night's sky light, sky colour, clouds and stars are lifted
 * toward a cold day ({@link QuasarClient#secondSun}). Only what this client draws: the server's time, mobs and crops
 * are untouched. Plain injections at RETURN, so every loader's branch can share it.
 */
@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {
    @Inject(method = "getSkyDarken(F)F", at = @At("RETURN"), cancellable = true)
    private void ton618Addition$secondSunLight(float partial, CallbackInfoReturnable<Float> cir) {
        float d = QuasarClient.secondSun(partial);
        if (d > 0f) {
            cir.setReturnValue(Mth.lerp(d, cir.getReturnValueF(), 1.0f));
        }
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void ton618Addition$secondSunSky(Vec3 pos, float partial, CallbackInfoReturnable<Vec3> cir) {
        float d = QuasarClient.secondSun(partial);
        if (d > 0f) {
            cir.setReturnValue(cir.getReturnValue().lerp(new Vec3(0.42, 0.62, 1.0), d));
        }
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void ton618Addition$secondSunClouds(float partial, CallbackInfoReturnable<Vec3> cir) {
        float d = QuasarClient.secondSun(partial);
        if (d > 0f) {
            cir.setReturnValue(cir.getReturnValue().lerp(new Vec3(0.92, 0.95, 1.0), d));
        }
    }

    @Inject(method = "getStarBrightness", at = @At("RETURN"), cancellable = true)
    private void ton618Addition$secondSunStars(float partial, CallbackInfoReturnable<Float> cir) {
        float d = QuasarClient.secondSun(partial);
        if (d > 0f) {
            cir.setReturnValue(cir.getReturnValueF() * (1f - d));
        }
    }
}
