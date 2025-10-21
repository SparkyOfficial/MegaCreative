package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Графический интерфейс для отображения миров игрока
 * Реализует ManagedGUIInterface для интеграции с GUIManager
 *
 * GUI for displaying player worlds
 * Implements ManagedGUIInterface for integration with GUIManager
 *
 * GUI zur Anzeige von Spielerwelten
 * Implementiert ManagedGUIInterface für die Integration mit GUIManager
 */
public class MyWorldsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    
    /**
     * Инициализирует графический интерфейс для отображения миров игрока
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, чьи миры будут отображаться
     *
     * Initializes player worlds GUI
     * @param plugin Reference to main plugin
     * @param player Player whose worlds will be displayed
     *
     * Initialisiert die Spielerwelten-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, dessen Welten angezeigt werden
     */
    public MyWorldsGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lМои миры");
        
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
        
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        // Create world button (only if player has less than 5 worlds)
        if (plugin.getServiceRegistry().getWorldManager().getPlayerWorldCount(player) < 5) {
            ItemStack createButton = new ItemStack(Material.EMERALD);
            ItemMeta createMeta = createButton.getItemMeta();
            createMeta.setDisplayName("§a§lСоздать новый мир");
            createMeta.setLore(Arrays.asList(
                "§7Создайте новый мир для строительства",
                "§7и программирования",
                "§e▶ Нажмите для создания"
            ));
            createButton.setItemMeta(createMeta);
            inventory.setItem(49, createButton);
        }
        
        // Display player worlds
        List<CreativeWorld> playerWorlds = plugin.getServiceRegistry().getWorldManager().getPlayerWorlds(player);
        int slot = 10;
        
        for (CreativeWorld world : playerWorlds) {
            if (slot > 43) break;
            
            ItemStack worldItem = new ItemStack(world.getWorldType().getIcon());
            ItemMeta worldMeta = worldItem.getItemMeta();
            worldMeta.setDisplayName("§f§l" + world.getName());
            worldMeta.setLore(Arrays.asList(
                "§7ID: §f" + world.getId(),
                "§7Тип: §f" + world.getWorldType().getDisplayName(),
                "§7Режим: §f" + world.getMode().getDisplayName(),
                "§7Онлайн: §f" + world.getOnlineCount(),
                "",
                "§a▶ ЛКМ - Войти в мир",
                "§c▶ ПКМ - Настройки мира"
            ));
            worldItem.setItemMeta(worldMeta);
            inventory.setItem(slot, worldItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
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
        // Register and open the GUI
        plugin.getServiceRegistry().getGuiManager().registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
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
    @Override
    public String getGUITitle() {
        return "My Worlds";
    }
    
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
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Handle create world button
        if (clicked.getType() == Material.EMERALD && displayName.contains("Создать")) {
            player.closeInventory();
            new WorldCreationGUI(plugin, player).open();
            return;
        }
        
        // Handle world item clicks
        List<CreativeWorld> playerWorlds = plugin.getServiceRegistry().getWorldManager().getPlayerWorlds(player);
        int slot = event.getSlot();
        int worldIndex = getWorldIndexFromSlot(slot);
        
        if (worldIndex >= 0 && worldIndex < playerWorlds.size()) {
            CreativeWorld world = playerWorlds.get(worldIndex);
            
            if (event.isLeftClick()) {
                // Join world
                player.closeInventory();
                player.performCommand("join " + world.getId());
            } else if (event.isRightClick()) {
                // Open world settings
                player.closeInventory();
                new WorldSettingsGUI(plugin, player, world).open();
            }
        }
    }
    
    /**
     * Получает индекс мира по слоту инвентаря
     * @param slot Слот инвентаря
     * @return Индекс мира или -1, если слот не содержит мира
     *
     * Gets world index by inventory slot
     * @param slot Inventory slot
     * @return World index or -1 if slot doesn't contain a world
     *
     * Ruft den Weltenindex nach Inventarslot ab
     * @param slot Inventarslot
     * @return Weltenindex oder -1, wenn der Slot keine Welt enthält
     */
    private int getWorldIndexFromSlot(int slot) {
        if (slot < 10 || slot > 43) return -1;
        if (slot % 9 == 0 || slot % 9 == 8) return -1;
        
        int row = slot / 9 - 1;
        int col = slot % 9 - 1;
        return row * 7 + col;
    }
}