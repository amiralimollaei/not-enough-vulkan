package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.VkModOptionRegistry;
import net.vulkanmod.config.option.OptionPage;
import net.vulkanmod.config.option.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Options.class)
public class MixinVulkanOptions {
    @Inject(method = "getOptionPages", at = @At("RETURN"), remap = false)
    private static void notEnoughVulkan$mapVideoSettings(CallbackInfoReturnable<List<OptionPage>> cir) {
        VkModOptionRegistry.mapVideoSettings(cir.getReturnValue());
    }
}
