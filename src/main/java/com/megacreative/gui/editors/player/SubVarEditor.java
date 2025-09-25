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

public class SubVarEditor extends AbstractParameterEditor {
    
    public SubVarEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Subtract Variable Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Variable name slot
        ItemStack varItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta varMeta = varItem.getItemMeta();
        varMeta.setDisplayName("§eИмя переменной");
        DataValue varName = codeBlock.getParameter("var", DataValue.of("myVar"));
        varMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя переменной для вычитания",
            "§aТекущее значение: §f" + (varName != null ? varName.asString() : "myVar")
        ));
        varItem.setItemMeta(varMeta);
        inventory.setItem(0, varItem);
        
        // Value slot
        ItemStack valueItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta valueMeta = valueItem.getItemMeta();
        valueMeta.setDisplayName("§eЗначение для вычитания");
        DataValue value = codeBlock.getParameter("value", DataValue.of("0"));
        valueMeta.setLore(java.util.Arrays.asList(
            "§7Введите значение для вычитания",
            "§aТекущее значение: §f" + (value != null ? value.asString() : "0")
        ));
        valueItem.setItemMeta(valueMeta);
        inventory.setItem(1, valueItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7действие вычитания значения из переменной",
            "",
            "§eКак использовать:",
            "§71. Укажите имя переменной",
            "§72. Установите значение для вычитания"
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
            case 0: // Variable name slot
                // Open anvil GUI for variable name input
                DataValue currentVar = codeBlock.getParameter("var", DataValue.of("myVar"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя переменной", 
                    newValue -> {
                        codeBlock.setParameter("var", DataValue.of(newValue));
                        player.sendMessage("§aИмя переменной установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Value slot
                // Open anvil GUI for value input
                DataValue currentValue = codeBlock.getParameter("value", DataValue.of("0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите значение для вычитания", 
                    newValue -> {
                        codeBlock.setParameter("value", DataValue.of(newValue));
                        player.sendMessage("§aЗначение установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки действия вычитания значения из переменной.");
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