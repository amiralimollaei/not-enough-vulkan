package io.github.amiralimollaei.mods.notenoughvulkan.config.vk;

/**
 * A hook that runs after the settings screen applies (and persists) changes.
 */
@FunctionalInterface
public interface SettingsSaveHook {
    void afterSave();
}