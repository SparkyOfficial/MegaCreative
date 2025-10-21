package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Управляет графическим интерфейсом для создания новых миров
 * Позволяет игрокам выбирать тип мира для создания
 *
 * Manages GUI for creating new worlds
 * Allows players to select world type for creation
 *
 * Verwaltet die GUI zum Erstellen neuer Welten
 * Ermöglicht Spielern die Auswahl des Weltenyps für die Erstellung
 */
public class WorldCreationGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    
    /**
     * Инициализирует графический интерфейс создания мира
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes world creation GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die Welten-Erstellungs-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public WorldCreationGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 27, "§a§lСоздание мира");
        
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
        
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }
        
        
        ItemStack normalWorld = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta normalMeta = normalWorld.getItemMeta();
        normalMeta.setDisplayName("§a§lОбычный мир");
        normalMeta.setLore(Arrays.asList(
            "§7Стандартный мир с горами,",
            "§7лесами и океанами",
            "§e▶ Нажмите для создания"
        ));
        normalWorld.setItemMeta(normalMeta);
        inventory.setItem(10, normalWorld);
        
        ItemStack flatWorld = new ItemStack(Material.STONE);
        ItemMeta flatMeta = flatWorld.getItemMeta();
        flatMeta.setDisplayName("§e§лПлоский мир");
        flatMeta.setLore(Arrays.asList(
            "§7Мир с плоской поверхностью,",
            "§7идеален для строительства",
            "§e▶ Нажмите для создания"
        ));
        flatWorld.setItemMeta(flatMeta);
        inventory.setItem(11, flatWorld);
        
        ItemStack voidWorld = new ItemStack(Material.BARRIER);
        ItemMeta voidMeta = voidWorld.getItemMeta();
        voidMeta.setDisplayName("§c§лПустой мир");
        voidMeta.setLore(Arrays.asList(
            "§7Полностью пустой мир,",
            "§7только спавн платформа",
            "§e▶ Нажмите для создания"
        ));
        voidWorld.setItemMeta(voidMeta);
        inventory.setItem(12, voidWorld);
        
        ItemStack oceanWorld = new ItemStack(Material.WATER_BUCKET);
        ItemMeta oceanMeta = oceanWorld.getItemMeta();
        oceanMeta.setDisplayName("§b§лОкеанский мир");
        oceanMeta.setLore(Arrays.asList(
            "§7Мир, покрытый океанами,",
            "§7с островами",
            "§e▶ Нажмите для создания"
        ));
        oceanWorld.setItemMeta(oceanMeta);
        inventory.setItem(13, oceanWorld);
        
        ItemStack netherWorld = new ItemStack(Material.NETHERRACK);
        ItemMeta netherMeta = netherWorld.getItemMeta();
        netherMeta.setDisplayName("§6§лАдский мир");
        netherMeta.setLore(Arrays.asList(
            "§7Мир в стиле Нижнего мира,",
            "§7с лавой и адским камнем",
            "§e▶ Нажмите для создания"
        ));
        netherWorld.setItemMeta(netherMeta);
        inventory.setItem(14, netherWorld);
        
        ItemStack endWorld = new ItemStack(Material.END_STONE);
        ItemMeta endMeta = endWorld.getItemMeta();
        endMeta.setDisplayName("§d§лКраевой мир");
        endMeta.setLore(Arrays.asList(
            "§7Мир в стиле Края,",
            "§7с краевым камнем",
            "§e▶ Нажмите для создания"
        ));
        endWorld.setItemMeta(endMeta);
        inventory.setItem(15, endWorld);
        
        
        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§лОтмена");
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(22, cancelButton);
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
        return "World Creation";
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
        
        
        if (displayName.contains("Отмена")) {
            player.closeInventory();
            return;
        }
        
        
        CreativeWorldType worldType = null;
        
        if (displayName.contains("Обычный мир")) {
            worldType = CreativeWorldType.SURVIVAL;
        } else if (displayName.contains("Плоский мир")) {
            worldType = CreativeWorldType.FLAT;
        } else if (displayName.contains("Пустой мир")) {
            worldType = CreativeWorldType.VOID;
        } else if (displayName.contains("Океанский мир")) {
            worldType = CreativeWorldType.OCEAN;
        } else if (displayName.contains("Адский мир")) {
            worldType = CreativeWorldType.NETHER;
        } else if (displayName.contains("Краевой мир")) {
            worldType = CreativeWorldType.END;
        }
        
        if (worldType != null) {
            
            player.closeInventory();
            
            
            player.sendMessage("§aВведите название для вашего нового мира в чат:");
            player.sendMessage("§7Примеры: §f" + player.getName() + "'s World, My Awesome Build, Creative Paradise");
            
            
            plugin.getServiceRegistry().getGuiManager().setPlayerMetadata(player, "pending_world_type", worldType);
            plugin.getServiceRegistry().getGuiManager().setPlayerMetadata(player, "awaiting_world_name", true);
        }
    }
    
}