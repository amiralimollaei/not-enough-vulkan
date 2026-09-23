package io.github.amiralimollaei.mods.notenoughvulkan.mixin.compat.vulkanmod;

import me.flashyreese.mods.sodiumextra.client.gui.FullscreenResolutionConfirmation;
import net.vulkanmod.config.option.CyclingOption;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.Options;
import net.vulkanmod.config.video.VideoModeManager;
import net.vulkanmod.config.video.VideoModeSet;
import net.vulkanmod.config.video.WindowMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Observes VulkanMod's resolution CyclingOption at its existing apply callback.
 * The option and VulkanMod's callback remain responsible for choosing and applying the mode.
 */
@Mixin(value = Options.class, remap = false)
public class MixinFullscreenResolutionConfirmation {
    @Inject(method = "getVideoOpts", at = @At("RETURN"), remap = false)
    private static void notEnoughVulkan$gateFullscreenResolutionControls(CallbackInfoReturnable<OptionBlock[]> cir) {
        Option<WindowMode> windowMode = null;
        Option<?> resolution = null;
        Option<?> refreshRate = null;

        for (Option<?>[] options : java.util.Arrays.stream(cir.getReturnValue()).map(OptionBlock::options).toList()) {
            for (int index = 0; index < options.length; index++) {
                Option<?> option = options[index];
                Object value = option.getNewValue();
                if (value instanceof WindowMode) {
                    @SuppressWarnings("unchecked")
                    Option<WindowMode> typedWindowMode = (Option<WindowMode>) option;
                    windowMode = typedWindowMode;
                }
                if (value instanceof VideoModeSet) {
                    resolution = option;
                    if (index + 1 < options.length && options[index + 1].getNewValue() instanceof Integer) {
                        refreshRate = options[index + 1];
                    }
                }
            }
        }

        if (windowMode != null && resolution != null && refreshRate != null) {
            FullscreenResolutionConfirmation.gateFullscreenResolutionControls(windowMode, resolution, refreshRate);
        }
    }

    @Inject(
            method = "lambda$getVideoOpts$7",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/vulkanmod/config/video/VideoModeManager;selectedVideoMode:Lnet/vulkanmod/config/video/VideoModeSet$VideoMode;",
                    opcode = org.objectweb.asm.Opcodes.PUTSTATIC,
                    remap = false
            ),
            remap = false
    )
    private static void notEnoughVulkan$requestFullscreenResolutionConfirmation(
            CyclingOption<Integer> refreshRateOption,
            VideoModeSet value,
            CallbackInfo ci
    ) {
        Integer refreshRate = refreshRateOption.getNewValue();
        if (refreshRate != null) {
            FullscreenResolutionConfirmation.request(
                    VideoModeManager.selectedVideoMode,
                    value.getVideoMode(refreshRate)
            );
        }
    }

    @Inject(
            method = "lambda$getVideoOpts$3",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/vulkanmod/config/video/VideoModeSet$VideoMode;refreshRate:I",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD,
                    remap = false
            ),
            remap = false
    )
    private static void notEnoughVulkan$requestRefreshRateConfirmation(Integer refreshRate, CallbackInfo ci) {
        VideoModeSet.VideoMode currentMode = VideoModeManager.selectedVideoMode;
        if (currentMode == null || refreshRate == null) {
            return;
        }

        FullscreenResolutionConfirmation.request(
                currentMode,
                new VideoModeSet.VideoMode(currentMode.width, currentMode.height, currentMode.bitDepth, refreshRate)
        );
    }
}
