package com.megacreative.coding.values;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class MapValue implements DataValue {
    private final Map<String, DataValue> map;

    public MapValue(Map<String, DataValue> map) {
        this.map = map != null ? new HashMap<>(map) : new HashMap<>();
    }

    public static MapValue of(Map<String, DataValue> map) {
        return new MapValue(map);
    }

    public static MapValue of() {
        return new MapValue(new HashMap<>());
    }

    public Map<String, DataValue> getMap() {
        return new HashMap<>(map);
    }

    public void put(String key, DataValue value) {
        map.put(key, value);
    }

    public DataValue get(String key) {
        return map.getOrDefault(key, DataValue.of(""));
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public int size() {
        return map.size();
    }

    @Override
    public ValueType getType() {
        return ValueType.DICTIONARY;
    }

    @Override
    public Object getValue() {
        return map;
    }

    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        // MapValue is immutable, so we don't allow setting values
        throw new IllegalArgumentException("MapValue is immutable");
    }

    @Override
    public String asString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, DataValue> entry : map.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue().asString());
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Number asNumber() {
        return map.size();
    }

    @Override
    public boolean asBoolean() {
        return !map.isEmpty();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean isValid() {
        for (Map.Entry<String, DataValue> entry : map.entrySet()) {
            if (entry.getKey() == null || !entry.getValue().isValid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Map with " + map.size() + " entries";
    }

    @Override
    public DataValue copy() {
        Map<String, DataValue> copiedMap = new HashMap<>();
        for (Map.Entry<String, DataValue> entry : map.entrySet()) {
            copiedMap.put(entry.getKey(), entry.getValue().copy());
        }
        return new MapValue(copiedMap);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("type", "DICTIONARY");
        result.put("value", map);
        return result;
    }
}