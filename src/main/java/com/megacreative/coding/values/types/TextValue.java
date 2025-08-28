package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;

import java.util.*;

/**
 * Text value implementation with advanced string operations
 */
public class TextValue implements DataValue {
    private String value;
    
    public TextValue(String value) {
        this.value = value == null ? "" : value;
    }
    
    @Override
    public ValueType getType() { return ValueType.TEXT; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        this.value = value == null ? "" : value.toString();
    }
    
    @Override
    public String asString() { return value; }
    
    @Override
    public Number asNumber() throws NumberFormatException {
        try {
            if (value.contains(".")) return Double.parseDouble(value);
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Cannot convert '" + value + "' to number");
        }
    }
    
    @Override
    public boolean asBoolean() {
        return Boolean.parseBoolean(value) || value.equalsIgnoreCase("yes") || 
               value.equalsIgnoreCase("да") || value.equals("1");
    }
    
    @Override
    public boolean isEmpty() { return value.isEmpty(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    public String getDescription() { return "Text: \"" + value + "\""; }
    
    @Override
    public DataValue clone() { return new TextValue(value); }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}