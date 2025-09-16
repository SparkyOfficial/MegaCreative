package com.megacreative.gui.coding.entity_event;

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
 * 🎆 Enhanced Entity Event Block GUI
 * 
 * Provides a specialized interface for selecting entity event actions.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс событий сущностей
 * 
 * Предоставляет специализированный интерфейс для выбора действий, связанных с событиями сущностей.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Entitätsereignis-Block-GUI
 * 
 * Bietet eine spezialisierte Schnittstelle zur Auswahl von Entitätsereignis-Aktionen.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class EntityEventBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes entity event block GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public EntityEventBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Событие сущности: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите событие сущности");
        infoLore.add("");
        infoLore.add("§aКликните на событие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available entity event actions for this block type
        loadAvailableEntityEventActions();
    }
    
    /**
     * Loads available entity event actions for this block type
     */
    private void loadAvailableEntityEventActions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available entity event actions for block material using BlockConfigService
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available entity event actions count: " + (availableActions != null ? availableActions.size() : "null"));
        
        // Simple fallback to default entity event actions if none found
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных событий для блока сущности " + blockMaterial.name());
            
            // Use default entity event actions as fallback
            availableActions = new ArrayList<>();
            availableActions.add("onEntitySpawn");
            availableActions.add("onEntityDeath");
            availableActions.add("onEntityDamage");
            availableActions.add("onEntityInteract");
            availableActions.add("onEntityTarget");
            availableActions.add("onEntityExplode");
            availableActions.add("onEntityPortalEnter");
            availableActions.add("onEntityPortalExit");
            player.sendMessage("§6Using entity event default actions as fallback");
        }
        
        // 🎆 ENHANCED: Group entity event actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizeEntityEventActions(availableActions);
        
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
     * 🎆 ENHANCED: Categorizes entity event actions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private Map<String, List<String>> categorizeEntityEventActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getEntityEventActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for a entity event action
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private String getEntityEventActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "onentityspawn":
            case "onentitydeath":
                return "🧬 Жизненный цикл";
            
            case "onentitydamage":
            case "onentityheal":
                return "❤️ Здоровье";
            
            case "onentityinteract":
            case "onentitytarget":
                return "🎯 Взаимодействие";
            
            case "onentityexplode":
            case "onentitycombust":
                return "💥 Разрушение";
            
            case "onentityportalenter":
            case "onentityportalexit":
                return "🌀 Порталы";
            
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
        lore.add("§7Доступно событий: " + actionCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * 🎆 ENHANCED: Create entity event action item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createActionItem(String actionId, String category) {
        // Create appropriate material for action type
        Material material = getEntityEventActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getEntityEventActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getEntityEventActionDescription(actionId));
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
     * Gets material for entity event action
     */
    private Material getEntityEventActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "onentityspawn":
                return Material.EGG;
            case "onentitydeath":
                return Material.SKELETON_SKULL;
            case "onentitydamage":
                return Material.RED_DYE;
            case "onentityinteract":
                return Material.STONE_BUTTON;
            case "onentityexplode":
                return Material.TNT;
            case "onentityportalenter":
                return Material.OBSIDIAN;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Gets display name for entity event action
     */
    private String getEntityEventActionDisplayName(String actionId) {
        // Return user-friendly names for entity event actions
        switch (actionId.toLowerCase()) {
            case "onentityspawn": return "Появление сущности";
            case "onentitydeath": return "Смерть сущности";
            case "onentitydamage": return "Урон сущности";
            case "onentityheal": return "Лечение сущности";
            case "onentityinteract": return "Взаимодействие сущности";
            case "onentitytarget": return "Цель сущности";
            case "onentityexplode": return "Взрыв сущности";
            case "onentitycombust": return "Горение сущности";
            case "onentityportalenter": return "Вход в портал";
            case "onentityportalexit": return "Выход из портала";
            default: return actionId;
        }
    }

    /**
     * Gets description for entity event action
     */
    private String getEntityEventActionDescription(String actionId) {
        // Return descriptions for entity event actions
        switch (actionId.toLowerCase()) {
            case "onentityspawn": return "Срабатывает когда сущность появляется в мире";
            case "onentitydeath": return "Срабатывает когда сущность умирает";
            case "onentitydamage": return "Срабатывает когда сущность получает урон";
            case "onentityheal": return "Срабатывает когда сущность восстанавливает здоровье";
            case "onentityinteract": return "Срабатывает когда сущность взаимодействует с блоками/игроками";
            case "onentitytarget": return "Срабатывает когда сущность выбирает цель";
            case "onentityexplode": return "Срабатывает когда сущность взрывается";
            case "onentitycombust": return "Срабатывает когда сущность начинает гореть";
            case "onentityportalenter": return "Срабатывает когда сущность входит в портал";
            case "onentityportalexit": return "Срабатывает когда сущность выходит из портала";
            default: return "Событие сущности " + actionId;
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
        return "Entity Event Block GUI for " + blockMaterial.name();
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
            player.sendMessage("§eℹ Это заголовок категории. Кликните по событию ниже.");
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
        player.sendMessage("§a✓ Событие '" + getEntityEventActionDisplayName(actionId) + "' установлено!");
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