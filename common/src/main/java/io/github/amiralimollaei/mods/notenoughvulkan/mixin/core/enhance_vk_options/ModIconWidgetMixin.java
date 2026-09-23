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
            at = @At(value = "INVOKE", target = "Lnet/vulkanmod/config/gui/render/GuiRenderer;drawScrollingString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIII)V"),
            remap = false
    )
    void notEnoughVulkan$renderNameAndVersion(Font font, Component name, int x, int y,
                                               int width, int color) {
        MutableComponent versionComponent = ModNameVersionRegistry.findVersion(name);

        if (versionComponent == null) {
            // VulkanMod already passes the center and clipping width for the text area.
            GuiRenderer.drawScrollingString(font, name, x, y, width, color);
            return;
        }

        // ModIconWidget is only 28px high. Center both lines as a unit so the version
        // remains inside the widget instead of being drawn below its bottom edge.
        int lineSpacing = 1;
        int textHeight = font.lineHeight * 2 + lineSpacing;
        int nameY = getY() + Math.max(0, (getHeight() - textHeight) / 2);
        GuiRenderer.drawScrollingString(font, name, x, nameY, width, color);

        TextColor nameColor = name.getStyle().getColor();
        if (nameColor == null) {
            nameColor = TextColor.fromRgb(0xffffffff);
        }

        int versionColor = (new Color(nameColor.getValue())).darker().getRGB();

        GuiRenderer.drawScrollingString(font, versionComponent.withColor(versionColor),
                x, nameY + font.lineHeight + lineSpacing, width, color);
    }
}
