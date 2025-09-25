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

public class GetPlayerNameEditor extends AbstractParameterEditor {
    
    public GetPlayerNameEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Get Player Name Editor");
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Get the current variable parameter value from the CodeBlock
        DataValue variableValue = codeBlock.getParameter("variable");
        String currentVariable = (variableValue != null) ? variableValue.asString() : "Не задано";

        // Create an item to represent the variable parameter
        ItemStack variableItem = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = variableItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aПеременная для сохранения");
            meta.setLore(Arrays.asList(
                "§7Текущая: §f" + currentVariable, 
                "§eНажмите, чтобы изменить."
            ));
            variableItem.setItemMeta(meta);
        }

        // Place the item in the inventory
        inventory.setItem(0, variableItem);
        
        // Done button
        ItemStack doneStack = new ItemStack(Material.EMERALD);
        inventory.setItem(8, doneStack);
    }
    
    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        event.setCancelled(true);
        
        int slot = event.getSlot();
        
        switch (slot) {
            case 0: // Variable slot
                openAnvilInputGUI();
                break;
                
            case 8: // Done button
                player.closeInventory();
                player.sendMessage("§aGet Player Name parameters saved!");
                break;
        }
    }
    
    private void openAnvilInputGUI() {
        // Get the current variable value
        DataValue variableValue = codeBlock.getParameter("variable");
        String currentVariable = (variableValue != null) ? variableValue.asString() : "";
        
        // Create a callback for when the player submits text
        Consumer<String> callback = (newVariable) -> {
            // When the player enters text, save it to the CodeBlock
            codeBlock.setParameter("variable", newVariable);
            player.sendMessage("§aVariable saved!");
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
        new com.megacreative.gui.AnvilInputGUI(plugin, player, "Enter variable name", callback, cancelCallback);
    }
}