package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.managers.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Управляет графическим интерфейсом выбора параметров для блоков кода
 * Позволяет игрокам выбирать значения параметров из предопределенного списка
 *
 * Manages parameter selection GUI for code blocks
 * Allows players to select parameter values from a predefined list
 *
 * Verwaltet die Parameterauswahl-GUI für Codeblöcke
 * Ermöglicht Spielern die Auswahl von Parameterwerten aus einer vordefinierten Liste
 */
public class ParameterSelectorGUI implements Listener {
    
    private final MegaCreative plugin;
    private final Player player;
    private final CodeBlock block;
    private final String parameterName;
    private final List<String> options;
    private final Consumer<String> onSelect;
    private final Inventory inventory;
    
    /**
     * Инициализирует графический интерфейс выбора параметров
     * @param plugin Ссылка на основной плагин
     * @param player Игрок, который будет использовать интерфейс
     * @param block Блок кода, для которого выбирается параметр
     * @param parameterName Имя параметра
     * @param options Список доступных опций
     * @param onSelect Обратный вызов при выборе опции
     *
     * Initializes parameter selection GUI
     * @param plugin Reference to main plugin
     * @param player Player who will use the interface
     * @param block Code block for which parameter is selected
     * @param parameterName Parameter name
     * @param options List of available options
     * @param onSelect Callback on option selection
     *
     * Initialisiert die Parameterauswahl-GUI
     * @param plugin Referenz zum Haupt-Plugin
     * @param player Spieler, der die Schnittstelle verwenden wird
     * @param block Codeblock, für den der Parameter ausgewählt wird
     * @param parameterName Parametername
     * @param options Liste der verfügbaren Optionen
     * @param onSelect Rückruf bei Optionsauswahl
     */
    public ParameterSelectorGUI(MegaCreative plugin, Player player, CodeBlock block, String parameterName, List<String> options, Consumer<String> onSelect) {
        this.plugin = plugin;
        this.player = player;
        this.block = block;
        this.parameterName = parameterName;
        this.options = options;
        this.onSelect = onSelect;
        this.inventory = Bukkit.createInventory(null, 27, "§8§lВыбор параметра: " + parameterName);
        
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
        
        // Отображение опций
        int slot = 10;
        for (String option : options) {
            if (slot > 16) break;
            
            ItemStack optionItem = new ItemStack(Material.PAPER);
            ItemMeta optionMeta = optionItem.getItemMeta();
            optionMeta.setDisplayName("§f§l" + option);
            optionMeta.setLore(Arrays.asList(
                "§7Параметр: §f" + parameterName,
                "§7Блок: §f" + block.getAction(),
                "",
                "§a▶ Нажмите для выбора"
            ));
            optionItem.setItemMeta(optionMeta);
            inventory.setItem(slot, optionItem);
            
            slot++;
        }
        
        // Кнопка отмены
        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§lОтмена");
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(22, cancelButton);
    }
    
    /**
     * Открывает графический интерфейс для игрока
     *
     * Opens the GUI for the player
     *
     * Öffnet die GUI für den Spieler
     */
    public void open() {
        // Регистрируем GUI в централизованной системе
        plugin.getServiceRegistry().getGuiManager().registerGUI(player, new GUIManager.ManagedGUIInterface() {
            @Override
            public void onInventoryClick(InventoryClickEvent event) {
                handleInventoryClick(event);
            }
            
            @Override
            public String getGUITitle() {
                return "ParameterSelectorGUI: " + parameterName;
            }
        }, inventory);
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
    private void handleInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        if (!(event.getWhoClicked() instanceof Player clicker) || !clicker.equals(player)) {
            return;
        }
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Кнопка отмены
        if (displayName.contains("Отмена")) {
            player.closeInventory();
            return;
        }
        
        // Выбор опции
        if (displayName.startsWith("§f§l")) {
            String selectedOption = displayName.substring(4); // Убираем "§f§l"
            
            player.closeInventory();
            
            // Вызываем callback
            onSelect.accept(selectedOption);
        }
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
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        handleInventoryClick(event);
    }
}