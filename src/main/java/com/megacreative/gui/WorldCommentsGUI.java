package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Управляет графическим интерфейсом для просмотра и добавления комментариев к миру
 * Позволяет игрокам просматривать комментарии и добавлять новые
 *
 * Manages GUI for viewing and adding comments to a world
 * Allows players to view comments and add new ones
 *
 * Verwaltet die GUI zum Anzeigen und Hinzufügen von Kommentaren zu einer Welt
 * Ermöglicht Spielern das Anzeigen von Kommentaren und das Hinzufügen neuer Kommentare
 */
public class WorldCommentsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private final int page;
    private static final int COMMENTS_PER_PAGE = 21;
    
    /**
     * Инициализирует графический интерфейс комментариев к миру
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param world Мир, к которому относятся комментарии
     * @param page Номер страницы для отображения комментариев
     *
     * Initializes world comments GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param world World to which comments belong
     * @param page Page number for displaying comments
     *
     * Initialisiert die Weltkommentar-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param world Welt, zu der die Kommentare gehören
     * @param page Seitennummer für die Anzeige von Kommentaren
     */
    public WorldCommentsGUI(MegaCreative plugin, Player player, CreativeWorld world, int page) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.page = page;
        this.guiManager = plugin.getGuiManager();
        this.inventory = Bukkit.createInventory(null, 54, "§6§lКомментарии: " + world.getName());
        
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
        
        // Заполнение стеклом
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName(" ");
        glass.setItemMeta(glassMeta);
        
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, glass);
        }
        
        List<WorldComment> comments = world.getComments();
        int startIndex = page * COMMENTS_PER_PAGE;
        int endIndex = Math.min(startIndex + COMMENTS_PER_PAGE, comments.size());
        
        // Отображение комментариев
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            WorldComment comment = comments.get(i);
            ItemStack commentItem = new ItemStack(Material.PAPER);
            ItemMeta commentMeta = commentItem.getItemMeta();
            commentMeta.setDisplayName("§f§l" + comment.getAuthorName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String date = sdf.format(new Date(comment.getTimestamp()));
            
                         commentMeta.setLore(Arrays.asList(
                 "§7" + comment.getText(),
                 "",
                 "§7Дата: §f" + date,
                 "§7Автор: §f" + comment.getAuthorName()
             ));
            commentItem.setItemMeta(commentMeta);
            inventory.setItem(slot, commentItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Кнопка добавления комментария
        ItemStack addButton = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addButton.getItemMeta();
        addMeta.setDisplayName("§a§lДобавить комментарий");
        addMeta.setLore(Arrays.asList(
            "§7Написать новый комментарий",
            "§7к этому миру",
            "§e▶ Нажмите для добавления"
        ));
        addButton.setItemMeta(addMeta);
        inventory.setItem(49, addButton);
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < comments.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(46, backButton);
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
        return "World Comments GUI for " + world.getName();
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
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Кнопка назад
        if (displayName.contains("Назад")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldActionsGUI(plugin, player, world).open();
            return;
        }
        
        // Добавление комментария
        if (displayName.contains("Добавить комментарий")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            player.sendMessage("§aНапишите ваш комментарий в чат или §eотмена§a для отмены:");
            plugin.getCommentInputs().put(player.getUniqueId(), world.getId());
            return;
        }
        
        // Навигация
        if (displayName.contains("Предыдущая страница")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldCommentsGUI(plugin, player, world, page - 1).open();
            return;
        }
        
        if (displayName.contains("Следующая страница")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            new WorldCommentsGUI(plugin, player, world, page + 1).open();
            return;
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