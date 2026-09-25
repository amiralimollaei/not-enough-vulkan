package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import net.vulkanmod.config.option.Option;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ResettableValue;

@Mixin(value = OptionWidget.class, remap = false)
public abstract class MixinVulkanOptionWidget extends VAbstractWidget {
    @Shadow
    @Final Option<?> option;

    @Shadow
    protected abstract boolean clicked(double mouseX, double mouseY);

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void notEnoughVulkan$resetToDefault(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        if (event.button() == 0
                && event.hasShiftDown()
                && this.clicked(event.x(), event.y())
                && this.option instanceof ResettableValue resettable
                && resettable.resetToDefault()) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            cir.setReturnValue(true);
        }
    }
}