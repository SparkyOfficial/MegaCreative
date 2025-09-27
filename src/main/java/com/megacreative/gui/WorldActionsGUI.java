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

/**
 * Графический интерфейс для выполнения действий с миром
 * Реализует ManagedGUIInterface для интеграции с GUIManager
 *
 * GUI for performing actions with a world
 * Implements ManagedGUIInterface for integration with GUIManager
 *
 * GUI zum Ausführen von Aktionen mit einer Welt
 * Implementiert ManagedGUIInterface für die Integration mit GUIManager
 */
public class WorldActionsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    
    /**
     * Инициализирует графический интерфейс действий с миром
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param world Мир, с которым будут выполняться действия
     *
     * Initializes world actions GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param world World to perform actions on
     *
     * Initialisiert die Weltaktions-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param world Welt, mit der Aktionen durchgeführt werden
     */
    public WorldActionsGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lДействия с миром: " + world.getName());
        
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
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }
        
        // Войти в мир
        ItemStack joinButton = new ItemStack(Material.EMERALD);
        ItemMeta joinMeta = joinButton.getItemMeta();
        joinMeta.setDisplayName("§a§lВойти в мир");
        joinMeta.setLore(Arrays.asList(
            "§7Перейти в мир для",
            "§7строительства и кодинга",
            "§e▶ Нажмите для входа"
        ));
        joinButton.setItemMeta(joinMeta);
        inventory.setItem(10, joinButton);
        
        // Настройки мира
        ItemStack settingsButton = new ItemStack(Material.COMPARATOR);
        ItemMeta settingsMeta = settingsButton.getItemMeta();
        settingsMeta.setDisplayName("§e§lНастройки мира");
        settingsMeta.setLore(Arrays.asList(
            "§7Изменить флаги и",
            "§7параметры мира",
            "§e▶ Нажмите для настроек"
        ));
        settingsButton.setItemMeta(settingsMeta);
        inventory.setItem(11, settingsButton);
        
        // Комментарии
        ItemStack commentsButton = new ItemStack(Material.BOOK);
        ItemMeta commentsMeta = commentsButton.getItemMeta();
        commentsMeta.setDisplayName("§b§lКомментарии");
        commentsMeta.setLore(Arrays.asList(
            "§7Просмотреть и добавить",
            "§7комментарии к миру",
            "§e▶ Нажмите для просмотра"
        ));
        commentsButton.setItemMeta(commentsMeta);
        inventory.setItem(12, commentsButton);
        
        // Скрипты
        ItemStack scriptsButton = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta scriptsMeta = scriptsButton.getItemMeta();
        scriptsMeta.setDisplayName("§6§lСкрипты");
        scriptsMeta.setLore(Arrays.asList(
            "§7Управление скриптами",
            "§7автоматизации",
            "§e▶ Нажмите для управления"
        ));
        scriptsButton.setItemMeta(scriptsMeta);
        inventory.setItem(13, scriptsButton);
        
        // Удалить мир
        ItemStack deleteButton = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.setDisplayName("§c§lУдалить мир");
        deleteMeta.setLore(Arrays.asList(
            "§7Удалить мир навсегда",
            "§c⚠ Это действие нельзя отменить!",
            "§e▶ Нажмите для удаления"
        ));
        deleteButton.setItemMeta(deleteMeta);
        inventory.setItem(14, deleteButton);
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§e§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(22, backButton);
    }
    
    /**
     * Открывает графический интерфейс для игрока
     *
     * Opens the GUI for the player
     *
     * Öffnet die GUI für den Spieler
     */
    public void open() {
        // Use the new GUIManager system
        plugin.getServiceRegistry().getGuiManager().registerGUI(player, this, inventory);
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
        return "World Actions: " + world.getName();
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
            // GUIManager will handle cleanup automatically
            new WorldBrowserGUI(plugin, player).open();
            return;
        }
        
        // Войти в мир
        if (displayName.contains("Войти в мир")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            player.performCommand("join " + world.getId());
        }
        
        // Настройки мира
        else if (displayName.contains("Настройки мира")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new WorldSettingsGUI(plugin, player, world).open();
        }
        
        // Комментарии
        else if (displayName.contains("Комментарии")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new WorldCommentsGUI(plugin, player, world, 0).open();
        }
        
        // Скрипты
        else if (displayName.contains("Скрипты")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            new ScriptsGUI(plugin, player).open();
        }
        
        // Удалить мир
        else if (displayName.contains("Удалить мир")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            // Confirm deletion with the player
            player.sendMessage("§cВы уверены, что хотите удалить мир '" + world.getName() + "'? Это действие нельзя отменить.");
            player.sendMessage("§cВведите /confirmdelete " + world.getId() + " для подтверждения удаления.");
        }
    }
}