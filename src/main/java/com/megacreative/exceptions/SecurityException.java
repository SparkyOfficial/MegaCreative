package com.megacreative.exceptions;

/**
 * Исключение для проблем с безопасностью
 */
public class SecurityException extends MegaCreativeException {
    
    public SecurityException(String message) {
        super(message);
    }
    
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
