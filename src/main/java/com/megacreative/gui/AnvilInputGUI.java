package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Управляет графическим интерфейсом ввода текста через наковальню
 * Реализует интерфейс ManagedGUIInterface для интеграции с GUIManager
 *
 * Manages text input GUI through anvil interface
 * Implements ManagedGUIInterface for integration with GUIManager
 *
 * Verwaltet die Texteingabe-GUI über eine Amboss-Schnittstelle
 * Implementiert ManagedGUIInterface für die Integration mit GUIManager
 */
public class AnvilInputGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final String title;
    private final Consumer<String> onComplete;
    private final Runnable onCancel;
    private final GUIManager guiManager;
    private Inventory anvilInventory;
    
    /**
     * Инициализирует графический интерфейс ввода текста через наковальню
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param title Заголовок интерфейса
     * @param onComplete Обратный вызов при успешном вводе
     * @param onCancel Обратный вызов при отмене
     *
     * Initializes text input GUI through anvil
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param title Interface title
     * @param onComplete Callback on successful input
     * @param onCancel Callback on cancellation
     *
     * Initialisiert die Texteingabe-GUI über einen Amboss
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param title Schnittstellentitel
     * @param onComplete Rückruf bei erfolgreicher Eingabe
     * @param onCancel Rückruf bei Abbruch
     */
    public AnvilInputGUI(MegaCreative plugin, Player player, String title, Consumer<String> onComplete, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.onComplete = onComplete;
        this.onCancel = onCancel;
        this.guiManager = plugin.getServiceRegistry().getGuiManager();
        
        openAnvil();
    }
    
    /**
     * Открывает интерфейс наковальни для ввода текста
     *
     * Opens anvil interface for text input
     *
     * Öffnet die Amboss-Schnittstelle für die Texteingabe
     */
    private void openAnvil() {
        // Создаем наковальню
        anvilInventory = Bukkit.createInventory(player, 3, title);
        
        // Устанавливаем начальный предмет
        ItemStack inputItem = new ItemStack(Material.PAPER);
        ItemMeta inputMeta = inputItem.getItemMeta();
        inputMeta.setDisplayName("§7Введите текст здесь");
        inputItem.setItemMeta(inputMeta);
        anvilInventory.setItem(0, inputItem);
        
        // Register with GUIManager and open inventory
        guiManager.registerGUI(player, this, anvilInventory);
        player.openInventory(anvilInventory);
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
        return "Anvil Input GUI: " + title;
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
        if (!event.getInventory().equals(anvilInventory)) return;
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        // Проверяем, что это наковальня
        if (!(event.getInventory() instanceof AnvilInventory)) return;
        
        AnvilInventory anvil = (AnvilInventory) event.getInventory();
        
        // Если игрок нажал на результат (слот 2)
        if (event.getSlot() == 2) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.hasItemMeta()) {
                String inputText = result.getItemMeta().getDisplayName();
                if (inputText != null && !inputText.isEmpty() && !inputText.equals("§7Введите текст здесь")) {
                    // Убираем цветовые коды
                    String cleanText = inputText.replaceAll("§[0-9a-fk-or]", "");
                    
                    player.closeInventory();
                    // GUIManager will handle automatic cleanup
                    
                    // Вызываем callback
                    onComplete.accept(cleanText);
                }
            }
        }
        
        // Если игрок закрыл инвентарь
        if (event.getAction().name().contains("DROP") || event.getAction().name().contains("PICKUP")) {
            // Проверяем, закрыл ли игрок инвентарь
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!player.getOpenInventory().getTitle().equals(title)) {
                    // GUIManager will handle automatic cleanup
                    onCancel.run();
                }
            }, 1L);
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
        // Handle anvil close - run cancel callback if not already processed
        if (onCancel != null) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                onCancel.run();
            }, 1L);
        }
    }
    
    /**
     * Обрабатывает события подготовки наковальни
     * @param event Событие подготовки наковальни
     *
     * Handles anvil prepare events
     * @param event Anvil prepare event
     *
     * Verarbeitet Amboss-Vorbereitungsereignisse
     * @param event Amboss-Vorbereitungsereignis
     */
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!event.getInventory().getViewers().contains(player)) return;
        
        ItemStack firstItem = event.getInventory().getItem(0);
        if (firstItem != null && firstItem.hasItemMeta()) {
            String inputText = firstItem.getItemMeta().getDisplayName();
            if (inputText != null && !inputText.isEmpty() && !inputText.equals("§7Введите текст здесь")) {
                // Создаем результат
                ItemStack result = new ItemStack(Material.PAPER);
                ItemMeta resultMeta = result.getItemMeta();
                resultMeta.setDisplayName(inputText);
                resultMeta.setLore(Arrays.asList(
                    "§7Нажмите, чтобы подтвердить",
                    "§7или закройте для отмены"
                ));
                result.setItemMeta(resultMeta);
                event.setResult(result);
            }
        }
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