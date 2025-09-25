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

public class ExplosionEditor extends AbstractParameterEditor {
    
    public ExplosionEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Explosion Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Power slot
        ItemStack powerItem = new ItemStack(Material.TNT);
        ItemMeta powerMeta = powerItem.getItemMeta();
        powerMeta.setDisplayName("§eExplosion Power");
        DataValue power = codeBlock.getParameter("power", DataValue.of("4.0"));
        powerMeta.setLore(java.util.Arrays.asList(
            "§7Enter the explosion power",
            "§aCurrent value: §f" + (power != null ? power.asString() : "4.0")
        ));
        powerItem.setItemMeta(powerMeta);
        inventory.setItem(0, powerItem);
        
        // Break blocks slot
        ItemStack breakBlocksItem = new ItemStack(Material.BARRIER);
        ItemMeta breakBlocksMeta = breakBlocksItem.getItemMeta();
        breakBlocksMeta.setDisplayName("§eBreak Blocks");
        DataValue breakBlocks = codeBlock.getParameter("breakBlocks", DataValue.of("true"));
        breakBlocksMeta.setLore(java.util.Arrays.asList(
            "§7Should blocks be broken? (true/false)",
            "§aCurrent value: §f" + (breakBlocks != null ? breakBlocks.asString() : "true")
        ));
        breakBlocksItem.setItemMeta(breakBlocksMeta);
        inventory.setItem(1, breakBlocksItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Explosion action",
            "",
            "§eHow to use:",
            "§71. Set the explosion power",
            "§72. Choose whether blocks should be broken"
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
            case 0: // Power slot
                // Open anvil GUI for power input
                DataValue currentPower = codeBlock.getParameter("power", DataValue.of("4.0"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter explosion power", 
                    newValue -> {
                        codeBlock.setParameter("power", DataValue.of(newValue));
                        player.sendMessage("§aExplosion power set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Break blocks slot
                // Open anvil GUI for break blocks input
                DataValue currentBreakBlocks = codeBlock.getParameter("breakBlocks", DataValue.of("true"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter break blocks (true/false)", 
                    newValue -> {
                        codeBlock.setParameter("breakBlocks", DataValue.of(newValue));
                        player.sendMessage("§aBreak blocks set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Explosion action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}