package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import com.megacreative.interfaces.IPlayerManager;
import lombok.Data;
import lombok.extern.java.Log;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages block grouping functionality for organizing code blocks into collapsible units
 * Allows players to group related code blocks together for better visual organization
 */
@Log
public class BlockGroupManager {
    
    private final Plugin plugin;
    private final IPlayerManager playerManager;
    
    // Active block groups by world
    private final Map<String, Map<UUID, BlockGroup>> worldGroups = new ConcurrentHashMap<>();
    
    // Player selection state for creating groups
    private final Map<UUID, GroupSelectionState> playerSelections = new ConcurrentHashMap<>();
    
    // Collapsed groups visual representations
    private final Map<UUID, CollapsedGroupDisplay> collapsedDisplays = new ConcurrentHashMap<>();
    
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
        player.sendMessage("§7Left-click air to finish selection and create group");
        player.sendMessage("§7Type /code group cancel to cancel");
        
        // Give selection tool
        ItemStack tool = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta meta = tool.getItemMeta();
        meta.setDisplayName("§6Block Group Selector");
        meta.setLore(Arrays.asList(
            "§7Right-click: Select block",
            "§7Left-click air: Create group",
            "§7Drop: Cancel selection"
        ));
        tool.setItemMeta(meta);
        
        player.getInventory().addItem(tool);
        
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
            
            // Remove selection particles
            removeSelectionParticles(player, blockLocation);
        } else {
            // Select block
            state.getSelectedBlocks().put(blockLocation, codeBlock);
            player.sendMessage("§a+ Selected block: " + codeBlock.getAction());
            
            // Show selection particles
            showSelectionParticles(player, blockLocation);
        }
        
        player.sendMessage("§7Selected: " + state.getSelectedBlocks().size() + " blocks");
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
        
        if (state.getSelectedBlocks().size() < 2) {
            player.sendMessage("§cYou need at least 2 blocks to create a group!");
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
        player.sendMessage("§7Use /code group collapse " + group.getName() + " to collapse it");
        
        log.info("Player " + player.getName() + " created group: " + group.getName() + 
                " with " + group.getBlocks().size() + " blocks");
    }
    
    /**
     * Collapses a group, hiding the blocks and showing a representative display
     */
    public void collapseGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        if (group.isCollapsed()) {
            player.sendMessage("§cGroup is already collapsed!");
            return;
        }
        
        // Hide all blocks in the group
        for (Location location : group.getBlocks().keySet()) {
            location.getBlock().setType(Material.AIR);
        }
        
        // Create collapsed display
        Location centerLocation = calculateGroupCenter(group.getBounds());
        CollapsedGroupDisplay display = createCollapsedDisplay(group, centerLocation);
        collapsedDisplays.put(group.getId(), display);
        
        // Mark as collapsed
        group.setCollapsed(true);
        
        player.sendMessage("§a✓ Collapsed group: " + group.getName());
        player.sendMessage("§7Right-click the group display to expand it");
        
        log.fine("Collapsed group: " + group.getName() + " for player " + player.getName());
    }
    
    /**
     * Expands a collapsed group, restoring all blocks
     */
    public void expandGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        if (!group.isCollapsed()) {
            player.sendMessage("§cGroup is not collapsed!");
            return;
        }
        
        // Restore all blocks in the group
        for (Map.Entry<Location, CodeBlock> entry : group.getBlocks().entrySet()) {
            Location location = entry.getKey();
            CodeBlock codeBlock = entry.getValue();
            location.getBlock().setType(codeBlock.getMaterial());
        }
        
        // Remove collapsed display
        CollapsedGroupDisplay display = collapsedDisplays.remove(group.getId());
        if (display != null) {
            display.remove();
        }
        
        // Mark as expanded
        group.setCollapsed(false);
        
        player.sendMessage("§a✓ Expanded group: " + group.getName());
        
        log.fine("Expanded group: " + group.getName() + " for player " + player.getName());
    }
    
    /**
     * Deletes a group, removing all block associations
     */
    public void deleteGroup(Player player, String groupName) {
        BlockGroup group = findGroupByName(player, groupName);
        if (group == null) {
            player.sendMessage("§cGroup not found: " + groupName);
            return;
        }
        
        // If collapsed, expand first
        if (group.isCollapsed()) {
            expandGroup(player, groupName);
        }
        
        // Remove from world groups
        String worldName = player.getWorld().getName();
        Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
        if (groups != null) {
            groups.remove(group.getId());
        }
        
        player.sendMessage("§a✓ Deleted group: " + group.getName());
        
        log.info("Player " + player.getName() + " deleted group: " + group.getName());
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
        
        List<BlockGroup> playerGroups = groups.values().stream()
            .filter(group -> group.getOwner().equals(player.getUniqueId()))
            .sorted(Comparator.comparing(BlockGroup::getName))
            .toList();
        
        if (playerGroups.isEmpty()) {
            player.sendMessage("§eYou have no groups in this world");
            return;
        }
        
        player.sendMessage("§6=== Your Block Groups ===");
        for (BlockGroup group : playerGroups) {
            String status = group.isCollapsed() ? "§8[Collapsed]" : "§a[Expanded]";
            player.sendMessage(String.format("§f%s %s §7(%d blocks)", 
                group.getName(), status, group.getBlocks().size()));
        }
    }
    
    /**
     * Cancels the current group selection
     */
    public void cancelSelection(Player player) {
        clearSelection(player);
        player.sendMessage("§e✓ Group selection cancelled");
    }
    
    /**
     * Handles clicking on a collapsed group display
     */
    public void handleCollapsedGroupClick(Player player, Location clickLocation) {
        for (Map.Entry<UUID, CollapsedGroupDisplay> entry : collapsedDisplays.entrySet()) {
            CollapsedGroupDisplay display = entry.getValue();
            if (display.getLocation().distance(clickLocation) < 2.0) {
                // Find the group
                String worldName = player.getWorld().getName();
                Map<UUID, BlockGroup> groups = worldGroups.get(worldName);
                if (groups != null) {
                    BlockGroup group = groups.get(entry.getKey());
                    if (group != null && group.getOwner().equals(player.getUniqueId())) {
                        expandGroup(player, group.getName());
                        return;
                    }
                }
            }
        }
    }
    
    /**
     * Cleans up all groups and displays for a world
     */
    public void cleanupWorld(String worldName) {
        Map<UUID, BlockGroup> groups = worldGroups.remove(worldName);
        if (groups != null) {
            for (BlockGroup group : groups.values()) {
                CollapsedGroupDisplay display = collapsedDisplays.remove(group.getId());
                if (display != null) {
                    display.remove();
                }
            }
        }
    }
    
    /**
     * Cleans up resources when plugin disables
     */
    public void cleanup() {
        for (CollapsedGroupDisplay display : collapsedDisplays.values()) {
            display.remove();
        }
        collapsedDisplays.clear();
        worldGroups.clear();
        playerSelections.clear();
    }
    
    // Private helper methods
    
    private void clearSelection(Player player) {
        GroupSelectionState state = playerSelections.remove(player.getUniqueId());
        if (state != null) {
            // Remove selection particles
            for (Location location : state.getSelectedBlocks().keySet()) {
                removeSelectionParticles(player, location);
            }
        }
        
        // Remove selection tool
        player.getInventory().remove(Material.GOLDEN_SWORD);
    }
    
    private void showSelectionParticles(Player player, Location location) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks++ > 100 || !playerSelections.containsKey(player.getUniqueId())) {
                    cancel();
                    return;
                }
                
                GroupSelectionState state = playerSelections.get(player.getUniqueId());
                if (state == null || !state.getSelectedBlocks().containsKey(location)) {
                    cancel();
                    return;
                }
                
                // Show particles around the block
                for (int i = 0; i < 8; i++) {
                    double angle = (i / 8.0) * 2 * Math.PI;
                    double x = location.getX() + 0.5 + Math.cos(angle) * 0.8;
                    double z = location.getZ() + 0.5 + Math.sin(angle) * 0.8;
                    double y = location.getY() + 1.0;
                    
                    player.spawnParticle(Particle.VILLAGER_HAPPY, x, y, z, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
    }
    
    private void removeSelectionParticles(Player player, Location location) {
        // Particles are handled by the runnable above
    }
    
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
    
    private Location calculateGroupCenter(GroupBounds bounds) {
        return new Location(null,
            (bounds.getMinX() + bounds.getMaxX()) / 2.0,
            (bounds.getMinY() + bounds.getMaxY()) / 2.0 + 1.0, // Slightly above
            (bounds.getMinZ() + bounds.getMaxZ()) / 2.0
        );
    }
    
    private CollapsedGroupDisplay createCollapsedDisplay(BlockGroup group, Location location) {
        // Create hologram
        ArmorStand hologram = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setMarker(true);
        hologram.setCustomNameVisible(true);
        hologram.setCustomName("§6[" + group.getName() + "] §7(" + group.getBlocks().size() + " blocks)");
        
        // Create display block
        location.getBlock().setType(Material.ENDER_CHEST);
        
        CollapsedGroupDisplay display = new CollapsedGroupDisplay(location, hologram);
        
        // Show particles around the display
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!collapsedDisplays.containsValue(display)) {
                    cancel();
                    return;
                }
                
                // Show rotating particles
                for (int i = 0; i < 12; i++) {
                    double angle = (System.currentTimeMillis() / 1000.0 + i / 12.0) * 2 * Math.PI;
                    double x = location.getX() + 0.5 + Math.cos(angle) * 1.2;
                    double z = location.getZ() + 0.5 + Math.sin(angle) * 1.2;
                    double y = location.getY() + 0.5;
                    
                    location.getWorld().spawnParticle(Particle.PORTAL, x, y, z, 1, 0, 0, 0, 0);
                }
            }
        }.runTaskTimer(plugin, 0, 10);
        
        return display;
    }
    
    // Data classes
    
    @Data
    public static class GroupSelectionState {
        private final Map<Location, CodeBlock> selectedBlocks = new HashMap<>();
    }
    
    @Data
    public static class CollapsedGroupDisplay {
        private final Location location;
        private final ArmorStand hologram;
        
        public void remove() {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
            location.getBlock().setType(Material.AIR);
        }
    }
    
    @Data
    public static class GroupBounds {
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        
        public int getWidth() { return maxX - minX + 1; }
        public int getHeight() { return maxY - minY + 1; }
        public int getDepth() { return maxZ - minZ + 1; }
    }
}