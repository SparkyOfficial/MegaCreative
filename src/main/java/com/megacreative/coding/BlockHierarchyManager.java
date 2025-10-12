package com.megacreative.coding;

import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Handles parent-child relationships between code blocks
 * Listens to CodeBlockPlacedEvent and establishes parent-child connections
 *
 * Обрабатывает родительские и дочерние связи между кодовыми блоками
 * Слушает события CodeBlockPlacedEvent и устанавливает родительские и дочерние связи
 *
 * Behandelt Eltern-Kind-Beziehungen zwischen Codeblöcken
 * Hört auf CodeBlockPlacedEvent und stellt Eltern-Kind-Beziehungen her
 */
public class BlockHierarchyManager implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(BlockHierarchyManager.class.getName());
    
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    
    private Map<Location, CodeBlock> sharedLocationToBlock = new HashMap<>();
    
    /**
     * Sets the shared location to block map
     * @param sharedMap The shared map
     */
    public void setSharedLocationToBlock(Map<Location, CodeBlock> sharedMap) {
        this.sharedLocationToBlock = sharedMap;
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
     * Handles block placement and establishes parent-child relationships
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping hierarchy for bracket block at " + location);
            return;
        }
        
        
        locationToBlock.put(location, codeBlock);
        
        
        CodeBlock parentBlock = findParentBlock(location);
        if (parentBlock != null) {
            parentBlock.addChild(codeBlock);
            LOGGER.fine("Established parent-child relationship: " + 
                getLocationString(parentBlock) + " -> " + getLocationString(codeBlock));
        }
    }
    
    /**
     * Finds the parent block for a child block based on indentation
     */
    private CodeBlock findParentBlock(Location childLocation) {
        int childLine = DevWorldGenerator.getCodeLineFromZ(childLocation.getBlockZ());
        int childX = childLocation.getBlockX();
        
        if (childX <= 0) {
            return null; 
        }
        
        
        for (int parentLine = childLine - 1; parentLine >= 0; parentLine--) {
            int parentZ = DevWorldGenerator.getZForCodeLine(parentLine);
            
            
            for (int parentX = 0; parentX < childX; parentX++) {
                Location parentLocation = new Location(
                    childLocation.getWorld(), 
                    parentX, 
                    childLocation.getBlockY(), 
                    parentZ
                );
                
                CodeBlock parentBlock = getBlockAtLocation(parentLocation);
                
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
        
        
        return !block.isBracket();
    }
    
    /**
     * Gets a string representation of a block's location for logging
     */
    private String getLocationString(CodeBlock block) {
        
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                Location loc = entry.getKey();
                return "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")";
            }
        }
        
        for (Map.Entry<Location, CodeBlock> entry : sharedLocationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                Location loc = entry.getKey();
                return "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")";
            }
        }
        return "unknown";
    }
}