package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;

import java.util.*;

/**
 * Data value representing a list/array of values
 * Supports operations like add, remove, get, size, etc.
 */
public class ListValue implements DataValue {
    
    private final List<DataValue> values;
    
    public ListValue() {
        this.values = new ArrayList<>();
    }
    
    public ListValue(List<DataValue> values) {
        this.values = new ArrayList<>(values);
    }
    
    
    @SuppressWarnings("unchecked")
    public static ListValue fromRawList(List<?> rawValues) {
        List<DataValue> convertedValues = new ArrayList<>();
        if (rawValues != null) {
            for (Object obj : rawValues) {
                if (obj instanceof DataValue) {
                    convertedValues.add((DataValue) obj);
                } else {
                    convertedValues.add(DataValue.fromObject(obj));
                }
            }
        }
        return new ListValue(convertedValues);
    }
    
    @Override
    public ValueType getType() {
        return ValueType.LIST;
    }
    
    @Override
    public Object getValue() {
        return values;
    }
    
    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        values.clear();
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            for (Object obj : list) {
                if (obj instanceof DataValue) {
                    values.add((DataValue) obj);
                } else {
                    values.add(DataValue.fromObject(obj));
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot set list from: " + value);
        }
    }
    
    /**
     * Adds a value to the end of the list
     */
    public void add(DataValue value) {
        values.add(value);
    }
    
    /**
     * Adds a value at a specific index
     */
    public void add(int index, DataValue value) {
        if (index >= 0 && index <= values.size()) {
            values.add(index, value);
        }
    }
    
    /**
     * Removes a value at a specific index
     */
    public DataValue remove(int index) {
        if (index >= 0 && index < values.size()) {
            return values.remove(index);
        }
        return null;
    }
    
    /**
     * Removes the first occurrence of a value
     */
    public boolean remove(DataValue value) {
        return values.remove(value);
    }
    
    /**
     * Gets a value at a specific index
     */
    public DataValue get(int index) {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        }
        return null;
    }
    
    /**
     * Sets a value at a specific index
     */
    public void set(int index, DataValue value) {
        if (index >= 0 && index < values.size()) {
            values.set(index, value);
        }
    }
    
    /**
     * Gets the size of the list
     */
    public int size() {
        return values.size();
    }
    
    /**
     * Checks if the list contains a value
     */
    public boolean contains(DataValue value) {
        return values.contains(value);
    }
    
    /**
     * Clears all values from the list
     */
    public void clear() {
        values.clear();
    }
    
    /**
     * Gets the index of the first occurrence of a value
     */
    public int indexOf(DataValue value) {
        return values.indexOf(value);
    }
    
    /**
     * Gets a copy of the internal list
     */
    public List<DataValue> getValues() {
        return new ArrayList<>(values);
    }
    
    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i).asString());
        }
        sb.append("]");
        return sb.toString();
    }
    
    @Override
    public Number asNumber() {
        return size();
    }
    
    @Override
    public boolean asBoolean() {
        return !isEmpty();
    }
    
    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    @Override
    public boolean isValid() {
        // values != null is always true since values is initialized in constructor
        // The check has been simplified as the null check is redundant
        return true;
    }
    
    @Override
    public String getDescription() {
        return "List with " + size() + " elements";
    }
    
    @Override
    public DataValue copy() {
        List<DataValue> copiedValues = new ArrayList<>();
        for (DataValue value : values) {
            copiedValues.add(value.copy());
        }
        return new ListValue(copiedValues);
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("type", getType().name());
        List<Map<String, Object>> serializedValues = new ArrayList<>();
        for (DataValue value : values) {
            serializedValues.add(value.serialize());
        }
        result.put("value", serializedValues);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ListValue listValue = (ListValue) obj;
        return Objects.equals(values, listValue.values);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
    
    @Override
    public String toString() {
        return "ListValue{size=" + size() + ", values=" + asString() + "}";
    }
}