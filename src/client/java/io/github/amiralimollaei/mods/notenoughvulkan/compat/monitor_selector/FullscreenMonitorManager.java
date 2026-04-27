package io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.vulkanmod.config.Config;
import net.vulkanmod.config.video.VideoModeManager;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/// modified from [pull #618](https://github.com/xCollateral/VulkanMod/pull/618)
public class FullscreenMonitorManager {
    private static long selectedFullscreenMonitor = -1;

    public static @Nullable Long[] getMonitors() {
        return VideoModeManager.getMonitors().keySet().toArray(Long[]::new);
    }

    /// monitor selector patch should only apply when having multiple monitors
    /// this function checks whether we should apply monitor selector patches or not
    public static boolean shouldApplyMonitorSelectorPatch() {
        return VideoModeManager.getMonitors().size() > 1;
    }

    public static long getSelectedFullscreenMonitor() {
        return selectedFullscreenMonitor;
    }

    public static void setSelectedFullscreenMonitor(long new_monitor) {
        selectedFullscreenMonitor = new_monitor;
    }

    public static void applySelectedFullscreenMonitor() {
        NotEnoughVulkanClientMod.options().compatSettings.selectedMonitor = glfwGetMonitorName(selectedFullscreenMonitor);
        VideoModeManager.applySelectedVideoMode();
    }

    public static void init() {
        setSelectedFullscreenMonitor(findFullscreenMonitorHandle());
    }

    public static long findFullscreenMonitorHandle() {
        Config config = NotEnoughVulkanClientMod.getVulkanModConfig();
        if (config == null) return GLFW.glfwGetPrimaryMonitor();
        return VideoModeManager.getMonitors().keySet().stream()
                .filter(v -> {
                    String monitorName = glfwGetMonitorName(v);
                    if (monitorName == null) return false;
                    return monitorName.equals(NotEnoughVulkanClientMod.options().compatSettings.selectedMonitor);
                })
                .findFirst().orElse(GLFW.glfwGetPrimaryMonitor());
    }
}