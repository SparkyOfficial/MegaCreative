package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigBasedConditionFactory {
    private final MegaCreative plugin;
    private final Logger log;
    private final Map<String, Class<? extends BlockCondition>> conditionClasses;
    
    public ConfigBasedConditionFactory(MegaCreative plugin) {
        this.plugin = plugin;
        this.log = plugin.getLogger();
        this.conditionClasses = new HashMap<>();
        loadConditionClasses();
    }
    
    /**
     * Loads condition classes from configuration
     */
    private void loadConditionClasses() {
        // In a real implementation, you would load this from a config file
        // Load condition classes from coding_blocks.yml
        try {
            YamlConfiguration config = loadConfig("coding_blocks.yml");
            
            // Load condition classes dynamically from the config
            if (config.contains("action_configurations")) {
                Set<String> conditionIds = config.getConfigurationSection("action_configurations").getKeys(false);
                for (String conditionId : conditionIds) {
                    // Try to find the class for this condition
                    String className = findConditionClass(conditionId);
                    if (className != null) {
                        registerConditionClass(conditionId, className);
                    }
                }
            }
            
            // Also load from the blocks section for backward compatibility
            if (config.contains("blocks")) {
                Set<String> blockTypes = config.getConfigurationSection("blocks").getKeys(false);
                for (String blockType : blockTypes) {
                    if (config.contains("blocks." + blockType + ".actions")) {
                        // Get list of actions for this block type (conditions are also actions)
                        for (String conditionId : config.getStringList("blocks." + blockType + ".actions")) {
                            // Try to find the class for this condition
                            String className = findConditionClass(conditionId);
                            if (className != null) {
                                registerConditionClass(conditionId, className);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Error loading condition classes from config: " + e.getMessage());
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
        
        log.warning("Could not find class for condition ID: " + conditionId);
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
            } else {
                log.warning("Class " + className + " does not implement BlockCondition interface");
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
}