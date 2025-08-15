package com.megacreative.coding;

import com.megacreative.coding.data.DataItemFactory;
import com.megacreative.coding.data.DataItemFactory.DataItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.megacreative.listeners.GuiListener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ParameterSelectorGUI {
    private final Player player;
    private final CodeBlock targetBlock;
    private final DataItem dataItem;
    private final Consumer<String> onParameterSelected;
    private final Inventory inventory;

    public ParameterSelectorGUI(Player player, CodeBlock targetBlock, DataItem dataItem, Consumer<String> onParameterSelected) {
        this.player = player;
        this.targetBlock = targetBlock;
        this.dataItem = dataItem;
        this.onParameterSelected = onParameterSelected;
        this.inventory = Bukkit.createInventory(null, 9, "§bВыберите параметр для вставки данных");
        // Регистрируем GUI в централизованной системе
        GuiListener.registerOpenGui(player, this);
        setupInventory();
    }

    private void setupInventory() {
        inventory.clear();
        
        // Получаем доступные параметры для данного действия
        List<String> availableParameters = getAvailableParameters(targetBlock.getAction());
        
        // Создаем предметы для каждого параметра
        for (int i = 0; i < availableParameters.size() && i < 7; i++) {
            String parameter = availableParameters.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            
            meta.setDisplayName("§a" + parameter);
            meta.setLore(Arrays.asList(
                "§7Тип данных: §f" + dataItem.type().getDisplayName(),
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

    private List<String> getAvailableParameters(String action) {
        // Возвращаем список параметров, которые подходят для данного типа данных
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
                
            case "healPlayer":
                return Arrays.asList("amount");
                
            case "setGameMode":
                return Arrays.asList("mode");
                
            case "setTime":
                return Arrays.asList("time");
                
            case "setWeather":
                return Arrays.asList("weather");
                
            case "explosion":
                return Arrays.asList("power", "breakBlocks");
                
            case "setBlock":
                return Arrays.asList("material", "coords");
                
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return Arrays.asList("var", "value");
                
            case "wait":
                return Arrays.asList("ticks");
                
            case "randomNumber":
                return Arrays.asList("min", "max", "var");
                
            case "playParticle":
                return Arrays.asList("particle", "count", "offset");
                
            case "getVar":
            case "getPlayerName":
                return Arrays.asList("var");
                
            case "setGlobalVar":
            case "getGlobalVar":
            case "setServerVar":
            case "getServerVar":
                return Arrays.asList("var", "value", "localVar");
                
            case "repeat":
                return Arrays.asList("times");
                
            case "callFunction":
                return Arrays.asList("function");
                
            case "saveFunction":
                return Arrays.asList("name");
                
            case "repeatTrigger":
                return Arrays.asList("ticks", "action");
                
            default:
                return Arrays.asList();
        }
    }

    public void open() {
        player.openInventory(inventory);
    }

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
            GuiListener.unregisterOpenGui(player);
            return;
        }
        
        // Обработка выбора параметра
        int clickedSlot = event.getSlot();
        List<String> availableParameters = getAvailableParameters(targetBlock.getAction());
        
        if (clickedSlot < availableParameters.size()) {
            String selectedParameter = availableParameters.get(clickedSlot);
            
            // Создаем строку-указатель на данные
            String dataPointer = "data:" + dataItem.type().name() + ":" + dataItem.value();
            
            // Вызываем callback с выбранным параметром и указателем на данные
            onParameterSelected.accept(selectedParameter + ":" + dataPointer);
            
            player.sendMessage("§a✓ Данные вставлены в параметр '" + selectedParameter + "'");
            player.closeInventory();
            GuiListener.unregisterOpenGui(player);
        }
    }
} 