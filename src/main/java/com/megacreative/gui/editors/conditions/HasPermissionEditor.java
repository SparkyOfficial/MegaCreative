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

public class HasPermissionEditor extends AbstractParameterEditor {
    
    public HasPermissionEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Has Permission Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Permission node slot
        ItemStack permissionItem = new ItemStack(Material.PAPER);
        ItemMeta permissionMeta = permissionItem.getItemMeta();
        permissionMeta.setDisplayName("§eУзел разрешения");
        DataValue permission = codeBlock.getParameter("permission", DataValue.of("megacreative.use"));
        permissionMeta.setLore(java.util.Arrays.asList(
            "§7Введите узел разрешения для проверки",
            "§aТекущее значение: §f" + (permission != null ? permission.asString() : "megacreative.use")
        ));
        permissionItem.setItemMeta(permissionMeta);
        inventory.setItem(0, permissionItem);
        
        // Permission check type slot
        ItemStack checkTypeItem = new ItemStack(Material.REDSTONE);
        ItemMeta checkTypeMeta = checkTypeItem.getItemMeta();
        checkTypeMeta.setDisplayName("§eТип проверки");
        DataValue checkType = codeBlock.getParameter("checkType", DataValue.of("HAS_PERMISSION"));
        checkTypeMeta.setLore(java.util.Arrays.asList(
            "§7Выберите тип проверки разрешения",
            "§aТекущее значение: §f" + (checkType != null ? checkType.asString() : "HAS_PERMISSION")
        ));
        checkTypeItem.setItemMeta(checkTypeMeta);
        inventory.setItem(1, checkTypeItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Помощь");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7Этот редактор позволяет настроить",
            "§7условие проверки разрешений игрока",
            "",
            "§eКак использовать:",
            "§71. Укажите узел разрешения",
            "§72. Выберите тип проверки"
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
            case 0: // Permission node slot
                // Open anvil GUI for permission input
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Введите узел разрешения", 
                    newValue -> {
                        codeBlock.setParameter("permission", newValue);
                        player.sendMessage("§aУзел разрешения установлен на: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Check type slot
                // Toggle between HAS_PERMISSION and LACKS_PERMISSION
                DataValue currentCheckType = codeBlock.getParameter("checkType", DataValue.of("HAS_PERMISSION"));
                String newCheckType = "HAS_PERMISSION".equals(currentCheckType.asString()) ? "LACKS_PERMISSION" : "HAS_PERMISSION";
                codeBlock.setParameter("checkType", DataValue.of(newCheckType));
                player.sendMessage("§aТип проверки установлен на: §f" + newCheckType);
                populateItems(); // Refresh the inventory
                break;
                
            case 8: // Help item
                player.sendMessage("§eПодсказка: Используйте этот редактор для настройки условия проверки разрешений игрока.");
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