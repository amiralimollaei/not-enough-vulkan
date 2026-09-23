package me.flashyreese.mods.sodiumextra.client.services;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class FabricPlatformRuntimeInformation implements SodiumExtraPlatformRuntime {
    @Override
    public Path configDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
