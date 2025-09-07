package com.megacreative.coding.values;

import com.megacreative.coding.values.SimpleDataValue;
import java.util.Objects;

/**
 * Comprehensive value type system for MegaCreative block coding
 * Supports all data types with advanced features like serialization and validation
 */
public enum ValueType {
    
    // === BASIC TYPES ===
    TEXT("Text", "§aТекст", "String values and messages"),
    NUMBER("Number", "§bЧисло", "Integer and decimal numbers"),
    BOOLEAN("Boolean", "§cБулево", "True/false values"),
    
    // === MINECRAFT TYPES ===
    LOCATION("Location", "§eПозиция", "World coordinates (x, y, z)"),
    ITEM("Item", "§6Предмет", "Minecraft items with NBT"),
    BLOCK("Block", "§8Блок", "Block types and materials"),
    ENTITY("Entity", "§dСущество", "Living entities and NPCs"),
    PLAYER("Player", "§9Игрок", "Online players"),
    WORLD("World", "§2Мир", "Minecraft worlds"),
    
    // === ADVANCED TYPES ===
    LIST("List", "§5Список", "Arrays of any value type"),
    DICTIONARY("Dictionary", "§4Словарь", "Key-value mappings"),
    VECTOR("Vector", "§3Вектор", "3D direction vectors"),
    SOUND("Sound", "§aЗвук", "Minecraft sound effects"),
    PARTICLE("Particle", "§bЧастица", "Visual particle effects"),
    POTION("Potion", "§dЗелье", "Potion effects and types"),
    
    // === SPECIAL TYPES ===
    ANY("Any", "§fЛюбой", "Accepts any value type"),
    VARIABLE("Variable", "§7Переменная", "Variable references"),
    FUNCTION("Function", "§1Функция", "Function references"),
    EVENT("Event", "§cСобытие", "Event data and context"),
    
    // === API TYPES ===
    JSON("JSON", "§eJSON", "JSON objects and arrays"),
    HTTP_RESPONSE("HTTP Response", "§6HTTP Ответ", "Web API responses"),
    DATABASE_RESULT("Database Result", "§2Результат БД", "Database query results"),
    PERMISSION("Permission", "§4Разрешение", "Permission nodes"),
    
    // === VISUAL TYPES ===
    COLOR("Color", "§cЦвет", "RGB and hex colors"),
    MATERIAL("Material", "§8Материал", "Block and item materials"),
    ENCHANTMENT("Enchantment", "§5Зачарование", "Item enchantments"),
    GAMEMODE("Gamemode", "§9Режим игры", "Player game modes");
    
    private final String name;
    private final String displayName;
    private final String description;
    
    ValueType(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Determines the appropriate ValueType for a given object
     * @param value The object to check
     * @return The matching ValueType, or TEXT if unknown
     */
    public static ValueType fromObject(Object value) {
        if (value == null) {
            return TEXT;
        }
        
        if (value instanceof Number) {
            return NUMBER;
        } else if (value instanceof Boolean) {
            return BOOLEAN;
        } else if (value instanceof org.bukkit.Location) {
            return LOCATION;
        } else if (value instanceof org.bukkit.entity.Entity) {
            return value instanceof org.bukkit.entity.Player ? PLAYER : ENTITY;
        } else if (value instanceof org.bukkit.World) {
            return WORLD;
        } else if (value instanceof org.bukkit.Material) {
            return MATERIAL;
        } else if (value instanceof java.util.Map) {
            return DICTIONARY;
        } else if (value instanceof java.util.Collection) {
            return LIST;
        } else if (value instanceof org.bukkit.Sound) {
            return SOUND;
        } else if (value instanceof org.bukkit.Particle) {
            return PARTICLE;
        } else if (value instanceof org.bukkit.potion.PotionEffectType) {
            return POTION;
        } else if (value instanceof org.bukkit.Color) {
            return COLOR;
        } else if (value instanceof org.bukkit.enchantments.Enchantment) {
            return ENCHANTMENT;
        } else if (value instanceof org.bukkit.GameMode) {
            return GAMEMODE;
        } else if (value instanceof String) {
            // Try to parse as JSON
            try {
                new org.json.JSONObject(value.toString());
                return JSON;
            } catch (org.json.JSONException e) {
                try {
                    new org.json.JSONArray(value.toString());
                    return JSON;
                } catch (org.json.JSONException ex) {
                    return TEXT;
                }
            }
        }
        
        return TEXT;
    }
    
    /**
     * Checks if this type is compatible with another type
     */
    public boolean isCompatible(ValueType other) {
        if (this == ANY || other == ANY) return true;
        if (this == other) return true;
        
        // Special compatibility rules
        switch (this) {
            case NUMBER:
                return other == TEXT; // Numbers can be converted to text
            case TEXT:
                return other == NUMBER || other == BOOLEAN; // Text can be parsed
            case LIST:
                return true; // Lists can contain any type
            case VARIABLE:
                return true; // Variables can hold any type
            default:
                return false;
        }
    }
    
    /**
     * Gets the default value for this type
     */
    public Object getDefaultValue() {
        switch (this) {
            case TEXT:
                return "";
            case NUMBER:
                return 0;
            case BOOLEAN:
                return false;
            case LOCATION:
                return null; // Should be a Location object in actual implementation
            case ITEM:
                return null; // Should be an ItemStack in actual implementation
            case BLOCK:
                return null; // Should be a Block in actual implementation
            case ENTITY:
                return null; // Should be an Entity in actual implementation
            case PLAYER:
                return null; // Should be a Player in actual implementation
            case WORLD:
                return null; // Should be a World in actual implementation
            case LIST:
                return new java.util.ArrayList<>();
            case DICTIONARY:
                return new java.util.HashMap<>();
            case VECTOR:
                return null; // Should be a Vector in actual implementation
            case SOUND:
                return null; // Should be a Sound in actual implementation
            case PARTICLE:
                return null; // Should be a Particle in actual implementation
            case POTION:
                return null; // Should be a PotionEffect in actual implementation
            default:
                return null;
        }
    }
    
    /**
     * Creates a new DataValue with the default value for this type
     */
    public DataValue createDefaultValue() {
        return new SimpleDataValue(getDefaultValue(), this);
    }
}