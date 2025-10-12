package com.megacreative.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.entity.Player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeHandler;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;

/**
 * Manages the game loop system that executes code for each player at 20 TPS
 * This is a core system for continuous script execution
 */
public class GameLoopManager {
    private static final Logger LOGGER = Logger.getLogger(GameLoopManager.class.getName());
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final ScriptEngine scriptEngine;
    
    
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
        
        Player[] players = plugin.getServer().getOnlinePlayers().toArray(new Player[0]);
        
        
        for (Player player : players) {
            
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
            
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
            if (creativeWorld == null || !creativeWorld.canCode(player)) {
                return;
            }
            
            
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                return;
            }
            
            
            
            
            
            
            LOGGER.fine("Executing game loop for player: " + player.getName());
            
        } catch (Exception e) {
            LOGGER.warning("Error executing game loop for player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Initialize the game loop system
     */
    public void initialize() {
        
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