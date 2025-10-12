package com.megacreative.listeners;

import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodeBlock;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        
        if (item.getType() != Material.GOLDEN_SWORD) return;
        if (!item.hasItemMeta()) return;
        if (!"§6Block Group Selector".equals(item.getItemMeta().getDisplayName())) return;
        
        BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
        BlockPlacementHandler placementHandler = serviceRegistry.getBlockPlacementHandler();
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            
            event.setCancelled(true);
            
            if (event.getClickedBlock() != null) {
                org.bukkit.Location blockLocation = event.getClickedBlock().getLocation();
                CodeBlock codeBlock = null;
                
                
                if (placementHandler != null && placementHandler.hasCodeBlock(blockLocation)) {
                    codeBlock = placementHandler.getCodeBlock(blockLocation);
                }
                
                if (codeBlock != null) {
                    groupManager.selectBlock(player, blockLocation, codeBlock);
                } else {
                    player.sendMessage("§cThis is not a code block! Only code blocks can be grouped.");
                }
            }
        } else if (event.getAction() == Action.LEFT_CLICK_AIR) {
            
            event.setCancelled(true);
            groupManager.createGroupFromSelection(player, null);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            
            event.setCancelled(true);
            showSelectionInfo(player, groupManager);
        }
    }
    
    /**
     * Handles clicking on collapsed groups to expand them
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onCollapsedGroupClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        
        if (item.getType() == Material.GOLDEN_SWORD && item.hasItemMeta() &&
            "§6Block Group Selector".equals(item.getItemMeta().getDisplayName())) {
            return;
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            
            if (event.getClickedBlock().getType() == Material.ENDER_CHEST) {
                BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
                groupManager.handleCollapsedGroupClick(player, event.getClickedBlock().getLocation());
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();
        
        
        if (item.getType() == Material.GOLDEN_SWORD && item.hasItemMeta()) {
            if ("§6Block Group Selector".equals(item.getItemMeta().getDisplayName())) {
                event.setCancelled(true);
                
                BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
                groupManager.cancelSelection(player);
            }
        }
    }
    
    /**
     * Shows current selection information to the player
     */
    private void showSelectionInfo(Player player, BlockGroupManager groupManager) {
        if (groupManager.isInSelectionMode(player)) {
            BlockGroupManager.GroupSelectionState state = groupManager.getSelectionState(player);
            if (state != null) {
                int selectedCount = state.getSelectedBlocks().size();
                player.sendMessage("§6=== Block Group Selection ===");
                player.sendMessage("§f• Selected blocks: §a" + selectedCount);
                player.sendMessage("§f• Right-click blocks to select/deselect");
                player.sendMessage("§f• Left-click air to create group (need 2+ blocks)");
                player.sendMessage("§f• Drop tool to cancel selection");
                
                if (selectedCount >= 2) {
                    player.sendMessage("§a✓ Ready to create group!");
                } else if (selectedCount == 1) {
                    player.sendMessage("§eSelect at least 1 more block to create a group");
                } else {
                    player.sendMessage("§eSelect some blocks to get started");
                }
            }
        } else {
            player.sendMessage("§cYou are not in group selection mode!");
            player.sendMessage("§7Use /group select to start selecting blocks");
        }
    }
}