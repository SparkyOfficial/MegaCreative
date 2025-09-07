package com.megacreative.config;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages block-related configurations for the plugin.
 */
public class BlockConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final Map<String, Material> blockMaterials = new HashMap<>();
    
    public BlockConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    /**
     * Loads the block configuration from the config file.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();
        
        // Load block materials
        blockMaterials.clear();
        if (config.contains("blocks")) {
            for (String key : config.getConfigurationSection("blocks").getKeys(false)) {
                String materialName = config.getString("blocks." + key);
                try {
                    Material material = Material.valueOf(materialName.toUpperCase());
                    blockMaterials.put(key, material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material type: " + materialName);
                }
            }
        }
    }
    
    /**
     * Gets the material for a specific block type.
     * @param blockType The type of block
     * @return The Material or null if not found
     */
    public Material getBlockMaterial(String blockType) {
        return blockMaterials.get(blockType);
    }
    
    /**
     * Gets all configured block materials.
     * @return Map of block types to their materials
     */
    public Map<String, Material> getBlockMaterials() {
        return new HashMap<>(blockMaterials);
    }
    
    /**
     * Reloads the configuration from disk.
     */
    public void reload() {
        plugin.reloadConfig();
        loadConfig();
    }
    
    /**
     * Saves the current configuration to disk.
     */
    public void save() {
        // Update config with current block materials
        for (Map.Entry<String, Material> entry : blockMaterials.entrySet()) {
            config.set("blocks." + entry.getKey(), entry.getValue().name());
        }
        
        // Save to disk
        plugin.saveConfig();
    }
}
