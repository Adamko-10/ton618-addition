package dev.adamko.ton618addition.client.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * The action bar's state and backdrop, for {@link dev.adamko.ton618addition.client.HudRescue} in TON 618's dark
 * (vanilla 1.20.1 draws the action bar inline in {@code Gui.render}).
 */
@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("overlayMessageString")
    Component ton618Addition$overlayMessageString();

    @Accessor("overlayMessageTime")
    int ton618Addition$overlayMessageTime();

    @Accessor("animateOverlayMessageColor")
    boolean ton618Addition$animateOverlayMessageColor();

    @Invoker("drawBackdrop")
    void ton618Addition$drawBackdrop(GuiGraphics graphics, Font font, int y, int width, int colour);
}
