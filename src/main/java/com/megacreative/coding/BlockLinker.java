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
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    
    
    private Map<Location, CodeBlock> sharedLocationToBlock = new HashMap<>();
    
    public BlockLinker(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sets the shared location to block map
     * @param sharedMap The shared map
     */
    public void setSharedLocationToBlock(Map<Location, CodeBlock> sharedMap) {
        this.sharedLocationToBlock = sharedMap;
    }
    
    /**
     * Gets all blocks in a world
     * @param world The world to get blocks from
     * @return A map of locations to code blocks in the world
     */
    public Map<Location, CodeBlock> getWorldBlocks(World world) {
        Map<Location, CodeBlock> worldBlocks = new HashMap<>();
        
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        
        for (Map.Entry<Location, CodeBlock> entry : sharedLocationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        return worldBlocks;
    }
    
    /**
     * Gets a block at a specific location, checking both local and shared maps
     * @param location The location to check
     * @return The code block at that location, or null if not found
     */
    private CodeBlock getBlockAtLocation(Location location) {
        
        CodeBlock block = locationToBlock.get(location);
        if (block != null) {
            return block;
        }
        
        
        return sharedLocationToBlock.get(location);
    }
    
    /**
     * Handles block placement and establishes horizontal connections
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping connection for bracket block at " + location);
            return;
        }
        
        
        locationToBlock.put(location, codeBlock);
        
        
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = getBlockAtLocation(prevLocation);
            if (prevBlock != null && !prevBlock.isBracket()) { 
                prevBlock.setNextBlock(codeBlock);
                LOGGER.fine("Connected horizontal: " + prevLocation + " -> " + location);
                
                
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(prevLocation, location));
            }
        }
        
        
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = getBlockAtLocation(nextLocation);
            if (nextBlock != null && !nextBlock.isBracket()) { 
                codeBlock.setNextBlock(nextBlock);
                LOGGER.fine("Connected horizontal: " + location + " -> " + nextLocation);
                
                
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(location, nextLocation));
            }
        }
        
        
        establishComplexConnections(location, codeBlock);
    }
    
    /**
     * Establishes complex connections for nested structures and control flow
     * @param location The location of the placed block
     * @param codeBlock The placed code block
     */
    private void establishComplexConnections(Location location, CodeBlock codeBlock) {
        
        if (isControlFlowBlock(codeBlock)) {
            
            Location endLocation = findControlStructureEnd(location);
            if (endLocation != null) {
                CodeBlock endBlock = getBlockAtLocation(endLocation);
                if (endBlock != null) {
                    
                    codeBlock.setNextBlock(endBlock);
                    LOGGER.fine("Connected control flow: " + location + " -> " + endLocation);
                    
                    
                    plugin.getServer().getPluginManager().callEvent(
                        new CodeBlocksConnectedEvent(location, endLocation));
                }
            }
        }
        
        
        if (isFunctionBlock(codeBlock)) {
            
            Location functionEndLocation = findFunctionEnd(location);
            if (functionEndLocation != null) {
                CodeBlock functionEndBlock = getBlockAtLocation(functionEndLocation);
                if (functionEndBlock != null) {
                    
                    codeBlock.setNextBlock(functionEndBlock);
                    LOGGER.fine("Connected function: " + location + " -> " + functionEndLocation);
                    
                    
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
        
        
        for (int x = startX + 1; x < maxBlocksPerLine; x++) {
            Location checkLocation = new Location(world, x, startLocation.getBlockY(), startLocation.getBlockZ());
            CodeBlock checkBlock = getBlockAtLocation(checkLocation);
            
            if (checkBlock != null) {
                
                if (x == startX) {
                    return checkLocation;
                }
                
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
        
        
        for (int x = startX + 1; x < maxBlocksPerLine; x++) {
            Location checkLocation = new Location(world, x, startLocation.getBlockY(), startLocation.getBlockZ());
            CodeBlock checkBlock = getBlockAtLocation(checkLocation);
            
            if (checkBlock != null) {
                
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
        Map<Location, CodeBlock> worldBlocks = getWorldBlocks(world);
        
        for (Map.Entry<Location, CodeBlock> entry : worldBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            
            CodeBlock nextBlock = block.getNextBlock();
            if (nextBlock != null) {
                
                Location expectedLocation = getNextLocationInLine(location);
                if (expectedLocation != null) {
                    CodeBlock actualBlock = getBlockAtLocation(expectedLocation);
                    if (actualBlock != nextBlock) {
                        LOGGER.warning("Invalid next block connection at " + location);
                        isValid = false;
                    }
                }
            }
            
            
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
        
        
        Map<Location, CodeBlock> worldBlocks = getWorldBlocks(world);
        for (CodeBlock block : worldBlocks.values()) {
            block.setNextBlock(null);
            block.setChildren(new ArrayList<>());
        }
        
        
        for (Map.Entry<Location, CodeBlock> entry : worldBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            
            Location prevLocation = getPreviousLocationInLine(location);
            if (prevLocation != null) {
                CodeBlock prevBlock = getBlockAtLocation(prevLocation);
                if (prevBlock != null && !prevBlock.isBracket()) {
                    prevBlock.setNextBlock(block);
                }
            }
            
            Location nextLocation = getNextLocationInLine(location);
            if (nextLocation != null) {
                CodeBlock nextBlock = getBlockAtLocation(nextLocation);
                if (nextBlock != null && !nextBlock.isBracket()) {
                    block.setNextBlock(nextBlock);
                }
            }
            
            
            establishComplexConnections(location, block);
        }
        
        LOGGER.info("Finished rebuilding connections in world: " + world.getName());
    }
}