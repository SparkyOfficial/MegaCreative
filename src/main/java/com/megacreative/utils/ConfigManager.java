package com.megacreative.utils;

import com.megacreative.MegaCreative;
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
        return config.getInt("worlds.maxPerPlayer", 5);
    }
    
    public int getWorldBorderSize() {
        return config.getInt("worlds.borderSize", 300);
    }
    
    public boolean isAutoSaveEnabled() {
        return config.getBoolean("worlds.autoSave", true);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("worlds.autoSaveInterval", 300);
    }
    
    public String getMessage(String key) {
        return config.getString("messages." + key, "§cСообщение не найдено: " + key);
    }
    
    public String getPrefix() {
        return config.getString("messages.prefix", "§8[§bMegaCreative§8] ");
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
