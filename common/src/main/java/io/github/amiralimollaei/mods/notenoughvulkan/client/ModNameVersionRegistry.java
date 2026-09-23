package io.github.amiralimollaei.mods.notenoughvulkan.client;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

public final class ModNameVersionRegistry {
    private static final String SODIUM_EXTRA_ID = "sodium-extra";
    private static final Map<String, String> MOD_IDS_BY_NAME = Map.of(
            normalize("Not Enough Vulkan"), "not-enough-vulkan",
            normalize("VulkanMod"), "vulkanmod",
            normalize("Beryl"), "beryl",
            normalize("Sodium Extra"), SODIUM_EXTRA_ID
    );

    // Sodium Extra is embedded in Not Enough Vulkan and only exposed through Fabric's provides entry.
    // It therefore has no separate mod metadata from which to read a version.
    private static final String SODIUM_EXTRA_VERSION = "0.9.3+mc26.2";

    private ModNameVersionRegistry() {
    }

    public static @Nullable MutableComponent findVersion(Component modName) {
        String modId = MOD_IDS_BY_NAME.get(normalize(modName.getString()));
        if (modId == null) {
            return null;
        }
        if (modId.equals(SODIUM_EXTRA_ID)) {
            return Component.literal(SODIUM_EXTRA_VERSION);
        }

        return NotEnoughVulkanClientMod.findModVersion(modId)
                .map(version -> Component.literal(version.toString()))
                .orElse(null);
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
