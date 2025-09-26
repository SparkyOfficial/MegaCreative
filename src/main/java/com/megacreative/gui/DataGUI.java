package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataType;
import com.megacreative.managers.GUIManager;
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
 * Управляет графическим интерфейсом для работы с типами данных
 * Предоставляет интуитивный интерфейс для получения различных типов данных
 *
 * Manages GUI for working with data types
 * Provides intuitive interface for obtaining various data types
 *
 * Verwaltet die GUI zur Arbeit mit Datentypen
 * Bietet eine intuitive Schnittstelle zum Abrufen verschiedener Datentypen
 */
public class DataGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    /**
     * Инициализирует графический интерфейс для работы с типами данных
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes data types GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die Datentypen-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public DataGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        this.inventory = Bukkit.createInventory(null, 27, "§8§lТипы данных");
        
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
        
        // Текстовые данные
        ItemStack textData = DataItemFactory.createDataItem(DataType.TEXT, "Не установлено");
        ItemMeta textMeta = textData.getItemMeta();
        textMeta.setDisplayName("§f§lТекстовые данные");
        textMeta.setLore(Arrays.asList(
            "§7Текст и сообщения",
            "§7Пример: §f'Привет мир'",
            "§e▶ Нажмите для получения"
        ));
        textData.setItemMeta(textMeta);
        inventory.setItem(10, textData);
        
        // Числовые данные
        ItemStack numberData = DataItemFactory.createDataItem(DataType.NUMBER, "0");
        ItemMeta numberMeta = numberData.getItemMeta();
        numberMeta.setDisplayName("§e§lЧисловые данные");
        numberMeta.setLore(Arrays.asList(
            "§7Целые и дробные числа",
            "§7Пример: §f42, 3.14",
            "§e▶ Нажмите для получения"
        ));
        numberData.setItemMeta(numberMeta);
        inventory.setItem(11, numberData);
        
        // Переменные
        ItemStack variableData = DataItemFactory.createDataItem(DataType.VARIABLE, "{playerName}");
        ItemMeta variableMeta = variableData.getItemMeta();
        variableMeta.setDisplayName("§b§lПеременные");
        variableMeta.setLore(Arrays.asList(
            "§7Динамические значения",
            "§7Пример: §f{playerName}",
            "§e▶ Нажмите для получения"
        ));
        variableData.setItemMeta(variableMeta);
        inventory.setItem(12, variableData);
        
        // Эффекты зелья
        ItemStack potionData = DataItemFactory.createDataItem(DataType.POTION_EFFECT, "SPEED:1");
        ItemMeta potionMeta = potionData.getItemMeta();
        potionMeta.setDisplayName("§6§lЭффекты зелья");
        potionMeta.setLore(Arrays.asList(
            "§7Эффекты зелий",
            "§7Пример: §fSPEED:1",
            "§e▶ Нажмите для получения"
        ));
        potionData.setItemMeta(potionMeta);
        inventory.setItem(13, potionData);
        
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
        return "Data Types GUI";
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
            return;
        }
        
        // Выдача данных
        if (displayName.contains("Текстовые данные")) {
            giveDataItem(DataType.TEXT);
        } else if (displayName.contains("Числовые данные")) {
            giveDataItem(DataType.NUMBER);
        } else if (displayName.contains("Переменные")) {
            giveDataItem(DataType.VARIABLE);
        } else if (displayName.contains("Эффекты зелья")) {
            giveDataItem(DataType.POTION_EFFECT);
        }
    }
    
    /**
     * Выдает предмет данных игроку
     * @param dataType Тип данных для выдачи
     *
     * Gives data item to player
     * @param dataType Data type to give
     *
     * Gibt Datengegenstand an Spieler
     * @param dataType Auszugebender Datentyp
     */
    private void giveDataItem(DataType dataType) {
        String defaultValue = switch (dataType) {
            case TEXT -> "Не установлено";
            case NUMBER -> "0";
            case VARIABLE -> "{playerName}";
            case POTION_EFFECT -> "SPEED:1";
        };
        
        ItemStack dataItem = DataItemFactory.createDataItem(dataType, defaultValue);
        player.getInventory().addItem(dataItem);
        player.sendMessage("§a✓ Вы получили " + dataType.getDisplayName());
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