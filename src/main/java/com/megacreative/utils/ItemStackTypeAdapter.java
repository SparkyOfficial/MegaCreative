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
 * Custom Gson TypeAdapter for ItemStack serialization/deserialization
 * Handles ItemStack conversion to/from JSON for persistent storage
 */
public class ItemStackTypeAdapter implements com.google.gson.JsonSerializer<ItemStack>, com.google.gson.JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext context) {
        if (itemStack == null) {
            return JsonNull.INSTANCE;
        }

        JsonObject json = new JsonObject();
        
        // Basic item properties
        json.addProperty("type", itemStack.getType().name());
        json.addProperty("amount", itemStack.getAmount());
        
        // Item meta (if present)
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            JsonObject metaJson = new JsonObject();
            
            // Display name
            if (meta.hasDisplayName()) {
                metaJson.addProperty("displayName", meta.getDisplayName());
            }
            
            // Lore
            if (meta.hasLore() && meta.getLore() != null) {
                JsonArray loreArray = new JsonArray();
                for (String loreLine : meta.getLore()) {
                    loreArray.add(loreLine);
                }
                metaJson.add("lore", loreArray);
            }
            
            // Custom model data
            if (meta.hasCustomModelData()) {
                metaJson.addProperty("customModelData", meta.getCustomModelData());
            }
            
            // Enchantments
            if (meta.hasEnchants()) {
                JsonObject enchantmentsJson = new JsonObject();
                for (Map.Entry<Enchantment, Integer> enchant : meta.getEnchants().entrySet()) {
                    enchantmentsJson.addProperty(enchant.getKey().getKey().getKey(), enchant.getValue());
                }
                metaJson.add("enchantments", enchantmentsJson);
            }
            
            json.add("meta", metaJson);
        }
        
        return json;
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) 
            throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }

        JsonObject json = jsonElement.getAsJsonObject();
        
        try {
            // Basic item creation
            Material material = Material.valueOf(json.get("type").getAsString());
            int amount = json.get("amount").getAsInt();
            
            ItemStack itemStack = new ItemStack(material, amount);
            
            // Restore meta (if present)
            if (json.has("meta")) {
                JsonObject metaJson = json.getAsJsonObject("meta");
                ItemMeta meta = itemStack.getItemMeta();
                
                if (meta != null) {
                    // Display name
                    if (metaJson.has("displayName")) {
                        meta.setDisplayName(metaJson.get("displayName").getAsString());
                    }
                    
                    // Lore
                    if (metaJson.has("lore")) {
                        JsonArray loreArray = metaJson.getAsJsonArray("lore");
                        List<String> lore = new ArrayList<>();
                        for (JsonElement loreElement : loreArray) {
                            lore.add(loreElement.getAsString());
                        }
                        meta.setLore(lore);
                    }
                    
                    // Custom model data
                    if (metaJson.has("customModelData")) {
                        meta.setCustomModelData(metaJson.get("customModelData").getAsInt());
                    }
                    
                    // Enchantments
                    if (metaJson.has("enchantments")) {
                        JsonObject enchantmentsJson = metaJson.getAsJsonObject("enchantments");
                        for (Map.Entry<String, JsonElement> enchantEntry : enchantmentsJson.entrySet()) {
                            try {
                                Enchantment enchantment = Enchantment.getByKey(
                                    org.bukkit.NamespacedKey.minecraft(enchantEntry.getKey())
                                );
                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, enchantEntry.getValue().getAsInt(), true);
                                }
                            } catch (Exception e) {
                                // Skip invalid enchantments
                            }
                        }
                    }
                    
                    itemStack.setItemMeta(meta);
                }
            }
            
            return itemStack;
            
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize ItemStack: " + e.getMessage(), e);
        }
    }
}