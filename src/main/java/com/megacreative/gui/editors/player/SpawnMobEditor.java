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

public class SpawnMobEditor extends AbstractParameterEditor {
    
    public SpawnMobEditor(MegaCreative plugin, Player player, CodeBlock codeBlock) {
        super(plugin, player, codeBlock, 9, "Spawn Mob Editor");
        
        // Set up the inventory with default items
        populateItems();
    }
    
    @Override
    public void populateItems() {
        inventory.clear();
        
        // Mob type slot
        ItemStack mobItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta mobMeta = mobItem.getItemMeta();
        mobMeta.setDisplayName("§eMob Type");
        DataValue mobType = codeBlock.getParameter("mob", DataValue.of("ZOMBIE"));
        mobMeta.setLore(java.util.Arrays.asList(
            "§7Enter the mob type to spawn",
            "§aCurrent value: §f" + (mobType != null ? mobType.asString() : "ZOMBIE")
        ));
        mobItem.setItemMeta(mobMeta);
        inventory.setItem(0, mobItem);
        
        // Amount slot
        ItemStack amountItem = new ItemStack(Material.HOPPER);
        ItemMeta amountMeta = amountItem.getItemMeta();
        amountMeta.setDisplayName("§eAmount");
        DataValue amount = codeBlock.getParameter("amount", DataValue.of("1"));
        amountMeta.setLore(java.util.Arrays.asList(
            "§7Enter the number of mobs to spawn",
            "§aCurrent value: §f" + (amount != null ? amount.asString() : "1")
        ));
        amountItem.setItemMeta(amountMeta);
        inventory.setItem(1, amountItem);
        
        // Help item
        ItemStack helpItem = new ItemStack(Material.BOOK);
        ItemMeta helpMeta = helpItem.getItemMeta();
        helpMeta.setDisplayName("§6Help");
        helpMeta.setLore(java.util.Arrays.asList(
            "§7This editor configures the Spawn Mob action",
            "",
            "§eHow to use:",
            "§71. Set the mob type to spawn",
            "§72. Set the number of mobs to spawn"
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
            case 0: // Mob type slot
                // Open anvil GUI for mob type input
                DataValue currentMob = codeBlock.getParameter("mob", DataValue.of("ZOMBIE"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter mob type", 
                    newValue -> {
                        codeBlock.setParameter("mob", DataValue.of(newValue));
                        player.sendMessage("§aMob type set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 1: // Amount slot
                // Open anvil GUI for amount input
                DataValue currentAmount = codeBlock.getParameter("amount", DataValue.of("1"));
                new AnvilInputGUI(
                    plugin, 
                    player, 
                    "Enter amount", 
                    newValue -> {
                        codeBlock.setParameter("amount", DataValue.of(newValue));
                        player.sendMessage("§aAmount set to: §f" + newValue);
                        populateItems(); // Refresh the inventory
                    },
                    () -> {} // Empty cancel callback
                );
                player.closeInventory();
                break;
                
            case 8: // Help item
                player.sendMessage("§eTip: Use this editor to configure the Spawn Mob action.");
                break;
        }
    }
    
    @Override
    public void open() {
        player.openInventory(inventory);
    }
}