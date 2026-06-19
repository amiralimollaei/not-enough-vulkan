package io.github.amiralimollaei.mods.notenoughvulkan.mixin.core.enhance_vk_options;

import io.github.amiralimollaei.mods.notenoughvulkan.client.ModNameVersionRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.vulkanmod.config.gui.render.GuiRenderer;
import net.vulkanmod.config.gui.widget.ModIconWidget;
import net.vulkanmod.config.gui.widget.VAbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.awt.*;

@Mixin(ModIconWidget.class)
public class ModIconWidgetMixin extends VAbstractWidget {
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/vulkanmod/config/gui/render/GuiRenderer;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V")
    )
    void notenoughvuklan$redirectTextRender(Font font, Component name, int x, int y, int color){
        int centerX = x + (getWidth() - 36) / 2 - 2;
        int centerY1 = y - (getHeight() / 2) + font.lineHeight;
        int centerY2 = centerY1 + font.lineHeight + 2;

        GuiRenderer.drawScrollingString(font, name, centerX, centerY1, getWidth() - 36, 0xffffffff);

        MutableComponent versionComponent = Component.empty();
        for (var entry : ModNameVersionRegistry.modNameToId.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name.getString())) {
                versionComponent = ModNameVersionRegistry.getVersionComponent(entry.getValue());
            }
        }

        TextColor nameColor = name.getStyle().getColor();
        if (nameColor == null){
            nameColor = TextColor.fromRgb(0xffffffff);
        }

        int versionColor = (new Color(nameColor.getValue())).darker().getRGB();

        GuiRenderer.drawScrollingString(font, versionComponent.withColor(versionColor), centerX, centerY2, getWidth() - 36, 0xffffffff);
    }
}
