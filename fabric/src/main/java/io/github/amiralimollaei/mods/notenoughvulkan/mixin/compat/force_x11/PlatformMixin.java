package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.force_x11;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.amiralimollaei.mods.notenoughvulkan.client.NotEnoughVulkanClientMod;
import net.vulkanmod.config.Platform;
import org.spongepowered.asm.mixin.*;

import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_WAYLAND;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_X11;


@Mixin(value = Platform.class, remap = false)
public abstract class PlatformMixin {
    @WrapMethod(method = "getSupportedPlat")
    private static int modifyActivePlatform(Operation<Integer> original) {
        int activePlat = original.call();
        if (NotEnoughVulkanClientMod.options().compatSettings.forceX11 && activePlat == GLFW_PLATFORM_WAYLAND)
            activePlat = GLFW_PLATFORM_X11;
        return activePlat;
    }
}
