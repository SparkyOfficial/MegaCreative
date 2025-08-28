package com.megacreative.listeners;

import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for block group functionality interactions
 */
public class BlockGroupListener implements Listener {
    
    private final ServiceRegistry serviceRegistry;
    
    public BlockGroupListener(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if player is holding the group selection tool
        if (item.getType() != Material.GOLDEN_SWORD) return;
        if (!item.hasItemMeta()) return;
        if (!"§6Block Group Selector".equals(item.getItemMeta().getDisplayName())) return;
        
        BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Right-click on block to select it
            event.setCancelled(true);
            
            if (event.getClickedBlock() != null) {
                // Check if this is a code block (integrate with BlockConfigService)
                // For now, we'll assume any block can be a code block
                // This would integrate with AutoConnectionManager to get the CodeBlock
                
                // Placeholder implementation
                groupManager.selectBlock(player, event.getClickedBlock().getLocation(), null);
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            // Left-click air to create group from selection
            event.setCancelled(true);
            groupManager.createGroupFromSelection(player, null);
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        // Check if player dropped the group selection tool (cancel selection)
        if (item.getType() == Material.GOLDEN_SWORD && item.hasItemMeta()) {
            if ("§6Block Group Selector".equals(item.getItemMeta().getDisplayName())) {
                event.setCancelled(true);
                
                BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
                groupManager.cancelSelection(player);
            }
        }
    }
}