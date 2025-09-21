package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;

import java.util.*;

/**
 * Number value with support for integers and decimals
 */
public class NumberValue implements DataValue {
    private Number value;
    
    public NumberValue(Number value) {
        this.value = value == null ? 0 : value;
    }
    
    @Override
    public ValueType getType() { return ValueType.NUMBER; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
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
    public String asString() { return value.toString(); }
    
    @Override
    public Number asNumber() { return value; }
    
    @Override
    public boolean asBoolean() { return value.doubleValue() != 0; }
    
    @Override
    public boolean isEmpty() { return value.equals(0); }
    
    @Override
    public boolean isValid() { return value != null && !Double.isNaN(value.doubleValue()); }
    
    @Override
    public String getDescription() { return "Number: " + value; }
    
    @Override
    public DataValue copy() { return new NumberValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}