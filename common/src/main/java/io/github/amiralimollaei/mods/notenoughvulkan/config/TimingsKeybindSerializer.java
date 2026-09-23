package io.github.amiralimollaei.mods.notenoughvulkan.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public final class TimingsKeybindSerializer implements JsonSerializer<TimingsKeybind>, JsonDeserializer<TimingsKeybind> {
    @Override
    public JsonElement serialize(TimingsKeybind binding, Type type, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("key", binding.key());
        json.addProperty("modifiers", binding.modifiers());
        return json;
    }

    @Override
    public TimingsKeybind deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (json == null || !json.isJsonObject()) {
            return TimingsKeybind.DEFAULT;
        }
        JsonObject object = json.getAsJsonObject();
        if (!object.has("key") || !object.has("modifiers")) {
            return TimingsKeybind.DEFAULT;
        }
        try {
            return new TimingsKeybind(object.get("key").getAsInt(), object.get("modifiers").getAsInt());
        } catch (NumberFormatException | UnsupportedOperationException e) {
            throw new JsonParseException("Invalid timings keybind", e);
        }
    }
}
