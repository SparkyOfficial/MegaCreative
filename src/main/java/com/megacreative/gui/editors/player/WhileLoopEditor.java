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

public class WhileLoopEditor extends AbstractParameterEditor {
    
    public WhileLoopEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "While Loop Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Condition slot
        ItemStack conditionItem = new ItemStack(Material.REDSTONE);
        ItemMeta conditionMeta = conditionItem.getItemMeta();
        conditionMeta.setDisplayName("§eУсловие цикла");
        conditionMeta.setLore(java.util.Arrays.asList(
            "§7Введите условие для цикла",
            "§aТекущее значение: §f" + codeBlock.getParameter("condition", DataValue.of("true")).asString()
        ));
        conditionItem.setItemMeta(conditionMeta);
        inventory.setItem(0, conditionItem);
        
        // Max iterations slot
        ItemStack maxItem = new ItemStack(Material.BARRIER);
        ItemMeta maxMeta = maxItem.getItemMeta();
        maxMeta.setDisplayName("§eМаксимальное количество итераций");
        maxMeta.setLore(java.util.Arrays.asList(
            "§7Введите максимальное количество итераций",
            "§aТекущее значение: §f" + codeBlock.getParameter("maxIterations", DataValue.of("100")).asString()
        ));
        maxItem.setItemMeta(maxMeta);
        inventory.setItem(1, maxItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7цикл while",
            "",
            "§eКак использовать:",
            "§71. Укажите условие цикла",
            "§72. Установите максимальное количество итераций"
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
            case 0: // Condition slot
                // Open anvil GUI for condition input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите условие цикла", 
                    newValue -> {
                        codeBlock.setParameter("condition", DataValue.of(newValue));
                        player.sendMessage("§aУсловие цикла установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Max iterations slot
                // Open anvil GUI for max iterations input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите максимальное количество итераций", 
                    newValue -> {
                        try {
                            int maxIterations = Integer.parseInt(newValue);
                            if (maxIterations > 0 && maxIterations <= 10000) {
                                codeBlock.setParameter("maxIterations", DataValue.of(maxIterations));
                                player.sendMessage("§aМаксимальное количество итераций установлено на: §f" + maxIterations);
                                populateItems(); // Refresh the inventory
                            } else {
                                player.sendMessage("§cКоличество должно быть между 1 и 10000!");
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
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки цикла while.");
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