package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DataGUI implements Listener {
    private final Player player;
    private final Inventory inventory;

    public DataGUI(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(null, 9, "§8Создать данные");
        Bukkit.getPluginManager().registerEvents(this, MegaCreative.getInstance());
        setupItems();
    }

    private void setupItems() {
        inventory.setItem(0, createButton(DataType.TEXT, "Текст", "Пример текста"));
        inventory.setItem(1, createButton(DataType.NUMBER, "Число", "123"));
        inventory.setItem(2, createButton(DataType.VARIABLE, "Переменная", "имя_переменной"));

        // Кнопка "Назад"
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cЗакрыть");
        backButton.setItemMeta(backMeta);
        inventory.setItem(8, backButton);
    }

    private ItemStack createButton(DataType type, String name, String exampleValue) {
        ItemStack item = new ItemStack(Material.WRITABLE_BOOK); // Изменили иконку для консистентности
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aСоздать " + name);
        meta.setLore(Arrays.asList(
                "§7Тип: §f" + type.name(),
                "§7Пример: §f" + exampleValue,
                "§e▶ Нажмите для создания"
        ));
        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player) || !event.getInventory().equals(inventory)) {
            return;
        }
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        // --- Обработка клика по кнопке ---
        int slot = event.getSlot();
        if (slot > 2) { // Если это не кнопка создания данных
            if(slot == 8) player.closeInventory();
            return;
        }

        DataType selectedType = null;
        String defaultValue = "";

        switch (slot) {
            case 0 -> {
                selectedType = DataType.TEXT;
                defaultValue = "Привет, мир!";
            }
            case 1 -> {
                selectedType = DataType.NUMBER;
                defaultValue = "100";
            }
            case 2 -> {
                selectedType = DataType.VARIABLE;
                defaultValue = "score";
            }
        }

        // --- Вот место исправления! ---
        // Создаем final копии переменных, которые будем использовать в лямбде.
        final DataType finalSelectedType = selectedType;
        final String finalDefaultValue = defaultValue;

        if (finalSelectedType != null) {
            new AnvilInputGUI(player, finalDefaultValue, (value) -> {
                // Внутри этой лямбды мы используем final-копии. Теперь ошибки нет.
                ItemStack dataItem = DataItemFactory.createDataItem(finalSelectedType, value);
                player.getInventory().addItem(dataItem);
                player.sendMessage("§a✓ Создан предмет-данные: " + finalSelectedType.getDisplayName() + " со значением '" + value + "'");
            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
} 