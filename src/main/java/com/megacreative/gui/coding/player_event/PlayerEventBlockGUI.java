package com.megacreative.gui.coding.player_event;

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
 * 🎆 Enhanced Player Event Block GUI
 * 
 * Provides a specialized interface for selecting player event actions.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс событий игрока
 * 
 * Предоставляет специализированный интерфейс для выбора действий, связанных с событиями игрока.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Spielerereignis-Block-GUI
 * 
 * Bietet eine spezialisierte Schnittstelle zur Auswahl von Spielerereignis-Aktionen.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class PlayerEventBlockGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    
    /**
     * Initializes player event block GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public PlayerEventBlockGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Событие игрока: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите событие игрока");
        infoLore.add("");
        infoLore.add("§aКликните на событие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available player event actions for this block type
        loadAvailablePlayerEventActions();
    }
    
    /**
     * Loads available player event actions for this block type
     */
    private void loadAvailablePlayerEventActions() {
        // Debug logging
        player.sendMessage("§eDebug: Checking material " + blockMaterial.name());
        
        // Get available player event actions for block material using BlockConfigService
        List<String> availableActions = blockConfigService.getActionsForMaterial(blockMaterial);
        
        player.sendMessage("§eDebug: Available player event actions count: " + (availableActions != null ? availableActions.size() : "null"));
        
        // Simple fallback to default player event actions if none found
        if (availableActions == null || availableActions.isEmpty()) {
            player.sendMessage("§cОшибка: Нет доступных событий для блока игрока " + blockMaterial.name());
            
            // Use default player event actions as fallback
            availableActions = new ArrayList<>();
            availableActions.add("onJoin");
            availableActions.add("onLeave");
            availableActions.add("onChat");
            availableActions.add("onMove");
            availableActions.add("onInteract");
            availableActions.add("onDamage");
            availableActions.add("onDeath");
            availableActions.add("onRespawn");
            player.sendMessage("§6Using player event default actions as fallback");
        }
        
        // 🎆 ENHANCED: Group player event actions by category for better organization
        Map<String, List<String>> categorizedActions = categorizePlayerEventActions(availableActions);
        
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
     * 🎆 ENHANCED: Categorizes player event actions for better organization
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private Map<String, List<String>> categorizePlayerEventActions(List<String> actions) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (String action : actions) {
            String category = getPlayerEventActionCategory(action);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(action);
        }
        
        return categories;
    }
    
    /**
     * 🎆 ENHANCED: Get category for a player event action
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private String getPlayerEventActionCategory(String actionId) {
        switch (actionId.toLowerCase()) {
            case "onjoin":
            case "onleave":
            case "onrespawn":
                return "🚪 Подключение/Отключение";
            
            case "onchat":
            case "oncommand":
                return "💬 Чат и команды";
            
            case "onmove":
            case "onjump":
            case "onsneak":
                return "🏃 Движение";
            
            case "oninteract":
            case "onbreakblock":
            case "onplaceblock":
                return "⛏️ Взаимодействие";
            
            case "ondamage":
            case "ondeath":
            case "onheal":
                return "❤️ Здоровье";
            
            case "onitempickup":
            case "onitemdrop":
            case "oninventoryclick":
                return "🎒 Инвентарь";
            
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
     * 🎆 ENHANCED: Create player event action item
     * Implements reference system-style: universal blocks with GUI configuration
     */
    private ItemStack createActionItem(String actionId, String category) {
        // Create appropriate material for action type
        Material material = getPlayerEventActionMaterial(actionId);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getPlayerEventActionDisplayName(actionId));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + getPlayerEventActionDescription(actionId));
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
     * Gets material for player event action
     */
    private Material getPlayerEventActionMaterial(String actionId) {
        // Return appropriate materials based on action type
        switch (actionId.toLowerCase()) {
            case "onjoin":
            case "onleave":
                return Material.OAK_DOOR;
            case "onchat":
                return Material.WRITABLE_BOOK;
            case "onmove":
                return Material.LEATHER_BOOTS;
            case "oninteract":
                return Material.STONE_BUTTON;
            case "ondamage":
                return Material.RED_DYE;
            case "ondeath":
                return Material.SKELETON_SKULL;
            case "onitempickup":
                return Material.CHEST;
            default:
                return Material.PAPER;
        }
    }
    
    /**
     * Gets display name for player event action
     */
    private String getPlayerEventActionDisplayName(String actionId) {
        // Return user-friendly names for player event actions
        switch (actionId.toLowerCase()) {
            case "onjoin": return "Игрок зашел";
            case "onleave": return "Игрок вышел";
            case "onchat": return "Чат игрока";
            case "onmove": return "Движение игрока";
            case "oninteract": return "Взаимодействие";
            case "ondamage": return "Получение урона";
            case "ondeath": return "Смерть игрока";
            case "onrespawn": return "Возрождение";
            case "onjump": return "Прыжок игрока";
            case "onsneak": return "Приседание";
            case "oncommand": return "Команда игрока";
            case "onbreakblock": return "Ломание блока";
            case "onplaceblock": return "Установка блока";
            case "onheal": return "Лечение";
            case "onitempickup": return "Подбор предмета";
            case "onitemdrop": return "Выброс предмета";
            case "oninventoryclick": return "Клик по инвентарю";
            default: return actionId;
        }
    }

    /**
     * Gets description for player event action
     */
    private String getPlayerEventActionDescription(String actionId) {
        // Return descriptions for player event actions
        switch (actionId.toLowerCase()) {
            case "onjoin": return "Срабатывает когда игрок заходит на сервер";
            case "onleave": return "Срабатывает когда игрок выходит с сервера";
            case "onchat": return "Срабатывает когда игрок пишет в чат";
            case "onmove": return "Срабатывает когда игрок двигается";
            case "oninteract": return "Срабатывает при взаимодействии игрока с блоками/сущностями";
            case "ondamage": return "Срабатывает когда игрок получает урон";
            case "ondeath": return "Срабатывает когда игрок умирает";
            case "onrespawn": return "Срабатывает когда игрок возрождается";
            case "onjump": return "Срабатывает когда игрок прыгает";
            case "onsneak": return "Срабатывает когда игрок приседает";
            case "oncommand": return "Срабатывает когда игрок использует команду";
            case "onbreakblock": return "Срабатывает когда игрок ломает блок";
            case "onplaceblock": return "Срабатывает когда игрок ставит блок";
            case "onheal": return "Срабатывает когда игрок восстанавливает здоровье";
            case "onitempickup": return "Срабатывает когда игрок подбирает предмет";
            case "onitemdrop": return "Срабатывает когда игрок выбрасывает предмет";
            case "oninventoryclick": return "Срабатывает когда игрок кликает по инвентарю";
            default: return "Событие игрока " + actionId;
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
        return "Player Event Block GUI for " + blockMaterial.name();
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
        player.sendMessage("§a✓ Событие '" + getPlayerEventActionDisplayName(actionId) + "' установлено!");
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