package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.TimingsOverlayKeybindHandler;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option.KeybindOptionWidget;
import io.github.amiralimollaei.mods.notenoughvulkan.config.TimingsKeybind;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class TimingsKeyboardMixin {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void notEnoughVulkan$toggleTimings(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (action != 1 || Minecraft.getInstance().gui.screen() != null || KeybindOptionWidget.isCapturing()) {
            return;
        }

        TimingsKeybind binding = NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.timingsKeybind;
        if (!TimingsKeybind.DEFAULT.equals(binding) && binding.matches(event)) {
            TimingsOverlayKeybindHandler.toggle();
        }
    }
}
