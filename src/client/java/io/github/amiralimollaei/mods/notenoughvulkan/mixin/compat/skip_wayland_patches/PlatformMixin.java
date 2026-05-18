package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.skip_wayland_patches;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.vulkanmod.config.Platform;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Platform.class, remap = false)
public abstract class PlatformMixin {
    @WrapMethod(method = "isWayLand")
    private static boolean notenoughvulkan$skipWaylandPatches(Operation<Boolean> original) {
        if (NotEnoughVulkanClientMod.options().compatSettings.skipWaylandPatches) {
            return false;
        }
        return original.call();
    }
}
