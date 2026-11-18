package dev.adamko.ton618addition.client.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** The camera's own way to turn it (protected in vanilla), for TA-02's shake on Fabric. */
@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setRotation")
    void ton618Addition$setRotation(float yaw, float pitch);
}
