package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod.option;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.OptionWidget;

final class ActionOptionWidget extends OptionWidget<ActionOption> {
    private final ActionOption actionOption;
    private boolean focused;

    ActionOptionWidget(ActionOption option) {
        super(option, option.getName());
        this.actionOption = option;
        this.displayedValue = Component.literal(">");
    }

    @Override
    protected void renderControls(double mouseX, double mouseY) {
        int centerX = this.controlX + this.controlWidth / 2;
        int centerY = this.y + (this.height - 8) / 2;
        int color = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        GuiRenderer.drawCenteredString(Minecraft.getInstance().font, this.displayedValue, centerX, centerY, color);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.actionOption.run();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double deltaX, double deltaY) {
    }

    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }
}
