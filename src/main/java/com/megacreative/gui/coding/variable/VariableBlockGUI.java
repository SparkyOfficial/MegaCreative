package com.megacreative.gui.coding.variable;

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
 * 🎆 Enhanced Variable Block GUI for Iron Blocks
 * 
 * Provides a specialized interface for selecting variable-related actions for iron blocks.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс переменных для Железных блоков
 * 
 * Предоставляет специализированный интерфейс для выбора действий, связанных с переменными, для железных блоков.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Variablenblock-GUI für Eisenblöcke
 * 
 * Bietet eine spezialisierte Schnittstelle zur Auswahl von variablenbezogenen Aktionen für Eisenblöcke.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class VariableBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes variable block GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure (should be IRON_BLOCK)
     */
    public VariableBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Переменная: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите действие для переменной");
        infoLore.add("");
        infoLore.add("§aКликните на действие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available variable actions for this block type
        loadAvailableVariableActions();
    }
    
    /**
     * Loads available variable actions for this block type
     */
    private void loadAvailableVariableActions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available variable actions for iron block material using BlockConfigService
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available variable actions count: " + (availableActions != null ? availableActions.size() : "null"));
        
        // If we don't have actions, try to get them from the block config
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных действий для блока переменной " + blockMaterial.name());
            
            // Try to get block config by material
            var blockConfig = blockConfigService.getBlockConfigByMaterial(blockMaterial);
            if (blockConfig != null) {
                player.sendMessage("§eDebug: Block config found: " + blockConfig.getId() + " - " + blockConfig.getDisplayName());
                
                // Get actions directly from the block configuration
                availableActions = blockConfig.getActions();
                player.sendMessage("§aDebug: Found actions from block config: " + (availableActions != null ? availableActions.size() : 0));
                
                // If still no actions, try to get default action
                if (availableActions == null || availableActions.isEmpty()) {
                    String defaultAction = blockConfig.getDefaultAction();
                    if (defaultAction != null && !defaultAction.isEmpty()) {
                        availableActions = new ArrayList<>();
                        availableActions.add(defaultAction);
                        player.sendMessage("§aDebug: Using default action: " + defaultAction);
                    }
                }
            } else {
                player.sendMessage("§eDebug: No block config found for material");
            }
            
            // If we still don't have actions, use appropriate default variable actions
            if (availableActions == null || availableActions.isEmpty()) {
                availableActions = new ArrayList<>();
                availableActions.add("setVar");
                availableActions.add("getVar");
                availableActions.add("addVar");
                availableActions.add("subVar");
                availableActions.add("mulVar");
                availableActions.add("divVar");
                availableActions.add("setGlobalVar");
                availableActions.add("getGlobalVar");
                availableActions.add("setServerVar");
                availableActions.add("getServerVar");
                player.sendMessage("§6Using variable default actions as fallback");
            }
        }
        
        // 🎆 ENHANCED: Group variable actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizeVariableActions(availableActions);
        
        // Create action items with visual categorization
        int slot = 10; // Start from first available slot
        
        for (Map.Entry<String, List<String>> category : categorizedActions.entrySet()) {
            String categoryName = category.getKey();
            List<String> actionsInCategory = category.getValue();
            
            // Add category separator if we have multiple categories
            if (categorizedActions.size() > 1) {
                ItemStack categoryItem = createCategoryItem(categoryName, actionsInCategory.size());
                if (slot < 44) {
                    inventory.setItem(slot, categoryItem);
                    slot++;
                    if (slot % 9 == 8) slot += 2; // Skip border
                }
            }
            
            // Add actions in this category
            for (String actionId : actionsInCategory) {
                if (slot >= 44) break; // Don't go into border area
                
                ItemStack actionItem = createActionItem(actionId, categoryName);
                inventory.setItem(slot, actionItem);
                
                // Move to next slot, skipping border slots
                slot++;
                if (slot % 9 == 8) slot += 2; // Skip right border and left border of next row
            }
            
            // Add spacing between categories
            if (slot < 44 && categorizedActions.size() > 1) {
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Categorizes variable actions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private Map<String, List<String>> categorizeVariableActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getVariableActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for a variable action
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private String getVariableActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "setvar":
            case "getvar":
                return "📝 Локальные переменные";
            
            case "setglobalvar":
            case "getglobalvar":
                return "🌍 Глобальные переменные";
            
            case "setservervar":
            case "getservervar":
                return "🌐 Серверные переменные";
            
            case "addvar":
            case "subvar":
            case "mulvar":
            case "divvar":
                return "🧮 Математические операции";
            
            default:
                return "🔧 Основные";
        }
    }
    
    /**
     * 🎆 ENHANCED: Create category header item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createCategoryItem(String categoryName, int actionCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно действий: " + actionCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 🎆 ENHANCED: Create variable action item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createActionItem(String actionId, String category) {
        // Create appropriate material for action type
        Material material = getVariableActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getVariableActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getVariableActionDescription(actionId));
        lore.add("");
        lore.add("§8⚙️ Категория: " + category);
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + actionId);
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Gets material for variable action
     */
    private Material getVariableActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "setvar":
            case "getvar":
                return Material.NAME_TAG;
            case "setglobalvar":
            case "getglobalvar":
                return Material.WRITABLE_BOOK;
            case "setservervar":
            case "getservervar":
                return Material.KNOWLEDGE_BOOK;
            case "addvar":
                return Material.GOLD_INGOT;
            case "subvar":
                return Material.IRON_INGOT;
            case "mulvar":
                return Material.DIAMOND;
            case "divvar":
                return Material.NETHER_STAR;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Gets display name for variable action
     */
    private String getVariableActionDisplayName(String actionId) {
        // Return user-friendly names for variable actions
        switch (actionId.toLowerCase()) {
            case "setvar": return "Установить переменную";
            case "getvar": return "Получить переменную";
            case "addvar": return "Добавить к переменной";
            case "subvar": return "Вычесть из переменной";
            case "mulvar": return "Умножить переменную";
            case "divvar": return "Разделить переменную";
            case "setglobalvar": return "Глобальная переменная";
            case "getglobalvar": return "Получить глобальную";
            case "setservervar": return "Серверная переменная";
            case "getservervar": return "Получить серверную";
            default: return actionId;
        }
    }

    /**
     * Gets description for variable action
     */
    private String getVariableActionDescription(String actionId) {
        // Return descriptions for variable actions
        switch (actionId.toLowerCase()) {
            case "setvar": return "Создает/изменяет локальную переменную";
            case "getvar": return "Получает значение локальной переменной";
            case "addvar": return "Добавляет значение к переменной";
            case "subvar": return "Вычитает значение из переменной";
            case "mulvar": return "Умножает переменную на значение";
            case "divvar": return "Делит переменную на значение";
            case "setglobalvar": return "Создает/изменяет глобальную переменную для всех";
            case "getglobalvar": return "Получает значение глобальной переменной";
            case "setservervar": return "Создает/изменяет серверную переменную";
            case "getservervar": return "Получает значение серверной переменной";
            default: return "Действие с переменной " + actionId;
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
        return "Variable Block GUI for " + blockMaterial.name();
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
        
        // Find action ID in lore
        String actionId = null;
        boolean isCategoryItem = false;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                actionId = line.substring(5); // Remove "§8ID: " prefix
                break;
            }
            if (line.contains("Категория")) {
                isCategoryItem = true;
                break;
            }
        }
        
        if (isCategoryItem) {
            // 🎆 ENHANCED: Handle category item click with helpful message
            player.sendMessage("§eℹ Это заголовок категории. Кликните по действию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (actionId != null) {
            selectAction(actionId);
        }
    }
    
    /**
     * 🎆 ENHANCED: Select action for the block
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private void selectAction(String actionId) {
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
        
        // Set the action
        codeBlock.setAction(actionId);
        
        // Save the world
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Действие '" + getVariableActionDisplayName(actionId) + "' установлено!");
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