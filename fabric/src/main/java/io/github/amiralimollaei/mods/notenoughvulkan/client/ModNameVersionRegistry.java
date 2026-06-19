package io.github.amiralimollaei.mods.notenoughvulkan.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.HashMap;
import java.util.Map;

public class ModNameVersionRegistry {
    public static Map<String, String> modNameToId = new HashMap<>();

    public static final String NotEnoughVulkanName = "Not Enough Vulkan";
    public static final String VulkanModName = "VulkanMod";
    public static final String BerylName = "Beryl";

    static {
        ModNameVersionRegistry.modNameToId.put(NotEnoughVulkanName, "not-enough-vulkan");
        ModNameVersionRegistry.modNameToId.put(VulkanModName, "vulkanmod");
        ModNameVersionRegistry.modNameToId.put(BerylName, "beryl");
    }

    public static MutableComponent getVersionComponent(String modId) {
        return Component.literal(NotEnoughVulkanClientMod.getModVersion(modId).toString());
    }
}
