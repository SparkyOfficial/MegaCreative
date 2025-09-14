package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * 🎆 УЛУЧШЕННЫЙ ГРАФИЧЕСКИЙ ИНТЕРФЕЙС ОТОБРАЖЕНИЯ ОШИБОК
 * Предоставляет удобные сообщения об ошибках и руководство по исправлению проблем конфигурации
 * Особенности:
 * - Визуальная категоризация ошибок
 * - Полезные предложения и решения
 * - Быстрые варианты исправления, когда это возможно
 * - Индикаторы серьезности ошибок
 *
 * 🎆 ENHANCED ERROR DISPLAY GUI
 * Provides user-friendly error messages and guidance for fixing configuration issues
 * Features:
 * - Visual error categorization
 * - Helpful suggestions and solutions
 * - Quick fix options when possible
 * - Error severity indicators
 *
 * 🎆 ERWEITERTES FEHLERANZEIGE-GUI
 * Bietet benutzerfreundliche Fehlermeldungen und Anleitungen zur Behebung von Konfigurationsproblemen
 * Funktionen:
 * - Visuelle Fehlerkategorisierung
 * - Hilfreiche Vorschläge und Lösungen
 * - Schnelle Korrekturmöglichkeiten, wenn möglich
 * - Fehler-Schweregrad-Indikatoren
 */
public class ErrorDisplayGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final String errorTitle;
    private final List<ErrorInfo> errors;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    public static class ErrorInfo {
        private final String message;
        private final ErrorSeverity severity;
        private final String suggestion;
        private final Runnable quickFix;
        
        public ErrorInfo(String message, ErrorSeverity severity, String suggestion) {
            this(message, severity, suggestion, null);
        }
        
        public ErrorInfo(String message, ErrorSeverity severity, String suggestion, Runnable quickFix) {
            this.message = message;
            this.severity = severity;
            this.suggestion = suggestion;
            this.quickFix = quickFix;
        }
        
        public String getMessage() { return message; }
        public ErrorSeverity getSeverity() { return severity; }
        public String getSuggestion() { return suggestion; }
        public Runnable getQuickFix() { return quickFix; }
        public boolean hasQuickFix() { return quickFix != null; }
    }
    
    public enum ErrorSeverity {
        ERROR("§c❌", "§cКритическая ошибка"),
        WARNING("§e⚠", "§eПредупреждение"),
        INFO("§b📝", "§bИнформация");
        
        private final String icon;
        private final String displayName;
        
        ErrorSeverity(String icon, String displayName) {
            this.icon = icon;
            this.displayName = displayName;
        }
        
        public String getIcon() { return icon; }
        public String getDisplayName() { return displayName; }
    }
    
    /**
     * Инициализирует графический интерфейс отображения ошибок
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param errorTitle Заголовок ошибки
     * @param errors Список ошибок для отображения
     *
     * Initializes error display GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param errorTitle Error title
     * @param errors List of errors to display
     *
     * Initialisiert die Fehleranzeige-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param errorTitle Fehlertitel
     * @param errors Liste der anzuzeigenden Fehler
     */
    public ErrorDisplayGUI(MegaCreative plugin, Player player, String errorTitle, List<ErrorInfo> errors) {
        this.plugin = plugin;
        this.player = player;
        this.errorTitle = errorTitle;
        this.errors = errors;
        this.guiManager = plugin.getGuiManager();
        
        // Calculate inventory size based on number of errors
        int size = Math.min(54, Math.max(27, ((errors.size() + 2) / 7 + 1) * 9));
        this.inventory = Bukkit.createInventory(null, size, "§8Ошибки: " + errorTitle);
        
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
        
        // Add background glass panes
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPane.getItemMeta();
        glassMeta.setDisplayName(" ");
        glassPane.setItemMeta(glassMeta);
        
        // Fill border slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i >= inventory.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, glassPane);
            }
        }
        
        // Add title item
        ItemStack titleItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName("§c§l" + errorTitle);
        List<String> titleLore = new ArrayList<>();
        titleLore.add("§7Обнаружены проблемы в конфигурации");
        titleLore.add("");
        titleLore.add("§eПроверьте ошибки ниже для исправления");
        titleMeta.setLore(titleLore);
        titleItem.setItemMeta(titleMeta);
        inventory.setItem(4, titleItem);
        
        // Add error items
        int slot = 10;
        for (int i = 0; i < errors.size() && slot < inventory.getSize() - 9; i++) {
            ErrorInfo error = errors.get(i);
            ItemStack errorItem = createErrorItem(error, i + 1);
            
            inventory.setItem(slot, errorItem);
            
            // Move to next slot, skipping borders
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border
        }
        
        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§c§lЗакрыть");
        List<String> closeLore = new ArrayList<>();
        closeLore.add("§7Закрыть окно ошибок");
        closeMeta.setLore(closeLore);
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(inventory.getSize() - 5, closeItem);
    }
    
    /**
     * Создает элемент ошибки
     *
     * Creates error item
     *
     * Erstellt Fehlerelement
     */
    private ItemStack createErrorItem(ErrorInfo error, int number) {
        Material material = getErrorMaterial(error.getSeverity());
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        meta.setDisplayName(error.getSeverity().getIcon() + " §fОшибка #" + number);
        
        List<String> lore = new ArrayList<>();
        lore.add("§7" + error.getMessage());
        lore.add("");
        lore.add("§e💡 Рекомендация:");
        lore.add("§7" + error.getSuggestion());
        lore.add("");
        lore.add(error.getSeverity().getDisplayName());
        
        if (error.hasQuickFix()) {
            lore.add("");
            lore.add("§a⚡ Клик для быстрого исправления");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Получает материал для ошибки
     *
     * Gets material for error
     *
     * Ruft das Material für den Fehler ab
     */
    private Material getErrorMaterial(ErrorSeverity severity) {
        switch (severity) {
            case ERROR:
                return Material.RED_WOOL;
            case WARNING:
                return Material.YELLOW_WOOL;
            case INFO:
                return Material.LIGHT_BLUE_WOOL;
            default:
                return Material.WHITE_WOOL;
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
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Play error sound
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
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
        return "Error Display GUI for " + errorTitle;
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
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Handle close button
        if (displayName.contains("Закрыть")) {
            player.closeInventory();
            return;
        }
        
        // Handle error items with quick fixes
        if (displayName.contains("Ошибка #")) {
            String numberStr = displayName.replaceAll(".*#(\\d+).*", "$1");
            try {
                int errorNumber = Integer.parseInt(numberStr) - 1;
                if (errorNumber >= 0 && errorNumber < errors.size()) {
                    ErrorInfo error = errors.get(errorNumber);
                    if (error.hasQuickFix()) {
                        player.sendMessage("§a⚡ Выполняется быстрое исправление...");
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                        
                        // Execute quick fix
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                error.getQuickFix().run();
                                player.sendMessage("§a✓ Исправление применено успешно!");
                                player.closeInventory();
                            } catch (Exception e) {
                                player.sendMessage("§c❌ Ошибка при выполнении исправления: " + e.getMessage());
                            }
                        });
                    } else {
                        player.sendMessage("§e💡 " + error.getSuggestion());
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid number
            }
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