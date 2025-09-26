package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import java.util.ArrayList;
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
 * Управляет графическим интерфейсом для управления скриптами
 * Позволяет игрокам просматривать, создавать и редактировать скрипты в своих мирах
 *
 * Manages GUI for script management
 * Allows players to view, create, and edit scripts in their worlds
 *
 * Verwaltet die GUI zur Skriptverwaltung
 * Ermöglicht Spielern das Anzeigen, Erstellen und Bearbeiten von Skripten in ihren Welten
 */
public class ScriptsGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private int page = 0;
    
    /**
     * Инициализирует графический интерфейс управления скриптами
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes script management GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die Skriptverwaltungs-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public ScriptsGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        this.inventory = Bukkit.createInventory(null, 54, "§8§lМои скрипты");
        
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
        
        // Получение скриптов текущего мира
        CreativeWorld currentWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        List<CodeScript> worldScripts = currentWorld != null ? currentWorld.getScripts() : new ArrayList<>();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, worldScripts.size());
        
        // Отображение скриптов
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            CodeScript script = worldScripts.get(i);
            ItemStack scriptItem = new ItemStack(Material.BOOK);
            ItemMeta scriptMeta = scriptItem.getItemMeta();
            scriptMeta.setDisplayName("§f§l" + script.getName());
            scriptMeta.setLore(Arrays.asList(
                "§7Статус: " + (script.isEnabled() ? "§aВключен" : "§cВыключен"),
                "§7Блоков: §f" + countBlocks(script),
                "",
                "§a▶ ЛКМ - Редактировать",
                "§e▶ ПКМ - Настройки"
            ));
            scriptItem.setItemMeta(scriptMeta);
            inventory.setItem(slot, scriptItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Кнопка создания нового скрипта
        ItemStack createButton = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.setDisplayName("§a§lСоздать новый скрипт");
        createMeta.setLore(Arrays.asList(
            "§7Создайте новый скрипт",
            "§7для автоматизации",
            "§e▶ Нажмите для создания"
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(49, createButton);
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < worldScripts.size()) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.setDisplayName("§a§lСледующая страница");
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(53, nextButton);
        }
    }
    
    /**
     * Подсчитывает количество блоков в скрипте
     * @param script Скрипт для подсчета блоков
     * @return Количество блоков в скрипте
     *
     * Counts the number of blocks in a script
     * @param script Script to count blocks
     * @return Number of blocks in the script
     *
     * Zählt die Anzahl der Blöcke in einem Skript
     * @param script Skript zum Zählen der Blöcke
     * @return Anzahl der Blöcke im Skript
     */
    private int countBlocks(CodeScript script) {
        if (script.getRootBlock() == null) return 0;
        return countBlocksRecursive(script.getRootBlock());
    }
    
    /**
     * Рекурсивно подсчитывает количество блоков
     * @param block Блок для подсчета
     * @return Количество блоков
     *
     * Recursively counts the number of blocks
     * @param block Block to count
     * @return Number of blocks
     *
     * Zählt rekursiv die Anzahl der Blöcke
     * @param block Block zum Zählen
     * @return Anzahl der Blöcke
     */
    private int countBlocksRecursive(com.megacreative.coding.CodeBlock block) {
        if (block == null) return 0;
        int count = 1;
        count += countBlocksRecursive(block.getNextBlock());
        for (com.megacreative.coding.CodeBlock child : block.getChildren()) {
            count += countBlocksRecursive(child);
        }
        return count;
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
        return "Scripts Management GUI";
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
        
        // Создание нового скрипта
        if (clicked.getType() == Material.EMERALD && displayName.contains("Создать")) {
            player.closeInventory();
            // GUIManager will handle automatic cleanup
            player.performCommand("createscript");
            return;
        }
        
        // Навигация
        if (displayName.contains("Предыдущая страница")) {
            page--;
            setupInventory();
            return;
        }
        
        if (displayName.contains("Следующая страница")) {
            page++;
            setupInventory();
            return;
        }
        
        // Клик по скрипту
        CreativeWorld currentWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        List<CodeScript> worldScripts = currentWorld != null ? currentWorld.getScripts() : new ArrayList<>();
        int slot = event.getSlot();
        int scriptIndex = getScriptIndexFromSlot(slot);
        
        if (scriptIndex >= 0 && scriptIndex < worldScripts.size()) {
            CodeScript script = worldScripts.get(scriptIndex);
            
            if (event.isLeftClick()) {
                // Редактирование скрипта
                player.closeInventory();
                // GUIManager will handle automatic cleanup
                player.performCommand("editscript " + script.getName());
            } else if (event.isRightClick()) {
                // Настройки скрипта
                player.closeInventory();
                // GUIManager will handle automatic cleanup
                player.performCommand("scriptsettings " + script.getName());
            }
        }
    }
    
    /**
     * Получает индекс скрипта по слоту инвентаря
     * @param slot Слот инвентаря
     * @return Индекс скрипта или -1, если слот не содержит скрипта
     *
     * Gets script index by inventory slot
     * @param slot Inventory slot
     * @return Script index or -1 if slot doesn't contain a script
     *
     * Ruft den Skriptindex nach Inventarslot ab
     * @param slot Inventarslot
     * @return Skriptindex oder -1, wenn der Slot kein Skript enthält
     */
    private int getScriptIndexFromSlot(int slot) {
        if (slot < 10 || slot > 43) return -1;
        
        int row = slot / 9;
        int col = slot % 9;
        
        if (col == 0 || col == 8) return -1;
        
        return (row - 1) * 7 + (col - 1) + page * 28;
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