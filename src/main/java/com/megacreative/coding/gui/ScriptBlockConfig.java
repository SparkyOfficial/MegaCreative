package com.megacreative.coding.gui;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages configuration for script blocks.
 * This class handles loading and accessing block configurations from the config file.
 */
public class ScriptBlockConfig {
    private final Map<String, BlockTypeConfig> blockTypes;
    private final File configFile;
    private YamlConfiguration config;
    
    public ScriptBlockConfig(File configFile) {
        this.configFile = configFile;
        this.blockTypes = new HashMap<>();
        loadConfig();
    }
    
    /**
     * Loads block configurations from the config file
     */
    private void loadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(key);
                if (blockSection != null) {
                    Material material = Material.valueOf(key);
                    BlockTypeConfig typeConfig = new BlockTypeConfig(
                        blockSection.getString("name"),
                        blockSection.getString("type"),
                        blockSection.getString("description"),
                        blockSection.getString("default_action"),
                        blockSection.getBoolean("is_constructor", false),
                        loadActions(blockSection)
                    );
                    blockTypes.put(key, typeConfig);
                }
            }
        }
    }
    
    /**
     * Loads available actions for a block type
     */
    private List<String> loadActions(ConfigurationSection section) {
        return section.getStringList("actions");
    }
    
    /**
     * Gets the configuration for a block type
     */
    public BlockTypeConfig getBlockTypeConfig(String materialName) {
        return blockTypes.get(materialName);
    }
    
    /**
     * Gets all available block type configurations
     */
    public Map<String, BlockTypeConfig> getAllBlockTypes() {
        return blockTypes;
    }
    
    /**
     * Configuration class for a block type
     */
    public static class BlockTypeConfig {
        private final String name;
        private final String type;
        private final String description;
        private final String defaultAction;
        private final boolean isConstructor;
        private final List<String> actions;
        
        public BlockTypeConfig(String name, String type, String description, 
                             String defaultAction, boolean isConstructor, List<String> actions) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.defaultAction = defaultAction;
            this.isConstructor = isConstructor;
            this.actions = actions != null ? actions : new ArrayList<>();
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getDefaultAction() {
            return defaultAction;
        }
        
        public boolean isConstructor() {
            return isConstructor;
        }
        
        public List<String> getActions() {
            return actions;
        }
    }
}