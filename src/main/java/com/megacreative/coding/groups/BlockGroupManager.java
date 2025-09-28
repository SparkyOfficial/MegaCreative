package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import com.megacreative.interfaces.IPlayerManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages block grouping functionality for organizing code blocks into collapsible units
 */
public class BlockGroupManager {
    private static final Logger log = Logger.getLogger(BlockGroupManager.class.getName());
    
    private final Plugin plugin;
    private final IPlayerManager playerManager;
    
    // Active block groups by world
    private final Map<String, Map<UUID, BlockGroup>> worldGroups = new ConcurrentHashMap<>();
    
    // Advanced block groups
    private final Map<String, Map<UUID, AdvancedBlockGroup>> worldAdvancedGroups = new ConcurrentHashMap<>();
    
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
        
        log.fine("Player " + player.getName() + " started group selection mode");
    }
    
    /**
     * Adds a block to the current selection
     */
    public void selectBlock(Player player, Location blockLocation, CodeBlock codeBlock) {
        GroupSelectionState state = playerSelections.get(player.getUniqueId());
        if (state == null) {
            return;
        }
        
        if (state.getSelectedBlocks().containsKey(blockLocation)) {
            // Deselect block
            state.getSelectedBlocks().remove(blockLocation);
        } else {
            // Select block
            state.getSelectedBlocks().put(blockLocation, codeBlock);
        }
    }
    
    /**
     * Cancels group selection for a player
     */
    public void cancelSelection(Player player) {
        playerSelections.remove(player.getUniqueId());
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
            return;
        }
        
        group.setCollapsed(true);
    }
    
    /**
     * Expands a group by name
     */
    public void expandGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            return;
        }
        
        group.setCollapsed(false);
    }
    
    /**
     * Deletes a group by name
     */
    public void deleteGroup(Player player, String groupName) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null) {
            return;
        }
        
        BlockGroup group = findGroupByName(player, groupName);
        if (group != null) {
            groups.remove(group.getId());
        }
    }
    
    /**
     * Lists all groups for a player
     */
    public void listGroups(Player player) {
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups == null || groups.isEmpty()) {
            return;
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
        if (state == null || state.getSelectedBlocks().isEmpty()) {
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
        
        log.info("Player " + player.getName() + " created group: " + group.getName() + 
                " with " + group.getBlocks().size() + " blocks");
    }
    
    /**
     * Creates an advanced group from the current selection
     */
    public void createAdvancedGroupFromSelection(Player player, String groupName, 
                                               AdvancedBlockGroup.ExecutionMode executionMode) {
        GroupSelectionState state = playerSelections.get(player.getUniqueId());
        if (state == null || state.getSelectedBlocks().isEmpty()) {
            return;
        }
        
        // Create the advanced group
        AdvancedBlockGroup group = new AdvancedBlockGroup(
            UUID.randomUUID(),
            groupName != null ? groupName : "Group " + (getPlayerGroupCount(player) + 1),
            player.getUniqueId(),
            new HashMap<>(state.getSelectedBlocks()),
            calculateGroupBounds(state.getSelectedBlocks().keySet())
        );
        
        group.setExecutionMode(executionMode != null ? executionMode : AdvancedBlockGroup.ExecutionMode.SEQUENTIAL);
        
        // Add to world advanced groups
        String worldName = player.getWorld().getName();
        worldAdvancedGroups.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                          .put(group.getId(), group);
        
        // Clear selection
        clearSelection(player);
        
        log.info("Player " + player.getName() + " created advanced group: " + group.getName() + 
                " with " + group.getBlocks().size() + " blocks");
    }
    
    /**
     * Converts a regular group to an advanced group
     */
    public AdvancedBlockGroup convertToAdvancedGroup(Player player, String groupName) {
        BlockGroup regularGroup = findGroupByName(player, groupName);
        if (regularGroup == null) {
            return null;
        }
        
        // Create advanced group from regular group
        AdvancedBlockGroup advancedGroup = new AdvancedBlockGroup(
            regularGroup.getId(),
            regularGroup.getName(),
            regularGroup.getOwner(),
            new HashMap<>(regularGroup.getBlocks()),
            regularGroup.getBounds()
        );
        
        // Copy properties
        advancedGroup.setCollapsed(regularGroup.isCollapsed());
        
        // Replace in world groups
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups != null) {
            groups.remove(regularGroup.getId());
        }
        
        // Add to advanced groups
        worldAdvancedGroups.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>())
                          .put(advancedGroup.getId(), advancedGroup);
        
        return advancedGroup;
    }
    
    /**
     * Locks/unlocks a group
     */
    public void toggleGroupLock(Player player, String groupName) {
        AdvancedBlockGroup group = findAdvancedGroupByName(player, groupName);
        if (group == null) {
            return;
        }
        
        group.setLocked(!group.isLocked());
    }
    
    /**
     * Sets execution limit for a group
     */
    public void setGroupExecutionLimit(Player player, String groupName, int limit) {
        AdvancedBlockGroup group = findAdvancedGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cAdvanced group not found: " + groupName);
            return;
        }
        
        group.setExecutionLimit(limit);
        player.sendMessage("§a✓ Set execution limit for " + groupName + " to " + 
                          (limit > 0 ? limit : "unlimited"));
    }
    
    /**
     * Adds a tag to a group
     */
    public void addGroupTag(Player player, String groupName, String tag) {
        AdvancedBlockGroup group = findAdvancedGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cAdvanced group not found: " + groupName);
            return;
        }
        
        group.addTag(tag);
        player.sendMessage("§a✓ Added tag '" + tag + "' to group " + groupName);
    }
    
    /**
     * Finds an advanced group by name for a specific player
     */
    private AdvancedBlockGroup findAdvancedGroupByName(Player player, String groupName) {
        String worldName = player.getWorld().getName();
        Map<UUID, AdvancedBlockGroup> groups = worldAdvancedGroups.get(worldName);
        if (groups == null) return null;
        
        return groups.values().stream()
            .filter(group -> group.getOwner().equals(player.getUniqueId()))
            .filter(group -> group.getName().equalsIgnoreCase(groupName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Gets all advanced groups for a player
     */
    public List<AdvancedBlockGroup> getPlayerAdvancedGroups(Player player) {
        String worldName = player.getWorld().getName();
        Map<UUID, AdvancedBlockGroup> groups = worldAdvancedGroups.get(worldName);
        if (groups == null) return new ArrayList<>();
        
        return groups.values().stream()
            .filter(group -> group.getOwner().equals(player.getUniqueId()))
            .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }
    
    /**
     * Gets advanced groups by tag
     */
    public List<AdvancedBlockGroup> getGroupsByTag(Player player, String tag) {
        return getPlayerAdvancedGroups(player).stream()
            .filter(group -> group.hasTag(tag))
            .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
    }
    
    /**
     * Cleans up resources when plugin disables
     */
    public void cleanup() {
        worldGroups.clear();
        worldAdvancedGroups.clear();
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
    
    public static class GroupSelectionState {
        private final Map<Location, CodeBlock> selectedBlocks = new HashMap<>();
        
        public GroupSelectionState() {}
        
        public Map<Location, CodeBlock> getSelectedBlocks() {
            return new HashMap<>(selectedBlocks);
        }
        
        public void addBlock(final Location location, final CodeBlock block) {
            selectedBlocks.put(location, block);
        }
        
        public void clear() {
            selectedBlocks.clear();
        }
        
        public int size() {
            return selectedBlocks.size();
        }
        
        public boolean isEmpty() {
            return selectedBlocks.isEmpty();
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final GroupSelectionState that = (GroupSelectionState) o;
            return Objects.equals(selectedBlocks, that.selectedBlocks);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(selectedBlocks);
        }
        
        @Override
        public String toString() {
            return "GroupSelectionState{" +
                   "selectedBlocks=" + selectedBlocks.keySet() +
                   '}';
        }
    }
    
    public static class GroupBounds {
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        
        public GroupBounds(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
        
        public int getMinX() { return minX; }
        public int getMinY() { return minY; }
        public int getMinZ() { return minZ; }
        public int getMaxX() { return maxX; }
        public int getMaxY() { return maxY; }
        public int getMaxZ() { return maxZ; }
        public int getWidth() { return maxX - minX + 1; }
        public int getHeight() { return maxY - minY + 1; }
        public int getDepth() { return maxZ - minZ + 1; }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final GroupBounds that = (GroupBounds) o;
            return minX == that.minX && 
                   minY == that.minY && 
                   minZ == that.minZ && 
                   maxX == that.maxX && 
                   maxY == that.maxY && 
                   maxZ == that.maxZ;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
        }
        
        @Override
        public String toString() {
            return String.format(
                "GroupBounds{minX=%d, minY=%d, minZ=%d, maxX=%d, maxY=%d, maxZ=%d, width=%d, height=%d, depth=%d}",
                minX, minY, minZ, maxX, maxY, maxZ, getWidth(), getHeight(), getDepth()
            );
        }
    }
}