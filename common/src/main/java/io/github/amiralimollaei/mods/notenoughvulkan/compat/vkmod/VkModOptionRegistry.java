package io.github.amiralimollaei.mods.notenoughvulkan.compat.vkmod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.vulkanmod.config.gui.OptionBlock;
import net.vulkanmod.config.option.Option;
import net.vulkanmod.config.option.OptionPage;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VkModOptionRegistry {
    private static final Map<Identifier, String> TRANSLATION_KEYS = Map.of(
            Identifier.parse("sodium:general.render_distance"), "options.renderDistance"
    );
    private static final Map<Identifier, Option<?>> OPTIONS = new HashMap<>();

    private VkModOptionRegistry() {
    }

    public static void mapVideoSettings(List<OptionPage> pages) {
        OPTIONS.clear();
        if (pages == null) return;
        for (OptionPage page : pages) {
            for (OptionBlock block : page.optionBlocks) {
                for (Option<?> option : block.options()) {
                    Identifier id = getOptionId(option);
                    if (id != null) {
                        OPTIONS.put(id, option);
                    }
                }
            }
        }
    }

    private static @Nullable Identifier getOptionId(Option<?> option) {
        String name = option.getName().getString();
        for (Map.Entry<Identifier, String> entry : TRANSLATION_KEYS.entrySet()) {
            String key = entry.getValue();
            String translatedKey = Component.translatable(key).getString();
            if (name.equals(translatedKey) || name.startsWith(translatedKey + ": ")) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static @Nullable Option<?> getOption(Identifier id) {
        return OPTIONS.get(id);
    }
}
