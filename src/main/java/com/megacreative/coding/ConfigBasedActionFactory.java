package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.executors.ExecutionResult;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Action factory that loads actions from configuration instead of manual registration.
 * This eliminates the need for a large switch-case in ActionFactory.
 */
public class ConfigBasedActionFactory {
    private static final Logger log = Logger.getLogger(ConfigBasedActionFactory.class.getName());
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final Map<String, Class<? extends BlockAction>> actionClasses = new HashMap<>();
    
    public ConfigBasedActionFactory(MegaCreative plugin) {
        this.plugin = plugin;
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        loadActionClasses();
    }
    
    /**
     * Loads action classes from configuration
     */
    private void loadActionClasses() {
        // In a real implementation, you would load this from a config file
        // For now, we'll register some common actions
        try {
            // Load action classes dynamically
            registerActionClass("sendMessage", "com.megacreative.coding.actions.SendMessageAction");
            registerActionClass("command", "com.megacreative.coding.actions.CommandAction");
            registerActionClass("teleport", "com.megacreative.coding.actions.TeleportAction");
            registerActionClass("wait", "com.megacreative.coding.actions.WaitAction");
            registerActionClass("repeat", "com.megacreative.coding.actions.RepeatAction");
            registerActionClass("whileLoop", "com.megacreative.coding.actions.WhileAction");
            registerActionClass("else", "com.megacreative.coding.actions.ElseAction");
            // Add more as needed
        } catch (Exception e) {
            log.severe("Error loading action classes: " + e.getMessage());
        }
    }
    
    /**
     * Registers an action class by its fully qualified name
     */
    @SuppressWarnings("unchecked")
    private void registerActionClass(String actionId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (BlockAction.class.isAssignableFrom(clazz)) {
                actionClasses.put(actionId, (Class<? extends BlockAction>) clazz);
                log.info("Registered action class: " + actionId + " -> " + className);
            } else {
                log.warning("Class " + className + " does not implement BlockAction interface");
            }
        } catch (ClassNotFoundException e) {
            log.warning("Action class not found: " + className);
        }
    }
    
    /**
     * Creates an action instance by action ID
     */
    public BlockAction createAction(String actionId) {
        Class<? extends BlockAction> actionClass = actionClasses.get(actionId);
        if (actionClass == null) {
            log.warning("No action class registered for action ID: " + actionId);
            return null;
        }
        
        try {
            // Try to find a constructor that takes MegaCreative plugin
            try {
                Constructor<? extends BlockAction> constructor = actionClass.getConstructor(MegaCreative.class);
                return constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Fallback to no-argument constructor
                Constructor<? extends BlockAction> constructor = actionClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            log.severe("Error creating action instance for " + actionId + ": " + e.getMessage());
            return null;
        }
    }
}