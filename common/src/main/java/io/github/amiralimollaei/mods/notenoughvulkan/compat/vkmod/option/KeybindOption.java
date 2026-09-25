package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option;

import io.github.amiralimollaei.mods.notenoughvulkan.config.VkKeybind;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ResettableValue;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.Option;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class KeybindOption extends Option<VkKeybind> implements ResettableValue {
    private final VkKeybind fallback;

    public KeybindOption(Component name, Consumer<VkKeybind> setter, Supplier<VkKeybind> getter, VkKeybind fallback) {
        super(name, setter, getter, VkKeybind::displayName);
        this.fallback = fallback;
    }

    @Override
    protected OptionWidget<?> createWidget() {
        return new KeybindOptionWidget(this);
    }

    @Override
    public boolean resetToDefault() {
        if (fallback == null) return false;
        this.setNewValue(fallback);
        return true;
    }
}
