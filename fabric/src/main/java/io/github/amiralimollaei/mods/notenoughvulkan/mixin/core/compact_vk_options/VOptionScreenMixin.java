package io.github.amiralimollaei.mods.notenoughvulkan.mixin.core.compact_vk_options;

import net.vulkanmod.config.gui.VOptionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = VOptionScreen.class, remap = false)
public class VOptionScreenMixin {
    @ModifyVariable(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/config/gui/VOptionScreen;buildLists(IIIII)V",
                    shift = At.Shift.BEFORE
            ),
            name = "itemHeight"
    )
    int notenoughvulkan$modifyListsHeight(int value) {
        return 18;
    }

    @ModifyArg(
            method = "buildPage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/vulkanmod/config/gui/VOptionScreen;addPageButtons(IIIIZ)V"
            ),
            index = 3
    )
    int notenoughvulkan$modifyPagesHeight(int height) {
        return 20;
    }
}
