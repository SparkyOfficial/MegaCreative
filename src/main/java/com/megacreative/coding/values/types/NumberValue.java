package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Number value with support for integers and decimals
 */
public class NumberValue implements DataValue {
    private Number value;
    
    public NumberValue(@NotNull Number value) {
        this.value = value == null ? 0 : value;
    }
    
    @Override
    @NotNull
    public ValueType getType() { return ValueType.NUMBER; }
    
    @Override
    @NotNull
    public Object getValue() { return value; }
    
    @Override
    public void setValue(@NotNull Object value) {
        if (value instanceof Number) {
            this.value = (Number) value;
        } else if (value instanceof String) {
            try {
                this.value = Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number: " + value);
            }
        } else {
            throw new IllegalArgumentException("Cannot set number from: " + value);
        }
    }
    
    @Override
    @NotNull
    public String asString() { return value.toString(); }
    
    @Override
    @NotNull
    public Number asNumber() { return value; }
    
    @Override
    public boolean asBoolean() { return value.doubleValue() != 0; }
    
    @Override
    public boolean isEmpty() { 
        // Properly check for null values and zero values
        // Static analysis flagged this as always false, but null checks are necessary for robustness
        return value == null || value.equals(0); 
    }
    
    @Override
    public boolean isValid() { return value != null && !Double.isNaN(value.doubleValue()); }
    
    @Override
    @NotNull
    public String getDescription() { return "Number: " + value; }
    
    @Override
    @NotNull
    public DataValue copy() { return new NumberValue(value); }
    
    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}