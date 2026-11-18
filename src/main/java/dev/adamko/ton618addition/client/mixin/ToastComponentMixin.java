package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The films hold the game's toasts back while they have the camera (NeoForge's cancellable ToastAddEvent; Minecraft
 * 1.21.11 calls the toast list ToastManager).
 */
@Mixin(ToastManager.class)
abstract class ToastComponentMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void ton618Addition$holdToast(Toast toast, CallbackInfo ci) {
        if (LastLightFilm.holdToast(toast) || QuasarJetsFilm.holdToast(toast)) {
            ci.cancel();
        }
    }
}
