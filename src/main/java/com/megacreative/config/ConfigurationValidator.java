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
 *
 * Configuration validator for MegaCreative plugin
 *
 * Konfigurationsvalidator für das MegaCreative-Plugin
 */
public class ConfigurationValidator {
    
    private final MegaCreative plugin;
    
    /**
     * Инициализирует валидатор конфигурации
     * @param plugin Экземпляр основного плагина
     *
     * Initializes configuration validator
     * @param plugin Main plugin instance
     *
     * Initialisiert den Konfigurationsvalidator
     * @param plugin Hauptplugin-Instanz
     */
    public ConfigurationValidator(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Валидирует основную конфигурацию
     *
     * Validates main configuration
     *
     * Validiert die Hauptkonfiguration
     */
    public void validateMainConfig() throws ConfigurationException {
        FileConfiguration config = plugin.getConfig();
        
        // Проверяем обязательные секции
        // Check required sections
        // Überprüfen Sie die erforderlichen Abschnitte
        if (!config.contains("worlds")) {
            throw new ConfigurationException("Отсутствует секция 'worlds' в конфигурации");
            // Missing 'worlds' section in configuration
            // Fehlender Abschnitt 'worlds' in der Konfiguration
        }
        
        if (!config.contains("coding")) {
            throw new ConfigurationException("Отсутствует секция 'coding' в конфигурации");
            // Missing 'coding' section in configuration
            // Fehlender Abschnitt 'coding' in der Konfiguration
        }
        
        // Проверяем настройки миров
        // Validate world settings
        // Welteneinstellungen validieren
        validateWorldSettings(config);
        
        // Проверяем настройки кодинга
        // Validate coding settings
        // Kodierungseinstellungen validieren
        validateCodingSettings(config);
        
        // Проверяем настройки безопасности
        // Validate security settings
        // Sicherheitseinstellungen validieren
        validateSecuritySettings(config);
    }
    
    /**
     * Валидирует настройки миров
     * @param config Конфигурация для валидации
     *
     * Validates world settings
     * @param config Configuration to validate
     *
     * Validiert die Welteinstellungen
     * @param config Zu validierende Konfiguration
     */
    private void validateWorldSettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("worlds.max_worlds_per_player")) {
            throw new ConfigurationException("Отсутствует настройка 'worlds.max_worlds_per_player'");
            // Missing setting 'worlds.max_worlds_per_player'
            // Fehlende Einstellung 'worlds.max_worlds_per_player'
        }
        
        int maxWorlds = config.getInt("worlds.max_worlds_per_player");
        if (maxWorlds <= 0 || maxWorlds > 100) {
            throw new ConfigurationException("Некорректное значение 'worlds.max_worlds_per_player': " + maxWorlds);
            // Incorrect value 'worlds.max_worlds_per_player':
            // Falscher Wert 'worlds.max_worlds_per_player':
        }
        
        if (!config.contains("worlds.world_border_size")) {
            throw new ConfigurationException("Отсутствует настройка 'worlds.world_border_size'");
            // Missing setting 'worlds.world_border_size'
            // Fehlende Einstellung 'worlds.world_border_size'
        }
        
        int borderSize = config.getInt("worlds.world_border_size");
        if (borderSize <= 0 || borderSize > 10000) {
            throw new ConfigurationException("Некорректное значение 'worlds.world_border_size': " + borderSize);
            // Incorrect value 'worlds.world_border_size':
            // Falscher Wert 'worlds.world_border_size':
        }
    }
    
    /**
     * Валидирует настройки кодинга
     * @param config Конфигурация для валидации
     *
     * Validates coding settings
     * @param config Configuration to validate
     *
     * Validiert die Kodierungseinstellungen
     * @param config Zu validierende Konfiguration
     */
    private void validateCodingSettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("coding.max_script_size")) {
            throw new ConfigurationException("Отсутствует настройка 'coding.max_script_size'");
            // Missing setting 'coding.max_script_size'
            // Fehlende Einstellung 'coding.max_script_size'
        }
        
        int maxScriptSize = config.getInt("coding.max_script_size");
        if (maxScriptSize <= 0 || maxScriptSize > 1000) {
            throw new ConfigurationException("Некорректное значение 'coding.max_script_size': " + maxScriptSize);
            // Incorrect value 'coding.max_script_size':
            // Falscher Wert 'coding.max_script_size':
        }
        
        if (!config.contains("coding.max_execution_time")) {
            throw new ConfigurationException("Отсутствует настройка 'coding.max_execution_time'");
            // Missing setting 'coding.max_execution_time'
            // Fehlende Einstellung 'coding.max_execution_time'
        }
        
        int maxExecutionTime = config.getInt("coding.max_execution_time");
        if (maxExecutionTime <= 0 || maxExecutionTime > 30000) {
            throw new ConfigurationException("Некорректное значение 'coding.max_execution_time': " + maxExecutionTime);
            // Incorrect value 'coding.max_execution_time':
            // Falscher Wert 'coding.max_execution_time':
        }
    }
    
    /**
     * Валидирует настройки безопасности
     * @param config Конфигурация для валидации
     *
     * Validates security settings
     * @param config Configuration to validate
     *
     * Validiert die Sicherheitseinstellungen
     * @param config Zu validierende Konfiguration
     */
    private void validateSecuritySettings(FileConfiguration config) throws ConfigurationException {
        if (!config.contains("security.allowed_commands")) {
            throw new ConfigurationException("Отсутствует настройка 'security.allowed_commands'");
            // Missing setting 'security.allowed_commands'
            // Fehlende Einstellung 'security.allowed_commands'
        }
        
        List<String> allowedCommands = config.getStringList("security.allowed_commands");
        if (allowedCommands.isEmpty()) {
            throw new ConfigurationException("Список разрешенных команд пуст");
            // List of allowed commands is empty
            // Liste der erlaubten Befehle ist leer
        }
        
        // Проверяем, что нет опасных команд
        // Check that there are no dangerous commands
        // Prüfen Sie, ob keine gefährlichen Befehle vorhanden sind
        for (String command : allowedCommands) {
            if (isDangerousCommand(command)) {
                throw new ConfigurationException("Обнаружена опасная команда в списке разрешенных: " + command);
                // Dangerous command found in allowed list:
                // Gefährlicher Befehl in der erlaubten Liste gefunden:
            }
        }
    }
    
    /**
     * Проверяет, является ли команда опасной
     * @param command Команда для проверки
     * @return true если команда опасная, иначе false
     *
     * Checks if command is dangerous
     * @param command Command to check
     * @return true if command is dangerous, false otherwise
     *
     * Prüft, ob der Befehl gefährlich ist
     * @param command Zu prüfender Befehl
     * @return true, wenn der Befehl gefährlich ist, sonst false
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
     *
     * Validates coding blocks configuration file
     *
     * Validiert die Konfigurationsdatei für Codierungsblöcke
     */
    public void validateCodingBlocksConfig() throws ConfigurationException {
        File codingBlocksFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!codingBlocksFile.exists()) {
            throw new ConfigurationException("Файл coding_blocks.yml не найден");
            // File coding_blocks.yml not found
            // Datei coding_blocks.yml nicht gefunden
        }
        
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(codingBlocksFile);
            
            if (!config.contains("blocks")) {
                throw new ConfigurationException("Отсутствует секция 'blocks' в coding_blocks.yml");
                // Missing 'blocks' section in coding_blocks.yml
                // Fehlender Abschnitt 'blocks' in coding_blocks.yml
            }
            
            // Проверяем, что есть хотя бы несколько блоков
            // Check that there are at least some blocks
            // Prüfen Sie, ob es mindestens einige Blöcke gibt
            if (config.getConfigurationSection("blocks") == null) {
                throw new ConfigurationException("Секция 'blocks' пуста в coding_blocks.yml");
                // Section 'blocks' is empty in coding_blocks.yml
                // Abschnitt 'blocks' ist in coding_blocks.yml leer
            }
            
        } catch (Exception e) {
            throw new ConfigurationException("Ошибка чтения coding_blocks.yml: " + e.getMessage());
            // Error reading coding_blocks.yml:
            // Fehler beim Lesen von coding_blocks.yml:
        }
    }
    
    /**
     * Создает резервную копию конфигурации
     *
     * Creates configuration backup
     *
     * Erstellt eine Konfigurationssicherung
     */
    public void createBackup() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup");
            
            if (configFile.exists()) {
                java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Создана резервная копия конфигурации");
                // Configuration backup created
                // Konfigurationssicherung erstellt
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать резервную копию конфигурации: " + e.getMessage());
            // Failed to create configuration backup:
            // Fehler beim Erstellen der Konfigurationssicherung:
        }
    }
    
    /**
     * Восстанавливает конфигурацию из резервной копии
     *
     * Restores configuration from backup
     *
     * Stellt die Konfiguration aus der Sicherung wieder her
     */
    public void restoreFromBackup() throws ConfigurationException {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            File backupFile = new File(plugin.getDataFolder(), "config.yml.backup");
            
            if (backupFile.exists()) {
                java.nio.file.Files.copy(backupFile.toPath(), configFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Конфигурация восстановлена из резервной копии");
                // Configuration restored from backup
                // Konfiguration aus Sicherung wiederhergestellt
            } else {
                throw new ConfigurationException("Резервная копия не найдена");
                // Backup not found
                // Sicherung nicht gefunden
            }
        } catch (IOException e) {
            throw new ConfigurationException("Не удалось восстановить конфигурацию: " + e.getMessage());
            // Failed to restore configuration:
            // Fehler beim Wiederherstellen der Konfiguration:
        }
    }
}