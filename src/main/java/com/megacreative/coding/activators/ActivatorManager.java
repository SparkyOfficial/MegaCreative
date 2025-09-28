package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeHandler;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages all activators across all creative worlds
 * Acts as a central registry and dispatcher for activator events
 */
public class ActivatorManager {
    
    private static final Logger LOGGER = Logger.getLogger(ActivatorManager.class.getName());
    
    private final MegaCreative plugin;
    private final Map<String, CodeHandler> worldCodeHandlers;
    
    public ActivatorManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.worldCodeHandlers = new ConcurrentHashMap<>();
    }
    
    /**
     * Registers a code handler for a creative world
     * @param worldId The ID of the creative world
     * @param codeHandler The code handler to register
     */
    public void registerCodeHandler(String worldId, CodeHandler codeHandler) {
        worldCodeHandlers.put(worldId, codeHandler);
        LOGGER.info("Registered code handler for world: " + worldId);
    }
    
    /**
     * Unregisters a code handler for a creative world
     * @param worldId The ID of the creative world
     */
    public void unregisterCodeHandler(String worldId) {
        CodeHandler removed = worldCodeHandlers.remove(worldId);
        if (removed != null) {
            LOGGER.info("Unregistered code handler for world: " + worldId);
        }
    }
    
    /**
     * Gets the code handler for a creative world
     * @param worldId The ID of the creative world
     * @return The code handler, or null if not found
     */
    public CodeHandler getCodeHandler(String worldId) {
        return worldCodeHandlers.get(worldId);
    }
    
    /**
     * Handles an event by dispatching it to the appropriate code handler
     * @param worldId The ID of the creative world where the event occurred
     * @param activatorType The type of activator to trigger
     * @param gameEvent The game event containing context data
     * @param player The player associated with the event (can be null)
     */
    public void handleEvent(String worldId, ActivatorType activatorType, GameEvent gameEvent, Player player) {
        CodeHandler codeHandler = worldCodeHandlers.get(worldId);
        if (codeHandler != null) {
            codeHandler.handleEvent(activatorType, gameEvent, player);
        } else {
            LOGGER.warning("No code handler found for world: " + worldId);
        }
    }
    
    /**
     * Gets all registered code handlers
     * @return Map of world IDs to code handlers
     */
    public Map<String, CodeHandler> getAllCodeHandlers() {
        return new ConcurrentHashMap<>(worldCodeHandlers);
    }
    
    /**
     * Clears all code handlers
     */
    public void clearAllCodeHandlers() {
        worldCodeHandlers.clear();
        LOGGER.info("Cleared all code handlers");
    }
    
    /**
     * Gets the number of registered code handlers
     * @return The count of code handlers
     */
    public int getCodeHandlerCount() {
        return worldCodeHandlers.size();
    }
    
    /**
     * Creates a new activator of the specified type for a creative world
     * @param world The creative world
     * @param type The type of activator to create
     * @return The created activator
     */
    public Activator createActivator(CreativeWorld world, ActivatorType type) {
        return type.createActivator(plugin, world);
    }
    
    /**
     * Registers an activator with its world's code handler
     * @param activator The activator to register
     */
    public void registerActivator(Activator activator) {
        CreativeWorld world = activator.getWorld();
        CodeHandler codeHandler = worldCodeHandlers.get(world.getId());
        if (codeHandler != null) {
            codeHandler.registerActivator(activator);
        } else {
            LOGGER.warning("No code handler found for world: " + world.getId() + " when registering activator");
        }
    }
}