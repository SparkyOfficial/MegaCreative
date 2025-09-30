package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.interfaces.IConditionFactory;
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
 * Factory for creating block conditions based on configuration
 */
public class ConfigBasedConditionFactory implements IConditionFactory {
    private final Plugin plugin;
    private static final Logger log = Logger.getLogger(ConfigBasedConditionFactory.class.getName());
    private final Map<String, Class<? extends BlockCondition>> conditionClasses = new ConcurrentHashMap<>();
    private CustomEventManager eventManager;
    
    public ConfigBasedConditionFactory(Plugin plugin) {
        this.plugin = plugin;
        loadConditionClasses();
    }
    
    /**
     * Loads condition classes from configuration
     */
    private void loadConditionClasses() {
        try {
            YamlConfiguration config = loadConfig("conditions.yml");
            
            // Load condition class mappings
            if (config.contains("conditions")) {
                var conditionsSection = config.getConfigurationSection("conditions");
                if (conditionsSection != null) {
                    for (String conditionId : conditionsSection.getKeys(false)) {
                        String className = config.getString("conditions." + conditionId);
                        if (className != null && !className.isEmpty()) {
                            registerConditionClass(conditionId, className);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error loading condition classes: " + e.getMessage());
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
     * Finds the class name for a condition ID by scanning available classes
     */
    private String findConditionClass(String conditionId) {
        // Common package prefixes to search
        String[] packagePrefixes = {
            "com.megacreative.coding.conditions.",
            "com.megacreative.coding.actions.",
            "com.megacreative.coding."
        };
        
        // Common class name suffixes to try
        String[] classSuffixes = {
            "",
            "Condition",
            "Action"
        };
        
        // Try different combinations
        for (String prefix : packagePrefixes) {
            for (String suffix : classSuffixes) {
                String className = prefix + capitalize(conditionId) + suffix;
                try {
                    Class<?> clazz = Class.forName(className);
                    if (BlockCondition.class.isAssignableFrom(clazz)) {
                        return className;
                    }
                } catch (ClassNotFoundException e) {
                    // Try next combination
                }
            }
        }
        
        // Try exact match
        try {
            Class<?> clazz = Class.forName(conditionId);
            if (BlockCondition.class.isAssignableFrom(clazz)) {
                return conditionId;
            }
        } catch (ClassNotFoundException e) {
            // Not found
        }
        
        log.warning("Could not find condition class for: " + conditionId);
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
     * Registers a condition class by its fully qualified name
     */
    @SuppressWarnings("unchecked")
    private void registerConditionClass(String conditionId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (BlockCondition.class.isAssignableFrom(clazz)) {
                conditionClasses.put(conditionId, (Class<? extends BlockCondition>) clazz);
                log.info("Registered condition class: " + conditionId + " -> " + className);
            }
        } catch (ClassNotFoundException e) {
            log.warning("Condition class not found: " + className);
        }
    }
    
    /**
     * Creates a condition instance by condition ID
     */
    public BlockCondition createCondition(String conditionId) {
        Class<? extends BlockCondition> conditionClass = conditionClasses.get(conditionId);
        if (conditionClass == null) {
            log.warning("No condition class registered for condition ID: " + conditionId);
            return null;
        }
        
        try {
            // Try to find a constructor that takes MegaCreative plugin
            try {
                Constructor<? extends BlockCondition> constructor = conditionClass.getConstructor(MegaCreative.class);
                return constructor.newInstance(plugin);
            } catch (NoSuchMethodException e) {
                // Fallback to no-argument constructor
                Constructor<? extends BlockCondition> constructor = conditionClass.getConstructor();
                return constructor.newInstance();
            }
        } catch (Exception e) {
            log.severe("Error creating condition instance for " + conditionId + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Scans for annotated conditions and registers them
     */
    public void registerAllConditions() {
        // This method is not used in ConfigBasedConditionFactory but required by interface
        log.info("registerAllConditions called but not implemented in ConfigBasedConditionFactory");
    }
    
    /**
     * Gets the display name for a condition
     * 
     * @param conditionId The condition ID
     * @return The display name, or the condition ID if no display name is set
     */
    public String getConditionDisplayName(String conditionId) {
        // This method is not used in ConfigBasedConditionFactory but required by interface
        log.fine("getConditionDisplayName called but not implemented in ConfigBasedConditionFactory");
        return conditionId;
    }
    
    /**
     * Gets all registered condition display names
     * 
     * @return A map of condition IDs to display names
     */
    public Map<String, String> getConditionDisplayNames() {
        // This method is not used in ConfigBasedConditionFactory but required by interface
        log.fine("getConditionDisplayNames called but not implemented in ConfigBasedConditionFactory");
        return Collections.emptyMap();
    }
    
    /**
     * Gets the condition count
     * @return Number of registered conditions
     */
    public int getConditionCount() {
        return conditionClasses.size();
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