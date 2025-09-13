package com.megacreative.utils;

import com.megacreative.MegaCreative;
import com.megacreative.configs.WorldCode;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    
    private final MegaCreative plugin;
    private FileConfiguration config;
    
    public ConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Установка значений по умолчанию
        config.addDefault("worlds.maxPerPlayer", 5);
        config.addDefault("worlds.borderSize", 300);
        config.addDefault("worlds.autoSave", true);
        config.addDefault("worlds.autoSaveInterval", 300); // 5 минут
        
        config.addDefault("messages.prefix", "§8[§bMegaCreative§8] ");
        config.addDefault("messages.noPermission", "§cУ вас нет прав на это действие!");
        config.addDefault("messages.worldNotFound", "§cМир не найден!");
        config.addDefault("messages.worldLimitReached", "§cВы достигли лимита миров!");
        config.addDefault("messages.worldCreated", "§aМир успешно создан!");
        config.addDefault("messages.worldDeleted", "§aМир успешно удален!");
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    public int getMaxWorldsPerPlayer() {
        if (config == null) {
            return 5; // Default value
        }
        return config.getInt("worlds.maxPerPlayer", 5);
    }
    
    public int getWorldBorderSize() {
        if (config == null) {
            return 300; // Default value
        }
        return config.getInt("worlds.borderSize", 300);
    }
    
    public boolean isAutoSaveEnabled() {
        if (config == null) {
            return true; // Default value
        }
        return config.getBoolean("worlds.autoSave", true);
    }
    
    public int getAutoSaveInterval() {
        if (config == null) {
            return 300; // Default value
        }
        return config.getInt("worlds.autoSaveInterval", 300);
    }
    
    public String getMessage(String key) {
        if (config == null) {
            return "§cСообщение не найдено: " + key; // Default value
        }
        return config.getString("messages." + key, "§cСообщение не найдено: " + key);
    }
    
    public String getPrefix() {
        if (config == null) {
            return "§8[§bMegaCreative§8] "; // Default value
        }
        return config.getString("messages.prefix", "§8[§bMegaCreative§8] ");
    }
    
    /**
     * Gets the WorldCode configuration manager
     * @return WorldCode class for static access
     */
    public Class<WorldCode> getWorldCode() {
        return WorldCode.class; // Return the class for static access
    }
    
    /**
     * Shuts down the config manager and cleans up resources
     */
    public void shutdown() {
        // Save any pending configuration changes
        plugin.saveConfig();
        config = null;
    }
}
