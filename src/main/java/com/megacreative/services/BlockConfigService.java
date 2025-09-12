package com.megacreative.services;

import com.megacreative.MegaCreative;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.function.Function;

/**
 * Центральный сервис для загрузки и управления конфигурацией всех блоков из coding_blocks.yml.
 * Единственный источник правды о блоках.
 */
public class BlockConfigService {

    private final MegaCreative plugin;
    private final Logger logger;
    // Ключ - это ID блока из YAML (onPlayerMove, sendMessage и т.д.)
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>();
    // Configuration for action slots
    private ConfigurationSection actionConfigurations;

    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        load();
    }

    public void reload() {
        load();
    }

    private void load() {
        blockConfigs.clear();
        materialToBlockIds.clear();

        File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("coding_blocks.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        // Load action configurations
        actionConfigurations = config.getConfigurationSection("action_configurations");

        // ПРАВИЛЬНО: читаем ключи внутри секции blocks
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        if (blocksSection != null) {
            for (String id : blocksSection.getKeys(false)) {
                ConfigurationSection section = blocksSection.getConfigurationSection(id);
                if (section != null) {
                    try {
                        BlockConfig blockConfig = new BlockConfig(id, section);
                        blockConfigs.put(id, blockConfig);
                        materialToBlockIds.computeIfAbsent(blockConfig.getMaterial(), k -> new ArrayList<>()).add(id);
                    } catch (Exception e) {
                        logger.warning("Failed to load block config for ID '" + id + "': " + e.getMessage());
                    }
                }
            }
        }
        logger.info("Loaded " + blockConfigs.size() + " block definitions from coding_blocks.yml.");
    }
    
    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }

    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return ids.stream().map(this::getBlockConfig).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public List<String> getAvailableActions(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return new ArrayList<>(ids);
    }
    
    public boolean isCodeBlock(Material material) {
        return materialToBlockIds.containsKey(material);
    }
    
    public Set<Material> getCodeBlockMaterials() {
        return new HashSet<>(materialToBlockIds.keySet());
    }
    
    public Collection<BlockConfig> getAllBlockConfigs() {
        return blockConfigs.values();
    }
    
    public BlockConfig getBlockConfigByDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) return null;
        for (BlockConfig config : blockConfigs.values()) {
            if (ChatColor.stripColor(config.getDisplayName()).equalsIgnoreCase(displayName)) {
                return config;
            }
        }
        return null;
    }
    
    public BlockConfig getFirstBlockConfig(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        if (!ids.isEmpty()) {
            return getBlockConfig(ids.get(0));
        }
        return null;
    }
    
    /**
     * Gets the primary block configuration for a material (alias for getFirstBlockConfig)
     * @param material The material to search for
     * @return The first BlockConfig for this material, or null if none found
     */
    public BlockConfig getBlockConfigByMaterial(Material material) {
        return getFirstBlockConfig(material);
    }
    
    public boolean isControlOrEventBlock(String blockType) {
        if (blockType == null) return false;
        BlockConfig config = getBlockConfig(blockType);
        if (config == null) return false;
        String type = config.getType();
        return "CONTROL".equals(type) || "EVENT".equals(type);
    }
    
    /**
     * Gets the action configurations section
     * @return The action configurations section
     */
    public ConfigurationSection getActionConfigurations() {
        return actionConfigurations;
    }
    
    /**
     * Gets a slot resolver function for a specific action
     * @param actionName The name of the action
     * @return A function that maps slot names to slot indices, or null if not found
     */
    public Function<String, Integer> getSlotResolver(String actionName) {
        if (actionConfigurations == null) return null;
        
        ConfigurationSection actionConfig = actionConfigurations.getConfigurationSection(actionName);
        if (actionConfig == null) return null;
        
        ConfigurationSection slots = actionConfig.getConfigurationSection("slots");
        if (slots == null) return null;
        
        Map<String, Integer> slotMap = new HashMap<>();
        for (String slotKey : slots.getKeys(false)) {
            ConfigurationSection slotConfig = slots.getConfigurationSection(slotKey);
            if (slotConfig != null) {
                String slotName = slotConfig.getString("slot_name");
                if (slotName != null) {
                    try {
                        int slotIndex = Integer.parseInt(slotKey);
                        slotMap.put(slotName, slotIndex);
                    } catch (NumberFormatException e) {
                        // Ignore invalid slot indices
                    }
                }
            }
        }
        
        return slotMap::get;
    }
    
    /**
     * Gets a group slots resolver function for a specific action
     * @param actionName The name of the action
     * @return A function that maps group names to slot indices, or null if not found
     */
    public Function<String, int[]> getGroupSlotsResolver(String actionName) {
        if (actionConfigurations == null) return null;
        
        ConfigurationSection actionConfig = actionConfigurations.getConfigurationSection(actionName);
        if (actionConfig == null) return null;
        
        ConfigurationSection itemGroups = actionConfig.getConfigurationSection("item_groups");
        if (itemGroups == null) return null;
        
        Map<String, int[]> groupMap = new HashMap<>();
        for (String groupKey : itemGroups.getKeys(false)) {
            ConfigurationSection groupConfig = itemGroups.getConfigurationSection(groupKey);
            if (groupConfig != null) {
                int[] slots = groupConfig.getIntegerList("slots").stream().mapToInt(Integer::intValue).toArray();
                groupMap.put(groupKey, slots);
            }
        }
        
        return groupMap::get;
    }

    /**
     * Внутренний класс-модель для хранения данных одного блока из конфига.
     */
    public static class BlockConfig {
        private final String id;
        private final Material material;
        private final String type;
        private final String displayName;
        private final String description;
        private final String category;
        private final String defaultAction;  // 🔧 FIX: Add default action field
        private final boolean isConstructor;
        private final StructureConfig structure;
        private final Map<String, Object> parameters;

        public BlockConfig(String id, ConfigurationSection section) {
            this.id = id;
            // Материал определяется по ID (ключу) блока
            this.material = Material.matchMaterial(id);
            if (this.material == null) {
                throw new IllegalArgumentException("Invalid material specified for " + id);
            }
            this.type = section.getString("type", "ACTION").toUpperCase();
            // В YAML используется поле "name", не "displayName"
            this.displayName = ChatColor.translateAlternateColorCodes('&', section.getString("name", id));
            this.description = section.getString("description", "No description.");
            this.category = section.getString("category", "default");
            this.defaultAction = section.getString("default_action", null);  // 🔧 FIX: Read default action from config
            this.isConstructor = section.getBoolean("is_constructor", false);
            
            // Загружаем конфигурацию структуры, если блок является конструктором
            if (isConstructor && section.contains("structure")) {
                ConfigurationSection structureSection = section.getConfigurationSection("structure");
                this.structure = new StructureConfig(structureSection);
            } else {
                this.structure = null;
            }

            this.parameters = new HashMap<>();
            ConfigurationSection paramsSection = section.getConfigurationSection("parameters");
            if (paramsSection != null) {
                for (String key : paramsSection.getKeys(false)) {
                    this.parameters.put(key, paramsSection.get(key));
                }
            }
        }
        
        // Геттеры
        public String getId() { return id; }
        public Material getMaterial() { return material; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getDefaultAction() { return defaultAction; }  // 🔧 FIX: Add getter for default action
        public boolean isConstructor() { return isConstructor; }
        public StructureConfig getStructure() { return structure; }
        public Map<String, Object> getParameters() { return parameters; }
    }
    
    /**
     * Конфигурация структуры для блоков-конструкторов
     */
    public static class StructureConfig {
        private final Material brackets;
        private final boolean hasSign;
        private final int bracketDistance;
        
        public StructureConfig(ConfigurationSection section) {
            String bracketMaterial = section.getString("brackets", "PISTON");
            this.brackets = Material.matchMaterial(bracketMaterial);
            this.hasSign = section.getBoolean("sign", true);
            this.bracketDistance = section.getInt("bracket_distance", 3);
        }
        
        public Material getBrackets() { return brackets; }
        public boolean hasSign() { return hasSign; }
        public int getBracketDistance() { return bracketDistance; }
    }
}