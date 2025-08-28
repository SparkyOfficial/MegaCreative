package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import com.megacreative.interfaces.IPlayerManager;
import lombok.Data;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages block grouping functionality for organizing code blocks into collapsible units
 */
@Log
public class BlockGroupManager {
    
    private final Plugin plugin;
    private final IPlayerManager playerManager;
    
    // Active block groups by world
    private final Map<String, Map<UUID, BlockGroup>> worldGroups = new ConcurrentHashMap<>();
    
    // Player selection state for creating groups
    private final Map<UUID, GroupSelectionState> playerSelections = new ConcurrentHashMap<>();
    
    public BlockGroupManager(Plugin plugin, IPlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }
    
    /**
     * Starts group selection mode for a player
     */
    public void startGroupSelection(Player player) {
        GroupSelectionState state = new GroupSelectionState();
        playerSelections.put(player.getUniqueId(), state);
        
        player.sendMessage("§a✓ Group selection mode activated!");
        player.sendMessage("§7Right-click blocks to select them for grouping");
        
        log.fine("Player " + player.getName() + " started group selection mode");
    }
    
    /**
     * Adds a block to the current selection
     */
    public void selectBlock(Player player, Location blockLocation, CodeBlock codeBlock) {
        GroupSelectionState state = playerSelections.get(player.getUniqueId());
        if (state == null) {
            player.sendMessage("§cYou are not in group selection mode!");
            return;
        }
        
        if (state.getSelectedBlocks().containsKey(blockLocation)) {
            // Deselect block
            state.getSelectedBlocks().remove(blockLocation);
            player.sendMessage("§e- Deselected block: " + codeBlock.getAction());
        } else {
            // Select block
            state.getSelectedBlocks().put(blockLocation, codeBlock);
            player.sendMessage("§a+ Selected block: " + codeBlock.getAction());
        }
        
        player.sendMessage("§7Selected: " + state.getSelectedBlocks().size() + " blocks");
    }
    
    /**
     * Cancels group selection for a player
     */
    public void cancelSelection(Player player) {
        playerSelections.remove(player.getUniqueId());
        player.sendMessage("§e✖ Group selection cancelled");
    }
    
    /**
     * Checks if player is in selection mode
     */
    public boolean isInSelectionMode(Player player) {
        return playerSelections.containsKey(player.getUniqueId());
    }
    
    /**
     * Gets selection state for a player
     */
    public GroupSelectionState getSelectionState(Player player) {
        return playerSelections.get(player.getUniqueId());
    }
    
    /**
     * Collapses a group by name
     */
    public void collapseGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        group.setCollapsed(true);
        player.sendMessage("§a✓ Collapsed group: " + groupName);
    }
    
    /**
     * Expands a group by name
     */
    public void expandGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        group.setCollapsed(false);
        player.sendMessage("§a✓ Expanded group: " + groupName);
    }
    
    /**
     * Deletes a group by name
     */
    public void deleteGroup(Player player, String groupName) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        BlockGroup group = findGroupByName(player, groupName);
        if (group != null) {
            groups.remove(group.getId());
            player.sendMessage("§a✓ Deleted group: " + groupName);
        } else {
            player.sendMessage("§cGroup not found: " + groupName);
        }
    }
    
    /**
     * Lists all groups for a player
     */
    public void listGroups(Player player) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null || groups.isEmpty()) {
            player.sendMessage("§eNo groups found in this world");
            return;
        }
        
        player.sendMessage("§a§lYour Groups:");
        for (BlockGroup group : groups.values()) {
            if (group.getOwner().equals(player.getUniqueId())) {
                String status = group.isCollapsed() ? "§c[Collapsed]" : "§a[Expanded]";
                player.sendMessage("§7- " + status + " §f" + group.getName() + " §7(" + group.getBlocks().size() + " blocks)");
            }
        }
    }
    
    /**
     * Handles click on collapsed group display
     */
    public void handleCollapsedGroupClick(Player player, Location location) {
        // Find group at this location and expand it
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null) return;
        
        for (BlockGroup group : groups.values()) {
            if (group.getOwner().equals(player.getUniqueId()) && group.isCollapsed()) {
                // Simple location-based matching - can be enhanced
                expandGroup(player, group.getName());
                break;
            }
        }
    }
    
    /**
     * Finds a group by name for a specific player
     */
    private BlockGroup findGroupByName(Player player, String groupName) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null) return null;
        
        return groups.values().stream()
            .filter(group -> group.getOwner().equals(player.getUniqueId()))
            .filter(group -> group.getName().equalsIgnoreCase(groupName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Creates a group from the current selection
     */
    public void createGroupFromSelection(Player player, String groupName) {
        GroupSelectionState state = playerSelections.get(player.getUniqueId());
        if (state == null) {
            player.sendMessage("§cYou are not in group selection mode!");
            return;
        }
        
        if (state.getSelectedBlocks().isEmpty()) {
            player.sendMessage("§cNo blocks selected for grouping!");
            return;
        }
        
        // Create the group
        BlockGroup group = new BlockGroup(
            UUID.randomUUID(),
            groupName != null ? groupName : "Group " + (getPlayerGroupCount(player) + 1),
            player.getUniqueId(),
            new HashMap<>(state.getSelectedBlocks()),
            calculateGroupBounds(state.getSelectedBlocks().keySet())
        );
        
        // Add to world groups
        String worldName = player.getWorld().getName();
        worldGroups.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                  .put(group.getId(), group);
        
        // Clear selection
        clearSelection(player);
        
        player.sendMessage("§a✓ Created group: " + group.getName() + " (" + group.getBlocks().size() + " blocks)");
        
        log.info("Player " + player.getName() + " created group: " + group.getName() + 
                " with " + group.getBlocks().size() + " blocks");
    }
    
    /**
     * Cleans up resources when plugin disables
     */
    public void cleanup() {
        worldGroups.clear();
        playerSelections.clear();
    }
    
    // Private helper methods
    
    private void clearSelection(Player player) {
        playerSelections.remove(player.getUniqueId());
    }
    
    private int getPlayerGroupCount(Player player) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null) return 0;
        
        return (int) groups.values().stream()
            .filter(group -> group.getOwner().equals(player.getUniqueId()))
            .count();
    }
    
    private GroupBounds calculateGroupBounds(Set<Location> locations) {
        if (locations.isEmpty()) {
            return new GroupBounds(0, 0, 0, 0, 0, 0);
        }
        
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        
        for (Location loc : locations) {
            minX = Math.min(minX, loc.getBlockX());
            minY = Math.min(minY, loc.getBlockY());
            minZ = Math.min(minZ, loc.getBlockZ());
            maxX = Math.max(maxX, loc.getBlockX());
            maxY = Math.max(maxY, loc.getBlockY());
            maxZ = Math.max(maxZ, loc.getBlockZ());
        }
        
        return new GroupBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    // Data classes
    
    @Data
    public static class GroupSelectionState {
        private final Map<Location, CodeBlock> selectedBlocks = new HashMap<>();
    }
    
    @Data
    public static class GroupBounds {
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        
        public int getWidth() { return maxX - minX + 1; }
        public int getHeight() { return maxY - minY + 1; }
        public int getDepth() { return maxZ - minZ + 1; }
    }
    
    @Data
    public static class BlockGroup {
        private final UUID id;
        private final String name;
        private final UUID owner;
        private final Map<Location, CodeBlock> blocks;
        private final GroupBounds bounds;
        private boolean collapsed = false;
        
        public BlockGroup(UUID id, String name, UUID owner, Map<Location, CodeBlock> blocks, GroupBounds bounds) {
            this.id = id;
            this.name = name;
            this.owner = owner;
            this.blocks = blocks;
            this.bounds = bounds;
            this.collapsed = false;
        }
    }
}