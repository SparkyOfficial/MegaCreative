package com.megacreative.gui.editors.player;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.gui.editors.AbstractParameterEditor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.function.Consumer;

public class RemoveItemsEditor extends AbstractParameterEditor {
    
    public RemoveItemsEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Remove Items Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current items parameter value from the CodeBlock
        DataValue itemsValue = codeBlock.getParameter("items");
        String currentItems = (itemsValue != null) ? itemsValue.asString() : "Не задано";

        // Create an item to represent the items parameter
        ItemStack itemsItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = itemsItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cУдаляемые предметы");
            meta.setLore(Arrays.asList(
                "§7Текущие: §f" + currentItems, 
                "§eНажмите, чтобы изменить."
            ));
            itemsItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, itemsItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Items slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aRemove Items parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current items value
        DataValue itemsValue = codeBlock.getParameter("items");
        String currentItems = (itemsValue != null) ? itemsValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newItems) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("items", newItems);
            player.sendMessage("§aItems to remove saved!");
            // Reopen the editor to show the updated value
            populateItems();
        };
        
        // Create a cancel callback
        Runnable cancelCallback = () -> {
            // Reopen the editor
            populateItems();
            player.openInventory(inventory);
        };
        
        // Create and open the anvil input GUI
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter items to remove (item1:amount1,item2:amount2)", callback, cancelCallback);
    }
}