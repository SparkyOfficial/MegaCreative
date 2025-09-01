package com.megacreative.coding.config;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Service for managing block configurations and their mappings to BlockType enums.
 * Handles loading, saving, and querying block configurations.
 */
public class BlockConfigService {
    private final MegaCreative plugin;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<Material, Map<String, BlockType>> materialToBlockTypeMap = new HashMap<>();
    
    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        loadBlockConfigs();
        buildMaterialToBlockTypeMap();
    }
    
    /**
     * Loads block configurations from the config file.
     */
    public void loadBlockConfigs() {
        File configFile = new File(plugin.getDataFolder(), "blocks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("blocks.yml", false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        blockConfigs.clear();
        
        // Load block configurations
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                ConfigurationSection blockSection = blocksSection.getConfigurationSection(key);
                if (blockSection != null) {
                    BlockConfig blockConfig = new BlockConfig(blockSection);
                    blockConfigs.put(key, blockConfig);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + blockConfigs.size() + " block configurations");
    }
    
    /**
     * Builds a mapping from Material to BlockType for quick lookup.
     */
    private void buildMaterialToBlockTypeMap() {
        materialToBlockTypeMap.clear();
        
        for (BlockType blockType : BlockType.values()) {
            materialToBlockTypeMap
                .computeIfAbsent(blockType.getMaterial(), k -> new HashMap<>())
                .put(blockType.getActionName(), blockType);
        }
    }
    
    /**
     * Gets the BlockType for the given material and action name.
     * 
     * @param material The material of the block
     * @param actionName The action name from configuration
     * @return The matching BlockType, or null if not found
     */
    public BlockType getBlockType(Material material, String actionName) {
        if (material == null || actionName == null) {
            return null;
        }
        
        Map<String, BlockType> actions = materialToBlockTypeMap.get(material);
        return actions != null ? actions.get(actionName.toLowerCase()) : null;
    }
    
    /**
     * Gets all block configurations.
     * 
     * @return A collection of all block configurations
     */
    public Collection<BlockConfig> getAllBlockConfigs() {
        return blockConfigs.values();
    }
    
    /**
     * Gets a block configuration by its ID.
     * 
     * @param id The ID of the block configuration
     * @return The block configuration, or null if not found
     */
    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }
    
    /**
     * Saves all block configurations to the config file.
     * 
     * @return true if the save was successful, false otherwise
     */
    public boolean saveBlockConfigs() {
        try {
            File configFile = new File(plugin.getDataFolder(), "blocks.yml");
            FileConfiguration config = new YamlConfiguration();
            
            // Save block configurations
            ConfigurationSection blocksSection = config.createSection("blocks");
            for (Map.Entry<String, BlockConfig> entry : blockConfigs.entrySet()) {
                entry.getValue().save(blocksSection.createSection(entry.getKey()));
            }
            
            config.save(configFile);
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save block configurations", e);
            return false;
        }
    }
    
    /**
     * Updates a block configuration.
     * 
     * @param id The ID of the block configuration
     * @param config The new configuration
     * @return true if the update was successful, false otherwise
     */
    public boolean updateBlockConfig(String id, BlockConfig config) {
        if (id == null || config == null) {
            return false;
        }
        
        blockConfigs.put(id, config);
        return saveBlockConfigs();
    }
    
    /**
     * Deletes a block configuration.
     * 
     * @param id The ID of the block configuration to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteBlockConfig(String id) {
        if (id == null || !blockConfigs.containsKey(id)) {
            return false;
        }
        
        blockConfigs.remove(id);
        return saveBlockConfigs();
    }
}
