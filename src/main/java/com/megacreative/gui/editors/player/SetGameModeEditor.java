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

public class SetGameModeEditor extends AbstractParameterEditor {
    
    public SetGameModeEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Set GameMode Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Game mode slot
        ItemStack modeItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta modeMeta = modeItem.getItemMeta();
        modeMeta.setDisplayName("§eGame Mode");
        DataValue mode = codeBlock.getParameter("mode", DataValue.of("CREATIVE"));
        modeMeta.setLore(java.util.Arrays.asList(
            "§7Enter the game mode (CREATIVE/SURVIVAL/ADVENTURE/SPECTATOR)",
            "§aCurrent value: §f" + (mode != null ? mode.asString() : "CREATIVE")
        ));
        modeItem.setItemMeta(modeMeta);
        inventory.setItem(0, modeItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Set GameMode action",
            "",
            "§eHow to use:",
            "§71. Set the game mode (CREATIVE/SURVIVAL/ADVENTURE/SPECTATOR)"
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
            case 0: // Game mode slot
                // Open anvil GUI for game mode input
                DataValue currentMode = codeBlock.getParameter("mode", DataValue.of("CREATIVE"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter game mode", 
                    newValue -> {
                        codeBlock.setParameter("mode", DataValue.of(newValue));
                        player.sendMessage("§aGame mode set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Set GameMode action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}