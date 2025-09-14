package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
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
import java.util.List;
import java.util.Map;

/**
 * Управляет графическим интерфейсом для просмотра шаблонов скриптов
 * Позволяет игрокам просматривать, импортировать и предварительно просматривать шаблоны
 *
 * Manages GUI for browsing script templates
 * Allows players to browse, import, and preview templates
 *
 * Verwaltet die GUI zum Durchsuchen von Skriptvorlagen
 * Ermöglicht Spielern das Durchsuchen, Importieren und Vorschauen von Vorlagen
 */
public class TemplateBrowserGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private final GUIManager guiManager;
    private int page = 0;
    
    /**
     * Инициализирует графический интерфейс библиотеки шаблонов
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     *
     * Initializes template browser GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     *
     * Initialisiert die Vorlagenbrowser-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     */
    public TemplateBrowserGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getGuiManager();
        this.inventory = Bukkit.createInventory(null, 54, "§8§lБиблиотека шаблонов");
        
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
        
        // Получение шаблонов
        List<CodeScript> templates = plugin.getTemplateManager().getTemplates();
        int startIndex = page * 28;
        int endIndex = Math.min(startIndex + 28, templates.size());
        
        // Отображение шаблонов
        int slot = 10;
        for (int i = startIndex; i < endIndex; i++) {
            if (slot > 43) break;
            
            CodeScript template = templates.get(i);
            
            ItemStack templateItem = new ItemStack(Material.BOOK);
            ItemMeta templateMeta = templateItem.getItemMeta();
            templateMeta.setDisplayName("§f§l" + template.getName());
            templateMeta.setLore(Arrays.asList(
                "§7Блоков: §f" + countBlocks(template.getRootBlock()),
                "§7Автор: §f" + (template.getAuthor() != null ? template.getAuthor() : "Неизвестно"),
                "",
                "§a▶ ЛКМ - Импортировать",
                "§e▶ ПКМ - Предварительный просмотр"
            ));
            templateItem.setItemMeta(templateMeta);
            inventory.setItem(slot, templateItem);
            
            slot++;
            if (slot % 9 == 8) slot += 2;
        }
        
        // Навигация
        if (page > 0) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.setDisplayName("§a§lПредыдущая страница");
            prevButton.setItemMeta(prevMeta);
            inventory.setItem(45, prevButton);
        }
        
        if (endIndex < templates.size()) {
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
     * Подсчитывает количество блоков в шаблоне
     * @param block Блок для подсчета
     * @return Количество блоков
     *
     * Counts the number of blocks in a template
     * @param block Block to count
     * @return Number of blocks
     *
     * Zählt die Anzahl der Blöcke in einer Vorlage
     * @param block Block zum Zählen
     * @return Anzahl der Blöcke
     */
    private int countBlocks(CodeBlock block) {
        if (block == null) return 0;
        int count = 1;
        count += countBlocks(block.getNextBlock());
        for (CodeBlock child : block.getChildren()) {
            count += countBlocks(child);
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
        return "Template Browser GUI";
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
            new ScriptsGUI(plugin, player).open();
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
        
        // Клик по шаблону
        List<CodeScript> templates = plugin.getTemplateManager().getTemplates();
        int slot = event.getSlot();
        int templateIndex = getTemplateIndexFromSlot(slot);
        
        if (templateIndex >= 0 && templateIndex < templates.size()) {
            CodeScript template = templates.get(templateIndex);
            
            if (event.isLeftClick()) {
                // Импорт шаблона
                player.closeInventory();
                // GUIManager will handle automatic cleanup
                player.performCommand("importtemplate " + template.getName());
            } else if (event.isRightClick()) {
                // Предварительный просмотр
                player.closeInventory();
                // GUIManager will handle automatic cleanup
                player.performCommand("previewtemplate " + template.getName());
            }
        }
    }
    
    /**
     * Получает индекс шаблона по слоту инвентаря
     * @param slot Слот инвентаря
     * @return Индекс шаблона или -1, если слот не содержит шаблона
     *
     * Gets template index by inventory slot
     * @param slot Inventory slot
     * @return Template index or -1 if slot doesn't contain a template
     *
     * Ruft den Vorlagenindex nach Inventarslot ab
     * @param slot Inventarslot
     * @return Vorlagenindex oder -1, wenn der Slot keine Vorlage enthält
     */
    private int getTemplateIndexFromSlot(int slot) {
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