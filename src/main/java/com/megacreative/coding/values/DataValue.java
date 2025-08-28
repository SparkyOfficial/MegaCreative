package com.megacreative.coding.values;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.Map;

/**
 * Advanced data value interface with serialization and validation
 * Provides type safety and automatic conversion between types
 */
public interface DataValue extends ConfigurationSerializable, Cloneable {
    
    /**
     * Gets the value type
     */
    ValueType getType();
    
    /**
     * Gets the raw value object
     */
    Object getValue();
    
    /**
     * Sets the raw value with validation
     */
    void setValue(Object value) throws IllegalArgumentException;
    
    /**
     * Converts this value to a string representation
     */
    String asString();
    
    /**
     * Converts this value to a number
     */
    Number asNumber() throws NumberFormatException;
    
    /**
     * Converts this value to a boolean
     */
    boolean asBoolean();
    
    /**
     * Checks if this value is null or empty
     */
    boolean isEmpty();
    
    /**
     * Validates the current value
     */
    boolean isValid();
    
    /**
     * Gets a human-readable description of this value
     */
    String getDescription();
    
    /**
     * Creates a deep copy of this value
     */
    DataValue clone();
    
    /**
     * Serializes this value to a map for storage
     */
    Map<String, Object> serialize();
    
    /**
     * Deserializes a value from a map
     */
    static DataValue deserialize(Map<String, Object> map) {
        ValueType type = ValueType.valueOf((String) map.get("type"));
        Object value = map.get("value");
        
        switch (type) {
            case TEXT: return new TextValue((String) value);
            case NUMBER: return new NumberValue((Number) value);
            case BOOLEAN: return new BooleanValue((Boolean) value);
            case LIST: return new ListValue((java.util.List<?>) value);
            case LOCATION: return new LocationValue((org.bukkit.Location) value);
            case ITEM: return new ItemValue((org.bukkit.inventory.ItemStack) value);
            case PLAYER: return new PlayerValue((org.bukkit.entity.Player) value);
            default: return new AnyValue(value);
        }
    }
    
    /**
     * Creates a value from an object with automatic type detection
     */
    static DataValue fromObject(Object object) {
        if (object == null) return new AnyValue(null);
        
        if (object instanceof String) return new TextValue((String) object);
        if (object instanceof Number) return new NumberValue((Number) object);
        if (object instanceof Boolean) return new BooleanValue((Boolean) object);
        if (object instanceof org.bukkit.Location) return new LocationValue((org.bukkit.Location) object);
        if (object instanceof org.bukkit.inventory.ItemStack) return new ItemValue((org.bukkit.inventory.ItemStack) object);
        if (object instanceof org.bukkit.entity.Player) return new PlayerValue((org.bukkit.entity.Player) object);
        if (object instanceof java.util.List) return new ListValue((java.util.List<?>) object);
        
        return new AnyValue(object);
    }
    
    /**
     * Converts between compatible types
     */
    static DataValue convert(DataValue value, ValueType targetType) {
        if (value.getType() == targetType) return value;
        if (!value.getType().isCompatible(targetType)) {
            throw new IllegalArgumentException("Cannot convert " + value.getType() + " to " + targetType);
        }
        
        switch (targetType) {
            case TEXT: return new TextValue(value.asString());
            case NUMBER: return new NumberValue(value.asNumber());
            case BOOLEAN: return new BooleanValue(value.asBoolean());
            default: return value;
        }
    }
}