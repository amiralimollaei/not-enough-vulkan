package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod.profiler;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.vulkanmod.render.chunk.WorldRenderer;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProfilerOverlay.class)
public class ProfilerOverlayMixin {
    @WrapOperation(
            method = "updateResults",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/render/profiling/ProfilerOverlay;getBuildStats()Ljava/lang/String;"
            )
    )
    public String notEnoughVulkan$preventProfilerCrash(ProfilerOverlay instance, Operation<String> original) {
        if (WorldRenderer.getInstance().getTaskDispatcher().getResourcesArray() == null) {
            return null;
        }
        return original.call(instance);
    }
}
