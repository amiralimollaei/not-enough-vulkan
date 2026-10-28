package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import net.minecraft.util.ARGB;
import net.vulkanmod.render.engine.VkGpuTexture;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// VulkanMod marks offscreen textures for a deferred clear but omits its color, this mixin fixes that.
@Mixin(value = VkGpuTexture.class, remap = false)
public class MixinVulkanGpuTexture {
    @Shadow
    private int clearColor;

    @Inject(method = "setClearColor", at = @At("HEAD"))
    private void sodiumExtra$storeClearColor(Vector4fc color, CallbackInfo ci) {
        this.clearColor = ARGB.colorFromFloat(color.w(), color.x(), color.y(), color.z());
    }
}
