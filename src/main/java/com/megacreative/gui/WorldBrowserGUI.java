package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Графический интерфейс для просмотра миров
 * Реализует ManagedGUIInterface для интеграции с GUIManager
 *
 * GUI for browsing worlds
 * Implements ManagedGUIInterface for integration with GUIManager
 *
 * GUI zum Durchsuchen von Welten
 * Implementiert ManagedGUIInterface für die Integration mit GUIManager
 */
public class WorldBrowserGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private int page = 0;
    
    /**
     * Инициализирует графический интерфейс браузера миров
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes world browser GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die Weltbrowser-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public WorldBrowserGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 54, "§8§lБраузер миров");
        
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
        
        
        List<CreativeWorld> publicWorlds = plugin.getServiceRegistry().getWorldManager().getAllPublicWorlds();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, publicWorlds.size());
        
        
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            CreativeWorld world = publicWorlds.get(i);
            ItemStack worldItem = new ItemStack(world.getWorldType().getIcon());
            ItemMeta worldMeta = worldItem.getItemMeta();
            worldMeta.setDisplayName("§f§l" + world.getName());
            
            boolean isFavorite = plugin.getServiceRegistry().getPlayerManager().isFavorite(player.getUniqueId(), world.getId());
            
            worldMeta.setLore(Arrays.asList(
                "§7ID: §f" + world.getId(),
                "§7Владелец: §f" + world.getOwnerName(),
                "§7Тип: §f" + world.getWorldType().getDisplayName(),
                "§7Онлайн: §f" + world.getOnlineCount(),
                "§7Рейтинг: " + (world.getRating() >= 0 ? "§a+" : "§c") + world.getRating(),
                "§7Избранное: " + (isFavorite ? "§a✓" : "§7✗"),
                "",
                "§a▶ ЛКМ - Войти в мир",
                "§e▶ ПКМ - Действия с миром"
            ));
            worldItem.setItemMeta(worldMeta);
            inventory.setItem(slot, worldItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§лПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < publicWorlds.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§лСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
        
        
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§e§лИнформация");
        infoMeta.setLore(Arrays.asList(
            "§7Всего миров: §f" + publicWorlds.size(),
            "§7Страница: §f" + (page + 1) + "/" + ((publicWorlds.size() - 1) / 28 + 1),
            "",
            "§7Здесь отображаются все публичные миры",
            "§7Вы можете войти в любой из них"
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(49, infoItem);
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
        return "World Browser";
    }
    
    /**
     * Открывает графический интерфейс для игрока
     *
     * Opens the GUI for the player
     *
     * Öffnet die GUI für den Spieler
     */
    public void open() {
        
        plugin.getServiceRegistry().getGuiManager().registerGUI(player, this, inventory);
        player.openInventory(inventory);
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
        
        
        if (displayName.contains("Предыдущая страница")) {
            page--;
            setupInventory();
        } else if (displayName.contains("Следующая страница")) {
            page++;
            setupInventory();
        } else if (displayName.contains("Информация")) {
            // Do nothing for info item
        } else {
            // TODO: Implement world interaction logic
            // This is a placeholder for future implementation
            // Possible implementation: Handle world item clicks and open world management GUI
            // Handle world item clicks
            // TODO: Implement world interaction logic
        }
    }
    
    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        // TODO: Implement cleanup logic for WorldBrowserGUI
        // This is a placeholder for future implementation
        // Possible implementation: Clean up resources and save any changes when GUI is closed
        // Clean up if needed
    }
}