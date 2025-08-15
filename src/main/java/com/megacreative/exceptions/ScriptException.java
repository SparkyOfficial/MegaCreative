package com.megacreative.exceptions;

/**
 * Исключение для операций со скриптами
 */
public class ScriptException extends MegaCreativeException {
    
    public ScriptException(String message) {
        super(message);
    }
    
    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
