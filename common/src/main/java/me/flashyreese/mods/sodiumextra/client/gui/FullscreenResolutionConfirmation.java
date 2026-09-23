package me.flashyreese.mods.sodiumextra.client.gui;

import com.mojang.blaze3d.platform.Window;
import io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector.FullscreenMonitorManager;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Util;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import org.lwjgl.glfw.GLFW;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Schedules the post-switch confirmation prompt for Wayland/XWayland fullscreen mode changes.
 * The prompt handles bad modes that return; pre-launch recovery handles hangs before it can open.
 */
public final class FullscreenResolutionConfirmation {
    private static boolean requested;
    private static Optional<FullscreenTarget> pendingPreviousTarget = Optional.empty();
    private static Optional<FullscreenTarget> previousTarget = Optional.empty();
    private static final List<WeakReference<Option<?>>> fullscreenResolutionControls = new ArrayList<>();

    /**
     * Called immediately before VulkanMod replaces {@link VideoModeManager#selectedVideoMode}.
     * This deliberately observes the existing CyclingOption callback instead of replacing its
     * control or changing how VulkanMod chooses a mode.
     */
    public static void request(VideoModeSet.VideoMode previousVideoMode, VideoModeSet.VideoMode requestedVideoMode) {
        if (!canConfirmChanges() || sameMode(previousVideoMode, requestedVideoMode)) {
            return;
        }

        SodiumExtraClientMod.armWaylandFullscreenResolutionRecovery();
        requested = true;
        previousTarget = pendingPreviousTarget.or(() -> Optional.of(captureCurrentTarget(previousVideoMode)));
        pendingPreviousTarget = Optional.empty();
    }

    public static void tick(Minecraft client) {
        if (!requested) {
            // A monitor change can be applied without a video-mode change. Do not let that
            // one-shot snapshot leak into a later, unrelated resolution confirmation.
            pendingPreviousTarget = Optional.empty();
            return;
        }

        if (client.getWindow() == null) {
            return;
        }

        // A single Apply can include both a video-mode change and disabling this opt-in.
        // In that case, honor the final setting rather than presenting a stale prompt.
        if (!canConfirmChanges()) {
            requested = false;
            previousTarget = Optional.empty();
            SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
            return;
        }

        requested = false;
        Optional<FullscreenTarget> target = previousTarget;
        previousTarget = Optional.empty();
        // The confirmation temporarily replaces VulkanMod's settings screen. Keep that exact
        // instance so returning from the prompt does not discard the user's other edits.
        Screen previousScreen = client.gui.screen();
        client.setScreenAndShow(new FullscreenResolutionConfirmScreen(target, previousScreen));
    }

    static void keep() {
        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
    }

    static void revert(Optional<FullscreenTarget> previousTarget) {
        previousTarget.ifPresent(target -> {
            if (target.monitor() != null) {
                FullscreenMonitorManager.setSelectedFullscreenMonitor(target.monitor());
                VideoModeManager.selectedMonitor = target.monitor();
                FullscreenMonitorManager.applySelectedFullscreenMonitor();
            }
            if (target.videoMode() != null) {
                VideoModeManager.selectedVideoMode = copyOf(target.videoMode());
                VideoModeManager.applySelectedVideoMode();
                Options.fullscreenDirty = true;

                Window window = Minecraft.getInstance().getWindow();
                if (window != null) {
                    window.changeFullscreenVideoMode();
                }

                if (Initializer.CONFIG != null) {
                    Initializer.CONFIG.write();
                }
            }
        });
        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
    }

    /** Captures the complete target before VulkanMod's monitor CyclingOption applies its new value. */
    public static void rememberCurrentFullscreenTarget() {
        pendingPreviousTarget = Optional.of(captureCurrentTarget(VideoModeManager.selectedVideoMode));
    }

    public static boolean isWaylandOrXWayland() {
        if (Util.getPlatform() != Util.OS.LINUX) {
            return false;
        }

        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND
                || System.getenv("WAYLAND_DISPLAY") != null
                || "wayland".equalsIgnoreCase(sessionType);
    }

    /**
     * On Wayland/XWayland this experimental opt-in controls whether VulkanMod's existing
     * resolution and refresh-rate CyclingOptions can be edited. Other platforms retain
     * VulkanMod's normal behaviour.
     */
    public static boolean isFullscreenResolutionControlEnabled() {
        return !isWaylandOrXWayland()
                || SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution;
    }

    public static void gateFullscreenResolutionControls(Option<WindowMode> windowMode,
            Option<?> resolution, Option<?> refreshRate) {
        resolution.setActivationFn(() -> windowMode.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN
                && isFullscreenResolutionControlEnabled());
        refreshRate.setActivationFn(() -> windowMode.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN
                && isFullscreenResolutionControlEnabled());
        fullscreenResolutionControls.add(new WeakReference<>(resolution));
        fullscreenResolutionControls.add(new WeakReference<>(refreshRate));
        updateFullscreenResolutionControlState();
    }

    /** Updates controls already displayed by VulkanMod when the Extras opt-in changes. */
    public static void updateFullscreenResolutionControlState() {
        Iterator<WeakReference<Option<?>>> iterator = fullscreenResolutionControls.iterator();
        while (iterator.hasNext()) {
            Option<?> option = iterator.next().get();
            if (option == null) {
                iterator.remove();
                continue;
            }

            // VulkanMod's updateActiveState assumes a widget is present.
            option.getWidget();
            option.updateActiveState();
        }
    }

    private static boolean canConfirmChanges() {
        Minecraft client = Minecraft.getInstance();
        return isWaylandOrXWayland()
                && client != null
                && Boolean.TRUE.equals(client.options.fullscreen().get())
                && SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution;
    }

    private static boolean sameMode(VideoModeSet.VideoMode first, VideoModeSet.VideoMode second) {
        return first != null && second != null
                && first.width == second.width
                && first.height == second.height
                && first.bitDepth == second.bitDepth
                && first.refreshRate == second.refreshRate;
    }

    private static VideoModeSet.VideoMode copyOf(VideoModeSet.VideoMode mode) {
        return mode == null ? null : new VideoModeSet.VideoMode(mode.width, mode.height, mode.bitDepth, mode.refreshRate);
    }

    private static FullscreenTarget captureCurrentTarget(VideoModeSet.VideoMode videoMode) {
        long monitor = FullscreenMonitorManager.getSelectedFullscreenMonitor();
        if (monitor == -1) {
            monitor = VideoModeManager.selectedMonitor;
        }
        return new FullscreenTarget(monitor == 0 ? null : monitor, copyOf(videoMode));
    }

    /** Immutable, defensive snapshot used by one confirmation transaction. */
    record FullscreenTarget(Long monitor, VideoModeSet.VideoMode videoMode) {
    }
}
