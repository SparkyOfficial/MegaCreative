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
import java.util.function.Consumer;

public class PlayerHealthEditor extends AbstractParameterEditor {
    
    public PlayerHealthEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Player Health Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Health value slot
        ItemStack healthItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta healthMeta = healthItem.getItemMeta();
        healthMeta.setDisplayName("§eHealth Value");
        DataValue health = codeBlock.getParameter("health", DataValue.of("10.0"));
        healthMeta.setLore(java.util.Arrays.asList(
            "§7Enter the health value to compare",
            "§aCurrent value: §f" + (health != null ? health.asString() : "10.0")
        ));
        healthItem.setItemMeta(healthMeta);
        inventory.setItem(0, healthItem);
        
        // Operator slot
        ItemStack operatorItem = new ItemStack(Material.COMPARATOR);
        ItemMeta operatorMeta = operatorItem.getItemMeta();
        operatorMeta.setDisplayName("§eOperator");
        DataValue operator = codeBlock.getParameter("operator", DataValue.of(">"));
        operatorMeta.setLore(java.util.Arrays.asList(
            "§7Enter the comparison operator",
            "§aCurrent value: §f" + (operator != null ? operator.asString() : ">")
        ));
        operatorItem.setItemMeta(operatorMeta);
        inventory.setItem(1, operatorItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Player Health condition",
            "",
            "§eHow to use:",
            "§71. Set the health value to compare",
            "§72. Set the comparison operator"
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
            case 0: // Health value slot
                // Open anvil GUI for health value input
                DataValue currentHealth = codeBlock.getParameter("health", DataValue.of("10.0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter health value", 
                    newValue -> {
                        codeBlock.setParameter("health", DataValue.of(newValue));
                        player.sendMessage("§aHealth value set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Operator slot
                // Open anvil GUI for operator input
                DataValue currentOperator = codeBlock.getParameter("operator", DataValue.of(">"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter operator", 
                    newValue -> {
                        codeBlock.setParameter("operator", DataValue.of(newValue));
                        player.sendMessage("§aOperator set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Player Health condition.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}