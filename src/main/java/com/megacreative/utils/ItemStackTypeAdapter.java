package com.megacreative.utils;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * Custom Gson TypeAdapter for ItemStack serialization/deserialization
 * Handles ItemStack conversion to/from JSON for persistent storage
 */
public class ItemStackTypeAdapter implements com.google.gson.JsonSerializer<ItemStack>, com.google.gson.JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        if (itemStack == null) {
            return JsonNull.INSTANCE;
        }
        
        // Используем встроенную в Bukkit сериализацию, она надежна и безопасна
        // Это решает проблему InaccessibleObjectException с Java 9+ модулями
        Map<String, Object> serializedData = itemStack.serialize();
        return context.serialize(serializedData);
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) 
            throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }

        try {
            // Десериализуем из карты, которую создает Bukkit
            Map<String, Object> serializedData = context.deserialize(jsonElement, Map.class);
            return ItemStack.deserialize(serializedData);
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize ItemStack using Bukkit serialization: " + e.getMessage(), e);
        }
    }
}