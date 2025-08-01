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
        this.inventory = Bukkit.createInventory(null, 27, "§8Создать шаблон данных");
        Bukkit.getPluginManager().registerEvents(this, MegaCreative.getInstance());
        setupItems();
    }

    private void setupItems() {
        // Добавляем новые типы данных: МАССИВ и ЭФФЕКТ
        inventory.setItem(0, createButton(DataType.TEXT, "Текст", "Хранит текстовую строку"));
        inventory.setItem(1, createButton(DataType.NUMBER, "Число", "Хранит целое или дробное число"));
        inventory.setItem(2, createButton(DataType.VARIABLE, "Переменная", "Ссылается на значение другой переменной"));
        inventory.setItem(3, createButton(DataType.POTION_EFFECT, "Эффект Зелья", "Хранит тип и уровень эффекта"));
    }

    private ItemStack createButton(DataType type, String name, String description) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§aПолучить шаблон: " + name);
        meta.setLore(Arrays.asList("§7" + description, "§e▶ Нажмите, чтобы получить"));
        item.setItemMeta(meta);
        return item;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player) || !event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        DataType type = null;
        switch (event.getSlot()) {
            case 0 -> type = DataType.TEXT;
            case 1 -> type = DataType.NUMBER;
            case 2 -> type = DataType.VARIABLE;
            case 3 -> type = DataType.POTION_EFFECT;
        }

        if (type != null) {
            // Создаем и выдаем предмет-данные с ПУСТЫМ значением
            ItemStack dataItem = DataItemFactory.createDataItem(type, "Не установлено");
            player.getInventory().addItem(dataItem);
            player.sendMessage("§a✓ Вы получили шаблон данных: §e" + type.getDisplayName());
            player.sendMessage("§7Возьмите его в руку и напишите значение в чат для настройки.");
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
} 