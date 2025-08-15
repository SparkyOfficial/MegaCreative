package com.megacreative.config;

import com.megacreative.exceptions.ConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Валидатор конфигурации плагина
 */
public class ConfigurationValidator {
    
    /**
     * Валидирует основную конфигурацию
     * @param config Конфигурация для валидации
     * @throws ConfigurationException если конфигурация неверна
     */
    public static void validateConfig(FileConfiguration config) throws ConfigurationException {
        validateWorldsSection(config);
        validateMessagesSection(config);
        validateGUISection(config);
        validateDevelopmentSection(config);
    }
    
    /**
     * Валидирует секцию миров
     * @param config Конфигурация
     * @throws ConfigurationException если секция неверна
     */
    private static void validateWorldsSection(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("worlds.maxPerPlayer")) {
            throw new ConfigurationException("Missing worlds.maxPerPlayer");
        }
        
        int maxWorlds = config.getInt("worlds.maxPerPlayer");
        if (maxWorlds <= 0 || maxWorlds > 100) {
            throw new ConfigurationException("Invalid maxPerPlayer value: " + maxWorlds + ". Must be between 1 and 100");
        }
        
        if (!config.contains("worlds.borderSize")) {
            throw new ConfigurationException("Missing worlds.borderSize");
        }
        
        int borderSize = config.getInt("worlds.borderSize");
        if (borderSize <= 0 || borderSize > 10000) {
            throw new ConfigurationException("Invalid borderSize value: " + borderSize + ". Must be between 1 and 10000");
        }
        
        if (!config.contains("worlds.autoSaveInterval")) {
            throw new ConfigurationException("Missing worlds.autoSaveInterval");
        }
        
        int autoSaveInterval = config.getInt("worlds.autoSaveInterval");
        if (autoSaveInterval <= 0 || autoSaveInterval > 3600) {
            throw new ConfigurationException("Invalid autoSaveInterval value: " + autoSaveInterval + ". Must be between 1 and 3600 seconds");
        }
    }
    
    /**
     * Валидирует секцию сообщений
     * @param config Конфигурация
     * @throws ConfigurationException если секция неверна
     */
    private static void validateMessagesSection(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("messages.prefix")) {
            throw new ConfigurationException("Missing messages.prefix");
        }
        
        String prefix = config.getString("messages.prefix");
        if (prefix == null || prefix.trim().isEmpty()) {
            throw new ConfigurationException("Prefix cannot be empty");
        }
        
        // Проверяем наличие основных сообщений
        String[] requiredMessages = {
            "messages.worldCreated",
            "messages.worldDeleted",
            "messages.worldNotFound",
            "messages.noPermission",
            "messages.playerNotFound"
        };
        
        for (String messagePath : requiredMessages) {
            if (!config.contains(messagePath)) {
                throw new ConfigurationException("Missing message: " + messagePath);
            }
        }
    }
    
    /**
     * Валидирует секцию GUI
     * @param config Конфигурация
     * @throws ConfigurationException если секция неверна
     */
    private static void validateGUISection(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("gui.updateInterval")) {
            throw new ConfigurationException("Missing gui.updateInterval");
        }
        
        int updateInterval = config.getInt("gui.updateInterval");
        if (updateInterval <= 0 || updateInterval > 100) {
            throw new ConfigurationException("Invalid gui.updateInterval value: " + updateInterval + ". Must be between 1 and 100 ticks");
        }
    }
    
    /**
     * Валидирует секцию разработки
     * @param config Конфигурация
     * @throws ConfigurationException если секция неверна
     */
    private static void validateDevelopmentSection(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("development.enableDebug")) {
            throw new ConfigurationException("Missing development.enableDebug");
        }
        
        if (!config.contains("development.logWorldActions")) {
            throw new ConfigurationException("Missing development.logWorldActions");
        }
    }
    
    /**
     * Валидирует конфигурацию блоков кодинга
     * @param config Конфигурация блоков
     * @throws ConfigurationException если конфигурация неверна
     */
    public static void validateCodingBlocksConfig(FileConfiguration config) throws ConfigurationException {
        if (config == null) {
            throw new ConfigurationException("Coding blocks configuration is null");
        }
        
        if (!config.contains("blocks")) {
            throw new ConfigurationException("Missing 'blocks' section in coding_blocks.yml");
        }
        
        // Проверяем наличие хотя бы одного блока
        if (config.getConfigurationSection("blocks") == null) {
            throw new ConfigurationException("No blocks defined in coding_blocks.yml");
        }
        
        // Проверяем каждый блок
        for (String blockKey : config.getConfigurationSection("blocks").getKeys(false)) {
            String materialPath = "blocks." + blockKey + ".material";
            String actionsPath = "blocks." + blockKey + ".actions";
            
            if (!config.contains(materialPath)) {
                throw new ConfigurationException("Missing material for block: " + blockKey);
            }
            
            if (!config.contains(actionsPath)) {
                throw new ConfigurationException("Missing actions for block: " + blockKey);
            }
            
            String material = config.getString(materialPath);
            if (material == null || material.trim().isEmpty()) {
                throw new ConfigurationException("Invalid material for block: " + blockKey);
            }
            
            // Проверяем, что actions - это список
            if (!config.isList(actionsPath)) {
                throw new ConfigurationException("Actions for block " + blockKey + " must be a list");
            }
        }
    }
}
