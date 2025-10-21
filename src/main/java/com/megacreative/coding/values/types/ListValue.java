package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.jetbrains.annotations.NotNull;

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
    public static @NotNull ListValue fromRawList(@NotNull List<?> rawValues) {
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
    @NotNull
    public ValueType getType() {
        return ValueType.LIST;
    }
    
    @Override
    @NotNull
    public Object getValue() {
        return values;
    }
    
    @Override
    public void setValue(@NotNull Object value) throws IllegalArgumentException {
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
    public void add(@NotNull DataValue value) {
        values.add(value);
    }
    
    /**
     * Adds a value at a specific index
     */
    public void add(int index, @NotNull DataValue value) {
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
    public boolean remove(@NotNull DataValue value) {
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
    public void set(int index, @NotNull DataValue value) {
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
    public boolean contains(@NotNull DataValue value) {
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
    public int indexOf(@NotNull DataValue value) {
        return values.indexOf(value);
    }
    
    /**
     * Gets a copy of the internal list
     */
    public @NotNull List<DataValue> getValues() {
        return new ArrayList<>(values);
    }
    
    @Override
    @NotNull
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
    @NotNull
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
        // TODO: Properly implement isValid for list values
        // This is a placeholder implementation that needs improvement
        // For list values, isValid might need more complex validation logic
        // Properly implement isValid for list values
        // Static analysis flagged this as always true, but we're checking if the list is not empty
        // The check has been simplified as the null check is redundant for final fields
        return !values.isEmpty();
    }
    
    @Override
    @NotNull
    public String getDescription() {
        return "List with " + size() + " elements";
    }
    
    @Override
    @NotNull
    public DataValue copy() {
        List<DataValue> copiedValues = new ArrayList<>();
        for (DataValue value : values) {
            copiedValues.add(value.copy());
        }
        return new ListValue(copiedValues);
    }
    
    @Override
    @NotNull
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