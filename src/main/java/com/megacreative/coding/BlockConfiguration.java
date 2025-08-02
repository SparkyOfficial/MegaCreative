package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для загрузки и управления конфигурацией блоков кодинга.
 * Позволяет настраивать доступные блоки без перекомпиляции плагина.
 */
public class BlockConfiguration {
    
    private final MegaCreative plugin;
    private final Map<Material, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<String, String> actionDescriptions = new HashMap<>();
    private int maxBlocksPerScript = 100;
    private int maxRecursionDepth = 50;
    private int executionTimeoutSeconds = 30;
    
    public BlockConfiguration(MegaCreative plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }
    
    /**
     * Загружает конфигурацию блоков из файла
     */
    public void loadConfiguration() {
        try {
            // Сначала пытаемся загрузить из файла в папке плагина
            File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
            YamlConfiguration config;
            
            if (configFile.exists()) {
                config = YamlConfiguration.loadConfiguration(configFile);
            } else {
                // Если файл не существует, копируем из ресурсов
                plugin.saveResource("coding_blocks.yml", false);
                config = YamlConfiguration.loadConfiguration(configFile);
            }
            
            // Загружаем настройки по умолчанию
            ConfigurationSection defaults = config.getConfigurationSection("defaults");
            if (defaults != null) {
                maxBlocksPerScript = defaults.getInt("max_blocks_per_script", 100);
                maxRecursionDepth = defaults.getInt("max_recursion_depth", 50);
                executionTimeoutSeconds = defaults.getInt("execution_timeout_seconds", 30);
            }
            
            // Загружаем конфигурацию блоков
            ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
            if (blocksSection != null) {
                for (String materialName : blocksSection.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialName);
                        ConfigurationSection blockSection = blocksSection.getConfigurationSection(materialName);
                        
                        if (blockSection != null) {
                            String name = blockSection.getString("name", "Неизвестный блок");
                            String description = blockSection.getString("description", "");
                            List<String> actions = blockSection.getStringList("actions");
                            
                            BlockConfig blockConfig = new BlockConfig(name, description, actions);
                            blockConfigs.put(material, blockConfig);
                            
                            // Добавляем описания действий
                            for (String action : actions) {
                                actionDescriptions.put(action, description);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Неизвестный материал: " + materialName);
                    }
                }
            }
            
            plugin.getLogger().info("Загружена конфигурация для " + blockConfigs.size() + " блоков кодинга");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки конфигурации блоков: " + e.getMessage());
        }
    }
    
    /**
     * Получает конфигурацию для указанного материала
     */
    public BlockConfig getBlockConfig(Material material) {
        return blockConfigs.get(material);
    }
    
    /**
     * Проверяет, поддерживается ли действие для указанного материала
     */
    public boolean isActionSupported(Material material, String action) {
        BlockConfig config = blockConfigs.get(material);
        return config != null && config.getActions().contains(action);
    }
    
    /**
     * Получает список доступных действий для материала
     */
    public List<String> getAvailableActions(Material material) {
        BlockConfig config = blockConfigs.get(material);
        return config != null ? config.getActions() : List.of();
    }
    
    /**
     * Получает описание действия
     */
    public String getActionDescription(String action) {
        return actionDescriptions.getOrDefault(action, "Неизвестное действие");
    }
    
    /**
     * Получает максимальное количество блоков в скрипте
     */
    public int getMaxBlocksPerScript() {
        return maxBlocksPerScript;
    }
    
    /**
     * Получает максимальную глубину рекурсии
     */
    public int getMaxRecursionDepth() {
        return maxRecursionDepth;
    }
    
    /**
     * Получает таймаут выполнения в секундах
     */
    public int getExecutionTimeoutSeconds() {
        return executionTimeoutSeconds;
    }
    
    /**
     * Внутренний класс для хранения конфигурации блока
     */
    public static class BlockConfig {
        private final String name;
        private final String description;
        private final List<String> actions;
        
        public BlockConfig(String name, String description, List<String> actions) {
            this.name = name;
            this.description = description;
            this.actions = actions;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public List<String> getActions() {
            return actions;
        }
    }
} 