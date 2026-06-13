package io.github.amiralimollaei.mods.notenoughvulkan;

import net.fabricmc.api.ClientModInitializer;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import net.vulkanmod.config.gui.ModSettingsRegistry;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import io.github.amiralimollaei.mods.notenoughvulkan.config.Options;

public class NotEnoughVulkanModInitializer implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModSettingsRegistry.INSTANCE.addModEntry(
                new ModSettingsEntry(
                        Component.literal("NEV").withStyle(ChatFormatting.RED),
                        () -> Identifier.fromNamespaceAndPath("not-enough-vulkan", "textures/icon.png"),
                        Options::getModOptions,
                        () -> {
                            SodiumExtraClientMod.options().writeChanges();
                            NotEnoughVulkanClientMod.options().writeChanges();
                        }
                )
        );
    }
}
