package io.github.amiralimollaei.mods.notenoughvulkan.config;

import io.github.amiralimollaei.mods.notenoughvulkan.client.NotEnoughVulkanClientMod;
import me.flashyreese.mods.sodiumextra.client.SodiumExtraClientMod;
import me.flashyreese.mods.sodiumextra.client.config.SodiumExtraGameOptions;
import net.minecraft.client.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.material.FogType;
import net.vulkanmod.config.Platform;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static me.flashyreese.mods.sodiumextra.client.config.SodiumExtraConfigUtils.*;

public abstract class Options {
    static SodiumExtraGameOptions sodiumExtraOptions = SodiumExtraClientMod.options();
    static NotEnoughVulkanGameOptions notEnoughVulkanOptions = NotEnoughVulkanClientMod.options();
    static Minecraft minecraft = Minecraft.getInstance();

    public static OptionBlock[] getAnimationsOpts() {
        return new OptionBlock[]{
                new OptionBlock("", new Option[]{
                        new SwitchOption(parseVanillaString("gui.socialInteractions.tab_all"),
                                value -> sodiumExtraOptions.animationSettings.animation = value,
                                () -> sodiumExtraOptions.animationSettings.animation)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animations_all.tooltip")),
                        new SwitchOption(parseVanillaString("block.minecraft.water"),
                                value -> sodiumExtraOptions.animationSettings.water = value,
                                () -> sodiumExtraOptions.animationSettings.water)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animate_water.tooltip")),
                        new SwitchOption(parseVanillaString("block.minecraft.lava"),
                                value -> sodiumExtraOptions.animationSettings.lava = value,
                                () -> sodiumExtraOptions.animationSettings.lava)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animate_lava.tooltip")),
                        new SwitchOption(parseVanillaString("block.minecraft.fire"),
                                value -> sodiumExtraOptions.animationSettings.fire = value,
                                () -> sodiumExtraOptions.animationSettings.fire)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animate_fire.tooltip")),
                        new SwitchOption(parseVanillaString("block.minecraft.nether_portal"),
                                value -> sodiumExtraOptions.animationSettings.portal = value,
                                () -> sodiumExtraOptions.animationSettings.portal)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animate_portal.tooltip")),
                        new SwitchOption(parseVanillaString("sodium-extra.option.block_animations"),
                                value -> sodiumExtraOptions.animationSettings.blockAnimations = value,
                                () -> sodiumExtraOptions.animationSettings.blockAnimations)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.block_animations.tooltip")),
                        new SwitchOption(parseVanillaString("block.minecraft.sculk_sensor"),
                                value -> sodiumExtraOptions.animationSettings.sculkSensor = value,
                                () -> sodiumExtraOptions.animationSettings.sculkSensor)
                                .setTooltip((v) -> Component.translatable("sodium-extra.option.animate_sculk_sensor.tooltip")),
                })
        };
    }

    public static OptionBlock[] getParticlesOpts() {
        OptionBlock otherParticlesGroup = new OptionBlock(
                "", new Option[]{
                new SwitchOption(parseVanillaString("gui.socialInteractions.tab_all"),
                        value -> sodiumExtraOptions.particleSettings.particles = value,
                        () -> sodiumExtraOptions.particleSettings.particles)
                        .setTooltip((v) -> Component.translatable("sodium-extra.option.particles_all.tooltip"))
                }
        );

        OptionBlock generalParticlesGroup = new OptionBlock(
                "General", new Option[]{
                new SwitchOption(parseVanillaString("subtitles.entity.generic.splash"),
                        value -> sodiumExtraOptions.particleSettings.rainSplash = value,
                        () -> sodiumExtraOptions.particleSettings.rainSplash)
                        .setTooltip((v) -> Component.translatable("sodium-extra.option.rain_splash.tooltip")),
                new SwitchOption(parseVanillaString("subtitles.block.generic.break"),
                        value -> sodiumExtraOptions.particleSettings.blockBreak = value,
                        () -> sodiumExtraOptions.particleSettings.blockBreak)
                        .setTooltip((v) -> Component.translatable("sodium-extra.option.block_break.tooltip")),
                new SwitchOption(parseVanillaString("subtitles.block.generic.hit"),
                value -> sodiumExtraOptions.particleSettings.blockBreaking = value,
                        () -> sodiumExtraOptions.particleSettings.blockBreaking)
                        .setTooltip((v) -> Component.translatable("sodium-extra.option.block_breaking.tooltip")),
            }
        );

        List<SwitchOption> options = new ArrayList<>();
        BuiltInRegistries.PARTICLE_TYPE.keySet().stream()
                .sorted((a, b) -> translatableName(a, "particles")
                        .getString()
                        .compareToIgnoreCase(translatableName(b, "particles").getString()))
                .forEach(id -> options.add(newParticleSwitchOption(id)));
        Option<?>[] optionsArray = new Option<?>[options.size()];
        optionsArray = options.toArray(optionsArray);
        OptionBlock allParticlesGroup = new OptionBlock(
                "Individual", optionsArray
        );

        return new OptionBlock[]{
                otherParticlesGroup,
                generalParticlesGroup,
                allParticlesGroup
        };
    }

    private static SwitchOption newParticleSwitchOption(ResourceLocation id) {
        SwitchOption switchOption = new SwitchOption(
                translatableName(id, "particles"),
                value -> sodiumExtraOptions.particleSettings.otherMap.put(id, value),
                () -> sodiumExtraOptions.particleSettings.otherMap.computeIfAbsent(id, k -> true)
        );
        switchOption.setTooltip((v) -> translatableTooltip(id, "particles"));
        return switchOption;
    }

    private static SwitchOption newStandardSwitchOption(String key, Consumer<Boolean> setter, Supplier<Boolean> getter) {
        SwitchOption option = new SwitchOption(Component.translatable("sodium-extra.option."+key), setter, getter);
        option.setTooltip((v) -> Component.translatable("sodium-extra.option."+key+".tooltip"));
        return option;
    }

    private static RangeOption newStandardRangeOption(String key, int min, int max, int step, Consumer<Integer> setter, Supplier<Integer> getter) {
        RangeOption option = new RangeOption(
                Component.translatable("sodium-extra.option."+key),
                min,
                max,
                step,
                setter,
                getter
        );
        option.setTooltip((v) -> Component.translatable("sodium-extra.option."+key+".tooltip"));
        return option;
    }

    private static RangeOption newStandardRangeOption(Component name, Component tooltip, int min, int max, int step, Consumer<Integer> setter, Supplier<Integer> getter) {
        RangeOption option = new RangeOption(
                name,
                min,
                max,
                step,
                setter,
                getter
        );
        option.setTooltip((v) -> tooltip);
        return option;
    }

    public static OptionBlock[] getDetailsOpts() {
        return new OptionBlock[]{
                new OptionBlock("", new Option[]{
                        newStandardSwitchOption("sky",
                                value -> sodiumExtraOptions.detailSettings.sky = value,
                                () -> sodiumExtraOptions.detailSettings.sky
                        ),
                        newStandardSwitchOption("stars",
                                value -> sodiumExtraOptions.detailSettings.stars = value,
                                () -> sodiumExtraOptions.detailSettings.stars
                        ),
                        newStandardSwitchOption("sun",
                                value -> sodiumExtraOptions.detailSettings.sun = value,
                                () -> sodiumExtraOptions.detailSettings.sun
                        ),
                        newStandardSwitchOption("moon",
                                value -> sodiumExtraOptions.detailSettings.moon = value,
                                () -> sodiumExtraOptions.detailSettings.moon
                        ),
                        new SwitchOption(parseVanillaString("soundCategory.weather"),
                                value -> sodiumExtraOptions.detailSettings.rainSnow = value,
                                () -> sodiumExtraOptions.detailSettings.rainSnow
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.rain_snow.tooltip")),
                        newStandardSwitchOption("biome_colors",
                                value -> sodiumExtraOptions.detailSettings.biomeColors = value,
                                () -> sodiumExtraOptions.detailSettings.biomeColors
                        ),
                        newStandardSwitchOption("sky_colors",
                                value -> sodiumExtraOptions.detailSettings.skyColors = value,
                                () -> sodiumExtraOptions.detailSettings.skyColors
                        ),
                })
        };
    }

    public static OptionBlock[] getRenderOpts() {
        List<OptionBlock> optionBlocks = new ArrayList<>();

        var multiDimensionFogOption = newStandardSwitchOption(
                "multi_dimension_fog",
                value -> sodiumExtraOptions.renderSettings.multiDimensionFogControl = value,
                () -> sodiumExtraOptions.renderSettings.multiDimensionFogControl
        );

        var fogStartOption = newStandardRangeOption(
                "fog_start",
                0, 100, 1,
                value -> sodiumExtraOptions.renderSettings.fogStart = value,
                () -> sodiumExtraOptions.renderSettings.fogStart
        );

        var singleFogOption = newStandardRangeOption(
                "single_fog",
                0, 32, 1,
                value -> sodiumExtraOptions.renderSettings.fogDistance = value,
                () -> sodiumExtraOptions.renderSettings.fogDistance
        );
        singleFogOption.setActivationFn(() -> !multiDimensionFogOption.getNewValue());

        optionBlocks.add(new OptionBlock("", new Option[]{
                multiDimensionFogOption,
                fogStartOption
        }));

        optionBlocks.add(new OptionBlock("", new Option[]{
                singleFogOption
        }));

        WorldDimensions.keysInOrder(Stream.empty())
                .filter(dim -> !sodiumExtraOptions.renderSettings.dimensionFogDistanceMap.containsKey(dim.location()))
                .forEach(dim -> sodiumExtraOptions.renderSettings.dimensionFogDistanceMap.put(dim.location(), 0));

        List<RangeOption> dimensionFogOptions = new ArrayList<>();
        sodiumExtraOptions.renderSettings.dimensionFogDistanceMap.keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(identifier -> {
                    var dimensionFogOption = newStandardRangeOption(
                            Component.translatable("sodium-extra.option.fog", translatableName(identifier, "dimensions")),
                            Component.translatable("sodium-extra.option.fog.tooltip"),
                            0, 32, 1,
                            value -> SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.put(identifier, value),
                            () -> SodiumExtraClientMod.options().renderSettings.dimensionFogDistanceMap.getOrDefault(identifier, 0)
                    );
                    dimensionFogOption.setActivationFn(multiDimensionFogOption::getNewValue);
                    dimensionFogOptions.add(dimensionFogOption);
                });
        RangeOption[] dimensionFogOptionsArray = new RangeOption[dimensionFogOptions.size()];
        dimensionFogOptionsArray = dimensionFogOptions.toArray(dimensionFogOptionsArray);

        multiDimensionFogOption.setOnChange(() -> {
            singleFogOption.updateActiveState();
            singleFogOption.resetValue();
            for (var option: dimensionFogOptions) {
                option.updateActiveState();
                option.resetValue();
            }
        });

        optionBlocks.add(new OptionBlock("", dimensionFogOptionsArray));

        optionBlocks.add(new OptionBlock("Light Updates", new Option[]{
                newStandardSwitchOption("light_updates",
                        value -> sodiumExtraOptions.renderSettings.lightUpdates = value,
                        () -> sodiumExtraOptions.renderSettings.lightUpdates
                )
        }));

        optionBlocks.add(new OptionBlock("Render", new Option[]{
                new SwitchOption(
                        parseVanillaString("entity.minecraft.item_frame"),
                        value -> sodiumExtraOptions.renderSettings.itemFrame = value,
                        () -> sodiumExtraOptions.renderSettings.itemFrame
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.item_frames.tooltip")),
                new SwitchOption(
                        parseVanillaString("entity.minecraft.armor_stand"),
                        value -> sodiumExtraOptions.renderSettings.armorStand = value,
                        () -> sodiumExtraOptions.renderSettings.armorStand
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.armor_stands.tooltip")),
                new SwitchOption(
                        parseVanillaString("entity.minecraft.painting"),
                        value -> sodiumExtraOptions.renderSettings.painting = value,
                        () -> sodiumExtraOptions.renderSettings.painting
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.paintings.tooltip"))
        }));

        optionBlocks.add(new OptionBlock("Block Render", new Option[]{
                new SwitchOption(
                        Component.translatable("sodium-extra.option.beacon_beam"),
                        value -> sodiumExtraOptions.renderSettings.beaconBeam = value,
                        () -> sodiumExtraOptions.renderSettings.beaconBeam
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.beacon_beam.tooltip")),
                new SwitchOption(
                        Component.translatable("sodium-extra.option.limit_beacon_beam_height"),
                        value -> sodiumExtraOptions.renderSettings.limitBeaconBeamHeight = value,
                        () -> sodiumExtraOptions.renderSettings.limitBeaconBeamHeight
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.limit_beacon_beam_height.tooltip")),
                new SwitchOption(
                        Component.translatable("sodium-extra.option.enchanting_table_book"),
                        value -> sodiumExtraOptions.renderSettings.enchantingTableBook = value,
                        () -> sodiumExtraOptions.renderSettings.enchantingTableBook
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.enchanting_table_book.tooltip")),
                new SwitchOption(
                        parseVanillaString("block.minecraft.piston"),
                        value -> sodiumExtraOptions.renderSettings.piston = value,
                        () -> sodiumExtraOptions.renderSettings.piston
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.piston.tooltip"))
        }));

        optionBlocks.add(new OptionBlock("Name Tags", new Option[]{
                new SwitchOption(
                        Component.translatable("sodium-extra.option.item_frame_name_tag"),
                        value -> sodiumExtraOptions.renderSettings.itemFrameNameTag = value,
                        () -> sodiumExtraOptions.renderSettings.itemFrameNameTag
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.item_frame_name_tag.tooltip")),
                new SwitchOption(
                        Component.translatable("sodium-extra.option.player_name_tag"),
                        value -> sodiumExtraOptions.renderSettings.playerNameTag = value,
                        () -> sodiumExtraOptions.renderSettings.playerNameTag
                ).setTooltip((v) -> Component.translatable("sodium-extra.option.player_name_tag.tooltip")),
        }));

        OptionBlock[] optionBlockArray = new OptionBlock[optionBlocks.size()];
        optionBlockArray = optionBlocks.toArray(optionBlockArray);

        return optionBlockArray;
    }

    public static OptionBlock[] getExtrasOpts() {
        SwitchOption reduceResolutionOnMac = new SwitchOption(
                Component.translatable("sodium-extra.option.reduce_resolution_on_mac"),
                (value) -> sodiumExtraOptions.extraSettings.reduceResolutionOnMac = value,
                () -> sodiumExtraOptions.extraSettings.reduceResolutionOnMac
        );
        reduceResolutionOnMac.setTooltip((v) -> Component.translatable("sodium-extra.option.reduce_resolution_on_mac.tooltip"));
        reduceResolutionOnMac.setActivationFn(
                () -> SodiumExtraClientMod.mixinConfig().getOptions().get("mixin.reduce_resolution_on_mac").isEnabled() && Platform.isMacOS()
        );
        SwitchOption skipWaylandPatches = new SwitchOption(
                Component.translatable("not-enough-vulkan.option.skip_wayland_patches"),
                (value) -> notEnoughVulkanOptions.compatSettings.skipWaylandPatches = value,
                () -> notEnoughVulkanOptions.compatSettings.skipWaylandPatches
        );
        skipWaylandPatches.setTooltip((v) -> Component.translatable("not-enough-vulkan.option.skip_wayland_patches.tooltip"));
        skipWaylandPatches.setActivationFn(
                () -> NotEnoughVulkanClientMod.mixinConfig().getOptions().get("mixin.compat.skip_wayland_patches").isEnabled() && Platform.isWayLand()
        );

        return new OptionBlock[]{
                new OptionBlock("Compatibility", new Option[]{
                        reduceResolutionOnMac, skipWaylandPatches
                }),
                new OptionBlock("Overlay", new Option[]{
                        new CyclingOption<>(
                                Component.translatable("sodium-extra.option.overlay_corner"),
                                SodiumExtraGameOptions.OverlayCorner.values(),
                                (value) -> sodiumExtraOptions.extraSettings.overlayCorner = value,
                                () -> sodiumExtraOptions.extraSettings.overlayCorner
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.overlay_corner.tooltip"))
                        .setTranslator(SodiumExtraGameOptions.OverlayCorner::getLocalizedName),
                        new CyclingOption<>(
                                Component.translatable("sodium-extra.option.text_contrast"),
                                SodiumExtraGameOptions.TextContrast.values(),
                                (value) -> sodiumExtraOptions.extraSettings.textContrast = value,
                                () -> sodiumExtraOptions.extraSettings.textContrast
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.text_contrast.tooltip"))
                        .setTranslator(SodiumExtraGameOptions.TextContrast::getLocalizedName),
                        new SwitchOption(Component.translatable("sodium-extra.option.show_fps"),
                                (value) -> sodiumExtraOptions.extraSettings.showFps = value,
                                () -> sodiumExtraOptions.extraSettings.showFps
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_fps.tooltip")),
                        new SwitchOption(Component.translatable("sodium-extra.option.show_fps_extended"),
                                (value) -> sodiumExtraOptions.extraSettings.showFPSExtended = value,
                                () -> sodiumExtraOptions.extraSettings.showFPSExtended
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_coordinates.tooltip")),
                        new SwitchOption(Component.translatable("sodium-extra.option.show_coordinates"),
                                (value) -> sodiumExtraOptions.extraSettings.showCoords = value,
                                () -> sodiumExtraOptions.extraSettings.showCoords
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.show_coordinates.tooltip")),
                        new RangeOption(Component.translatable("sodium-extra.option.cloud_height"),
                                -64, 319, 1,
                                (value) -> sodiumExtraOptions.extraSettings.cloudHeight = value,
                                () -> sodiumExtraOptions.extraSettings.cloudHeight
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.cloud_height.tooltip"))
                }),
                new OptionBlock("Advanced Item Tooltips", new Option[]{
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.advanced_item_tooltips"),
                                (value) -> {
                                    Minecraft.getInstance().options.advancedItemTooltips = value;
                                    Minecraft.getInstance().options.save();
                                },
                                () -> Minecraft.getInstance().options.advancedItemTooltips
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.advanced_item_tooltips.tooltip"))
                }),
                new OptionBlock("Toasts", new Option[]{
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.toasts"),
                                (value) -> sodiumExtraOptions.extraSettings.toasts = value,
                                () -> sodiumExtraOptions.extraSettings.toasts
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.toasts.tooltip")),
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.advancement_toast"),
                                (value) -> sodiumExtraOptions.extraSettings.advancementToast = value,
                                () -> sodiumExtraOptions.extraSettings.advancementToast
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.advancement_toast.tooltip")),
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.recipe_toast"),
                                (value) -> sodiumExtraOptions.extraSettings.recipeToast = value,
                                () -> sodiumExtraOptions.extraSettings.recipeToast
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.recipe_toast.tooltip")),
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.system_toast"),
                                (value) -> sodiumExtraOptions.extraSettings.systemToast = value,
                                () -> sodiumExtraOptions.extraSettings.systemToast
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.system_toast.tooltip")),
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.tutorial_toast"),
                                (value) -> sodiumExtraOptions.extraSettings.tutorialToast = value,
                                () -> sodiumExtraOptions.extraSettings.tutorialToast
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.tutorial_toast.tooltip"))
                }),
                new OptionBlock("Other", new Option[]{
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.instant_sneak"),
                                (value) -> sodiumExtraOptions.extraSettings.instantSneak = value,
                                () -> sodiumExtraOptions.extraSettings.instantSneak
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.instant_sneak.tooltip")),
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.prevent_shaders"),
                                (value) -> {
                                    sodiumExtraOptions.extraSettings.preventShaders = value;
                                    minecraft.levelRenderer.allChanged();
                                },
                                () -> sodiumExtraOptions.extraSettings.preventShaders
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.prevent_shaders.tooltip"))
                }),
                new OptionBlock("Debug Hud", new Option[]{
                        new SwitchOption(
                                Component.translatable("sodium-extra.option.steady_debug_hud"),
                                (value) -> sodiumExtraOptions.extraSettings.steadyDebugHud = value,
                                () -> sodiumExtraOptions.extraSettings.steadyDebugHud
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.steady_debug_hud.tooltip")),
                        new RangeOption(
                                Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval"),
                                1, 20, 1,
                                (value) -> sodiumExtraOptions.extraSettings.steadyDebugHudRefreshInterval = value,
                                () -> sodiumExtraOptions.extraSettings.steadyDebugHudRefreshInterval
                        ).setTooltip((v) -> Component.translatable("sodium-extra.option.steady_debug_hud_refresh_interval.tooltip"))
                }),
        };
    }

    private static boolean supportsWayland() {
        String sessionType = System.getenv("XDG_SESSION_TYPE");
        if (sessionType == null) return false;
        return sessionType.equalsIgnoreCase("wayland");
    }


    public static List<OptionPage> getModOptions() {
        List<OptionPage> optionPages = new ArrayList<>();
        optionPages.add(
                new OptionPage(Component.translatable("sodium-extra.option.animations").getString(), getAnimationsOpts())
        );
        optionPages.add(
                new OptionPage(parseVanillaString("options.particles").getString(), getParticlesOpts())
        );
        optionPages.add(
                new OptionPage(Component.translatable("sodium-extra.option.details").getString(), getDetailsOpts())
        );
        optionPages.add(
                new OptionPage(Component.translatable("sodium-extra.option.render").getString(), getRenderOpts())
        );
        optionPages.add(
                new OptionPage(Component.translatable("sodium-extra.option.extras").getString(), getExtrasOpts())
        );
        return optionPages;
    }
}
