package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod;

import net.vulkanmod.render.profiling.ProfilerOverlay;

public final class TimingsOverlayKeybindHandler {
    private static boolean customToggle;

    private TimingsOverlayKeybindHandler() {
    }

    public static void toggle() {
        customToggle = true;
        try {
            ProfilerOverlay.toggle();
        } finally {
            customToggle = false;
        }
    }

    public static boolean isCustomToggle() {
        return customToggle;
    }
}
