package com.megacreative.managers;

import com.megacreative.MegaCreative;
import java.util.ArrayList;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.IPlayerManager;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced GUI Manager with memory leak prevention and proper event handling
 *
 * Усовершенствованный менеджер GUI с предотвращением утечек памяти и правильной обработкой событий
 *
 * Verbesserter GUI-Manager mit Speicherleck-Prävention und ordnungsgemäßer Ereignisbehandlung
 */
public class GUIManager implements Listener {
    private static final Logger log = Logger.getLogger(GUIManager.class.getName());
    
    private final MegaCreative plugin;
    private final IPlayerManager playerManager;
    private final VariableManager variableManager;
    
    // Thread-safe maps to prevent memory leaks
    // Потокобезопасные карты для предотвращения утечек памяти
    // Thread-sichere Karten zur Verhinderung von Speicherlecks
    private final Map<UUID, ManagedGUI> activeGUIs = new ConcurrentHashMap<>();
    private final Map<Inventory, ManagedGUI> inventoryToGUI = new ConcurrentHashMap<>();
    
    // Player metadata storage
    // Хранилище метаданных игроков
    // Spieler-Metadaten-Speicher
    private final Map<UUID, Map<String, Object>> playerMetadata = new ConcurrentHashMap<>();
    
    /**
     * Constructor with required dependencies
     * @param playerManager The player manager instance
     * @param variableManager The variable manager instance
     *
     * Конструктор с необходимыми зависимостями
     * @param playerManager экземпляр менеджера игроков
     * @param variableManager экземпляр менеджера переменных
     *
     * Konstruktor mit erforderlichen Abhängigkeiten
     * @param playerManager Die Spieler-Manager-Instanz
     * @param variableManager Die Variablen-Manager-Instanz
     */
    public GUIManager(IPlayerManager playerManager, VariableManager variableManager) {
        this.plugin = MegaCreative.getInstance();
        this.playerManager = playerManager;
        this.variableManager = variableManager;
    }
    
    /**
     * Shuts down the GUIManager and cleans up resources
     *
     * Завершает работу GUIManager и очищает ресурсы
     *
     * Schaltet den GUIManager herunter und bereinigt Ressourcen
     */
    public void shutdown() {
        // Close all open GUIs
        for (UUID playerId : new ArrayList<>(activeGUIs.keySet())) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.closeInventory();
            }
        }
        
        // Clear all references
        activeGUIs.clear();
        inventoryToGUI.clear();
        playerMetadata.clear();
    }
    
    /**
     * Interface for manageable GUIs
     *
     * Интерфейс для управляемых GUI
     *
     * Schnittstelle für verwaltbare GUIs
     */
    public interface ManagedGUIInterface {
        /**
         * Handles inventory click events
         *
         * Обрабатывает события кликов в инвентаре
         *
         * Verarbeitet Inventar-Klick-Ereignisse
         */
        void onInventoryClick(InventoryClickEvent event);
        
        /**
         * Handles inventory close events
         *
         * Обрабатывает события закрытия инвентаря
         *
         * Verarbeitet Inventar-Schließen-Ereignisse
         */
        default void onInventoryClose(InventoryCloseEvent event) {
            // Default implementation - override if needed
            // Стандартная реализация - переопределите при необходимости
            // Standardimplementierung - überschreiben falls nötig
        }
        
        /**
         * Called when GUI is being cleaned up
         *
         * Вызывается при очистке GUI
         *
         * Wird aufgerufen, wenn die GUI bereinigt wird
         */
        default void onCleanup() {
            // Default implementation - override if needed
            // Стандартная реализация - переопределите при необходимости
            // Standardimplementierung - überschreiben falls nötig
        }
        
        /**
         * Gets the GUI title for debugging
         *
         * Получает заголовок GUI для отладки
         *
         * Gibt den GUI-Titel zur Fehlerbehebung zurück
         */
        default String getGUITitle() {
            return "Unknown GUI";
        }
    }
    
    /**
     * Wrapper for GUI objects with metadata
     *
     * Обертка для GUI объектов с метаданными
     *
     * Wrapper für GUI-Objekte mit Metadaten
     */
    private static class ManagedGUI {
        private final ManagedGUIInterface gui;
        private final long createdTime;
        private long lastAccessTime;
        private final String title;
        
        /**
         * Constructor for ManagedGUI
         * @param gui the GUI interface
         *
         * Конструктор для ManagedGUI
         * @param gui интерфейс GUI
         *
         * Konstruktor für ManagedGUI
         * @param gui die GUI-Schnittstelle
         */
        public ManagedGUI(ManagedGUIInterface gui) {
            this.gui = gui;
            this.createdTime = System.currentTimeMillis();
            this.lastAccessTime = createdTime;
            this.title = gui.getGUITitle();
        }
        
        /**
         * Gets the GUI interface
         * @return the GUI interface
         *
         * Получает интерфейс GUI
         * @return интерфейс GUI
         *
         * Gibt die GUI-Schnittstelle zurück
         * @return die GUI-Schnittstelle
         */
        public ManagedGUIInterface getGUI() {
            updateLastAccess();
            return gui;
        }
        
        /**
         * Updates last access time
         *
         * Обновляет время последнего доступа
         *
         * Aktualisiert die letzte Zugriffszeit
         */
        public void updateLastAccess() {
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        /**
         * Cleans up the GUI
         *
         * Очищает GUI
         *
         * Bereinigt die GUI
         */
        public void cleanup() {
            gui.onCleanup();
        }
        
        // Getters
        public long getCreatedTime() { return createdTime; }
        public long getLastAccessTime() { return lastAccessTime; }
        public String getTitle() { return title; }
    }
    
    /**
     * Registers a GUI for a player with proper memory management
     *
     * Регистрирует GUI для игрока с правильным управлением памятью
     *
     * Registriert eine GUI für einen Spieler mit ordnungsgemäßer Speicherverwaltung
     */
    public void registerGUI(Player player, ManagedGUIInterface gui, Inventory inventory) {
        UUID playerId = player.getUniqueId();
        
        // Clean up any existing GUI for this player
        unregisterGUI(player);
        
        ManagedGUI managedGUI = new ManagedGUI(gui);
        activeGUIs.put(playerId, managedGUI);
        inventoryToGUI.put(inventory, managedGUI);
        
        log.fine("Registered GUI: " + gui.getGUITitle() + " for player: " + player.getName());
    }
    
    /**
     * Unregisters a GUI for a player with proper cleanup
     *
     * Отменяет регистрацию GUI для игрока с правильной очисткой
     *
     * Hebt die Registrierung einer GUI für einen Spieler mit ordnungsgemäßer Bereinigung auf
     */
    public void unregisterGUI(Player player) {
        UUID playerId = player.getUniqueId();
        ManagedGUI managedGUI = activeGUIs.remove(playerId);
        
        if (managedGUI != null) {
            // Remove from inventory mapping
            inventoryToGUI.entrySet().removeIf(entry -> entry.getValue().equals(managedGUI));
            
            // Call cleanup
            managedGUI.cleanup();
            
            log.fine("Unregistered GUI: " + managedGUI.getTitle() + " for player: " + player.getName());
        }
    }
    
    /**
     * Gets the active GUI for a player
     *
     * Получает активный GUI для игрока
     *
     * Gibt die aktive GUI für einen Spieler zurück
     */
    public ManagedGUIInterface getActiveGUI(Player player) {
        ManagedGUI managedGUI = activeGUIs.get(player.getUniqueId());
        return managedGUI != null ? managedGUI.getGUI() : null;
    }
    
    /**
     * Checks if a player has an active GUI
     *
     * Проверяет, есть ли у игрока активный GUI
     *
     * Prüft, ob ein Spieler eine aktive GUI hat
     */
    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * Sets metadata for a player
     *
     * Устанавливает метаданные для игрока
     *
     * Setzt Metadaten für einen Spieler
     */
    public <T> void setPlayerMetadata(Player player, String key, T value) {
        UUID playerId = player.getUniqueId();
        playerMetadata.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>()).put(key, value);
    }
    
    /**
     * Gets metadata for a player
     *
     * Получает метаданные для игрока
     *
     * Gibt Metadaten für einen Spieler zurück
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlayerMetadata(Player player, String key, Class<T> type) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> metadata = playerMetadata.get(playerId);
        if (metadata == null) {
            return null;
        }
        
        Object value = metadata.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        return null;
    }
    
    /**
     * Removes metadata for a player
     *
     * Удаляет метаданные для игрока
     *
     * Entfernt Metadaten für einen Spieler
     */
    public void removePlayerMetadata(Player player, String key) {
        UUID playerId = player.getUniqueId();
        Map<String, Object> metadata = playerMetadata.get(playerId);
        if (metadata != null) {
            metadata.remove(key);
        }
    }
    
    /**
     * Clears all metadata for a player
     *
     * Очищает все метаданные для игрока
     *
     * Löscht alle Metadaten für einen Spieler
     */
    public void clearPlayerMetadata(Player player) {
        UUID playerId = player.getUniqueId();
        playerMetadata.remove(playerId);
    }
    
    // === Методы для работы с подтверждением удаления мира ===
    // === Methods for working with world deletion confirmation ===
    // === Methoden für die Arbeit mit der Weltlöschbestätigung ===
    
    /**
     * Устанавливает флаг ожидания подтверждения удаления мира
     *
     * Sets the flag for waiting for world deletion confirmation
     *
     * Setzt das Flag für das Warten auf die Weltlöschbestätigung
     */
    public void setAwaitingDeleteConfirmation(Player player, String worldId) {
        setPlayerMetadata(player, "delete_confirmation_world_id", worldId);
        setPlayerMetadata(player, "awaiting_delete_confirmation", true);
    }
    
    /**
     * Получает ID мира для подтверждения удаления
     *
     * Gets the world ID for deletion confirmation
     *
     * Gibt die Welt-ID für die Löschbestätigung zurück
     */
    public String getDeleteConfirmationWorldId(Player player) {
        return getPlayerMetadata(player, "delete_confirmation_world_id", String.class);
    }
    
    /**
     * Проверяет, ожидает ли игрок подтверждения удаления мира
     *
     * Checks if the player is waiting for world deletion confirmation
     *
     * Prüft, ob der Spieler auf die Weltlöschbestätigung wartet
     */
    public boolean isAwaitingDeleteConfirmation(Player player) {
        return Boolean.TRUE.equals(getPlayerMetadata(player, "awaiting_delete_confirmation", Boolean.class));
    }
    
    /**
     * Очищает данные подтверждения удаления мира
     *
     * Clears world deletion confirmation data
     *
     * Löscht die Daten zur Weltlöschbestätigung
     */
    public void clearDeleteConfirmation(Player player) {
        removePlayerMetadata(player, "delete_confirmation_world_id");
        removePlayerMetadata(player, "awaiting_delete_confirmation");
    }
    
    // === Методы для работы с вводом комментариев ===
    // === Methods for working with comment input ===
    // === Methoden für die Arbeit mit Kommentareingaben ===
    
    /**
     * Устанавливает флаг ожидания ввода комментария
     *
     * Sets the flag for waiting for comment input
     *
     * Setzt das Flag für das Warten auf Kommentareingabe
     */
    public void setAwaitingCommentInput(Player player, String worldId) {
        setPlayerMetadata(player, "comment_input_world_id", worldId);
        setPlayerMetadata(player, "awaiting_comment_input", true);
    }
    
    /**
     * Получает ID мира для ввода комментария
     *
     * Gets the world ID for comment input
     *
     * Gibt die Welt-ID für die Kommentareingabe zurück
     */
    public String getCommentInputWorldId(Player player) {
        return getPlayerMetadata(player, "comment_input_world_id", String.class);
    }
    
    /**
     * Проверяет, ожидает ли игрок ввода комментария
     *
     * Checks if the player is waiting for comment input
     *
     * Prüft, ob der Spieler auf Kommentareingabe wartet
     */
    public boolean isAwaitingCommentInput(Player player) {
        return Boolean.TRUE.equals(getPlayerMetadata(player, "awaiting_comment_input", Boolean.class));
    }
    
    /**
     * Очищает данные ввода комментария
     *
     * Clears comment input data
     *
     * Löscht die Kommentareingabedaten
     */
    public void clearCommentInput(Player player) {
        removePlayerMetadata(player, "comment_input_world_id");
        removePlayerMetadata(player, "awaiting_comment_input");
    }
    
    /**
     * Handles inventory click events
     *
     * Обрабатывает события кликов в инвентаре
     *
     * Verarbeitet Inventar-Klick-Ereignisse
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ManagedGUI managedGUI = inventoryToGUI.get(event.getInventory());
        
        if (managedGUI != null) {
            managedGUI.getGUI().onInventoryClick(event);
        }
    }
    
    /**
     * Handles inventory close events
     *
     * Обрабатывает события закрытия инвентаря
     *
     * Verarbeitet Inventar-Schließen-Ereignisse
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        ManagedGUI managedGUI = inventoryToGUI.get(event.getInventory());
        
        if (managedGUI != null) {
            managedGUI.getGUI().onInventoryClose(event);
        }
    }
    
    /**
     * Handles player quit events
     *
     * Обрабатывает события выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unregisterGUI(event.getPlayer());
        clearPlayerMetadata(event.getPlayer());
    }
    
    public void cleanup() {
        activeGUIs.clear();
        inventoryToGUI.clear();
        playerMetadata.clear();
    }
}