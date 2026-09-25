package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod.profiler;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import net.vulkanmod.render.profiling.BuildTimeProfiler;
import net.vulkanmod.render.profiling.ProfilerOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class TimingsKeyboardMixin {
    @Inject(
            method = {"keyPress"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V",
                    ordinal = 1,
                    shift = At.Shift.AFTER
            )}
    )
    private void notEnoughVulkan$toggleProfiler(long handle, int action, KeyEvent event, CallbackInfo ci) {
        if (NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkProfilerOverlayKeybind.matches(event)) {
            ProfilerOverlay.toggle();
        } else if (ProfilerOverlay.shouldRender) {
            ProfilerOverlay.onKeyPress(event.key());
        }
        if (NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkBuildTimeProfilerKeybind.matches(event)) {
            BuildTimeProfiler.startBench();
        }
    }
}
