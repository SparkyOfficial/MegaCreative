package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlockBrokenEvent;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Manages the structure of code blocks, handling both horizontal connections (nextBlock)
 * and vertical relationships (parent-child) between code blocks.
 * 
 * This class replaces the separate BlockLinker and BlockHierarchyManager classes
 * to provide a unified approach to code block structure management.
 * 
 * Listens to CodeBlockPlacedEvent and CodeBlockBrokenEvent to establish and maintain
 * connections between adjacent blocks.
 */
public class CodeStructureManager implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(CodeStructureManager.class.getName());
    
    private final MegaCreative plugin;
    private final BlockPlacementHandler placementHandler;
    
    public CodeStructureManager(MegaCreative plugin, BlockPlacementHandler placementHandler) {
        this.plugin = plugin;
        this.placementHandler = placementHandler;
    }
    
    /**
     * Handles block placement and establishes both horizontal and vertical connections
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        // Use fine logging instead of info to reduce spam
        LOGGER.fine("Structure manager is processing block at " + location);
        
        // Skip connection logic for bracket blocks
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping connection for bracket block at " + location);
            return;
        }
        
        // Handle horizontal connections (nextBlock relationships)
        handleHorizontalConnections(location, codeBlock);
        
        // Handle vertical connections (parent-child relationships)
        handleVerticalConnections(location, codeBlock);
    }
    
    /**
     * Handles block removal and cleans up connections
     */
    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        LOGGER.fine("Structure manager is cleaning up connections for block at " + location);
        
        // Clean up nextBlock connections
        if (codeBlock.getNextBlock() != null) {
            codeBlock.setNextBlock(null);
        }
        
        // Clean up parent-child connections
        // Note: We don't need to remove this block from its parent's children list
        // as the block is being removed entirely
    }
    
    /**
     * Handles horizontal connections between code blocks (nextBlock relationships)
     * @param location The location of the placed block
     * @param codeBlock The placed code block
     */
    private void handleHorizontalConnections(Location location, CodeBlock codeBlock) {
        // Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = placementHandler.getCodeBlock(prevLocation);
            if (prevBlock != null && !prevBlock.isBracket()) { // Skip brackets
                prevBlock.setNextBlock(codeBlock);
                // Use fine logging instead of info to reduce spam
                LOGGER.fine("Found neighbor at " + prevLocation + ". Linking...");
                LOGGER.fine("Connected horizontal: " + prevLocation + " -> " + location);
            }
        }
        
        // Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = placementHandler.getCodeBlock(nextLocation);
            if (nextBlock != null && !nextBlock.isBracket()) { // Skip brackets
                codeBlock.setNextBlock(nextBlock);
                // Use fine logging instead of info to reduce spam
                LOGGER.fine("Found next block at " + nextLocation + ". Linking...");
                LOGGER.fine("Connected horizontal: " + location + " -> " + nextLocation);
            }
        }
    }
    
    /**
     * Handles vertical connections between code blocks (parent-child relationships)
     * @param location The location of the placed block
     * @param codeBlock The placed code block
     */
    private void handleVerticalConnections(Location location, CodeBlock codeBlock) {
        // Find parent block and establish parent-child relationship
        CodeBlock parentBlock = findParentBlock(location);
        if (parentBlock != null) {
            parentBlock.addChild(codeBlock);
            LOGGER.fine("Established parent-child relationship: " + 
                getLocationString(parentBlock) + " -> " + getLocationString(codeBlock));
        }
    }
    
    /**
     * Gets the previous location in the same line
     * @param location The current location
     * @return The previous location in the same line, or null if not found
     */
    private Location getPreviousLocationInLine(Location location) {
        if (location == null) return null;
        
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return null;
        
        int currentX = location.getBlockX();
        if (currentX <= 0) return null;
        
        // Return the previous block in the same line
        return new Location(location.getWorld(), currentX - 1, location.getBlockY(), location.getBlockZ()).clone();
    }
    
    /**
     * Gets the next location in the same line
     * @param location The current location
     * @return The next location in the same line, or null if not found
     */
    private Location getNextLocationInLine(Location location) {
        if (location == null) return null;
        
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return null;
        
        int currentX = location.getBlockX();
        int maxBlocksPerLine = DevWorldGenerator.getBlocksPerLine();
        
        if (currentX >= maxBlocksPerLine - 1) return null;
        
        // Return the next block in the same line
        return new Location(location.getWorld(), currentX + 1, location.getBlockY(), location.getBlockZ()).clone();
    }
    
    /**
     * Finds the parent block for a child block based on indentation
     */
    private CodeBlock findParentBlock(Location childLocation) {
        int childLine = DevWorldGenerator.getCodeLineFromZ(childLocation.getBlockZ());
        int childX = childLocation.getBlockX();
        
        if (childX <= 0) {
            return null; // No parent for blocks at the start of the line
        }
        
        // Look for parent in previous lines with less indentation
        for (int parentLine = childLine - 1; parentLine >= 0; parentLine--) {
            int parentZ = DevWorldGenerator.getZForCodeLine(parentLine);
            
            // Look for blocks with less X coordinate (less indentation)
            for (int parentX = 0; parentX < childX; parentX++) {
                Location parentLocation = new Location(
                    childLocation.getWorld(), 
                    parentX, 
                    childLocation.getBlockY(), 
                    parentZ
                );
                // Use the placement handler to find parent blocks
                CodeBlock parentBlock = placementHandler.getCodeBlock(parentLocation);
                
                if (parentBlock != null && isControlBlock(parentBlock)) {
                    LOGGER.fine("Found parent block at (" + parentX + ", " + parentLine + 
                        ") for child at (" + childX + ", " + childLine + ")");
                    return parentBlock;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a block is a control block that can have children
     */
    private boolean isControlBlock(CodeBlock block) {
        // For now, we'll consider any non-bracket block as potentially having children
        // In a more sophisticated implementation, this would check block configuration
        return !block.isBracket();
    }
    
    /**
     * Gets a string representation of a block's location for logging
     */
    private String getLocationString(CodeBlock block) {
        // Find the location of this block in the placement handler's map
        Map<Location, CodeBlock> blockMap = placementHandler.getBlockCodeBlocks();
        for (Map.Entry<Location, CodeBlock> entry : blockMap.entrySet()) {
            if (entry.getValue() == block) {
                Location loc = entry.getKey();
                return "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")";
            }
        }
        return "unknown";
    }
    
    /**
     * Validates all connections in a world
     * @param world The world to validate connections in
     * @return true if all connections are valid, false otherwise
     */
    public boolean validateConnections(World world) {
        if (world == null) {
            return false;
        }
        
        boolean isValid = true;
        Map<Location, CodeBlock> worldBlocks = placementHandler.getBlockCodeBlocks();
        
        for (Map.Entry<Location, CodeBlock> entry : worldBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            // Validate next block connection
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                // Check if the next block actually exists at the expected location
                Location expectedLocation = getNextLocationInLine(location);
                if (expectedLocation != null) {
                    CodeBlock actualBlock = placementHandler.getCodeBlock(expectedLocation);
                    if (actualBlock != nextBlock) {
                        LOGGER.warning("Invalid next block connection at " + location);
                        isValid = false;
                    }
                }
            }
            
            // Validate child block connections
            for (CodeBlock child : block.getChildren()) {
                if (child == null) {
                    LOGGER.warning("Null child block found at " + location);
                    isValid = false;
                }
            }
        }
        
        return isValid;
    }
    
    /**
     * Rebuilds all connections in a world
     * @param world The world to rebuild connections in
     */
    public void rebuildConnections(World world) {
        if (world == null) {
            return;
        }
        
        LOGGER.fine("Rebuilding connections in world: " + world.getName());
        
        // Clear all existing connections
        Map<Location, CodeBlock> worldBlocks = placementHandler.getBlockCodeBlocks();
        for (CodeBlock block : worldBlocks.values()) {
            block.setNextBlock(null);
            block.setChildren(new ArrayList<>());
        }
        
        // Rebuild all connections by re-processing each block
        for (Map.Entry<Location, CodeBlock> entry : worldBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            // Re-establish horizontal connections
            Location prevLocation = getPreviousLocationInLine(location);
            if (prevLocation != null) {
                CodeBlock prevBlock = placementHandler.getCodeBlock(prevLocation);
                if (prevBlock != null && !prevBlock.isBracket()) {
                    prevBlock.setNextBlock(block);
                }
            }
            
            Location nextLocation = getNextLocationInLine(location);
            if (nextLocation != null) {
                CodeBlock nextBlock = placementHandler.getCodeBlock(nextLocation);
                if (nextBlock != null && !nextBlock.isBracket()) {
                    block.setNextBlock(nextBlock);
                }
            }
            
            // Re-establish vertical connections
            CodeBlock parentBlock = findParentBlock(location);
            if (parentBlock != null) {
                parentBlock.addChild(block);
            }
        }
        
        LOGGER.fine("Finished rebuilding connections in world: " + world.getName());
    }
}