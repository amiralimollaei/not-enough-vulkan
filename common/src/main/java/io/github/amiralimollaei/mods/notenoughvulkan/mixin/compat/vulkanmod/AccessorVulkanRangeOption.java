package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import net.vulkanmod.config.option.RangeOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RangeOption.class, remap = false)
public interface AccessorVulkanRangeOption {
    @Accessor("max")
    int notEnoughVulkan$getMax();
}