package com.megacreative.coding.variables;

import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * Specialized DataValue for storing and working with Player objects.
 * This provides type safety and convenience methods for player operations.
 */
public class PlayerValue implements DataValue, Cloneable {
    
    private Player player;
    
    public PlayerValue(Player player) {
        this.player = player;
    }
    
    @Override
    public ValueType getType() {
        return ValueType.PLAYER;
    }
    
    @Override
    public Object getValue() {
        return player;
    }
    
    @Override
    public void setValue(Object value) throws IllegalArgumentException {
        if (value instanceof Player) {
            this.player = (Player) value;
        } else {
            throw new IllegalArgumentException("Value must be a Player instance");
        }
    }
    
    @Override
    public String asString() {
        if (player == null) {
            return "null";
        }
        return player.getName();
    }
    
    @Override
    public Number asNumber() throws NumberFormatException {
        // For player, we might return a hash code or some other numeric representation
        return player != null ? player.hashCode() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return player != null && player.isOnline();
    }
    
    @Override
    public boolean isEmpty() {
        return player == null;
    }
    
    @Override
    public boolean isValid() {
        return player != null && player.isOnline();
    }
    
    @Override
    public String getDescription() {
        return "Player: " + asString();
    }
    
    // Convenience methods for player operations
    public String getName() {
        return player != null ? player.getName() : null;
    }
    
    public String getDisplayName() {
        return player != null ? player.getDisplayName() : null;
    }
    
    public boolean isOnline() {
        return player != null && player.isOnline();
    }
    
    public boolean hasPermission(String permission) {
        return player != null && player.hasPermission(permission);
    }
    
    @Override
    public DataValue clone() {
        try {
            // Call super.clone() first to create the new instance
            PlayerValue cloned = (PlayerValue) super.clone();
            // Player objects are managed by the server and should not be cloned
            return cloned;
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    @Override
    public DataValue copy() {
        return clone();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", getType().name());
        map.put("value", player != null ? player.getName() : null);
        return map;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PlayerValue that = (PlayerValue) obj;
        return player != null ? player.equals(that.player) : that.player == null;
    }
    
    @Override
    public int hashCode() {
        return player != null ? player.hashCode() : 0;
    }
}