package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.interfaces.IConditionFactory;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.values.DataValue;
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

/**
 * Factory for creating block conditions based on configuration
 */
public class ConfigBasedConditionFactory implements IConditionFactory {
    private final Plugin plugin;
    // private static final Logger log = Logger.getLogger(ConfigBasedConditionFactory.class.getName());  // Removed logger declaration
    private final Map<String, Class<? extends BlockCondition>> conditionClasses = new ConcurrentHashMap<>();
    
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
            // Removed log statement
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
        
        // Removed log statement
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
                // Removed log statement
            } else {
                // Removed log statement
            }
        } catch (ClassNotFoundException e) {
            // Removed log statement
        }
    }
    
    /**
     * Creates a condition instance by condition ID
     */
    public BlockCondition createCondition(String conditionId) {
        Class<? extends BlockCondition> conditionClass = conditionClasses.get(conditionId);
        if (conditionClass == null) {
            // Removed log statement
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
            // Removed log statement
            return null;
        }
    }
    
    /**
     * Scans for annotated conditions and registers them
     */
    public void registerAllConditions() {
        // This method is not used in ConfigBasedConditionFactory but required by interface
    }
    
    /**
     * Gets the display name for a condition
     * 
     * @param conditionId The condition ID
     * @return The display name, or the condition ID if no display name is set
     */
    public String getConditionDisplayName(String conditionId) {
        // This method is not used in ConfigBasedConditionFactory but required by interface
        return conditionId;
    }
    
    /**
     * Gets all registered condition display names
     * 
     * @return A map of condition IDs to display names
     */
    public Map<String, String> getConditionDisplayNames() {
        // This method is not used in ConfigBasedConditionFactory but required by interface
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
        // Implementation not required for this factory
    }
    
    /**
     * Publishes an event with associated data to the event system.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     */
    public void publishEvent(String eventName, Map<String, DataValue> eventData) {
        // Implementation not required for this factory
    }
}