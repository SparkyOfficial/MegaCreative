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

public class IsInWorldEditor extends AbstractParameterEditor {
    
    public IsInWorldEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Is In World Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // World name slot
        ItemStack worldItem = new ItemStack(Material.GLOBE_BANNER_PATTERN);
        ItemMeta worldMeta = worldItem.getItemMeta();
        worldMeta.setDisplayName("§eИмя мира");
        DataValue worldName = codeBlock.getParameter("world", DataValue.of("world"));
        worldMeta.setLore(java.util.Arrays.asList(
            "§7Введите имя мира для проверки",
            "§aТекущее значение: §f" + (worldName != null ? worldName.asString() : "world")
        ));
        worldItem.setItemMeta(worldMeta);
        inventory.setItem(0, worldItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7условие проверки нахождения в мире",
            "",
            "§eКак использовать:",
            "§71. Укажите имя мира для проверки"
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
            case 0: // World name slot
                // Open anvil GUI for world name input
                DataValue currentWorld = codeBlock.getParameter("world", DataValue.of("world"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите имя мира", 
                    newValue -> {
                        codeBlock.setParameter("world", DataValue.of(newValue));
                        player.sendMessage("§aИмя мира установлено на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки условия проверки нахождения в мире.");
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