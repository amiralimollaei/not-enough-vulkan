package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.bobby;

import de.johni0702.minecraft.bobby.Bobby;
import net.vulkanmod.config.option.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = Options.class, remap = false)
public abstract class OptionsMixin {
    @ModifyArg(
            method = "getGraphicsOpts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/config/option/RangeOption;<init>(Lnet/minecraft/network/chat/Component;IIILjava/util/function/Consumer;Ljava/util/function/Supplier;)V",
                    ordinal = 1
            ),
            index = 2
    )
    private static int notenoughvulkan$increaseMaxRenderDistance(int maxRenderDistance) {
        return Bobby.getInstance().getConfig().getMaxRenderDistance();
    }
}
