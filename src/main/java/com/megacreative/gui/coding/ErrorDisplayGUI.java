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
 * 🎆 Enhanced Error Display GUI
 * 
 * Implements Reference System-style: universal blocks with GUI configuration
 * with categories, beautiful selection, and smart signs on blocks with information.
 *
 * 🎆 Улучшенный графический интерфейс отображения ошибок
 * 
 * Реализует стиль reference system: универсальные блоки с настройкой через GUI
 * с категориями, красивым выбором и умными табличками на блоках с информацией.
 *
 * 🎆 Erweitertes Fehleranzeige-GUI
 * 
 * Implementiert Reference-System-Stil: universelle Blöcke mit GUI-Konfiguration
 * mit Kategorien, schöner Auswahl und intelligenten Schildern an Blöcken mit Informationen.
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
     * Initializes error display GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param errorTitle Error title
     * @param errors List of errors to display
     */
    public ErrorDisplayGUI(MegaCreative plugin, Player player, String errorTitle, List<ErrorInfo> errors) {
        this.plugin = plugin;
        this.player = player;
        this.errorTitle = errorTitle;
        this.errors = errors;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        // Calculate inventory size based on number of errors (54 slots max for double chest)
        int size = Math.min(54, Math.max(27, ((errors.size() + 2) / 7 + 1) * 9));
        this.inventory = Bukkit.createInventory(null, size, "§8Ошибки: " + errorTitle);
        
        setupInventory();
    }
    
    /**
     * Sets up the GUI inventory with enhanced design
     */
    private void setupInventory() {
        inventory.clear();
        
        // Add decorative border with category-specific materials
        ItemStack borderItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        borderMeta.setDisplayName(" ");
        borderItem.setItemMeta(borderMeta);
        
        // Fill border slots
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i >= inventory.getSize() - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, borderItem);
            }
        }
        
        // Add title item with enhanced visual design
        ItemStack titleItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta titleMeta = titleItem.getItemMeta();
        titleMeta.setDisplayName("§c§l" + errorTitle);
        List<String> titleLore = new ArrayList<>();
        titleLore.add("§7Обнаружены проблемы в конфигурации");
        titleLore.add("");
        titleLore.add("§eПроверьте ошибки ниже для исправления");
        titleLore.add("");
        titleLore.add("§f✨ Reference system-стиль: универсальные блоки");
        titleLore.add("§fс настройкой через GUI");
        titleMeta.setLore(titleLore);
        titleItem.setItemMeta(titleMeta);
        inventory.setItem(4, titleItem);
        
        // Add error items with enhanced design
        int slot = 10;
        for (int i = 0; i < errors.size() && slot < inventory.getSize() - 9; i++) {
            ErrorInfo error = errors.get(i);
            ItemStack errorItem = createErrorItem(error, i + 1);
            
            inventory.setItem(slot, errorItem);
            
            // Move to next slot, skipping borders
            slot++;
            if (slot % 9 == 8) slot += 2; // Skip border
        }
        
        // Add control buttons with enhanced design
        addControlButtons();
    }
    
    /**
     * Adds control buttons with enhanced design
     */
    private void addControlButtons() {
        // Close button with enhanced visual design
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.setDisplayName("§c§lЗакрыть");
        List<String> closeLore = new ArrayList<>();
        closeLore.add("§7Закрыть окно ошибок");
        closeLore.add("");
        closeLore.add("§f✨ Reference system-стиль: универсальные блоки");
        closeLore.add("§fс настройкой через GUI");
        closeMeta.setLore(closeLore);
        closeItem.setItemMeta(closeMeta);
        inventory.setItem(inventory.getSize() - 5, closeItem);
        
        // Add back button
        ItemStack backButton = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§c⬅ Назад");
        List<String> backLore = new ArrayList<>();
        backLore.add("§7Вернуться к предыдущему меню");
        backLore.add("");
        backLore.add("§f✨ Reference system-стиль: универсальные блоки");
        backLore.add("§fс настройкой через GUI");
        backMeta.setLore(backLore);
        backButton.setItemMeta(backMeta);
        inventory.setItem(inventory.getSize() - 6, backButton);
    }
    
    /**
     * Creates error item with enhanced design
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
        lore.add("");
        lore.add("§f✨ Reference system-стиль: универсальные блоки");
        lore.add("§fс настройкой через GUI");
        
        if (error.hasQuickFix()) {
            lore.add("");
            lore.add("§a⚡ Клик для быстрого исправления");
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Gets material for error
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
     * Opens the GUI for the player
     */
    public void open() {
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
        
        // Audio feedback when opening GUI
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.5f);
        
        // Add visual effects for reference system-style magic
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
            player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 1);
    }
    
    @Override
    /**
     * Gets the GUI title
     * @return Interface title
     */
    public String getGUITitle() {
        return "Error Display GUI for " + errorTitle;
    }
    
    @Override
    /**
     * Handles inventory click events
     * @param event Inventory click event
     */
    public void onInventoryClick(InventoryClickEvent event) {
        if (!player.equals(event.getWhoClicked())) return;
        if (!inventory.equals(event.getInventory())) return;
        
        event.setCancelled(true); // Cancel all clicks by default
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        int slot = event.getSlot();
        
        // Handle back button
        if (slot == inventory.getSize() - 6) {
            player.closeInventory();
            return;
        }
        
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
                        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.2f);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore invalid number
            }
        }
    }
    
    @Override
    /**
     * Handles inventory close events
     * @param event Inventory close event
     */
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    /**
     * Performs resource cleanup when interface is closed
     */
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
}