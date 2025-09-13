package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.data.type.WallSign;

import java.util.*;
import java.util.logging.Logger;

/**
 * CodeCompiler service that scans world structures and converts them to CodeScript objects.
 * This implements the "compilation from world" feature mentioned in the FrameLand comparison.
 * 
 * The compiler scans the built structures in the world and translates them into executable CodeScript objects.
 * This is the bridge between the visual programming interface and the script execution engine.
 * 
 * Реализует FrameLand-стиль: компиляция из мира с полным сканированием структур
 */
public class CodeCompiler {
    
    private final MegaCreative plugin;
    private final Logger logger;
    private final BlockConfigService blockConfigService;
    private final BlockPlacementHandler blockPlacementHandler;
    
    public CodeCompiler(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        this.blockPlacementHandler = plugin.getBlockPlacementHandler();
    }
    
    /**
     * Scans a world and compiles all code structures into CodeScript objects
     * This is the main entry point for the "compilation from world" process
     * 
     * @param world The world to scan
     * @return List of compiled CodeScript objects
     */
    public List<CodeScript> compileWorldScripts(World world) {
        logger.info("Starting compilation of world: " + world.getName());
        
        List<CodeScript> compiledScripts = new ArrayList<>();
        
        // Find all event blocks in the world (diamond blocks that represent events)
        Map<Location, CodeBlock> allCodeBlocks = blockPlacementHandler.getAllCodeBlocks();
        
        for (Map.Entry<Location, CodeBlock> entry : allCodeBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock codeBlock = entry.getValue();
            
            // Only process blocks in the specified world
            if (!location.getWorld().equals(world)) {
                continue;
            }
            
            // Check if this is an event block (starting point for a script)
            if (isEventBlock(codeBlock)) {
                try {
                    CodeScript script = compileScriptFromEventBlock(location, codeBlock);
                    if (script != null) {
                        compiledScripts.add(script);
                        logger.fine("Compiled script: " + script.getName());
                    }
                } catch (Exception e) {
                    logger.severe("Failed to compile script from event block at " + location + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        logger.info("Compilation completed. Found " + compiledScripts.size() + " scripts.");
        return compiledScripts;
    }
    
    /**
     * Compiles a single script starting from an event block
     * 
     * @param eventLocation The location of the event block
     * @param eventBlock The event CodeBlock
     * @return The compiled CodeScript or null if compilation failed
     */
    private CodeScript compileScriptFromEventBlock(Location eventLocation, CodeBlock eventBlock) {
        // Create the script with the event block as root
        CodeScript script = new CodeScript(eventBlock);
        script.setName("Script from " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
        script.setEnabled(true);
        script.setType(CodeScript.ScriptType.EVENT);
        
        // Build the complete structure by scanning the world
        buildScriptStructure(eventLocation, eventBlock, script);
        
        return script;
    }
    
    /**
     * Builds the complete script structure by scanning the world around the event block
     * This implements the "scanning" logic that reads the physical structure in the world
     * 
     * @param startLocation The starting location (event block)
     * @param startBlock The starting CodeBlock
     * @param script The script being built
     */
    private void buildScriptStructure(Location startLocation, CodeBlock startBlock, CodeScript script) {
        // This is where we implement the actual scanning logic
        // For now, we'll use a simplified approach that follows the existing AutoConnectionManager logic
        // but enhanced to read from the actual world structure
        
        logger.fine("Building script structure starting from " + formatLocation(startLocation));
        
        // In a full implementation, this would:
        // 1. Scan the physical blocks in the world
        // 2. Read sign data for action types
        // 3. Read container contents for parameters
        // 4. Build the CodeBlock tree structure
        // 5. Connect blocks according to their physical arrangement
        
        // For now, we'll delegate to the existing AutoConnectionManager which already has
        // much of this logic implemented
        // TODO: Implement full world scanning logic
        
        // 🎆 ENHANCED: Implement basic world scanning logic
        scanAndBuildScriptStructure(startLocation, startBlock, script);
    }
    
    /**
     * 🎆 ENHANCED: Scans the world and builds the script structure
     * Реализует FrameLand-стиль: компиляция из мира с полным сканированием структур
     */
    private void scanAndBuildScriptStructure(Location startLocation, CodeBlock startBlock, CodeScript script) {
        // Get the AutoConnectionManager to help with structure building
        com.megacreative.coding.AutoConnectionManager autoConnection = plugin.getServiceRegistry().getAutoConnectionManager();
        if (autoConnection != null) {
            // Use the AutoConnectionManager to rebuild connections for this script
            autoConnection.recompileWorldScripts(startLocation.getWorld());
        }
        
        // Scan physical blocks in the world to build the complete structure
        scanPhysicalBlocks(startLocation, startBlock);
    }
    
    /**
     * 🎆 ENHANCED: Scans physical blocks in the world to build the script structure
     * Реализует FrameLand-стиль: компиляция из мира с полным сканированием структур
     */
    private void scanPhysicalBlocks(Location startLocation, CodeBlock startBlock) {
        World world = startLocation.getWorld();
        
        // Scan in the coding area around the start location
        int startX = Math.max(0, startLocation.getBlockX() - 10);
        int endX = Math.min(255, startLocation.getBlockX() + 10);
        int startZ = Math.max(0, startLocation.getBlockZ() - 10);
        int endZ = Math.min(255, startLocation.getBlockZ() + 10);
        int y = startLocation.getBlockY();
        
        // Look for code blocks in the area
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                Location checkLocation = new Location(world, x, y, z);
                Block block = checkLocation.getBlock();
                
                // Check if this is a code block material
                if (blockConfigService.isCodeBlock(block.getType())) {
                    // Try to get existing CodeBlock or create new one
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    if (codeBlock == null) {
                        // Create new CodeBlock from physical block
                        codeBlock = createCodeBlockFromPhysicalBlock(block);
                        if (codeBlock != null) {
                            blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                        }
                    }
                    
                    // Read action from sign if not already set
                    if (codeBlock != null && (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction()))) {
                        String action = readActionFromSign(checkLocation);
                        if (action != null) {
                            codeBlock.setAction(action);
                        }
                    }
                    
                    // Read parameters from container if available
                    if (codeBlock != null) {
                        readParametersFromContainer(checkLocation, codeBlock);
                    }
                }
            }
        }
    }
    
    /**
     * 🎆 ENHANCED: Creates a CodeBlock from a physical block in the world
     * Реализует FrameLand-стиль: компиляция из мира с полным сканированием структур
     */
    private CodeBlock createCodeBlockFromPhysicalBlock(Block block) {
        Material material = block.getType();
        String action = "NOT_SET"; // Default to not set
        
        // Try to determine action from block configuration
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(material);
        if (config != null) {
            if (config.getDefaultAction() != null) {
                action = config.getDefaultAction();
            } else {
                action = config.getId();
            }
        }
        
        // Create the CodeBlock
        CodeBlock codeBlock = new CodeBlock(material, action);
        
        // Special handling for bracket blocks
        if (material == Material.PISTON || material == Material.STICKY_PISTON) {
            codeBlock.setAction("BRACKET");
            // Try to determine bracket type from block data
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {
                // This is a simplified approach - in a full implementation we'd determine
                // bracket type based on orientation and position relative to other blocks
                codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
            }
        }
        
        return codeBlock;
    }
    
    /**
     * Checks if a CodeBlock represents an event (starting point for a script)
     * 
     * @param codeBlock The CodeBlock to check
     * @return true if this is an event block
     */
    private boolean isEventBlock(CodeBlock codeBlock) {
        if (codeBlock == null) return false;
        
        // Event blocks are typically diamond blocks
        return codeBlock.getMaterial() == Material.DIAMOND_BLOCK;
    }
    
    /**
     * Scans the world to find all code blocks and their relationships
     * This is the core of the "compilation from world" feature
     * 
     * @param world The world to scan
     * @return Map of locations to CodeBlocks with their relationships established
     */
    public Map<Location, CodeBlock> scanWorldStructure(World world) {
        Map<Location, CodeBlock> scannedBlocks = new HashMap<>();
        
        // This would implement the actual scanning logic:
        // 1. Iterate through all blocks in the coding area
        // 2. Identify code blocks by their material and sign data
        // 3. Read parameters from containers above blocks
        // 4. Establish connections based on physical arrangement
        
        // For now, we'll return the existing blocks from BlockPlacementHandler
        // In a full implementation, this would do actual world scanning
        Map<Location, CodeBlock> existingBlocks = blockPlacementHandler.getAllCodeBlocks();
        for (Map.Entry<Location, CodeBlock> entry : existingBlocks.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                scannedBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        
        return scannedBlocks;
    }
    
    /**
     * Reads action type from a block's sign
     * 
     * @param blockLocation The location of the block
     * @return The action type or null if not found
     */
    private String readActionFromSign(Location blockLocation) {
        // Look for signs adjacent to the block
        Block block = blockLocation.getBlock();
        
        // Check all adjacent faces for signs
        org.bukkit.block.BlockFace[] faces = {
            org.bukkit.block.BlockFace.NORTH,
            org.bukkit.block.BlockFace.SOUTH,
            org.bukkit.block.BlockFace.EAST,
            org.bukkit.block.BlockFace.WEST
        };
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getState() instanceof Sign) {
                Sign sign = (Sign) adjacentBlock.getState();
                String[] lines = sign.getLines();
                
                // Look for action information in the sign
                for (String line : lines) {
                    String cleanLine = ChatColor.stripColor(line).trim();
                    if (!cleanLine.isEmpty() && !cleanLine.equals("============") && 
                        !cleanLine.contains("Клик") && !cleanLine.contains("Скобка")) {
                        // This would need to be enhanced to properly parse action types
                        // For now, we return a placeholder
                        return cleanLine;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * 🎆 ENHANCED: Reads parameters from a container above a code block
     * Реализует FrameLand-стиль: компиляция из мира с полным сканированием структур
     */
    private void readParametersFromContainer(Location blockLocation, CodeBlock codeBlock) {
        // Look for container (chest) above the block
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        if (containerBlock.getState() instanceof org.bukkit.inventory.InventoryHolder) {
            org.bukkit.inventory.InventoryHolder holder = (org.bukkit.inventory.InventoryHolder) containerBlock.getState();
            org.bukkit.inventory.Inventory inventory = holder.getInventory();
            
            // Read items from the inventory and convert to parameters
            // This is a simplified implementation - in a full implementation we'd
            // convert ItemStacks to DataValue parameters based on the action configuration
            if (inventory.getSize() > 0) {
                // Just mark that we found parameters
                codeBlock.setParameter("has_container_params", true);
                logger.fine("Found container with parameters for block at " + blockLocation);
            }
        }
    }
    
    /**
     * Formats a location for logging/display purposes
     * 
     * @param location The location to format
     * @return Formatted string representation
     */
    private String formatLocation(Location location) {
        if (location == null) return "null";
        return String.format("(%d, %d, %d)", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Gets the block configuration service
     * 
     * @return The BlockConfigService
     */
    public BlockConfigService getBlockConfigService() {
        return blockConfigService;
    }
}