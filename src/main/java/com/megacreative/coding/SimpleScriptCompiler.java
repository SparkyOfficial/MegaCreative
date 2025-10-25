package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * A simplified script compiler that builds script structures on-demand
 * instead of trying to maintain real-time connections between blocks.
 * 
 * @author Андрій Будильников
 */
public class SimpleScriptCompiler {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleScriptCompiler.class.getName());
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final BlockPlacementHandler placementHandler;
    
    public SimpleScriptCompiler(MegaCreative plugin, BlockConfigService blockConfigService, 
                               BlockPlacementHandler placementHandler) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
        this.placementHandler = placementHandler;
    }
    
    /**
     * Compiles all scripts in a world by finding event blocks and building
     * their action chains from scratch.
     * 
     * @param world The world to compile scripts for
     * @return List of compiled scripts
     */
    public List<CodeScript> compileWorldScripts(World world) {
        List<CodeScript> scripts = new ArrayList<>();
        
        // Find all event blocks in the world
        List<CodeBlock> eventBlocks = findEventBlocksInWorld(world);
        
        // For each event block, build its complete script
        for (CodeBlock eventBlock : eventBlocks) {
            try {
                CodeScript script = compileScriptFromEventBlock(eventBlock, world);
                if (script != null) {
                    scripts.add(script);
                }
            } catch (Exception e) {
                LOGGER.warning("Error compiling script from event block at " + 
                    formatLocation(eventBlock) + ": " + e.getMessage());
            }
        }
        
        return scripts;
    }
    
    /**
     * Finds all event blocks (blocks with actions starting with "on") in a world
     * 
     * @param world The world to search in
     * @return List of event blocks
     */
    private List<CodeBlock> findEventBlocksInWorld(World world) {
        List<CodeBlock> eventBlocks = new ArrayList<>();
        
        // Iterate through all code blocks in the placement handler
        for (Location location : placementHandler.getBlockCodeBlocks().keySet()) {
            // Check if this block is in the specified world
            if (location.getWorld().equals(world)) {
                CodeBlock block = placementHandler.getCodeBlock(location);
                if (block != null && isEventBlock(block)) {
                    eventBlocks.add(block);
                }
            }
        }
        
        return eventBlocks;
    }
    
    /**
     * Checks if a block is an event block (action starts with "on")
     * 
     * @param block The block to check
     * @return true if it's an event block
     */
    private boolean isEventBlock(CodeBlock block) {
        return block != null && block.getAction() != null && 
               block.getAction().startsWith("on");
    }
    
    /**
     * Compiles a complete script starting from an event block
     * 
     * @param eventBlock The root event block
     * @param world The world the block is in
     * @return The compiled script
     */
    private CodeScript compileScriptFromEventBlock(CodeBlock eventBlock, World world) {
        // Create a new script with the event block as root
        CodeScript script = new CodeScript(eventBlock);
        
        // Build the horizontal chain (next blocks)
        buildHorizontalChain(eventBlock);
        
        // Build vertical chains (children/parents)
        buildVerticalChains(eventBlock);
        
        return script;
    }
    
    /**
     * Builds the horizontal chain of blocks (nextBlock relationships)
     * by scanning to the right from the starting block
     * 
     * @param startBlock The block to start from
     */
    private void buildHorizontalChain(CodeBlock startBlock) {
        CodeBlock currentBlock = startBlock;
        Location currentLocation = findBlockLocation(startBlock);
        
        if (currentLocation == null) return;
        
        // Keep scanning to the right until we find no more blocks
        while (currentBlock != null) {
            // Get the next location to the right
            Location nextLocation = currentLocation.clone().add(1, 0, 0);
            CodeBlock nextBlock = placementHandler.getCodeBlock(nextLocation);
            
            // If we found a next block, connect it
            if (nextBlock != null) {
                currentBlock.setNextBlock(nextBlock);
                currentBlock = nextBlock;
                currentLocation = nextLocation;
            } else {
                // No more blocks to the right, stop
                break;
            }
        }
    }
    
    /**
     * Builds vertical chains (parent-child relationships) for control blocks
     * 
     * @param rootBlock The root block to start from
     */
    private void buildVerticalChains(CodeBlock rootBlock) {
        // This is a simplified version - in a real implementation,
        // you would need to scan for parent-child relationships based on
        // indentation and block types
        // For now, we'll just ensure existing children are properly connected
        connectChildren(rootBlock);
    }
    
    /**
     * Recursively connects children for a block and its chain
     * 
     * @param block The block to process
     */
    private void connectChildren(CodeBlock block) {
        if (block == null) return;
        
        // Process this block's children
        findAndConnectChildren(block);
        
        // Process next block in chain
        connectChildren(block.getNextBlock());
        
        // Process children's chains
        for (CodeBlock child : block.getChildren()) {
            connectChildren(child);
        }
    }
    
    /**
     * Finds and connects children for a specific block based on position
     * 
     * @param parentBlock The parent block
     */
    private void findAndConnectChildren(CodeBlock parentBlock) {
        if (parentBlock == null || !isControlBlock(parentBlock)) {
            return;
        }
        
        Location parentLocation = findBlockLocation(parentBlock);
        if (parentLocation == null) return;
        
        // Look for children blocks below and to the right
        // This is a simplified approach - a full implementation would need
        // to check indentation levels and block types more carefully
        Location childLocation = parentLocation.clone().add(0, 0, 2); // 2 blocks down (next line)
        
        // Check if there's a block at the expected child position
        CodeBlock childBlock = placementHandler.getCodeBlock(childLocation);
        if (childBlock != null && childBlock.getX() > parentBlock.getX()) {
            // Add as child if it's indented (further right)
            parentBlock.addChild(childBlock);
        }
    }
    
    /**
     * Checks if a block is a control block that can have children
     * 
     * @param block The block to check
     * @return true if it's a control block
     */
    private boolean isControlBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) return false;
        
        // Control blocks that can have children
        switch (block.getMaterialName()) {
            case "OAK_PLANKS":  // CONDITION
            case "OBSIDIAN":    // IF_VAR
            case "REDSTONE_BLOCK": // IF_GAME
            case "BRICKS":      // IF_MOB
            case "EMERALD_BLOCK": // REPEAT
            case "END_STONE":   // ELSE
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Finds the location of a specific code block
     * 
     * @param block The block to find
     * @return The location, or null if not found
     */
    private Location findBlockLocation(CodeBlock block) {
        if (block == null) return null;
        
        // Search through all locations to find this block
        for (Location location : placementHandler.getBlockCodeBlocks().keySet()) {
            CodeBlock storedBlock = placementHandler.getCodeBlock(location);
            if (storedBlock != null && storedBlock.getId().equals(block.getId())) {
                return location;
            }
        }
        
        return null;
    }
    
    /**
     * Formats a location for logging
     * 
     * @param block The block to get location for
     * @return Formatted location string
     */
    private String formatLocation(CodeBlock block) {
        Location location = findBlockLocation(block);
        if (location != null) {
            return "(" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
        }
        return "unknown";
    }
    
    /**
     * Saves compiled scripts to a creative world
     * 
     * @param world The Bukkit world
     * @param scripts The scripts to save
     */
    public void saveScriptsToWorld(World world, List<CodeScript> scripts) {
        try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
            
            if (creativeWorld != null) {
                creativeWorld.setScripts(scripts);
                worldManager.saveWorld(creativeWorld);
                LOGGER.info("Saved " + scripts.size() + " scripts to world " + world.getName());
            }
        } catch (Exception e) {
            LOGGER.warning("Error saving scripts to world: " + e.getMessage());
        }
    }
}