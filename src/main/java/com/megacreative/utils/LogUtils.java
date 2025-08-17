package com.megacreative.utils;

import com.megacreative.MegaCreative;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Утилитный класс для структурированного логирования в MegaCreative
 */
public class LogUtils {
    
    private static final Logger logger = Logger.getLogger("MegaCreative");
    private static MegaCreative plugin;
    
    public static void initialize(MegaCreative megaCreative) {
        plugin = megaCreative;
    }
    
    /**
     * Логирует информационное сообщение
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
     */
    public static void debug(String message) {
        if (plugin != null) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Логирует информацию о производительности
     */
    public static void performance(String message) {
        if (plugin != null) {
            plugin.getLogger().info("[PERFORMANCE] " + message);
        }
    }
    
    /**
     * Логирует информацию о безопасности
     */
    public static void security(String message) {
        if (plugin != null) {
            plugin.getLogger().warning("[SECURITY] " + message);
        } else {
            logger.warning("[SECURITY] " + message);
        }
    }
}
