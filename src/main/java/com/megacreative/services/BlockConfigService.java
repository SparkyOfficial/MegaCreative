package com.megacreative.services;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Сервис для загрузки и управления конфигурацией всех блоков из coding_blocks.yml.
 * Адаптирован под новый формат, где ID блока является его названием.
 */
public class BlockConfigService {

    private final MegaCreative plugin;
    private final Logger logger;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>(); // Ключ - ID блока (например, "onPlayerMove")
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>();

    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        load();
    }

    public void reload() {
        load();
    }
    
    public void load() {
        blockConfigs.clear();
        materialToBlockIds.clear();

        File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("coding_blocks.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Парсим каждый блок по его ID
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String materialName : blocksSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName);
                    ConfigurationSection materialSection = blocksSection.getConfigurationSection(materialName);
                    
                    if (materialSection != null) {
                        String displayName = materialSection.getString("name", materialName);
                        String type = materialSection.getString("type", "ACTION");
                        String description = materialSection.getString("description", "");
                        
                        List<String> actions = materialSection.getStringList("actions");
                        for (String actionId : actions) {
                            BlockConfig blockConfig = new BlockConfig(actionId, material, type, displayName, description, "general");
                            blockConfigs.put(actionId, blockConfig);
                            materialToBlockIds.computeIfAbsent(material, k -> new ArrayList<>()).add(actionId);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    logger.warning("Invalid material in coding_blocks.yml: " + materialName);
                }
            }
        }
        
        // Parse action configurations
        ConfigurationSection actionConfigsSection = config.getConfigurationSection("action_configurations");
        if (actionConfigsSection != null) {
            for (String actionId : actionConfigsSection.getKeys(false)) {
                BlockConfig existingConfig = blockConfigs.get(actionId);
                if (existingConfig != null) {
                    ConfigurationSection actionSection = actionConfigsSection.getConfigurationSection(actionId);
                    if (actionSection != null) {
                        existingConfig.loadParameters(actionSection);
                    }
                }
            }
        }
        
        logger.info("Loaded " + blockConfigs.size() + " block definitions.");
    }

    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }

    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return ids.stream().map(this::getBlockConfig).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean isCodeBlock(Material material) {
        return materialToBlockIds.containsKey(material);
    }
    
    public Set<Material> getCodeBlockMaterials() {
        return materialToBlockIds.keySet();
    }
    
    public Collection<BlockConfig> getAllBlockConfigs() {
        return blockConfigs.values();
    }
    
    public List<String> getAvailableActions(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        // Возвращаем ID блоков, так как они теперь являются действиями
        return new ArrayList<>(ids);
    }
    
    public List<String> getActionsForMaterial(Material material) {
        return getAvailableActions(material);
    }

    /**
     * Получает конфигурацию блока по его отображаемому имени (без цветовых кодов).
     * @param displayName Отображаемое имя
     * @return Конфигурация блока или null, если не найдена.
     */
    public BlockConfig getBlockConfigByDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (BlockConfig config : blockConfigs.values()) {
            if (org.bukkit.ChatColor.stripColor(config.getDisplayName()).equalsIgnoreCase(displayName)) {
                return config;
            }
        }
        return null;
    }

    public boolean isControlOrEventBlock(String blockType) {
        if (blockType == null) return false;
        return blockType.equalsIgnoreCase("EVENT") ||
               blockType.equalsIgnoreCase("CONTROL") ||
               blockType.equalsIgnoreCase("FUNCTION");
    }
    
    /**
     * Gets the first block configuration for a material
     */
    public BlockConfig getFirstBlockConfig(Material material) {
        List<String> ids = materialToBlockIds.get(material);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return getBlockConfig(ids.get(0));
    }
    
    /**
     * Gets the default action for a material
     */
    public String getDefaultAction(Material material) {
        BlockConfig config = getFirstBlockConfig(material);
        return config != null ? config.getId() : null;
    }
    
    /**
     * Gets the display name for a material
     */
    public String getBlockName(Material material) {
        BlockConfig config = getFirstBlockConfig(material);
        return config != null ? config.getDisplayName() : material.name();
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

    // Внутренний класс для хранения данных конфига
    public static class BlockConfig {
        private final String id;
        private final Material material;
        private final String type;
        private final String displayName;
        private final String description;
        private final String category;
        private final Map<String, ParameterConfig> parameters;

        public BlockConfig(String id, Material material, String type, String displayName, String description, String category) {
            this.id = id;
            this.material = material;
            this.type = type;
            this.displayName = displayName;
            this.description = description;
            this.category = category;
            this.parameters = new HashMap<>();
        }

        public void loadParameters(ConfigurationSection section) {
            ConfigurationSection slotsSection = section.getConfigurationSection("slots");
            if (slotsSection != null) {
                for (String slotKey : slotsSection.getKeys(false)) {
                    try {
                        int slotNumber = Integer.parseInt(slotKey);
                        ConfigurationSection slotSection = slotsSection.getConfigurationSection(slotKey);
                        if (slotSection != null) {
                            String paramName = slotSection.getString("slot_name", "slot_" + slotKey);
                            this.parameters.put(paramName, new ParameterConfig(paramName, slotSection));
                        }
                    } catch (NumberFormatException e) {
                        // Handle item groups
                        ConfigurationSection groupSection = slotsSection.getConfigurationSection(slotKey);
                        if (groupSection != null) {
                            // For now, we'll skip group processing in this simplified version
                        }
                    }
                }
            }
            
            // Handle item groups
            ConfigurationSection groupsSection = section.getConfigurationSection("item_groups");
            if (groupsSection != null) {
                for (String groupName : groupsSection.getKeys(false)) {
                    ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                    if (groupSection != null) {
                        // For now, we'll skip group processing in this simplified version
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
        public Map<String, ParameterConfig> getParameters() { return parameters; }
        public String getActionName() { return id; } // In the new system, ID and action name are the same
    }

    public static class ParameterConfig {
        private final String name;
        private final String displayName;
        private final String description;
        private final String placeholderItem;
        private final int slot;

        public ParameterConfig(String name, ConfigurationSection section) {
            this.name = name;
            this.displayName = section.getString("name", name);
            this.description = section.getString("description", "");
            this.placeholderItem = section.getString("placeholder_item", "PAPER");
            this.slot = section.getInt("slot", -1);
        }

        // Getters
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getPlaceholderItem() { return placeholderItem; }
        public int getSlot() { return slot; }
    }
}