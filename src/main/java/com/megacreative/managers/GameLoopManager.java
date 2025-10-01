package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeHandler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages the game loop system that executes code for each player at 20 TPS
 * This is a core system for continuous script execution
 */
public class GameLoopManager {
    private static final Logger LOGGER = Logger.getLogger(GameLoopManager.class.getName());
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final ScriptEngine scriptEngine;
    
    // Track game loop status for each player
    private final Map<String, Boolean> playerGameLoopEnabled = new ConcurrentHashMap<>();
    
    public GameLoopManager(MegaCreative plugin, IWorldManager worldManager, ScriptEngine scriptEngine) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.scriptEngine = scriptEngine;
    }
    
    /**
     * Enable the game loop for a specific player
     * @param player The player to enable the game loop for
     */
    public void enableGameLoop(Player player) {
        if (player != null) {
            playerGameLoopEnabled.put(player.getUniqueId().toString(), true);
            LOGGER.info("Enabled game loop for player: " + player.getName());
        }
    }
    
    /**
     * Disable the game loop for a specific player
     * @param player The player to disable the game loop for
     */
    public void disableGameLoop(Player player) {
        if (player != null) {
            playerGameLoopEnabled.put(player.getUniqueId().toString(), false);
            LOGGER.info("Disabled game loop for player: " + player.getName());
        }
    }
    
    /**
     * Check if the game loop is enabled for a specific player
     * @param player The player to check
     * @return true if the game loop is enabled, false otherwise
     */
    public boolean isGameLoopEnabled(Player player) {
        if (player == null) return false;
        return playerGameLoopEnabled.getOrDefault(player.getUniqueId().toString(), false);
    }
    
    /**
     * Execute the game loop for all online players
     * This method is called on every server tick (20 times per second)
     */
    public void executeGameLoop() {
        // Get all online players
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        
        // Execute game loop for each player
        for (Player player : players) {
            // Check if game loop is enabled for this player
            if (isGameLoopEnabled(player)) {
                executePlayerGameLoop(player);
            }
        }
    }
    
    /**
     * Execute the game loop for a specific player
     * @param player The player to execute the game loop for
     */
    private void executePlayerGameLoop(Player player) {
        try {
            // Find the creative world for this player
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) {
                return;
            }
            
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                return;
            }
            
            // TODO: Find and execute the game loop code blocks for this player
            // This would involve finding blocks with the "Game Loop" event type
            // and executing them through the ScriptEngine
            
            // For now, we'll just log that we would execute the game loop
            LOGGER.fine("Executing game loop for player: " + player.getName());
            
        } catch (Exception e) {
            LOGGER.warning("Error executing game loop for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Initialize the game loop system
     */
    public void initialize() {
        // Enable game loop for all players by default
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            enableGameLoop(player);
        }
        
        LOGGER.info("GameLoopManager initialized");
    }
    
    /**
     * Clean up resources when the plugin is disabled
     */
    public void cleanup() {
        playerGameLoopEnabled.clear();
        LOGGER.info("GameLoopManager cleaned up");
    }
}