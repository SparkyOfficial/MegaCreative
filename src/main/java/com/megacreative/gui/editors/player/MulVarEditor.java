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
import java.util.function.Consumer;

public class MulVarEditor extends AbstractParameterEditor {
    
    public MulVarEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Multiply Variable Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Variable name slot
        ItemStack varItem = new ItemStack(Material.IRON_INGOT);
        ItemMeta varMeta = varItem.getItemMeta();
        varMeta.setDisplayName("§eVariable Name");
        DataValue varName = codeBlock.getParameter("var", DataValue.of("myVar"));
        varMeta.setLore(java.util.Arrays.asList(
            "§7Enter the variable name",
            "§aCurrent value: §f" + (varName != null ? varName.asString() : "myVar")
        ));
        varItem.setItemMeta(varMeta);
        inventory.setItem(0, varItem);
        
        // Value slot
        ItemStack valueItem = new ItemStack(Material.GOLD_INGOT);
        ItemMeta valueMeta = valueItem.getItemMeta();
        valueMeta.setDisplayName("§eValue to Multiply");
        DataValue value = codeBlock.getParameter("value", DataValue.of("0"));
        valueMeta.setLore(java.util.Arrays.asList(
            "§7Enter the value to multiply by",
            "§aCurrent value: §f" + (value != null ? value.asString() : "0")
        ));
        valueItem.setItemMeta(valueMeta);
        inventory.setItem(1, valueItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Multiply Variable action",
            "",
            "§eHow to use:",
            "§71. Set the variable name",
            "§72. Set the value to multiply by"
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
                    "Enter variable name", 
                    newValue -> {
                        codeBlock.setParameter("var", DataValue.of(newValue));
                        player.sendMessage("§aVariable name set to: §f" + newValue);
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
                    "Enter value to multiply", 
                    newValue -> {
                        codeBlock.setParameter("value", DataValue.of(newValue));
                        player.sendMessage("§aValue set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Multiply Variable action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}