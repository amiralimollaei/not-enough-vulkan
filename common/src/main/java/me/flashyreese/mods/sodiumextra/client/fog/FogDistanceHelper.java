package me.flashyreese.mods.sodiumextra.client.fog;

import com.google.gson.JsonObject;
import me.flashyreese.mods.greenlight.feature.ClientFeature;
import me.flashyreese.mods.greenlight.feature.Greenlight;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import me.flashyreese.mods.sodiumextra.mixin.fog.AccessorIntegratedServer;
import me.flashyreese.mods.sodiumextra.mixin.fog.AccessorMinecraft;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.InclusiveIntSpan;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.SettingsState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

public final class FogDistanceHelper {
    public static final Identifier SODIUM_RENDER_DISTANCE_OPTION_ID = Identifier.parse("sodium:general.render_distance");
    public static final int FOG_DISTANCE_OFF = -1;
    public static final int FOG_DISTANCE_VANILLA = 0;
    // The cloud shader fades clouds from the camera to cloudEnd; 100% puts the fade end at the
    // cloud render edge, which is vanilla's own formula.
    public static final int VANILLA_CLOUD_FOG_PERCENT = 100;
    private static final int VANILLA_MAX_FOG_DISTANCE = 32;
    private static final int VANILLA_MAX_CLOUD_RENDER_DISTANCE = 128;
    private static final int PROTECTED_FOG_DISTANCE_MAX_BLOCKS = 256;
    private static final float CHUNK_SIZE = 16F;
    private static final ClientFeature<ProtectedGameplayFogPolicy> PROTECTED_GAMEPLAY_FOG = Greenlight
            .feature(Identifier.fromNamespaceAndPath("sodium-extra", "protected_gameplay_fog"))
            .decoder(1, ProtectedGameplayFogPolicy::fromJson)
            .register();
    public enum ProtectedFogType {
        BLINDNESS("blindness"),
        DARKNESS("darkness"),
        LAVA("lava"),
        POWDER_SNOW("powder_snow"),
        WATER("water");

        private final String policyKey;

        ProtectedFogType(String policyKey) {
            this.policyKey = policyKey;
        }
    }

    public static SodiumExtraGameOptions.AtmosphericFogSettings getAtmosphericSettings(ClientLevel level) {
        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        Identifier dimensionEffectsId = level.dimensionTypeRegistration()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> level.dimension().identifier());
        return fogSettings.getAtmospheric(dimensionEffectsId);
    }

    public static int getFogDistance(ClientLevel level) {
        return getAtmosphericSettings(level).distanceChunks;
    }

    public static InclusiveIntSpan getFogDistanceRange(SettingsState state) {
        return new InclusiveIntSpan(FOG_DISTANCE_OFF, getMaxFogDistance(state), 1);
    }

    public static InclusiveIntSpan getProtectedGameplayFogDistanceRange() {
        return new InclusiveIntSpan(FOG_DISTANCE_OFF, PROTECTED_FOG_DISTANCE_MAX_BLOCKS, 1);
    }

    public static int getMaxFogDistance() {
        return getMaxFogDistance(null);
    }

    public static int getMaxFogDistance(SettingsState state) {
        int maxFogDistance = VANILLA_MAX_FOG_DISTANCE;
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft != null && minecraft.options != null) {
            maxFogDistance = Math.max(maxFogDistance, minecraft.options.renderDistance().get());
            Object valueSet = minecraft.options.renderDistance().values();

            if (valueSet instanceof OptionInstance.IntRange range) {
                maxFogDistance = Math.max(maxFogDistance, range.maxInclusive());
            } else if (valueSet instanceof OptionInstance.ClampingLazyMaxIntRange range) {
                maxFogDistance = Math.max(maxFogDistance, range.maxInclusive());
            } else {
                maxFogDistance = Math.max(maxFogDistance, getIntAccessor(valueSet, "maxInclusive", maxFogDistance));
            }
        }

        maxFogDistance = Math.max(maxFogDistance, getSodiumRenderDistanceMax(state, maxFogDistance));

        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        maxFogDistance = Math.max(maxFogDistance, fogSettings.atmospheric.distanceChunks);
        for (SodiumExtraGameOptions.AtmosphericFogSettings settings : fogSettings.dimensionOverrides.values()) {
            maxFogDistance = Math.max(maxFogDistance, settings.distanceChunks);
        }

        return maxFogDistance;
    }

    private static int getSodiumRenderDistanceMax(SettingsState state, int fallback) {
        return state == null ? fallback : state.maxOf(SODIUM_RENDER_DISTANCE_OPTION_ID, fallback);
    }

    public static float getStart(SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        return settings.distanceChunks * CHUNK_SIZE * (settings.startPercent / 100.0F);
    }

    public static float applyStartMultiplier(float start, SodiumExtraGameOptions.AtmosphericFogSettings settings) {
        return start * (settings.startPercent / 100.0F);
    }

    public static float getEnd(int fogDistance) {
        return (fogDistance + 1) * CHUNK_SIZE;
    }

    public static float getCloudEnd(int cloudFogPercent) {
        return getCloudRenderDistance() * CHUNK_SIZE * (Math.clamp(cloudFogPercent, 0, 100) / 100.0F);
    }

    private static int getCloudRenderDistance() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return VANILLA_MAX_CLOUD_RENDER_DISTANCE;
        }

        return Math.max(1, minecraft.options.cloudRange().get());
    }

    public static boolean disablesFog(int fogDistance) {
        return fogDistance == FOG_DISTANCE_OFF;
    }

    public static boolean isBossFogActive() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.gui != null && minecraft.gui.hud != null && minecraft.gui.hud.getBossOverlay().shouldCreateWorldFog();
    }

    public static boolean shouldModifyProtectedGameplayFog() {
        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        return fogSettings.advanced
                && fogSettings.protectedGameplay.enabledWhenAllowed
                && (isLocalWorldAllowedForProtectedGameplayFog() || PROTECTED_GAMEPLAY_FOG.policy().isPresent());
    }

    public static int getProtectedGameplayFogDistance(ProtectedFogType type) {
        SodiumExtraGameOptions.FogSettings fogSettings = getFogSettings();
        if (!fogSettings.advanced || !fogSettings.protectedGameplay.enabledWhenAllowed) {
            return FOG_DISTANCE_VANILLA;
        }

        int distanceBlocks = getConfiguredProtectedGameplayFogDistance(fogSettings.protectedGameplay, type);
        if (isLocalWorldAllowedForProtectedGameplayFog()) {
            return distanceBlocks;
        }

        return PROTECTED_GAMEPLAY_FOG.policy()
                .map(policy -> policy.clamp(type, distanceBlocks))
                .orElse(FOG_DISTANCE_VANILLA);
    }

    private static int getConfiguredProtectedGameplayFogDistance(SodiumExtraGameOptions.ProtectedFogSettings settings, ProtectedFogType type) {
        return switch (type) {
            case BLINDNESS -> settings.blindnessDistanceBlocks;
            case DARKNESS -> settings.darknessDistanceBlocks;
            case LAVA -> settings.lavaDistanceBlocks;
            case POWDER_SNOW -> settings.powderSnowDistanceBlocks;
            case WATER -> settings.waterDistanceBlocks;
        };
    }

    public static void applyProtectedGameplayFog(FogData fog, int distanceBlocks, float environmentalStartMultiplier, float skyEndMultiplier) {
        if (distanceBlocks == FOG_DISTANCE_VANILLA) {
            return;
        }

        if (disablesFog(distanceBlocks)) {
            fog.environmentalStart = Float.MAX_VALUE;
            fog.environmentalEnd = Float.MAX_VALUE;
            fog.skyEnd = Float.MAX_VALUE;
            fog.cloudEnd = Float.MAX_VALUE;
            return;
        }

        float end = distanceBlocks;
        fog.environmentalStart = end * environmentalStartMultiplier;
        fog.environmentalEnd = end;
        fog.skyEnd = end * skyEndMultiplier;
        fog.cloudEnd = end * skyEndMultiplier;
    }

    private static SodiumExtraGameOptions.FogSettings getFogSettings() {
        SodiumExtraGameOptions.RenderSettings renderSettings = SodiumExtraClientMod.options().renderSettings;
        renderSettings.sanitize();
        return renderSettings.fogSettings;
    }

    private static boolean isLocalWorldAllowedForProtectedGameplayFog() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || !minecraft.hasSingleplayerServer()) {
            return false;
        }

        if (!minecraft.isMultiplayerServer()) {
            return true;
        }

        Object server = ((AccessorMinecraft)minecraft).sodiumExtra$getSingleplayerServer();
        return server instanceof AccessorIntegratedServer accessor && accessor.sodiumExtra$commandsAllowedForOtherPlayers();
    }

    private static int getIntAccessor(Object object, String methodName, int fallback) {
        if (object == null) {
            return fallback;
        }

        try {
            Method method = object.getClass().getMethod(methodName);
            Object result = method.invoke(object);
            return result instanceof Integer value ? value : fallback;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return fallback;
        }
    }

    public static float decodeRenderDistanceStart(float renderDistanceStart, float renderDistanceEnd) {
        return renderDistanceStart;
    }

    public static float decodeRenderDistanceEnd(float renderDistanceStart, float renderDistanceEnd) {
        return renderDistanceEnd;
    }

    private record ProtectedGameplayFogPolicy(Map<ProtectedFogType, ProtectedFogRule> rules) {
        private static ProtectedGameplayFogPolicy fromJson(JsonObject settings) {
            EnumMap<ProtectedFogType, ProtectedFogRule> rules = new EnumMap<>(ProtectedFogType.class);

            for (ProtectedFogType type : ProtectedFogType.values()) {
                JsonObject rule = GsonHelper.getAsJsonObject(settings, type.policyKey, new JsonObject());
                boolean enabled = GsonHelper.getAsBoolean(rule, "enabled", false);
                int maxDistanceBlocks = Math.clamp(GsonHelper.getAsInt(rule, "max_distance_blocks", FOG_DISTANCE_VANILLA), FOG_DISTANCE_VANILLA, PROTECTED_FOG_DISTANCE_MAX_BLOCKS);
                boolean allowOff = GsonHelper.getAsBoolean(rule, "allow_off", false);

                rules.put(type, new ProtectedFogRule(enabled, maxDistanceBlocks, allowOff));
            }

            return new ProtectedGameplayFogPolicy(Map.copyOf(rules));
        }

        private int clamp(ProtectedFogType type, int distanceBlocks) {
            ProtectedFogRule rule = this.rules.get(type);
            return rule != null ? rule.clamp(distanceBlocks) : FOG_DISTANCE_VANILLA;
        }
    }

    private record ProtectedFogRule(boolean enabled, int maxDistanceBlocks, boolean allowOff) {
        private int clamp(int distanceBlocks) {
            if (!this.enabled) {
                return FOG_DISTANCE_VANILLA;
            }

            if (distanceBlocks == FOG_DISTANCE_VANILLA) {
                return FOG_DISTANCE_VANILLA;
            }

            if (disablesFog(distanceBlocks)) {
                return this.allowOff ? FOG_DISTANCE_OFF : this.maxDistanceBlocks;
            }

            return Math.min(distanceBlocks, this.maxDistanceBlocks);
        }
    }

}
