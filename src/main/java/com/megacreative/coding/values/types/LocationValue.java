package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.Location;

import java.util.*;

/**
 * Location value with world coordinates
 */
public class LocationValue implements DataValue {
    private Location value;
    
    public LocationValue(Location value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.LOCATION; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Location) {
            this.value = (Location) value;
        } else {
            throw new IllegalArgumentException("Cannot set location from: " + value);
        }
    }
    
    @Override
    public String asString() {
        if (value == null) return "null";
        return String.format("%.1f, %.1f, %.1f (%s)", 
                value.getX(), value.getY(), value.getZ(), 
                value.getWorld() != null ? value.getWorld().getName() : "unknown");
    }
    
    @Override
    public Number asNumber() { throw new NumberFormatException("Cannot convert location to number"); }
    
    @Override
    public boolean asBoolean() { return value != null; }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return value != null && value.getWorld() != null; }
    
    @Override
    public String getDescription() { return "Location: " + asString(); }
    
    @Override
    public DataValue copy() { 
        return new LocationValue(value != null ? value.clone() : null); 
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value);
        return map;
    }
}