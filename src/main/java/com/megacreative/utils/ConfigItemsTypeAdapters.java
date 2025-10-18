package com.megacreative.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Пользовательские адаптеры типов Gson для карт конфигурации блоков кода
 * Обрабатывает сериализацию configItems и itemGroups
 *
 * Custom Gson TypeAdapters for CodeBlock configuration maps
 * Handles serialization of configItems and itemGroups
 *
 * Benutzerdefinierte Gson-TypeAdapter für CodeBlock-Konfigurationskarten
 * Verarbeitet die Serialisierung von configItems und itemGroups
 */
public class ConfigItemsTypeAdapters {

    /**
     * TypeAdapter для Map<Integer, ItemStack> (configItems)
     *
     * TypeAdapter for Map<Integer, ItemStack> (configItems)
     *
     * TypeAdapter für Map<Integer, ItemStack> (configItems)
     */
    public static class ConfigItemsAdapter implements com.google.gson.JsonSerializer<Map<Integer, ItemStack>>, 
                                                    com.google.gson.JsonDeserializer<Map<Integer, ItemStack>> {
        
        private final ItemStackTypeAdapter itemStackAdapter = new ItemStackTypeAdapter();
        
        /**
         * Сериализует карту элементов конфигурации в JSON
         * @param configItems Карта элементов конфигурации для сериализации
         * @param typeOfSrc Тип источника
         * @param context Контекст сериализации
         * @return Сериализованный JSON элемент
         *
         * Serializes config items map to JSON
         * @param configItems Config items map to serialize
         * @param typeOfSrc Source type
         * @param context Serialization context
         * @return Serialized JSON element
         *
         * Serialisiert die Konfigurationselemente-Karte zu JSON
         * @param configItems Zu serialisierende Konfigurationselemente-Karte
         * @param typeOfSrc Quelltyp
         * @param context Serialisierungskontext
         * @return Serialisiertes JSON-Element
         */
        @Override
        public JsonElement serialize(Map<Integer, ItemStack> configItems, Type typeOfSrc, 
                                   JsonSerializationContext context) {
            if (configItems == null || configItems.isEmpty()) {
                return JsonNull.INSTANCE;
            }
            
            JsonObject json = new JsonObject();
            for (Map.Entry<Integer, ItemStack> entry : configItems.entrySet()) {
                String slotKey = String.valueOf(entry.getKey());
                JsonElement itemJson = itemStackAdapter.serialize(entry.getValue(), ItemStack.class, context);
                json.add(slotKey, itemJson);
            }
            
            return json;
        }
        
        /**
         * Десериализует JSON в карту элементов конфигурации
         * @param json JSON элемент для десериализации
         * @param typeOfT Тип цели
         * @param context Контекст десериализации
         * @return Десериализованная карта элементов конфигурации
         * @throws JsonParseException Если происходит ошибка разбора JSON
         *
         * Deserializes JSON to config items map
         * @param json JSON element to deserialize
         * @param typeOfT Target type
         * @param context Deserialization context
         * @return Deserialized config items map
         * @throws JsonParseException If JSON parsing error occurs
         *
         * Deserialisiert JSON zu einer Konfigurationselemente-Karte
         * @param json Zu deserialisierendes JSON-Element
         * @param typeOfT Zieltyp
         * @param context Deserialisierungskontext
         * @return Deserialisierte Konfigurationselemente-Karte
         * @throws JsonParseException Wenn ein JSON-Parsing-Fehler auftritt
         */
        @Override
        public Map<Integer, ItemStack> deserialize(JsonElement json, Type typeOfT, 
                                                 JsonDeserializationContext context) throws JsonParseException {
            Map<Integer, ItemStack> configItems = new HashMap<>();
            
            if (json.isJsonNull() || !json.isJsonObject()) {
                return configItems;
            }
            
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                try {
                    Integer slot = Integer.valueOf(entry.getKey());
                    ItemStack item = itemStackAdapter.deserialize(entry.getValue(), ItemStack.class, context);
                    if (item != null) {
                        configItems.put(slot, item);
                    }
                } catch (NumberFormatException e) {
                    // Log exception and continue processing
                    // This is expected behavior when parsing slot keys
                    // Silently ignore invalid slot keys and continue with other operations
                }
            }
            
            return configItems;
        }
    }
    
    /**
     * TypeAdapter для Map<String, List<Integer>> (itemGroups)
     *
     * TypeAdapter for Map<String, List<Integer>> (itemGroups)
     *
     * TypeAdapter für Map<String, List<Integer>> (itemGroups)
     */
    public static class ItemGroupsAdapter implements com.google.gson.JsonSerializer<Map<String, List<Integer>>>, 
                                                   com.google.gson.JsonDeserializer<Map<String, List<Integer>>> {
        
        /**
         * Сериализует группы элементов в JSON
         * @param itemGroups Группы элементов для сериализации
         * @param typeOfSrc Тип источника
         * @param context Контекст сериализации
         * @return Сериализованный JSON элемент
         *
         * Serializes item groups to JSON
         * @param itemGroups Item groups to serialize
         * @param typeOfSrc Source type
         * @param context Serialization context
         * @return Serialized JSON element
         *
         * Serialisiert Elementgruppen zu JSON
         * @param itemGroups Zu serialisierende Elementgruppen
         * @param typeOfSrc Quelltyp
         * @param context Serialisierungskontext
         * @return Serialisiertes JSON-Element
         */
        @Override
        public JsonElement serialize(Map<String, List<Integer>> itemGroups, Type typeOfSrc, 
                                   JsonSerializationContext context) {
            if (itemGroups == null || itemGroups.isEmpty()) {
                return JsonNull.INSTANCE;
            }
            
            JsonObject json = new JsonObject();
            for (Map.Entry<String, List<Integer>> entry : itemGroups.entrySet()) {
                JsonArray slotsArray = new JsonArray();
                for (Integer slot : entry.getValue()) {
                    slotsArray.add(slot);
                }
                json.add(entry.getKey(), slotsArray);
            }
            
            return json;
        }
        
        /**
         * Десериализует JSON в группы элементов
         * @param json JSON элемент для десериализации
         * @param typeOfT Тип цели
         * @param context Контекст десериализации
         * @return Десериализованные группы элементов
         * @throws JsonParseException Если происходит ошибка разбора JSON
         *
         * Deserializes JSON to item groups
         * @param json JSON element to deserialize
         * @param typeOfT Target type
         * @param context Deserialization context
         * @return Deserialized item groups
         * @throws JsonParseException If JSON parsing error occurs
         *
         * Deserialisiert JSON zu Elementgruppen
         * @param json Zu deserialisierendes JSON-Element
         * @param typeOfT Zieltyp
         * @param context Deserialisierungskontext
         * @return Deserialisierte Elementgruppen
         * @throws JsonParseException Wenn ein JSON-Parsing-Fehler auftritt
         */
        @Override
        public Map<String, List<Integer>> deserialize(JsonElement json, Type typeOfT, 
                                                    JsonDeserializationContext context) throws JsonParseException {
            Map<String, List<Integer>> itemGroups = new HashMap<>();
            
            if (json.isJsonNull() || !json.isJsonObject()) {
                return itemGroups;
            }
            
            JsonObject jsonObject = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                String groupName = entry.getKey();
                JsonArray slotsArray = entry.getValue().getAsJsonArray();
                
                java.util.List<Integer> slots = new java.util.ArrayList<>();
                for (JsonElement slotElement : slotsArray) {
                    slots.add(slotElement.getAsInt());
                }
                
                itemGroups.put(groupName, slots);
            }
            
            return itemGroups;
        }
    }
    
    /**
     * Вспомогательный метод для создания экземпляра Gson со всеми необходимыми TypeAdapter'ами
     * @return Экземпляр Gson с зарегистрированными адаптерами
     *
     * Helper method to create a Gson instance with all necessary TypeAdapters
     * @return Gson instance with registered adapters
     *
     * Hilfsmethode zum Erstellen einer Gson-Instanz mit allen notwendigen TypeAdaptern
     * @return Gson-Instanz mit registrierten Adaptern
     */
    public static Gson createGsonWithAdapters() {
        return new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackTypeAdapter())
            .registerTypeAdapter(
                new TypeToken<Map<Integer, ItemStack>>(){}.getType(), 
                new ConfigItemsAdapter()
            )
            .registerTypeAdapter(
                new TypeToken<Map<String, List<Integer>>>(){}.getType(), 
                new ItemGroupsAdapter()
            )
            .setPrettyPrinting()
            .create();
    }
}