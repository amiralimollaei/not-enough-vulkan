package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.skip_wayland_patches;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Minecraft.class, remap = false, priority = 1500)
public abstract class MinecraftMixinSquared {
    @TargetHandler(
            mixin = "net.vulkanmod.mixin.wayland.MinecraftMixin",
            name = "bypassWaylandIcon"
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
