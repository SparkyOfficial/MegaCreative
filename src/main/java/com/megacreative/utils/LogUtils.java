package com.megacreative.utils;

import com.megacreative.MegaCreative;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Утилитный класс для структурированного логирования в MegaCreative
 *
 * Utility class for structured logging in MegaCreative
 *
 * Utility-Klasse für strukturiertes Logging in MegaCreative
 */
public class LogUtils {
    
    private static final Logger logger = Logger.getLogger("MegaCreative");
    private static MegaCreative plugin;
    
    /**
     * Инициализирует утилиту логирования с экземпляром плагина
     * @param megaCreative Экземпляр основного плагина
     *
     * Initializes the logging utility with plugin instance
     * @param megaCreative Main plugin instance
     *
     * Initialisiert das Logging-Dienstprogramm mit der Plugin-Instanz
     * @param megaCreative Hauptplugin-Instanz
     */
    public static void initialize(MegaCreative megaCreative) {
        plugin = megaCreative;
    }
    
    /**
     * Логирует информационное сообщение
     * @param message Сообщение для логирования
     *
     * Logs an informational message
     * @param message Message to log
     *
     * Protokolliert eine Informationsmeldung
     * @param message Zu protokollierende Nachricht
     */
    public static void info(String message) {
        if (plugin != null) {
            plugin.getLogger().info("[INFO] " + message);
        } else {
            logger.info("[INFO] " + message);
        }
    }
    
    /**
     * Логирует предупреждение
     * @param message Сообщение предупреждения
     *
     * Logs a warning message
     * @param message Warning message
     *
     * Protokolliert eine Warnmeldung
     * @param message Warnmeldung
     */
    public static void warning(String message) {
        if (plugin != null) {
            plugin.getLogger().warning("[WARNING] " + message);
        } else {
            logger.warning("[WARNING] " + message);
        }
    }
    
    /**
     * Логирует ошибку
     * @param message Сообщение об ошибке
     *
     * Logs an error message
     * @param message Error message
     *
     * Protokolliert eine Fehlermeldung
     * @param message Fehlermeldung
     */
    public static void error(String message) {
        if (plugin != null) {
            plugin.getLogger().severe("[ERROR] " + message);
        } else {
            logger.severe("[ERROR] " + message);
        }
    }
    
    /**
     * Логирует ошибку с исключением
     * @param message Сообщение об ошибке
     * @param throwable Исключение для логирования
     *
     * Logs an error with exception
     * @param message Error message
     * @param throwable Exception to log
     *
     * Protokolliert einen Fehler mit Ausnahme
     * @param message Fehlermeldung
     * @param throwable Zu protokollierende Ausnahme
     */
    public static void error(String message, Throwable throwable) {
        if (plugin != null) {
            plugin.getLogger().log(Level.SEVERE, "[ERROR] " + message, throwable);
        } else {
            logger.log(Level.SEVERE, "[ERROR] " + message, throwable);
        }
    }
    
    /**
     * Логирует отладочную информацию
     * @param message Отладочное сообщение
     *
     * Logs debug information
     * @param message Debug message
     *
     * Protokolliert Debug-Informationen
     * @param message Debug-Nachricht
     */
    public static void debug(String message) {
        if (plugin != null) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Логирует информацию о производительности
     * @param message Сообщение о производительности
     *
     * Logs performance information
     * @param message Performance message
     *
     * Protokolliert Leistungsinformationen
     * @param message Leistungsnachricht
     */
    public static void performance(String message) {
        if (plugin != null) {
            plugin.getLogger().info("[PERFORMANCE] " + message);
        }
    }
    
    /**
     * Логирует информацию о безопасности
     * @param message Сообщение о безопасности
     *
     * Logs security information
     * @param message Security message
     *
     * Protokolliert Sicherheitsinformationen
     * @param message Sicherheitsnachricht
     */
    public static void security(String message) {
        if (plugin != null) {
            plugin.getLogger().warning("[SECURITY] " + message);
        } else {
            logger.warning("[SECURITY] " + message);
        }
    }
}