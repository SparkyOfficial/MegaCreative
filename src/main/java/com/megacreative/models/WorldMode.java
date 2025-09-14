package com.megacreative.models;

import org.bukkit.GameMode;
import org.bukkit.Material;

/**
 * World modes that define the behavior and capabilities of a world
 *
 * Режимы мира, определяющие поведение и возможности мира
 *
 * Weltenmodi, die das Verhalten und die Fähigkeiten einer Welt definieren
 */
public enum WorldMode {
    /**
     * Play mode - for playing the world
     *
     * Режим игры - для игры в мире
     *
     * Spielmodus - zum Spielen in der Welt
     */
    PLAY("Игра", Material.DIAMOND_SWORD, GameMode.ADVENTURE, true),
    /**
     * Build mode - for building in the world
     *
     * Режим строительства - для строительства в мире
     *
     * Baumodus - zum Bauen in der Welt
     */
    BUILD("Строительство", Material.GOLDEN_PICKAXE, GameMode.CREATIVE, false),
    /**
     * Development mode - for coding and development
     *
     * Режим разработки - для программирования и разработки
     *
     * Entwicklungsmodus - für Codierung und Entwicklung
     */
    DEV("Разработка", Material.COMMAND_BLOCK, GameMode.CREATIVE, false);
    
    private final String displayName;
    private final Material icon;
    private final GameMode defaultGameMode;
    private final boolean codeEnabled;
    
    /**
     * Creates a WorldMode
     * @param displayName Display name
     * @param icon Icon material
     * @param defaultGameMode Default game mode
     * @param codeEnabled Whether coding is enabled
     *
     * Создает WorldMode
     * @param displayName Отображаемое имя
     * @param icon Материал иконки
     * @param defaultGameMode Режим игры по умолчанию
     * @param codeEnabled Включено ли программирование
     *
     * Erstellt einen WorldMode
     * @param displayName Anzeigename
     * @param icon Symbolmaterial
     * @param defaultGameMode Standard-Spielmodus
     * @param codeEnabled Ob Codierung aktiviert ist
     */
    WorldMode(String displayName, Material icon, GameMode defaultGameMode, boolean codeEnabled) {
        this.displayName = displayName;
        this.icon = icon;
        this.defaultGameMode = defaultGameMode;
        this.codeEnabled = codeEnabled;
    }
    
    /**
     * Gets the display name
     * @return Display name
     *
     * Получает отображаемое имя
     * @return Отображаемое имя
     *
     * Ruft den Anzeigenamen ab
     * @return Anzeigename
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the icon material
     * @return Icon material
     *
     * Получает материал иконки
     * @return Материал иконки
     *
     * Ruft das Symbolmaterial ab
     * @return Symbolmaterial
     */
    public Material getIcon() {
        return icon;
    }
    
    /**
     * Gets the default game mode
     * @return Default game mode
     *
     * Получает режим игры по умолчанию
     * @return Режим игры по умолчанию
     *
     * Ruft den Standard-Spielmodus ab
     * @return Standard-Spielmodus
     */
    public GameMode getDefaultGameMode() {
        return defaultGameMode;
    }
    
    /**
     * Checks if coding is enabled
     * @return true if coding is enabled
     *
     * Проверяет, включено ли программирование
     * @return true, если программирование включено
     *
     * Prüft, ob Codierung aktiviert ist
     * @return true, wenn Codierung aktiviert ist
     */
    public boolean isCodeEnabled() {
        return codeEnabled;
    }
}