package com.megacreative.gui.coding;

/**
 * Категории для организации блоков кода в рабочей области
 *
 * Categories for organizing code blocks in the workspace
 *
 * Kategorien zur Organisation von Codeblöcken im Arbeitsbereich
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
    
    /**
     * Инициализирует категорию блока
     * @param displayName Отображаемое имя категории
     * @param coloredName Цветное имя категории
     *
     * Initializes block category
     * @param displayName Display name of category
     * @param coloredName Colored name of category
     *
     * Initialisiert die Blockkategorie
     * @param displayName Anzeigename der Kategorie
     * @param coloredName Farbiger Name der Kategorie
     */
    BlockCategory(String displayName, String coloredName) {
        this.displayName = displayName;
        this.coloredName = coloredName;
    }
    
    /**
     * Получает отображаемое имя категории
     * @return Отображаемое имя категории
     *
     * Gets display name of category
     * @return Display name of category
     *
     * Ruft den Anzeigenamen der Kategorie ab
     * @return Anzeigename der Kategorie
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Получает цветное имя категории
     * @return Цветное имя категории
     *
     * Gets colored name of category
     * @return Colored name of category
     *
     * Ruft den farbigen Namen der Kategorie ab
     * @return Farbiger Name der Kategorie
     */
    public String getColoredName() {
        return coloredName;
    }
}