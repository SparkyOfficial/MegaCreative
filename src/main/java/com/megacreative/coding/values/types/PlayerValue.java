package com.megacreative.coding.values.types;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Player value with online validation
 */
public class PlayerValue implements DataValue {
    private Player value;
    
    public PlayerValue(@NotNull Player value) {
        this.value = value;
    }
    
    @Override
    @NotNull
    public ValueType getType() { return ValueType.PLAYER; }
    
    @Override
    @NotNull
    public Object getValue() { return value; }
    
    @Override
    public void setValue(@NotNull Object value) {
        if (value instanceof Player) {
            this.value = (Player) value;
        } else {
            throw new IllegalArgumentException("Cannot set player from: " + value);
        }
    }
    
    @Override
    @NotNull
    public String asString() { return value != null ? value.getName() : "null"; }
    
    @Override
    @NotNull
    public Number asNumber() { throw new NumberFormatException("Cannot convert player to number"); }
    
    @Override
    public boolean asBoolean() { return value != null && value.isOnline(); }
    
    @Override
    public boolean isEmpty() { return value == null; }
    
    @Override
    public boolean isValid() { return value != null && value.isOnline(); }
    
    @Override
    @NotNull
    public String getDescription() { return "Player: " + asString(); }
    
    @Override
    @NotNull
    public DataValue copy() {
        return new PlayerValue(value);
    }
    
    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", value != null ? value.getName() : null);
        return map;
    }
}