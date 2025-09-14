package com.megacreative.utils;

import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Пользовательский адаптер типов Gson для сериализации/десериализации ItemStack
 * Обрабатывает преобразование ItemStack в/из JSON для постоянного хранения
 *
 * Custom Gson TypeAdapter for ItemStack serialization/deserialization
 * Handles ItemStack conversion to/from JSON for persistent storage
 *
 * Benutzerdefinierter Gson-TypeAdapter für ItemStack-Serialisierung/Deserialisierung
 * Verarbeitet die Konvertierung von ItemStack zu/von JSON für dauerhafte Speicherung
 */
public class ItemStackTypeAdapter implements com.google.gson.JsonSerializer<ItemStack>, com.google.gson.JsonDeserializer<ItemStack> {

    /**
     * Сериализует ItemStack в JSON элемент
     * @param itemStack ItemStack для сериализации
     * @param type Тип сериализуемого объекта
     * @param context Контекст сериализации Gson
     * @return Сериализованный JSON элемент
     *
     * Serializes ItemStack to JSON element
     * @param itemStack ItemStack to serialize
     * @param type Type of object being serialized
     * @param context Gson serialization context
     * @return Serialized JSON element
     *
     * Serialisiert ItemStack zu einem JSON-Element
     * @param itemStack Zu serialisierender ItemStack
     * @param type Typ des zu serialisierenden Objekts
     * @param context Gson-Serialisierungskontext
     * @return Serialisiertes JSON-Element
     */
    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        if (itemStack == null) {
            return JsonNull.INSTANCE;
        }
        
        // Используем встроенную в Bukkit сериализацию, она надежна и безопасна
        // Use Bukkit's built-in serialization, it's reliable and safe
        // Verwenden Sie die in Bukkit eingebaute Serialisierung, sie ist zuverlässig und sicher
        // Это решает проблему InaccessibleObjectException с Java 9+ модулями
        // This solves the InaccessibleObjectException problem with Java 9+ modules
        // Dies löst das InaccessibleObjectException-Problem mit Java 9+-Modulen
        Map<String, Object> serializedData = itemStack.serialize();
        return context.serialize(serializedData);
    }

    /**
     * Десериализует JSON элемент в ItemStack
     * @param jsonElement JSON элемент для десериализации
     * @param type Тип целевого объекта
     * @param context Контекст десериализации Gson
     * @return Десериализованный ItemStack
     * @throws JsonParseException Если десериализация не удалась
     *
     * Deserializes JSON element to ItemStack
     * @param jsonElement JSON element to deserialize
     * @param type Target object type
     * @param context Gson deserialization context
     * @return Deserialized ItemStack
     * @throws JsonParseException If deserialization failed
     *
     * Deserialisiert ein JSON-Element zu einem ItemStack
     * @param jsonElement Zu deserialisierendes JSON-Element
     * @param type Zielobjekttyp
     * @param context Gson-Deserialisierungskontext
     * @return Deserialisierter ItemStack
     * @throws JsonParseException Wenn die Deserialisierung fehlgeschlagen ist
     */
    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) 
            throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }

        try {
            // Десериализуем из карты, которую создает Bukkit
            // Deserialize from the map that Bukkit creates
            // Deserialisieren aus der Karte, die Bukkit erstellt
            Map<String, Object> serializedData = context.deserialize(jsonElement, Map.class);
            return ItemStack.deserialize(serializedData);
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize ItemStack using Bukkit serialization: " + e.getMessage(), e);
            // Не удалось десериализовать ItemStack с использованием сериализации Bukkit:
            // Fehler bei der Deserialisierung von ItemStack mit Bukkit-Serialisierung:
        }
    }
}