package com.megacreative.exceptions;

/**
 * Исключение для операций со скриптами
 *
 * Exception for script operations
 *
 * Ausnahme für Skriptoperationen
 */
public class ScriptException extends MegaCreativeException {
    
    /**
     * Создает новое исключение скрипта с указанным сообщением
     * @param message Сообщение об ошибке
     *
     * Creates a new script exception with the specified message
     * @param message Error message
     *
     * Erstellt eine neue Skriptausnahme mit der angegebenen Nachricht
     * @param message Fehlermeldung
     */
    public ScriptException(String message) {
        super(message);
    }
    
    /**
     * Создает новое исключение скрипта с указанным сообщением и причиной
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     *
     * Creates a new script exception with the specified message and cause
     * @param message Error message
     * @param cause Exception cause
     *
     * Erstellt eine neue Skriptausnahme mit der angegebenen Nachricht und Ursache
     * @param message Fehlermeldung
     * @param cause Ausnahmeursache
     */
    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}