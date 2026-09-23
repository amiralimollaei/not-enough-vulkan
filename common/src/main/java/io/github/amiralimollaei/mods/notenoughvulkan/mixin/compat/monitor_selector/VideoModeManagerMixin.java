package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.monitor_selector;

import com.mojang.blaze3d.platform.Window;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector.FullscreenMonitorManager;
import net.vulkanmod.config.video.VideoModeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = VideoModeManager.class, remap = false)
public class VideoModeManagerMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private static void notEnoughVulkan$initFullscreenMonitorManager(CallbackInfo ci) {
        FullscreenMonitorManager.init();
    }

    @Inject(method = "selectBestMonitor", at = @At("HEAD"), cancellable = true)
    private static void notEnoughVulkan$modifySelectedMonitor(Window window, CallbackInfo ci) {
        FullscreenMonitorManager.init();
        long selectedMonitor = FullscreenMonitorManager.getSelectedFullscreenMonitor();
        if (FullscreenMonitorManager.shouldApplyMonitorSelectorPatch() && selectedMonitor != 0L) {
            ci.cancel();
            VideoModeManager.selectedMonitor = selectedMonitor;
        }
    }
}
