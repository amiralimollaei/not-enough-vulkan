package io.github.amiralimollaei.mods.notenoughvulkan.config.vk;

import net.minecraft.resources.Identifier;

/**
 * Read-only view over registered option values, used to compute option availability
 * and dynamic spans.
 */
public interface SettingsState {
    boolean readBool(Identifier id);

    int readInt(Identifier id);

    <E extends Enum<E>> E readEnum(Identifier id, Class<E> type);

    default int maxOf(Identifier id, int fallback) {
        return fallback;
    }
}
