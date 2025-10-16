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
 * Центральный сервис для загрузки и управления конфигурацией всех блоков из coding_blocks.yml
 * Единственный источник правды о блоках
 *
 * Central service for loading and managing configuration of all blocks from coding_blocks.yml
 * Single source of truth about blocks
 *
 * Zentraler Dienst zum Laden und Verwalten der Konfiguration aller Blöcke aus coding_blocks.yml
 * Einzelne Quelle der Wahrheit über Blöcke
 */
public class BlockConfigService {

    private final MegaCreative plugin;
    private final Logger logger;
    
    
    
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>();
    
    
    
    private ConfigurationSection actionConfigurations;

    /**
     * Инициализирует сервис конфигурации блоков
     * @param plugin Экземпляр основного плагина
     *
     * Initializes block configuration service
     * @param plugin Main plugin instance
     *
     * Initialisiert den Blockkonfigurationsdienst
     * @param plugin Hauptplugin-Instanz
     */
    public BlockConfigService(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        load();
    }

    /**
     * Перезагружает конфигурацию блоков
     *
     * Reloads block configuration
     *
     * Lädt die Blockkonfiguration neu
     */
    public void reload() {
        load();
        
        loadActionParametersForAllBlocks();
    }

    /**
     * Loads action parameters for all block configurations
     * This should be called after the service registry is fully initialized
     */
    public void loadActionParametersForAllBlocks() {
        if (plugin.getServiceRegistry() == null) {
            plugin.getLogger().warning("ServiceRegistry not available, deferring action parameter loading");
            return;
        }
        
        plugin.getLogger().info("Loading action parameters for all block configurations...");
        for (BlockConfig config : blockConfigs.values()) {
            try {
                config.loadActionParameters(plugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load action parameters for block config " + config.getId() + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Finished loading action parameters for all block configurations");
    }
    
    /**
     * Ensures all block configurations have their materials properly set
     * This fixes an issue where materials might not be set correctly during initial loading
     */
    public void ensureMaterialsAreSet() {
        for (BlockConfig config : blockConfigs.values()) {
            if (config.getMaterial() == null) {
                
                Material material = Material.matchMaterial(config.getId());
                if (material != null) {
                    config.setMaterial(material);
                    
                    materialToBlockIds.computeIfAbsent(material, k -> new ArrayList<>()).add(config.getId());
                    plugin.getLogger().info("Set material " + material.name() + " for block config " + config.getId());
                }
            }
        }
    }

    /**
     * Загружает конфигурацию блоков из файла
     *
     * Loads block configuration from file
     *
     * Lädt die Blockkonfiguration aus der Datei
     */
    private void load() {
        blockConfigs.clear();
        materialToBlockIds.clear();

        File configFile = new File(plugin.getDataFolder(), "coding_blocks.yml");
        if (!configFile.exists()) {
            plugin.saveResource("coding_blocks.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        
        plugin.getLogger().info("Loading coding_blocks.yml from: " + configFile.getAbsolutePath());
        plugin.getLogger().info("File exists: " + configFile.exists());
        
        
        
        
        actionConfigurations = config.getConfigurationSection("action_configurations");
        plugin.getLogger().info("Action configurations loaded: " + (actionConfigurations != null));

        
        
        
        ConfigurationSection blocksSection = config.getConfigurationSection("blocks");
        plugin.getLogger().info("Blocks section exists: " + (blocksSection != null));
        
        if (blocksSection != null) {
            plugin.getLogger().info("Blocks section keys: " + blocksSection.getKeys(false).size());
            Set<String> seenIds = new HashSet<>();
            for (String id : blocksSection.getKeys(false)) {
                if (!seenIds.add(id)) {
                    plugin.getLogger().severe("Duplicate block ID detected in coding_blocks.yml: " + id + ". This ID must be unique.");
                    continue;
                }
                ConfigurationSection section = blocksSection.getConfigurationSection(id);
                if (section != null) {
                    try {
                        plugin.getLogger().info("Loading block config: " + id);
                        BlockConfig blockConfig = new BlockConfig(id, section, plugin);
                        blockConfigs.put(id, blockConfig);
                        
                        
                        Material material = Material.matchMaterial(id);
                        if (material != null) {
                            
                            blockConfig.setMaterial(material);
                            List<String> ids = materialToBlockIds.computeIfAbsent(material, k -> new ArrayList<>());
                            if (ids.contains(id)) {
                                plugin.getLogger().severe("Duplicate mapping for material " + material + " to block ID " + id + ". Skipping duplicate.");
                            } else {
                                ids.add(id);
                            }
                            plugin.getLogger().info("Successfully loaded block config: " + id + " with material " + material);
                        } else {
                            plugin.getLogger().warning("Invalid material for block config: " + id);
                            
                            
                            
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load block config for ID '" + id + "': " + e.getMessage());
                        
                        
                        e.printStackTrace(); 
                    }
                } else {
                    plugin.getLogger().warning("Section is null for ID: " + id);
                }
            }
        }
        plugin.getLogger().info("Loaded " + blockConfigs.size() + " block definitions from coding_blocks.yml.");
        
        
        
        
        ensureMaterialsAreSet();

        // Validate uniqueness across actions lists
        validateUniqueActionsPerMaterial();
        
        
        plugin.getLogger().info("Loaded materials: " + materialToBlockIds.keySet().size());
        for (Material material : materialToBlockIds.keySet()) {
            List<String> actions = materialToBlockIds.get(material);
            plugin.getLogger().info("Material: " + material.name() + " -> Actions: " + (actions != null ? actions.size() : 0));
            if (actions != null) {
                for (String action : actions) {
                    plugin.getLogger().info("  - " + action);
                }
            }
        }
    }

    /**
     * Validates that action identifiers are unique per material to avoid ambiguous resolution
     */
    private void validateUniqueActionsPerMaterial() {
        for (Map.Entry<Material, List<String>> entry : materialToBlockIds.entrySet()) {
            Material material = entry.getKey();
            Map<String, Integer> counts = new HashMap<>();
            for (String id : entry.getValue()) {
                BlockConfig cfg = blockConfigs.get(id);
                if (cfg == null) continue;
                for (String action : cfg.getActions()) {
                    counts.merge(action, 1, Integer::sum);
                }
            }
            for (Map.Entry<String, Integer> c : counts.entrySet()) {
                if (c.getValue() > 1) {
                    logger.severe("Duplicate action '" + c.getKey() + "' for material " + material + " across multiple block IDs. Please fix coding_blocks.yml");
                }
            }
        }
    }
    
    /**
     * Получает конфигурацию блока по ID
     * @param id ID блока
     * @return Конфигурация блока или null, если не найдена
     *
     * Gets block configuration by ID
     * @param id Block ID
     * @return Block configuration or null if not found
     *
     * Ruft die Blockkonfiguration nach ID ab
     * @param id Block-ID
     * @return Blockkonfiguration oder null, wenn nicht gefunden
     */
    public BlockConfig getBlockConfig(String id) {
        return blockConfigs.get(id);
    }

    /**
     * Получает конфигурации блоков для материала
     * @param material Материал блока
     * @return Список конфигураций блоков
     *
     * Gets block configurations for material
     * @param material Block material
     * @return List of block configurations
     *
     * Ruft Blockkonfigurationen für Material ab
     * @param material Blockmaterial
     * @return Liste der Blockkonfigurationen
     */
    public List<BlockConfig> getBlockConfigsForMaterial(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return ids.stream().map(this::getBlockConfig).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Получает доступные действия для материала
     * @param material Материал блока
     * @return Список доступных действий
     *
     * Gets available actions for material
     * @param material Block material
     * @return List of available actions
     *
     * Ruft verfügbare Aktionen für Material ab
     * @param material Blockmaterial
     * @return Liste der verfügbaren Aktionen
     */
    public List<String> getAvailableActions(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        return new ArrayList<>(ids);
    }
    
    /**
     * Получает действия для материала
     * @param material Материал блока
     * @return Список действий
     *
     * Gets actions for material
     * @param material Block material
     * @return List of actions
     *
     * Ruft Aktionen für Material ab
     * @param material Blockmaterial
     * @return Liste der Aktionen
     */
    public List<String> getActionsForMaterial(Material material) {
        List<BlockConfig> configs = getBlockConfigsForMaterial(material);
        List<String> actions = new ArrayList<>();
        
        for (BlockConfig config : configs) {
            actions.addAll(config.getActions());
        }
        
        return actions;
    }
    
    /**
     * Проверяет, является ли материал кодовым блоком
     * @param material Материал для проверки
     * @return true если материал является кодовым блоком, иначе false
     *
     * Checks if material is a code block
     * @param material Material to check
     * @return true if material is a code block, false otherwise
     *
     * Prüft, ob das Material ein Codeblock ist
     * @param material Zu prüfendes Material
     * @return true, wenn das Material ein Codeblock ist, sonst false
     */
    public boolean isCodeBlock(Material material) {
        return materialToBlockIds.containsKey(material);
    }
    
    /**
     * Получает материалы кодовых блоков
     * @return Набор материалов кодовых блоков
     *
     * Gets code block materials
     * @return Set of code block materials
     *
     * Ruft Codeblock-Materialien ab
     * @return Menge der Codeblock-Materialien
     */
    public Set<Material> getCodeBlockMaterials() {
        return new HashSet<>(materialToBlockIds.keySet());
    }
    
    /**
     * Получает все конфигурации блоков
     * @return Коллекция всех конфигураций блоков
     *
     * Gets all block configurations
     * @return Collection of all block configurations
     *
     * Ruft alle Blockkonfigurationen ab
     * @return Sammlung aller Blockkonfigurationen
     */
    public Collection<BlockConfig> getAllBlockConfigs() {
        return blockConfigs.values();
    }
    
    /**
     * Получает конфигурацию блока по отображаемому имени
     * @param displayName Отображаемое имя блока
     * @return Конфигурация блока или null, если не найдена
     *
     * Gets block configuration by display name
     * @param displayName Block display name
     * @return Block configuration or null if not found
     *
     * Ruft die Blockkonfiguration nach Anzeigenamen ab
     * @param displayName Blockanzeigename
     * @return Blockkonfiguration oder null, wenn nicht gefunden
     */
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
     * Получает первую конфигурацию блока для материала
     * @param material Материал блока
     * @return Первая конфигурация блока или null, если не найдена
     *
     * Gets first block configuration for material
     * @param material Block material
     * @return First block configuration or null if not found
     *
     * Ruft die erste Blockkonfiguration für Material ab
     * @param material Blockmaterial
     * @return Erste Blockkonfiguration oder null, wenn nicht gefunden
     */
    public BlockConfig getFirstBlockConfig(Material material) {
        List<String> ids = materialToBlockIds.getOrDefault(material, Collections.emptyList());
        if (!ids.isEmpty()) {
            BlockConfig config = getBlockConfig(ids.get(0));
            
            if (config != null && config.getMaterial() == null) {
                try {
                    java.lang.reflect.Field materialField = BlockConfig.class.getDeclaredField("material");
                    materialField.setAccessible(true);
                    materialField.set(config, material);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to set material for block config " + config.getId() + ": " + e.getMessage());
                }
            }
            return config;
        }
        return null;
    }
    
    /**
     * Получает конфигурацию блока по материалу (псевдоним для getFirstBlockConfig)
     * @param material Материал для поиска
     * @return Первая конфигурация блока для этого материала или null, если не найдена
     *
     * Gets the primary block configuration for a material (alias for getFirstBlockConfig)
     * @param material The material to search for
     * @return The first BlockConfig for this material, or null if none found
     *
     * Ruft die primäre Blockkonfiguration für ein Material ab (Alias für getFirstBlockConfig)
     * @param material Das zu suchende Material
     * @return Die erste BlockConfig für dieses Material oder null, wenn keine gefunden wurde
     */
    public BlockConfig getBlockConfigByMaterial(Material material) {
        return getFirstBlockConfig(material);
    }
    
    /**
     * Проверяет, является ли блок управляющим или событийным
     * @param blockType Тип блока
     * @return true если блок управляющий или событийный, иначе false
     *
     * Checks if block is control or event block
     * @param blockType Block type
     * @return true if block is control or event, false otherwise
     *
     * Prüft, ob der Block ein Steuerungs- oder Ereignisblock ist
     * @param blockType Blocktyp
     * @return true, wenn der Block Steuerung oder Ereignis ist, sonst false
     */
    public boolean isControlOrEventBlock(String blockType) {
        if (blockType == null) return false;
        BlockConfig config = getBlockConfig(blockType);
        if (config == null) return false;
        String type = config.getType();
        return "CONTROL".equals(type) || "EVENT".equals(type);
    }
    
    /**
     * Получает секцию конфигураций действий
     * @return Секция конфигураций действий
     *
     * Gets the action configurations section
     * @return The action configurations section
     *
     * Ruft den Aktionskonfigurationsabschnitt ab
     * @return Der Aktionskonfigurationsabschnitt
     */
    public ConfigurationSection getActionConfigurations() {
        return actionConfigurations;
    }
    
    /**
     * Получает функцию разрешения слотов для определенного действия
     * @param actionName Название действия
     * @return Функция, которая сопоставляет имена слотов с индексами слотов, или null, если не найдена
     *
     * Gets a slot resolver function for a specific action
     * @param actionName The name of the action
     * @return A function that maps slot names to slot indices, or null if not found
     *
     * Ruft eine Slot-Auflösungsfunktion für eine bestimmte Aktion ab
     * @param actionName Der Name der Aktion
     * @return Eine Funktion, die Slot-Namen Slot-Indizes zuordnet, oder null, wenn nicht gefunden
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
                        
                        
                        
                    }
                }
            }
        }
        
        return slotMap::get;
    }

    /**
     * Resolves parameter name by slot index for a specific action based on configuration
     * @param actionName Action name
     * @param slotIndex Inventory slot index
     * @return Parameter name or null if not configured
     */
    public String getParameterNameForSlot(String actionName, int slotIndex) {
        if (actionConfigurations == null) return null;
        ConfigurationSection actionConfig = actionConfigurations.getConfigurationSection(actionName);
        if (actionConfig == null) return null;
        ConfigurationSection slots = actionConfig.getConfigurationSection("slots");
        if (slots == null) return null;
        ConfigurationSection slotSection = slots.getConfigurationSection(String.valueOf(slotIndex));
        if (slotSection == null) return null;
        return slotSection.getString("slot_name");
    }

    /**
     * Resolves slot index by parameter name for a specific action based on configuration
     * @param actionName Action name
     * @param paramName Parameter name
     * @return Slot index or null if not configured
     */
    public Integer findSlotForParameter(String actionName, String paramName) {
        Function<String, Integer> resolver = getSlotResolver(actionName);
        if (resolver == null || paramName == null) return null;
        return resolver.apply(paramName);
    }
    
    /**
     * Получает функцию разрешения групп слотов для определенного действия
     * @param actionName Название действия
     * @return Функция, которая сопоставляет имена групп с индексами слотов, или null, если не найдена
     *
     * Gets a group slots resolver function for a specific action
     * @param actionName The name of the action
     * @return A function that maps group names to slot indices, or null if not found
     *
     * Ruft eine Gruppenslot-Auflösungsfunktion für eine bestimmte Aktion ab
     * @param actionName Der Name der Aktion
     * @return Eine Funktion, die Gruppennamen Slot-Indizes zuordnet, oder null, wenn nicht gefunden
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
     * Внутренний класс-модель для хранения данных одного блока из конфига
     *
     * Internal model class for storing data of one block from config
     *
     * Interne Modellklasse zum Speichern von Daten eines Blocks aus der Konфигuration
     */
    public static class BlockConfig {
        private final String id;
        private Material material;
        private final String type;
        private final String displayName;
        private final String description;
        private final String category;
        private final String defaultAction;  
        
        
        private final boolean isConstructor;
        private final StructureConfig structure;
        private final Map<String, Object> parameters;
        private final List<String> actions;
        private final Map<String, ParameterConfig> actionParameters; 

        /**
         * Creates block configuration from configuration section
         * @param id Block configuration ID
         * @param section Configuration section
         * @param plugin Main plugin instance
         *
         * Создает конфигурацию блока из секции конфигурации
         * @param id ID конфигурации блока
         * @param section Секция конфигурации
         * @param plugin Экземпляр основного плагина
         *
         * Erstellt eine Blockkonfiguration aus dem Konfigurationsabschnitt
         * @param id Blockkonfigurations-ID
         * @param section Konfigurationsabschnitt
         * @param plugin Hauptplugin-Instanz
         */
        public BlockConfig(String id, ConfigurationSection section, MegaCreative plugin) {
            this.id = id;
            
            this.material = Material.matchMaterial(id);
            
            if (this.material == null) {
                String type = section.getString("type", "ACTION").toUpperCase();
                switch (type) {
                    case "EVENT":
                        this.material = Material.DIAMOND_BLOCK;
                        break;
                    case "ACTION":
                        this.material = Material.COBBLESTONE;
                        break;
                    case "CONDITION":
                        this.material = Material.OAK_PLANKS;
                        break;
                    case "CONTROL":
                        this.material = Material.PISTON;
                        break;
                    case "FUNCTION":
                        this.material = Material.LAPIS_BLOCK;
                        break;
                    case "VARIABLE":
                        this.material = Material.IRON_BLOCK;
                        break;
                    default:
                        this.material = Material.STONE; 
                        break;
                }
            }
            this.type = section.getString("type", "ACTION").toUpperCase();
            
            
            
            this.displayName = ChatColor.translateAlternateColorCodes('&', section.getString("name", id));
            this.description = section.getString("description", "No description.");
            
            
            this.category = section.getString("category", "default");
            this.defaultAction = section.getString("default_action", null);  
            
            
            this.isConstructor = section.getBoolean("is_constructor", false);
            
            
            
            
            if (isConstructor && section.contains("structure")) {
                ConfigurationSection structureSection = section.getConfigurationSection("structure");
                if (structureSection != null) {
                    this.structure = new StructureConfig(structureSection);
                } else {
                    this.structure = null;
                }
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
            
            
            this.actions = section.getStringList("actions");
            
            
            this.actionParameters = new HashMap<>();
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
        }
        
        /**
         * Loads action parameters for this block configuration
         * This should be called after the service registry is fully initialized
         */
        public void loadActionParameters(MegaCreative plugin) {
            try {
                
                if (plugin == null || plugin.getServiceRegistry() == null) {
                    
                    return;
                }
                
                
                BlockConfigService blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
                if (blockConfigService == null) {
                    return;
                }
                
                ConfigurationSection actionConfigurations = blockConfigService.getActionConfigurations();
                if (actionConfigurations != null) {
                    for (String action : this.actions) {
                        ConfigurationSection actionSection = actionConfigurations.getConfigurationSection(action);
                        if (actionSection != null) {
                            ConfigurationSection slots = actionSection.getConfigurationSection("slots");
                            if (slots != null) {
                                for (String slotKey : slots.getKeys(false)) {
                                    ConfigurationSection slotSection = slots.getConfigurationSection(slotKey);
                                    if (slotSection != null) {
                                        String slotName = slotSection.getString("slot_name");
                                        if (slotName != null) {
                                            ParameterConfig paramConfig = new ParameterConfig(slotSection);
                                            this.actionParameters.put(slotName, paramConfig);
                                        }
                                    }
                                }
                            }
                            
                            
                            ConfigurationSection itemGroups = actionSection.getConfigurationSection("item_groups");
                            if (itemGroups != null) {
                                for (String groupKey : itemGroups.getKeys(false)) {
                                    ConfigurationSection groupSection = itemGroups.getConfigurationSection(groupKey);
                                    if (groupSection != null) {
                                        
                                        ParameterConfig paramConfig = new ParameterConfig(groupSection);
                                        this.actionParameters.put(groupKey, paramConfig);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                
                java.util.logging.Logger.getLogger(BlockConfig.class.getName()).warning("Failed to load action parameters: " + e.getMessage());
                e.printStackTrace(); 
            }
        }
        
        
        public void setMaterial(Material material) {
            this.material = material;
        }
        
        
        
        
        public String getId() { return id; }
        public Material getMaterial() { return material; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getDefaultAction() { return defaultAction; }  
        
        
        public boolean isConstructor() { return isConstructor; }
        public StructureConfig getStructure() { return structure; }
        public Map<String, Object> getParameters() { return parameters; }
        public List<String> getActions() { return actions != null ? new ArrayList<>(actions) : new ArrayList<>(); }
        public Map<String, ParameterConfig> getActionParameters() { return actionParameters; } 
    }
    
    /**
     * Конфигурация структуры для блоков-конструкторов
     *
     * Structure configuration for constructor blocks
     *
     * Strukturkonfiguration für Konstruktorblöcke
     */
    public static class StructureConfig {
        private final Material brackets;
        private final boolean hasSign;
        private final int bracketDistance;
        
        /**
         * Creates structure configuration from configuration section
         * @param section Structure configuration section
         *
         * Создает конфигурацию структуры из секции конфигурации
         * @param section Секция конфигурации структуры
         *
         * Erstellt eine Strukturkonfiguration aus dem Konfigurationsabschnitt
         * @param section Strukturkonfigurationsabschnitt
         */
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
    
    /**
     * Configuration for action parameters
     */
    public static class ParameterConfig {
        private final String slotName;
        private final String name;
        private final String description;
        private final String placeholderItem;
        private final boolean required;
        private final String validation;
        private final String defaultValue;
        private final String hint;
        
        public ParameterConfig(ConfigurationSection section) {
            this.slotName = section.getString("slot_name");
            this.name = section.getString("name");
            this.description = section.getString("description");
            this.placeholderItem = section.getString("placeholder_item");
            this.required = section.getBoolean("required", false);
            this.validation = section.getString("validation");
            this.defaultValue = section.getString("default_value");
            this.hint = section.getString("hint");
        }
        
        
        public String getSlotName() { return slotName; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getPlaceholderItem() { return placeholderItem; }
        public boolean isRequired() { return required; }
        public String getValidation() { return validation; }
        public String getDefaultValue() { return defaultValue; }
        public String getHint() { return hint; }
    }
}