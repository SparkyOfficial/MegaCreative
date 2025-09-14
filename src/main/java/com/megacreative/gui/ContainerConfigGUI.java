package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.coding.containers.BlockContainerManager;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Пользовательский графический интерфейс перетаскивания для настройки контейнеров над блоками кода
 * Предоставляет интуитивный интерфейс для игроков для настройки предметов, которые будут выданы
 * при выполнении PlayerEntryAction
 *
 * Custom drag and drop GUI for configuring containers above code blocks
 * Provides an intuitive interface for players to configure items that will be given
 * when they execute a PlayerEntryAction
 *
 * Benutzerdefinierte Drag-and-Drop-GUI zur Konfiguration von Containern über Codeblöcken
 * Bietet eine intuitive Schnittstelle für Spieler zur Konfiguration von Gegenständen, die
 * bei der Ausführung einer PlayerEntryAction ausgegeben werden
 */
public class ContainerConfigGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Location blockLocation;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final BlockContainerManager containerManager;
    
    /**
     * Инициализирует графический интерфейс настройки контейнера
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param blockLocation Расположение блока для настройки
     *
     * Initializes container configuration GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param blockLocation Location of block to configure
     *
     * Initialisiert die Container-Konfigurations-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param blockLocation Position des zu konfigurierenden Blocks
     */
    public ContainerConfigGUI(MegaCreative plugin, Player player, Location blockLocation) {
        this.plugin = plugin;
        this.player = player;
        this.blockLocation = blockLocation;
        this.guiManager = plugin.getGuiManager();
        
        // Handle null service registry in test environment
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        this.containerManager = serviceRegistry != null ? serviceRegistry.getBlockContainerManager() : null;
        
        this.inventory = Bukkit.createInventory(null, 27, "§8§lКонфигурация предметов");
        
        setupInventory();
    }
    
    /**
     * Настраивает инвентарь графического интерфейса
     *
     * Sets up the GUI inventory
     *
     * Richtet das GUI-Inventar ein
     */
    private void setupInventory() {
        inventory.clear();
        
        // Fill with glass panes for visual separation
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        // Fill border slots
        for (int i = 0; i < 27; i++) {
            if (i < 9 || i >= 18 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glass);
            }
        }
        
        // Add instruction items
        ItemStack instruction = new ItemStack(Material.PAPER);
        ItemMeta instructionMeta = instruction.getItemMeta();
        instructionMeta.setDisplayName("§e§lИнструкция");
        instructionMeta.setLore(Arrays.asList(
            "§7Перетащите предметы в",
            "§7центральные слоты для",
            "§7настройки выдачи",
            "",
            "§aЛКМ§7 - выбрать предмет",
            "§aПКМ§7 - изменить количество"
        ));
        instruction.setItemMeta(instructionMeta);
        inventory.setItem(4, instruction);
        
        // Add help item
        ItemStack help = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = help.getItemMeta();
        helpMeta.setDisplayName("§6§lПомощь");
        helpMeta.setLore(Arrays.asList(
            "§7Эти предметы будут",
            "§7выданы игроку при",
            "§7выполнении действия",
            "",
            "§eПодсказка:",
            "§7Используйте несколько",
            "§7слотов для выдачи",
            "§7разных предметов"
        ));
        help.setItemMeta(helpMeta);
        inventory.setItem(22, help);
        
        // Load existing items from container if it exists and containerManager is available
        if (containerManager != null) {
            loadContainerItems();
        }
    }
    
    /**
     * Загружает существующие предметы из контейнера
     *
     * Loads existing items from the container
     *
     * Lädt vorhandene Gegenstände aus dem Container
     */
    private void loadContainerItems() {
        try {
            // Get container location (one block above)
            Location containerLocation = blockLocation.clone().add(0, 1, 0);
            
            // Check if container exists
            if (containerLocation.getBlock().getState() instanceof org.bukkit.block.Container containerState) {
                // Load items from container inventory to GUI
                org.bukkit.inventory.Inventory containerInventory = containerState.getInventory();
                
                // Copy items to GUI (slots 9-17)
                for (int i = 0; i < 9 && i < containerInventory.getSize(); i++) {
                    ItemStack item = containerInventory.getItem(i);
                    if (item != null && item.getType() != Material.AIR) {
                        inventory.setItem(i + 9, item.clone());
                    }
                }
            }
        } catch (Exception e) {
            if (plugin != null && plugin.getLogger() != null) {
                plugin.getLogger().warning("Error loading container items: " + e.getMessage());
            }
        }
    }
    
    /**
     * Сохраняет предметы из графического интерфейса в контейнер
     *
     * Saves items from GUI to the container
     *
     * Speichert Gegenstände von der GUI im Container
     */
    private void saveContainerItems() {
        // Skip saving if containerManager is not available (test environment)
        if (containerManager == null) {
            if (player != null) {
                player.sendMessage("§cОшибка: Система контейнеров недоступна!");
            }
            return;
        }
        
        try {
            // Get container location (one block above)
            Location containerLocation = blockLocation.clone().add(0, 1, 0);
            
            // Check if container exists
            if (containerLocation.getBlock().getState() instanceof org.bukkit.block.Container containerState) {
                // Get container inventory
                org.bukkit.inventory.Inventory containerInventory = containerState.getInventory();
                
                // Clear container inventory
                containerInventory.clear();
                
                // Copy items from GUI to container (slots 9-17)
                for (int i = 0; i < 9; i++) {
                    ItemStack item = inventory.getItem(i + 9);
                    if (item != null && item.getType() != Material.AIR) {
                        containerInventory.setItem(i, item.clone());
                    }
                }
                
                if (player != null) {
                    player.sendMessage("§a✓ Предметы для выдачи сохранены!");
                }
            } else {
                if (player != null) {
                    player.sendMessage("§cОшибка: Контейнер не найден!");
                }
            }
        } catch (Exception e) {
            if (plugin != null && plugin.getLogger() != null) {
                plugin.getLogger().warning("Error saving container items: " + e.getMessage());
            }
            if (player != null) {
                player.sendMessage("§cОшибка при сохранении предметов: " + e.getMessage());
            }
        }
    }
    
    /**
     * Открывает графический интерфейс для игрока
     *
     * Opens the GUI for the player
     *
     * Öffnet die GUI für den Spieler
     */
    public void open() {
        // Register with GUIManager and open inventory
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
    @Override
    /**
     * Получает заголовок графического интерфейса
     * @return Заголовок интерфейса
     *
     * Gets the GUI title
     * @return Interface title
     *
     * Ruft den GUI-Titel ab
     * @return Schnittstellentitel
     */
    public String getGUITitle() {
        return "Container Configuration GUI for " + (blockLocation != null ? blockLocation.toString() : "unknown location");
    }
    
    @Override
    /**
     * Обрабатывает события кликов в инвентаре
     * @param event Событие клика в инвентаре
     *
     * Handles inventory click events
     * @param event Inventory click event
     *
     * Verarbeitet Inventarklick-Ereignisse
     * @param event Inventarklick-Ereignis
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (player == null || !player.equals(event.getWhoClicked())) return;
        if (inventory == null || !inventory.equals(event.getInventory())) return;
        
        int slot = event.getSlot();
        
        // Allow interaction with center slots (9-17) only
        if (slot >= 9 && slot <= 17) {
            // Allow normal interaction for item configuration
            return;
        }
        
        // Cancel interaction with all other slots
        event.setCancelled(true);
        
        // Handle clicks on special items
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Handle help item click
        if (displayName.contains("Помощь") && player != null) {
            player.sendMessage("§eПодсказка: Перетащите предметы в центральные слоты для настройки выдачи.");
        }
    }
    
    @Override
    /**
     * Обрабатывает события закрытия инвентаря
     * @param event Событие закрытия инвентаря
     *
     * Handles inventory close events
     * @param event Inventory close event
     *
     * Verarbeitet Inventarschließ-Ereignisse
     * @param event Inventarschließ-Ereignis
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Save items when GUI is closed
        saveContainerItems();
        
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Выполняет очистку ресурсов при закрытии интерфейса
     *
     * Performs resource cleanup when interface is closed
     *
     * Führt eine Ressourcenbereinigung durch, wenn die Schnittstelle geschlossen wird
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}