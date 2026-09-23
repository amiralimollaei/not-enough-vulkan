package me.flashyreese.mods.sodiumextra.client.config;

import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ModSettingsSpec;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ModSettingsSpec.PageSpec;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.SettingsState;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ValueFormatter;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import me.flashyreese.mods.sodiumextra.client.gui.FullscreenResolutionConfirmation;
import me.flashyreese.mods.sodiumextra.common.util.ControlValueFormatterExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.debug.DebugOptionsScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.vulkanmod.config.option.PerformanceImpact;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Registers the Sodium Extra options with VulkanMod's settings screen using
 * the {@link ModSettingsSpec} DSL.
 */
public class SodiumExtraConfig {
    private static Identifier id(String path) {
        return Identifier.parse("sodium-extra:" + path);
    }

    private static final Identifier ADVANCED_FOG_OPTION_ID = id("advanced_fog_settings");
    private static final Identifier MULTI_DIMENSION_FOG_OPTION_ID = id("multi_dimension_fog");
    private static final Identifier PROTECTED_GAMEPLAY_FOG_OPTION_ID = id("protected_gameplay_fog");
    private static final Identifier WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID = id("wayland_fullscreen_resolution");
    private static final Identifier CLOUD_HEIGHT_OVERRIDE_OPTION_ID = id("cloud_height_override");
    private static final Identifier PANINI_PROJECTION_OPTION_ID = id("panini_projection");
    private static final List<Identifier> DEFAULT_DIMENSION_EFFECT_IDS = List.of(
            Level.OVERWORLD.identifier(),
            Level.NETHER.identifier(),
            Level.END.identifier()
    );

    private static boolean isFogMixinEnabled() {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.fog").isEnabled();
    }

    private static boolean isAdvancedFogOptionEnabled(SettingsState state) {
        return isFogMixinEnabled() && state.readBool(ADVANCED_FOG_OPTION_ID);
    }

    private static boolean isSingleFogOptionEnabled(SettingsState state) {
        boolean advanced = state.readBool(ADVANCED_FOG_OPTION_ID);
        return isFogMixinEnabled() && (!advanced || !state.readBool(MULTI_DIMENSION_FOG_OPTION_ID));
    }

    private static boolean isDimensionFogOptionEnabled(SettingsState state) {
        return isFogMixinEnabled()
                && state.readBool(ADVANCED_FOG_OPTION_ID)
                && state.readBool(MULTI_DIMENSION_FOG_OPTION_ID);
    }

    private static boolean isProtectedGameplayFogOptionEnabled(SettingsState state) {
        return isFogMixinEnabled()
                && state.readBool(ADVANCED_FOG_OPTION_ID)
                && state.readBool(PROTECTED_GAMEPLAY_FOG_OPTION_ID);
    }

    private static boolean isCloudHeightOptionEnabled(SettingsState state) {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled()
                && state.readBool(CLOUD_HEIGHT_OVERRIDE_OPTION_ID);
    }

    private static boolean isPaniniProjectionOptionEnabled(SettingsState state) {
        return SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.panini_projection").isEnabled()
                && state.readBool(PANINI_PROJECTION_OPTION_ID);
    }

    private static boolean isFpsExtendedEnabled(SettingsState state) {
        return state.readBool(id("show_fps"));
    }

    private static SodiumExtraGameOptions.FogSettings fogSettings() {
        SodiumExtraGameOptions.RenderSettings renderSettings = SodiumExtraClientMod.options().renderSettings;
        renderSettings.sanitize();
        return renderSettings.fogSettings;
    }

    private static List<Identifier> getDimensionFogEffectIds(SodiumExtraGameOptions.FogSettings fogSettings) {
        // Build the list of dimensions to show options for without creating overrides; an override is
        // only persisted once the player actually edits that dimension's fog distance.
        Set<Identifier> identifiers = new LinkedHashSet<>(DEFAULT_DIMENSION_EFFECT_IDS);
        addKnownWorldDimensionEffectIds(identifiers);
        identifiers.addAll(fogSettings.dimensionOverrides.keySet());

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

    private static void setAtmosphericFogStart(int value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.startPercent = Math.clamp(value, 0, 100);
    }

    private static void setAtmosphericCloudFog(int value) {
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        fogSettings.atmospheric.cloudFogPercent = Math.clamp(value, 0, 100);
    }

    private static void setDimensionFogStart(Identifier dimensionId, int value) {
        fogSettings().getOrCreateDimensionOverride(dimensionId).startPercent = Math.clamp(value, 0, 100);
    }

    private static void setDimensionCloudFog(Identifier dimensionId, int value) {
        fogSettings().getOrCreateDimensionOverride(dimensionId).cloudFogPercent = Math.clamp(value, 0, 100);
    }

    // Shared scaffolding for the global and per-dimension fog options; both sets are gated by the
    // advanced + multi-dimension toggles (global controls active when per-dimension mode is off).
    private static <B extends ModSettingsSpec.OptionSpec<?>> B fogOption(B option, Function<SettingsState, Boolean> enabledProvider, String nameKey, String tooltipKey) {
        option.enabledWhen(enabledProvider);
        option.title(Component.translatable(nameKey));
        option.hint(Component.translatable(tooltipKey));
        option.onSave(SodiumExtraClientMod.options());
        return option;
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
                .map(SodiumExtraConfig::capitalizeNamespacePart)
                .collect(Collectors.joining(" "));

        return Component.literal(name.isBlank() ? namespace : name);
    }

    private static String capitalizeNamespacePart(String part) {
        return part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1).toLowerCase(Locale.ROOT);
    }

    private static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        String name = Component.translatable(key).getString().replaceAll("§.", "");
        return Component.literal(name.isBlank() ? fallbackName(key) : name);
    }

    private static Component translatableName(Identifier identifier, String category) {
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

    private static Component translatableTooltip(Identifier identifier, String category) {
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

    private static final Component EMPTY_TITLE = Component.empty();

    public void register(ModSettingsSpec spec) {
        createAnimationsPage(spec);
        createParticlesPage(spec);
        createDetailsPage(spec);
        createRenderPage(spec);
        createExtraPage(spec);
    }

    private void createAnimationsPage(ModSettingsSpec spec) {
        boolean animationMixin = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.animation").isEnabled();
        spec.page(Component.translatable("sodium-extra.option.animations"))
                .group(EMPTY_TITLE, g -> g.add(spec.toggle(id("animations_all"))
                        .title(parseVanillaString("gui.socialInteractions.tab_all"))
                        .hint(Component.translatable("sodium-extra.option.animations_all.tooltip"))
                        .onSave(SodiumExtraClientMod.options())
                        .bind(value -> SodiumExtraClientMod.options().animationSettings.animation = value, () -> SodiumExtraClientMod.options().animationSettings.animation)
                        .defaults(true)
                        .enabled(animationMixin)))
                .group(EMPTY_TITLE, g -> {
                    g.add(spec.toggle(id("animate_water"))
                            .title(parseVanillaString("block.minecraft.water"))
                            .hint(Component.translatable("sodium-extra.option.animate_water.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.water = value, () -> SodiumExtraClientMod.options().animationSettings.water)
                            .defaults(true)
                            .enabled(animationMixin));
                    g.add(spec.toggle(id("animate_lava"))
                            .title(parseVanillaString("block.minecraft.lava"))
                            .hint(Component.translatable("sodium-extra.option.animate_lava.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.lava = value, () -> SodiumExtraClientMod.options().animationSettings.lava)
                            .defaults(true)
                            .enabled(animationMixin));
                    g.add(spec.toggle(id("animate_fire"))
                            .title(parseVanillaString("block.minecraft.fire"))
                            .hint(Component.translatable("sodium-extra.option.animate_fire.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.fire = value, () -> SodiumExtraClientMod.options().animationSettings.fire)
                            .defaults(true)
                            .enabled(animationMixin));
                    g.add(spec.toggle(id("animate_portal"))
                            .title(parseVanillaString("block.minecraft.nether_portal"))
                            .hint(Component.translatable("sodium-extra.option.animate_portal.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.portal = value, () -> SodiumExtraClientMod.options().animationSettings.portal)
                            .defaults(true)
                            .enabled(animationMixin));
                    g.add(spec.toggle(id("block_animations"))
                            .title(Component.translatable("sodium-extra.option.block_animations"))
                            .hint(Component.translatable("sodium-extra.option.block_animations.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.blockAnimations = value, () -> SodiumExtraClientMod.options().animationSettings.blockAnimations)
                            .defaults(true)
                            .enabled(animationMixin));
                    g.add(spec.toggle(id("animate_sculk_sensor"))
                            .title(parseVanillaString("block.minecraft.sculk_sensor"))
                            .hint(Component.translatable("sodium-extra.option.animate_sculk_sensor.tooltip"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(value -> SodiumExtraClientMod.options().animationSettings.sculkSensor = value, () -> SodiumExtraClientMod.options().animationSettings.sculkSensor)
                            .defaults(true)
                            .enabled(animationMixin));
                });
    }

    private void createParticlesPage(ModSettingsSpec spec) {
        boolean particleMixin = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled();
        PageSpec page = spec.page(parseVanillaString("options.particles"));

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(id("particles_all"))
                .title(parseVanillaString("gui.socialInteractions.tab_all"))
                .hint(Component.translatable("sodium-extra.option.particles_all.tooltip"))
                .onSave(SodiumExtraClientMod.options())
                .bind(value -> SodiumExtraClientMod.options().particleSettings.particles = value, () -> SodiumExtraClientMod.options().particleSettings.particles)
                .defaults(true)
                .enabled(particleMixin)));

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("rain_splash_particles"))
                    .title(parseVanillaString("subtitles.entity.generic.splash"))
                    .hint(Component.translatable("sodium-extra.option.rain_splash.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(value -> SodiumExtraClientMod.options().particleSettings.rainSplash = value, () -> SodiumExtraClientMod.options().particleSettings.rainSplash)
                    .defaults(true)
                    .enabled(particleMixin));
            g.add(spec.toggle(id("block_break_particles"))
                    .title(parseVanillaString("subtitles.block.generic.break"))
                    .hint(Component.translatable("sodium-extra.option.block_break.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(value -> SodiumExtraClientMod.options().particleSettings.blockBreak = value, () -> SodiumExtraClientMod.options().particleSettings.blockBreak)
                    .defaults(true)
                    .enabled(particleMixin));
            g.add(spec.toggle(id("block_breaking_particles"))
                    .title(parseVanillaString("subtitles.block.generic.hit"))
                    .hint(Component.translatable("sodium-extra.option.block_breaking.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(value -> SodiumExtraClientMod.options().particleSettings.blockBreaking = value, () -> SodiumExtraClientMod.options().particleSettings.blockBreaking)
                    .defaults(true)
                    .enabled(particleMixin));
        });

        Map<String, List<Identifier>> particlesByNamespace = new TreeMap<>(SodiumExtraConfig::compareParticleNamespace);
        BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .forEach(identifier -> particlesByNamespace.computeIfAbsent(identifier.getNamespace(), namespace -> new ArrayList<>()).add(identifier));

        particlesByNamespace.forEach((namespace, identifiers) -> page.group(particleNamespaceName(namespace), g -> {
            identifiers.stream()
                    .sorted((a, b) -> translatableName(a, "particles")
                            .getString()
                            .compareToIgnoreCase(translatableName(b, "particles").getString()))
                    .forEach(particleId -> g.add(spec.toggle(id("particle." + particleId.toLanguageKey("options.particles")))
                            .title(translatableName(particleId, "particles"))
                            .hint(translatableTooltip(particleId, "particles"))
                            .onSave(SodiumExtraClientMod.options())
                            .bind(
                                    value -> SodiumExtraClientMod.options().particleSettings.otherMap.put(particleId, value),
                                    () -> SodiumExtraClientMod.options().particleSettings.otherMap.getOrDefault(particleId, true)
                            )
                            .defaults(true)
                            .enabled(particleMixin)));
        }));
    }

    private void createDetailsPage(ModSettingsSpec spec) {
        PageSpec page = spec.page(Component.translatable("sodium-extra.option.details"));
        boolean sky = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky").isEnabled();
        boolean particle = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.particle").isEnabled();

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("sky"))
                    .title(Component.translatable("sodium-extra.option.sky"))
                    .hint(Component.translatable("sodium-extra.option.sky.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.sky = value,
                            () -> SodiumExtraClientMod.options().detailSettings.sky
                    )
                    .defaults(true)
                    .enabled(sky));
            g.add(spec.toggle(id("stars"))
                    .title(Component.translatable("sodium-extra.option.stars"))
                    .hint(Component.translatable("sodium-extra.option.stars.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.stars = value,
                            () -> SodiumExtraClientMod.options().detailSettings.stars
                    )
                    .defaults(true)
                    .enabled(sky));
            g.add(spec.toggle(id("sun"))
                    .title(Component.translatable("sodium-extra.option.sun"))
                    .hint(Component.translatable("sodium-extra.option.sun.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.sun = value,
                            () -> SodiumExtraClientMod.options().detailSettings.sun
                    )
                    .defaults(true)
                    .enabled(sky));
            g.add(spec.toggle(id("moon"))
                    .title(Component.translatable("sodium-extra.option.moon"))
                    .hint(Component.translatable("sodium-extra.option.moon.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.moon = value,
                            () -> SodiumExtraClientMod.options().detailSettings.moon
                    )
                    .defaults(true)
                    .enabled(sky));
            g.add(spec.toggle(id("rain_snow"))
                    .title(parseVanillaString("soundCategory.weather"))
                    .hint(Component.translatable("sodium-extra.option.rain_snow.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.rainSnow = value,
                            () -> SodiumExtraClientMod.options().detailSettings.rainSnow
                    )
                    .defaults(true)
                    .enabled(particle));
            g.add(spec.toggle(id("biome_colors"))
                    .title(Component.translatable("sodium-extra.option.biome_colors"))
                    .hint(Component.translatable("sodium-extra.option.biome_colors.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.biomeColors = value,
                            () -> SodiumExtraClientMod.options().detailSettings.biomeColors
                    )
                    .defaults(true)
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.biome_colors").isEnabled()));
            g.add(spec.toggle(id("sky_colors"))
                    .title(Component.translatable("sodium-extra.option.sky_colors"))
                    .hint(Component.translatable("sodium-extra.option.sky_colors.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().detailSettings.skyColors = value,
                            () -> SodiumExtraClientMod.options().detailSettings.skyColors
                    )
                    .defaults(true)
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.sky_colors").isEnabled()));
        });
    }

    private void createRenderPage(ModSettingsSpec spec) {
        PageSpec page = spec.page(Component.translatable("sodium-extra.option.render"));
        SodiumExtraGameOptions.FogSettings fogSettings = fogSettings();
        List<Identifier> dimensionFogEffectIds = getDimensionFogEffectIds(fogSettings);

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(ADVANCED_FOG_OPTION_ID)
                .enabled(isFogMixinEnabled())
                .title(Component.translatable("sodium-extra.option.advanced_fog_settings"))
                .hint(Component.translatable("sodium-extra.option.advanced_fog_settings.tooltip"))
                .onSave(SodiumExtraClientMod.options())
                .bind(
                        value -> SodiumExtraClientMod.options().renderSettings.fogSettings.advanced = value,
                        () -> SodiumExtraClientMod.options().renderSettings.fogSettings.advanced
                )
                .defaults(false)));

        page.group(EMPTY_TITLE, g -> {
            g.add(fogOption(spec.slider(id("single_fog")), SodiumExtraConfig::isSingleFogOptionEnabled, "sodium-extra.option.fog_distance", "sodium-extra.option.fog_distance.tooltip")
                    .spanProvider(FogDistanceHelper::getFogDistanceRange)
                    .format(ControlValueFormatterExtended.fogDistance())
                    .bind(
                            value -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.distanceChunks = value,
                            () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.distanceChunks
                    )
                    .defaults(0));
            g.add(fogOption(spec.slider(id("fog_start")), SodiumExtraConfig::isSingleFogOptionEnabled, "sodium-extra.option.fog_start", "sodium-extra.option.fog_start.tooltip")
                    .span(0, 100, 1)
                    .format(ValueFormatter.percent())
                    .bind(SodiumExtraConfig::setAtmosphericFogStart, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.startPercent)
                    .defaults(100));
            g.add(fogOption(spec.slider(id("cloud_fog")), SodiumExtraConfig::isSingleFogOptionEnabled, "sodium-extra.option.cloud_fog", "sodium-extra.option.cloud_fog.tooltip")
                    .span(0, 100, 1)
                    .format(ValueFormatter.percent())
                    .bind(SodiumExtraConfig::setAtmosphericCloudFog, () -> SodiumExtraClientMod.options().renderSettings.fogSettings.atmospheric.cloudFogPercent)
                    .defaults(FogDistanceHelper.VANILLA_CLOUD_FOG_PERCENT));
        });

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(MULTI_DIMENSION_FOG_OPTION_ID)
                .enabledWhen(SodiumExtraConfig::isAdvancedFogOptionEnabled)
                .title(Component.translatable("sodium-extra.option.multi_dimension_fog"))
                .hint(Component.translatable("sodium-extra.option.multi_dimension_fog.tooltip"))
                .onSave(SodiumExtraClientMod.options())
                .bind(
                        value -> SodiumExtraClientMod.options().renderSettings.fogSettings.multiDimensionFogControl = value,
                        () -> SodiumExtraClientMod.options().renderSettings.fogSettings.multiDimensionFogControl
                )
                .defaults(false)));

        dimensionFogEffectIds.forEach(identifier -> {
            String dimensionKey = identifier.toLanguageKey("options.dimensions");
            page.group(translatableName(identifier, "dimensions"), g -> {
                g.add(fogOption(spec.slider(id("fog." + dimensionKey)), SodiumExtraConfig::isDimensionFogOptionEnabled, "sodium-extra.option.fog_distance", "sodium-extra.option.fog.tooltip")
                        .spanProvider(FogDistanceHelper::getFogDistanceRange)
                        .format(ControlValueFormatterExtended.fogDistance())
                        .bind(
                                value -> SodiumExtraClientMod.options().renderSettings.fogSettings.getOrCreateDimensionOverride(identifier).distanceChunks = value,
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.getDimensionFogDistance(identifier)
                        )
                        .defaults(0));
                g.add(fogOption(spec.slider(id("fog_start." + dimensionKey)), SodiumExtraConfig::isDimensionFogOptionEnabled, "sodium-extra.option.fog_start", "sodium-extra.option.fog_start.tooltip")
                        .span(0, 100, 1)
                        .format(ValueFormatter.percent())
                        .bind(
                                value -> setDimensionFogStart(identifier, value),
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.getDimensionFogStart(identifier)
                        )
                        .defaults(100));
                g.add(fogOption(spec.slider(id("cloud_fog." + dimensionKey)), SodiumExtraConfig::isDimensionFogOptionEnabled, "sodium-extra.option.cloud_fog", "sodium-extra.option.cloud_fog.tooltip")
                        .span(0, 100, 1)
                        .format(ValueFormatter.percent())
                        .bind(
                                value -> setDimensionCloudFog(identifier, value),
                                () -> SodiumExtraClientMod.options().renderSettings.fogSettings.getDimensionCloudFogPercent(identifier)
                        )
                        .defaults(FogDistanceHelper.VANILLA_CLOUD_FOG_PERCENT));
            });
        });

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(PROTECTED_GAMEPLAY_FOG_OPTION_ID)
                    .enabledWhen(SodiumExtraConfig::isAdvancedFogOptionEnabled)
                    .title(Component.translatable("sodium-extra.option.protected_gameplay_fog"))
                    .hint(Component.translatable("sodium-extra.option.protected_gameplay_fog.tooltip"))
                    .onSave(SodiumExtraClientMod.options())
                    .bind(
                            value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.enabledWhenAllowed = value,
                            () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.enabledWhenAllowed
                    )
                    .defaults(false));
            g.add(protectedFogSlider(spec, id("protected_gameplay_fog.blindness"), "blindness",
                    value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.blindnessDistanceBlocks = value,
                    () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.blindnessDistanceBlocks));
            g.add(protectedFogSlider(spec, id("protected_gameplay_fog.darkness"), "darkness",
                    value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.darknessDistanceBlocks = value,
                    () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.darknessDistanceBlocks));
            g.add(protectedFogSlider(spec, id("protected_gameplay_fog.lava"), "lava",
                    value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.lavaDistanceBlocks = value,
                    () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.lavaDistanceBlocks));
            g.add(protectedFogSlider(spec, id("protected_gameplay_fog.powder_snow"), "powder_snow",
                    value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.powderSnowDistanceBlocks = value,
                    () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.powderSnowDistanceBlocks));
            g.add(protectedFogSlider(spec, id("protected_gameplay_fog.water"), "water",
                    value -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.waterDistanceBlocks = value,
                    () -> SodiumExtraClientMod.options().renderSettings.fogSettings.protectedGameplay.waterDistanceBlocks));
        });

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(id("light_updates"))
                .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.light_updates").isEnabled())
                .title(Component.translatable("sodium-extra.option.light_updates"))
                .hint(Component.translatable("sodium-extra.option.light_updates.tooltip"))
                .bind((value) -> SodiumExtraClientMod.options().renderSettings.lightUpdates = value, () -> SodiumExtraClientMod.options().renderSettings.lightUpdates)
                .onSave(SodiumExtraClientMod.options())
                .defaults(true)));

        boolean renderEntity = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.entity").isEnabled();
        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("item_frame"))
                    .enabled(renderEntity)
                    .title(parseVanillaString("entity.minecraft.item_frame"))
                    .hint(Component.translatable("sodium-extra.option.item_frames.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.itemFrame = value, () -> SodiumExtraClientMod.options().renderSettings.itemFrame)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
            g.add(spec.toggle(id("armor_stands"))
                    .enabled(renderEntity)
                    .title(parseVanillaString("entity.minecraft.armor_stand"))
                    .hint(Component.translatable("sodium-extra.option.armor_stands.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.armorStand = value, () -> SodiumExtraClientMod.options().renderSettings.armorStand)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
            g.add(spec.toggle(id("paintings"))
                    .enabled(renderEntity)
                    .title(parseVanillaString("entity.minecraft.painting"))
                    .hint(Component.translatable("sodium-extra.option.paintings.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.painting = value, () -> SodiumExtraClientMod.options().renderSettings.painting)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
        });

        boolean renderBlockEntity = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.render.block.entity").isEnabled();
        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("beacon_beam"))
                    .enabled(renderBlockEntity)
                    .title(Component.translatable("sodium-extra.option.beacon_beam"))
                    .hint(Component.translatable("sodium-extra.option.beacon_beam.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.beaconBeam = value, () -> SodiumExtraClientMod.options().renderSettings.beaconBeam)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
            g.add(spec.toggle(id("limit_beacon_beam_height"))
                    .enabled(renderBlockEntity)
                    .title(Component.translatable("sodium-extra.option.limit_beacon_beam_height"))
                    .hint(Component.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight = value, () -> SodiumExtraClientMod.options().renderSettings.limitBeaconBeamHeight)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(false));
            g.add(spec.toggle(id("enchanting_table_book"))
                    .enabled(renderBlockEntity)
                    .title(Component.translatable("sodium-extra.option.enchanting_table_book"))
                    .hint(Component.translatable("sodium-extra.option.enchanting_table_book.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.enchantingTableBook = value, () -> SodiumExtraClientMod.options().renderSettings.enchantingTableBook)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
            g.add(spec.toggle(id("piston"))
                    .enabled(renderBlockEntity)
                    .title(parseVanillaString("block.minecraft.piston"))
                    .hint(Component.translatable("sodium-extra.option.piston.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.piston = value, () -> SodiumExtraClientMod.options().renderSettings.piston)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
        });
        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("item_frame_name_tag"))
                    .enabled(renderEntity)
                    .title(Component.translatable("sodium-extra.option.item_frame_name_tag"))
                    .hint(Component.translatable("sodium-extra.option.item_frame_name_tag.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.itemFrameNameTag = value, () -> SodiumExtraClientMod.options().renderSettings.itemFrameNameTag)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
            g.add(spec.toggle(id("player_name_tag"))
                    .enabled(renderEntity)
                    .title(Component.translatable("sodium-extra.option.player_name_tag"))
                    .hint(Component.translatable("sodium-extra.option.player_name_tag.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().renderSettings.playerNameTag = value, () -> SodiumExtraClientMod.options().renderSettings.playerNameTag)
                    .onSave(SodiumExtraClientMod.options())
                    .defaults(true));
        });
    }

    private static ModSettingsSpec.OptionSpec<Integer> protectedFogSlider(ModSettingsSpec spec, Identifier id, String key,
            java.util.function.Consumer<Integer> setter, java.util.function.Supplier<Integer> getter) {
        return spec.slider(id)
                .span(FogDistanceHelper.getProtectedGameplayFogDistanceRange())
                .format(ControlValueFormatterExtended.protectedFogDistance())
                .enabledWhen(SodiumExtraConfig::isProtectedGameplayFogOptionEnabled)
                .title(Component.translatable("sodium-extra.option.protected_gameplay_fog." + key))
                .hint(Component.translatable("sodium-extra.option.protected_gameplay_fog." + key + ".tooltip"))
                .onSave(SodiumExtraClientMod.options())
                .bind(setter, getter)
                .defaults(0);
    }

    private void createExtraPage(ModSettingsSpec spec) {
        PageSpec page = spec.page(Component.translatable("sodium-extra.option.extras"));

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(id("reduce_resolution_on_mac"))
                .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && System.getProperty("os.name").toLowerCase().contains("mac"))
                .title(Component.translatable("sodium-extra.option.reduce_resolution_on_mac"))
                .hint(Component.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                .impact(PerformanceImpact.HIGH)
                .bind((value) -> SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac = value, () -> SodiumExtraClientMod.options().extraSettings.reduceResolutionOnMac)
                .onSave(SodiumExtraClientMod.options())
                .defaults(false)));

        page.group(EMPTY_TITLE, g -> g.add(spec.toggle(WAYLAND_FULLSCREEN_RESOLUTION_OPTION_ID)
                .enabled(FullscreenResolutionConfirmation.isWaylandOrXWayland())
                .title(Component.translatable("sodium-extra.option.wayland_fullscreen_resolution"))
                .hint(Component.translatable("sodium-extra.option.wayland_fullscreen_resolution.tooltip"))
                .impact(PerformanceImpact.MEDIUM)
                .bind((value) -> {
                    SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution = value;
                    FullscreenResolutionConfirmation.updateFullscreenResolutionControlState();
                    if (!value) {
                        SodiumExtraClientMod.disarmWaylandFullscreenResolutionRecovery();
                    }
                }, () -> SodiumExtraClientMod.options().extraSettings.waylandFullscreenResolution)
                .onSave(SodiumExtraClientMod.options())
                .defaults(false)));

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.choice(id("overlay_corner"), SodiumExtraGameOptions.OverlayCorner.class)
                    .title(Component.translatable("sodium-extra.option.overlay_corner"))
                    .hint(Component.translatable("sodium-extra.option.overlay_corner.tooltip"))
                    .defaults(SodiumExtraGameOptions.OverlayCorner.TOP_LEFT)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.overlayCorner = value, () -> SodiumExtraClientMod.options().extraSettings.overlayCorner)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.choice(id("text_contrast"), SodiumExtraGameOptions.TextContrast.class)
                    .title(Component.translatable("sodium-extra.option.text_contrast"))
                    .hint(Component.translatable("sodium-extra.option.text_contrast.tooltip"))
                    .defaults(SodiumExtraGameOptions.TextContrast.NONE)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.textContrast = value, () -> SodiumExtraClientMod.options().extraSettings.textContrast)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("show_fps"))
                    .title(Component.translatable("sodium-extra.option.show_fps"))
                    .hint(Component.translatable("sodium-extra.option.show_fps.tooltip"))
                    .defaults(false)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.showFps = value, () -> SodiumExtraClientMod.options().extraSettings.showFps)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("show_fps_extended"))
                    .enabledWhen(SodiumExtraConfig::isFpsExtendedEnabled)
                    .title(Component.translatable("sodium-extra.option.show_fps_extended"))
                    .hint(Component.translatable("sodium-extra.option.show_fps_extended.tooltip"))
                    .defaults(true)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.showFPSExtended = value, () -> SodiumExtraClientMod.options().extraSettings.showFPSExtended)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("show_coordinates"))
                    .title(Component.translatable("sodium-extra.option.show_coordinates"))
                    .hint(Component.translatable("sodium-extra.option.show_coordinates.tooltip"))
                    .defaults(false)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.showCoords = value, () -> SodiumExtraClientMod.options().extraSettings.showCoords)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(CLOUD_HEIGHT_OVERRIDE_OPTION_ID)
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.cloud").isEnabled())
                    .title(Component.translatable("sodium-extra.option.cloud_height_override"))
                    .hint(Component.translatable("sodium-extra.option.cloud_height_override.tooltip"))
                    .defaults(false)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.cloudHeightOverride = value, () -> SodiumExtraClientMod.options().extraSettings.cloudHeightOverride)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.slider(id("cloud_height"))
                    .span(-64, 319, 1)
                    .format(ValueFormatter.plainNumber())
                    .enabledWhen(SodiumExtraConfig::isCloudHeightOptionEnabled)
                    .title(Component.translatable("sodium-extra.option.cloud_height"))
                    .hint(Component.translatable("sodium-extra.option.cloud_height.tooltip"))
                    .defaults(192)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.cloudHeight = value, () -> SodiumExtraClientMod.options().extraSettings.cloudHeight)
                    .onSave(SodiumExtraClientMod.options()));
        });

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("advanced_item_tooltips"))
                    .title(Component.translatable("sodium-extra.option.advanced_item_tooltips"))
                    .hint(Component.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                    .onSave(() -> Minecraft.getInstance().options.save())
                    .bind((value) -> Minecraft.getInstance().options.advancedItemTooltips = value, () -> Minecraft.getInstance().options.advancedItemTooltips)
                    .defaults(false));
            g.add(spec.button()
                    .title(Component.translatable("sodium-extra.option.more_hud_overlays"))
                    .hint(Component.translatable("sodium-extra.option.more_hud_overlays.tooltip"))
                    .onClick(() -> Minecraft.getInstance().setScreenAndShow(new DebugOptionsScreen())));
        });

        boolean toasts = SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.toasts").isEnabled();
        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("toasts"))
                    .enabled(toasts)
                    .title(Component.translatable("sodium-extra.option.toasts"))
                    .hint(Component.translatable("sodium-extra.option.toasts.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.toasts = value, () -> SodiumExtraClientMod.options().extraSettings.toasts)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("advancement_toast"))
                    .enabled(toasts)
                    .title(Component.translatable("sodium-extra.option.advancement_toast"))
                    .hint(Component.translatable("sodium-extra.option.advancement_toast.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.advancementToast = value, () -> SodiumExtraClientMod.options().extraSettings.advancementToast)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("recipe_toast"))
                    .enabled(toasts)
                    .title(Component.translatable("sodium-extra.option.recipe_toast"))
                    .hint(Component.translatable("sodium-extra.option.recipe_toast.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.recipeToast = value, () -> SodiumExtraClientMod.options().extraSettings.recipeToast)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("system_toast"))
                    .enabled(toasts)
                    .title(Component.translatable("sodium-extra.option.system_toast"))
                    .hint(Component.translatable("sodium-extra.option.system_toast.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.systemToast = value, () -> SodiumExtraClientMod.options().extraSettings.systemToast)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("tutorial_toast"))
                    .enabled(toasts)
                    .title(Component.translatable("sodium-extra.option.tutorial_toast"))
                    .hint(Component.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.tutorialToast = value, () -> SodiumExtraClientMod.options().extraSettings.tutorialToast)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
        });

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("instant_sneak"))
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.instant_sneak").isEnabled())
                    .title(Component.translatable("sodium-extra.option.instant_sneak"))
                    .hint(Component.translatable("sodium-extra.option.instant_sneak.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.instantSneak = value, () -> SodiumExtraClientMod.options().extraSettings.instantSneak)
                    .defaults(false)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.toggle(id("prevent_shaders"))
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.prevent_shaders").isEnabled())
                    .title(Component.translatable("sodium-extra.option.prevent_shaders"))
                    .hint(Component.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                    .bind(value -> {
                        SodiumExtraClientMod.options().extraSettings.preventShaders = value;
                        Minecraft client = Minecraft.getInstance();
                        if (client.level != null) {
                            client.levelExtractor.allChanged();
                        }
                    }, () -> SodiumExtraClientMod.options().extraSettings.preventShaders)
                    .defaults(false)
                    .onSave(SodiumExtraClientMod.options()));
        });

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(PANINI_PROJECTION_OPTION_ID)
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.panini_projection").isEnabled())
                    .title(Component.translatable("sodium-extra.option.panini_projection"))
                    .hint(Component.translatable("sodium-extra.option.panini_projection.tooltip"))
                    .impact(PerformanceImpact.MEDIUM)
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.paniniProjection = value, () -> SodiumExtraClientMod.options().extraSettings.paniniProjection)
                    .defaults(false)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.slider(id("panini_projection_strength"))
                    .span(0, 100, 1)
                    .format(ValueFormatter.percent())
                    .enabledWhen(SodiumExtraConfig::isPaniniProjectionOptionEnabled)
                    .title(Component.translatable("sodium-extra.option.panini_projection_strength"))
                    .hint(Component.translatable("sodium-extra.option.panini_projection_strength.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.paniniProjectionStrength = value, () -> SodiumExtraClientMod.options().extraSettings.paniniProjectionStrength)
                    .defaults(25)
                    .onSave(SodiumExtraClientMod.options()));
        });

        page.group(EMPTY_TITLE, g -> {
            g.add(spec.toggle(id("steady_debug_hud"))
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                    .title(Component.translatable("sodium-extra.option.steady_debug_hud"))
                    .hint(Component.translatable("sodium-extra.option.steady_debug_hud.tooltip"))
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.steadyDebugHud = value, () -> SodiumExtraClientMod.options().extraSettings.steadyDebugHud)
                    .defaults(true)
                    .onSave(SodiumExtraClientMod.options()));
            g.add(spec.slider(id("steady_debug_hud_refresh_interval"))
                    .span(1, 20, 1)
                    .format(ControlValueFormatterExtended.ticks())
                    .enabled(SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.steady_debug_hud").isEnabled())
                    .title(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"))
                    .hint(Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                    .defaults(1)
                    .onSave(SodiumExtraClientMod.options())
                    .bind((value) -> SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval = value, () -> SodiumExtraClientMod.options().extraSettings.steadyDebugHudRefreshInterval));
        });
    }
}
