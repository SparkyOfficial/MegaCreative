package com.megacreative.services;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for loading and managing block configurations from coding_blocks.yml
 * Replaces hardcoded material mappings with config-driven approach
 * Updated to work with the new advanced configuration format
 */
public class BlockConfigService {
    
    private static final Logger log = Logger.getLogger(BlockConfigService.class.getName());
    
    private final Plugin plugin;
    private final Map<String, BlockConfig> actionToConfig = new HashMap<>(); // action name -> config
    private final Map<Material, List<BlockConfig>> materialToConfigs = new HashMap<>(); // material -> list of configs
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
                // Save the new configuration file
                plugin.saveResource("new_coding_blocks.yml", false);
                // Rename it to coding_blocks.yml
                File newConfigFile = new File(plugin.getDataFolder(), "new_coding_blocks.yml");
                newConfigFile.renameTo(configFile);
            }
            
            config = YamlConfiguration.loadConfiguration(configFile);
            parseBlockConfigurations();
            
            log.info("Loaded " + actionToConfig.size() + " block configurations from coding_blocks.yml");
            
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to load block configurations", e);
        }
    }
    
    /**
     * Parses block configurations from the YAML config
     * Updated to work with the new advanced configuration format
     */
    private void parseBlockConfigurations() {
        actionToConfig.clear();
        materialToConfigs.clear();
        codeBlocks.clear();
        
        // In the new format, each key is an action name, not a material
        for (String actionName : config.getKeys(false)) {
            // Skip non-block configuration sections
            if (actionName.equals("defaults") || actionName.equals("blocks")) {
                continue;
            }
            
            ConfigurationSection blockSection = config.getConfigurationSection(actionName);
            if (blockSection != null) {
                try {
                    BlockConfig blockConfig = parseAdvancedBlockConfig(actionName, blockSection);
                    actionToConfig.put(actionName, blockConfig);
                    
                    // Add to material mapping
                    materialToConfigs.computeIfAbsent(blockConfig.getMaterial(), k -> new ArrayList<>()).add(blockConfig);
                    codeBlocks.add(blockConfig.getMaterial());
                } catch (Exception e) {
                    log.warning("Failed to parse block config for action: " + actionName + " - " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Parses individual block configuration in the new advanced format
     */
    private BlockConfig parseAdvancedBlockConfig(String actionName, ConfigurationSection section) {
        String materialName = section.getString("material");
        Material material = Material.valueOf(materialName);
        
        String displayName = section.getString("displayName", actionName);
        String description = section.getString("description", "");
        String type = section.getString("type", "ACTION"); // Default to ACTION if not specified
        String category = section.getString("category", "general");
        
        // Parse parameters
        Map<String, ParameterConfig> parameters = new HashMap<>();
        ConfigurationSection paramsSection = section.getConfigurationSection("parameters");
        if (paramsSection != null) {
            for (String paramName : paramsSection.getKeys(false)) {
                ConfigurationSection paramSection = paramsSection.getConfigurationSection(paramName);
                if (paramSection != null) {
                    String paramType = paramSection.getString("type", "TEXT");
                    int slot = paramSection.getInt("slot", -1);
                    String paramDescription = paramSection.getString("description", "");
                    
                    parameters.put(paramName, new ParameterConfig(paramName, paramType, slot, paramDescription));
                }
            }
        }
        
        return new BlockConfig(actionName, material, displayName, description, type, category, parameters);
    }
    
    /**
     * Gets block configuration for an action name
     */
    public BlockConfig getBlockConfig(String actionName) {
        return actionToConfig.get(actionName);
    }
    
    /**
     * Gets all block configurations for a material
     */
    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        return materialToConfigs.getOrDefault(material, new ArrayList<>());
    }
    
    /**
     * Gets the first block configuration for a material (for backward compatibility)
     */
    public BlockConfig getBlockConfig(Material material) {
        List<BlockConfig> configs = materialToConfigs.get(material);
        return configs != null && !configs.isEmpty() ? configs.get(0) : null;
    }
    
    /**
     * Gets all block configurations
     */
    public Collection<BlockConfig> getAllBlockConfigs() {
        return actionToConfig.values();
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
     * Gets the display name for an action
     */
    public String getBlockName(String actionName) {
        BlockConfig config = actionToConfig.get(actionName);
        return config != null ? config.getDisplayName() : actionName;
    }
    
    /**
     * Gets the block type for an action
     */
    public String getBlockType(String actionName) {
        BlockConfig config = actionToConfig.get(actionName);
        return config != null ? config.getType() : "ACTION";
    }
    
    /**
     * Checks if a block type is a control or event block
     */
    public boolean isControlOrEventBlock(String type) {
        return "EVENT".equals(type) || "CONTROL".equals(type) || "FUNCTION".equals(type);
    }
    
    /**
     * Checks if an action is a control or event block
     */
    public boolean isControlOrEventBlock(String actionName) {
        String type = getBlockType(actionName);
        return isControlOrEventBlock(type);
    }
    
    /**
     * Reloads the configuration
     */
    public void reload() {
        loadConfig();
    }
    
    /**
     * Block configuration data class for the new advanced format
     */
    public static class BlockConfig {
        private final String actionName;
        private final Material material;
        private final String displayName;
        private final String description;
        private final String type;
        private final String category;
        private final Map<String, ParameterConfig> parameters;
        
        public BlockConfig(String actionName, Material material, String displayName, String description, 
                          String type, String category, Map<String, ParameterConfig> parameters) {
            this.actionName = actionName;
            this.material = material;
            this.displayName = displayName;
            this.description = description;
            this.type = type;
            this.category = category;
            this.parameters = new HashMap<>(parameters);
        }
        
        public String getActionName() { return actionName; }
        public Material getMaterial() { return material; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public String getCategory() { return category; }
        public Map<String, ParameterConfig> getParameters() { return parameters; }
        
        /**
         * Checks if this block has a specific parameter
         */
        public boolean hasParameter(String paramName) {
            return parameters.containsKey(paramName);
        }
        
        /**
         * Gets a parameter configuration
         */
        public ParameterConfig getParameter(String paramName) {
            return parameters.get(paramName);
        }
        
        @Override
        public String toString() {
            return "BlockConfig{" +
                    "actionName='" + actionName + '\'' +
                    ", material=" + material +
                    ", displayName='" + displayName + '\'' +
                    ", type='" + type + '\'' +
                    ", parameters=" + parameters.size() +
                    '}';
        }
    }
    
    /**
     * Parameter configuration data class
     */
    public static class ParameterConfig {
        private final String name;
        private final String type;
        private final int slot;
        private final String description;
        
        public ParameterConfig(String name, String type, int slot, String description) {
            this.name = name;
            this.type = type;
            this.slot = slot;
            this.description = description;
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public int getSlot() { return slot; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            return "ParameterConfig{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", slot=" + slot +
                    '}';
        }
    }
}