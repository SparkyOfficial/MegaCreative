package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;

import java.util.*;

/**
 * List value with type-safe operations
 */
public class ListValue implements DataValue {
    private List<DataValue> value;
    
    public ListValue(List<?> value) {
        this.value = new ArrayList<>();
        if (value != null) {
            for (Object item : value) {
                this.value.add(DataValue.fromObject(item));
            }
        }
    }
    
    @Override
    public ValueType getType() { return ValueType.LIST; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof List) {
            this.value = new ArrayList<>();
            for (Object item : (List<?>) value) {
                this.value.add(DataValue.fromObject(item));
            }
        } else {
            throw new IllegalArgumentException("Cannot set list from: " + value);
        }
    }
    
    @Override
    public String asString() { 
        return "[" + value.size() + " items]";
    }
    
    @Override
    public Number asNumber() { return value.size(); }
    
    @Override
    public boolean asBoolean() { return !value.isEmpty(); }
    
    @Override
    public boolean isEmpty() { return value.isEmpty(); }
    
    @Override
    public boolean isValid() { return value != null; }
    
    @Override
    public String getDescription() { return "List: " + asString(); }
    
    @Override
    public DataValue clone() { 
        List<Object> cloned = new ArrayList<>();
        for (DataValue item : value) {
            cloned.add(item.getValue());
        }
        return new ListValue(cloned);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        
        List<Map<String, Object>> serializedItems = new ArrayList<>();
        for (DataValue item : value) {
            serializedItems.add(item.serialize());
        }
        map.put("value", serializedItems);
        return map;
    }
    
    // List-specific methods
    public void add(DataValue item) { value.add(item); }
    public void remove(int index) { value.remove(index); }
    public DataValue get(int index) { return value.get(index); }
    public int size() { return value.size(); }
}