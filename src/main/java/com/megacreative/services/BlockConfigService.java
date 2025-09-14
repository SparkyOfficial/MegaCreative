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
    // Ключ - это ID блока из YAML (onPlayerMove, sendMessage и т.д.)
    // Key is the block ID from YAML (onPlayerMove, sendMessage, etc.)
    // Schlüssel ist die Block-ID aus YAML (onPlayerMove, sendMessage, etc.)
    private final Map<String, BlockConfig> blockConfigs = new HashMap<>();
    private final Map<Material, List<String>> materialToBlockIds = new HashMap<>();
    // Configuration for action slots
    // Конфигурация для слотов действий
    // Konfiguration für Aktionsslots
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
        
        // Load action configurations
        // Загружаем конфигурации действий
        // Lade Aktionskonfigurationen
        actionConfigurations = config.getConfigurationSection("action_configurations");

        // ПРАВИЛЬНО: читаем ключи внутри секции blocks
        // CORRECTLY: read keys within the blocks section
        // RICHTIG: Lese Schlüssel innerhalb des blocks-Abschnitts
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
                        // Не удалось загрузить конфигурацию блока для ID:
                        // Fehler beim Laden der Blockkonfiguration für ID:
                    }
                }
            }
        }
        logger.info("Loaded " + blockConfigs.size() + " block definitions from coding_blocks.yml.");
        // Загружено определений блоков из coding_blocks.yml
        // Blockdefinitionen aus coding_blocks.yml geladen
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
            return getBlockConfig(ids.get(0));
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
                        // Ignore invalid slot indices
                        // Игнорировать недопустимые индексы слотов
                        // Ignoriere ungültige Slot-Indizes
                    }
                }
            }
        }
        
        return slotMap::get;
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
     * Interne Modellklasse zum Speichern von Daten eines Blocks aus der Konfiguration
     */
    public static class BlockConfig {
        private final String id;
        private final Material material;
        private final String type;
        private final String displayName;
        private final String description;
        private final String category;
        private final String defaultAction;  // 🔧 FIX: Add default action field
        // 🔧 ИСПРАВЛЕНИЕ: Добавить поле действия по умолчанию
        // 🔧 FIX: Füge Standardaktionsfeld hinzu
        private final boolean isConstructor;
        private final StructureConfig structure;
        private final Map<String, Object> parameters;

        /**
         * Создает конфигурацию блока из секции конфигурации
         * @param id ID блока
         * @param section Секция конфигурации блока
         *
         * Creates block configuration from configuration section
         * @param id Block ID
         * @param section Block configuration section
         *
         * Erstellt eine Blockkonfiguration aus dem Konfigurationsabschnitt
         * @param id Block-ID
         * @param section Blockkonfigurationsabschnitt
         */
        public BlockConfig(String id, ConfigurationSection section) {
            this.id = id;
            // Материал определяется по ID (ключу) блока
            // Material is determined by ID (key) of block
            // Material wird durch ID (Schlüssel) des Blocks bestimmt
            this.material = Material.matchMaterial(id);
            if (this.material == null) {
                throw new IllegalArgumentException("Invalid material specified for " + id);
                // Указан недопустимый материал для
                // Ungültiges Material angegeben für
            }
            this.type = section.getString("type", "ACTION").toUpperCase();
            // В YAML используется поле "name", не "displayName"
            // In YAML, the "name" field is used, not "displayName"
            // In YAML wird das Feld "name" verwendet, nicht "displayName"
            this.displayName = ChatColor.translateAlternateColorCodes('&', section.getString("name", id));
            this.description = section.getString("description", "No description.");
            // Нет описания
            // Keine Beschreibung
            this.category = section.getString("category", "default");
            this.defaultAction = section.getString("default_action", null);  // 🔧 FIX: Read default action from config
            // 🔧 ИСПРАВЛЕНИЕ: Чтение действия по умолчанию из конфигурации
            // 🔧 FIX: Lese Standardaktion aus Konfiguration
            this.isConstructor = section.getBoolean("is_constructor", false);
            
            // Загружаем конфигурацию структуры, если блок является конструктором
            // Load structure configuration if block is a constructor
            // Lade Strukturkonfiguration, wenn Block ein Konstruktor ist
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
        // Getters
        // Getter
        public String getId() { return id; }
        public Material getMaterial() { return material; }
        public String getType() { return type; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getDefaultAction() { return defaultAction; }  // 🔧 FIX: Add getter for default action
        // 🔧 ИСПРАВЛЕНИЕ: Добавить геттер для действия по умолчанию
        // 🔧 FIX: Füge Getter für Standardaktion hinzu
        public boolean isConstructor() { return isConstructor; }
        public StructureConfig getStructure() { return structure; }
        public Map<String, Object> getParameters() { return parameters; }
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
         * Создает конфигурацию структуры из секции конфигурации
         * @param section Секция конфигурации структуры
         *
         * Creates structure configuration from configuration section
         * @param section Structure configuration section
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
}