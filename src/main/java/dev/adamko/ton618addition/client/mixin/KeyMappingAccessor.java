package dev.adamko.ton618addition.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** A key's default, for {@link dev.adamko.ton618addition.client.SkillKeyDefaults} (Fabric 26.3). */
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
    @Mutable
    @Accessor("defaultKey")
    void ton618Addition$setDefaultKey(InputConstants.Key key);
}
