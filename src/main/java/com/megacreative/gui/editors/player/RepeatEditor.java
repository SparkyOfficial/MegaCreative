package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.gui.AnvilInputGUI;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RepeatEditor extends AbstractParameterEditor {
    
    public RepeatEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Repeat Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Count slot
        ItemStack countItem = new ItemStack(Material.REPEATER);
        ItemMeta countMeta = countItem.getItemMeta();
        countMeta.setDisplayName("§eКоличество повторений");
        countMeta.setLore(java.util.Arrays.asList(
            "§7Введите количество повторений",
            "§aТекущее значение: §f" + codeBlock.getParameter("count", DataValue.of("5")).asString()
        ));
        countItem.setItemMeta(countMeta);
        inventory.setItem(0, countItem);
        
        // Delay slot
        ItemStack delayItem = new ItemStack(Material.CLOCK);
        ItemMeta delayMeta = delayItem.getItemMeta();
        delayMeta.setDisplayName("§eЗадержка между повторениями");
        delayMeta.setLore(java.util.Arrays.asList(
            "§7Введите задержку в тиках",
            "§aТекущее значение: §f" + codeBlock.getParameter("delay", DataValue.of("20")).asString()
        ));
        delayItem.setItemMeta(delayMeta);
        inventory.setItem(1, delayItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7действие повторения",
            "",
            "§eКак использовать:",
            "§71. Укажите количество повторений",
            "§72. Установите задержку между повторениями"
        ));
        helpItem.setItemMeta(helpMeta);
        inventory.setItem(8, helpItem);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        
        switch (slot) {
            case 0: // Count slot
                // Open anvil GUI for count input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите количество повторений", 
                    newValue -> {
                        try {
                            int count = Integer.parseInt(newValue);
                            if (count > 0 && count <= 1000) {
                                codeBlock.setParameter("count", DataValue.of(count));
                                player.sendMessage("§aКоличество повторений установлено на: §f" + count);
                                populateItems(); // Refresh the inventory
                            } else {
                                player.sendMessage("§cКоличество должно быть между 1 и 1000!");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cВведите корректное числовое значение!");
                        }
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Delay slot
                // Open anvil GUI for delay input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите задержку в тиках", 
                    newValue -> {
                        try {
                            int delay = Integer.parseInt(newValue);
                            if (delay >= 0 && delay <= 1200) {
                                codeBlock.setParameter("delay", DataValue.of(delay));
                                player.sendMessage("§aЗадержка установлена на: §f" + delay + " тиков");
                                populateItems(); // Refresh the inventory
                            } else {
                                player.sendMessage("§cЗадержка должна быть между 0 и 1200 тиков!");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cВведите корректное числовое значение!");
                        }
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки действия повторения.");
                break;
        }
    }
    
    @Override
    protected void onLoadContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Load items from container to editor inventory
        for (int i = 0; i < Math.min(containerInventory.getSize(), inventory.getSize()); i++) {
            ItemStack item = containerInventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(i, item.clone());
            }
        }
    }
    
    @Override
    protected void onSaveContainerItems(org.bukkit.inventory.Inventory containerInventory) {
        // Save items from editor inventory to container
        for (int i = 0; i < Math.min(containerInventory.getSize(), inventory.getSize()); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                containerInventory.setItem(i, item.clone());
            } else {
                containerInventory.clear(i);
            }
        }
    }
}