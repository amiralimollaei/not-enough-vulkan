package me.flashyreese.mods.sodiumextra.client.services;

import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public final class NeoForgePlatformRuntimeInformation implements SodiumExtraPlatformRuntime {
    @Override
    public Path configDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
