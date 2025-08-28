package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;

import java.util.*;

/**
 * Boolean value implementation
 */
public class BooleanValue implements DataValue {
    private boolean value;
    
    public BooleanValue(boolean value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.BOOLEAN; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Boolean) {
            this.value = (Boolean) value;
        } else if (value instanceof String) {
            String str = (String) value;
            this.value = Boolean.parseBoolean(str) || str.equalsIgnoreCase("yes") || 
                        str.equalsIgnoreCase("да") || str.equals("1");
        } else if (value instanceof Number) {
            this.value = ((Number) value).doubleValue() != 0;
        } else {
            this.value = value != null;
        }
    }
    
    @Override
    public String asString() { return value ? "true" : "false"; }
    
    @Override
    public Number asNumber() { return value ? 1 : 0; }
    
    @Override
    public boolean asBoolean() { return value; }
    
    @Override
    public boolean isEmpty() { return !value; }
    
    @Override
    public boolean isValid() { return true; }
    
    @Override
    public String getDescription() { return "Boolean: " + value; }
    
    @Override
    public DataValue clone() { return new BooleanValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}