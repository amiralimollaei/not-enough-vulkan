package me.flashyreese.mods.sodiumextra.mixin.reduce_resolution_on_mac;

import me.flashyreese.mods.sodiumextra.client.util.MacReducedResolution;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.vulkanmod.render.engine.VkBackend", remap = false)
public class MixinVkBackend {
    @Inject(method = "setWindowHints", at = @At("HEAD"))
    private void sodiumExtra$setWindowHints(CallbackInfo ci) {
        MacReducedResolution.useVulkanModBackend();
        if (MacReducedResolution.isEnabled()) {
            GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, GLFW.GLFW_FALSE);
        }
    }
}
