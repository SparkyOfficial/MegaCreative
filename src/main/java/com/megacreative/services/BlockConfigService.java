package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Service for managing block configurations from coding_blocks.yml
 * Handles loading, parsing, and querying block configurations
 */
public class BlockConfigService {
    private final MegaCreative plugin;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>();
    private final Map<String, Set<String>> categoryToBlockIds = new HashMap<>();

    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Loads the block configuration from coding_blocks.yml
     */
    public void loadConfig() {
        blockConfigs.clear();
        materialToBlockIds.clear();
        categoryToBlockIds.clear();

        // Load the configuration file
        File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        FileConfiguration config;

        if (!configFile.exists()) {
            // Copy default config from resources
            plugin.saveResource("coding_blocks.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Also load default config from resources to merge with user config
        InputStream defaultConfigStream = plugin.getResource("coding_blocks.yml");
        if (defaultConfigStream != null) {
            FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            // Merge default config with user config
            mergeConfigurations(config, defaultConfig);
        }

        // Parse blocks section
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String materialName : blocksSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName);
                    ConfigurationSection materialSection = blocksSection.getConfigurationSection(materialName);
                    
                    if (materialSection != null) {
                        // Parse actions for this material
                        List<String> actions = materialSection.getStringList("actions");
                        String displayName = materialSection.getString("name", materialName);
                        String description = materialSection.getString("description", "");
                        String type = materialSection.getString("type", "ACTION");
                        String category = materialSection.getString("category", "general");

                        // Create a block config for each action
                        for (String action : actions) {
                            String blockId = materialName + "_" + action;
                            BlockConfig blockConfig = new BlockConfig(blockId, material, type, displayName, description, category, action);
                            blockConfigs.put(blockId, blockConfig);
                            
                            // Add to material mapping
                            materialToBlockIds.computeIfAbsent(material, k -> new ArrayList<>()).add(blockId);
                            
                            // Add to category mapping
                            categoryToBlockIds.computeIfAbsent(category, k -> new HashSet<>()).add(blockId);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in coding_blocks.yml: " + materialName);
                }
            }
        }

        plugin.getLogger().info("Loaded " + blockConfigs.size() + " block configurations");
    }

    /**
     * Merges default configuration with user configuration
     */
    private void mergeConfigurations(FileConfiguration userConfig, FileConfiguration defaultConfig) {
        // This is a simple merge - in a real implementation you might want more sophisticated merging
        for (String key : defaultConfig.getKeys(true)) {
            if (!userConfig.contains(key)) {
                userConfig.set(key, defaultConfig.get(key));
            }
        }
    }

    /**
     * Reloads the block configuration
     */
    public void reload() {
        loadConfig();
    }

    /**
     * Checks if a block type is a control or event block
     */
    public boolean isControlOrEventBlock(String blockType) {
        return "CONTROL".equals(blockType) || "EVENT".equals(blockType);
    }
    
    /**
     * Gets a block configuration by its ID
     */
    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }

    /**
     * Gets the list of all block configurations for a material
     */
    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return ids.stream().map(this::getBlockConfig).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Checks if a material is a code block
     */
    public boolean isCodeBlock(Material material) {
        return materialToBlockIds.containsKey(material);
    }

    public Set<Material> getCodeBlockMaterials() {
        return materialToBlockIds.keySet();
    }

    public Collection<BlockConfig> getAllBlockConfigs() {
        return blockConfigs.values();
    }
    
    /**
     * Gets available actions for a material
     */
    public List<String> getAvailableActions(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return new ArrayList<>(ids);
    }
    
    /**
     * Gets the default action for a material
     */
    public String getDefaultAction(Material material) {
        List<String> actions = getAvailableActions(material);
        return actions.isEmpty() ? null : actions.get(0);
    }
    
    /**
     * Gets the display name for a material
     */
    public String getBlockName(Material material) {
        List<BlockConfig> configs = getBlockConfigsForMaterial(material);
        return configs.isEmpty() ? material.name() : configs.get(0).getDisplayName();
    }
    
    /**
     * Gets slot number for a parameter of a specific action
     * @param actionId The action ID (e.g., "sendMessage")
     * @param paramName The parameter name (e.g., "message_slot")
     * @return Slot number or -1 if not found
     */
    public int getSlotForParameter(String actionId, String paramName) {
        BlockConfig config = getBlockConfig(actionId);
        if (config != null) {
            ParameterConfig paramConfig = config.getParameters().get(paramName);
            if (paramConfig != null) {
                return paramConfig.getSlot();
            }
        }
        return -1;
    }
    
    /**
     * Gets a function that resolves slot numbers from parameter names for a specific action
     * @param actionId The action ID (e.g., "sendMessage")
     * @return Function that maps parameter names to slot numbers
     */
    public java.util.function.Function<String, Integer> getSlotResolver(String actionId) {
        return paramName -> getSlotForParameter(actionId, paramName);
    }
    
    /**
     * Gets a function that resolves group slots for a specific action
     * @param actionId The action ID (e.g., "giveItems")
     * @return Function that maps group names to slot lists
     */
    public java.util.function.Function<String, List<Integer>> getGroupSlotsResolver(String actionId) {
        return groupName -> {
            BlockConfig config = getBlockConfig(actionId);
            if (config != null) {
                List<Integer> slots = new ArrayList<>();
                // In a real implementation, you would look up group slots from the config
                // For now, we'll return an empty list
                return slots;
            }
            return Collections.emptyList();
        };
    }
    
    /**
     * Получает список всех возможных действий (ID блоков) для данного материала.
     */
    public List<String> getAvailableActionsForMaterial(Material material) {
        return materialToBlockIds.getOrDefault(material, Collections.emptyList());
    }
    
    /**
     * Получает первую конфигурацию блока для данного материала.
     * Полезно для DevWorldProtectionListener и других мест, где нужна базовая информация.
     */
    public BlockConfig getFirstBlockConfig(Material material) {
        List<String> ids = materialToBlockIds.get(material);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return getBlockConfig(ids.get(0));
    }
    
    /**
     * Gets a block configuration by material
     * This method gets the first block config for a material
     */
    public BlockConfig getBlockConfig(Material material) {
        List<String> ids = materialToBlockIds.get(material);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return getBlockConfig(ids.get(0));
    }

    // --- Внутренние классы для хранения данных ---

    public static class BlockConfig {
        private final String id;
        private final Material material;
        private final String type;
        private final String displayName;
        private final String description;
        private final String category;
        private final String actionName;
        private final Map<String, ParameterConfig> parameters;

        public BlockConfig(String id, Material material, String type, String displayName, 
                          String description, String category, String actionName) {
            this.id = id;
            this.material = material;
            this.type = type;
            this.displayName = displayName;
            this.description = description;
            this.category = category;
            this.actionName = actionName;
            
            // Initialize with empty parameters map
            this.parameters = new HashMap<>();
        }

        // Constructor for block configs with parameters
        public BlockConfig(String id, ConfigurationSection section) {
            this.id = id;
            this.material = Material.valueOf(section.getString("material"));
            this.type = section.getString("type", "ACTION");
            this.displayName = section.getString("displayName", id);
            this.description = section.getString("description", "");
            this.category = section.getString("category", "general");
            this.actionName = id; // In the new system, ID and action name are the same
            
            // Parse parameters
            this.parameters = new HashMap<>();
            ConfigurationSection paramsSection = section.getConfigurationSection("parameters");
            if (paramsSection != null) {
                for (String paramName : paramsSection.getKeys(false)) {
                    ConfigurationSection paramSection = paramsSection.getConfigurationSection(paramName);
                    if (paramSection != null) {
                        ParameterConfig paramConfig = new ParameterConfig(paramName, paramSection);
                        this.parameters.put(paramName, paramConfig);
                    }
                }
            }
        }

        // Getters
        public String getId() { return id; }
        public Material getMaterial() { return material; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getActionName() { return actionName; }
        public Map<String, ParameterConfig> getParameters() { return parameters; }
    }

    public static class ParameterConfig {
        private final String name;
        private final String type;
        private final int slot;
        private final String description;

        public ParameterConfig(String name, ConfigurationSection section) {
            this.name = name;
            this.type = section.getString("type", "TEXT");
            this.slot = section.getInt("slot", -1);
            this.description = section.getString("description", "");
        }

        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public int getSlot() { return slot; }
        public String getDescription() { return description; }
    }
}