package com.megacreative.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Gson TypeAdapters for CodeBlock configuration maps
 * Handles serialization of configItems and itemGroups
 */
public class ConfigItemsTypeAdapters {

    /**
     * TypeAdapter for Map<Integer, ItemStack> (configItems)
     */
    public static class ConfigItemsAdapter implements com.google.gson.JsonSerializer<Map<Integer, ItemStack>>, 
                                                    com.google.gson.JsonDeserializer<Map<Integer, ItemStack>> {
        
        private final ItemStackTypeAdapter itemStackAdapter = new ItemStackTypeAdapter();
        
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
                    // Skip invalid slot numbers
                }
            }
            
            return configItems;
        }
    }
    
    /**
     * TypeAdapter for Map<String, List<Integer>> (itemGroups)
     */
    public static class ItemGroupsAdapter implements com.google.gson.JsonSerializer<Map<String, List<Integer>>>, 
                                                   com.google.gson.JsonDeserializer<Map<String, List<Integer>>> {
        
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
     * Helper method to create a Gson instance with all necessary TypeAdapters
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