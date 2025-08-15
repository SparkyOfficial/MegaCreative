package com.megacreative.exceptions;

/**
 * Исключение для проблем с конфигурацией
 */
public class ConfigurationException extends MegaCreativeException {
    
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
