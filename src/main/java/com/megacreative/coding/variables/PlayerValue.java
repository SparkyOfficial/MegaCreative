package com.megacreative.coding.variables;

import org.bukkit.entity.Player;

/**
 * Specialized DataValue for storing and working with Player objects.
 * This provides type safety and convenience methods for player operations.
 */
public class PlayerValue extends DataValue {
    
    private final Player player;
    
    public PlayerValue(Player player) {
        super(player);
        this.player = player;
    }
    
    @Override
    public Player asPlayer() {
        return player;
    }
    
    @Override
    public boolean isPlayer() {
        return true;
    }
    
    @Override
    public String asString() {
        if (player == null) {
            return "null";
        }
        return "Player{name=" + player.getName() + ", uuid=" + player.getUniqueId() + "}";
    }
    
    @Override
    public double asNumber() {
        // For player, we might return a hash code or some other numeric representation
        return player != null ? player.hashCode() : 0;
    }
    
    @Override
    public boolean asBoolean() {
        return player != null && player.isOnline();
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        
        PlayerValue that = (PlayerValue) obj;
        return player != null ? player.equals(that.player) : that.player == null;
    }
    
    @Override
    public int hashCode() {
        return player != null ? player.hashCode() : 0;
    }
}