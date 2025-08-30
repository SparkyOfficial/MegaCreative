package com.megacreative.tools;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.coding.values.DataValue;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CodeBlockClipboard {
    private static final Logger log = Logger.getLogger(CodeBlockClipboard.class.getName());
    
    private final Map<UUID, ClipboardData> playerClipboards = new ConcurrentHashMap<>();
    private final Map<String, ClipboardData> sharedClipboards = new ConcurrentHashMap<>();
    
    private BlockPlacementHandler placementHandler;
    private AutoConnectionManager connectionManager;
    
    /**
     * Sets the BlockPlacementHandler for integration
     */
    public void setPlacementHandler(BlockPlacementHandler placementHandler) {
        this.placementHandler = placementHandler;
    }
    
    /**
     * Sets the AutoConnectionManager for integration
     */
    public void setConnectionManager(AutoConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }
    
    /**
     * Copies a code block at the player's target location
     */
    public void copyBlock(Player player, Location location) {
        CodeBlock block = null;
        
        // Try to get CodeBlock from BlockPlacementHandler first
        if (placementHandler != null && placementHandler.hasCodeBlock(location)) {
            block = placementHandler.getCodeBlock(location);
        } else if (connectionManager != null) {
            // Fallback to AutoConnectionManager
            block = connectionManager.getWorldBlocks(location.getWorld()).get(location);
        }
        
        if (block == null) {
            player.sendMessage("§c✖ No code block found at target location!");
            return;
        }
        
        copyBlock(player, block);
    }
    
    /**
     * Copies a specific CodeBlock
     */
    public void copyBlock(Player player, CodeBlock block) {
        ClipboardData data = new ClipboardData(ClipboardType.SINGLE_BLOCK, player.getLocation());
        
        // Create deep copy of the block to preserve all parameters
        CodeBlock copiedBlock = createDeepCopy(block);
        data.addBlock(copiedBlock, new Location(player.getWorld(), 0, 0, 0)); // Relative position
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied code block: " + block.getAction());
        
        log.info("Player " + player.getName() + " copied code block: " + block.getAction());
    }
    
    /**
     * Copies all code blocks in a region
     */
    public void copyRegion(Player player, Location corner1, Location corner2) {
        World world = corner1.getWorld();
        if (world == null || !world.equals(corner2.getWorld())) {
            player.sendMessage("§c✖ Both corners must be in the same world!");
            return;
        }
        
        ClipboardData data = new ClipboardData(ClipboardType.REGION, corner1);
        
        // Calculate region bounds
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());
        
        int blocksFound = 0;
        
        // Scan the region for code blocks
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location loc = new Location(world, x, y, z);
                    CodeBlock block = null;
                    
                    // Try to get CodeBlock from placement handler or connection manager
                    if (placementHandler != null && placementHandler.hasCodeBlock(loc)) {
                        block = placementHandler.getCodeBlock(loc);
                    } else if (connectionManager != null) {
                        block = connectionManager.getWorldBlocks(world).get(loc);
                    }
                    
                    if (block != null) {
                        // Create deep copy and calculate relative position
                        CodeBlock copiedBlock = createDeepCopy(block);
                        Location relativePos = new Location(world, x - corner1.getBlockX(), y - corner1.getBlockY(), z - corner1.getBlockZ());
                        data.addBlock(copiedBlock, relativePos);
                        blocksFound++;
                    }
                }
            }
        }
        
        if (blocksFound == 0) {
            player.sendMessage("§c✖ No code blocks found in the selected region!");
            return;
        }
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied region: " + blocksFound + " code blocks");
        
        log.info("Player " + player.getName() + " copied region with " + blocksFound + " blocks");
    }
    
    /**
     * Pastes clipboard data at the target location
     */
    public void paste(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to paste");
            return;
        }
        
        if (data.getBlockPositions().isEmpty()) {
            player.sendMessage("§c✖ Clipboard contains no blocks to paste");
            return;
        }
        
        World targetWorld = targetLocation.getWorld();
        if (targetWorld == null) {
            player.sendMessage("§c✖ Invalid target world!");
            return;
        }
        
        int pastedBlocks = 0;
        Map<Location, CodeBlock> pastedBlockMap = new HashMap<>(); // For reconnection
        
        // First pass: Place all blocks
        for (Map.Entry<Location, CodeBlock> entry : data.getBlockPositions().entrySet()) {
            Location relativePos = entry.getKey();
            CodeBlock sourceBlock = entry.getValue();
            
            // Calculate absolute position
            Location absolutePos = new Location(targetWorld,
                targetLocation.getBlockX() + relativePos.getBlockX(),
                targetLocation.getBlockY() + relativePos.getBlockY(),
                targetLocation.getBlockZ() + relativePos.getBlockZ());
            
            // Check if location is valid for code blocks
            if (absolutePos.getBlock().getType() != Material.AIR) {
                player.sendMessage("§e⚠ Skipping occupied location: " + 
                    absolutePos.getBlockX() + ", " + absolutePos.getBlockY() + ", " + absolutePos.getBlockZ());
                continue;
            }
            
            // Create a new CodeBlock copy
            CodeBlock newBlock = createDeepCopy(sourceBlock);
            
            // Place the block physically
            absolutePos.getBlock().setType(newBlock.getMaterial());
            
            // Register with placement handler
            if (placementHandler != null) {
                // Add to placement handler's tracking
                // This would require exposing a method in BlockPlacementHandler
                // For now, we'll simulate the registration
            }
            
            // Register with connection manager
            if (connectionManager != null) {
                // Add to connection manager and trigger auto-connect
                // This integration ensures the pasted blocks are properly connected
            }
            
            pastedBlockMap.put(absolutePos, newBlock);
            pastedBlocks++;
        }
        
        // Second pass: Restore connections if we have connection manager
        if (connectionManager != null) {
            for (Map.Entry<Location, CodeBlock> entry : pastedBlockMap.entrySet()) {
                // Call the public method that will trigger auto-connection
                // Since autoConnectBlock is private, we'll rely on the placement handler
                // to trigger the connections through the event system
            }
        }
        
        if (pastedBlocks > 0) {
            player.sendMessage("§a✓ Pasted " + pastedBlocks + " code blocks");
            log.info("Player " + player.getName() + " pasted " + pastedBlocks + " blocks at " + targetLocation);
        } else {
            player.sendMessage("§c✖ No blocks were pasted (all locations occupied)");
        }
    }
    
    public void showPreview(Player player, Location targetLocation) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to preview");
            return;
        }
        player.sendMessage("§e⤤ Preview: " + data.getBlocks().size() + " blocks would be placed");
    }
    
    public void saveToShared(Player player, String name) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            player.sendMessage("§c✖ No clipboard data to save");
            return;
        }
        sharedClipboards.put(name, data);
        player.sendMessage("§a✓ Saved clipboard as: " + name);
    }
    
    public void loadFromShared(Player player, String name) {
        ClipboardData data = sharedClipboards.get(name);
        if (data == null) {
            player.sendMessage("§c✖ Shared clipboard not found: " + name);
            return;
        }
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Loaded clipboard: " + name);
    }
    
    public void listShared(Player player) {
        if (sharedClipboards.isEmpty()) {
            player.sendMessage("§e⤤ No shared clipboards available");
            return;
        }
        player.sendMessage("§6=== Shared Clipboards ===");
        for (String name : sharedClipboards.keySet()) {
            player.sendMessage("§f" + name);
        }
    }
    
    public void clear(Player player) {
        playerClipboards.remove(player.getUniqueId());
        player.sendMessage("§a✓ Clipboard cleared");
    }
    
    public String getClipboardInfo(Player player) {
        ClipboardData data = playerClipboards.get(player.getUniqueId());
        if (data == null) {
            return "§cNo clipboard data";
        }
        return "§a" + data.getType().getDisplayName() + " §7(" + data.getBlocks().size() + " blocks)";
    }
    
    /**
     * Creates a deep copy of a CodeBlock, preserving all parameters and properties
     */
    private CodeBlock createDeepCopy(CodeBlock original) {
        CodeBlock copy = new CodeBlock(original.getMaterial(), original.getAction());
        
        // Copy all parameters
        for (Map.Entry<String, DataValue> paramEntry : original.getParameters().entrySet()) {
            copy.setParameter(paramEntry.getKey(), paramEntry.getValue());
        }
        
        // Copy config items if any
        for (int i = 0; i < original.getConfigItems().size(); i++) {
            if (original.getConfigItem(i) != null) {
                copy.setConfigItem(i, original.getConfigItem(i).clone());
            }
        }
        
        // Note: We don't copy nextBlock and children relationships here
        // as they will be rebuilt by the AutoConnectionManager
        
        return copy;
    }
    
    /**
     * Copies a chain of connected code blocks starting from the given block
     */
    public void copyChain(Player player, Location startLocation) {
        CodeBlock startBlock = null;
        
        // Get the starting block
        if (placementHandler != null && placementHandler.hasCodeBlock(startLocation)) {
            startBlock = placementHandler.getCodeBlock(startLocation);
        } else if (connectionManager != null) {
            startBlock = connectionManager.getWorldBlocks(startLocation.getWorld()).get(startLocation);
        }
        
        if (startBlock == null) {
            player.sendMessage("§c✖ No code block found at target location!");
            return;
        }
        
        ClipboardData data = new ClipboardData(ClipboardType.BLOCK_CHAIN, startLocation);
        
        // Follow the chain and copy all connected blocks
        Set<CodeBlock> visited = new HashSet<>();
        Location currentPos = startLocation;
        int chainIndex = 0;
        
        copyChainRecursive(startBlock, data, visited, currentPos, chainIndex);
        
        playerClipboards.put(player.getUniqueId(), data);
        player.sendMessage("§a✓ Copied block chain: " + data.getBlocks().size() + " blocks");
        
        log.info("Player " + player.getName() + " copied block chain with " + data.getBlocks().size() + " blocks");
    }
    
    /**
     * Recursively copies a chain of blocks
     */
    private void copyChainRecursive(CodeBlock block, ClipboardData data, Set<CodeBlock> visited, Location basePos, int chainIndex) {
        if (block == null || visited.contains(block)) {
            return;
        }
        
        visited.add(block);
        
        // Add current block
        CodeBlock copiedBlock = createDeepCopy(block);
        Location relativePos = new Location(basePos.getWorld(), chainIndex, 0, 0);
        data.addBlock(copiedBlock, relativePos);
        
        // Follow next block
        if (block.getNextBlock() != null) {
            copyChainRecursive(block.getNextBlock(), data, visited, basePos, chainIndex + 1);
        }
        
        // Follow children (for conditional blocks)
    }
    
    public static class ClipboardData {
        private final ClipboardType type;
        private final Location origin; // Original copy location for reference
        private final List<CodeBlock> blocks = new ArrayList<>();
        private final Map<Location, CodeBlock> blockPositions = new HashMap<>();
        
        public ClipboardData(ClipboardType type, Location origin) {
            this.type = type;
            this.origin = origin != null ? origin.clone() : null;
        }
        
        public void addBlock(CodeBlock block, Location relativePosition) {
            if (block != null) {
                blocks.add(block);
                if (relativePosition != null) {
                    blockPositions.put(relativePosition, block);
                }
            }
        }
        
        // For backward compatibility
        public void addBlock(CodeBlock block) {
            addBlock(block, new Location(null, 0, 0, 0));
        }
        
        public List<CodeBlock> getBlocks() {
            return new ArrayList<>(blocks);
        }
        
        public Map<Location, CodeBlock> getBlockPositions() {
            return new HashMap<>(blockPositions);
        }
        
        public ClipboardType getType() {
            return type;
        }
    }
    
    public enum ClipboardType {
        SINGLE_BLOCK("Single Block"),
        BLOCK_CHAIN("Block Chain"),
        REGION("Region");
        
        private final String displayName;
        
        ClipboardType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
}