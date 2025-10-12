package com.megacreative.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Управляет конфигурациями блоков для плагина
 *
 * Manages block-related configurations for the plugin
 *
 * Verwaltet blockbezogene Konfigurationen für das Plugin
 */
public class BlockConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final Map<String, Material> blockMaterials = new HashMap<>();
    
    /**
     * Инициализирует конфигурацию блоков
     * @param plugin Экземпляр основного плагина
     *
     * Initializes block configuration
     * @param plugin Main plugin instance
     *
     * Initialisiert die Blockkonfiguration
     * @param plugin Hauptplugin-Instanz
     */
    public BlockConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Загружает конфигурацию блоков из файла конфигурации
     *
     * Loads the block configuration from the config file
     *
     * Lädt die Blockkonfiguration aus der Konfigurationsdatei
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        
        
        
        
        blockMaterials.clear();
        if (config.contains("blocks")) {
            for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
                String materialName = config.getString("blocks." + key);
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    blockMaterials.put(key, material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material type: " + materialName);
                    
                    
                }
            }
        }
    }
    
    /**
     * Получает материал для определенного типа блока
     * @param blockType Тип блока
     * @return Material или null, если не найден
     *
     * Gets the material for a specific block type
     * @param blockType The type of block
     * @return The Material or null if not found
     *
     * Ruft das Material für einen bestimmten Blocktyp ab
     * @param blockType Der Blocktyp
     * @return Das Material oder null, wenn nicht gefunden
     */
    public Material getBlockMaterial(String blockType) {
        return blockMaterials.get(blockType);
    }
    
    /**
     * Получает все сконфигурированные материалы блоков
     * @return Карта типов блоков и их материалов
     *
     * Gets all configured block materials
     * @return Map of block types to their materials
     *
     * Ruft alle konfigurierten Blockmaterialien ab
     * @return Karte der Blocktypen zu ihren Materialien
     */
    public Map<String, Material> getBlockMaterials() {
        return new HashMap<>(blockMaterials);
    }
    
    /**
     * Перезагружает конфигурацию с диска
     *
     * Reloads the configuration from disk
     *
     * Lädt die Konfiguration von der Festplatte neu
     */
    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }
    
    /**
     * Сохраняет текущую конфигурацию на диск
     *
     * Saves the current configuration to disk
     *
     * Speichert die aktuelle Konfiguration auf der Festplatte
     */
    public void save() {
        
        
        
        for (Map.Entry<String, Material> entry : blockMaterials.entrySet()) {
            config.set("blocks." + entry.getKey(), entry.getValue().name());
        }
        
        
        
        
        plugin.saveConfig();
    }
}