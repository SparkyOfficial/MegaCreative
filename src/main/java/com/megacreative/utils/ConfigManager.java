package com.megacreative.utils;

import com.megacreative.MegaCreative;
import com.megacreative.configs.WorldCode;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Менеджер конфигурации для плагина MegaCreative
 * Обрабатывает загрузку, сохранение и управление настройками плагина
 *
 * Configuration manager for MegaCreative plugin
 * Handles loading, saving and managing plugin settings
 *
 * Konfigurationsmanager für das MegaCreative-Plugin
 * Behandelt das Laden, Speichern und Verwalten von Plugin-Einstellungen
 */
public class ConfigManager {
    
    private final MegaCreative plugin;
    private FileConfiguration config;
    
    /**
     * Инициализирует менеджер конфигурации
     * @param plugin Экземпляр основного плагина
     *
     * Initializes the configuration manager
     * @param plugin Main plugin instance
     *
     * Initialisiert den Konfigurationsmanager
     * @param plugin Hauptplugin-Instanz
     */
    public ConfigManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Загружает конфигурацию плагина из файла config.yml
     * Устанавливает значения по умолчанию для всех параметров
     *
     * Loads plugin configuration from config.yml file
     * Sets default values for all parameters
     *
     * Lädt die Plugin-Konfiguration aus der config.yml-Datei
     * Setzt Standardwerte für alle Parameter
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        
        
        
        config.addDefault("worlds.maxPerPlayer", 5);
        config.addDefault("worlds.borderSize", 300);
        config.addDefault("worlds.autoSave", true);
        config.addDefault("worlds.autoSaveInterval", 300); 
        
        
        
        config.addDefault("messages.prefix", "§8[§bMegaCreative§8] ");
        config.addDefault("messages.noPermission", "§cУ вас нет прав на это действие!");
        
        
        config.addDefault("messages.worldNotFound", "§cМир не найден!");
        
        
        config.addDefault("messages.worldLimitReached", "§cВы достигли лимита миров!");
        
        
        config.addDefault("messages.worldCreated", "§aМир успешно создан!");
        
        
        config.addDefault("messages.worldDeleted", "§aМир успешно удален!");
        
        
        
        config.options().copyDefaults(true);
        plugin.saveConfig();
    }
    
    /**
     * Получает максимальное количество миров на игрока
     * @return Максимальное количество миров
     *
     * Gets the maximum number of worlds per player
     * @return Maximum number of worlds
     *
     * Ruft die maximale Anzahl von Welten pro Spieler ab
     * @return Maximale Anzahl von Welten
     */
    public int getMaxWorldsPerPlayer() {
        if (config == null) {
            return 5; 
            
            
        }
        return config.getInt("worlds.maxPerPlayer", 5);
    }
    
    /**
     * Получает размер границы мира
     * @return Размер границы мира
     *
     * Gets the world border size
     * @return World border size
     *
     * Ruft die Weltgrenzengröße ab
     * @return Weltgrenzengröße
     */
    public int getWorldBorderSize() {
        if (config == null) {
            return 300; 
            
            
        }
        return config.getInt("worlds.borderSize", 300);
    }
    
    /**
     * Проверяет, включено ли автоматическое сохранение
     * @return true если автоматическое сохранение включено, иначе false
     *
     * Checks if auto-save is enabled
     * @return true if auto-save is enabled, false otherwise
     *
     * Prüft, ob die automatische Speicherung aktiviert ist
     * @return true, wenn die automatische Speicherung aktiviert ist, sonst false
     */
    public boolean isAutoSaveEnabled() {
        if (config == null) {
            return true; 
            
            
        }
        return config.getBoolean("worlds.autoSave", true);
    }
    
    /**
     * Получает интервал автоматического сохранения в секундах
     * @return Интервал автоматического сохранения
     *
     * Gets the auto-save interval in seconds
     * @return Auto-save interval
     *
     * Ruft das Intervall der automatischen Speicherung in Sekunden ab
     * @return Intervall der automatischen Speicherung
     */
    public int getAutoSaveInterval() {
        if (config == null) {
            return 300; 
            
            
        }
        return config.getInt("worlds.autoSaveInterval", 300);
    }
    
    /**
     * Получает сообщение по ключу
     * @param key Ключ сообщения
     * @return Сообщение
     *
     * Gets message by key
     * @param key Message key
     * @return Message
     *
     * Ruft die Nachricht nach Schlüssel ab
     * @param key Nachrichtenschlüssel
     * @return Nachricht
     */
    public String getMessage(String key) {
        if (config == null) {
            return "§cСообщение не найдено: " + key; 
            
            
        }
        return config.getString("messages." + key, "§cСообщение не найдено: " + key);
    }
    
    /**
     * Получает префикс сообщений плагина
     * @return Префикс сообщений
     *
     * Gets the plugin message prefix
     * @return Message prefix
     *
     * Ruft das Nachrichtenpräfix des Plugins ab
     * @return Nachrichtenpräfix
     */
    public String getPrefix() {
        if (config == null) {
            return "§8[§bMegaCreative§8] "; 
            
            
        }
        return config.getString("messages.prefix", "§8[§bMegaCreative§8] ");
    }
    
    /**
     * Получает менеджер конфигурации WorldCode
     * @return Класс WorldCode для статического доступа
     *
     * Gets the WorldCode configuration manager
     * @return WorldCode class for static access
     *
     * Ruft den WorldCode-Konfigurationsmanager ab
     * @return WorldCode-Klasse für statischen Zugriff
     */
    public Class<WorldCode> getWorldCode() {
        return WorldCode.class; 
        
        
    }
    
    /**
     * Выключает менеджер конфигурации и очищает ресурсы
     *
     * Shuts down the config manager and cleans up resources
     *
     * Schaltet den Konfigurationsmanager aus und bereinigt Ressourcen
     */
    public void shutdown() {
        
        
        
        plugin.saveConfig();
        config = null;
    }
}