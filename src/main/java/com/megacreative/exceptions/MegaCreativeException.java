package com.megacreative.exceptions;

/**
 * Базовое исключение для плагина MegaCreative
 *
 * Base exception for MegaCreative plugin
 *
 * Basisausnahme für das MegaCreative-Plugin
 */
public class MegaCreativeException extends RuntimeException {
    
    /**
     * Создает новое исключение MegaCreative с указанным сообщением
     * @param message Сообщение об ошибке
     *
     * Creates a new MegaCreative exception with the specified message
     * @param message Error message
     *
     * Erstellt eine neue MegaCreative-Ausnahme mit der angegebenen Nachricht
     * @param message Fehlermeldung
     */
    public MegaCreativeException(String message) {
        super(message);
    }
    
    /**
     * Создает новое исключение MegaCreative с указанным сообщением и причиной
     * @param message Сообщение об ошибке
     * @param cause Причина исключения
     *
     * Creates a new MegaCreative exception with the specified message and cause
     * @param message Error message
     * @param cause Exception cause
     *
     * Erstellt eine neue MegaCreative-Ausnahme mit der angegebenen Nachricht und Ursache
     * @param message Fehlermeldung
     * @param cause Ausnahmeursache
     */
    public MegaCreativeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Создает новое исключение MegaCreative с указанной причиной
     * @param cause Причина исключения
     *
     * Creates a new MegaCreative exception with the specified cause
     * @param cause Exception cause
     *
     * Erstellt eine neue MegaCreative-Ausnahme mit der angegebenen Ursache
     * @param cause Ausnahmeursache
     */
    public MegaCreativeException(Throwable cause) {
        super(cause);
    }
}