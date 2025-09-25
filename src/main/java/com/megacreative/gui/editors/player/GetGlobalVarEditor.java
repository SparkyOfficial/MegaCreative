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

public class GetGlobalVarEditor extends AbstractParameterEditor {
    
    public GetGlobalVarEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Get Global Variable Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Variable name slot
        ItemStack varItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta varMeta = varItem.getItemMeta();
        varMeta.setDisplayName("§eИмя глобальной переменной");
        varMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя глобальной переменной для получения",
            "§aТекущее значение: §f" + codeBlock.getParameter("var", DataValue.of("globalVar")).asString()
        ));
        varItem.setItemMeta(varMeta);
        inventory.setItem(0, varItem);
        
        // Target variable slot
        ItemStack targetItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta targetMeta = targetItem.getItemMeta();
        targetMeta.setDisplayName("§eЦелевая переменная");
        targetMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя целевой переменной",
            "§aТекущее значение: §f" + codeBlock.getParameter("target", DataValue.of("result")).asString()
        ));
        targetItem.setItemMeta(targetMeta);
        inventory.setItem(1, targetItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7действие получения значения глобальной переменной",
            "",
            "§eКак использовать:",
            "§71. Укажите имя глобальной переменной для получения",
            "§72. Установите целевую переменную"
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
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя глобальной переменной", 
                    newValue -> {
                        codeBlock.setParameter("var", DataValue.of(newValue));
                        player.sendMessage("§aИмя глобальной переменной установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Target variable slot
                // Open anvil GUI for target variable input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя целевой переменной", 
                    newValue -> {
                        codeBlock.setParameter("target", DataValue.of(newValue));
                        player.sendMessage("§aЦелевая переменная установлена на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки действия получения значения глобальной переменной.");
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