package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.VulkanWorldTarget;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.vulkan.pass.MainPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WorldRenderer.class, remap = false)
public class MixinVulkanWorldRenderer {
    @WrapOperation(
            method = "renderSectionLayer",
            at = @At(
                    value = "INVOKE",
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
