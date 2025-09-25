package com.megacreative.managers;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player modes (DEV/PLAY) for the MegaCreative plugin.
 * Tracks each player's current mode and provides methods to check and change modes.
 */
public class PlayerModeManager {
    
    /**
     * Enum representing the two player modes
     */
    public enum PlayerMode {
        DEV,  // Development mode - Creative, coding blocks, no script execution
        PLAY  // Play mode - Adventure/Survival, clean inventory, script execution enabled
    }
    
    // Map to store each player's current mode
    private final Map<UUID, PlayerMode> playerModes = new HashMap<>();
    
    /**
     * Sets the mode for a player
     * @param player The player to set the mode for
     * @param mode The mode to set
     */
    public void setMode(Player player, PlayerMode mode) {
        playerModes.put(player.getUniqueId(), mode);
    }
    
    /**
     * Gets the current mode for a player
     * @param player The player to get the mode for
     * @return The player's current mode, or DEV if not set
     */
    public PlayerMode getMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), PlayerMode.DEV);
    }
    
    /**
     * Checks if a player is in PLAY mode
     * @param player The player to check
     * @return true if the player is in PLAY mode, false otherwise
     */
    public boolean isInPlayMode(Player player) {
        return getMode(player) == PlayerMode.PLAY;
    }
    
    /**
     * Checks if a player is in DEV mode
     * @param player The player to check
     * @return true if the player is in DEV mode, false otherwise
     */
    public boolean isInDevMode(Player player) {
        return getMode(player) == PlayerMode.DEV;
    }
    
    /**
     * Clears the mode for a player (when they leave the server)
     * @param player The player to clear the mode for
     */
    public void clearMode(Player player) {
        playerModes.remove(player.getUniqueId());
    }
}