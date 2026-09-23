package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.TimingsOverlayKeybindHandler;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option.KeybindOptionWidget;
import io.github.amiralimollaei.mods.notenoughvulkan.config.TimingsKeybind;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ProfilerOverlay.class, remap = false)
public class TimingsOverlayMixin {
    @Inject(method = "toggle", at = @At("HEAD"), cancellable = true)
    private static void notEnoughVulkan$replaceDefaultKeybind(CallbackInfo ci) {
        if (KeybindOptionWidget.isCapturing()
                || !TimingsKeybind.DEFAULT.equals(NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.timingsKeybind)
                && !TimingsOverlayKeybindHandler.isCustomToggle()) {
            ci.cancel();
        }
    }
}
