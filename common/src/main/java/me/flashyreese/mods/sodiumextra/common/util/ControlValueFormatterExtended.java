package me.flashyreese.mods.sodiumextra.common.util;

import com.mojang.blaze3d.platform.Monitor;
import me.flashyreese.mods.sodiumextra.client.fog.FogDistanceHelper;
import io.github.amiralimollaei.mods.notenoughvulkan.config.vk.ValueFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public interface ControlValueFormatterExtended extends ValueFormatter {
    static ValueFormatter resolution() {
        return (v) -> {
            Monitor monitor = Minecraft.getInstance().getWindow().findBestMonitor();
            if (monitor == null || monitor.modeCount() <= 0) {
                return Component.translatable("options.fullscreen.unavailable");
            } else {
                int modeIndex = Math.clamp(v - 1, 0, monitor.modeCount() - 1);
                return v == 0 ? Component.translatable("options.fullscreen.current") : Component.literal(monitor.mode(modeIndex).toString().replace(" (24bit)", ""));
            }
        };
    }

    static ValueFormatter fogDistance() {
        return (v) -> {
            if (v == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
                return Component.translatable("options.gamma.default");
            } else if (FogDistanceHelper.disablesFog(v)) {
                return Component.translatable("options.off");
            } else {
                return Component.translatable("options.chunks", v);
            }
        };
    }

    static ValueFormatter protectedFogDistance() {
        return (v) -> {
            if (v == FogDistanceHelper.FOG_DISTANCE_VANILLA) {
                return Component.translatable("options.gamma.default");
            } else if (FogDistanceHelper.disablesFog(v)) {
                return Component.translatable("options.off");
            } else {
                return Component.translatable("sodium-extra.units.blocks", v);
            }
        };
    }

    static ValueFormatter ticks() {
        return (v) -> Component.translatable("sodium-extra.units.ticks", v);
    }
}
