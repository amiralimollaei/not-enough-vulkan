package io.github.amiralimollaei.mods.notenoughvulkan.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.amiralimollaei.mods.notenoughvulkan.NotEnoughVulkanClientMod;
import me.flashyreese.mods.sodiumextra.common.util.IdentifierSerializer;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class NotEnoughVulkanGameOptions {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();
    public final CompatSettings compatSettings = new CompatSettings();
    private File file;

    public static NotEnoughVulkanGameOptions load(File file) {
        NotEnoughVulkanGameOptions config = null;

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                config = gson.fromJson(reader, NotEnoughVulkanGameOptions.class);
            } catch (Exception e) {
                NotEnoughVulkanClientMod.logger().error("Could not parse config, falling back to defaults!", e);
            }
        }
        if (config == null) config = new NotEnoughVulkanGameOptions();

        config.file = file;
        config.writeChanges();

        return config;
    }

    public void writeChanges() {
        File dir = this.file.getParentFile();

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("Could not create parent directories");
            }
        } else if (!dir.isDirectory()) {
            throw new RuntimeException("The parent file is not a directory");
        }

        try (FileWriter writer = new FileWriter(this.file)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException("Could not save configuration file", e);
        }
    }

    public static class CompatSettings {
        public boolean skipWaylandPatches;
        public String selectedMonitor;
        public boolean forceX11;

        public CompatSettings() {
            skipWaylandPatches = false;
            forceX11 = false;
        }
    }
}