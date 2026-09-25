package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option;

import io.github.amiralimollaei.mods.notenoughvulkan.config.VkKeybind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;
import org.lwjgl.glfw.GLFW;

public final class KeybindOptionWidget extends OptionWidget<KeybindOption> {
    private static KeybindOptionWidget capturing;
    private final KeybindOption keybindOption;

    public KeybindOptionWidget(KeybindOption option) {
        super(option, option.getName());
        this.keybindOption = option;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return capture(event);
    }

    public static boolean capture(KeyEvent event) {
        if (capturing == null) {
            return false;
        }
        int key = event.key();
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            capturing = null;
        } else if (key != GLFW.GLFW_KEY_LEFT_ALT && key != GLFW.GLFW_KEY_RIGHT_ALT
                && key != GLFW.GLFW_KEY_LEFT_CONTROL && key != GLFW.GLFW_KEY_RIGHT_CONTROL
                && key != GLFW.GLFW_KEY_LEFT_SHIFT && key != GLFW.GLFW_KEY_RIGHT_SHIFT
                && key != GLFW.GLFW_KEY_UNKNOWN) {
            capturing.keybindOption.setNewValue(VkKeybind.from(event));
            capturing.updateDisplayedValue();
            capturing = null;
        }
        return true;
    }

    public static void stopCapture() {
        capturing = null;
    }

    public static boolean isCapturing() {
        return capturing != null;
    }

    @Override
    protected void renderControls(double mouseX, double mouseY) {
        Component label = capturing == this
                ? Component.translatable("not-enough-vulkan.option.keybind.press_key")
                : this.keybindOption.getDisplayedValue();
        int centerX = this.controlX + this.controlWidth / 2;
        int centerY = this.y + (this.height - 8) / 2;
        GuiRenderer.drawCenteredString(Minecraft.getInstance().font, label, centerX, centerY,
                this.active ? 0xFFFFFFFF : 0xFFA0A0A0);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        capturing = this;
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }
}
