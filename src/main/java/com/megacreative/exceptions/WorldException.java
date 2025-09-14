package com.megacreative.exceptions;

/**
 * Исключение для операций с мирами
 *
 * Exception for world operations
 *
 * Ausnahme für Weltenoperationen
 */
public class WorldException extends MegaCreativeException {
    
    /**
     * Создает новое исключение мира с указанным сообщением
     * @param message Сообщение об ошибке
     *
     * Creates a new world exception with the specified message
     * @param message Error message
     *
     * Erstellt eine neue Welt-Ausnahme mit der angegebenen Nachricht
     * @param message Fehlermeldung
     */
    public WorldException(String message) {
        super(message);
    }
    
    /**
     * Создает новое исключение мира с указанным сообщением и причиной
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     *
     * Creates a new world exception with the specified message and cause
     * @param message Error message
     * @param cause Exception cause
     *
     * Erstellt eine neue Welt-Ausnahme mit der angegebenen Nachricht und Ursache
     * @param message Fehlermeldung
     * @param cause Ausnahmeursache
     */
    public WorldException(String message, Throwable cause) {
        super(message, cause);
    }
}