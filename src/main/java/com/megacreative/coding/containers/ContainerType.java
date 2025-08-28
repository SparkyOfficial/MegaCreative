package com.megacreative.coding.containers;

/**
 * Container types for code blocks
 */
public enum ContainerType {
    SIGN("Sign", "§eТабличка", "Text-based parameter display"),
    CHEST("Chest", "§6Сундук", "Item-based parameter storage"),
    BARREL("Barrel", "§8Бочка", "Compact parameter storage"),
    SHULKER_BOX("Shulker Box", "§5Шалкер", "Portable parameter storage");
    
    private final String name;
    private final String displayName;
    private final String description;
    
    ContainerType(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
}