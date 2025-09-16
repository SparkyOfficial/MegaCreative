package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.*;

/**
 * 🎆 Enhanced Condition Selection GUI for Code Blocks
 * 
 * Provides a categorized interface for selecting conditions for code blocks.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс выбора условий для блоков кода
 * 
 * Предоставляет категоризированный интерфейс для выбора условий для блоков кода.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Bedingungsauswahl-GUI für Codeblöcke
 * 
 * Bietet eine kategorisierte Schnittstelle zur Auswahl von Bedingungen für Codeblöcke.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class ConditionSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes condition selection GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public ConditionSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор условия: " + getBlockDisplayName());
        
        setupInventory();
    }
    
    /**
     * Gets display name for block
     */
    private String getBlockDisplayName() {
        // Get display name from block config service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(blockMaterial);
        return config != null ? config.getDisplayName() : blockMaterial.name();
    }
    
    /**
     * Sets up the GUI inventory
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add background glass panes
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill border slots
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add info item
        ItemStack infoItem = new ItemStack(blockMaterial);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§l" + getBlockDisplayName());
        List<String> infoLore = new ArrayList<>();
        infoLore.add("§7Выберите условие для этого блока");
        infoLore.add("");
        infoLore.add("§aКликните на условие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available conditions for this block type
        loadAvailableConditions();
    }
    
    /**
     * Loads available conditions for this block type
     */
    private void loadAvailableConditions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available conditions for this block material using BlockConfigService
        List<String> availableConditions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available conditions count: " + (availableConditions != null ? availableConditions.size() : "null"));
        
        // 🔧 FIX: Enhanced condition loading logic
        if (availableConditions == null || availableConditions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных условий для блока " + blockMaterial.name());
            
            // Try to get all block configs for debugging
            var allConfigs = blockConfigService.getAllBlockConfigs();
            player.sendMessage("§eDebug: Total block configs: " + allConfigs.size());
            
            // Check if this material is recognized as a code block
            boolean isCodeBlock = blockConfigService.isCodeBlock(blockMaterial);
            player.sendMessage("§eDebug: Is code block: " + isCodeBlock);
            
            // Try to get block config by material
            var blockConfig = blockConfigService.getBlockConfigByMaterial(blockMaterial);
            if (blockConfig != null) {
                player.sendMessage("§eDebug: Block config found: " + blockConfig.getId() + " - " + blockConfig.getDisplayName());
                player.sendMessage("§eDebug: Block type: " + blockConfig.getType());
                player.sendMessage("§eDebug: Default condition: " + blockConfig.getDefaultAction());
                
                // 🔧 FIX: Load conditions from the block configuration's actions list
                List<String> conditions = new ArrayList<>();
                
                // First, try to get actions directly from the block config
                if (blockConfig.getParameters().containsKey("actions")) {
                    // This is for backward compatibility with old config format
                    Object actionsObj = blockConfig.getParameters().get("actions");
                    if (actionsObj instanceof List) {
                        conditions.addAll((List<String>) actionsObj);
                        player.sendMessage("§aDebug: Found conditions from block config parameters: " + conditions.size());
                    }
                }
                
                // Try to get actions from the YAML configuration file
                // Conditions are stored in the same "actions" list in the config
                conditions = blockConfigService.getActionsForMaterial(blockMaterial);
                
                // Fallback to getting conditions from material mapping
                if (conditions.isEmpty()) {
                    conditions = blockConfigService.getActionsForMaterial(blockMaterial);
                }
                
                if (conditions != null && !conditions.isEmpty()) {
                    availableConditions = conditions;
                    player.sendMessage("§aDebug: Found conditions after re-check: " + conditions.size());
                }
            } else {
                player.sendMessage("§eDebug: No block config found for material");
                
                // List all available materials for debugging
                Set<Material> codeBlockMaterials = blockConfigService.getCodeBlockMaterials();
                player.sendMessage("§eDebug: Available code block materials (" + codeBlockMaterials.size() + "):");
                int count = 0;
                for (Material mat : codeBlockMaterials) {
                    player.sendMessage("§7- " + mat.name());
                    count++;
                    if (count >= 10) {
                        player.sendMessage("§7... and " + (codeBlockMaterials.size() - 10) + " more");
                        break;
                    }
                }
            }
            
            // If we still don't have conditions, use default conditions as fallback
            // 🔧 FIX: Use only appropriate default conditions for this block type
            if (availableConditions == null || availableConditions.isEmpty()) {
                // 🔧 FIX: Add default conditions based on block type
                availableConditions = new ArrayList<>();
                
                // Get the block config to determine appropriate default conditions
                var defaultBlockConfig = blockConfigService.getBlockConfigByMaterial(blockMaterial);
                if (defaultBlockConfig != null) {
                    String blockType = defaultBlockConfig.getType();
                    
                    // Add appropriate default conditions based on block type
                    switch (blockType) {
                        case "CONDITION":
                            // For variable condition blocks (OBSIDIAN), add variable-related default conditions
                            if (blockMaterial == Material.OBSIDIAN) {
                                availableConditions.add("ifVarEquals");
                                availableConditions.add("ifVarGreater");
                                availableConditions.add("ifVarLess");
                                player.sendMessage("§6Using variable condition defaults as fallback");
                            } 
                            // For other condition blocks, use general defaults
                            else {
                                availableConditions.add("hasItem");
                                availableConditions.add("isOp");
                                availableConditions.add("hasPermission");
                                player.sendMessage("§6Using general condition defaults as fallback");
                            }
                            break;
                        default:
                            availableConditions.add("hasItem");
                            availableConditions.add("isOp");
                            availableConditions.add("hasPermission");
                            player.sendMessage("§6Using general condition defaults as fallback");
                            break;
                    }
                } else {
                    // Fallback to general defaults
                    availableConditions.add("hasItem");
                    availableConditions.add("isOp");
                    availableConditions.add("hasPermission");
                    player.sendMessage("§6Using general condition defaults as fallback");
                }
            }
        }
        
        // 🎆 ENHANCED: Group conditions by category for better organization
        Map<String, List<String>> categorizedConditions = categorizeConditions(availableConditions);
        
        // Create condition items with visual categorization
        int slot = 10; // Start from first available slot
        
        for (Map.Entry<String, List<String>> category : categorizedConditions.entrySet()) {
            String categoryName = category.getKey();
            List<String> conditionsInCategory = category.getValue();
            
            // Add category separator if we have multiple categories
            if (categorizedConditions.size() > 1) {
                ItemStack categoryItem = createCategoryItem(categoryName, conditionsInCategory.size());
                if (slot < 44) {
                    inventory.setItem(slot, categoryItem);
                    slot++;
                    if (slot % 9 == 8) slot += 2; // Skip border
                }
            }
            
            // Add conditions in this category
            for (String conditionId : conditionsInCategory) {
                if (slot >= 44) break; // Don't go into border area
                
                ItemStack conditionItem = createConditionItem(conditionId, categoryName);
                inventory.setItem(slot, conditionItem);
                
                // Move to next slot, skipping border slots
                slot++;
                if (slot % 9 == 8) slot += 2; // Skip right border and left border of next row
            }
            
            // Add spacing between categories
            if (slot < 44 && categorizedConditions.size() > 1) {
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Categorizes conditions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private Map<String, List<String>> categorizeConditions(List<String> conditions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String condition : conditions) {
            String category = getConditionCategory(condition);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(condition);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for a condition
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private String getConditionCategory(String conditionId) {
        switch (conditionId.toLowerCase()) {
            case "isop":
            case "haspermission":
            case "playergamemode":
                return "👤 Игрок";
            
            case "hasitem":
            case "isplayerholding":
            case "hasarmor":
                return "🎒 Инвентарь";
            
            case "isnearblock":
            case "isblocktype":
                return "🧱 Блоки";
            
            case "mobnear":
            case "isnearentity":
            case "isriding":
                return "🧟 Существа";
            
            case "isnight":
            case "isinday":
            case "israining":
            case "worldtime":
                return "🌍 Мир и время";
            
            case "ifvarequals":
            case "ifvargreater":
            case "ifvarless":
            case "comparevariable":
                return "📊 Переменные";
            
            case "checkplayerstats":
            case "checkplayerinventory":
                return "📈 Статистика";
            
            case "checkserveronline":
            case "checkworldweather":
                return "🌐 Сервер";
            
            case "worldguardregioncheck":
                return "🛡️ Защита";
            
            default:
                return "🔧 Основные";
        }
    }
    
    /**
     * 🎆 ENHANCED: Create category header item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createCategoryItem(String categoryName, int conditionCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно условий: " + conditionCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 🎆 ENHANCED: Create condition item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createConditionItem(String conditionId, String category) {
        // Create appropriate material for condition type
        Material material = getConditionMaterial(conditionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getConditionDisplayName(conditionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getConditionDescription(conditionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + category);
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + conditionId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Gets material for condition
     */
    private Material getConditionMaterial(String conditionId) {
        // Return appropriate materials based on condition type
        switch (conditionId.toLowerCase()) {
            case "isop":
            case "haspermission":
                return Material.GOLD_INGOT;
            case "hasitem":
            case "isplayerholding":
                return Material.CHEST;
            case "isnearblock":
            case "isblocktype":
                return Material.STONE;
            case "mobnear":
            case "isnearentity":
                return Material.ZOMBIE_HEAD;
            case "playergamemode":
                return Material.GRASS_BLOCK;
            case "playerhealth":
                return Material.RED_DYE;
            case "isinday":
            case "isnight":
                return Material.CLOCK;
            case "israining":
                return Material.WATER_BUCKET;
            case "worldtime":
                return Material.SUNFLOWER;
            case "ifvarequals":
            case "ifvargreater":
            case "ifvarless":
            case "comparevariable":
                return Material.NAME_TAG;
            case "hasarmor":
                return Material.DIAMOND_CHESTPLATE;
            case "isriding":
                return Material.SADDLE;
            case "checkplayerstats":
                return Material.PAPER;
            case "checkplayerinventory":
                return Material.ENDER_CHEST;
            case "checkserveronline":
                return Material.COMMAND_BLOCK;
            case "checkworldweather":
                return Material.SNOWBALL;
            case "worldguardregioncheck":
                return Material.BARRIER;
            default:
                return Material.STONE;
        }
    }
    
    /**
     * Gets display name for condition
     */
    private String getConditionDisplayName(String conditionId) {
        // Return user-friendly names for conditions
        switch (conditionId.toLowerCase()) {
            case "isop": return "Игрок оператор";
            case "haspermission": return "Есть разрешение";
            case "isnearblock": return "Рядом с блоком";
            case "mobnear": return "Рядом с мобом";
            case "playergamemode": return "Режим игры";
            case "playerhealth": return "Здоровье игрока";
            case "hasitem": return "Есть предмет";
            case "isinday": return "Дневное время";
            case "isnight": return "Ночное время";
            case "israining": return "Идет дождь";
            case "worldtime": return "Время мира";
            case "ifvarequals": return "Переменная равна";
            case "ifvargreater": return "Переменная больше";
            case "ifvarless": return "Переменная меньше";
            case "comparevariable": return "Сравнить переменные";
            case "isblocktype": return "Тип блока";
            case "isplayerholding": return "Игрок держит";
            case "isnearentity": return "Рядом с существом";
            case "hasarmor": return "Есть броня";
            case "isriding": return "Игрок едет";
            case "checkplayerstats": return "Проверить статистику";
            case "checkplayerinventory": return "Проверить инвентарь";
            case "checkserveronline": return "Сервер онлайн";
            case "checkworldweather": return "Погода в мире";
            case "worldguardregioncheck": return "В регионе WorldGuard";
            default: return conditionId;
        }
    }
    
    /**
     * Gets description for condition
     */
    private String getConditionDescription(String conditionId) {
        // Return descriptions for conditions
        switch (conditionId.toLowerCase()) {
            case "isop": return "Проверяет, является ли игрок оператором";
            case "haspermission": return "Проверяет, есть ли у игрока разрешение";
            case "isnearblock": return "Проверяет, находится ли игрок рядом с блоком";
            case "mobnear": return "Проверяет, находится ли рядом моб";
            case "playergamemode": return "Проверяет режим игры игрока";
            case "playerhealth": return "Проверяет здоровье игрока";
            case "hasitem": return "Проверяет, есть ли у игрока предмет";
            case "isinday": return "Проверяет, является ли сейчас день";
            case "isnight": return "Проверяет, является ли сейчас ночь";
            case "israining": return "Проверяет, идет ли дождь";
            case "worldtime": return "Проверяет время в мире";
            case "ifvarequals": return "Проверяет, равна ли переменная значению";
            case "ifvargreater": return "Проверяет, больше ли переменная значения";
            case "ifvarless": return "Проверяет, меньше ли переменная значения";
            case "comparevariable": return "Сравнивает две переменные";
            case "isblocktype": return "Проверяет тип блока в определенной позиции";
            case "isplayerholding": return "Проверяет, держит ли игрок предмет";
            case "isnearentity": return "Проверяет, находится ли рядом существо";
            case "hasarmor": return "Проверяет, надета ли броня";
            case "isriding": return "Проверяет, едет ли игрок на существе";
            case "checkplayerstats": return "Проверяет статистику игрока";
            case "checkplayerinventory": return "Проверяет инвентарь игрока";
            case "checkserveronline": return "Проверяет статус сервера";
            case "checkworldweather": return "Проверяет погоду в мире";
            case "worldguardregioncheck": return "Проверяет, находится ли игрок в регионе WorldGuard";
            default: return "Условие " + conditionId;
        }
    }
    
    /**
     * Opens the GUI for the player
     * Implements reference system-style: universal blocks with GUI configuration
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Gets the GUI title
     */
    public String getGUITitle() {
        return "Condition Selection GUI for " + blockMaterial.name();
    }
    
    @Override
    /**
     * Handles inventory click events
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        ItemMeta meta = clicked.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) return;
        
        // Find condition ID in lore
        String conditionId = null;
        boolean isCategoryItem = false;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                conditionId = line.substring(5); // Remove "§8ID: " prefix
                break;
            }
            if (line.contains("Категория")) {
                isCategoryItem = true;
                break;
            }
        }
        
        if (isCategoryItem) {
            // 🎆 ENHANCED: Handle category item click with helpful message
            player.sendMessage("§eℹ Это заголовок категории. Кликните по условию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (conditionId != null) {
            selectCondition(conditionId);
        }
    }
    
    /**
     * 🎆 ENHANCED: Select condition for the block
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private void selectCondition(String conditionId) {
        // Get the code block
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null) {
            player.sendMessage("§cОшибка: Не удалось получить обработчик блоков");
            return;
        }
        
        CodeBlock codeBlock = placementHandler.getCodeBlock(blockLocation);
        if (codeBlock == null) {
            player.sendMessage("§cОшибка: Блок кода не найден");
            return;
        }
        
        // Set the condition
        codeBlock.setAction(conditionId);
        
        // Save the world
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Условие '" + getConditionDisplayName(conditionId) + "' установлено!");
        player.sendMessage("§eКликните снова по блоку для настройки параметров.");
        
        // Add visual feedback for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, 
            player.getLocation().add(0, 1, 0), 15, 0.5, 0.5, 0.5, 1);
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        
        // Close this GUI
        player.closeInventory();
    }
    
    @Override
    /**
     * Handles inventory close events
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Performs resource cleanup when interface is closed
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}