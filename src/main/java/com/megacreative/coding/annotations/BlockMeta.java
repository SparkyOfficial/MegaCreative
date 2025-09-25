package com.megacreative.coding.annotations;

import com.megacreative.coding.BlockType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking block actions and conditions with metadata.
 * This allows for automatic registration and discovery of block implementations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BlockMeta {
    /**
     * Unique identifier for the block
     * @return the block ID
     */
    String id();
    
    /**
     * Display name for the block
     * @return the display name
     */
    String displayName();
    
    /**
     * Type of the block
     * @return the block type
     */
    BlockType type();
}