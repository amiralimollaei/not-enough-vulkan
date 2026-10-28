package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.vulkanmod.render.engine.VkGpuTexture;
import net.vulkanmod.render.engine.VkTextureView;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.pass.MainPass;

public final class VulkanWorldTarget {
    private VulkanWorldTarget() {
    }

    public static boolean bindAlternateMainTarget(MainPass mainPass) {
        RenderTarget target = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        VkTextureView colorView = (VkTextureView) target.getColorTextureView();
        VkGpuTexture color = colorView.texture();
        VkGpuTexture depth = (VkGpuTexture) target.getDepthTexture();
        GpuTextureView nativeView = mainPass.getColorAttachmentView();

        // Let the main pass handle its own swapchain and render-pass lifecycle.
        if (nativeView != null && color == nativeView.texture()
                && colorView.baseMipLevel() == nativeView.baseMipLevel()
                && depth == mainPass.getDepthAttachment()) {
            return false;
        }

        Framebuffer bound = Renderer.getInstance().getBoundFramebuffer();
        boolean targetBound = bound != null
                && bound.getColorAttachment() == color.getVulkanImage()
                && bound.getColorAttachmentView() == color.getVulkanImage().getLevelImageView(colorView.baseMipLevel())
                && bound.getDepthAttachment() == (depth == null ? null : depth.getVulkanImage());
        if (targetBound) {
            // Binding also consumes deferred clears, even if the attachments already match.
            if (color.needsClear() || (depth != null && depth.needsClear())) {
                colorView.getFbo(depth).bind();
            }
            return true;
        }
        colorView.getFbo(depth).bind();
        return true;
    }
}
