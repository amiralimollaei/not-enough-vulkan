package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.monitor_selector;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector.FullscreenMonitorManager;
import me.flashyreese.mods.sodiumextra.client.gui.FullscreenResolutionConfirmation;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Arrays;

@Mixin(value = Options.class, remap = false)
public class OptionsMixin {
    @Unique
    private static boolean notEnoughVulkan$suppressMonitorChange;

    @Shadow
    @Final private static net.minecraft.client.Options mcOptions;

    @Shadow
    public static boolean fullscreenDirty;

    /// modified from [pull #618](https://github.com/xCollateral/VulkanMod/pull/618)
    @ModifyArg(
            method = "getVideoOpts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/config/gui/OptionBlock;<init>(Ljava/lang/String;[Lnet/vulkanmod/config/option/Option;)V",
                    ordinal = 0
            )
    )
    private static Option<?>[] notEnoughVulkan$addExtraVideoOptions(Option<?>[] options) {
        if (!FullscreenMonitorManager.shouldApplyMonitorSelectorPatch()) {
            return options;
        }
        return insertMonitorSelectorOption(options);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static Option<?>[] insertMonitorSelectorOption(Option<?>[] options) {
        int windowModeIndex = -1;
        CyclingOption<WindowMode> windowModeOption = null;
        CyclingOption<VideoModeSet> resolutionOption = null;
        CyclingOption<Integer> refreshRateOption = null;

        for (int index = 0; index < options.length; index++) {
            Option<?> option = options[index];
            if (!(option instanceof CyclingOption<?> cyclingOption)) {
                continue;
            }

            Object value = cyclingOption.getNewValue();
            if (value instanceof WindowMode) {
                windowModeIndex = index;
                windowModeOption = (CyclingOption<WindowMode>) cyclingOption;
            } else if (value instanceof VideoModeSet) {
                resolutionOption = (CyclingOption<VideoModeSet>) cyclingOption;
            } else if (resolutionOption != null && value instanceof Integer) {
                refreshRateOption = (CyclingOption<Integer>) cyclingOption;
            }
        }

        if (windowModeOption == null || resolutionOption == null || refreshRateOption == null) {
            return options;
        }

        CyclingOption<Long> monitorOption = createMonitorOption();
        bindMonitorOptionCallbacks(monitorOption, windowModeOption, resolutionOption, refreshRateOption);
        return ArrayUtils.add(options, windowModeIndex + 1, monitorOption);
    }

    @Unique
    private static CyclingOption<Long> createMonitorOption() {
        CyclingOption<Long> monitorOption = new CyclingOption<>(
                Component.translatable("not-enough-vulkan.option.monitor_selector"),
                FullscreenMonitorManager.getMonitors(),
                monitorHandle -> {
                    // The resolution confirmation is applied after this monitor option. Capture
                    // the old monitor while it is still available so Revert can restore the
                    // complete fullscreen target, not just its resolution.
                    FullscreenResolutionConfirmation.rememberCurrentFullscreenTarget();
                    FullscreenMonitorManager.setSelectedFullscreenMonitor(monitorHandle);
                    FullscreenMonitorManager.applySelectedFullscreenMonitor();

                    if (mcOptions.fullscreen().get()) fullscreenDirty = true;
                },
                () -> {
                    long monitor = FullscreenMonitorManager.getSelectedFullscreenMonitor();
                    if (monitor == 0L) {
                        monitor = VideoModeManager.selectedMonitor;
                    }
                    return monitor;
                }
        );
        monitorOption.setTooltip(ignored ->
                Component.translatable("not-enough-vulkan.option.monitor_selector.tooltip"));
        monitorOption.setTranslator(OptionsMixin::getMonitorName);
        monitorOption.setNewValue(FullscreenMonitorManager.getSelectedFullscreenMonitor());
        return monitorOption;
    }

    @Unique
    private static void bindMonitorOptionCallbacks(CyclingOption<Long> monitorOption,
                                                   CyclingOption<WindowMode> windowModeOption,
                                                   CyclingOption<VideoModeSet> resolutionOption,
                                                   CyclingOption<Integer> refreshRateOption) {
        monitorOption.setOnChange(() -> {
            if (notEnoughVulkan$suppressMonitorChange) {
                return;
            }

            updateResolutionOption(resolutionOption, monitorOption.getNewValue(),
                    resolutionOption.getNewValue());
        });

        monitorOption.setActivationFn(() ->
                windowModeOption.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN);
        windowModeOption.setOnChange(() -> {
            resolutionOption.updateActiveState();
            refreshRateOption.updateActiveState();
            monitorOption.updateActiveState();
            if (windowModeOption.getNewValue() != WindowMode.EXCLUSIVE_FULLSCREEN) {
                VideoModeSet preferredResolution = resolutionOption.getNewValue();
                notEnoughVulkan$suppressMonitorChange = true;
                try {
                    monitorOption.resetValue();
                } finally {
                    notEnoughVulkan$suppressMonitorChange = false;
                }

                updateResolutionOption(resolutionOption, monitorOption.getNewValue(),
                        preferredResolution);
            }
        });
    }

    @Unique
    private static void updateResolutionOption(CyclingOption<VideoModeSet> resolutionOption,
                                                long monitor,
                                                VideoModeSet preferredResolution) {
        VideoModeSet[] availableResolutions = VideoModeManager.monitorToVideoModeSets.get(monitor);
        if (availableResolutions == null || availableResolutions.length == 0) {
            availableResolutions = new VideoModeSet[]{VideoModeSet.getDummy()};
        }

        resolutionOption.setValues(availableResolutions);
        VideoModeSet selectedResolution = Arrays.stream(availableResolutions)
                .filter(resolution -> resolution.equals(preferredResolution))
                .findFirst()
                .orElse(availableResolutions[availableResolutions.length - 1]);
        resolutionOption.setNewValue(selectedResolution);
    }

    @Unique
    private static Component getMonitorName(Long monitor) {
        String name = FullscreenMonitorManager.getMonitorName(monitor);
        return name == null ? Component.literal("Unknown Monitor") : Component.literal(name);
    }
}
