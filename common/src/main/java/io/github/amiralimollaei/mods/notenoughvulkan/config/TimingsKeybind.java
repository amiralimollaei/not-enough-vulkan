package io.github.amiralimollaei.mods.notenoughvulkan.config;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public record TimingsKeybind(int key, int modifiers) {
    public static final int ALT = 1;
    public static final int CONTROL = 2;
    public static final int SHIFT = 4;
    public static final TimingsKeybind DEFAULT = new TimingsKeybind(GLFW.GLFW_KEY_F8, ALT);

    public static TimingsKeybind from(KeyEvent event) {
        int modifiers = (event.hasAltDown() ? ALT : 0)
                | (event.hasControlDown() ? CONTROL : 0)
                | (event.hasShiftDown() ? SHIFT : 0);
        return new TimingsKeybind(event.key(), modifiers);
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
}
