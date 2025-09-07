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
 * Единственный сервис для загрузки и управления конфигурацией всех блоков из coding_blocks.yml.
 * Заменяет BlockConfiguration.java и старые системы.
 */
public class BlockConfigService {
    
    private final MegaCreative plugin;
    private final Logger logger;
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>(); // Ключ - ID блока (onPlayerMove)
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>(); // Ключ - Материал, значение - список ID блоков

    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        load();
    }

    public void load() {
        blockConfigs.clear();
        materialToBlockIds.clear();

        File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("coding_blocks.yml", false); // Убедись, что твой "мастер-план" лежит в JAR
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) continue;

            try {
                BlockConfig blockConfig = new BlockConfig(id, section);
                blockConfigs.put(id, blockConfig);
                materialToBlockIds.computeIfAbsent(blockConfig.getMaterial(), k -> new ArrayList<>()).add(id);
            } catch (Exception e) {
                logger.warning("Failed to load block configuration for '" + id + "': " + e.getMessage());
            }
        }
        logger.info("Loaded " + blockConfigs.size() + " block definitions.");
    }

    // --- ОСНОВНЫЕ МЕТОДЫ ДОСТУПА ---

    /**
     * Получает конфигурацию блока по его ID (например, "onPlayerMove").
     */
    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }

    /**
     * Получает список всех возможных блоков для данного материала.
     */
    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return ids.stream().map(this::getBlockConfig).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Проверяет, является ли материал блоком кода.
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