package io.github.amiralimollaei.mods.notenoughvulkan.mixin.core.compact_vk_options;

import net.vulkanmod.config.gui.VOptionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VOptionList.class, remap = false)
public class VOptionListMixin {
    @Shadow
    int itemMargin;

    @Shadow
    int totalItemHeight;

    @Shadow
    int itemHeight;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    void notenoughvulkan$modifyListsMargin(int x, int y, int width, int height, int itemHeight, CallbackInfo ci) {
        //this.itemMargin = 1;
        //this.totalItemHeight = this.itemHeight + this.itemMargin;
    }
}
