package io.github.amiralimollaei.mods.notenoughvulkan.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import me.flashyreese.mods.sodiumextra.client.config.ConfigFileIO;
import me.flashyreese.mods.sodiumextra.common.util.IdentifierSerializer;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NotEnoughVulkanGameOptions {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(TimingsKeybind.class, new TimingsKeybindSerializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public PatchesSettings patchesSettings = new PatchesSettings();
    private Path path;

    public static NotEnoughVulkanGameOptions load(Path path) {
        NotEnoughVulkanGameOptions config;
        boolean shouldWriteChanges = true;

        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                config = GSON.fromJson(reader, NotEnoughVulkanGameOptions.class);
                if (config == null) {
                    throw new JsonParseException("Root element must be a JSON object");
                }
            } catch (IOException | JsonParseException | IllegalStateException e) {
                NotEnoughVulkanClientMod.logger().warn("Could not read config, falling back to defaults", e);
                config = new NotEnoughVulkanGameOptions();
                shouldWriteChanges = moveCorruptConfig(path);
            }
        } else {
            config = new NotEnoughVulkanGameOptions();
        }

        config.sanitize();
        config.path = path;

        if (shouldWriteChanges) {
            config.writeChanges();
        }

        return config;
    }

    public void writeChanges() {
        if (this.path == null) {
            NotEnoughVulkanClientMod.logger().warn("Could not save configuration file because no path was set");
            return;
        }

        try {
            this.sanitize();
            ConfigFileIO.writeStringAtomically(this.path, GSON.toJson(this) + System.lineSeparator());
        } catch (IOException e) {
            NotEnoughVulkanClientMod.logger().warn("Could not save configuration file", e);
        }
    }

    private void sanitize() {
        if (this.patchesSettings == null) {
            this.patchesSettings = new PatchesSettings();
        }
        if (this.patchesSettings.selectedMonitor == null) {
            this.patchesSettings.selectedMonitor = "";
        }
        if (this.patchesSettings.timingsKeybind == null
                || this.patchesSettings.timingsKeybind.key() <= 0
                || this.patchesSettings.timingsKeybind.key() > GLFW.GLFW_KEY_LAST
                || (this.patchesSettings.timingsKeybind.modifiers() & ~7) != 0) {
            this.patchesSettings.timingsKeybind = TimingsKeybind.DEFAULT;
        }
    }

    private static boolean moveCorruptConfig(Path path) {
        try {
            Path corruptPath = ConfigFileIO.moveCorruptFile(path);
            NotEnoughVulkanClientMod.logger().warn("Moved corrupt configuration file to {}", corruptPath);
            return true;
        } catch (IOException e) {
            NotEnoughVulkanClientMod.logger().warn("Could not move corrupt configuration file", e);
            return false;
        }
    }

    public static final class PatchesSettings {
        public boolean skipWaylandPatches = false;
        public String selectedMonitor = "";
        public TimingsKeybind timingsKeybind = TimingsKeybind.DEFAULT;
    }
}
