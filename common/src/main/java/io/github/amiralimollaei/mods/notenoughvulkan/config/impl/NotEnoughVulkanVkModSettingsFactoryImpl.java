package io.github.amiralimollaei.mods.notenoughvulkan.config.impl;

import io.github.amiralimollaei.mods.notenoughvulkan.config.NotEnoughVulkanConfig;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ModSettingsSpec;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraConfig;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.api.VkModSettingsEntryBuilder;
import net.vulkanmod.config.api.VkModSettingsFactory;
import net.vulkanmod.config.gui.ModSettingsEntry;

public class NotEnoughVulkanVkModSettingsFactoryImpl implements VkModSettingsFactory {
    private final ModSettingsSpec spec = ModSettingsSpec.create(Component.literal("Not Enough Vulkan").withColor(0xFF9000));

    public NotEnoughVulkanVkModSettingsFactoryImpl() {
        new SodiumExtraConfig().register(spec);
        new NotEnoughVulkanConfig().register(spec);
    }

    @Override
    public ModSettingsEntry build(VkModSettingsEntryBuilder vkModSettingsEntryBuilder) {
        return spec.build(vkModSettingsEntryBuilder);
    }
}
