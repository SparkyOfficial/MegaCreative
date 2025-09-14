package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.TrustedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Графический интерфейс для управления доверенными игроками
 * Позволяет добавлять, удалять и просматривать доверенных игроков
 *
 * GUI for managing trusted players
 * Allows adding, removing, and viewing trusted players
 *
 * GUI zur Verwaltung vertrauenswürdiger Spieler
 * Ermöglicht das Hinzufügen, Entfernen und Anzeigen vertrauenswürdiger Spieler
 */
public class TrustedPlayersGUI implements GUIManager.ManagedGUIInterface {

    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private final GUIManager guiManager;

    /**
     * Инициализирует графический интерфейс управления доверенными игроками
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes trusted players management GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die GUI zur Verwaltung vertrauenswürdiger Spieler
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public TrustedPlayersGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getGuiManager();
        this.inventory = Bukkit.createInventory(null, 54, "§8Управление доверенными игроками");
        setupGUI();
    }

    /**
     * Настраивает графический интерфейс
     *
     * Sets up the GUI
     *
     * Richtet die GUI ein
     */
    private void setupGUI() {
        // Заполняем фон
        ItemStack background = createItem(Material.BLACK_STAINED_GLASS_PANE, "§7", "");
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, background);
        }

        // Заголовок
        inventory.setItem(4, createItem(Material.SHIELD, "§e§lДоверенные игроки", 
            "§7Управление правами доступа"));

        // Кнопки действий
        inventory.setItem(19, createItem(Material.EMERALD, "§a§lДобавить строителя", 
            "§7Добавить доверенного строителя",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(21, createItem(Material.LAPIS_LAZULI, "§b§lДобавить программиста", 
            "§7Добавить доверенного программиста",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(23, createItem(Material.REDSTONE, "§c§lУдалить игрока", 
            "§7Удалить из доверенных",
            "§7ПКМ для выбора игрока"));
        inventory.setItem(25, createItem(Material.BOOK, "§6§lИнформация", 
            "§7Просмотр информации о правах",
            "§7ПКМ для выбора игрока"));

        // Список доверенных игроков
        displayTrustedPlayers();
    }

    /**
     * Отображает список доверенных игроков
     *
     * Displays the list of trusted players
     *
     * Zeigt die Liste der vertrauenswürdigen Spieler an
     */
    private void displayTrustedPlayers() {
        List<TrustedPlayer> allTrusted = plugin.getTrustedPlayerManager().getAllTrustedPlayers();
        
        if (allTrusted.isEmpty()) {
            inventory.setItem(31, createItem(Material.BARRIER, "§cНет доверенных игроков", 
                "§7Список пуст"));
            return;
        }

        int slot = 28;
        for (TrustedPlayer trusted : allTrusted) {
            if (slot >= 53) break; // Не выходим за границы инвентаря
            
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setDisplayName("§f" + trusted.getPlayerName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Тип: §f" + trusted.getType().getDisplayName());
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String addedDate = sdf.format(new Date(trusted.getAddedAt()));
            lore.add("§7Добавлен: §f" + addedDate);
            lore.add("§7Добавил: §f" + trusted.getAddedBy());
            lore.add("");
            lore.add("§7ПКМ для удаления");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
            
            inventory.setItem(slot, head);
            slot++;
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
        return "Trusted Players Management GUI";
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
        
        // Handle different button clicks
        if (displayName.contains("Добавить строителя")) {
            player.closeInventory();
            player.sendMessage("§aНапишите имя игрока для добавления в строители:");
            // Here you would typically store state to handle the input
        } else if (displayName.contains("Добавить программиста")) {
            player.closeInventory();
            player.sendMessage("§aНапишите имя игрока для добавления в программисты:");
        } else if (displayName.contains("Удалить игрока")) {
            player.closeInventory();
            player.sendMessage("§cНапишите имя игрока для удаления:");
        } else if (displayName.contains("Информация")) {
            // Show information about trusted players system
            player.sendMessage("§e=== Информация о доверенных игроках ===");
            player.sendMessage("§aСтроители: могут строить в ваших мирах");
            player.sendMessage("§bПрограммисты: могут редактировать скрипты");
        }
        // Handle clicks on trusted player heads for removal
        else if (clicked.getType() == Material.PLAYER_HEAD) {
            String playerName = displayName.replace("§f", "");
            player.sendMessage("§cУдалить " + playerName + " из доверенных? Напишите 'да' для подтверждения");
            player.closeInventory();
        }
    }

    /**
     * Создает предмет с указанными параметрами
     * @param material Материал предмета
     * @param name Имя предмета
     * @param lore Описание предмета
     * @return Созданный предмет
     *
     * Creates an item with specified parameters
     * @param material Item material
     * @param name Item name
     * @param lore Item description
     * @return Created item
     *
     * Erstellt einen Gegenstand mit den angegebenen Parametern
     * @param material Gegenstandsmaterial
     * @param name Gegenstandsname
     * @param lore Gegenstandsbeschreibung
     * @return Erstellter Gegenstand
     */
    public static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Получает инвентарь графического интерфейса
     * @return Инвентарь графического интерфейса
     *
     * Gets the GUI inventory
     * @return GUI inventory
     *
     * Ruft das GUI-Inventar ab
     * @return GUI-Inventar
     */
    public Inventory getInventory() {
        return inventory;
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