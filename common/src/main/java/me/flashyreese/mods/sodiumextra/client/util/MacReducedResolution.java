package me.flashyreese.mods.sodiumextra.client.util;

import com.mojang.blaze3d.systems.GpuSurface;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.minecraft.util.Util;

public final class MacReducedResolution {
    /*
     * Vulkan framebuffer dimensions must agree with the presentation surface:
     * - Minecraft's Vulkan backend explicitly reduces the swapchain configuration.
     * - VulkanMod requests a non-Retina Metal layer. GLFW still reports Cocoa
     *   backing pixels, so reduce Minecraft's framebuffer once to match that
     *   layer and VulkanMod's swapchain.
     * Rendering a reduced texture into a full-size swapchain fixed the border
     * but caused bad 1% lows on the Mac tester's machine.
     */
    private enum Backend {
        UNKNOWN,
        VULKAN,
        VULKAN_MOD
    }

    private static Backend backend = Backend.UNKNOWN;
    private static boolean enabledForWindow;
    private static int windowWidth = -1;
    private static int windowHeight = -1;

    public static boolean isEnabled() {
        return enabledForWindow;
    }

    public static int reduce(int value) {
        return Math.max(1, value / 2);
    }

    public static void useVulkanBackend() {
        useBackend(Backend.VULKAN);
    }

    public static void useVulkanModBackend() {
        useBackend(Backend.VULKAN_MOD);
    }

    private static void useBackend(Backend selectedBackend) {
        backend = selectedBackend;
        // Snapshot this restart-required option when window hints are prepared.
        // Reading the live setting during presentation can disable upscaling
        // while the current window still uses a reduced render target.
        enabledForWindow = Util.getPlatform() == Util.OS.OSX
                && SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac;
        windowWidth = -1;
        windowHeight = -1;
    }

    public static void rememberWindowSize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
    }

    public static boolean shouldReduceFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return isEnabled() && (backend == Backend.VULKAN || backend == Backend.VULKAN_MOD)
                && isHighDpiFramebuffer(framebufferWidth, framebufferHeight, windowWidth, windowHeight);
    }

    public static GpuSurface.Configuration reduceSurfaceConfiguration(GpuSurface.Configuration config) {
        if (!isEnabled() || backend != Backend.VULKAN || !isHighDpiFramebuffer(config.width(), config.height(), windowWidth, windowHeight)) {
            return config;
        }

        return new GpuSurface.Configuration(reduce(config.width()), reduce(config.height()), config.presentMode());
    }

    public static boolean shouldScalePresentation(int sourceWidth, int sourceHeight, int targetWidth, int targetHeight) {
        // Fallback for any path where the render target and presentation target still disagree.
        return isEnabled() && (sourceWidth < targetWidth || sourceHeight < targetHeight);
    }

    private static boolean isHighDpiFramebuffer(int framebufferWidth, int framebufferHeight, int windowWidth, int windowHeight) {
        return windowWidth > 0 && windowHeight > 0 && (framebufferWidth > windowWidth || framebufferHeight > windowHeight);
    }
}
