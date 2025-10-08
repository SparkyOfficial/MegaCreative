package com.megacreative.coding.script;

/**
 * Represents the type of a script block.
 * This enum defines the different types of blocks that can be used in the visual script editor.
 */
public enum ScriptBlockType {
    /**
     * A condition block that controls script flow based on a condition
     */
    CONDITION,
    
    /**
     * An action block that performs some action when executed
     */
    ACTION,
    
    /**
     * A trigger block that starts a script when a specific event occurs
     */
    TRIGGER,
    
    /**
     * A variable block that stores and manages script variables
     */
    VARIABLE,
    
    /**
     * A loop block that repeats a section of the script
     */
    LOOP,
    
    /**
     * A function block that defines a reusable section of script
     */
    FUNCTION
}