package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Text value implementation with advanced string operations
 */
public class TextValue implements DataValue {
    private String value;
    
    public TextValue(@NotNull String value) {
        // value is never null due to @NotNull annotation
        this.value = value;
    }
    
    @Override
    @NotNull
    public ValueType getType() { return ValueType.TEXT; }
    
    @Override
    @NotNull
    public Object getValue() { return value; }
    
    @Override
    public void setValue(@NotNull Object value) {
        // value is never null due to @NotNull annotation
        this.value = value.toString();
    }
    
    @Override
    @NotNull
    public String asString() { return value; }
    
    @Override
    @NotNull
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
    public boolean isValid() { return true; }
    
    @Override
    @NotNull
    public String getDescription() { return "Text: \"" + value + "\""; }
    
    /**
     * Creates a copy of this TextValue
     * @return A new TextValue with the same value
     */
    @Override
    @NotNull
    public DataValue copy() { 
        return new TextValue(value); 
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