package com.megacreative.config;

import com.megacreative.MegaCreative;
import com.megacreative.exceptions.ConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Валидатор конфигурации плагина MegaCreative
 */
public class ConfigurationValidator {
    
    private final MegaCreative plugin;
    
    public ConfigurationValidator(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Валидирует основную конфигурацию
     */
    public void validateMainConfig() throws ConfigurationException {
        FileConfiguration config = plugin.getConfig();
        
        // Проверяем обязательные секции
        if (!config.contains("worlds")) {
            throw new ConfigurationException("Отсутствует секция 'worlds' в конфигурации");
        }
        
        if (!config.contains("coding")) {
            throw new ConfigurationException("Отсутствует секция 'coding' в конфигурации");
        }
        
        // Проверяем настройки миров
        validateWorldSettings(config);
        
        // Проверяем настройки кодинга
        validateCodingSettings(config);
        
        // Проверяем настройки безопасности
        validateSecuritySettings(config);
    }
    
    /**
     * Валидирует настройки миров
     */
    private void validateWorldSettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("worlds.max_worlds_per_player")) {
            throw new ConfigurationException("Отсутствует настройка 'worlds.max_worlds_per_player'");
        }
        
        int maxWorlds = config.getInt("worlds.max_worlds_per_player");
        if (maxWorlds <= 0 || maxWorlds > 100) {
            throw new ConfigurationException("Некорректное значение 'worlds.max_worlds_per_player': " + maxWorlds);
        }
        
        if (!config.contains("worlds.world_border_size")) {
            throw new ConfigurationException("Отсутствует настройка 'worlds.world_border_size'");
        }
        
        int borderSize = config.getInt("worlds.world_border_size");
        if (borderSize <= 0 || borderSize > 10000) {
            throw new ConfigurationException("Некорректное значение 'worlds.world_border_size': " + borderSize);
        }
    }
    
    /**
     * Валидирует настройки кодинга
     */
    private void validateCodingSettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("coding.max_script_size")) {
            throw new ConfigurationException("Отсутствует настройка 'coding.max_script_size'");
        }
        
        int maxScriptSize = config.getInt("coding.max_script_size");
        if (maxScriptSize <= 0 || maxScriptSize > 1000) {
            throw new ConfigurationException("Некорректное значение 'coding.max_script_size': " + maxScriptSize);
        }
        
        if (!config.contains("coding.max_execution_time")) {
            throw new ConfigurationException("Отсутствует настройка 'coding.max_execution_time'");
        }
        
        int maxExecutionTime = config.getInt("coding.max_execution_time");
        if (maxExecutionTime <= 0 || maxExecutionTime > 30000) {
            throw new ConfigurationException("Некорректное значение 'coding.max_execution_time': " + maxExecutionTime);
        }
    }
    
    /**
     * Валидирует настройки безопасности
     */
    private void validateSecuritySettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("security.allowed_commands")) {
            throw new ConfigurationException("Отсутствует настройка 'security.allowed_commands'");
        }
        
        List<String> allowedCommands = config.getStringList("security.allowed_commands");
        if (allowedCommands.isEmpty()) {
            throw new ConfigurationException("Список разрешенных команд пуст");
        }
        
        // Проверяем, что нет опасных команд
        for (String command : allowedCommands) {
            if (isDangerousCommand(command)) {
                throw new ConfigurationException("Обнаружена опасная команда в списке разрешенных: " + command);
            }
        }
    }
    
    /**
     * Проверяет, является ли команда опасной
     */
    private boolean isDangerousCommand(String command) {
        String lowerCommand = command.toLowerCase();
        return lowerCommand.startsWith("op") ||
               lowerCommand.startsWith("deop") ||
               lowerCommand.startsWith("ban") ||
               lowerCommand.startsWith("kick") ||
               lowerCommand.startsWith("stop") ||
               lowerCommand.startsWith("restart") ||
               lowerCommand.startsWith("reload") ||
               lowerCommand.startsWith("save-all") ||
               lowerCommand.startsWith("save-off") ||
               lowerCommand.startsWith("save-on");
    }
    
    /**
     * Валидирует файл конфигурации блоков кодинга
     */
    public void validateCodingBlocksConfig() throws ConfigurationException {
        File codingBlocksFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!codingBlocksFile.exists()) {
            throw new ConfigurationException("Файл coding_blocks.yml не найден");
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(codingBlocksFile);
            
            if (!config.contains("blocks")) {
                throw new ConfigurationException("Отсутствует секция 'blocks' в coding_blocks.yml");
            }
            
            // Проверяем, что есть хотя бы несколько блоков
            if (config.getConfigurationSection("blocks") == null) {
                throw new ConfigurationException("Секция 'blocks' пуста в coding_blocks.yml");
            }
            
        } catch (Exception e) {
            throw new ConfigurationException("Ошибка чтения coding_blocks.yml: " + e.getMessage());
        }
    }
    
    /**
     * Создает резервную копию конфигурации
     */
    public void createBackup() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup");
            
            if (configFile.exists()) {
                org.apache.commons.io.FileUtils.copyFile(configFile, backupFile);
                plugin.getLogger().info("Создана резервная копия конфигурации");
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать резервную копию конфигурации: " + e.getMessage());
        }
    }
    
    /**
     * Восстанавливает конфигурацию из резервной копии
     */
    public void restoreFromBackup() throws ConfigurationException {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup");
            
            if (backupFile.exists()) {
                org.apache.commons.io.FileUtils.copyFile(backupFile, configFile);
                plugin.getLogger().info("Конфигурация восстановлена из резервной копии");
            } else {
                throw new ConfigurationException("Резервная копия не найдена");
            }
        } catch (IOException e) {
            throw new ConfigurationException("Не удалось восстановить конфигурацию: " + e.getMessage());
        }
    }
}
