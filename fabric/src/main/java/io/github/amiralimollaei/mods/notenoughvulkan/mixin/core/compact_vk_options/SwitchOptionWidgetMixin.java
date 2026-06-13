package io.github.amiralimollaei.mods.notenoughvulkan.mixin.core.compact_vk_options;

import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.widget.OptionWidget;
import net.vulkanmod.config.gui.widget.SwitchOptionWidget;
import net.vulkanmod.config.option.SwitchOption;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SwitchOptionWidget.class, remap = false)
public abstract class SwitchOptionWidgetMixin extends OptionWidget<SwitchOption> {
    public SwitchOptionWidgetMixin(SwitchOption option, Component name) {
        super(option, name);
    }

    @ModifyVariable(
            method = "renderControls",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/vulkanmod/config/gui/widget/SwitchOptionWidget;option:Lnet/vulkanmod/config/option/Option;",
                    shift = At.Shift.BEFORE,
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            ),
            name = "y0"
    )
    int notenoughvulkan$modifySwitchOptionTopMargin(int y0){
        return this.y + 3;
    }

    @ModifyVariable(
            method = "renderControls",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/vulkanmod/config/gui/widget/SwitchOptionWidget;option:Lnet/vulkanmod/config/option/Option;",
                    shift = At.Shift.BEFORE,
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            ),
            name = "height"
    )
    int notenoughvulkan$modifySwitchOptionHeight(int height){
        return this.height - 6;
    }

    @ModifyVariable(
            method = "renderControls",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/vulkanmod/config/gui/widget/SwitchOptionWidget;option:Lnet/vulkanmod/config/option/Option;",
                    shift = At.Shift.BEFORE,
                    opcode = Opcodes.GETFIELD,
                    ordinal = 0
            ),
            name = "h1"
    )
    int notenoughvulkan$modifySwitchOptionBottomMargin(int height){
        return this.height - 10;
    }
}
