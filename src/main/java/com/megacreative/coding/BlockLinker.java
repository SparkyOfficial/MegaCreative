package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlocksConnectedEvent;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Handles horizontal connections between code blocks (nextBlock relationships)
 * Listens to CodeBlockPlacedEvent and establishes connections between adjacent blocks
 *
 * Обрабатывает горизонтальные соединения между кодовыми блоками (отношения nextBlock)
 * Слушает события CodeBlockPlacedEvent и устанавливает соединения между соседними блоками
 *
 * Behandelt horizontale Verbindungen zwischen Codeblöcken (nextBlock-Beziehungen)
 * Hört auf CodeBlockPlacedEvent und stellt Verbindungen zwischen benachbarten Blöcken her
 * 
 * @author Андрій Будильников
 */
public class BlockLinker implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(BlockLinker.class.getName());
    
    private final MegaCreative plugin;
    private final BlockPlacementHandler placementHandler;
    
    public BlockLinker(MegaCreative plugin, BlockPlacementHandler placementHandler) {
        this.plugin = plugin;
        this.placementHandler = placementHandler;
    }
    
    /**
     * Handles block placement and establishes horizontal connections
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        LOGGER.info("Linker is trying to link block at " + location);
        
        // Skip connection logic for bracket blocks
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping connection for bracket block at " + location);
            return;
        }
        
        // Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = placementHandler.getCodeBlock(prevLocation);
            if (prevBlock != null && !prevBlock.isBracket()) { // Skip brackets
                prevBlock.setNextBlock(codeBlock);
                LOGGER.info("Found neighbor at " + prevLocation + ". Linking...");
                LOGGER.fine("Connected horizontal: " + prevLocation + " -> " + location);
                
                // Fire event for connection visualization
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(prevLocation, location));
            }
        }
        
        // Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = placementHandler.getCodeBlock(nextLocation);
            if (nextBlock != null && !nextBlock.isBracket()) { // Skip brackets
                codeBlock.setNextBlock(nextBlock);
                LOGGER.info("Found next block at " + nextLocation + ". Linking...");
                LOGGER.fine("Connected horizontal: " + location + " -> " + nextLocation);
                
                // Fire event for connection visualization
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(location, nextLocation));
            }
        }
        
        // Enhanced connection logic for complex structures
        establishComplexConnections(location, codeBlock);
    }
    
    /**
     * Establishes complex connections for nested structures and control flow
     * @param location The location of the placed block
     * @param codeBlock The placed code block
     */
    private void establishComplexConnections(Location location, CodeBlock codeBlock) {
        // Handle connections for control flow blocks (IF, WHILE, etc.)
        if (isControlFlowBlock(codeBlock)) {
            // Find matching closing bracket or end of control structure
            Location endLocation = findControlStructureEnd(location);
            if (endLocation != null) {
                CodeBlock endBlock = placementHandler.getCodeBlock(endLocation);
                if (endBlock != null) {
                    // Create connection to end of control structure
                    codeBlock.setNextBlock(endBlock);
                    LOGGER.fine("Connected control flow: " + location + " -> " + endLocation);
                    
                    // Fire event for connection visualization
                    plugin.getServer().getPluginManager().callEvent(
                        new CodeBlocksConnectedEvent(location, endLocation));
                }
            }
        }
        
        // Handle connections for function blocks
        if (isFunctionBlock(codeBlock)) {
            // Find function end or return statement
            Location functionEndLocation = findFunctionEnd(location);
            if (functionEndLocation != null) {
                CodeBlock functionEndBlock = placementHandler.getCodeBlock(functionEndLocation);
                if (functionEndBlock != null) {
                    // Create connection to end of function
                    codeBlock.setNextBlock(functionEndBlock);
                    LOGGER.fine("Connected function: " + location + " -> " + functionEndLocation);
                    
                    // Fire event for connection visualization
                    plugin.getServer().getPluginManager().callEvent(
                        new CodeBlocksConnectedEvent(location, functionEndLocation));
                }
            }
        }
    }
    
    /**
     * Checks if a block is a control flow block (IF, WHILE, FOR, etc.)
     * @param block The block to check
     * @return true if the block is a control flow block
     */
    private boolean isControlFlowBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        String action = block.getAction().toLowerCase();
        return action.contains("if") || action.contains("while") || action.contains("for") || 
               action.contains("loop") || action.contains("repeat") || action.contains("else");
    }
    
    /**
     * Checks if a block is a function block
     * @param block The block to check
     * @return true if the block is a function block
     */
    private boolean isFunctionBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        String action = block.getAction().toLowerCase();
        return action.contains("function") || action.contains("method") || action.contains("procedure");
    }
    
    /**
     * Finds the end of a control structure (matching bracket or structure end)
     * @param startLocation The starting location of the control structure
     * @return The location of the end of the control structure, or null if not found
     */
    private Location findControlStructureEnd(Location startLocation) {
        if (startLocation == null) {
            return null;
        }
        
        int line = DevWorldGenerator.getCodeLineFromZ(startLocation.getBlockZ());
        int startX = startLocation.getBlockX();
        int maxBlocksPerLine = DevWorldGenerator.getBlocksPerLine();
        World world = startLocation.getWorld();
        
        // Look for the next block at the same indentation level
        for (int x = startX + 1; x < maxBlocksPerLine; x++) {
            Location checkLocation = new Location(world, x, startLocation.getBlockY(), startLocation.getBlockZ());
            CodeBlock checkBlock = placementHandler.getCodeBlock(checkLocation);
            
            if (checkBlock != null) {
                // If we find a block at the same indentation level, it's the end of the control structure
                if (x == startX) {
                    return checkLocation;
                }
                // If we find a block with less indentation, it's the end of the control structure
                else if (x < startX) {
                    return checkLocation;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Finds the end of a function (return statement or end of function)
     * @param startLocation The starting location of the function
     * @return The location of the end of the function, or null if not found
     */
    private Location findFunctionEnd(Location startLocation) {
        if (startLocation == null) {
            return null;
        }
        
        int line = DevWorldGenerator.getCodeLineFromZ(startLocation.getBlockZ());
        int startX = startLocation.getBlockX();
        int maxBlocksPerLine = DevWorldGenerator.getBlocksPerLine();
        World world = startLocation.getWorld();
        
        // Look for a return statement or end of function
        for (int x = startX + 1; x < maxBlocksPerLine; x++) {
            Location checkLocation = new Location(world, x, startLocation.getBlockY(), startLocation.getBlockZ());
            CodeBlock checkBlock = placementHandler.getCodeBlock(checkLocation);
            
            if (checkBlock != null) {
                // If we find a return statement, it's the end of the function
                if (checkBlock.getAction() != null && checkBlock.getAction().toLowerCase().contains("return")) {
                    return checkLocation;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Gets the previous location in the same line
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
        
        LOGGER.info("Rebuilding connections in world: " + world.getName());
        
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
            
            // Re-establish complex connections
            establishComplexConnections(location, block);
        }
        
        LOGGER.info("Finished rebuilding connections in world: " + world.getName());
    }
}