package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.gui.DataGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for handling interactions with coding items
 * Handles right-click actions on special coding items like "Create Data" and "Game Value"
 */
public class CodingItemListener implements Listener {
    
    private final MegaCreative plugin;
    
    public CodingItemListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-click events with items in main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND || 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if player is using the "Create Data" item
        if (isCreateDataItem(item)) {
            event.setCancelled(true);
            openDataGUI(player);
            return;
        }
        
        // Check if player is using the "Game Value" item
        if (isGameValueItem(item)) {
            event.setCancelled(true);
            openDataGUI(player);
            return;
        }
    }
    
    /**
     * Checks if the item is the "Create Data" coding item
     */
    private boolean isCreateDataItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        return item.getItemMeta().getDisplayName().equals(CodingItems.DATA_CREATOR_NAME);
    }
    
    /**
     * Checks if the item is the "Game Value" coding item
     */
    private boolean isGameValueItem(ItemStack item) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return false;
        }
        
        return item.getItemMeta().getDisplayName().equals(CodingItems.GAME_VALUE_NAME);
    }
    
    /**
     * Opens the DataGUI for the player
     */
    private void openDataGUI(Player player) {
        try {
            DataGUI dataGUI = new DataGUI(plugin, player);
            dataGUI.open();
        } catch (Exception e) {
            player.sendMessage("§cError opening Data GUI: " + e.getMessage());
            plugin.getLogger().severe("Error opening Data GUI for player " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}