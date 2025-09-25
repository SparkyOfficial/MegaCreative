package com.megacreative.gui.editors.events;

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

public class OnCommandEventEditor extends AbstractParameterEditor {
    
    public OnCommandEventEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "On Command Event Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Command name slot
        ItemStack commandItem = new ItemStack(Material.COMMAND_BLOCK);
        ItemMeta commandMeta = commandItem.getItemMeta();
        commandMeta.setDisplayName("§eИмя команды");
        DataValue commandName = codeBlock.getParameter("command", DataValue.of("test"));
        commandMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя команды для отслеживания",
            "§aТекущее значение: §f" + (commandName != null ? commandName.asString() : "test")
        ));
        commandItem.setItemMeta(commandMeta);
        inventory.setItem(0, commandItem);
        
        // Permission check slot
        ItemStack permissionItem = new ItemStack(Material.PAPER);
        ItemMeta permissionMeta = permissionItem.getItemMeta();
        permissionMeta.setDisplayName("§eПроверка разрешения");
        DataValue permission = codeBlock.getParameter("permission", DataValue.of(""));
        permissionMeta.setLore(java.util.Arrays.asList(
            "§7Введите разрешение для выполнения команды",
            "§aТекущее значение: §f" + (permission != null ? permission.asString() : "")
        ));
        permissionItem.setItemMeta(permissionMeta);
        inventory.setItem(1, permissionItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7событие выполнения команды",
            "",
            "§eКак использовать:",
            "§71. Укажите имя команды",
            "§72. Установите разрешение (опционально)"
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
            case 0: // Command name slot
                // Open anvil GUI for command input
                DataValue currentCommand = codeBlock.getParameter("command", DataValue.of("test"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя команды", 
                    newValue -> {
                        codeBlock.setParameter("command", DataValue.of(newValue));
                        player.sendMessage("§aИмя команды установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Permission slot
                // Open anvil GUI for permission input
                DataValue currentPermission = codeBlock.getParameter("permission", DataValue.of(""));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите разрешение (пусто для без проверки)", 
                    newValue -> {
                        codeBlock.setParameter("permission", DataValue.of(newValue));
                        player.sendMessage("§aРазрешение установлено на: §f" + (newValue.isEmpty() ? "без проверки" : newValue));
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки события выполнения команды.");
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