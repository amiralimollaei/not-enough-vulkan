package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option.KeybindOptionWidget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.vulkanmod.config.gui.VOptionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = VOptionScreen.class, remap = false)
public class TimingsOptionScreenMixin {
    @Shadow
    private void updateState() {
        throw new AssertionError();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void notEnoughVulkan$captureTimingsKey(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (KeybindOptionWidget.capture(event)) {
            this.updateState();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "onClose", at = @At("HEAD"))
    private void notEnoughVulkan$stopCapture(CallbackInfo ci) {
        KeybindOptionWidget.stopCapture();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void notEnoughVulkan$stopCaptureOnClick(MouseButtonEvent event, boolean doubleClick,
            CallbackInfoReturnable<Boolean> cir) {
        KeybindOptionWidget.stopCapture();
    }
}
