package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.VulkanWorldTarget;
import net.vulkanmod.render.sky.CloudRenderer;
import net.vulkanmod.vulkan.pass.MainPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CloudRenderer.class, remap = false)
public class MixinVulkanCloudRenderer {
    @WrapOperation(
            method = "renderClouds",
            at = @At(value = "INVOKE",
                    target = "Lnet/vulkanmod/vulkan/pass/MainPass;rebindMainTarget()V",
                    ordinal = 0
            )
    )
    private void sodiumExtra$bindWorldTarget(MainPass instance, Operation<Void> original) {
        if (!VulkanWorldTarget.bindAlternateMainTarget(instance)) {
            original.call(instance);
        }
    }
}
