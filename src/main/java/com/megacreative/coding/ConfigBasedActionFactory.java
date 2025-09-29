package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.interfaces.IActionFactory;
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
 * Factory for creating block actions based on configuration
 */
public class ConfigBasedActionFactory implements IActionFactory {
    private final Plugin plugin;
    // private static final Logger log = Logger.getLogger(ConfigBasedActionFactory.class.getName());  // Removed logger declaration
    private final Map<String, Class<? extends BlockAction>> actionClasses = new ConcurrentHashMap<>();
    
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
     * Registers an action class by its fully qualified name
     */
    @SuppressWarnings("unchecked")
    private void registerActionClass(String actionId, String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (BlockAction.class.isAssignableFrom(clazz)) {
                actionClasses.put(actionId, (Class<? extends BlockAction>) clazz);
                // Removed log statement
            }
        } catch (ClassNotFoundException e) {
            // Removed log statement
        }
    }
    
    /**
     * Creates an action instance by action ID
     */
    public BlockAction createAction(String actionId) {
        Class<? extends BlockAction> actionClass = actionClasses.get(actionId);
        if (actionClass == null) {
            // Removed log statement
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
            // Removed log statement
            return null;
        }
    }
    
    /**
     * Scans for annotated actions and registers them
     */
    public void registerAllActions() {
        // This method is not used in ConfigBasedActionFactory but required by interface
    }
    
    /**
     * Gets the display name for an action
     * 
     * @param actionId The action ID
     * @return The display name, or the action ID if no display name is set
     */
    public String getActionDisplayName(String actionId) {
        // This method is not used in ConfigBasedActionFactory but required by interface
        return actionId;
    }
    
    /**
     * Gets all registered action display names
     * 
     * @return A map of action IDs to display names
     */
    public Map<String, String> getActionDisplayNames() {
        // This method is not used in ConfigBasedActionFactory but required by interface
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