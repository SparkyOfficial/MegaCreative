package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для загрузки и управления конфигурацией блоков кодинга.
 * Позволяет настраивать доступные блоки без перекомпиляции плагина.
 * Поддерживает именованные слоты и группы предметов.
 */
public class BlockConfiguration {
    
    private final MegaCreative plugin;
    private final Map<Material, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<String, String> actionDescriptions = new HashMap<>();
    
    // --- НОВЫЕ ПОЛЯ ДЛЯ ИМЕНОВАННЫХ СЛОТОВ И ГРУПП ---
    private final Map<String, ActionSlotConfig> actionSlotConfigs = new HashMap<>();
    private final Map<String, ActionGroupConfig> actionGroupConfigs = new HashMap<>();
    
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
            // Загружаем файл конфигурации
            File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
            YamlConfiguration config;
            
            if (configFile.exists()) {
                config = YamlConfiguration.loadConfiguration(configFile);
            } else {
                // Если файл не существует, копируем из ресурсов
                plugin.saveResource("coding_blocks.yml", false);
                config = YamlConfiguration.loadConfiguration(configFile);
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
            
            // --- ЗАГРУЗКА КОНФИГУРАЦИИ ИМЕНОВАННЫХ СЛОТОВ И ГРУПП ---
            ConfigurationSection actionConfigsSection = config.getConfigurationSection("action_configurations");
            if (actionConfigsSection != null) {
                for (String actionName : actionConfigsSection.getKeys(false)) {
                    ConfigurationSection actionSection = actionConfigsSection.getConfigurationSection(actionName);
                    if (actionSection != null) {
                        loadActionSlotConfig(actionName, actionSection);
                        loadActionGroupConfig(actionName, actionSection);
                    }
                }
            }
            
            plugin.getLogger().info("Загружена конфигурация для " + blockConfigs.size() + " блоков кодинга");
            plugin.getLogger().info("Загружено " + actionSlotConfigs.size() + " конфигураций слотов");
            plugin.getLogger().info("Загружено " + actionGroupConfigs.size() + " конфигураций групп");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка загрузки конфигурации блоков: " + e.getMessage());
        }
    }
    
    /**
     * Загружает конфигурацию слотов для действия
     */
    private void loadActionSlotConfig(String actionName, ConfigurationSection actionSection) {
        ConfigurationSection slotsSection = actionSection.getConfigurationSection("slots");
        if (slotsSection != null) {
            Map<Integer, SlotConfig> slots = new HashMap<>();
            
            for (String slotNumberStr : slotsSection.getKeys(false)) {
                try {
                    int slotNumber = Integer.parseInt(slotNumberStr);
                    ConfigurationSection slotSection = slotsSection.getConfigurationSection(slotNumberStr);
                    
                    if (slotSection != null) {
                        String name = slotSection.getString("name", "Неизвестный слот");
                        String description = slotSection.getString("description", "");
                        String placeholderItem = slotSection.getString("placeholder_item", "STONE");
                        String slotName = slotSection.getString("slot_name", "slot_" + slotNumber);
                        
                        slots.put(slotNumber, new SlotConfig(name, description, placeholderItem, slotName));
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Некорректный номер слота: " + slotNumberStr);
                }
            }
            
            if (!slots.isEmpty()) {
                actionSlotConfigs.put(actionName, new ActionSlotConfig(slots));
            }
        }
    }
    
    /**
     * Загружает конфигурацию групп для действия
     */
    private void loadActionGroupConfig(String actionName, ConfigurationSection actionSection) {
        ConfigurationSection groupsSection = actionSection.getConfigurationSection("item_groups");
        if (groupsSection != null) {
            Map<String, GroupConfig> groups = new HashMap<>();
            
            for (String groupName : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
                
                if (groupSection != null) {
                    List<Integer> slots = groupSection.getIntegerList("slots");
                    String name = groupSection.getString("name", "Группа предметов");
                    String description = groupSection.getString("description", "");
                    String placeholderItem = groupSection.getString("placeholder_item", "CHEST");
                    
                    groups.put(groupName, new GroupConfig(slots, name, description, placeholderItem));
                }
            }
            
            if (!groups.isEmpty()) {
                actionGroupConfigs.put(actionName, new ActionGroupConfig(groups));
            }
        }
    }
    
    /**
     * Получает конфигурацию для указанного материала
     */
    public BlockConfig getBlockConfig(Material material) {
        return blockConfigs.get(material);
    }
    
    /**
     * Получает список действий для указанного материала
     */
    public List<String> getActionsForMaterial(Material material) {
        BlockConfig config = getBlockConfig(material);
        return config != null ? config.getActions() : null;
    }
    
    /**
     * Получает номер слота по имени слота для действия
     */
    public Integer getSlotNumber(String actionName, String slotName) {
        ActionSlotConfig config = actionSlotConfigs.get(actionName);
        if (config != null) {
            for (Map.Entry<Integer, SlotConfig> entry : config.getSlots().entrySet()) {
                if (entry.getValue().getSlotName().equals(slotName)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    /**
     * Получает слоты для группы предметов
     */
    public List<Integer> getSlotsForGroup(String actionName, String groupName) {
        ActionGroupConfig config = actionGroupConfigs.get(actionName);
        if (config != null) {
            GroupConfig group = config.getGroups().get(groupName);
            return group != null ? group.getSlots() : new ArrayList<>();
        }
        return new ArrayList<>();
    }
    
    /**
     * Получает конфигурацию слотов для действия
     */
    public ActionSlotConfig getActionSlotConfig(String actionName) {
        return actionSlotConfigs.get(actionName);
    }
    
    /**
     * Получает конфигурацию групп для действия
     */
    public ActionGroupConfig getActionGroupConfig(String actionName) {
        return actionGroupConfigs.get(actionName);
    }

    /**
     * Возвращает агрегированную конфигурацию действия (слоты + группы).
     * @param actionName Имя действия
     * @return ActionConfig или null, если для действия нет ни слотов, ни групп
     */
    public ActionConfig getActionConfig(String actionName) {
        ActionSlotConfig slots = actionSlotConfigs.get(actionName);
        ActionGroupConfig groups = actionGroupConfigs.get(actionName);
        boolean hasSlots = slots != null && !slots.getSlots().isEmpty();
        boolean hasGroups = groups != null && !groups.getGroups().isEmpty();
        if (!hasSlots && !hasGroups) {
            return null;
        }
        return new ActionConfig(
            hasSlots ? new HashMap<>(slots.getSlots()) : new HashMap<>(),
            hasGroups ? new HashMap<>(groups.getGroups()) : new HashMap<>()
        );
    }

    /**
     * Проверяет, есть ли конфигурация (слоты или группы) для действия
     */
    public boolean hasActionConfig(String actionName) {
        ActionSlotConfig slots = actionSlotConfigs.get(actionName);
        ActionGroupConfig groups = actionGroupConfigs.get(actionName);
        return (slots != null && !slots.getSlots().isEmpty()) || (groups != null && !groups.getGroups().isEmpty());
    }
    
    /**
     * Создает placeholder предмет для слота
     */
    public ItemStack createPlaceholderItem(String actionName, int slotNumber) {
        ActionSlotConfig config = actionSlotConfigs.get(actionName);
        if (config != null) {
            SlotConfig slotConfig = config.getSlots().get(slotNumber);
            if (slotConfig != null) {
                try {
                    Material material = Material.valueOf(slotConfig.getPlaceholderItem());
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    
                    if (meta != null) {
                        meta.setDisplayName(slotConfig.getName());
                        
                        List<String> lore = new ArrayList<>();
                        lore.add(slotConfig.getDescription());
                        meta.setLore(lore);
                        
                        item.setItemMeta(meta);
                    }
                    
                    return item;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал для placeholder: " + slotConfig.getPlaceholderItem());
                }
            }
        }
        
        // Fallback
        ItemStack fallback = new ItemStack(Material.STONE);
        ItemMeta meta = fallback.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Слот " + slotNumber);
            fallback.setItemMeta(meta);
        }
        return fallback;
    }
    
    /**
     * Создает placeholder предмет для группы
     */
    public ItemStack createGroupPlaceholderItem(String actionName, String groupName) {
        ActionGroupConfig config = actionGroupConfigs.get(actionName);
        if (config != null) {
            GroupConfig groupConfig = config.getGroups().get(groupName);
            if (groupConfig != null) {
                try {
                    Material material = Material.valueOf(groupConfig.getPlaceholderItem());
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    
                    if (meta != null) {
                        meta.setDisplayName(groupConfig.getName());
                        
                        List<String> lore = new ArrayList<>();
                        lore.add(groupConfig.getDescription());
                        lore.add("§7Слоты: " + groupConfig.getSlots().toString());
                        meta.setLore(lore);
                        
                        item.setItemMeta(meta);
                    }
                    
                    return item;
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Неизвестный материал для group placeholder: " + groupConfig.getPlaceholderItem());
                }
            }
        }
        
        // Fallback
        ItemStack fallback = new ItemStack(Material.CHEST);
        ItemMeta meta = fallback.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§7Группа " + groupName);
            fallback.setItemMeta(meta);
        }
        return fallback;
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
    
    /**
     * Внутренний класс для хранения конфигурации слота
     */
    public static class SlotConfig {
        private final String name;
        private final String description;
        private final String placeholderItem;
        private final String slotName;
        
        public SlotConfig(String name, String description, String placeholderItem, String slotName) {
            this.name = name;
            this.description = description;
            this.placeholderItem = placeholderItem;
            this.slotName = slotName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getPlaceholderItem() {
            return placeholderItem;
        }
        
        public String getSlotName() {
            return slotName;
        }
    }
    
    /**
     * Внутренний класс для хранения конфигурации группы
     */
    public static class GroupConfig {
        private final List<Integer> slots;
        private final String name;
        private final String description;
        private final String placeholderItem;
        
        public GroupConfig(List<Integer> slots, String name, String description, String placeholderItem) {
            this.slots = slots;
            this.name = name;
            this.description = description;
            this.placeholderItem = placeholderItem;
        }
        
        public List<Integer> getSlots() {
            return slots;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getPlaceholderItem() {
            return placeholderItem;
        }
    }
    
    /**
     * Внутренний класс для хранения конфигурации слотов действия
     */
    public static class ActionSlotConfig {
        private final Map<Integer, SlotConfig> slots;
        
        public ActionSlotConfig(Map<Integer, SlotConfig> slots) {
            this.slots = slots;
        }
        
        public Map<Integer, SlotConfig> getSlots() {
            return slots;
        }
    }
    
    /**
     * Внутренний класс для хранения конфигурации групп действия
     */
    public static class ActionGroupConfig {
        private final Map<String, GroupConfig> groups;
        
        public ActionGroupConfig(Map<String, GroupConfig> groups) {
            this.groups = groups;
        }
        
        public Map<String, GroupConfig> getGroups() {
            return groups;
        }
    }

    /**
     * Агрегированная конфигурация для действия, объединяющая именные слоты и группы предметов.
     */
    public static class ActionConfig {
        private final Map<Integer, SlotConfig> slots;
        private final Map<String, GroupConfig> groups;

        public ActionConfig(Map<Integer, SlotConfig> slots, Map<String, GroupConfig> groups) {
            this.slots = slots;
            this.groups = groups;
        }

        public boolean hasSlots() { return slots != null && !slots.isEmpty(); }
        public boolean hasGroups() { return groups != null && !groups.isEmpty(); }
        public Map<Integer, SlotConfig> getSlots() { return slots; }
        public Map<String, GroupConfig> getGroups() { return groups; }
    }
} 