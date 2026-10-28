package me.flashyreese.mods.sodiumextra.mixin.panini_projection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.render.sky.CloudRenderer;
import net.vulkanmod.vulkan.Renderer;
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
        Minecraft minecraft = Minecraft.getInstance();
        Renderer renderer = Renderer.getInstance();
        // leave custom targets alone.
        if (renderer.getBoundFramebuffer() == renderer.getMainPass().getMainFramebuffer()) {
            RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
            ((VkTextureView) target.getColorTextureView()).getFbo(target.getDepthTexture()).bind();
        } else {
            original.call(instance);
        }
    }
}
