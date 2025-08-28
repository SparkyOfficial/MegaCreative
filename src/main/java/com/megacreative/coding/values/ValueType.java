package com.megacreative.coding.values;

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
            case TEXT: return "";
            case NUMBER: return 0;
            case BOOLEAN: return false;
            case LIST: return new java.util.ArrayList<>();
            case DICTIONARY: return new java.util.HashMap<>();
            default: return null;
        }
    }
}