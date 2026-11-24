package dev.adamko.ton618addition.client.mixin;

import dev.adamko.ton618addition.client.Ton618AdditionClient;
import dev.adamko.ton618addition.link.Ton618Link;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Last Light joins TON 618 just before RimuLib's client starts: by then every mod's main entrypoint has run (TON 618 and
 * The Shooting Star have registered their skills; Fabric runs main entrypoints in no fixed order), and RimuLib hasn't
 * made the skills' key bindings yet, so Last Light gets its own (the SA Series' RimulibClientMixin).
 */
@Pseudo
@Mixin(targets = "cyou.rimuru.rimulib.client.RimulibFabricClient", remap = false)
abstract class RimulibClientMixin {
    @Inject(method = "onInitializeClient()V", at = @At("HEAD"), require = 0, remap = false)
    private void ton618Addition$join(CallbackInfo ci) {
        Ton618Link.install();
        Ton618AdditionClient.afterConstruction();
    }

    /** Right after RimuLib made the skills' keys: ours get their defaults (RimuLib 26.3 gives only letter keys one). */
    @Inject(method = "onInitializeClient()V", at = @At("TAIL"), require = 0, remap = false)
    private void ton618Addition$keys(CallbackInfo ci) {
        dev.adamko.ton618addition.client.SkillKeyDefaults.give();
    }
}
