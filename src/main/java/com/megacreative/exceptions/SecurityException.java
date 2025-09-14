package com.megacreative.exceptions;

/**
 * Исключение для проблем с безопасностью
 *
 * Exception for security problems
 *
 * Ausnahme für Sicherheitsprobleme
 */
public class SecurityException extends MegaCreativeException {
    
    /**
     * Создает новое исключение безопасности с указанным сообщением
     * @param message Сообщение об ошибке
     *
     * Creates a new security exception with the specified message
     * @param message Error message
     *
     * Erstellt eine neue Sicherheitsausnahme mit der angegebenen Nachricht
     * @param message Fehlermeldung
     */
    public SecurityException(String message) {
        super(message);
    }
    
    /**
     * Создает новое исключение безопасности с указанным сообщением и причиной
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     *
     * Creates a new security exception with the specified message and cause
     * @param message Error message
     * @param cause Exception cause
     *
     * Erstellt eine neue Sicherheitsausnahme mit der angegebenen Nachricht und Ursache
     * @param message Fehlermeldung
     * @param cause Ausnahmeursache
     */
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}