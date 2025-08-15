package com.megacreative.exceptions;

/**
 * Базовое исключение для плагина MegaCreative
 */
public class MegaCreativeException extends RuntimeException {
    
    public MegaCreativeException(String message) {
        super(message);
    }
    
    public MegaCreativeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public MegaCreativeException(Throwable cause) {
        super(cause);
    }
}
