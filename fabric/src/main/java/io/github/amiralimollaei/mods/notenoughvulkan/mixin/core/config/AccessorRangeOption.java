package io.github.amiralimollaei.mods.notenoughvulkan.mixin.core.config;

import net.vulkanmod.config.option.RangeOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RangeOption.class)
public interface AccessorRangeOption {
    @Accessor("min")
    int notenoughvulkan$getMin();

    @Accessor("max")
    int notenoughvulkan$getMax();

    @Accessor("step")
    int notenoughvulkan$getStep();
}
