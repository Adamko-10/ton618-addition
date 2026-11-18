package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.film.LastLightFilm;
import dev.adamko.ton618addition.client.quasarfilm.QuasarJetsFilm;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The game's toasts are held back while a film has the camera (NeoForge's ToastAddEvent); shown when it lets go. */
@Mixin(ToastComponent.class)
abstract class ToastComponentMixin {
    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void ton618Addition$holdToast(Toast toast, CallbackInfo ci) {
        if (LastLightFilm.holdToast(toast) || QuasarJetsFilm.holdToast(toast)) {
            ci.cancel();
        }
    }
}
