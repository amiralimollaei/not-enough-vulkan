package io.github.amiralimollaei.mods.notenoughvulkan.config;

import com.google.gson.*;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Type;

public class VkKeybindSerializer implements JsonSerializer<VkKeybind>, JsonDeserializer<VkKeybind> {
    @Override
    public JsonElement serialize(VkKeybind binding, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("key", binding.key());
        json.addProperty("modifiers", binding.modifiers());
        return json;
    }

    @Override
    public @Nullable VkKeybind deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || !json.isJsonObject()) {
            return null;
        }
        JsonObject object = json.getAsJsonObject();
        if (!object.has("key") || !object.has("modifiers")) {
            return null;
        }
        try {
            return new VkKeybind(object.get("key").getAsInt(), object.get("modifiers").getAsInt());
        } catch (NumberFormatException | UnsupportedOperationException e) {
            throw new JsonParseException("Invalid keybind", e);
        }
    }
}
