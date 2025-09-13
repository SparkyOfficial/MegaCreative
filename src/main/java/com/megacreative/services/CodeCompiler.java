package com.megacreative.services;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.services.BlockConfigService;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.*;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.block.BlockFace;

/**
 * CodeCompiler service that scans world structures and converts them to CodeScript objects.
 * This implements the "compilation from world" feature mentioned in the reference system comparison.
 * 
 * The compiler scans the built structures in the world and translates them into executable CodeScript objects.
 * This is the bridge between the visual programming interface and the script execution engine.
 * 
 * Implements reference system-style: compilation from world with full structure scanning
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
        
        // First, scan the world structure to ensure all blocks are registered
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        logger.info("World scan found " + scannedBlocks.size() + " code blocks");
        
        List<CodeScript> compiledScripts = new ArrayList<>();
        int scriptCount = 0;
        int errorCount = 0;
        
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
                        scriptCount++;
                        logger.fine("Compiled script: " + script.getName());
                    } else {
                        logger.warning("Failed to compile script from event block at " + formatLocation(location));
                        errorCount++;
                    }
                } catch (Exception e) {
                    logger.severe("Failed to compile script from event block at " + formatLocation(location) + ": " + e.getMessage());
                    e.printStackTrace();
                    errorCount++;
                }
            }
        }
        
        logger.info("Compilation completed. Found " + scriptCount + " scripts with " + errorCount + " errors.");
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
        
        logger.fine("Starting compilation of script from event block at " + formatLocation(eventLocation));
        
        // Build the complete structure by scanning the world
        buildScriptStructure(eventLocation, eventBlock, script);
        
        logger.fine("Completed compilation of script: " + script.getName());
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
        logger.fine("Building script structure starting from " + formatLocation(startLocation));
        
        // Scan physical blocks in the world to build the complete structure
        scanPhysicalBlocks(startLocation, startBlock);
        
        // Use AutoConnectionManager to establish proper connections
        com.megacreative.coding.AutoConnectionManager autoConnection = plugin.getServiceRegistry().getAutoConnectionManager();
        if (autoConnection != null) {
            logger.fine("Recompiling world scripts with AutoConnectionManager");
            autoConnection.recompileWorldScripts(startLocation.getWorld());
        }
        
        logger.fine("Script structure building completed for script: " + script.getName());
    }
    
    /**
     * Scans physical blocks in the world to build the script structure
     * Implements reference system-style: compilation from world with full structure scanning
     */
    private void scanPhysicalBlocks(Location startLocation, CodeBlock startBlock) {
        World world = startLocation.getWorld();
        
        // Enhanced scanning with better area coverage
        int scanRadius = 25; // Increased scan radius
        int startX = Math.max(0, startLocation.getBlockX() - scanRadius);
        int endX = Math.min(255, startLocation.getBlockX() + scanRadius);
        int startZ = Math.max(0, startLocation.getBlockZ() - scanRadius);
        int endZ = Math.min(255, startLocation.getBlockZ() + scanRadius);
        int y = startLocation.getBlockY();
        
        int blocksProcessed = 0;
        
        logger.fine("Scanning physical blocks in area: (" + startX + "," + startZ + ") to (" + endX + "," + endZ + ")");
        
        // Look for code blocks in the area
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                Location checkLocation = new Location(world, x, y, z);
                Block block = checkLocation.getBlock();
                
                // Check if this is a code block material
                if (blockConfigService.isCodeBlock(block.getType())) {
                    // Try to get existing CodeBlock or create new one
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        // Create new CodeBlock from physical block
                        codeBlock = createCodeBlockFromPhysicalBlock(block);
                        if (codeBlock != null) {
                            blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                            wasCreated = true;
                        }
                    }
                    
                    // Read action from sign if not already set
                    if (codeBlock != null && (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction()))) {
                        String action = readActionFromSign(checkLocation);
                        if (action != null) {
                            codeBlock.setAction(action);
                            if (wasCreated) {
                                logger.fine("Created code block with action '" + action + "' at " + formatLocation(checkLocation));
                            }
                        }
                    }
                    
                    // Read parameters from container if available
                    if (codeBlock != null) {
                        readParametersFromContainer(checkLocation, codeBlock);
                    }
                    
                    if (codeBlock != null) {
                        blocksProcessed++;
                    }
                }
                // Also check for bracket pistons
                else if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                    CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                    boolean wasCreated = false;
                    
                    if (codeBlock == null) {
                        // Create new CodeBlock for bracket
                        codeBlock = createBracketBlockFromPhysicalBlock(block);
                        if (codeBlock != null) {
                            blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                            wasCreated = true;
                        }
                    }
                    
                    if (codeBlock != null && wasCreated) {
                        logger.fine("Created bracket block at " + formatLocation(checkLocation));
                        blocksProcessed++;
                    }
                }
            }
        }
        
        logger.fine("Physical block scan completed. Processed " + blocksProcessed + " blocks.");
    }
    
    /**
     * Creates a CodeBlock from a physical block in the world
     * Implements reference system-style: compilation from world with full structure scanning
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
        
        return codeBlock;
    }
    
    /**
     * Creates a bracket CodeBlock from a physical piston block
     */
    private CodeBlock createBracketBlockFromPhysicalBlock(Block block) {
        Material material = block.getType();
        
        // Create the CodeBlock for bracket
        CodeBlock codeBlock = new CodeBlock(material, "BRACKET");
        
        // Try to determine bracket type from block data and sign
        Location location = block.getLocation();
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
        
        for (BlockFace face : faces) {
            Block adjacentBlock = block.getRelative(face);
            if (adjacentBlock.getState() instanceof Sign) {
                Sign sign = (Sign) adjacentBlock.getState();
                String[] lines = sign.getLines();
                
                if (lines.length > 1) {
                    String line2 = ChatColor.stripColor(lines[1]).trim();
                    if (line2.contains("{")) {
                        codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                        break;
                    } else if (line2.contains("}")) {
                        codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                        break;
                    }
                }
            }
        }
        
        // If we couldn't determine from sign, try from piston orientation
        if (codeBlock.getBracketType() == null) {
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Piston pistonData) {
                BlockFace facing = pistonData.getFacing();
                // Simple heuristic: if facing east, it's likely an opening bracket
                // if facing west, it's likely a closing bracket
                if (facing == BlockFace.EAST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
                } else if (facing == BlockFace.WEST) {
                    codeBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                } else {
                    codeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default
                }
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
        
        // Enhanced world scanning with proper structure detection
        logger.info("Starting enhanced world scan for code structures in world: " + world.getName());
        
        // Iterate through the development area with optimized scanning
        int minX = 0, maxX = 255;
        int minZ = 0, maxZ = 255;
        int y = world.getHighestBlockYAt(0, 0); // Assume consistent height in dev world
    
        int blocksScanned = 0;
        int blocksProcessed = 0;
        
        // Scan in chunks for better performance
        for (int chunkX = minX; chunkX <= maxX; chunkX += 16) {
            for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ += 16) {
                // Scan each position in the chunk
                for (int x = chunkX; x < Math.min(chunkX + 16, maxX + 1); x++) {
                    for (int z = chunkZ; z < Math.min(chunkZ + 16, maxZ + 1); z++) {
                        Location checkLocation = new Location(world, x, y, z);
                        Block block = checkLocation.getBlock();
                        
                        blocksScanned++;
                        
                        // Identify code blocks by their material
                        if (blockConfigService.isCodeBlock(block.getType()) || 
                            block.getType() == Material.PISTON || 
                            block.getType() == Material.STICKY_PISTON) {
                            
                            // Create or get existing CodeBlock
                            CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(checkLocation);
                            boolean isNewBlock = (codeBlock == null);
                            
                            if (codeBlock == null) {
                                if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
                                    codeBlock = createBracketBlockFromPhysicalBlock(block);
                                } else {
                                    codeBlock = createCodeBlockFromPhysicalBlock(block);
                                }
                                
                                if (codeBlock != null) {
                                    blockPlacementHandler.getAllCodeBlocks().put(checkLocation, codeBlock);
                                }
                            }
                            
                            if (codeBlock != null) {
                                // Read action from sign
                                if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                                    String action = readActionFromSign(checkLocation);
                                    if (action != null) {
                                        codeBlock.setAction(action);
                                    }
                                }
                                
                                // Read parameters from container
                                readParametersFromContainer(checkLocation, codeBlock);
                                
                                scannedBlocks.put(checkLocation, codeBlock);
                                blocksProcessed++;
                                
                                // Log new block discovery
                                if (isNewBlock) {
                                    logger.fine("Discovered new code block at " + formatLocation(checkLocation) + 
                                        " with action: " + codeBlock.getAction());
                                }
                            }
                        }
                    }
                }
            }
        }
        
        logger.info("World scan completed. Scanned " + blocksScanned + " blocks, processed " + blocksProcessed + " code blocks.");
        return scannedBlocks;
    }

    /**
     * Reads action type from a block's sign with enhanced parsing
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
                
                // Look for action information in the sign with enhanced parsing
                for (String line : lines) {
                    String cleanLine = ChatColor.stripColor(line).trim();
                    if (!cleanLine.isEmpty() && !cleanLine.equals("============") && 
                        !cleanLine.contains("Клик") && !cleanLine.contains("Скобка") &&
                        !cleanLine.contains("★★★★★★★★★★★★") && !cleanLine.contains("➜")) {
                        
                        // Try to match with known actions from configuration
                        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
                        if (config != null) {
                            // First try exact match
                            List<String> availableActions = blockConfigService.getAvailableActions(block.getType());
                            for (String action : availableActions) {
                                if (action.equalsIgnoreCase(cleanLine)) {
                                    logger.fine("Found exact action match: " + action + " for block at " + formatLocation(blockLocation));
                                    return action;
                                }
                            }
                            
                            // Then try partial match
                            for (String action : availableActions) {
                                if (action.toLowerCase().contains(cleanLine.toLowerCase()) ||
                                    cleanLine.toLowerCase().contains(action.toLowerCase())) {
                                    logger.fine("Found partial action match: " + action + " for block at " + formatLocation(blockLocation));
                                    return action;
                                }
                            }
                        }
                        
                        // If no match found in configuration, try to determine from context
                        String determinedAction = determineActionFromContext(cleanLine, block.getType());
                        if (determinedAction != null) {
                            logger.fine("Determined action from context: " + determinedAction + " for block at " + formatLocation(blockLocation));
                            return determinedAction;
                        }
                        
                        // If still no match, return the line as is
                        logger.fine("Using raw sign text as action: " + cleanLine + " for block at " + formatLocation(blockLocation));
                        return cleanLine;
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Determines action from context clues and block type
     * 
     * @param signText The text from the sign
     * @param blockType The type of block
     * @return The determined action or null if undetermined
     */
    private String determineActionFromContext(String signText, Material blockType) {
        // Common action patterns
        String lowerText = signText.toLowerCase();
        
        // Event blocks (diamond)
        if (blockType == Material.DIAMOND_BLOCK) {
            if (lowerText.contains("join") || lowerText.contains("вход")) return "onJoin";
            if (lowerText.contains("leave") || lowerText.contains("выход")) return "onLeave";
            if (lowerText.contains("chat") || lowerText.contains("чат")) return "onChat";
            if (lowerText.contains("break") || lowerText.contains("сломать")) return "onBlockBreak";
            if (lowerText.contains("place") || lowerText.contains("поставить")) return "onBlockPlace";
        }
        
        // Action blocks (cobblestone)
        if (blockType == Material.COBBLESTONE) {
            if (lowerText.contains("message") || lowerText.contains("сообщение")) return "sendMessage";
            if (lowerText.contains("teleport") || lowerText.contains("телепорт")) return "teleport";
            if (lowerText.contains("give") || lowerText.contains("выдать")) return "giveItem";
            if (lowerText.contains("sound") || lowerText.contains("звук")) return "playSound";
        }
        
        // Condition blocks (planks)
        if (blockType == Material.OAK_PLANKS) {
            if (lowerText.contains("item") || lowerText.contains("предмет")) return "hasItem";
            if (lowerText.contains("op") || lowerText.contains("оператор")) return "isOp";
            if (lowerText.contains("near") || lowerText.contains("рядом")) return "isNearBlock";
        }
        
        return null; // Could not determine action from context
    }

    /**
     * Reads parameters from a container above a code block with enhanced parsing
     * Implements reference system-style: compilation from world with full structure scanning
     */
    private void readParametersFromContainer(Location blockLocation, CodeBlock codeBlock) {
        // Look for container (chest) above the block
        Location containerLocation = blockLocation.clone().add(0, 1, 0);
        Block containerBlock = containerLocation.getBlock();
        
        if (containerBlock.getState() instanceof Container) {
            Container container = (Container) containerBlock.getState();
            Inventory inventory = container.getInventory();
            
            // Convert ItemStacks to DataValue parameters
            convertItemStacksToParameters(inventory, codeBlock);
            
            logger.fine("Found container with parameters for block at " + blockLocation);
            
            // Add visual feedback for parameter reading
            containerLocation.getWorld().spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, 
                containerLocation.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 1.0);
        }
    }

    /**
     * Converts ItemStacks from container inventory to DataValue parameters in CodeBlock
     */
    private void convertItemStacksToParameters(Inventory inventory, CodeBlock codeBlock) {
        Map<String, DataValue> newParameters = new HashMap<>();
        int processedItems = 0;
        
        // Process each slot in the inventory
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            
            // Skip placeholder items
            if (isPlaceholderItem(item)) continue;
            
            // Try to determine parameter name for this slot
            String paramName = getParameterNameForSlot(codeBlock.getAction(), slot);
            if (paramName == null) {
                // Fallback: use generic slot-based parameter name
                paramName = "slot_" + slot;
            }
            
            // Convert ItemStack to DataValue
            DataValue paramValue = convertItemStackToDataValue(item);
            if (paramValue != null) {
                newParameters.put(paramName, paramValue);
                processedItems++;
            }
        }
        
        // Update CodeBlock parameters
        for (Map.Entry<String, DataValue> entry : newParameters.entrySet()) {
            codeBlock.setParameter(entry.getKey(), entry.getValue());
        }
        
        if (processedItems > 0) {
            logger.fine("Converted " + processedItems + " ItemStacks to DataValue parameters for block " + codeBlock.getAction());
        }
    }

    /**
     * Converts an ItemStack to a DataValue
     */
    private DataValue convertItemStackToDataValue(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return new AnyValue(null);
        }
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        String displayName = meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
        
        // Clean display name from color codes for processing
        String cleanName = ChatColor.stripColor(displayName).trim();
        
        // 1. Try to extract value from existing parameter items (our converted items)
        if (meta != null && meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.startsWith("§8Parameter: ")) {
                    // This is a parameter item we created - extract the value
                    return extractValueFromParameterItem(item, lore);
                }
            }
        }
        
        // 2. Try to detect type from material
        switch (item.getType()) {
            case PAPER:
                // Extract text from display name or use item name
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    return new TextValue("Текст");
                }
            
            case GOLD_NUGGET:
            case GOLD_INGOT:
                // Try to parse number from name or use amount
                if (!cleanName.isEmpty()) {
                    try {
                        String numberStr = cleanName.replaceAll("[^0-9.-]", "");
                        if (!numberStr.isEmpty()) {
                            return new NumberValue(Double.parseDouble(numberStr));
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return new NumberValue(item.getAmount());
            
            case LIME_DYE:
                return new BooleanValue(true);
            case RED_DYE:
                return new BooleanValue(false);
            
            case CHEST:
            case BARREL:
                // Consider these as lists or containers
                return new ListValue(new ArrayList<>());
            
            default:
                // For other items, create text value from name or material
                if (!cleanName.isEmpty()) {
                    return new TextValue(cleanName);
                } else {
                    // Use material name as text value
                    return new TextValue(item.getType().name().toLowerCase().replace("_", " "));
                }
        }
    }

    /**
     * Extracts value from a parameter item we created
     */
    private DataValue extractValueFromParameterItem(ItemStack item, List<String> lore) {
        // Look for "Value: " line in lore
        for (String line : lore) {
            String cleanLine = ChatColor.stripColor(line);
            if (cleanLine.startsWith("Value: ")) {
                String valueStr = cleanLine.substring(7); // Remove "Value: "
                
                // Check type from the previous line
                int index = lore.indexOf(line);
                if (index > 0) {
                    String typeLine = ChatColor.stripColor(lore.get(index - 1));
                    
                    if (typeLine.contains("Number")) {
                        try {
                            return new NumberValue(Double.parseDouble(valueStr));
                        } catch (NumberFormatException e) {
                            return new TextValue(valueStr);
                        }
                    } else if (typeLine.contains("Boolean")) {
                        return new BooleanValue("True".equalsIgnoreCase(valueStr));
                    } else if (typeLine.contains("List")) {
                        return new ListValue(new ArrayList<>());
                    }
                }
                
                // Default to text
                return new TextValue(valueStr);
            }
        }
        
        // Fallback
        return new TextValue(item.getType().name().toLowerCase());
    }

    /**
     * Gets parameter name for a specific slot based on action type
     */
    private String getParameterNameForSlot(String action, int slot) {
        // Action-specific parameter mapping based on coding_blocks.yml
        switch (action) {
            case "sendMessage":
                return slot == 0 ? "message" : "param_" + slot;
            case "teleport":
                return slot == 0 ? "coords" : "param_" + slot;
            case "giveItem":
                return switch (slot) {
                    case 0 -> "item";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "playSound":
                return switch (slot) {
                    case 0 -> "sound";
                    case 1 -> "volume";
                    case 2 -> "pitch";
                    default -> "param_" + slot;
                };
            case "effect":
                return switch (slot) {
                    case 0 -> "effect";
                    case 1 -> "duration";
                    case 2 -> "amplifier";
                    default -> "param_" + slot;
                };
            case "setVar":
            case "addVar":
            case "subVar":
            case "mulVar":
            case "divVar":
                return switch (slot) {
                    case 0 -> "var";
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "spawnMob":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "amount";
                    default -> "param_" + slot;
                };
            case "wait":
                return slot == 0 ? "ticks" : "param_" + slot;
            case "randomNumber":
                return switch (slot) {
                    case 0 -> "min";
                    case 1 -> "max";
                    case 2 -> "var";
                    default -> "param_" + slot;
                };
            case "setTime":
                return slot == 0 ? "time" : "param_" + slot;
            case "setWeather":
                return slot == 0 ? "weather" : "param_" + slot;
            case "command":
                return slot == 0 ? "command" : "param_" + slot;
            case "broadcast":
                return slot == 0 ? "message" : "param_" + slot;
            case "healPlayer":
                return slot == 0 ? "amount" : "param_" + slot;
            case "explosion":
                return switch (slot) {
                    case 0 -> "power";
                    case 1 -> "breakBlocks";
                    default -> "param_" + slot;
                };
            case "setBlock":
                return switch (slot) {
                    case 0 -> "material";
                    case 1 -> "coords";
                    default -> "param_" + slot;
                };
            // Variable conditions (unified handling)
            case "compareVariable":
                return switch (slot) {
                    case 0 -> "var1";
                    case 1 -> "operator";
                    case 2 -> "var2";
                    default -> "param_" + slot;
                };
            case "ifVarEquals":
            case "ifVarGreater":
            case "ifVarLess":
                return switch (slot) {
                    case 0 -> "variable"; // Legacy parameter name for backward compatibility
                    case 1 -> "value";
                    default -> "param_" + slot;
                };
            case "hasItem":
                return slot == 0 ? "item" : "param_" + slot;
            case "isNearBlock":
                return switch (slot) {
                    case 0 -> "block";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
            case "mobNear":
                return switch (slot) {
                    case 0 -> "mob";
                    case 1 -> "radius";
                    default -> "param_" + slot;
                };
        
            // Generic fallback
            default:
                return switch (slot) {
                    case 0 -> "message";
                    case 1 -> "amount";
                    case 2 -> "target";
                    case 3 -> "item";
                    case 4 -> "location";
                    default -> "param_" + slot;
                };
        }
    }

    /**
     * Checks if an ItemStack is a placeholder item
     */
    private boolean isPlaceholderItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                if (line.contains("placeholder") || line.contains("Placeholder")) {
                    return true;
                }
            }
        }
        
        return false;
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
    
    /**
     * Compiles all scripts in a world and generates executable code strings
     * This method mimics reference system's approach of converting world structures to code strings
     * 
     * @param world The world to compile
     * @return List of compiled code strings
     */
    public List<String> compileWorldToCodeStrings(World world) {
        List<String> compiledCode = new ArrayList<>();
        
        // Scan the world for code structures
        Map<Location, CodeBlock> scannedBlocks = scanWorldStructure(world);
        
        // Group blocks by Y level (line) for structured compilation
        Map<Integer, List<CodeBlock>> blocksByLine = new HashMap<>();
        
        for (Map.Entry<Location, CodeBlock> entry : scannedBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock block = entry.getValue();
            
            int yLevel = location.getBlockY();
            blocksByLine.computeIfAbsent(yLevel, k -> new ArrayList<>()).add(block);
        }
        
        // Process each line
        for (Map.Entry<Integer, List<CodeBlock>> lineEntry : blocksByLine.entrySet()) {
            int yLevel = lineEntry.getKey();
            List<CodeBlock> lineBlocks = lineEntry.getValue();
            
            // Sort blocks by X coordinate (left to right)
            lineBlocks.sort((a, b) -> {
                Location locA = null;
                Location locB = null;
                
                // Find locations for these blocks
                for (Map.Entry<Location, CodeBlock> blockEntry : scannedBlocks.entrySet()) {
                    if (blockEntry.getValue() == a) locA = blockEntry.getKey();
                    if (blockEntry.getValue() == b) locB = blockEntry.getKey();
                }
                
                if (locA != null && locB != null) {
                    return Integer.compare(locA.getBlockX(), locB.getBlockX());
                }
                return 0;
            });
            
            // Convert line to code string
            List<String> lineCode = new ArrayList<>();
            for (CodeBlock block : lineBlocks) {
                String function = getFunctionFromBlock(block);
                if (function != null && !function.isEmpty()) {
                    lineCode.add(function);
                }
            }
            
            if (!lineCode.isEmpty()) {
                // Join functions with "&" separator like reference system
                String lineResult = String.join("&", lineCode);
                compiledCode.add(lineResult);
            }
        }
        
        logger.info("Compiled " + compiledCode.size() + " lines of code from world: " + world.getName());
        return compiledCode;
    }

    /**
     * Converts a CodeBlock to its function representation
     * This mimics reference system's GetFunc_new.get() method
     * 
     * @param block The CodeBlock to convert
     * @return Function string representation
     */
    private String getFunctionFromBlock(CodeBlock block) {
        if (block == null) return null;
        
        String action = block.getAction();
        if (action == null || action.equals("NOT_SET")) return null;
        
        // Handle special cases like brackets
        if (block.getMaterial() == Material.PISTON || block.getMaterial() == Material.STICKY_PISTON) {
            if (block.getBracketType() == CodeBlock.BracketType.OPEN) {
                return "{";
            } else if (block.getBracketType() == CodeBlock.BracketType.CLOSE) {
                return "}";
            }
            return null;
        }
        
        // Handle event blocks (diamond)
        if (block.getMaterial() == Material.DIAMOND_BLOCK) {
            switch (action) {
                case "onJoin": return "joinEvent";
                case "onLeave": return "quitEvent";
                case "onChat": return "messageEvent";
                case "onBlockBreak": return "breakEvent";
                case "onBlockPlace": return "placeEvent";
                case "onPlayerMove": return "moveEvent";
                case "onPlayerDeath": return "playerDeathEvent";
                default: return action;
            }
        }
        
        // Handle action blocks (cobblestone)
        if (block.getMaterial() == Material.COBBLESTONE) {
            return action;
        }
        
        // Handle condition blocks (planks)
        if (block.getMaterial() == Material.OAK_PLANKS) {
            return action;
        }
        
        // Handle other block types
        return action;
    }

    /**
     * Saves compiled code to a configuration file like reference system's WorldCode system
     * 
     * @param worldId The world ID
     * @param codeLines The compiled code lines
     */
    public void saveCompiledCode(String worldId, List<String> codeLines) {
        // Save to WorldCode configuration like reference system's WorldCode system
        logger.info("Saving compiled code for world: " + worldId);
        logger.info("Code lines: " + codeLines.size());
        
        // Import and use WorldCode system
        com.megacreative.configs.WorldCode.setCode(worldId, codeLines);
        
        logger.info("Successfully saved compiled code to WorldCode configuration");
    }
}