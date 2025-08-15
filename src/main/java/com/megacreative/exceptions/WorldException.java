package com.megacreative.exceptions;

/**
 * Исключение для операций с мирами
 */
public class WorldException extends MegaCreativeException {
    
    public WorldException(String message) {
        super(message);
    }
    
    public WorldException(String message, Throwable cause) {
        super(message, cause);
    }
}
