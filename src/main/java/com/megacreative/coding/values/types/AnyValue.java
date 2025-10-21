package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Any value - accepts any object type
 */
public class AnyValue implements DataValue, Cloneable {
    private Object value;
    
    public AnyValue(@NotNull Object value) {
        this.value = value;
    }
    
    @Override
    @NotNull
    public ValueType getType() { return ValueType.ANY; }
    
    @Override
    @NotNull
    public Object getValue() { return value; }
    
    @Override
    public void setValue(@NotNull Object value) { this.value = value; }
    
    @Override
    @NotNull
    public String asString() { return value != null ? value.toString() : "null"; }
    
    @Override
    @NotNull
    public Number asNumber() {
        if (value instanceof Number) return (Number) value;
        try {
            return Double.parseDouble(asString());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot convert to number: " + value);
        }
    }
    
    @Override
    public boolean asBoolean() {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        return value != null && !asString().isEmpty();
    }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return true; }
    
    @Override
    @NotNull
    public String getDescription() { 
        String type = value != null ? value.getClass().getSimpleName() : "null";
        return "Any (" + type + "): " + asString(); 
    }
    
    @Override
    @NotNull
    public DataValue clone() {
        try {
            
            AnyValue cloned = (AnyValue) super.clone();
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    @Override
    @NotNull
    public DataValue copy() {
        return clone();
    }
    
    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}