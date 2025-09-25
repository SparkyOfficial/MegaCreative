package com.megacreative.gui.editors.conditions;

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

public class IfVarEqualsEditor extends AbstractParameterEditor {
    
    public IfVarEqualsEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "If Variable Equals Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Variable name slot
        ItemStack varItem = new ItemStack(Material.OBSIDIAN);
        ItemMeta varMeta = varItem.getItemMeta();
        varMeta.setDisplayName("§eИмя переменной");
        DataValue varName = codeBlock.getParameter("variable", DataValue.of("myVar"));
        varMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя переменной для проверки",
            "§aТекущее значение: §f" + (varName != null ? varName.asString() : "myVar")
        ));
        varItem.setItemMeta(varMeta);
        inventory.setItem(0, varItem);
        
        // Value slot
        ItemStack valueItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta valueMeta = valueItem.getItemMeta();
        valueMeta.setDisplayName("§eЗначение для сравнения");
        DataValue value = codeBlock.getParameter("value", DataValue.of("0"));
        valueMeta.setLore(java.util.Arrays.asList(
            "§7Введите значение для сравнения",
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
            "§7условие сравнения переменной с значением",
            "",
            "§eКак использовать:",
            "§71. Укажите имя переменной для проверки",
            "§72. Установите значение для сравнения"
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
                DataValue currentVar = codeBlock.getParameter("variable", DataValue.of("myVar"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя переменной", 
                    newValue -> {
                        codeBlock.setParameter("variable", DataValue.of(newValue));
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
                    "Введите значение для сравнения", 
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
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки условия сравнения переменной с значением.");
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