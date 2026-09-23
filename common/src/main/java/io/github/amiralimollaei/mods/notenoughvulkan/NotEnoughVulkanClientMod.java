package io.github.amiralimollaei.mods.notenoughvulkan;

import io.github.amiralimollaei.mods.notenoughvulkan.config.NotEnoughVulkanGameOptions;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

public final class NotEnoughVulkanClientMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("Not Enough Vulkan");

    private static NotEnoughVulkanGameOptions CONFIG;
    private static CaffeineConfig MIXIN_CONFIG;

    public static Logger logger() {
        return LOGGER;
    }

    public static NotEnoughVulkanGameOptions notEnoughVulkanOptions() {
        if (CONFIG == null) {
            Path configPath = FabricLoader.getInstance().getConfigDir()
                    .resolve("not-enough-vulkan-options.json");
            CONFIG = NotEnoughVulkanGameOptions.load(configPath);
        }

        return CONFIG;
    }

    public static CaffeineConfig mixinConfig() {
        if (MIXIN_CONFIG == null) {
            MIXIN_CONFIG = CaffeineConfig.builder("Not Enough Vulkan")
                    .withSettingsKey("not-enough-vulkan:options")
                    .addMixinOption("core", true, false)
                    .addMixinOption("core.enhance_vk_options", true, false)
                    .addMixinOption("compat", true)
                    .addMixinOption("compat.vulkanmod", true)
                    .addMixinOption("compat.bobby", FabricLoader.getInstance().isModLoaded("bobby"))
                    .addMixinOption("compat.skip_wayland_patches", true)
                    .addMixinOption("compat.monitor_selector", true)
                    .build(FabricLoader.getInstance().getConfigDir().resolve("not-enough-vulkan.properties"));
        }

        return MIXIN_CONFIG;
    }

    public static Optional<Version> findModVersion(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(container -> container.getMetadata().getVersion());
    }

    /// VulkanMod does not load its configuration soon enough for the monitor selector
    /// feature to work properly, so we load it on demand ourselves.
    public static Config getVulkanModConfig() {
        if (Initializer.CONFIG == null) {
            Path configPath = FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("vulkanmod_settings.json");

            Initializer.CONFIG = Config.load(configPath);
        }

        return Initializer.CONFIG;
    }
}
