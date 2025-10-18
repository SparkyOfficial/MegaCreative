package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.containers.BlockContainer;
import com.megacreative.coding.containers.BlockContainerManager;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for block container interactions
 * Handles sign editor and other container GUI interactions
 */
public class BlockContainerListener implements Listener {
    
    private final MegaCreative plugin;
    
    public BlockContainerListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        
        if (player.hasMetadata("editing_sign_container")) {
            handleSignEditorClick(event, player);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        
        if (player.hasMetadata("editing_sign_container")) {
            player.removeMetadata("editing_sign_container", plugin);
        }
    }
    
    /**
     * Handles clicks in the sign editor inventory
     */
    private void handleSignEditorClick(InventoryClickEvent event, Player player) {
        event.setCancelled(true);
        
        Inventory inventory = event.getInventory();
        int slot = event.getRawSlot();
        
        
        if (!event.getView().getTitle().startsWith("§eSign Editor: ")) {
            return;
        }
        
        
        MetadataValue metadata = player.getMetadata("editing_sign_container").get(0);
        if (!(metadata.value() instanceof BlockContainer container)) {
            player.sendMessage("§cError: Container data not found.");
            player.closeInventory();
            return;
        }
        
        
        if (slot >= 0 && slot < 4) {
            
            handleLineEdit(player, container, slot);
        } else if (slot == 8) {
            
            handleSaveClick(player, container, inventory);
        }
    }
    
    /**
     * Handles line editing clicks
     */
    private void handleLineEdit(Player player, BlockContainer container, int slot) {
        
        player.sendMessage("§ePlease type the new text for line " + (slot + 1) + " in chat.");
        player.sendMessage("§7Type 'cancel' to cancel editing.");
        
        
        player.setMetadata("editing_sign_line", new org.bukkit.metadata.FixedMetadataValue(plugin, slot));
        player.setMetadata("editing_sign_container_line", new org.bukkit.metadata.FixedMetadataValue(plugin, container));
        
        
        player.closeInventory();
    }
    
    /**
     * Handles save button clicks
     */
    private void handleSaveClick(Player player, BlockContainer container, Inventory inventory) {
        
        updateSignFromInventory(player, container, inventory);
        
        
        player.removeMetadata("editing_sign_container", plugin);
        player.closeInventory();
        player.sendMessage("§aSign updated successfully!");
    }
    
    /**
     * Updates the sign from the inventory items
     */
    private void updateSignFromInventory(Player player, BlockContainer container, Inventory inventory) {
        
        org.bukkit.block.Block signBlock = container.getContainerLocation().getBlock();
        if (!(signBlock.getState() instanceof Sign sign)) {
            player.sendMessage("§cSign is no longer available.");
            return;
        }
        
        
        for (int i = 0; i < 4; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.PAPER) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore != null && lore.size() > 1) {
                        String currentLine = lore.get(1).substring(9); 
                        if (currentLine.startsWith("§o")) {
                            currentLine = ""; 
                        }
                        sign.setLine(i, currentLine);
                    }
                }
            }
        }
        
        sign.update();
    }
}