package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Data value representing a map/dictionary of key-value pairs
 * Supports operations like put, get, remove, contains, etc.
 */
public class MapValue implements DataValue {
    
    private final Map<String, DataValue> values;
    
    public MapValue() {
        this.values = new HashMap<>();
    }
    
    public MapValue(@NotNull Map<String, DataValue> values) {
        this.values = new HashMap<>(values);
    }
    
    @Override
    @NotNull
    public ValueType getType() {
        return ValueType.DICTIONARY;
    }
    
    @Override
    @NotNull
    public Object getValue() {
        return values;
    }
    
    @Override
    public void setValue(@NotNull Object value) throws IllegalArgumentException {
        values.clear();
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = entry.getKey().toString();
                Object val = entry.getValue();
                if (val instanceof DataValue) {
                    values.put(key, (DataValue) val);
                } else {
                    values.put(key, DataValue.fromObject(val));
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot set map from: " + value);
        }
    }
    
    /**
     * Puts a key-value pair into the map
     */
    public DataValue put(@NotNull String key, @NotNull DataValue value) {
        return values.put(key, value);
    }
    
    /**
     * Gets a value by key
     */
    public DataValue get(@NotNull String key) {
        return values.get(key);
    }
    
    /**
     * Removes a key-value pair by key
     */
    public DataValue remove(@NotNull String key) {
        return values.remove(key);
    }
    
    /**
     * Checks if the map contains a key
     */
    public boolean containsKey(@NotNull String key) {
        return values.containsKey(key);
    }
    
    /**
     * Checks if the map contains a value
     */
    public boolean containsValue(@NotNull DataValue value) {
        return values.containsValue(value);
    }
    
    /**
     * Gets the size of the map
     */
    public int size() {
        return values.size();
    }
    
    /**
     * Checks if the map is empty
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }
    
    /**
     * Clears all key-value pairs from the map
     */
    public void clear() {
        values.clear();
    }
    
    /**
     * Gets all keys in the map
     */
    @NotNull
    public Set<String> keySet() {
        return values.keySet();
    }
    
    /**
     * Gets a copy of the internal map
     */
    @NotNull
    public Map<String, DataValue> getValues() {
        return new HashMap<>(values);
    }
    
    /**
     * Merges another map into this one
     */
    public void putAll(@NotNull MapValue other) {
        values.putAll(other.values);
    }
    
    /**
     * Gets a value with a default if key doesn't exist
     */
    @NotNull
    public DataValue getOrDefault(@NotNull String key, @NotNull DataValue defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }
    
    @Override
    @NotNull
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, DataValue> entry : values.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"")
              .append(entry.getKey())
              .append("\": ")
              .append(entry.getValue().asString());
            first = false;
        }
        sb.append("}");
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
    public boolean isValid() {
        return true; // values is never null as it's initialized in constructor
    }
    
    @Override
    @NotNull
    public String getDescription() {
        return "Map with " + size() + " key-value pairs";
    }
    
    @Override
    @NotNull
    public DataValue copy() {
        Map<String, DataValue> copiedValues = new HashMap<>();
        for (Map.Entry<String, DataValue> entry : values.entrySet()) {
            copiedValues.put(entry.getKey(), entry.getValue().copy());
        }
        return new MapValue(copiedValues);
    }
    
    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("type", getType().name());
        Map<String, Map<String, Object>> serializedValues = new HashMap<>();
        for (Map.Entry<String, DataValue> entry : values.entrySet()) {
            serializedValues.put(entry.getKey(), entry.getValue().serialize());
        }
        result.put("value", serializedValues);
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MapValue mapValue = (MapValue) obj;
        return Objects.equals(values, mapValue.values);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
    
    @Override
    @NotNull
    public String toString() {
        return "MapValue{size=" + size() + ", values=" + asString() + "}";
    }
}