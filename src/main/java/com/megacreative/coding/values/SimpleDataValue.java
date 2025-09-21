package com.megacreative.coding.values;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of DataValue that can hold any type of value.
 */
@SerializableAs("SimpleDataValue")
public class SimpleDataValue implements DataValue {
    private ValueType type;
    private Object value;

    public SimpleDataValue(Object value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        this.value = value;
    }

    @Override
    public String asString() {
        return value != null ? value.toString() : "";
    }

    @Override
    public Number asNumber() throws NumberFormatException {
        if (value instanceof Number) {
            return (Number) value;
        }
        return Double.parseDouble(asString());
    }

    @Override
    public boolean asBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = asString().toLowerCase();
        return str.equals("true") || str.equals("yes") || str.equals("1");
    }

    @Override
    public boolean isEmpty() {
        return value == null || (value instanceof String && ((String) value).trim().isEmpty());
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type.name());
        result.put("value", value);
        return result;
    }

    public static SimpleDataValue deserialize(Map<String, Object> args) {
        ValueType type = ValueType.valueOf((String) args.get("type"));
        Object value = args.get("value");
        return new SimpleDataValue(value, type);
    }

    @Override
    public DataValue copy() {
        return new SimpleDataValue(value, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DataValue)) return false;
        DataValue other = (DataValue) obj;
        return (value == null) ? other.getValue() == null : value.equals(other.getValue());
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public boolean isValid() {
        // A SimpleDataValue is valid if it has a type and a value
        return type != null && value != null;
    }
    
    @Override
    public String getDescription() {
        return "Simple data value of type " + (type != null ? type.name() : "null");
    }

    @Override
    public String toString() {
        return asString();
    }
}
