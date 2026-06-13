package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.skip_wayland_patches;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InputConstants.class, remap = false, priority = 1500)
public abstract class InputConstantsMMixin {
    @TargetHandler(
            mixin = "net.vulkanmod.mixin.wayland.InputConstantsM",
            name = "grabOrReleaseMouse"
    )
    @ModifyExpressionValue(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/config/Platform;isWayLand()Z"
            )
    )
    private static boolean notenoughvulkan$skipWaylandPatches(boolean original) {
        if (NotEnoughVulkanClientMod.options().compatSettings.skipWaylandPatches || NotEnoughVulkanClientMod.options().compatSettings.forceX11) {
            return false;
        }
        return original;
    }
}
