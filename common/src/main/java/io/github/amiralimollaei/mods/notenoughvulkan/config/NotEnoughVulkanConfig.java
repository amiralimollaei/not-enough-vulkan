package io.github.amiralimollaei.mods.notenoughvulkan.config;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ModSettingsSpec;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Not Enough Vulkan's own settings, registered via the {@link ModSettingsSpec} DSL.
 */
public class NotEnoughVulkanConfig {
    public void register(ModSettingsSpec spec) {
        spec.icon(Identifier.parse("not-enough-vulkan:textures/icon.png"));
        spec.page(Component.translatable("not-enough-vulkan.page.patches"))
                .group(Component.literal("Debug"), g -> {
                    g.add(
                            spec.keybind(Identifier.parse("not-enough-vulkan:profiler_overlay_keybind"))
                            .title(Component.translatable("not-enough-vulkan.option.profiler_overlay_keybind"))
                            .hint(Component.translatable("not-enough-vulkan.option.keybind.tooltip"))
                            .defaults(VkKeybind.PROFILER_OVERLAY_DEFAULT)
                            .bind(
                                    value -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkProfilerOverlayKeybind = value,
                                    () -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkProfilerOverlayKeybind
                            )
                            .onSave(() -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().writeChanges())
                    );
                    g.add(
                            spec.keybind(Identifier.parse("not-enough-vulkan:build_time_profiler_keybind"))
                            .title(Component.translatable("not-enough-vulkan.option.build_time_profiler_keybind"))
                            .hint(Component.translatable("not-enough-vulkan.option.keybind.tooltip"))
                            .defaults(VkKeybind.BUILD_TIME_PROFILER_DEFAULT)
                            .bind(
                                    value -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkBuildTimeProfilerKeybind = value,
                                    () -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.vkBuildTimeProfilerKeybind
                            )
                            .onSave(() -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().writeChanges())
                    );
                })
                .group(Component.literal("Compatibility"), g -> g.add(spec.toggle(
                                Identifier.parse("not-enough-vulkan:skip_wayland_patches"))
                        .title(Component.translatable("not-enough-vulkan.option.skip_wayland_patches"))
                        .hint(Component.translatable("not-enough-vulkan.option.skip_wayland_patches.tooltip"))
                        .defaults(false)
                        .bind(
                                value -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.skipWaylandPatches = value,
                                () -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().patchesSettings.skipWaylandPatches
                        )
                        .onSave(() -> NotEnoughVulkanClientMod.notEnoughVulkanOptions().writeChanges())));
    }
}
