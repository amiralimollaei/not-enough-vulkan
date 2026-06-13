package me.flashyreese.mods.sodiumextra.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FogType;

import java.util.*;
import java.util.stream.Collectors;

public class SodiumExtraConfigUtils {
    public static Identifier id(String path) {
        return Identifier.parse("sodium-extra:" + path);
    }

    public static Component parseVanillaString(String key) {
        // Strip formatting codes like "§a"
        return Component.literal(Component.translatable(key).getString().replaceAll("§.", ""));
    }

    public static Component fogTypeName(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase();
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            String pretty = Arrays.stream(type.name().split("_"))
                    .map(s -> s.charAt(0) + s.substring(1).toLowerCase())
                    .collect(Collectors.joining(" ")) + " Fog";
            return Component.literal(pretty);
        }
        return translated;
    }

    public static Component fogTypeTooltip(FogType type) {
        String key = "sodium-extra.option.fog_type." + type.name().toLowerCase() + ".tooltip";
        Component translated = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translated)) {
            return Component.translatable("sodium-extra.option.fog_type.default.tooltip", fogTypeName(type));
        }
        return translated;
    }

    public static Component translatableName(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category));
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.literal(
                    Arrays.stream(key.substring(key.lastIndexOf('.') + 1).split("_"))
                            .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                            .collect(Collectors.joining(" "))
            );
        }
        return translatable;
    }

    public static Component translatableTooltip(Identifier identifier, String category) {
        String key = identifier.toLanguageKey("options.".concat(category)).concat(".tooltip");
        Component translatable = Component.translatable(key);

        if (!ComponentUtils.isTranslationResolvable(translatable)) {
            translatable = Component.translatable(
                    "sodium-extra.option.".concat(category).concat(".tooltips"),
                    translatableName(identifier, category)
            );
        }
        return translatable;
    }
}
