package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option;

import io.github.amiralimollaei.mods.notenoughvulkan.config.TimingsKeybind;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ResettableValue;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.option.Option;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class KeybindOption extends Option<TimingsKeybind> implements ResettableValue {
    public KeybindOption(Component name, Consumer<TimingsKeybind> setter, Supplier<TimingsKeybind> getter) {
        super(name, setter, getter, TimingsKeybind::displayName);
    }

    @Override
    protected OptionWidget<?> createWidget() {
        return new KeybindOptionWidget(this);
    }

    @Override
    public boolean resetToDefault() {
        this.setNewValue(TimingsKeybind.DEFAULT);
        return true;
    }
}
