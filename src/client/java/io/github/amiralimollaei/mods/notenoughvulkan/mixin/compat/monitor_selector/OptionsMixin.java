package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.monitor_selector;

import io.github.amiralimollaei.mods.notenoughvulkan.compat.monitor_selector.FullscreenMonitorManager;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

import static org.lwjgl.glfw.GLFW.glfwGetMonitorName;

@Mixin(value = Options.class, remap = false)
public class OptionsMixin {
    @Shadow
    public static net.minecraft.client.Options mcOptions;

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
    private static Option<?>[] notenoughvuklan$addExtraVideoOpts(Option<?>[] options) {
        if (!FullscreenMonitorManager.shouldApplyMonitorSelectorPatch()) return options;

        return insertMonitorSelectorOption(options);
    }

    @Unique
    private static Option<?> @NonNull [] insertMonitorSelectorOption(Option<?>[] options) {
        CyclingOption<Long> monitorOption = new CyclingOption<Long>(
                Component.translatable("not-enough-vulkan.option.monitor_selector"),
                FullscreenMonitorManager.getMonitors(),
                (monitorHandle) -> {
                    FullscreenMonitorManager.setSelectedFullscreenMonitor(monitorHandle);
                    FullscreenMonitorManager.applySelectedFullscreenMonitor();

                    if (mcOptions.fullscreen().get()) fullscreenDirty = true;
                },
                () -> {
                    long monitor = FullscreenMonitorManager.getSelectedFullscreenMonitor();
                    if (monitor == -1) {
                        monitor = VideoModeManager.selectedMonitor;
                    }
                    return monitor;
                }
        );
        monitorOption.setTooltip((v) -> Component.translatable("not-enough-vulkan.option.monitor_selector.tooltip"));
        monitorOption.setTranslator(monitor_address -> Component.nullToEmpty(glfwGetMonitorName(monitor_address)));
        monitorOption.setNewValue(VideoModeManager.selectedMonitor);

        // update resolution Option when the screen changes
        CyclingOption<VideoModeSet> resolutionOption = (CyclingOption<VideoModeSet>) options[1];
        CyclingOption<Integer> refreshRateOption = (CyclingOption<Integer>) options[2];
        monitorOption.setOnChange(() -> {
            var newVideoResolutions = VideoModeManager.monitorToVideoModeSets.get(monitorOption.getNewValue());
            resolutionOption.setValues(newVideoResolutions);
            resolutionOption.setNewValue(newVideoResolutions != null ? newVideoResolutions[newVideoResolutions.length - 1] : VideoModeSet.getDummy());
        });
        // monitorOption should only be active when window mode is fullscreen
        CyclingOption<WindowMode> windowModeOption = (CyclingOption<WindowMode>) options[0];
        monitorOption.setActivationFn(() -> windowModeOption.getNewValue() == WindowMode.EXCLUSIVE_FULLSCREEN);
        windowModeOption.setOnChange(() -> {
            resolutionOption.updateActiveState();
            refreshRateOption.updateActiveState();
            monitorOption.updateActiveState();
            // reset monitorOption when disabled
            if (windowModeOption.getNewValue() != WindowMode.EXCLUSIVE_FULLSCREEN) monitorOption.resetValue();
        });
        return ArrayUtils.add(options, 1, monitorOption);
    }
}