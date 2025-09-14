package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldFlags;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Графический интерфейс для настройки параметров мира
 * Реализует ManagedGUIInterface для интеграции с GUIManager
 *
 * GUI for configuring world settings
 * Implements ManagedGUIInterface for integration with GUIManager
 *
 * GUI zur Konfiguration von Welteinstellungen
 * Implementiert ManagedGUIInterface für die Integration mit GUIManager
 */
public class WorldSettingsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CreativeWorld world;
    private final Inventory inventory;
    
    /**
     * Инициализирует графический интерфейс настроек мира
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param world Мир, для которого настраиваются параметры
     *
     * Initializes world settings GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param world World for which settings are configured
     *
     * Initialisiert die Welteinstellungs-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param world Welt, für die Einstellungen konfiguriert werden
     */
    public WorldSettingsGUI(MegaCreative plugin, Player player, CreativeWorld world) {
        this.plugin = plugin;
        this.player = player;
        this.world = world;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lНастройки мира: " + world.getName());
        
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
        
        WorldFlags flags = world.getFlags();
        
        // Настройки флагов
        ItemStack mobSpawning = new ItemStack(flags.isMobSpawning() ? Material.ZOMBIE_HEAD : Material.BARRIER);
        ItemMeta mobMeta = mobSpawning.getItemMeta();
        mobMeta.setDisplayName("§e§lСпавн мобов");
        mobMeta.setLore(Arrays.asList(
            "§7Текущее состояние: " + (flags.isMobSpawning() ? "§aВключено" : "§cВыключено"),
            "§e▶ Нажмите для изменения"
        ));
        mobSpawning.setItemMeta(mobMeta);
        inventory.setItem(10, mobSpawning);
        
        ItemStack pvp = new ItemStack(flags.isPvp() ? Material.DIAMOND_SWORD : Material.SHIELD);
        ItemMeta pvpMeta = pvp.getItemMeta();
        pvpMeta.setDisplayName("§c§lPvP");
        pvpMeta.setLore(Arrays.asList(
            "§7Текущее состояние: " + (flags.isPvp() ? "§aВключено" : "§cВыключено"),
            "§e▶ Нажмите для изменения"
        ));
        pvp.setItemMeta(pvpMeta);
        inventory.setItem(11, pvp);
        
        ItemStack explosions = new ItemStack(flags.isExplosions() ? Material.TNT : Material.BARRIER);
        ItemMeta expMeta = explosions.getItemMeta();
        expMeta.setDisplayName("§6§lВзрывы");
        expMeta.setLore(Arrays.asList(
            "§7Текущее состояние: " + (flags.isExplosions() ? "§aВключено" : "§cВыключено"),
            "§e▶ Нажмите для изменения"
        ));
        explosions.setItemMeta(expMeta);
        inventory.setItem(12, explosions);
        
        // Кнопка удаления мира
        ItemStack deleteButton = new ItemStack(Material.RED_STAINED_GLASS);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.setDisplayName("§c§lУдалить мир");
        deleteMeta.setLore(Arrays.asList(
            "§7⚠ ВНИМАНИЕ! Это действие",
            "§7нельзя отменить!",
            "§c▶ Нажмите для удаления"
        ));
        deleteButton.setItemMeta(deleteMeta);
        inventory.setItem(16, deleteButton);
        
        // Кнопка назад
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§e§lНазад");
        backButton.setItemMeta(backMeta);
        inventory.setItem(22, backButton);
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
        return "World Settings: " + world.getName();
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
        plugin.getGuiManager().registerGUI(player, this, inventory);
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
            new MyWorldsGUI(plugin, player).open();
            return;
        }
        
        // Удаление мира
        if (displayName.contains("Удалить мир")) {
            player.closeInventory();
            // GUIManager will handle cleanup automatically
            player.sendMessage("§cДля удаления мира напишите в чат: §eУДАЛИТЬ");
            plugin.getDeleteConfirmations().put(player.getUniqueId(), world.getId());
            return;
        }
        
        // Изменение флагов
        WorldFlags flags = world.getFlags();
        
        if (displayName.contains("Спавн мобов")) {
            flags.setMobSpawning(!flags.isMobSpawning());
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        } else if (displayName.contains("PvP")) {
            flags.setPvp(!flags.isPvp());
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        } else if (displayName.contains("Взрывы")) {
            flags.setExplosions(!flags.isExplosions());
            plugin.getWorldManager().saveWorld(world);
            setupInventory();
        }
    }
}