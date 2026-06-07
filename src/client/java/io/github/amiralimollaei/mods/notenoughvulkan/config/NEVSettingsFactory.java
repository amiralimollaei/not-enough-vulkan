package io.github.amiralimollaei.mods.notenoughvulkan.config;

import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.FogTypeConfig;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.api.VkModSettingsEntryBuilder;
import net.vulkanmod.config.api.VkModSettingsFactory;
import net.vulkanmod.config.api.page.VkPageOptionsBuilder;
import net.vulkanmod.config.gui.ModSettingsEntry;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.RangeOption;
import net.vulkanmod.config.option.SwitchOption;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class NEVSettingsFactory implements VkModSettingsFactory {
    private static final SodiumExtraGameOptions sodiumExtraOptions = SodiumExtraClientMod.options();
    private static final NotEnoughVulkanGameOptions notEnoughVulkanOptions = NotEnoughVulkanClientMod.options();
    private static final Minecraft minecraft = Minecraft.getInstance();

    @Override
    public ModSettingsEntry build(VkModSettingsEntryBuilder builder) {
        // general mod info
        builder
                .setModName(Component.literal("NEV").withStyle(ChatFormatting.RED))
                .setIcon(Identifier.fromNamespaceAndPath("not-enough-vulkan", "textures/icon-64x.png"))
                .setOnApply(() -> {
                    SodiumExtraClientMod.options().writeChanges();
                    NotEnoughVulkanClientMod.options().writeChanges();
                });

        // animations page
        builder
                .withPage(Component.translatable("sodium-extra.option.animations").getString())
                    .withOptionBlock("")
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("gui.socialInteractions.tab_all"),
                                        value -> sodiumExtraOptions.animationSettings.animation = value,
                                        () -> sodiumExtraOptions.animationSettings.animation
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animations_all.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("block.minecraft.water"),
                                        value -> sodiumExtraOptions.animationSettings.water = value,
                                        () -> sodiumExtraOptions.animationSettings.water
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animate_water.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("block.minecraft.lava"),
                                        value -> sodiumExtraOptions.animationSettings.lava = value,
                                        () -> sodiumExtraOptions.animationSettings.lava
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animate_lava.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("block.minecraft.fire"),
                                        value -> sodiumExtraOptions.animationSettings.fire = value,
                                        () -> sodiumExtraOptions.animationSettings.fire
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animate_fire.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("block.minecraft.nether_portal"),
                                        value -> sodiumExtraOptions.animationSettings.portal = value,
                                        () -> sodiumExtraOptions.animationSettings.portal
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animate_portal.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("sodium-extra.option.block_animations"),
                                        value -> sodiumExtraOptions.animationSettings.blockAnimations = value,
                                        () -> sodiumExtraOptions.animationSettings.blockAnimations
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.block_animations.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("block.minecraft.sculk_sensor"),
                                        value -> sodiumExtraOptions.animationSettings.sculkSensor = value,
                                        () -> sodiumExtraOptions.animationSettings.sculkSensor
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.animate_sculk_sensor.tooltip"))
                        );

        // particles page
        var particlesPageBuilder = builder.withPage(parseVanillaString("options.particles").getString());
        particlesPageBuilder
                    .withOptionBlock("")
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("gui.socialInteractions.tab_all"),
                                value -> sodiumExtraOptions.particleSettings.particles = value,
                                () -> sodiumExtraOptions.particleSettings.particles
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.particles_all.tooltip"))
                        )
                    .finish()
                    .withOptionBlock("General")
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("subtitles.entity.generic.splash"),
                                        value -> sodiumExtraOptions.particleSettings.rainSplash = value,
                                        () -> sodiumExtraOptions.particleSettings.rainSplash
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.rain_splash.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("subtitles.block.generic.break"),
                                        value -> sodiumExtraOptions.particleSettings.blockBreak = value,
                                        () -> sodiumExtraOptions.particleSettings.blockBreak
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.block_break.tooltip"))
                        )
                        .addOption(
                                new SwitchOption(
                                        parseVanillaString("subtitles.block.generic.hit"),
                                        value -> sodiumExtraOptions.particleSettings.blockBreaking = value,
                                        () -> sodiumExtraOptions.particleSettings.blockBreaking
                                ).setTooltip((v) -> Component.translatable("sodium-extra.option.block_breaking.tooltip"))
                        );
        var optionsBuilder = particlesPageBuilder.withOptionBlock("Individual");
        BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .sorted((a, b) -> particleName(a)
                        .getString()
                        .compareToIgnoreCase(particleName(b).getString()))
                .forEach(id -> optionsBuilder.addOption(newParticleSwitchOption(id)));

        // details page
        builder.withPage(Component.translatable("sodium-extra.option.details").getString())
                .withOptionBlock("")
                    .addOption(newStandardSwitchOption("sky",
                            value -> sodiumExtraOptions.detailSettings.sky = value,
                            () -> sodiumExtraOptions.detailSettings.sky
                    ))
                    .addOption(newStandardSwitchOption("stars",
                            value -> sodiumExtraOptions.detailSettings.stars = value,
                            () -> sodiumExtraOptions.detailSettings.stars
                    ))
                    .addOption(newStandardSwitchOption("sun",
                            value -> sodiumExtraOptions.detailSettings.sun = value,
                            () -> sodiumExtraOptions.detailSettings.sun
                    ))
                    .addOption(newStandardSwitchOption("moon",
                            value -> sodiumExtraOptions.detailSettings.moon = value,
                            () -> sodiumExtraOptions.detailSettings.moon
                    ))
                    .addOption(new SwitchOption(parseVanillaString("soundCategory.weather"),
                            value -> sodiumExtraOptions.detailSettings.sky = value,
                            () -> sodiumExtraOptions.detailSettings.sky
                    ).setTooltip((v) -> Component.translatable("sodium-extra.option.rain_snow.tooltip")))
                    .addOption(newStandardSwitchOption("biome_colors",
                                    value -> sodiumExtraOptions.detailSettings.biomeColors = value,
                                    () -> sodiumExtraOptions.detailSettings.biomeColors
                    ))
                    .addOption(newStandardSwitchOption("sky_colors",
                            value -> sodiumExtraOptions.detailSettings.skyColors = value,
                            () -> sodiumExtraOptions.detailSettings.skyColors
                    ));

        var renderPageBuilder = builder.withPage(Component.translatable("sodium-extra.option.render").getString());
        renderPageBuilder.withOptionBlock("")
                .addOption(newStandardSwitchOption("global_fog",
                        value -> sodiumExtraOptions.renderSettings.globalFog = value,
                        () -> sodiumExtraOptions.renderSettings.globalFog
                ));
        Arrays.stream(FogType.values())
                .sorted(Comparator.comparing(Enum::name))
                .filter(type -> type != FogType.NONE)
                .forEach(fogtype -> addFogRenderOptionBlock(renderPageBuilder, fogtype));
        renderPageBuilder.withOptionBlock("Light Updates")
                .addOption(newStandardSwitchOption("light_updates",
                                value -> sodiumExtraOptions.renderSettings.lightUpdates = value,
                                () -> sodiumExtraOptions.renderSettings.lightUpdates
                ));
        renderPageBuilder.withOptionBlock("Render")
                .addOption(
                        new SwitchOption(
                                parseVanillaString("entity.minecraft.item_frame"),
                                value -> sodiumExtraOptions.renderSettings.itemFrame = value,
                                () -> sodiumExtraOptions.renderSettings.itemFrame
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.item_frames.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                parseVanillaString("entity.minecraft.armor_stand"),
                                value -> sodiumExtraOptions.renderSettings.armorStand = value,
                                () -> sodiumExtraOptions.renderSettings.armorStand
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.armor_stands.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                parseVanillaString("entity.minecraft.painting"),
                                value -> sodiumExtraOptions.renderSettings.painting = value,
                                () -> sodiumExtraOptions.renderSettings.painting
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.paintings.tooltip"))
                );
        renderPageBuilder.withOptionBlock("Block Render")
                .addOption(
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.beacon_beam"),
                                value -> sodiumExtraOptions.renderSettings.beaconBeam = value,
                                () -> sodiumExtraOptions.renderSettings.beaconBeam
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.beacon_beam.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.limit_beacon_beam_height"),
                                value -> sodiumExtraOptions.renderSettings.limitBeaconBeamHeight = value,
                                () -> sodiumExtraOptions.renderSettings.limitBeaconBeamHeight
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.enchanting_table_book"),
                                value -> sodiumExtraOptions.renderSettings.enchantingTableBook = value,
                                () -> sodiumExtraOptions.renderSettings.enchantingTableBook
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.enchanting_table_book.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                parseVanillaString("block.minecraft.piston"),
                                value -> sodiumExtraOptions.renderSettings.piston = value,
                                () -> sodiumExtraOptions.renderSettings.piston
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.piston.tooltip"))
                );
        renderPageBuilder.withOptionBlock("Name Tags")
                .addOption(
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.item_frame_name_tag"),
                                value -> sodiumExtraOptions.renderSettings.itemFrameNameTag = value,
                                () -> sodiumExtraOptions.renderSettings.itemFrameNameTag
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.item_frame_name_tag.tooltip"))
                )
                .addOption(
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.player_name_tag"),
                                value -> sodiumExtraOptions.renderSettings.playerNameTag = value,
                                () -> sodiumExtraOptions.renderSettings.playerNameTag
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.player_name_tag.tooltip"))
                );

        builder.withPage(Component.translatable("sodium-extra.option.extras").getString())
                .withOptionBlock("Compatibility")
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.reduce_resolution_on_mac"),
                                    (value) -> sodiumExtraOptions.extraSettings.reduceResolutionOnMac = value,
                                    () -> sodiumExtraOptions.extraSettings.reduceResolutionOnMac
                            )
                            .setTooltip((v) -> Component.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"))
                            .setActivationFn(
                                    () -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && Platform.isMacOS()
                            )
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("not-enough-vulkan.option.skip_wayland_patches"),
                                    (value) -> notEnoughVulkanOptions.compatSettings.skipWaylandPatches = value,
                                    () -> notEnoughVulkanOptions.compatSettings.skipWaylandPatches
                            )
                            .setTooltip((v) -> Component.translatable("not-enough-vulkan.option.skip_wayland_patches.tooltip"))
                            .setActivationFn(
                                    () -> NotEnoughVulkanClientMod.mixinConfig().getOptions().get("mixin.compat.skip_wayland_patches").isEnabled() && Platform.isWayLand()
                            )
                    )
                .finish()
                .withOptionBlock("Overlay")
                    .addOption(
                            new CyclingOption<>(
                                    Component.translatable("sodium-extra.option.overlay_corner"),
                                    SodiumExtraGameOptions.OverlayCorner.values(),
                                    (value) -> sodiumExtraOptions.extraSettings.overlayCorner = value,
                                    () -> sodiumExtraOptions.extraSettings.overlayCorner
                            )
                            .setTooltip((v) -> Component.translatable("sodium-extra.option.overlay_corner.tooltip"))
                            .setTranslator(SodiumExtraGameOptions.OverlayCorner::getLocalizedName)
                    )
                    .addOption(
                            new CyclingOption<>(
                                    Component.translatable("sodium-extra.option.text_contrast"),
                                    SodiumExtraGameOptions.TextContrast.values(),
                                    (value) -> sodiumExtraOptions.extraSettings.textContrast = value,
                                    () -> sodiumExtraOptions.extraSettings.textContrast
                            )
                            .setTooltip((v) -> Component.translatable("sodium-extra.option.text_contrast.tooltip"))
                            .setTranslator(SodiumExtraGameOptions.TextContrast::getLocalizedName)
                    )
                    .addOption(
                            new SwitchOption(Component.translatable("sodium-extra.option.show_fps"),
                                    (value) -> sodiumExtraOptions.extraSettings.showFps = value,
                                    () -> sodiumExtraOptions.extraSettings.showFps
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_fps.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(Component.translatable("sodium-extra.option.show_fps_extended"),
                                    (value) -> sodiumExtraOptions.extraSettings.showFPSExtended = value,
                                    () -> sodiumExtraOptions.extraSettings.showFPSExtended
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_coordinates.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(Component.translatable("sodium-extra.option.show_coordinates"),
                                    (value) -> sodiumExtraOptions.extraSettings.showCoords = value,
                                    () -> sodiumExtraOptions.extraSettings.showCoords
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_coordinates.tooltip"))
                    )
                    .addOption(
                            new RangeOption(Component.translatable("sodium-extra.option.cloud_height"),
                                    -64, 320, 1,
                                    (value) -> sodiumExtraOptions.extraSettings.cloudHeight = value,
                                    () -> sodiumExtraOptions.extraSettings.cloudHeight
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.cloud_height.tooltip"))
                    )
                .finish()
                .withOptionBlock("Advanced Item Tooltips")
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.advanced_item_tooltips"),
                                    (value) -> {
                                        Minecraft.getInstance().options.advancedItemTooltips = value;
                                        Minecraft.getInstance().options.save();
                                    },
                                    () -> Minecraft.getInstance().options.advancedItemTooltips
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                    )
                .finish()
                .withOptionBlock("Toasts")
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.toasts"),
                                    (value) -> sodiumExtraOptions.extraSettings.toasts = value,
                                    () -> sodiumExtraOptions.extraSettings.toasts
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.toasts.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.advancement_toast"),
                                    (value) -> sodiumExtraOptions.extraSettings.advancementToast = value,
                                    () -> sodiumExtraOptions.extraSettings.advancementToast
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.advancement_toast.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.recipe_toast"),
                                    (value) -> sodiumExtraOptions.extraSettings.recipeToast = value,
                                    () -> sodiumExtraOptions.extraSettings.recipeToast
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.recipe_toast.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.system_toast"),
                                    (value) -> sodiumExtraOptions.extraSettings.systemToast = value,
                                    () -> sodiumExtraOptions.extraSettings.systemToast
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.system_toast.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.tutorial_toast"),
                                    (value) -> sodiumExtraOptions.extraSettings.tutorialToast = value,
                                    () -> sodiumExtraOptions.extraSettings.tutorialToast
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                    )
                .finish()
                .withOptionBlock("Other")
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.instant_sneak"),
                                    (value) -> sodiumExtraOptions.extraSettings.instantSneak = value,
                                    () -> sodiumExtraOptions.extraSettings.instantSneak
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.instant_sneak.tooltip"))
                    )
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.prevent_shaders"),
                                    (value) -> {
                                        sodiumExtraOptions.extraSettings.preventShaders = value;
                                        minecraft.levelRenderer.allChanged();
                                    },
                                    () -> sodiumExtraOptions.extraSettings.preventShaders
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                    )
                .finish()
                .withOptionBlock("Debug Hud")
                    .addOption(
                            new SwitchOption(
                                    Component.translatable("sodium-extra.option.steady_debug_hud"),
                                    (value) -> sodiumExtraOptions.extraSettings.steadyDebugHud = value,
                                    () -> sodiumExtraOptions.extraSettings.steadyDebugHud
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.steady_debug_hud.tooltip"))
                    )
                    .addOption(
                            new RangeOption(
                                    Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"),
                                    1, 20, 1,
                                    (value) -> sodiumExtraOptions.extraSettings.steadyDebugHudRefreshInterval = value,
                                    () -> sodiumExtraOptions.extraSettings.steadyDebugHudRefreshInterval
                            ).setTooltip((v) -> Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                    );

        // build
        return builder.build();
    }


    public static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        return Component.literal(Component.translatable(key).getString().replaceAll("§.", ""));
    }

    static Component particleName(Identifier identifier) {
        String key = identifier.toLanguageKey("options.particles");
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.literal(
                    Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                            .collect(Collectors.joining(" "))
            );
        }
        return translatable;
    }

    private static Component particleTooltip(Identifier identifier) {
        String key = identifier.toLanguageKey("options.particles.tooltip");
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.translatable(
                    "sodium-extra.option.particles.tooltips",
                    particleName(identifier)
            );
        }
        return translatable;
    }

    static Component fogTypeName(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase();
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            String pretty = Arrays.stream(type.name().split("_"))
                    .map(s -> s.charAt(0) + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" ")) + " Fog";
            return Component.literal(pretty);
        }
        return translated;
    }

    static Component fogTypeTooltip(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase() + ".tooltip";
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            return Component.translatable("sodium-extra.option.fog_type.default.tooltip", fogTypeName(type));
        }
        return translated;
    }

    static SwitchOption newParticleSwitchOption(Identifier id) {
        SwitchOption switchOption = new SwitchOption(
                particleName(id),
                value -> sodiumExtraOptions.particleSettings.otherMap.put(id, value),
                () -> sodiumExtraOptions.particleSettings.otherMap.computeIfAbsent(id, k -> true)
        );
        switchOption.setTooltip((v) -> particleTooltip(id));
        return switchOption;
    }

    static SwitchOption newStandardSwitchOption(String key, Consumer<Boolean> setter, Supplier<Boolean> getter) {
        SwitchOption option = new SwitchOption(Component.translatable("sodium-extra.option."+key), setter, getter);
        option.setTooltip((v) -> Component.translatable("sodium-extra.option."+key+".tooltip"));
        return option;
    }

    private static void addFogRenderOptionBlock(VkPageOptionsBuilder renderPageBuilder, FogType fogType) {
        var optionBlockBuilder = renderPageBuilder.withOptionBlock("Fog Type: " + fogType.name());

        // Environment Start
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.environment_start", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.environmentStartMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentStartMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.environment_start.tooltip"))
        );

        // Environment End
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.environment_end", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.environmentEndMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).environmentEndMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.environment_end.tooltip"))
        );

        // Render Start
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.render_distance_start", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.renderDistanceStartMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceStartMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.render_distance_start.tooltip"))
        );

        // Render End
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.render_distance_end", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.renderDistanceEndMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).renderDistanceEndMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.render_distance_end.tooltip"))
        );

        // Sky End
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.sky_end", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.skyEndMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).skyEndMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.sky_end.tooltip"))
        );

        // Sky End
        optionBlockBuilder.addOption(
                new RangeOption(
                        Component.translatable("sodium-extra.option.fog_type.cloud_end", fogTypeName(fogType)),
                        0, 300, 1,
                        val -> {
                            FogTypeConfig ftconfig = sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig());
                            ftconfig.cloudEndMultiplier = val;
                        },
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).cloudEndMultiplier
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.fog_type.cloud_end.tooltip"))
        );

        // all
        optionBlockBuilder.addOption(
                new SwitchOption(
                        fogTypeName(fogType),
                        (val) -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable = val,
                        () -> sodiumExtraOptions.renderSettings.fogTypeConfig.computeIfAbsent(fogType, k -> new FogTypeConfig()).enable
                ).setTooltip((v) -> fogTypeTooltip(fogType))
        );
    }
}
