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

public class CallFunctionEditor extends AbstractParameterEditor {
    
    public CallFunctionEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Call Function Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Function name slot
        ItemStack functionItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta functionMeta = functionItem.getItemMeta();
        functionMeta.setDisplayName("§eИмя функции");
        functionMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя функции для вызова",
            "§aТекущее значение: §f" + codeBlock.getParameter("function", DataValue.of("myFunction")).asString()
        ));
        functionItem.setItemMeta(functionMeta);
        inventory.setItem(0, functionItem);
        
        // Parameters slot
        ItemStack paramsItem = new ItemStack(Material.CHEST);
        ItemMeta paramsMeta = paramsItem.getItemMeta();
        paramsMeta.setDisplayName("§eПараметры функции");
        paramsMeta.setLore(java.util.Arrays.asList(
            "§7Введите параметры функции (через запятую)",
            "§aТекущее значение: §f" + codeBlock.getParameter("parameters", DataValue.of("")).asString()
        ));
        paramsItem.setItemMeta(paramsMeta);
        inventory.setItem(1, paramsItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7вызов функции",
            "",
            "§eКак использовать:",
            "§71. Укажите имя функции для вызова",
            "§72. Установите параметры функции"
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
            case 0: // Function name slot
                // Open anvil GUI for function name input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя функции", 
                    newValue -> {
                        codeBlock.setParameter("function", DataValue.of(newValue));
                        player.sendMessage("§aИмя функции установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Parameters slot
                // Open anvil GUI for parameters input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите параметры функции (через запятую)", 
                    newValue -> {
                        codeBlock.setParameter("parameters", DataValue.of(newValue));
                        player.sendMessage("§aПараметры функции установлены на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки вызова функции.");
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