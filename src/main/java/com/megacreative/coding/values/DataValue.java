package com.megacreative.coding.values;

import com.megacreative.coding.values.types.*;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.HashMap;
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
     * Gets the raw value object (alias for getValue)
     */
    default Object getRawValue() {
        return getValue();
    }
    
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
     * Checks if this value represents text
     * @return true if this value is a string or can be converted to a string
     */
    default boolean isText() {
        return getValue() instanceof String || getValue() instanceof CharSequence;
    }
    
    /**
     * Gets a human-readable description of this value
     */
    String getDescription();
    
    /**
     * Creates a deep copy of this value
     */
    DataValue clone();
    
    /**
     * Creates a DataValue from an object
     * @param value The value to convert
     * @return A new DataValue instance
     */
    static DataValue of(Object value) {
        return DataValue.fromObject(value);
    }
    
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
            case LIST: {
                java.util.List<?> rawList = (java.util.List<?>) value;
                java.util.List<DataValue> convertedList = new java.util.ArrayList<>();
                for (Object item : rawList) {
                    convertedList.add(fromObject(item));
                }
                return new ListValue(convertedList);
            }
            case DICTIONARY: return new MapValue((java.util.Map<String, DataValue>) value);
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
        if (object instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) object;
            java.util.List<DataValue> convertedList = new java.util.ArrayList<>();
            for (Object item : list) {
                convertedList.add(fromObject(item));
            }
            return new ListValue(convertedList);
        }
        if (object instanceof java.util.Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            Map<String, DataValue> convertedMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                DataValue value = fromObject(entry.getValue());
                convertedMap.put(key, value);
            }
            return new MapValue(convertedMap);
        }
        
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