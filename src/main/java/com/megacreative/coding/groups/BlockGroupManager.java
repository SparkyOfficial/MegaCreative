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