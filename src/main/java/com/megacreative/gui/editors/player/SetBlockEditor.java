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

public class SetBlockEditor extends AbstractParameterEditor {
    
    public SetBlockEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set Block Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Block material slot
        ItemStack materialItem = new ItemStack(Material.STONE);
        ItemMeta materialMeta = materialItem.getItemMeta();
        materialMeta.setDisplayName("§eBlock Material");
        DataValue material = codeBlock.getParameter("material", DataValue.of("STONE"));
        materialMeta.setLore(java.util.Arrays.asList(
            "§7Enter the block material to set",
            "§aCurrent value: §f" + (material != null ? material.asString() : "STONE")
        ));
        materialItem.setItemMeta(materialMeta);
        inventory.setItem(0, materialItem);
        
        // Coordinates slot
        ItemStack coordsItem = new ItemStack(Material.COMPASS);
        ItemMeta coordsMeta = coordsItem.getItemMeta();
        coordsMeta.setDisplayName("§eCoordinates");
        DataValue coords = codeBlock.getParameter("coords", DataValue.of("100 70 200"));
        coordsMeta.setLore(java.util.Arrays.asList(
            "§7Enter the coordinates (x y z)",
            "§aCurrent value: §f" + (coords != null ? coords.asString() : "100 70 200")
        ));
        coordsItem.setItemMeta(coordsMeta);
        inventory.setItem(1, coordsItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Set Block action",
            "",
            "§eHow to use:",
            "§71. Set the block material to set",
            "§72. Set the coordinates (x y z)"
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
            case 0: // Block material slot
                // Open anvil GUI for block material input
                DataValue currentMaterial = codeBlock.getParameter("material", DataValue.of("STONE"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter block material", 
                    newValue -> {
                        codeBlock.setParameter("material", DataValue.of(newValue));
                        player.sendMessage("§aBlock material set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Coordinates slot
                // Open anvil GUI for coordinates input
                DataValue currentCoords = codeBlock.getParameter("coords", DataValue.of("100 70 200"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter coordinates (x y z)", 
                    newValue -> {
                        codeBlock.setParameter("coords", DataValue.of(newValue));
                        player.sendMessage("§aCoordinates set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Set Block action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}