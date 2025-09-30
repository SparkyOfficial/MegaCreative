package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.interfaces.IActionFactory;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.events.CustomEventManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Factory for creating block actions based on configuration
 */
public class ConfigBasedActionFactory implements IActionFactory {
    private final Plugin plugin;
    private static final Logger log = Logger.getLogger(ConfigBasedActionFactory.class.getName());
    private final Map<String, Class<? extends BlockAction>> actionClasses = new ConcurrentHashMap<>();
    private CustomEventManager eventManager;
    
    public ConfigBasedActionFactory(Plugin plugin) {
        this.plugin = plugin;
        loadActionClasses();
    }
    
    /**
     * Loads action classes from configuration
     */
    private void loadActionClasses() {
        try {
            YamlConfiguration config = loadConfig("actions.yml");
            
            // Load action class mappings
            if (config.contains("actions")) {
                var actionsSection = config.getConfigurationSection("actions");
                if (actionsSection != null) {
                    for (String actionId : actionsSection.getKeys(false)) {
                        String className = config.getString("actions." + actionId);
                        if (className != null && !className.isEmpty()) {
                            registerActionClass(actionId, className);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error loading action classes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads a configuration file from resources or data folder
     */
    private YamlConfiguration loadConfig(String fileName) {
        // First try to load from data folder (user-modified version)
        File dataFolderFile = new File(plugin.getDataFolder(), fileName);
        if (dataFolderFile.exists()) {
            return YamlConfiguration.loadConfiguration(dataFolderFile);
        }
        
        // Fallback to resource file
        InputStream resourceStream = plugin.getResource(fileName);
        if (resourceStream != null) {
            return YamlConfiguration.loadConfiguration(new InputStreamReader(resourceStream));
        }
        
        // Create empty config if neither exists
        return new YamlConfiguration();
    }
    
    /**
     * Finds the class name for an action ID by scanning available classes
     */
    private String findActionClass(String actionId) {
        // Common package prefixes to search
        String[] packagePrefixes = {
            "com.megacreative.coding.actions.",
            "com.megacreative.coding.conditions.",
            "com.megacreative.coding."
        };
        
        // Common class name suffixes to try
        String[] classSuffixes = {
            "",
            "Action",
            "Condition"
        };
        
        // Try different combinations
        for (String prefix : packagePrefixes) {
            for (String suffix : classSuffixes) {
                String className = prefix + capitalize(actionId) + suffix;
                try {
                    Class<?> clazz = Class.forName(className);
                    if (BlockAction.class.isAssignableFrom(clazz)) {
                        return className;
                    }
                } catch (ClassNotFoundException e) {
                    // Try next combination
                }
            }
        }
        
        // Try exact match
        try {
            Class<?> clazz = Class.forName(actionId);
            if (BlockAction.class.isAssignableFrom(clazz)) {
                return actionId;
            }
        } catch (ClassNotFoundException e) {
            // Not found
        }
        
        log.warning("Could not find action class for: " + actionId);
        return null;
    }
    
    /**
     * Capitalizes the first letter of a string
     */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
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
    
    /**
     * Scans for annotated actions and registers them
     */
    public void registerAllActions() {
        // This method is not used in ConfigBasedActionFactory but required by interface
        log.info("registerAllActions called but not implemented in ConfigBasedActionFactory");
    }
    
    /**
     * Gets the display name for an action
     * 
     * @param actionId The action ID
     * @return The display name, or the action ID if no display name is set
     */
    public String getActionDisplayName(String actionId) {
        // This method is not used in ConfigBasedActionFactory but required by interface
        log.fine("getActionDisplayName called but not implemented in ConfigBasedActionFactory");
        return actionId;
    }
    
    /**
     * Gets all registered action display names
     * 
     * @return A map of action IDs to display names
     */
    public Map<String, String> getActionDisplayNames() {
        // This method is not used in ConfigBasedActionFactory but required by interface
        log.fine("getActionDisplayNames called but not implemented in ConfigBasedActionFactory");
        return Collections.emptyMap();
    }
    
    /**
     * Gets the action count
     * @return Number of registered actions
     */
    public int getActionCount() {
        return actionClasses.size();
    }
    
    /**
     * Publishes an event to the event system.
     * 
     * @param event The event to publish
     */
    public void publishEvent(CustomEvent event) {
        // Get the event manager from the service registry
        if (eventManager == null && plugin instanceof MegaCreative) {
            eventManager = ((MegaCreative) plugin).getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Create event data map
                Map<String, DataValue> eventData = new HashMap<>();
                
                // Add basic event information
                eventData.put("event_id", DataValue.fromObject(event.getId().toString()));
                eventData.put("event_name", DataValue.fromObject(event.getName()));
                eventData.put("event_category", DataValue.fromObject(event.getCategory()));
                eventData.put("event_description", DataValue.fromObject(event.getDescription()));
                eventData.put("event_author", DataValue.fromObject(event.getAuthor()));
                eventData.put("event_created_time", DataValue.fromObject(event.getCreatedTime()));
                
                // Add event data fields
                for (Map.Entry<String, CustomEvent.EventDataField> entry : event.getDataFields().entrySet()) {
                    eventData.put("data_" + entry.getKey(), DataValue.fromObject(entry.getKey()));
                }
                
                // Trigger the event through the event manager
                eventManager.triggerEvent(event.getName(), eventData, null, "global");
            } catch (Exception e) {
                log.severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            log.info("Published event: " + event.getName());
        }
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        // Get the event manager from the service registry
        if (eventManager == null && plugin instanceof MegaCreative) {
            eventManager = ((MegaCreative) plugin).getServiceRegistry().getService(CustomEventManager.class);
        }
        
        // If we have an event manager, use it to trigger the event
        if (eventManager != null) {
            try {
                // Trigger the event through the event manager
                eventManager.triggerEvent(eventName, eventData, null, "global");
            } catch (Exception e) {
                log.severe("Failed to publish event through CustomEventManager: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Fallback to logging if no event manager is available
            log.info("Published event: " + eventName + " with data: " + eventData.size() + " fields");
        }
    }
}