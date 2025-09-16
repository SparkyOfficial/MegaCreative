package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.events.CustomEvent;
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
 * 🎆 Enhanced Event Selection GUI for Code Blocks
 * 
 * Provides a categorized interface for selecting events for code blocks.
 * Implements Reference System-style: universal blocks with GUI configuration.
 *
 * 🎆 Улучшенный графический интерфейс выбора событий для блоков кода
 * 
 * Предоставляет категоризированный интерфейс для выбора событий для блоков кода.
 * Реализует стиль Reference System: универсальные блоки с настройкой через GUI.
 *
 * 🎆 Erweiterte Ereignisauswahl-GUI für Codeblöcke
 * 
 * Bietet eine kategorisierte Schnittstelle zur Auswahl von Ereignissen für Codeblöcke.
 * Implementiert Reference System-Stil: universelle Blöcke mit GUI-Konfiguration.
 */
public class EventSelectionGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Material blockMaterial;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockConfigService blockConfigService;
    private final CustomEventManager eventManager;
    
    /**
     * Initializes event selection GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     * @param blockMaterial Material of block to configure
     */
    public EventSelectionGUI(MegaCreative plugin, Player player, Location blockLocation, Material blockMaterial) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.blockMaterial = blockMaterial;
        this.guiManager = plugin.getGuiManager();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        this.eventManager = plugin.getServiceRegistry().getCustomEventManager();
        
        // Create inventory with appropriate size
        this.inventory = Bukkit.createInventory(null, 54, "§8Выбор события: " + getBlockDisplayName());
        
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
        infoLore.add("§7Выберите событие для этого блока");
        infoLore.add("");
        infoLore.add("§aКликните на событие чтобы");
        infoLore.add("§аназначить его блоку");
        infoLore.add("");
        infoLore.add("§f✨ Reference system-стиль: универсальные блоки");
        infoLore.add("§fс настройкой через GUI");
        infoMeta.setLore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem);
        
        // Load available events
        loadAvailableEvents();
    }
    
    /**
     * Loads available events and sets up the GUI
     */
    private void loadAvailableEvents() {
        // Get all registered events from the event manager
        Map<String, CustomEvent> events = eventManager.getEvents();
        
        // Categorize events
        Map<String, List<String>> categorizedEvents = categorizeEvents(events);
        
        // Create event items with visual categorization
        int slot = 10; // Start from first available slot
        
        for (Map.Entry<String, List<String>> category : categorizedEvents.entrySet()) {
            String categoryName = category.getKey();
            List<String> eventsInCategory = category.getValue();
            
            // Add category separator if we have multiple categories
            if (categorizedEvents.size() > 1) {
                ItemStack categoryItem = createCategoryItem(categoryName, eventsInCategory.size());
                if (slot < 44) {
                    inventory.setItem(slot, categoryItem);
                    slot++;
                    if (slot % 9 == 8) slot += 2; // Skip border
                }
            }
            
            // Add events in this category
            for (String eventId : eventsInCategory) {
                if (slot >= 44) break; // Don't go into border area
                
                CustomEvent event = events.get(eventId);
                if (event != null) {
                    ItemStack eventItem = createEventItem(event, categoryName);
                    inventory.setItem(slot, eventItem);
                }
                
                // Move to next slot, skipping border slots
                slot++;
                if (slot % 9 == 8) slot += 2; // Skip right border and left border of next row
            }
            
            // Add spacing between categories
            if (slot < 44 && categorizedEvents.size() > 1) {
                slot++;
                if (slot % 9 == 8) slot += 2;
            }
        }
    }
    
    /**
     * Categorizes events for better organization
     */
    private Map<String, List<String>> categorizeEvents(Map<String, CustomEvent> events) {
        Map<String, List<String>> categories = new LinkedHashMap<>();
        
        for (Map.Entry<String, CustomEvent> entry : events.entrySet()) {
            String eventId = entry.getKey();
            CustomEvent event = entry.getValue();
            
            String category = getEventCategory(event);
            categories.computeIfAbsent(category, k -> new ArrayList<>()).add(eventId);
        }
        
        return categories;
    }
    
    /**
     * Gets category for an event
     */
    private String getEventCategory(CustomEvent event) {
        String category = event.getCategory();
        if (category == null || category.isEmpty()) {
            category = "🔧 Основные";
        }
        
        switch (category.toLowerCase()) {
            case "player": return "👤 Игрок";
            case "system": return "⚙️ Система";
            case "world": return "🌍 Мир";
            case "entity": return "🧟 Существа";
            case "block": return "🧱 Блоки";
            case "chat": return "💬 Чат";
            case "command": return "⌨️ Команды";
            default: return "🔧 " + category;
        }
    }
    
    /**
     * Creates category header item
     */
    private ItemStack createCategoryItem(String categoryName, int eventCount) {
        ItemStack item = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName("§e§l" + categoryName);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Доступно событий: " + eventCount);
        lore.add("§8Категория");
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Creates event item
     */
    private ItemStack createEventItem(CustomEvent event, String category) {
        // Create appropriate material for event type
        Material material = getEventMaterial(event.getName());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        meta.setDisplayName("§a§l" + getEventDisplayName(event.getName()));
        
        // Set lore with description and category
        List<String> lore = new ArrayList<>();
        lore.add("§7" + (event.getDescription() != null ? event.getDescription() : "Событие " + event.getName()));
        lore.add("");
        lore.add("§8⚙️ Категория: " + category);
        lore.add("");
        lore.add("§e⚡ Кликните чтобы выбрать");
        lore.add("§8ID: " + event.getName());
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        meta.setLore(lore);
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Gets material for event
     */
    private Material getEventMaterial(String eventId) {
        // Return appropriate materials based on event type
        switch (eventId.toLowerCase()) {
            case "playerconnect":
            case "playerdisconnect":
                return Material.PLAYER_HEAD;
            case "onjoin":
            case "onleave":
                return Material.OAK_DOOR;
            case "onchat":
                return Material.PAPER;
            case "onblockbreak":
            case "onblockplace":
                return Material.STONE;
            case "onplayermove":
                return Material.LEATHER_BOOTS;
            case "onplayerdeath":
                return Material.SKELETON_SKULL;
            case "oncommand":
                return Material.COMMAND_BLOCK;
            case "ontick":
                return Material.CLOCK;
            case "scriptcomplete":
                return Material.BOOK;
            case "usermessage":
                return Material.WRITABLE_BOOK;
            default:
                return Material.NETHER_STAR;
        }
    }
    
    /**
     * Gets display name for event
     */
    private String getEventDisplayName(String eventId) {
        // Return user-friendly names for events
        switch (eventId.toLowerCase()) {
            case "playerconnect": return "Игрок подключился";
            case "playerdisconnect": return "Игрок отключился";
            case "onjoin": return "Вход на сервер";
            case "onleave": return "Выход с сервера";
            case "onchat": return "Сообщение в чате";
            case "onblockbreak": return "Блок разрушен";
            case "onblockplace": return "Блок установлен";
            case "onplayermove": return "Игрок перемещается";
            case "onplayerdeath": return "Игрок умер";
            case "oncommand": return "Выполнение команды";
            case "ontick": return "Тик сервера";
            case "scriptcomplete": return "Скрипт завершен";
            case "usermessage": return "Пользовательское сообщение";
            default: return eventId;
        }
    }
    
    /**
     * Opens the GUI for the player
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
        return "Event Selection GUI for " + blockMaterial.name();
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
        
        // Find event ID in lore
        String eventId = null;
        boolean isCategoryItem = false;
        for (String line : lore) {
            if (line.startsWith("§8ID: ")) {
                eventId = line.substring(5); // Remove "§8ID: " prefix
                break;
            }
            if (line.contains("Категория")) {
                isCategoryItem = true;
                break;
            }
        }
        
        if (isCategoryItem) {
            // Handle category item click with helpful message
            player.sendMessage("§eℹ Это заголовок категории. Кликните по событию ниже.");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 0.8f);
            return;
        }
        
        if (eventId != null) {
            selectEvent(eventId);
        }
    }
    
    /**
     * Selects event for the block
     */
    private void selectEvent(String eventId) {
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
        
        // Set the event
        codeBlock.setAction(eventId);
        
        // Save the world
        var creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld != null) {
            plugin.getWorldManager().saveWorld(creativeWorld);
        }
        
        // Notify player
        player.sendMessage("§a✓ Событие '" + getEventDisplayName(eventId) + "' установлено!");
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