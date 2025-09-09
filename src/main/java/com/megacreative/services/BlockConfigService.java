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

        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
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
        logger.info("Loaded " + blockConfigs.size() + " block definitions from coding_blocks.yml.");
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
        private final Map<String, Object> parameters;

        public BlockConfig(String id, ConfigurationSection section) {
            this.id = id;
            this.material = Material.matchMaterial(section.getString("material", "STONE"));
            if (this.material == null) {
                throw new IllegalArgumentException("Invalid material specified for " + id);
            }
            this.type = section.getString("type", "ACTION").toUpperCase();
            this.displayName = ChatColor.translateAlternateColorCodes('&', section.getString("displayName", id));
            this.description = section.getString("description", "No description.");
            this.category = section.getString("category", "default");

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
        public Map<String, Object> getParameters() { return parameters; }
    }
}