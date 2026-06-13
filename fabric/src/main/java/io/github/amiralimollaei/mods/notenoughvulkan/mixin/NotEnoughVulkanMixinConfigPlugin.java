package io.github.amiralimollaei.mods.notenoughvulkan.mixin;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import net.caffeinemc.caffeineconfig.AbstractCaffeineConfigMixinPlugin;
import net.caffeinemc.caffeineconfig.CaffeineConfig;

public class NotEnoughVulkanMixinConfigPlugin extends AbstractCaffeineConfigMixinPlugin {

    private static final String MIXIN_PACKAGE_ROOT = "io.github.amiralimollaei.mods.notenoughvulkan.mixin.";

    @Override
    protected CaffeineConfig createConfig() {
        return NotEnoughVulkanClientMod.mixinConfig();
    }

    @Override
    protected String mixinPackageRoot() {
        return MIXIN_PACKAGE_ROOT;
    }
}
