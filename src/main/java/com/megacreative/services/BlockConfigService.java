package com.megacreative.services;

import lombok.extern.java.Log;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Service for loading and managing block configurations from coding_blocks.yml
 * Replaces hardcoded material mappings with config-driven approach
 */
@Log
public class BlockConfigService {
    
    private final Plugin plugin;
    private final Map<Material, BlockConfig> materialToConfig = new HashMap<>();
    private final Set<Material> codeBlocks = new HashSet<>();
    private YamlConfiguration config;
    
    public BlockConfigService(Plugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Loads block configurations from coding_blocks.yml
     */
    public void loadConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
            
            if (!configFile.exists()) {
                plugin.saveResource("coding_blocks.yml", false);
            }
            
            config = YamlConfiguration.loadConfiguration(configFile);
            parseBlockConfigurations();
            
            log.info("Loaded " + materialToConfig.size() + " block configurations from coding_blocks.yml");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load block configurations", e);
        }
    }
    
    /**
     * Parses block configurations from the YAML config
     */
    private void parseBlockConfigurations() {
        materialToConfig.clear();
        codeBlocks.clear();
        
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection == null) {
            log.warning("No 'blocks' section found in coding_blocks.yml");
            return;
        }
        
        for (String materialName : blocksSection.getKeys(false)) {
            try {
                Material material = Material.valueOf(materialName);
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(materialName);
                
                if (blockSection != null) {
                    BlockConfig blockConfig = parseBlockConfig(material, blockSection);
                    materialToConfig.put(material, blockConfig);
                    codeBlocks.add(material);
                }
                
            } catch (IllegalArgumentException e) {
                log.warning("Invalid material in config: " + materialName);
            }
        }
    }
    
    /**
     * Parses individual block configuration
     */
    private BlockConfig parseBlockConfig(Material material, ConfigurationSection section) {
        String name = section.getString("name", material.name());
        String description = section.getString("description", "");
        List<String> actions = section.getStringList("actions");
        
        // Get default action (first action in the list)
        String defaultAction = actions.isEmpty() ? null : actions.get(0);
        
        return new BlockConfig(material, name, description, actions, defaultAction);
    }
    
    /**
     * Gets the default action for a material
     */
    public String getDefaultAction(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getDefaultAction() : null;
    }
    
    /**
     * Gets all available actions for a material
     */
    public List<String> getAvailableActions(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getActions() : Collections.emptyList();
    }
    
    /**
     * Checks if a material is a valid code block
     */
    public boolean isCodeBlock(Material material) {
        return codeBlocks.contains(material);
    }
    
    /**
     * Gets all valid code block materials
     */
    public Set<Material> getCodeBlockMaterials() {
        return new HashSet<>(codeBlocks);
    }
    
    /**
     * Gets block configuration for a material
     */
    public BlockConfig getBlockConfig(Material material) {
        return materialToConfig.get(material);
    }
    
    /**
     * Gets the display name for a material
     */
    public String getBlockName(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getName() : material.name();
    }
    
    /**
     * Gets the description for a material
     */
    public String getBlockDescription(Material material) {
        BlockConfig config = materialToConfig.get(material);
        return config != null ? config.getDescription() : "";
    }
    
    /**
     * Reloads the configuration
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * Block configuration data class
     */
    public static class BlockConfig {
        private final Material material;
        private final String name;
        private final String description;
        private final List<String> actions;
        private final String defaultAction;
        
        public BlockConfig(Material material, String name, String description, 
                          List<String> actions, String defaultAction) {
            this.material = material;
            this.name = name;
            this.description = description;
            this.actions = new ArrayList<>(actions);
            this.defaultAction = defaultAction;
        }
        
        public Material getMaterial() { return material; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<String> getActions() { return actions; }
        public String getDefaultAction() { return defaultAction; }
        
        /**
         * Checks if this block supports a specific action
         */
        public boolean supportsAction(String action) {
            return actions.contains(action);
        }
        
        @Override
        public String toString() {
            return "BlockConfig{" +
                    "material=" + material +
                    ", name='" + name + '\'' +
                    ", defaultAction='" + defaultAction + '\'' +
                    ", actions=" + actions.size() +
                    '}';
        }
    }
}