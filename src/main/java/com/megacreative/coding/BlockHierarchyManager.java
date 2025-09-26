package com.megacreative.coding;

import com.megacreative.coding.events.CodeBlockPlacedEvent;
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
    
    /**
     * Handles block placement and establishes parent-child relationships
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        // Skip hierarchy logic for bracket blocks
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping hierarchy for bracket block at " + location);
            return;
        }
        
        // Add block to our tracking
        locationToBlock.put(location, codeBlock);
        
        // Find parent block and establish parent-child relationship
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
                CodeBlock parentBlock = locationToBlock.get(parentLocation);
                
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
        // Find the location of this block in our map
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                Location loc = entry.getKey();
                return "(" + loc.getBlockX() + ", " + loc.getBlockZ() + ")";
            }
        }
        return "unknown";
    }
}