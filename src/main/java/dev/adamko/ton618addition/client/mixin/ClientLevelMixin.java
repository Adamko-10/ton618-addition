package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.quasar.QuasarClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * TA-02 · Quasar Jets' second sun, as this client sees it: while TON618 blazes as a quasar, night turns to a blue-white
 * day (the land's light, the sky, the clouds) and the stars fade. Minecraft 1.21.11 keeps these as the client level's
 * environment attributes: layers of our own on top of them (as the SA Series' 1.21.11 sky). Only what this client draws
 * changes; the world's time on the server never does.
 */
@Mixin(ClientLevel.class)
abstract class ClientLevelMixin {
    @Inject(method = "addEnvironmentAttributeLayers(Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;)Lnet/minecraft/world/attribute/EnvironmentAttributeSystem$Builder;",
        at = @At("RETURN"))
    private void ton618Addition$secondSun(EnvironmentAttributeSystem.Builder layers,
                                          CallbackInfoReturnable<EnvironmentAttributeSystem.Builder> cir) {
        EnvironmentAttributeSystem.Builder b = cir.getReturnValue();
        b.addTimeBasedLayer(EnvironmentAttributes.SKY_LIGHT_FACTOR, (value, tick) -> {
            float d = secondSun();
            return d > 0f ? Mth.lerp(d, value, 1.0f) : value;
        });
        b.addTimeBasedLayer(EnvironmentAttributes.SKY_COLOR, (value, tick) -> {
            float d = secondSun();
            return d > 0f ? ARGB.srgbLerp(d, value, ARGB.colorFromFloat(1f, 0.42f, 0.62f, 1.0f)) : value;
        });
        b.addTimeBasedLayer(EnvironmentAttributes.CLOUD_COLOR, (value, tick) -> {
            float d = secondSun();
            return d > 0f ? ARGB.color(ARGB.alpha(value), ARGB.srgbLerp(d, value, ARGB.colorFromFloat(1f, 0.92f, 0.95f, 1.0f))) : value;
        });
        b.addTimeBasedLayer(EnvironmentAttributes.STAR_BRIGHTNESS, (value, tick) -> {
            float d = secondSun();
            return d > 0f ? value * (1f - d) : value;
        });
    }

    private static float secondSun() {
        return QuasarClient.secondSun(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true));
    }
}
