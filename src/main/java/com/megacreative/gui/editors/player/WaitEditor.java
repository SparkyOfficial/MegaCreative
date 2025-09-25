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

public class WaitEditor extends AbstractParameterEditor {
    
    public WaitEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Wait Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current ticks parameter value from the CodeBlock
        DataValue ticksValue = codeBlock.getParameter("ticks");
        String currentTicks = (ticksValue != null) ? ticksValue.asString() : "Не задано";

        // Create an item to represent the ticks parameter
        ItemStack ticksItem = new ItemStack(Material.CLOCK);
        ItemMeta meta = ticksItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aЗадержка (в тиках)");
            meta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentTicks, 
                "§eНажмите, чтобы изменить."
            ));
            ticksItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, ticksItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Ticks slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aWait parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current ticks value
        DataValue ticksValue = codeBlock.getParameter("ticks");
        String currentTicks = (ticksValue != null) ? ticksValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newTicks) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("ticks", newTicks);
            player.sendMessage("§aWait time saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter wait time in ticks", callback, cancelCallback);
    }
}