package io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.vulkanmod.config.video.VideoModeManager;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;

import static org.lwjgl.glfw.GLFW.glfwGetMonitorName;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;

/// modified from [pull #618](https://github.com/xCollateral/VulkanMod/pull/618)
public final class FullscreenMonitorManager {
    private static final long NO_MONITOR = 0L;
    private static long selectedFullscreenMonitor = NO_MONITOR;

    private FullscreenMonitorManager() {
    }

    public static Long[] getMonitors() {
        return VideoModeManager.getMonitors().keySet().stream()
                .sorted(Comparator.comparing(FullscreenMonitorManager::monitorSortKey))
                .toArray(Long[]::new);
    }

    /// monitor selector patch should only apply when having multiple monitors
    /// this function checks whether we should apply monitor selector patches or not
    public static boolean shouldApplyMonitorSelectorPatch() {
        return VideoModeManager.getMonitors().size() > 1;
    }

    public static long getSelectedFullscreenMonitor() {
        return selectedFullscreenMonitor;
    }

    public static void setSelectedFullscreenMonitor(long monitor) {
        selectedFullscreenMonitor = monitor;
    }

    public static void applySelectedFullscreenMonitor() {
        selectedFullscreenMonitor = validMonitorOrFallback(selectedFullscreenMonitor);
        if (selectedFullscreenMonitor == NO_MONITOR) {
            return;
        }

        VideoModeManager.selectedMonitor = selectedFullscreenMonitor;
        String monitorName = getMonitorName(selectedFullscreenMonitor);
        NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.selectedMonitor =
                monitorName == null ? "" : monitorName;
        NotEnoughVulkanClientMod.notEnoughVulkanOptions().writeChanges();
        VideoModeManager.applySelectedVideoMode();
    }

    public static void init() {
        setSelectedFullscreenMonitor(findFullscreenMonitorHandle());
    }

    public static long findFullscreenMonitorHandle() {
        NotEnoughVulkanClientMod.getVulkanModConfig();
        String configuredMonitor = NotEnoughVulkanClientMod.notEnoughVulkanOptions()
                .patchesSettings.selectedMonitor;

        return VideoModeManager.getMonitors().keySet().stream()
                .filter(monitor -> configuredMonitor.equals(getMonitorName(monitor)))
                .findFirst()
                .orElseGet(() -> validMonitorOrFallback(glfwGetPrimaryMonitor()));
    }

    private static long validMonitorOrFallback(long monitor) {
        if (VideoModeManager.getMonitors().containsKey(monitor)) {
            return monitor;
        }

        long primaryMonitor = glfwGetPrimaryMonitor();
        if (VideoModeManager.getMonitors().containsKey(primaryMonitor)) {
            return primaryMonitor;
        }

        return VideoModeManager.getMonitors().keySet().stream()
                .findFirst()
                .orElse(NO_MONITOR);
    }

    private static String monitorSortKey(long monitor) {
        String name = getMonitorName(monitor);
        return name == null ? "" : name;
    }

    public static @Nullable String getMonitorName(long monitor) {
        try {
            return glfwGetMonitorName(monitor);
        } catch (Exception e) {
            return null;
        }
    }
}
