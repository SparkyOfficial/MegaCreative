package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Player value with online validation
 */
public class PlayerValue implements DataValue {
    private Player value;
    
    public PlayerValue(Player value) {
        this.value = value;
    }
    
    @Override
    public ValueType getType() { return ValueType.PLAYER; }
    
    @Override
    public Object getValue() { return value; }
    
    @Override
    public void setValue(Object value) {
        if (value instanceof Player) {
            this.value = (Player) value;
        } else {
            throw new IllegalArgumentException("Cannot set player from: " + value);
        }
    }
    
    @Override
    public String asString() { return value != null ? value.getName() : "null"; }
    
    @Override
    public Number asNumber() { throw new NumberFormatException("Cannot convert player to number"); }
    
    @Override
    public boolean asBoolean() { return value != null && value.isOnline(); }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return value != null && value.isOnline(); }
    
    @Override
    public String getDescription() { return "Player: " + asString(); }
    
    @Override
    public DataValue clone() {
        try {
            PlayerValue cloned = (PlayerValue) super.clone();
            // The value field is not final, so we can directly assign it
            cloned.value = value;
            return cloned;
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Clone not supported", e);
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value != null ? value.getName() : null);
        return map;
    }
}