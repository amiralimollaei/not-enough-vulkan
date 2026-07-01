package me.flashyreese.mods.sodiumextra.client.config;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import io.github.amiralimollaei.mods.notenoughvulkan.client.NotEnoughVulkanClientMod;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.gui.FullscreenResolutionConfirmation;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import me.flashyreese.mods.sodiumextra.client.fog.FogShaderTransformer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class SodiumExtraConfigUtils {
    private static Identifier id(String path) {
        return Identifier.parse("sodium-extra:" + path);
    }

    private static final Identifier ADVANCED_FOG_OPTION_ID = id("advanced_fog_settings");
    private static final Identifier MULTI_DIMENSION_FOG_OPTION_ID = id("multi_dimension_fog");
    private static final Identifier PROTECTED_GAMEPLAY_FOG_OPTION_ID = id("protected_gameplay_fog");
    private static final Identifier WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID = id("wayland_fullscreen_resolution");
    private static final Identifier CLOUD_HEIGHT_OVERRIDE_OPTION_ID = id("cloud_height_override");
    private static final Identifier SODIUM_FULLSCREEN_MODE_OPTION_ID = Identifier.parse("sodium:general.fullscreen_mode");
    private static final Identifier SODIUM_FULLSCREEN_RESOLUTION_OPTION_ID = Identifier.parse("sodium:general.fullscreen_resolution");
    private static final Identifier SODIUM_VSYNC_OPTION_ID = Identifier.parse("sodium:general.vsync");
    private static final List<Identifier> DEFAULT_DIMENSION_EFFECT_IDS = List.of(
            Level.OVERWORLD.identifier(),
            Level.NETHER.identifier(),
            Level.END.identifier()
    );

    private static boolean isFogMixinEnabled() {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled();
    }

    public static SodiumExtraGameOptions.FogSettings fogSettings() {
        SodiumExtraGameOptions.RenderSettings renderSettings = SodiumExtraClientMod.options().renderSettings;
        renderSettings.sanitize();
        return renderSettings.fogSettings;
    }

    private static SodiumExtraGameOptions.AtmosphericFogSettings atmosphericFogSettings() {
        return fogSettings().atmospheric;
    }

    private static SodiumExtraGameOptions.AtmosphericFogSettings createDimensionFogSettings() {
        SodiumExtraGameOptions.AtmosphericFogSettings source = atmosphericFogSettings();
        SodiumExtraGameOptions.AtmosphericFogSettings settings = new SodiumExtraGameOptions.AtmosphericFogSettings();
        settings.startPercent = source.startPercent;
        settings.shapeMode = source.shapeMode;
        settings.affectSkyFog = source.affectSkyFog;
        settings.affectCloudFog = source.affectCloudFog;
        return settings;
    }

    public static List<Identifier> getDimensionFogEffectIds(SodiumExtraGameOptions.FogSettings fogSettings) {
        Set<Identifier> identifiers = new LinkedHashSet<>(DEFAULT_DIMENSION_EFFECT_IDS);
        addKnownWorldDimensionEffectIds(identifiers);
        identifiers.addAll(fogSettings.dimensionOverrides.keySet());

        identifiers.forEach(identifier -> fogSettings.dimensionOverrides.computeIfAbsent(identifier, ignored -> createDimensionFogSettings()));

        return identifiers.stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();
    }

    private static void addKnownWorldDimensionEffectIds(Set<Identifier> identifiers) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        if (minecraft.level != null) {
            identifiers.add(getDimensionFogEffectId(minecraft.level));
        }
    }

    private static Identifier getDimensionFogEffectId(Level level) {
        return level.dimensionTypeRegistration()
                .unwrapKey()
                .map(key -> key.identifier())
                .orElseGet(() -> level.dimension().identifier());
    }

    public static void setAtmosphericFogStart(int value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        int clampedValue = Math.clamp(value, 0, 100);
        fogSettings.atmospheric.startPercent = clampedValue;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.startPercent = clampedValue);
    }

    public static void setAtmosphericFogShape(SodiumExtraGameOptions.FogShapeMode value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.shapeMode = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.shapeMode = value);
    }

    public static void setAtmosphericSkyFog(boolean value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.affectSkyFog = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.affectSkyFog = value);
    }

    public static void setAtmosphericCloudFog(boolean value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.affectCloudFog = value;
        fogSettings.dimensionOverrides.values().forEach(settings -> settings.affectCloudFog = value);
    }

    private static int compareParticleNamespace(String a, String b) {
        if (a.equals(Identifier.DEFAULT_NAMESPACE) && !b.equals(Identifier.DEFAULT_NAMESPACE)) {
            return -1;
        }

        if (!a.equals(Identifier.DEFAULT_NAMESPACE) && b.equals(Identifier.DEFAULT_NAMESPACE)) {
            return 1;
        }

        int result = a.compareToIgnoreCase(b);
        return result != 0 ? result : a.compareTo(b);
    }

    private static Component particleNamespaceName(String namespace) {
        String name = Arrays.stream(namespace.split("[^A-Za-z0-9]+"))
                .filter(part -> !part.isBlank())
                .map(SodiumExtraConfigUtils::capitalizeNamespacePart)
                .collect(Collectors.joining(" "));

        return Component.literal(name.isBlank() ? namespace : name);
    }

    private static String capitalizeNamespacePart(String part) {
        return part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT);
    }

    private static Boolean isFullscreenResolutionOptionEnabled() {
        Monitor monitor = getMonitor();
        if (monitor == null || monitor.modeCount() <= 0) {
            return false;
        }

        return NotEnoughVulkanClientMod.getVulkanModConfig().windowMode == WindowMode.EXCLUSIVE_FULLSCREEN.mode;
    }

    private static boolean canUseFullscreenResolution() {
        return canUseFullscreenResolution(SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution);
    }

    private static boolean canUseFullscreenResolution(boolean waylandFullscreenResolution) {
        Util.OS os = Util.getPlatform();
        return os == Util.OS.WINDOWS
                || os == Util.OS.OSX
                || isX11()
                || (isWaylandOrXWayland() && waylandFullscreenResolution);
    }

    private static boolean isX11() {
        return Util.getPlatform() == Util.OS.LINUX
                && GLX.getGlfwPlatform() == GLFW.GLFW_PLATFORM_X11
                && !isWaylandSession();
    }

    private static boolean isWaylandOrXWayland() {
        return Util.getPlatform() == Util.OS.LINUX
                && (GLX.getGlfwPlatform() == GLFW.GLFW_PLATFORM_WAYLAND || isWaylandSession());
    }

    private static boolean isWaylandSession() {
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        return System.getenv("WAYLAND_DISPLAY") != null || "wayland".equalsIgnoreCase(sessionType);
    }

    private static Monitor getMonitor() {
        Window window = Minecraft.getInstance().getWindow();
        return window == null ? null : window.findBestMonitor();
    }

    private static Integer getFullscreenResolution() {
        Monitor monitor = getMonitor();
        if (monitor == null) {
            return 0;
        }

        return Minecraft.getInstance().getWindow().getPreferredFullscreenVideoMode()
                .map(monitor::indexOfMode)
                .map(value -> value + 1)
                .orElse(0);
    }

    private static void setFullscreenResolution(Integer value) {
        Monitor monitor = getMonitor();
        if (monitor == null || monitor.modeCount() <= 0) {
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        Optional<VideoMode> previousMode = window.getPreferredFullscreenVideoMode();
        if (!canUseFullscreenResolution() || value == 0) {
            window.setPreferredFullscreenVideoMode(Optional.empty());
            SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
            return;
        }

        // Arm recovery before the video-mode reload; the prompt handles bad switches that return, and
        // pre-launch recovery handles hangs before the prompt can open.
        if (isWaylandOrXWayland()) {
            SodiumExtraClientMod.armWaylandFullscreenResolutionRecovery();
            FullscreenResolutionConfirmation.request(previousMode);
        } else {
            SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
        }

        int modeIndex = Math.clamp(value - 1, 0, monitor.modeCount() - 1);
        window.setPreferredFullscreenVideoMode(Optional.of(monitor.mode(modeIndex)));
    }

    private static void clearPreferredFullscreenVideoMode() {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null && window.getPreferredFullscreenVideoMode().isPresent()) {
            window.setPreferredFullscreenVideoMode(Optional.empty());
        }
    }

    public static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        String name = Component.translatable(key).getString().replaceAll("§.", "");
        return Component.literal(name.isBlank() ? fallbackName(key) : name);
    }

    public static Component translatableName(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category));
        Component translatable = Component.translatable(key);
        String name = translatable.getString().replaceAll("§.", "");

        if (!ComponentUtils.isTranslationResolvable(translatable) || name.isBlank()) {
            return Component.literal(fallbackName(key));
        }

        return Component.literal(name);
    }

    private static String fallbackName(String key) {
        return Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                .filter(s -> !s.isBlank())
                .map(s -> s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static Component translatableTooltip(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category)).concat(".tooltip");
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.translatable(
                    "sodium-extra.option.".concat(category).concat(".tooltips"),
                    translatableName(identifier, category)
            );
        }
        return translatable;
    }
}
