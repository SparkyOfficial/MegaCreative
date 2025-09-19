package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.worlds.DevWorldGenerator;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Enhanced AutoConnectionManager with CodeBlock structure integration
 * Automatically connects blocks according to dev world lines and maintains proper nextBlock/children relationships
 * Integrates with BlockPlacementHandler for consistent CodeBlock management
 * Enhanced with improved block connection and script compilation logic.
 */
public class AutoConnectionManager implements Listener {
    
    // Constants for magic numbers to reduce cognitive complexity
    private static final int MAX_CHILD_SEARCH_LINES = 10;
    private static final int MAX_INDENTATION_LEVEL = 5;
    private static final double MAX_OWNER_DISTANCE = 16.0;
    private static final double LAST_RESORT_DISTANCE = 8.0;
    private static final int MAX_BLOCKS_PER_LINE = DevWorldGenerator.getBlocksPerLine();
    // Constants for duplicated values
    
    private static final float PARTICLE_SIZE = 1.0f;
    // Constants for script messages
    private static final String SCRIPT_COMPILATION_SUCCESS = "§a✓ Скрипт скомпилирован и создан для события: §f";
    private static final String SCRIPT_REMOVAL_SUCCESS = "Removed script for event block: ";
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    private final Map<UUID, List<CodeBlock>> playerScriptBlocks = new HashMap<>();
    private final Map<Location, Player> blockOwners = new HashMap<>(); // Track block owners
    
    public AutoConnectionManager(MegaCreative plugin, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
    }
    
    /**
     * Helper method to get world manager safely
     */
    private IWorldManager getWorldManager() {
        if (plugin == null || plugin.getServiceRegistry() == null) {
            return null;
        }
        return plugin.getServiceRegistry().getWorldManager();
    }
    
    /**
     * Helper method to get block placement handler safely
     */
    private BlockPlacementHandler getBlockPlacementHandler() {
        if (plugin == null || plugin.getServiceRegistry() == null) {
            return null;
        }
        return plugin.getServiceRegistry().getBlockPlacementHandler();
    }
    
    /**
     * Checks if a world is a development world
     */
    private boolean isDevWorld(World world) {
        String worldName = world.getName();
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative");
    }
    
    /**
     * Synchronizes with BlockPlacementHandler's CodeBlock map
     * This ensures both systems work with the same CodeBlock instances
     */
    public void synchronizeWithPlacementHandler(BlockPlacementHandler placementHandler) {
        Map<Location, CodeBlock> placementBlocks = placementHandler.getBlockCodeBlocks();
        
        // Sync existing blocks from placement handler
        for (Map.Entry<Location, CodeBlock> entry : placementBlocks.entrySet()) {
            Location location = entry.getKey();
            CodeBlock codeBlock = entry.getValue();
            
            if (!locationToBlock.containsKey(location)) {
                locationToBlock.put(location, codeBlock);
                // Auto-connect this block if it's in a dev world
                if (isDevWorld(location.getWorld())) {
                    autoConnectBlock(codeBlock, location);
                }
            }
        }
        
        if (plugin != null) {
            plugin.getLogger().fine("Synchronized " + placementBlocks.size() + " blocks with AutoConnectionManager");
        }
    }
    
    /**
     * Enhanced block placement handler with BlockPlacementHandler integration
     * Processes auto-connection AFTER BlockPlacementHandler has created the CodeBlock
     */
    @EventHandler(priority = EventPriority.MONITOR) // Use MONITOR to run after BlockPlacementHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        ItemStack itemInHand = event.getItemInHand();

        // Check if this is a dev world
        if (!isDevWorld(block.getWorld())) {
            handleNonDevWorld(block);
            return;
        }
        
        logBlockPlacement(player, location);
        
        // Check if this is a code block by checking if BlockPlacementHandler created a CodeBlock
        BlockPlacementHandler placementHandler = getBlockPlacementHandler();
        if (placementHandler == null || !placementHandler.hasCodeBlock(location)) {
            handleNonCodeBlock(location);
            return; // Not a code block or not handled by BlockPlacementHandler
        }

        // Get the CodeBlock that was created by BlockPlacementHandler
        CodeBlock codeBlock = placementHandler.getCodeBlock(location);
        if (codeBlock != null) {
            processCodeBlockPlacement(player, location, itemInHand, codeBlock);
        } else {
            logNullCodeBlock(location);
        }
    }
    
    /**
     * Handles the case when a block is placed in a non-dev world
     */
    private void handleNonDevWorld(Block block) {
        if (plugin != null) {
            plugin.getLogger().info("AutoConnectionManager: Block placement not in dev world: " + block.getWorld().getName());
        }
    }
    
    /**
     * Logs block placement information
     */
    private void logBlockPlacement(Player player, Location location) {
        if (plugin != null) {
            plugin.getLogger().info("AutoConnectionManager: Processing block placement by " + player.getName() + " at " + location);
        }
    }
    
    /**
     * Handles the case when a block is not a code block
     */
    private void handleNonCodeBlock(Location location) {
        if (plugin != null) {
            plugin.getLogger().info("AutoConnectionManager: Not a code block or not handled by BlockPlacementHandler at " + location);
        }
    }
    
    /**
     * Processes the placement of a code block
     */
    private void processCodeBlockPlacement(Player player, Location location, ItemStack itemInHand, CodeBlock codeBlock) {
        // Add to our tracking map
        locationToBlock.put(location, codeBlock);
        
        // Track block owner
        blockOwners.put(location, player);
        
        // Add to player's script blocks
        addBlockToPlayerScript(player, codeBlock);
        
        // Auto-connect with neighboring blocks
        autoConnectBlock(codeBlock, location);
        
        // If this is an event block, create a script and add it to the world
        if (isEventBlock(codeBlock)) {
            createAndAddScript(codeBlock, player, location);
        }
        
        // Get configuration for display name
        String displayName = itemInHand.hasItemMeta() ? org.bukkit.ChatColor.stripColor(itemInHand.getItemMeta().getDisplayName()) : "";
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByDisplayName(displayName);
        String blockName = config != null ? config.getDisplayName() : "Unknown Block";
        
        // Уменьшен спам - сообщение только важных событий
        logSuccessfulPlacement(player, location, blockName);
    }
    
    /**
     * Logs successful block placement
     */
    private void logSuccessfulPlacement(Player player, Location location, String blockName) {
        if (plugin != null) {
            plugin.getLogger().info("Block '" + blockName + "' placed and auto-connected at " + location + " for player " + player.getName());
            plugin.getLogger().fine("Auto-connected CodeBlock at " + location + " for player " + player.getName());
        }
    }
    
    /**
     * Logs when a CodeBlock is null
     */
    private void logNullCodeBlock(Location location) {
        if (plugin != null) {
            plugin.getLogger().warning("AutoConnectionManager: CodeBlock is null at " + location);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Location location = event.getBlock().getLocation();
        CodeBlock codeBlock = locationToBlock.get(location);
        
        if (codeBlock != null) {
            // Disconnect from neighboring blocks
            disconnectBlock(codeBlock, location);
            
            // Remove from our tracking
            locationToBlock.remove(location);
            
            // Remove block owner tracking
            blockOwners.remove(location);
            
            // Remove from player script
            removeBlockFromPlayerScript(event.getPlayer(), codeBlock);
            
            // If this is an event block, remove the corresponding script from the world
            if (isEventBlock(codeBlock)) {
                removeScript(codeBlock, location);
            }
            
            // Also ensure BlockPlacementHandler is synchronized
            BlockPlacementHandler placementHandler = getBlockPlacementHandler();
            if (placementHandler != null && plugin != null) {
                    plugin.getLogger().fine("CodeBlock disconnected at " + location);
                }
            
            
            event.getPlayer().sendMessage("§cБлок кода удален и отсоединён от цепочки!");
        }
    }
    
    /**
     * Enhanced automatic block connection with proper CodeBlock structure integration
     * Properly sets nextBlock and children relationships for execution flow
     * Enhanced with improved connection logic and error handling.
     */
    private void autoConnectBlock(CodeBlock codeBlock, Location location) {
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return;
        
        if (plugin != null) {
            plugin.getLogger().fine("Auto-connecting block at " + location + " (line " + line + ")");
        }
        
        // Step 1: Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null) {
                prevBlock.setNextBlock(codeBlock);
                if (plugin != null) {
                    plugin.getLogger().fine("Connected horizontal: " + prevLocation + " -> " + location);
                }
                
                // Add visual feedback for connection
                addConnectionEffect(prevLocation, location);
            }
        }
        
        // Step 2: Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null) {
                codeBlock.setNextBlock(nextBlock);
                if (plugin != null) {
                    plugin.getLogger().fine("Connected horizontal: " + location + " -> " + nextLocation);
                }
                
                // Add visual feedback for connection
                addConnectionEffect(location, nextLocation);
            }
        }
        
        // Step 3: Handle parent-child relationships (vertical connections)
        handleParentChildConnections(codeBlock, location, line);
        
        // Step 4: Update player script blocks
        updatePlayerScriptBlocks(codeBlock, location);
    }
    
    /**
     * Handles parent-child relationships for conditional blocks and loops
     * Creates proper hierarchical structure for execution flow
     * Enhanced with improved parent finding and connection logic.
     */
    private void handleParentChildConnections(CodeBlock codeBlock, Location location, int line) {
        // Check if this block should be a child of a parent block (indented blocks)
        if (location.getBlockX() > 0) {
            CodeBlock parentBlock = findParentBlock(location, line);
            if (parentBlock != null) {
                parentBlock.addChild(codeBlock);
                if (plugin != null) {
                    plugin.getLogger().fine("Added child relationship: parent at line " + line + " -> child at " + location);
                }
            }
        }
        
        // If this is a conditional/loop block at start of line, look for children in next lines
        if (location.getBlockX() == 0 && isControlBlock(codeBlock)) {
            connectChildBlocks(codeBlock, location);
        }
    }
    
    /**
     * Finds the parent block for a child block based on indentation and bracket balancing
     * Enhanced with bracket-aware parent finding using bracket balancing algorithm
     */
    private CodeBlock findParentBlock(Location childLocation, int childLine) {
        // Use bracket balancing algorithm to find the correct parent
        CodeBlock bracketParent = findParentUsingBracketBalancing(childLocation, childLine);
        if (bracketParent != null) {
            return bracketParent;
        }
        
        // Fallback to indentation-based parent finding
        return findParentByIndentation(childLocation, childLine);
    }
    
    /**
     * Finds parent using bracket balancing algorithm
     * Looks for the nearest unclosed opening bracket
     * Enhanced with improved bracket balancing logic.
     */
    private CodeBlock findParentUsingBracketBalancing(Location childLocation, int childLine) {
        int bracketBalance = 0;
        
        // Start from current line and move up and left
        for (int line = childLine; line >= 0; line--) {
            int lineZ = DevWorldGenerator.getZForCodeLine(line);
            
            // Scan from right to left in this line
            int startX = calculateStartX(line, childLine, childLocation);
            
            for (int x = startX; x >= 0; x--) {
                Location checkLocation = new Location(childLocation.getWorld(), x, childLocation.getBlockY(), lineZ);
                CodeBlock checkBlock = locationToBlock.get(checkLocation);
                
                if (checkBlock != null && checkBlock.isBracket()) {
                    CodeBlock parent = processBracketBlock(checkBlock, bracketBalance, childLocation, x, line);
                    if (parent != null) {
                        return parent;
                    }
                    bracketBalance = updateBracketBalance(checkBlock, bracketBalance);
                }
            }
        }
        
        return null; // No unmatched opening bracket found
    }
    
    /**
     * Process a bracket block and determine if it's a parent
     */
    private CodeBlock processBracketBlock(CodeBlock checkBlock, int bracketBalance, Location childLocation, int x, int line) {
        if (isClosingBracket(checkBlock)) {
            // Found closing bracket, no action needed here
            return null;
        } else if (isOpeningBracket(checkBlock)) {
            CodeBlock parent = handleOpeningBracket(checkBlock, bracketBalance);
            if (parent != null) {
                logBracketParentFound(childLocation, x, line);
                return parent;
            }
        }
        return null;
    }
    
    /**
     * Update bracket balance based on the block type
     */
    private int updateBracketBalance(CodeBlock checkBlock, int bracketBalance) {
        if (isClosingBracket(checkBlock)) {
            return bracketBalance + 1; // Found closing bracket, increment balance
        } else if (isOpeningBracket(checkBlock)) {
            return bracketBalance - 1; // Match this opening bracket with previous closing bracket
        }
        return bracketBalance;
    }
    
    /**
     * Calculates the starting X coordinate for scanning
     */
    private int calculateStartX(int line, int childLine, Location childLocation) {
        return (line == childLine) ? childLocation.getBlockX() - 1 : MAX_BLOCKS_PER_LINE - 1;
    }
    
    /**
     * Checks if a block is a closing bracket
     */
    private boolean isClosingBracket(CodeBlock block) {
        return block.getBracketType() == CodeBlock.BracketType.CLOSE;
    }
    
    /**
     * Checks if a block is an opening bracket
     */
    private boolean isOpeningBracket(CodeBlock block) {
        return block.getBracketType() == CodeBlock.BracketType.OPEN;
    }
    
    /**
     * Handles an opening bracket based on the current bracket balance
     */
    private CodeBlock handleOpeningBracket(CodeBlock block, int bracketBalance) {
        if (bracketBalance == 0) {
            // Found unmatched opening bracket - this is our parent!
            return block;
        }
        return null;
    }
    
    /**
     * Logs when a bracket parent is found
     */
    private void logBracketParentFound(Location childLocation, int x, int line) {
        if (plugin != null) {
            plugin.getLogger().fine("Found bracket parent at (" + x + ", " + line + ") for child at " + childLocation);
        }
    }
    
    /**
     * Traditional indentation-based parent finding (fallback method)
     * Enhanced with improved indentation logic.
     */
    private CodeBlock findParentByIndentation(Location childLocation, int childLine) {
        // Look for parent in previous lines with less indentation
        for (int parentLine = childLine - 1; parentLine >= 0; parentLine--) {
            int parentZ = DevWorldGenerator.getZForCodeLine(parentLine);
            
            // Look for blocks with less X coordinate (less indentation)
            for (int parentX = 0; parentX < childLocation.getBlockX(); parentX++) {
                Location parentLocation = new Location(childLocation.getWorld(), parentX, childLocation.getBlockY(), parentZ);
                CodeBlock parentBlock = locationToBlock.get(parentLocation);
                
                if (parentBlock != null && isControlBlock(parentBlock)) {
                    if (plugin != null) {
                        plugin.getLogger().fine("Found indentation parent at (" + parentX + ", " + parentLine + ") for child at " + childLocation);
                    }
                    return parentBlock;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a block is a control block that can have children
     * Uses the new BlockConfigService to determine this properly
     */
    private boolean isControlBlock(CodeBlock block) {
        String action = block.getAction();
        if (action == null) return false;
        
        // Get the block configuration from the service
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(action);
        if (config == null) return false;
        
        // Check if it's a control or event block
        return blockConfigService.isControlOrEventBlock(config.getType());
    }
    
    /**
     * Updates player script blocks and maintains execution order
     * Enhanced with improved sorting and error handling.
     */
    private void updatePlayerScriptBlocks(CodeBlock codeBlock, Location location) {
        // Find the player who owns this block using a more sophisticated approach
        Player owner = findBlockOwner(location);
        
        if (owner != null) {
            handleOwnedBlock(codeBlock, owner);
        } else {
            handleUnownedBlock(codeBlock);
        }
    }
    
    /**
     * Handles a block that has an owner
     */
    private void handleOwnedBlock(CodeBlock codeBlock, Player owner) {
        UUID playerId = owner.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        // Add to player's blocks if not already present
        if (!blocks.contains(codeBlock)) {
            blocks.add(codeBlock);
            sortAndLogBlocks(blocks, owner);
        }
    }
    
    /**
     * Sorts blocks and logs the update
     */
    private void sortAndLogBlocks(List<CodeBlock> blocks, Player owner) {
        // Sort blocks by location for proper execution order
        blocks.sort((a, b) -> {
            Location locA = getLocationForBlock(a);
            Location locB = getLocationForBlock(b);
            if (locA == null || locB == null) return 0;
            
            // Sort by Z (line) first, then by X (position in line)
            int lineCompare = Integer.compare(locA.getBlockZ(), locB.getBlockZ());
            if (lineCompare != 0) return lineCompare;
            return Integer.compare(locA.getBlockX(), locB.getBlockX());
        });
        
        if (plugin != null) {
            plugin.getLogger().fine("Updated player script blocks for: " + owner.getName());
        }
    }
    
    /**
     * Handles a block that doesn't have an owner
     */
    private void handleUnownedBlock(CodeBlock codeBlock) {
        // Fallback to the simplified approach if owner cannot be determined
        for (Map.Entry<UUID, List<CodeBlock>> entry : playerScriptBlocks.entrySet()) {
            List<CodeBlock> blocks = entry.getValue();
            if (blocks.contains(codeBlock)) {
                if (plugin != null) {
                    plugin.getLogger().fine("Updated player script blocks for: " + entry.getKey());
                }
                break;
            }
        }
    }
    
    /**
     * Looks for child blocks (indented blocks) for control structures
     * Enhanced with improved child finding logic.
     */
    private void connectChildBlocks(CodeBlock parentBlock, Location parentLocation) {
        int parentLine = DevWorldGenerator.getCodeLineFromZ(parentLocation.getBlockZ());
        
        logChildSearch(parentLine);
        
        // Look for indented blocks in subsequent lines (children)
        for (int childLine = parentLine + 1; childLine < parentLine + MAX_CHILD_SEARCH_LINES && childLine < DevWorldGenerator.getLinesCount(); childLine++) {
            int childZ = DevWorldGenerator.getZForCodeLine(childLine);
            
            // Check different indentation levels (X > 0 means indented)
            for (int childX = 1; childX <= MAX_INDENTATION_LEVEL; childX++) {
                Location childLocation = new Location(parentLocation.getWorld(), childX, parentLocation.getBlockY(), childZ);
                CodeBlock childBlock = locationToBlock.get(childLocation);
                
                if (childBlock != null) {
                    connectChildBlock(parentBlock, parentLine, childLine, childX, childBlock, childLocation);
                }
            }
            
            // If we find a non-indented block, stop looking for children
            Location endLocation = new Location(parentLocation.getWorld(), 0, parentLocation.getBlockY(), childZ);
            if (locationToBlock.containsKey(endLocation)) {
                handleNonIndentedBlock(childLine);
                break;
            }
        }
    }
    
    /**
     * Logs the start of child search
     */
    private void logChildSearch(int parentLine) {
        if (plugin != null) {
            plugin.getLogger().fine("Looking for child blocks for parent at line " + parentLine);
        }
    }
    
    /**
     * Connects a child block to its parent
     */
    private void connectChildBlock(CodeBlock parentBlock, int parentLine, int childLine, int childX, CodeBlock childBlock, Location childLocation) {
        parentBlock.addChild(childBlock);
        if (plugin != null) {
            plugin.getLogger().fine("Connected child: parent line " + parentLine + " -> child at (" + childX + ", " + childLine + ")");
        }
        buildBlockChain(childBlock, childLocation); // Recursively build child chain
    }
    
    /**
     * Handles when a non-indented block is found
     */
    private void handleNonIndentedBlock(int childLine) {
        if (plugin != null) {
            plugin.getLogger().fine("Found non-indented block at line " + childLine + ", ending child search");
        }
    }
    
    /**
     * Adds a block to a player's script blocks
     */
    private void addBlockToPlayerScript(Player player, CodeBlock block) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        if (!blocks.contains(block)) {
            blocks.add(block);
            if (plugin != null) {
                plugin.getLogger().fine("Added CodeBlock to player " + player.getName() + " script. Total blocks: " + blocks.size());
            }
        }
    }
    
    /**
     * Removes a block from a player's script blocks
     */
    private void removeBlockFromPlayerScript(Player player, CodeBlock block) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.get(playerId);
        
        if (blocks != null && blocks.remove(block) && plugin != null) {
                plugin.getLogger().fine("Removed CodeBlock from player " + player.getName() + " script. Remaining blocks: " + blocks.size());
            }
        
    }
    
    /**
     * Checks if a block is an event block
     */
    public boolean isEventBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) return false;
        
        // Get the block configuration
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) return false;
        
        // Check if it's an event block
        return "EVENT".equals(config.getType());
    }
    
    /**
     * Creates a script from an event block and adds it to the world
     * Enhanced with improved script creation and error handling.
     */
    public void createAndAddScript(CodeBlock eventBlock, Player player, Location location) {
        try {
            // Use the new compilation method to create a complete script
            CodeScript script = compileScriptFromEventBlock(eventBlock, location);
            if (script == null) {
                player.sendMessage("§cОшибка при создании скрипта!");
                return;
            }
            
            // Find the creative world using service registry
            com.megacreative.interfaces.IWorldManager worldManager = getWorldManager();
            if (worldManager == null) {
                plugin.getLogger().warning("World manager not available");
                return;
            }
            
            handleScriptCreation(eventBlock, location, script, worldManager, player);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create script for event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Handles the script creation and addition to the world
     */
    private void handleScriptCreation(CodeBlock eventBlock, Location location, CodeScript script, 
                                  com.megacreative.interfaces.IWorldManager worldManager, Player player) {
        com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
        if (creativeWorld != null) {
            // Add the script to the world
            addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
            
            player.sendMessage(SCRIPT_COMPILATION_SUCCESS + eventBlock.getAction());
            plugin.getLogger().fine("Compiled and added script for event block: " + eventBlock.getAction() + " in world: " + creativeWorld.getName());
        } else {
            plugin.getLogger().warning("Could not find creative world for location: " + location);
        }
    }
    
    /**
     * Adds a script to the world and handles duplicates
     */
    private void addScriptToWorld(CodeBlock eventBlock, CodeScript script, 
                              com.megacreative.models.CreativeWorld creativeWorld, 
                              com.megacreative.interfaces.IWorldManager worldManager) {
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts == null) {
            scripts = new ArrayList<>();
            creativeWorld.setScripts(scripts);
        }
        
        // Remove any existing script with the same root block action to avoid duplicates
        scripts.removeIf(existingScript -> 
            existingScript.getRootBlock() != null && 
            eventBlock.getAction().equals(existingScript.getRootBlock().getAction()));
        
        scripts.add(script);
        
        // Save the creative world to persist the script
        worldManager.saveWorld(creativeWorld);
    }
    
    /**
     * Removes a script corresponding to an event block from the world
     */
    public void removeScript(CodeBlock eventBlock, Location location) {
        try {
            // Find the creative world using service registry
            com.megacreative.interfaces.IWorldManager worldManager = getWorldManager();
            if (worldManager == null) {
                plugin.getLogger().warning("World manager not available");
                return;
            }
            
            handleScriptRemoval(eventBlock, location, worldManager);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove script for event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Handles the script removal from the world
     */
    private void handleScriptRemoval(CodeBlock eventBlock, Location location, 
                                com.megacreative.interfaces.IWorldManager worldManager) {
        com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
        if (creativeWorld != null) {
            // Remove the script from the world
            removeScriptFromWorld(eventBlock, creativeWorld, worldManager);
        } else {
            plugin.getLogger().warning("Could not find creative world for location: " + location);
        }
    }
    
    /**
     * Removes a script from the world
     */
    private void removeScriptFromWorld(CodeBlock eventBlock, 
                                  com.megacreative.models.CreativeWorld creativeWorld, 
                                  com.megacreative.interfaces.IWorldManager worldManager) {
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts != null) {
            // Find and remove the script that corresponds to this event block
            boolean removed = scripts.removeIf(script -> 
                script.getRootBlock() != null && 
                eventBlock.getAction().equals(script.getRootBlock().getAction())
            );
            
            if (removed) {
                // Save the creative world to persist the change
                worldManager.saveWorld(creativeWorld);
                plugin.getLogger().fine(SCRIPT_REMOVAL_SUCCESS + eventBlock.getAction() + " from world: " + creativeWorld.getName());
            }
        }
    }
    
    /**
     * Cleans up all player blocks for a specific player
     */
    public void cleanupPlayerBlocks(Player player) {
        playerScriptBlocks.remove(player.getUniqueId());
    }
    
    /**
     * Shuts down the AutoConnectionManager and cleans up resources
     */
    public void shutdown() {
        // Clear all block references
        locationToBlock.clear();
        playerScriptBlocks.clear();
    }

    
    /**
     * Очищает все связи для мира
     */
    public void clearWorldConnections(World world) {
        locationToBlock.entrySet().removeIf(entry -> entry.getKey().getWorld().equals(world));
    }
    
    /**
     * Получает все блоки в мире
     */
    public Map<Location, CodeBlock> getWorldBlocks(World world) {
        Map<Location, CodeBlock> worldBlocks = new HashMap<>();
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        return worldBlocks;
    }
    
    /**
     * Rebuilds all connections for blocks in a specific world
     * Useful for initializing connections when loading existing worlds
     * Enhanced with improved connection rebuilding logic.
     */
    public void rebuildWorldConnections(World world) {
        plugin.getLogger().fine("Rebuilding connections for world: " + world.getName());
        
        // Get all blocks in the world
        Map<Location, CodeBlock> worldBlocks = getWorldBlocks(world);
        
        // Clear existing connections
        for (CodeBlock block : worldBlocks.values()) {
            block.setNextBlock(null);
            block.getChildren().clear();
        }
        
        // Rebuild connections
        for (Map.Entry<Location, CodeBlock> entry : worldBlocks.entrySet()) {
            autoConnectBlock(entry.getValue(), entry.getKey());
        }
        
        plugin.getLogger().fine("Rebuilt connections for " + worldBlocks.size() + " blocks");
    }
    
    /**
     * Forces synchronization with BlockPlacementHandler and rebuilds connections
     */
    public void forceSynchronization() {
        BlockPlacementHandler placementHandler = getBlockPlacementHandler();
        if (placementHandler != null) {
            synchronizeWithPlacementHandler(placementHandler);
            
            // Rebuild connections for all loaded worlds
            if (plugin != null) {
                for (World world : plugin.getServer().getWorlds()) {
                    if (isDevWorld(world)) {
                        rebuildWorldConnections(world);
                    }
                }
            }
        }
    }
    
    /**
     * Gets connection statistics for debugging
     */
    public String getConnectionStats() {
        int totalBlocks = locationToBlock.size();
        int connectedBlocks = 0;
        int parentBlocks = 0;
        int childBlocks = 0;
        
        for (CodeBlock block : locationToBlock.values()) {
            if (block.getNextBlock() != null) {
                connectedBlocks++;
            }
            if (!block.getChildren().isEmpty()) {
                parentBlocks++;
                childBlocks += block.getChildren().size();
            }
        }
        
        return String.format("Blocks: %d, Connected: %d, Parents: %d, Total Children: %d", 
                            totalBlocks, connectedBlocks, parentBlocks, childBlocks);
    }
    
    /**
     * Reloads block configuration
     */
    public void reloadBlockConfig() {
        blockConfigService.reload();
        plugin.getLogger().fine("Block configuration reloaded");
    }
    
    /**
     * Recompiles all scripts in a world
     * This should be called when the world is loaded or when significant changes are made
     */
    public void recompileWorldScripts(org.bukkit.World world) {
        if (plugin != null) {
            plugin.getLogger().fine("Recompiling all scripts for world: " + world.getName());
        }
        
        // Use service registry to get world manager
        IWorldManager worldManager = getWorldManager();
        if (worldManager == null) {
            handleMissingWorldManager();
            return;
        }
        
        com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
        if (creativeWorld == null) return;
        
        // Process scripts for the world
        ScriptCompilationResult result = processWorldScripts(world, creativeWorld);
        
        // Update the creative world with new scripts
        creativeWorld.setScripts(result.getScripts());
        worldManager.saveWorld(creativeWorld);
        
        if (plugin != null) {
            plugin.getLogger().fine("Recompiled " + result.getScriptCount() + " scripts for world: " + world.getName() + " with " + result.getErrorCount() + " errors");
        }
    }
    
    /**
     * Handles the case when world manager is not available
     */
    private void handleMissingWorldManager() {
        if (plugin != null) {
            plugin.getLogger().warning("World manager not available");
        }
    }
    
    /**
     * Processes all scripts in a world
     */
    private ScriptCompilationResult processWorldScripts(org.bukkit.World world) {
        // Clear existing scripts
        List<CodeScript> newScripts = new ArrayList<>();
        
        // Find all event blocks in the world and compile scripts from them
        int scriptCount = 0;
        int errorCount = 0;
        
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (!entry.getKey().getWorld().equals(world)) continue;
            
            CodeBlock block = entry.getValue();
            if (isEventBlock(block)) {
                ScriptCompilationAttempt attempt = attemptScriptCompilation(block, entry.getKey());
                if (attempt.wasSuccessful()) {
                    newScripts.add(attempt.getCompiledScript());
                    scriptCount++;
                    logSuccessfulCompilation(attempt.getCompiledScript());
                } else {
                    errorCount++;
                    logCompilationError(entry.getKey());
                }
            }
        }
        
        return new ScriptCompilationResult(newScripts, scriptCount, errorCount);
    }
    
    /**
     * Attempts to compile a script from an event block
     */
    private ScriptCompilationAttempt attemptScriptCompilation(CodeBlock block, Location location) {
        try {
            CodeScript compiledScript = compileScriptFromEventBlock(block, location);
            return new ScriptCompilationAttempt(compiledScript, compiledScript != null);
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().severe("Error compiling script from event block at " + location + ": " + e.getMessage());
                e.printStackTrace();
            }
            return new ScriptCompilationAttempt(null, false);
        }
    }
    
    /**
     * Logs successful script compilation
     */
    private void logSuccessfulCompilation(CodeScript compiledScript) {
        if (plugin != null) {
            plugin.getLogger().fine("Successfully compiled script: " + compiledScript.getName());
        }
    }
    
    /**
     * Logs compilation error
     */
    private void logCompilationError(Location location) {
        if (plugin != null) {
            plugin.getLogger().warning("Failed to compile script from event block at " + location);
        }
    }
    
    /**
     * Helper class to hold script compilation results
     */
    private static class ScriptCompilationResult {
        private final List<CodeScript> scripts;
        private final int scriptCount;
        private final int errorCount;
        
        public ScriptCompilationResult(List<CodeScript> scripts, int scriptCount, int errorCount) {
            this.scripts = scripts;
            this.scriptCount = scriptCount;
            this.errorCount = errorCount;
        }
        
        public List<CodeScript> getScripts() {
            return scripts;
        }
        
        public int getScriptCount() {
            return scriptCount;
        }
        
        public int getErrorCount() {
            return errorCount;
        }
    }
    
    /**
     * Helper class to hold script compilation attempt results
     */
    private static class ScriptCompilationAttempt {
        private final CodeScript compiledScript;
        private final boolean successful;
        
        public ScriptCompilationAttempt(CodeScript compiledScript, boolean successful) {
            this.compiledScript = compiledScript;
            this.successful = successful;
        }
        
        public CodeScript getCompiledScript() {
            return compiledScript;
        }
        
        public boolean wasSuccessful() {
            return successful;
        }
    }
    
    /**
     * Compiles a complete script from an event block by following all connections
     * This implements the "compilation" process mentioned in the roadmap
     * Enhanced with improved script compilation logic.
     */
    public CodeScript compileScriptFromEventBlock(CodeBlock eventBlock, Location eventLocation) {
        try {
            // Create the root script
            CodeScript script = new CodeScript(eventBlock);
            script.setName("Compiled Script for " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
            script.setEnabled(true);
            script.setType(CodeScript.ScriptType.EVENT);
            
            plugin.getLogger().fine("Starting compilation of script from event block: " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
            
            // Build the complete block chain by following connections
            buildBlockChain(eventBlock, eventLocation);
            
            plugin.getLogger().fine("Successfully compiled script from event block: " + eventBlock.getAction() + " with connected blocks");
            return script;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to compile script from event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return null;
        }
    }
    
    /**
     * Builds the complete block chain by following nextBlock and children connections
     * Enhanced with improved chain building logic.
     */
    private void buildBlockChain(CodeBlock currentBlock, Location currentLocation) {
        if (currentBlock == null || currentLocation == null) return;
        
        // Follow the next block in the chain
        Location nextLocation = getNextLocationInLine(currentLocation);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null) {
                currentBlock.setNextBlock(nextBlock);
                buildBlockChain(nextBlock, nextLocation); // Recursively build chain
            }
        }
        
        // Build child blocks (for conditionals, loops)
        if (isControlBlock(currentBlock)) {
            buildChildBlocks(currentBlock, currentLocation);
        }
    }
    
    /**
     * Builds child blocks for control structures
     * Enhanced with improved child building logic.
     */
    private void buildChildBlocks(CodeBlock parentBlock, Location parentLocation) {
        int parentLine = DevWorldGenerator.getCodeLineFromZ(parentLocation.getBlockZ());
        
        // Look for indented blocks in subsequent lines (children)
        for (int childLine = parentLine + 1; childLine < parentLine + MAX_CHILD_SEARCH_LINES && childLine < DevWorldGenerator.getLinesCount(); childLine++) {
            int childZ = DevWorldGenerator.getZForCodeLine(childLine);
            
            // Check different indentation levels (X > 0 means indented)
            for (int childX = 1; childX <= MAX_INDENTATION_LEVEL; childX++) {
                Location childLocation = new Location(parentLocation.getWorld(), childX, parentLocation.getBlockY(), childZ);
                CodeBlock childBlock = locationToBlock.get(childLocation);
                
                if (childBlock != null) {
                    parentBlock.addChild(childBlock);
                    buildBlockChain(childBlock, childLocation); // Recursively build child chain
                }
            }
            
            // If we find a non-indented block, stop looking for children
            Location endLocation = new Location(parentLocation.getWorld(), 0, parentLocation.getBlockY(), childZ);
            if (locationToBlock.containsKey(endLocation)) {
                break;
            }
        }
    }
    
    /**
     * Adds a code block to the location tracking map
     * Used during world hydration to register existing blocks
     */
    public void addCodeBlock(Location location, CodeBlock codeBlock) {
        if (!locationToBlock.containsKey(location)) {
            locationToBlock.put(location, codeBlock);
            plugin.getLogger().fine("Added CodeBlock to tracking at " + location);
        }
    }
    
    /**
     * 🎆 ENHANCED: Adds visual effects for block connections
     * Implements reference system-style: visual code construction with feedback
     */
    private void addConnectionEffect(Location from, Location to) {
        // Create a beam of particles between connected blocks
        org.bukkit.World world = from.getWorld();
        
        // Calculate direction and distance
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (distance > 0) {
            // Normalize direction
            dx /= distance;
            dy /= distance;
            dz /= distance;
            
            // Create particle beam
            // Cast distance to int for loop iteration
            for (int i = 0; i < (int)(distance * 2); i++) {
                Location particleLoc = from.clone().add(
                    dx * i * 0.5, 
                    dy * i * 0.5, 
                    dz * i * 0.5
                );
                
                world.spawnParticle(org.bukkit.Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 255), PARTICLE_SIZE));
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
     * Gets all available materials for code blocks
     */
    public Set<Material> getCodeBlockMaterials() {
        return blockConfigService.getCodeBlockMaterials();
    }
    
    /**
     * Disconnects a block from its neighbors
     * Enhanced with improved disconnection logic and error handling.
     */
    public void disconnectBlock(CodeBlock codeBlock, Location location) {
        if (codeBlock == null || location == null) {
            return;
        }
        
        try {
            // Get the line number for this location
            int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
            if (line == -1) return;
            
            handleBlockDisconnection(codeBlock, location);
        } catch (Exception e) {
            if (plugin != null) {
                plugin.getLogger().warning("Error disconnecting block at " + location + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles the disconnection of a block from its neighbors
     */
    private void handleBlockDisconnection(CodeBlock codeBlock, Location location) {
        // Disconnect from previous block in the same line
        disconnectFromPreviousBlock(codeBlock, location);
        
        // Disconnect from next block in the same line
        disconnectFromNextBlock(codeBlock, location);
        
        // Disconnect from parent blocks
        disconnectFromParentBlocks(codeBlock);
        
        // Remove from our tracking
        locationToBlock.remove(location);
        
        if (plugin != null) {
            plugin.getLogger().fine("Disconnected block at " + location);
        }
    }
    
    /**
     * Disconnects a block from its previous block
     */
    private void disconnectFromPreviousBlock(CodeBlock codeBlock, Location location) {
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null && prevBlock.getNextBlock() == codeBlock) {
                prevBlock.setNextBlock(null);
                if (plugin != null) {
                    plugin.getLogger().fine("Disconnected previous block at " + prevLocation);
                }
            }
        }
    }
    
    /**
     * Disconnects a block from its next block
     */
    private void disconnectFromNextBlock(CodeBlock codeBlock, Location location) {
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null && codeBlock.getNextBlock() == nextBlock) {
                codeBlock.setNextBlock(null);
                if (plugin != null) {
                    plugin.getLogger().fine("Disconnected next block at " + nextLocation);
                }
            }
        }
    }
    
    /**
     * Disconnects a block from its parent blocks
     */
    private void disconnectFromParentBlocks(CodeBlock codeBlock) {
        // Find all blocks that have this block as a child and remove it
        for (CodeBlock parentBlock : new ArrayList<>(locationToBlock.values())) {
            if (parentBlock.getChildren().contains(codeBlock)) {
                parentBlock.getChildren().remove(codeBlock);
                if (plugin != null) {
                    plugin.getLogger().fine("Removed child relationship from parent block at " + getLocationForBlock(parentBlock));
                }
            }
        }
    }
    
    /**
     * Gets the previous location in the same line
     */
    public Location getPreviousLocationInLine(Location location) {
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
    public Location getNextLocationInLine(Location location) {
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
     * Finds the owner of a block
     */
    public Player findBlockOwner(Location location) {
        if (location == null || plugin == null) return null;
        
        // Track which player placed each block using a mapping
        if (blockOwners.containsKey(location)) {
            return blockOwners.get(location);
        }
        
        // Try to find the owner by checking nearby players within a reasonable distance
        Player closestPlayer = findClosestCreativePlayer(location);
        
        // If we found a nearby creative player, return them
        if (closestPlayer != null) {
            return closestPlayer;
        }
        
        // Last resort: find any player, but only if very close
        return findLastResortPlayer(location);
    }
    
    /**
     * Finds the closest creative player to the location
     */
    private Player findClosestCreativePlayer(Location location) {
        Player closestPlayer = null;
        double closestDistance = MAX_OWNER_DISTANCE; // Max distance in blocks
        
        for (Player player : location.getWorld().getPlayers()) {
            // Only consider players in creative mode
            if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
                double distance = player.getLocation().distanceSquared(location);
                // Cast one of the operands to double for proper comparison
                if (distance < (double)(closestDistance * closestDistance) && distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = player;
                }
            }
        }
        
        return closestPlayer;
    }
    
    /**
     * Finds a player as a last resort option
     */
    private Player findLastResortPlayer(Location location) {
        double lastResortDistance = LAST_RESORT_DISTANCE;
        for (Player player : location.getWorld().getPlayers()) {
            double distance = player.getLocation().distanceSquared(location);
            // Cast one of the operands to double for proper comparison
            if (distance < (double)(lastResortDistance * lastResortDistance)) {
                return player;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the location for a block
     */
    public Location getLocationForBlock(CodeBlock block) {
        if (block == null) return null;
        
        // Search through our location mapping to find the location for this block
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                return entry.getKey().clone(); // Return a clone to prevent external modification
            }
        }
        
        return null;
    }

}