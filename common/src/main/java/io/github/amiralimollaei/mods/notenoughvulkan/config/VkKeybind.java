package io.github.amiralimollaei.mods.notenoughvulkan.config;

import com.google.gson.*;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;


public record VkKeybind(int key, int modifiers) {
    public static final int ALT = 1;
    public static final int CONTROL = 2;
    public static final int SHIFT = 4;

    public static final VkKeybind PROFILER_OVERLAY_DEFAULT = new VkKeybind(GLFW.GLFW_KEY_F8, ALT);
    public static final VkKeybind BUILD_TIME_PROFILER_DEFAULT = new VkKeybind(GLFW.GLFW_KEY_F10, ALT);

    public static VkKeybind from(KeyEvent event) {
        int modifiers = (event.hasAltDown() ? ALT : 0)
                | (event.hasControlDown() ? CONTROL : 0)
                | (event.hasShiftDown() ? SHIFT : 0);
        return new VkKeybind(event.key(), modifiers);
    }

    public boolean matches(KeyEvent event) {
        return this.key == event.key() && this.modifiers == from(event).modifiers;
    }

    public Component displayName() {
        String name = InputConstants.Type.KEYSYM.getOrCreate(this.key).getDisplayName().getString();
        return Component.literal(((this.modifiers & CONTROL) != 0 ? "Ctrl + " : "")
                + ((this.modifiers & ALT) != 0 ? "Alt + " : "")
                + ((this.modifiers & SHIFT) != 0 ? "Shift + " : "") + name);
    }

    public boolean isGarbage() {
        return this.key() <= 0 || this.key() > GLFW.GLFW_KEY_LAST || (this.modifiers() & ~7) != 0;
    }
}
