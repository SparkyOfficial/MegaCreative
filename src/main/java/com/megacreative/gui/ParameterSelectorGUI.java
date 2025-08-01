package com.megacreative.gui;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.data.DataItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.List;

public class ParameterSelectorGUI implements Listener {
    private final Player player;
    private final CodeBlock codeBlock;
    private final Location blockLocation;
    private final Inventory inventory;
    private final DataItemFactory.DataItem dataItem;

    public ParameterSelectorGUI(Player player, CodeBlock codeBlock, Location blockLocation, DataItemFactory.DataItem dataItem) {
        this.player = player;
        this.codeBlock = codeBlock;
        this.blockLocation = blockLocation;
        this.dataItem = dataItem;
        this.inventory = Bukkit.createInventory(null, 9, "§8Выберите параметр");
        Bukkit.getPluginManager().registerEvents(this, MegaCreative.getInstance());
        setupItems();
    }

    private void setupItems() {
        List<String> parameters = getParametersForAction(codeBlock.getAction());
        
        for (int i = 0; i < parameters.size() && i < 7; i++) {
            String paramName = parameters.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§a" + paramName);
            meta.setLore(Arrays.asList(
                "§7Вставить данные: §f" + dataItem.type().getDisplayName(),
                "§7Значение: §f" + dataItem.value(),
                "§e▶ Нажмите для вставки"
            ));
            item.setItemMeta(meta);
            inventory.setItem(i, item);
        }
        
        // Кнопка "Отмена"
        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.setDisplayName("§c§lОтмена");
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(8, cancelButton);
    }

    private List<String> getParametersForAction(String action) {
        switch (action) {
            case "sendMessage":
            case "broadcast":
                return Arrays.asList("message");
            case "teleport":
                return Arrays.asList("coords");
            case "giveItem":
                return Arrays.asList("item", "amount");
            case "playSound":
                return Arrays.asList("sound", "volume", "pitch");
            case "effect":
                return Arrays.asList("effect", "duration", "amplifier");
            case "command":
                return Arrays.asList("command");
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return Arrays.asList("var", "value");
            case "setTime":
                return Arrays.asList("time");
            case "setWeather":
                return Arrays.asList("weather");
            case "spawnMob":
                return Arrays.asList("mob", "amount");
            case "isInWorld":
                return Arrays.asList("world");
            case "hasItem":
                return Arrays.asList("item");
            case "isNearBlock":
                return Arrays.asList("block", "radius");
            case "timeOfDay":
                return Arrays.asList("time");
            case "ifVar":
            case "ifNotVar":
                return Arrays.asList("var", "value");
            case "ifGameMode":
                return Arrays.asList("mode");
            case "ifWorldType":
                return Arrays.asList("type");
            case "ifMobType":
                return Arrays.asList("mob");
            case "ifMobNear":
                return Arrays.asList("radius");
            case "getVar":
                return Arrays.asList("var");
            default:
                return Arrays.asList();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getWhoClicked().equals(player)) return;
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
        
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        
        String displayName = clicked.getItemMeta().getDisplayName();
        
        // Обработка "Отмена"
        if (displayName.contains("Отмена")) {
            player.closeInventory();
            return;
        }
        
        // Получаем имя параметра
        String paramName = displayName.replace("§a", "");
        
        // Создаем строку данных в формате data:TYPE:value
        String dataString = "data:" + dataItem.type().name() + ":" + dataItem.value();
        
        // Устанавливаем параметр в блок
        codeBlock.setParameter(paramName, dataString);
        
        player.sendMessage("§a✓ Данные вставлены в параметр '" + paramName + "'");
        player.sendMessage("§7Тип: " + dataItem.type().getDisplayName() + ", Значение: " + dataItem.value());
        
        player.closeInventory();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(inventory) && event.getPlayer().equals(player)) {
            HandlerList.unregisterAll(this);
        }
    }
} 