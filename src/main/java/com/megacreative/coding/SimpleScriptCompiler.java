package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
                LOGGER.info("Linked horizontal: " + formatLocation(currentBlock) + " -> " + formatLocation(nextBlock));
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
        // Build the complete tree structure by linking all blocks
        Map<Location, CodeBlock> allBlocks = placementHandler.getBlockCodeBlocks();
        linkBlocks(rootBlock, allBlocks);
    }
    
    /**
     * Recursively links blocks to build the complete script structure
     * 
     * @param current The current block to process
     * @param allBlocks Map of all blocks in the world
     */
    private void linkBlocks(CodeBlock current, Map<Location, CodeBlock> allBlocks) {
        if (current == null) return;

        Location currentLocation = findBlockLocation(current);
        if (currentLocation == null) return;
        
        // 1. Связываем горизонтальную цепочку (nextBlock)
        Location nextLocation = currentLocation.clone().add(1, 0, 0);
        CodeBlock nextBlock = allBlocks.get(nextLocation);
        if (nextBlock != null) {
            current.setNextBlock(nextBlock);
            LOGGER.info("Linked horizontal: " + formatLocation(current) + " -> " + formatLocation(nextBlock));
        }

        // 2. Если это блок-контейнер (IF, REPEAT), ищем его дочерние элементы
        if (isControlBlock(current)) {
            // Дочерние блоки должны быть на следующей линии (например, Z+2)
            // и их X должен быть больше, чем у родителя (отступ)
            int parentX = currentLocation.getBlockX();
            int parentZ = currentLocation.getBlockZ();
            
            // Ищем дочерние блоки на следующей линии (Z + LINES_SPACING)
            int childLineZ = parentZ + DevWorldGenerator.getLinesSpacing();
            
            // Ищем первый блок с отступом (X > parentX) на следующей линии
            for (int childX = parentX + 1; childX < parentX + 20; childX++) { // Ограничиваем поиск 20 блоками вправо
                Location childLocation = new Location(currentLocation.getWorld(), childX, currentLocation.getBlockY(), childLineZ);
                CodeBlock childBlock = allBlocks.get(childLocation);
                
                if (childBlock != null) {
                    current.addChild(childBlock);
                    LOGGER.info("Linked child: " + formatLocation(current) + " -> " + formatLocation(childBlock));
                    // Рекурсивно строим цепочку для дочернего блока
                    linkBlocks(childBlock, allBlocks);
                    break; // Берем только первый дочерний блок
                }
            }
        }

        // 3. Рекурсивно идем по основной (горизонтальной) цепочке
        linkBlocks(current.getNextBlock(), allBlocks);
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
    
    /**
     * Prints detailed script structure for debugging
     * 
     * @param scripts The scripts to print
     */
    public void printScriptStructure(List<CodeScript> scripts) {
        for (int i = 0; i < scripts.size(); i++) {
            CodeScript script = scripts.get(i);
            LOGGER.info("Script " + (i + 1) + ": " + script.getRootBlock().getAction() + 
                       " (" + script.getBlocks().size() + " blocks)");
            printBlockChain(script.getRootBlock(), 0);
        }
    }
    
    /**
     * Recursively prints the block chain structure
     * 
     * @param block The block to print
     * @param indent The indentation level
     */
    private void printBlockChain(CodeBlock block, int indent) {
        if (block == null) return;
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }
        sb.append("- [").append(getBlockType(block)).append(": ").append(block.getAction()).append("]");
        
        LOGGER.info(sb.toString());
        
        // Print children if any
        if (!block.getChildren().isEmpty()) {
            for (CodeBlock child : block.getChildren()) {
                printBlockChain(child, indent + 1);
            }
        }
        
        // Continue with next block
        printBlockChain(block.getNextBlock(), indent);
    }
    
    /**
     * Gets a simple block type description for logging
     * 
     * @param block The block to describe
     * @return The block type description
     */
    private String getBlockType(CodeBlock block) {
        if (isEventBlock(block)) {
            return "Event";
        } else if (isControlBlock(block)) {
            return "Control";
        } else {
            return "Action";
        }
    }
}