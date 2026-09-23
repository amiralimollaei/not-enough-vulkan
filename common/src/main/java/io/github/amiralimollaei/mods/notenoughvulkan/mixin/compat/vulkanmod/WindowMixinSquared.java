package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.video.WindowMode;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeX11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * VulkanMod implements windowed fullscreen as a monitor-sized, undecorated
 * window. Mutter promotes that XWayland window to EWMH fullscreen, but GLFW
 * still considers it monitor-less. Its later windowed restore therefore only
 * sends a resize and never asks the window manager to leave fullscreen.
 */
@Mixin(value = Window.class, remap = false, priority = 1500)
public abstract class WindowMixinSquared {
    @Shadow @Final private long handle;

    @TargetHandler(
            mixin = "net.vulkanmod.mixin.window.WindowMixin",
            name = "setMode"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V",
                    remap = false
            ),
            remap = false
    )
    private void notEnoughVulkan$leaveX11FullscreenBeforeRestoringWindow(
            long window, long monitor, int x, int y, int width, int height, int refreshRate,
            Operation<Void> original
    ) {
        if (monitor == 0L
                && Initializer.CONFIG != null
                && Initializer.CONFIG.windowMode == WindowMode.WINDOWED.mode
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
            notEnoughVulkan$leaveX11Fullscreen(window);
        }

        original.call(window, monitor, x, y, width, height, refreshRate);
    }

    @TargetHandler(
            mixin = "net.vulkanmod.mixin.window.WindowMixin",
            name = "setMode"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("TAIL"))
    private void notEnoughVulkan$refocusAfterRestoringWindow(CallbackInfo ci) {
        if (Initializer.CONFIG != null
                && Initializer.CONFIG.windowMode == WindowMode.WINDOWED.mode
                && GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
            GLFW.glfwFocusWindow(this.handle);
        }
    }

    /**
     * Requests removal of {@code _NET_WM_STATE_FULLSCREEN} before GLFW sends
     * its windowed resize. Both requests use GLFW's X11 connection, preserving
     * their order at the X server.
     */
    private static void notEnoughVulkan$leaveX11Fullscreen(long glfwWindow) {
        long displayAddress = GLFWNativeX11.glfwGetX11Display();
        long x11Window = GLFWNativeX11.glfwGetX11Window(glfwWindow);

        if (displayAddress == 0L || x11Window == 0L) {
            return;
        }

        X11.Display display = new X11.Display();
        display.setPointer(new Pointer(displayAddress));
        X11 x11 = X11.INSTANCE;
        X11.Atom netWmState = x11.XInternAtom(display, "_NET_WM_STATE", false);
        X11.Atom netWmStateFullscreen = x11.XInternAtom(display, "_NET_WM_STATE_FULLSCREEN", false);

        if (netWmState == null || netWmStateFullscreen == null) {
            return;
        }

        X11.XClientMessageEvent message = new X11.XClientMessageEvent();
        message.type = X11.ClientMessage;
        message.display = display;
        message.window = new X11.Window(x11Window);
        message.message_type = netWmState;
        message.format = 32;
        message.data = new X11.XClientMessageEvent.Data();
        message.data.setType(NativeLong[].class);
        message.data.l = new NativeLong[] {
                new NativeLong(0), // _NET_WM_STATE_REMOVE
                new NativeLong(netWmStateFullscreen.longValue()),
                new NativeLong(0),
                new NativeLong(1), // Normal application request.
                new NativeLong(0)
        };

        X11.XEvent event = new X11.XEvent();
        event.setType(X11.XClientMessageEvent.class);
        event.xclient = message;

        x11.XSendEvent(
                display,
                x11.XRootWindow(display, x11.XDefaultScreen(display)),
                0,
                new NativeLong(X11.SubstructureNotifyMask | X11.SubstructureRedirectMask),
                event
        );
        x11.XFlush(display);
    }
}
