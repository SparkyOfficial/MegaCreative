package com.megacreative.gui.coding;

/**
 * Categories for organizing code blocks in the workspace
 */
public enum BlockCategory {
    EVENTS("Events", "§6Events"),
    ACTIONS("Actions", "§aActions"), 
    CONDITIONS("Conditions", "§eConditions"),
    VARIABLES("Variables", "§bVariables"),
    FUNCTIONS("Functions", "§dFunctions"),
    CONTROL("Control Flow", "§cControl"),
    ENTITIES("Entities", "§9Entities"),
    WORLD("World", "§2World"),
    ITEMS("Items", "§fItems"),
    UTILITY("Utility", "§7Utility");
    
    private final String displayName;
    private final String coloredName;
    
    BlockCategory(String displayName, String coloredName) {
        this.displayName = displayName;
        this.coloredName = coloredName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColoredName() {
        return coloredName;
    }
}