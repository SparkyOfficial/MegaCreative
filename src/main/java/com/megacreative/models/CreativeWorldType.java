package com.megacreative.models;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * Types of creative worlds available in the plugin
 *
 * Типы творческих миров, доступные в плагине
 *
 * Arten kreativer Welten, die im Plugin verfügbar sind
 */
public enum CreativeWorldType {
    /**
     * Flat world type
     *
     * Тип плоского мира
     *
     * Flache Weltart
     */
    FLAT("Плоский", Material.GRASS_BLOCK, World.Environment.NORMAL, "FLAT"),
    /**
     * Survival world type
     *
     * Тип мира выживания
     *
     * Überlebensweltart
     */
    SURVIVAL("Выживание", Material.STONE, World.Environment.NORMAL, "DEFAULT"),
    /**
     * Ocean world type
     *
     * Тип океанического мира
     *
     * Ozeanweltart
     */
    OCEAN("Океан", Material.WATER_BUCKET, World.Environment.NORMAL, "DEFAULT"),
    /**
     * Nether world type
     *
     * Тип мира Незера
     *
     * Nether-Weltart
     */
    NETHER("Незер", Material.NETHERRACK, World.Environment.NETHER, "DEFAULT"),
    /**
     * End world type
     *
     * Тип мира Энда
     *
     * End-Weltart
     */
    END("Энд", Material.END_STONE, World.Environment.THE_END, "DEFAULT"),
    /**
     * Void world type
     *
     * Тип пустого мира
     *
     * Leere Weltart
     */
    VOID("Пустота", Material.BARRIER, World.Environment.NORMAL, "VOID");
    
    private final String displayName;
    private final Material icon;
    private final World.Environment environment;
    private final String generatorType;
    
    /**
     * Creates a CreativeWorldType
     * @param displayName Display name
     * @param icon Icon material
     * @param environment World environment
     * @param generatorType Generator type
     *
     * Создает CreativeWorldType
     * @param displayName Отображаемое имя
     * @param icon Материал иконки
     * @param environment Окружение мира
     * @param generatorType Тип генератора
     *
     * Erstellt einen CreativeWorldType
     * @param displayName Anzeigename
     * @param icon Symbolmaterial
     * @param environment Weltumgebung
     * @param generatorType Generatortyp
     */
    CreativeWorldType(String displayName, Material icon, World.Environment environment, String generatorType) {
        this.displayName = displayName;
        this.icon = icon;
        this.environment = environment;
        this.generatorType = generatorType;
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
     * Gets the world environment
     * @return World environment
     *
     * Получает окружение мира
     * @return Окружение мира
     *
     * Ruft die Weltumgebung ab
     * @return Weltumgebung
     */
    public World.Environment getEnvironment() {
        return environment;
    }
    
    /**
     * Gets the generator type
     * @return Generator type
     *
     * Получает тип генератора
     * @return Тип генератора
     *
     * Ruft den Generatortyp ab
     * @return Generatortyp
     */
    public String getGeneratorType() {
        return generatorType;
    }
}