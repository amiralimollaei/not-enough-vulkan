package io.github.amiralimollaei.mods.notenoughvulkan.config.vk;

import net.minecraft.network.chat.Component;

/**
 * Formats integer option values for display.
 */
@FunctionalInterface
public interface ValueFormatter {
    Component format(int value);

    static ValueFormatter plainNumber() {
        return value -> Component.literal(Integer.toString(value));
    }

    static ValueFormatter percent() {
        return value -> Component.literal(value + "%");
    }
}