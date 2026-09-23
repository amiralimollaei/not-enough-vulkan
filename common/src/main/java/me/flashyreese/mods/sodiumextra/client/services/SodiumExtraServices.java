package me.flashyreese.mods.sodiumextra.client.services;

import java.util.ServiceLoader;

public final class SodiumExtraServices {
    public static final SodiumExtraPlatformRuntime PLATFORM_RUNTIME =
            load(SodiumExtraPlatformRuntime.class);

    private SodiumExtraServices() {
    }

    private static <T> T load(Class<T> service) {
        return ServiceLoader.load(service)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No provider found for " + service.getName()));
    }
}
