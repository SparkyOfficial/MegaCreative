package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigBasedActionFactory {
    private final MegaCreative plugin;
    private final Logger log;
    private final Map<String, Class<? extends BlockAction>> actionClasses;
    
    public ConfigBasedActionFactory(MegaCreative plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.actionClasses = new HashMap<>();
        loadActionClasses();
    }
    
    /**
     * Loads action classes from configuration
     */
    private void loadActionClasses() {
        // Load action classes from coding_blocks.yml
        try {
            YamlConfiguration config = loadConfig("coding_blocks.yml");
            
            // Load action classes dynamically from the config
            if (config.contains("action_configurations")) {
                Set<String> actionIds = config.getConfigurationSection("action_configurations").getKeys(false);
                for (String actionId : actionIds) {
                    // Try to find the class for this action
                    String className = findActionClass(actionId);
                    if (className != null) {
                        registerActionClass(actionId, className);
                    }
                }
            }
            
            // Also load from the blocks section for backward compatibility
            if (config.contains("blocks")) {
                Set<String> blockTypes = config.getConfigurationSection("blocks").getKeys(false);
                for (String blockType : blockTypes) {
                    if (config.contains("blocks." + blockType + ".actions")) {
                        // Get list of actions for this block type
                        for (String actionId : config.getStringList("blocks." + blockType + ".actions")) {
                            // Try to find the class for this action
                            String className = findActionClass(actionId);
                            if (className != null) {
                                registerActionClass(actionId, className);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error loading action classes from config: " + e.getMessage());
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
        
        log.warning("Could not find class for action ID: " + actionId);
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