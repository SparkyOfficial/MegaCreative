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

public class DataGUI implements GUIManager.ManagedGUIInterface {
    
    private final MegaCreative plugin;
    private final Player player;
    private final Inventory inventory;
    private final GUIManager guiManager;
    
    public DataGUI(MegaCreative plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.guiManager = plugin.getGuiManager();
        this.inventory = Bukkit.createInventory(null, 27, "§8§lТипы данных");
        
        setupInventory();
    }
    
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
    
    public void open() {
        // Register with GUIManager and open inventory
        guiManager.registerGUI(player, this, inventory);
        player.openInventory(inventory);
    }
    
    @Override
    public String getGUITitle() {
        return "Data Types GUI";
    }
    
    @Override
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
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optional cleanup when GUI is closed
        // GUIManager handles automatic unregistration
    }
    
    @Override
    public void onCleanup() {
        // Called when GUI is being cleaned up by GUIManager
        // No special cleanup needed for this GUI
    }
} 