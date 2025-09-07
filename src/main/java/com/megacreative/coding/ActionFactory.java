package com.megacreative.coding;

import com.megacreative.coding.actions.*;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory for creating BlockAction instances dynamically based on configuration.
 * Replaces hardcoded action registration with a flexible, configuration-driven approach.
 */
public class ActionFactory {
    
    private static final Logger log = Logger.getLogger(ActionFactory.class.getName());
    
    private final BlockConfigService blockConfigService;
    private final ParameterResolver parameterResolver;
    private final Map<String, Class<? extends BlockAction>> actionClassMap = new HashMap<>();
    
    public ActionFactory(BlockConfigService blockConfigService, ParameterResolver parameterResolver) {
        this.blockConfigService = blockConfigService;
        this.parameterResolver = parameterResolver;
        initializeActionClassMap();
    }
    
    /**
     * Initializes the map of action names to their implementation classes
     */
    private void initializeActionClassMap() {
        // Player actions
        actionClassMap.put("sendMessage", SendMessageAction.class);
        actionClassMap.put("teleportPlayer", TeleportAction.class);
        actionClassMap.put("giveItem", GiveItemAction.class);
        actionClassMap.put("playSound", PlaySoundAction.class);
        
        // Variable actions
        actionClassMap.put("setVariable", SetVariableAction.class);
        actionClassMap.put("getVariable", GetVariableAction.class);
        actionClassMap.put("addToVariable", AddToVariableAction.class);
        actionClassMap.put("subtractFromVariable", SubtractFromVariableAction.class);
        
        // Game actions
        actionClassMap.put("spawnMob", SpawnMobAction.class);
        actionClassMap.put("createExplosion", CreateExplosionAction.class);
        actionClassMap.put("setWeather", SetWeatherAction.class);
        actionClassMap.put("setTime", SetTimeAction.class);
        
        // Event actions
        actionClassMap.put("onPlayerJoin", PlayerJoinAction.class);
        actionClassMap.put("onPlayerQuit", PlayerQuitAction.class);
        actionClassMap.put("onPlayerInteract", PlayerInteractAction.class);
        actionClassMap.put("onPlayerMove", PlayerMoveAction.class);
        actionClassMap.put("onPlayerChat", PlayerChatAction.class);
        actionClassMap.put("onPlayerDeath", PlayerDeathAction.class);
        actionClassMap.put("onPlayerRespawn", PlayerRespawnAction.class);
        
        // Control flow actions
        actionClassMap.put("ifCondition", IfCondition.class);
        actionClassMap.put("elseCondition", ElseCondition.class);
        
        log.info("Initialized ActionFactory with " + actionClassMap.size() + " action types");
    }
    
    /**
     * Creates a BlockAction instance for the specified action name
     * @param actionName The name of the action to create
     * @param material The material associated with the block
     * @return A new BlockAction instance, or null if not found
     */
    public BlockAction createAction(String actionName, Material material) {
        if (actionName == null) {
            return null;
        }
        
        // First try to get from the class map
        Class<? extends BlockAction> actionClass = actionClassMap.get(actionName);
        if (actionClass != null) {
            try {
                // Try to create an instance with parameter resolver
                if (ParameterResolver.class.isAssignableFrom(actionClass.getConstructors()[0].getParameterTypes()[0])) {
                    return actionClass.getConstructor(ParameterResolver.class).newInstance(parameterResolver);
                }
                // Try to create an instance with no parameters
                return actionClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.warning("Failed to create action instance for " + actionName + ": " + e.getMessage());
                // Fall back to generic action
            }
        }
        
        // If not found in class map, create a generic action based on configuration
        var blockConfig = blockConfigService.getBlockConfig(actionName);
        if (blockConfig != null) {
            return new GenericBlockAction(blockConfig);
        }
        
        log.warning("No action implementation found for: " + actionName);
        return null;
    }
    
    /**
     * Registers a custom action class
     * @param actionName The name of the action
     * @param actionClass The implementation class
     */
    public void registerActionClass(String actionName, Class<? extends BlockAction> actionClass) {
        if (actionName != null && actionClass != null) {
            actionClassMap.put(actionName, actionClass);
            log.info("Registered custom action class for " + actionName);
        }
    }
    
    /**
     * Gets the number of registered action types
     * @return The number of registered action types
     */
    public int getRegisteredActionCount() {
        return actionClassMap.size();
    }
}