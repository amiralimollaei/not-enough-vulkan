package io.github.amiralimollaei.mods.notenoughvulkan;

import io.github.amiralimollaei.mods.notenoughvulkan.config.NotEnoughVulkanGameOptions;
import net.caffeinemc.caffeineconfig.CaffeineConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.vulkanmod.Initializer;
import net.vulkanmod.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

public class NotEnoughVulkanClientMod {
    private static NotEnoughVulkanGameOptions CONFIG;
    private static CaffeineConfig MIXIN_CONFIG;
    private static Logger LOGGER;

    public static Logger logger() {
        if (LOGGER == null) {
            LOGGER = LoggerFactory.getLogger("Not Enough Vulkan");
        }

        return LOGGER;
    }

    public static boolean isVulkanModOlderThanOrEqual(String version) {
        Version VKModVersion = FabricLoader.getInstance().getAllMods().stream()
                .filter((modContainer -> Objects.equals(modContainer.getMetadata().getId(), "vulkanmod")))
                .findFirst().get().getMetadata().getVersion();
        try {
            SemanticVersion CompareSemVer = SemanticVersion.parse(version);
            int comparisonResult = VKModVersion.compareTo(CompareSemVer);
            NotEnoughVulkanClientMod.logger().info("comparisonResult: {}", comparisonResult);
            return comparisonResult <= 0;
        } catch (VersionParsingException e) {
            NotEnoughVulkanClientMod.logger().warn("Unable to parse version: {}", VKModVersion);
            return false;
        }
    }

    private static boolean packageExists(String packageName) {
        return Arrays.stream(Package.getPackages()).anyMatch((p) -> p.getName().equals(packageName));
    }

    private static NotEnoughVulkanGameOptions loadConfig() {
        return NotEnoughVulkanGameOptions.load(FabricLoader.getInstance().getConfigDir().resolve("not-enough-vulkan-options.json").toFile());
    }

    public static NotEnoughVulkanGameOptions options() {
        if (CONFIG == null) {
            CONFIG = loadConfig();
        }

        return CONFIG;
    }

    public static CaffeineConfig mixinConfig() {
        if (MIXIN_CONFIG == null) {
            MIXIN_CONFIG = CaffeineConfig.builder("Not Enough Vulkan").withSettingsKey("not-enough-vulkan:options")
                    .addMixinOption("core", true, false)
                    .addMixinOption("core.compact_vk_options", false)
                    .addMixinOption("compat", true)
                    .addMixinOption("compat.bobby", packageExists("de.johni0702.minecraft.bobby"))
                    .addMixinOption("compat.skip_wayland_patches", true)
                    .addMixinOption("compat.monitor_selector", true)
                    .addMixinOption(
                            "compat.force_x11",
                            NotEnoughVulkanClientMod.isVulkanModOlderThanOrEqual("0.6.5")
                    )

                    //.withInfoUrl("https://github.com/amiralimollaei/not-enough-vulkan/wiki/Configuration-File")
                    .build(FabricLoader.getInstance().getConfigDir().resolve("not-enough-vulkan.properties"));
        }
        return MIXIN_CONFIG;
    }

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
