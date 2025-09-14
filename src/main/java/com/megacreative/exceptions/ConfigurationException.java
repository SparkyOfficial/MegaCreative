package com.megacreative.exceptions;

/**
 * Исключение для проблем с конфигурацией
 *
 * Exception for configuration problems
 *
 * Ausnahme für Konfigurationsprobleme
 */
public class ConfigurationException extends MegaCreativeException {
    
    /**
     * Создает новое исключение конфигурации с указанным сообщением
     * @param message Сообщение об ошибке
     *
     * Creates a new configuration exception with the specified message
     * @param message Error message
     *
     * Erstellt eine neue Konfigurationsausnahme mit der angegebenen Nachricht
     * @param message Fehlermeldung
     */
    public ConfigurationException(String message) {
        super(message);
    }
    
    /**
     * Создает новое исключение конфигурации с указанным сообщением и причиной
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     *
     * Creates a new configuration exception with the specified message and cause
     * @param message Error message
     * @param cause Exception cause
     *
     * Erstellt eine neue Konfigurationsausnahme mit der angegebenen Nachricht und Ursache
     * @param message Fehlermeldung
     * @param cause Ausnahmeursache
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}