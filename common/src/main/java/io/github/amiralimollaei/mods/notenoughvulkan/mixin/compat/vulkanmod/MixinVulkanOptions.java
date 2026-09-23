package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.VkModOptionRegistry;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Options.class)
public class MixinVulkanOptions {
    @ModifyReturnValue(method = "getOptionPages", at = @At("RETURN"), remap = false)
    private static List<OptionPage> notEnoughVulkan$processVideoOptions(List<OptionPage> pages) {
        if (pages == null) return null;
        VkModOptionRegistry.mapVideoSettings(pages);
        return pages;
    }
}
