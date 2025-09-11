package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.worlds.DevWorldGenerator;
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
 */
public class AutoConnectionManager implements Listener {
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    private final Map<UUID, List<CodeBlock>> playerScriptBlocks = new HashMap<>();
    
    public AutoConnectionManager(MegaCreative plugin, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
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
        
        plugin.getLogger().fine("Synchronized " + placementBlocks.size() + " blocks with AutoConnectionManager");
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
        if (!isDevWorld(block.getWorld())) return;
        
        // Check if this is a code block by checking if BlockPlacementHandler created a CodeBlock
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null || !placementHandler.hasCodeBlock(location)) {
            return; // Not a code block or not handled by BlockPlacementHandler
        }

        // Get the CodeBlock that was created by BlockPlacementHandler
        CodeBlock codeBlock = placementHandler.getCodeBlock(location);
        if (codeBlock != null) {
            // Add to our tracking map
            locationToBlock.put(location, codeBlock);
            
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
            
            player.sendMessage("§aБлок §f" + blockName + "§a установлен и автоматически подключен!");
            plugin.getLogger().fine("Auto-connected CodeBlock at " + location + " for player " + player.getName());
        }
    }
    
    /**
     * Enhanced block break handler with proper disconnection and synchronization
     */
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
            
            // Remove from player script
            removeBlockFromPlayerScript(event.getPlayer(), codeBlock);
            
            // If this is an event block, remove the corresponding script from the world
            if (isEventBlock(codeBlock)) {
                removeScript(codeBlock, location);
            }
            
            // Also ensure BlockPlacementHandler is synchronized
            BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
            if (placementHandler != null) {
                // The BlockPlacementHandler should handle this in its own event handler
                plugin.getLogger().fine("CodeBlock disconnected at " + location);
            }
            
            event.getPlayer().sendMessage("§cБлок кода удален и отсоединен от цепочки!");
        }
    }
    
    /**
     * Enhanced automatic block connection with proper CodeBlock structure integration
     * Properly sets nextBlock and children relationships for execution flow
     */
    private void autoConnectBlock(CodeBlock codeBlock, Location location) {
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return;
        
        plugin.getLogger().fine("Auto-connecting block at " + location + " (line " + line + ")");
        
        // Step 1: Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null) {
                prevBlock.setNextBlock(codeBlock);
                plugin.getLogger().fine("Connected horizontal: " + prevLocation + " -> " + location);
            }
        }
        
        // Step 2: Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null) {
                codeBlock.setNextBlock(nextBlock);
                plugin.getLogger().fine("Connected horizontal: " + location + " -> " + nextLocation);
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
     */
    private void handleParentChildConnections(CodeBlock codeBlock, Location location, int line) {
        // Check if this block should be a child of a parent block (indented blocks)
        if (location.getBlockX() > 0) {
            CodeBlock parentBlock = findParentBlock(location, line);
            if (parentBlock != null) {
                parentBlock.addChild(codeBlock);
                plugin.getLogger().fine("Added child relationship: parent at line " + line + " -> child at " + location);
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
     */
    private CodeBlock findParentUsingBracketBalancing(Location childLocation, int childLine) {
        int bracketBalance = 0;
        
        // Start from current line and move up and left
        for (int line = childLine; line >= 0; line--) {
            int lineZ = DevWorldGenerator.getZForCodeLine(line);
            
            // Scan from right to left in this line
            int startX = (line == childLine) ? childLocation.getBlockX() - 1 : DevWorldGenerator.getBlocksPerLine() - 1;
            
            for (int x = startX; x >= 0; x--) {
                Location checkLocation = new Location(childLocation.getWorld(), x, childLocation.getBlockY(), lineZ);
                CodeBlock checkBlock = locationToBlock.get(checkLocation);
                
                if (checkBlock != null && checkBlock.isBracket()) {
                    if (checkBlock.getBracketType() == CodeBlock.BracketType.CLOSE) {
                        bracketBalance++; // Found closing bracket, increment balance
                    } else if (checkBlock.getBracketType() == CodeBlock.BracketType.OPEN) {
                        if (bracketBalance == 0) {
                            // Found unmatched opening bracket - this is our parent!
                            plugin.getLogger().fine("Found bracket parent at (" + x + ", " + line + ") for child at " + childLocation);
                            return checkBlock;
                        } else {
                            bracketBalance--; // Match this opening bracket with previous closing bracket
                        }
                    }
                }
            }
        }
        
        return null; // No unmatched opening bracket found
    }
    
    /**
     * Traditional indentation-based parent finding (fallback method)
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
                    plugin.getLogger().fine("Found indentation parent at (" + parentX + ", " + parentLine + ") for child at " + childLocation);
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
     */
    private void updatePlayerScriptBlocks(CodeBlock codeBlock, Location location) {
        // Find the player who owns this block (simplified approach)
        for (Map.Entry<UUID, List<CodeBlock>> entry : playerScriptBlocks.entrySet()) {
            List<CodeBlock> blocks = entry.getValue();
            
            // Add to player's blocks if not already present
            if (!blocks.contains(codeBlock)) {
                blocks.add(codeBlock);
                
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
                
                plugin.getLogger().fine("Updated player script blocks for: " + entry.getKey());
                break;
            }
        }
    }
    
    /**
     * Enhanced child block connection with proper hierarchy management
     * Connects child blocks for conditionals and loops with better logic
     */
    private void connectChildBlocks(CodeBlock parentBlock, Location parentLocation) {
        int parentLine = DevWorldGenerator.getCodeLineFromZ(parentLocation.getBlockZ());
        
        plugin.getLogger().fine("Looking for child blocks for parent at line " + parentLine);
        
        // Look for indented blocks in subsequent lines
        for (int childLine = parentLine + 1; childLine < parentLine + 10 && childLine < DevWorldGenerator.getLinesCount(); childLine++) {
            int childZ = DevWorldGenerator.getZForCodeLine(childLine);
            
            // Check different indentation levels (X > 0 means indented)
            boolean foundChildInLine = false;
            
            for (int childX = 1; childX <= 5; childX++) { // Check indentation levels 1-5
                Location childLocation = new Location(parentLocation.getWorld(), childX, parentLocation.getBlockY(), childZ);
                CodeBlock childBlock = locationToBlock.get(childLocation);
                
                if (childBlock != null) {
                    // Only add as child if not already connected
                    if (!parentBlock.getChildren().contains(childBlock)) {
                        parentBlock.addChild(childBlock);
                        plugin.getLogger().fine("Connected child: parent line " + parentLine + " -> child at (" + childX + ", " + childLine + ")");
                        foundChildInLine = true;
                    }
                }
            }
            
            // If no indented blocks found in this line and it's not empty, stop searching
            // (This handles the case where the conditional/loop block ends)
            if (!foundChildInLine) {
                // Check if there's any block at X=0 (non-indented) which would end the child section
                Location endLocation = new Location(parentLocation.getWorld(), 0, parentLocation.getBlockY(), childZ);
                if (locationToBlock.containsKey(endLocation)) {
                    plugin.getLogger().fine("Found non-indented block at line " + childLine + ", ending child search");
                    break;
                }
            }
        }
    }
    
    /**
     * Отключает блок от соседних блоков
     */
    private void disconnectBlock(CodeBlock codeBlock, Location location) {
        // Находим предыдущий блок и обновляем его ссылку
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null && prevBlock.getNextBlock() == codeBlock) {
                // Ищем следующий блок для переподключения
                CodeBlock nextBlock = codeBlock.getNextBlock();
                prevBlock.setNextBlock(nextBlock);
            }
        }
        
        // Удаляем из дочерних блоков родительского блока
        for (CodeBlock block : locationToBlock.values()) {
            block.getChildren().remove(codeBlock);
        }
    }
    
    /**
     * Получает предыдущую позицию в той же линии
     */
    private Location getPreviousLocationInLine(Location location) {
        int prevX = location.getBlockX() - 1;
        if (prevX < 0) return null;
        
        return new Location(location.getWorld(), prevX, location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Получает следующую позицию в той же линии
     */
    private Location getNextLocationInLine(Location location) {
        int nextX = location.getBlockX() + 1;
        if (nextX >= DevWorldGenerator.getBlocksPerLine()) return null;
        
        return new Location(location.getWorld(), nextX, location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Создает CodeBlock на основе материала блока
     */
    private CodeBlock createCodeBlockFromMaterial(Material material, Location location) {
        String action = determineActionFromMaterial(material);
        if (action == null) return null;
        
        CodeBlock codeBlock = new CodeBlock(material, action);
        // Note: We're removing the plugin dependency from CodeBlock in a later step
        
        return codeBlock;
    }
    
    /**
     * Определяет действие блока на основе его материала
     * Теперь использует конфигурацию из coding_blocks.yml
     */
    private String determineActionFromMaterial(Material material) {
        // Get the first block config for this material as a fallback
        BlockConfigService.BlockConfig config = blockConfigService.getFirstBlockConfig(material);
        return config != null ? config.getId() : null;
    }
    
    /**
     * Gets the block type for a material using the new configuration system
     */
    private String getBlockType(Material material) {
        BlockConfigService.BlockConfig config = blockConfigService.getFirstBlockConfig(material);
        return config != null ? config.getType() : "ACTION";
    }
    
    /**
     * Checks if a block type is a control or event block using the new configuration system
     */
    private boolean isControlOrEventBlock(String blockType) {
        return blockConfigService.isControlOrEventBlock(blockType);
    }
    
    /**
     * Gets the location for a CodeBlock by searching through the location map
     */
    private Location getLocationForBlock(CodeBlock targetBlock) {
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue().equals(targetBlock)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Enhanced block addition to player script tracking
     * Maintains proper ordering and relationships
     */
    private void addBlockToPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.computeIfAbsent(playerId, k -> new ArrayList<>());
        
        if (!blocks.contains(codeBlock)) {
            blocks.add(codeBlock);
            
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
            
            plugin.getLogger().fine("Added CodeBlock to player " + player.getName() + " script. Total blocks: " + blocks.size());
        }
    }
    
    /**
     * Removes a block from player script tracking
     */
    private void removeBlockFromPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.get(playerId);
        
        if (blocks != null) {
            blocks.remove(codeBlock);
            if (blocks.isEmpty()) {
                playerScriptBlocks.remove(playerId);
            }
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
     */
    public void createAndAddScript(CodeBlock eventBlock, Player player, Location location) {
        try {
            // Use the new compilation method to create a complete script
            CodeScript script = compileScriptFromEventBlock(eventBlock, location);
            if (script == null) {
                player.sendMessage("§cОшибка при создании скрипта!");
                return;
            }
            
            // Find the creative world
            com.megacreative.models.CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                // Add the script to the world
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
                plugin.getWorldManager().saveWorld(creativeWorld);
                
                player.sendMessage("§a✓ Скрипт скомпилирован и создан для события: §f" + eventBlock.getAction());
                plugin.getLogger().fine("Compiled and added script for event block: " + eventBlock.getAction() + " in world: " + creativeWorld.getName());
            } else {
                plugin.getLogger().warning("Could not find creative world for location: " + location);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create script for event block: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Removes a script corresponding to an event block from the world
     */
    public void removeScript(CodeBlock eventBlock, Location location) {
        try {
            // Find the creative world
            com.megacreative.models.CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                // Remove the script from the world
                List<CodeScript> scripts = creativeWorld.getScripts();
                if (scripts != null) {
                    // Find and remove the script that corresponds to this event block
                    boolean removed = scripts.removeIf(script -> 
                        script.getRootBlock() != null && 
                        eventBlock.getAction().equals(script.getRootBlock().getAction())
                    );
                    
                    if (removed) {
                        // Save the creative world to persist the change
                        plugin.getWorldManager().saveWorld(creativeWorld);
                        plugin.getLogger().fine("Removed script for event block: " + eventBlock.getAction() + " from world: " + creativeWorld.getName());
                    }
                }
            } else {
                plugin.getLogger().warning("Could not find creative world for location: " + location);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove script for event block: " + e.getMessage());
            e.printStackTrace();
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
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler != null) {
            synchronizeWithPlacementHandler(placementHandler);
            
            // Rebuild connections for all loaded worlds
            for (World world : plugin.getServer().getWorlds()) {
                if (isDevWorld(world)) {
                    rebuildWorldConnections(world);
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
     * Gets all available materials for code blocks
     */
    public Set<Material> getCodeBlockMaterials() {
        return blockConfigService.getCodeBlockMaterials();
    }
    
    /**
     * Reloads block configuration
     */
    public void reloadBlockConfig() {
        blockConfigService.reload();
        plugin.getLogger().fine("Block configuration reloaded");
    }
    
    /**
     * Determines if a block should auto-connect to the next block
     * Control and event blocks don't auto-connect as they have special connection logic
     */
    private boolean shouldAutoConnect(String action, String blockType) {
        // Get the block configuration
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(action);
        if (config == null) return true; // Default to auto-connect
        
        // Check if it's a control or event block
        return !blockConfigService.isControlOrEventBlock(config.getType());
    }
    
    /**
     * Compiles a complete script from an event block by following all connections
     * This implements the "compilation" process mentioned in the roadmap
     */
    public CodeScript compileScriptFromEventBlock(CodeBlock eventBlock, Location eventLocation) {
        try {
            // Create the root script
            CodeScript script = new CodeScript(eventBlock);
            script.setName("Compiled Script for " + eventBlock.getAction());
            script.setEnabled(true);
            script.setType(CodeScript.ScriptType.EVENT);
            
            // Build the complete block chain by following connections
            buildBlockChain(eventBlock, eventLocation);
            
            plugin.getLogger().fine("Compiled script from event block: " + eventBlock.getAction() + " with connected blocks");
            return script;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to compile script from event block: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Builds the complete block chain by following nextBlock and children connections
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
     */
    private void buildChildBlocks(CodeBlock parentBlock, Location parentLocation) {
        int parentLine = DevWorldGenerator.getCodeLineFromZ(parentLocation.getBlockZ());
        
        // Look for indented blocks in subsequent lines (children)
        for (int childLine = parentLine + 1; childLine < parentLine + 10 && childLine < DevWorldGenerator.getLinesCount(); childLine++) {
            int childZ = DevWorldGenerator.getZForCodeLine(childLine);
            
            // Check different indentation levels (X > 0 means indented)
            for (int childX = 1; childX <= 5; childX++) {
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
     * Rebuilds and recompiles all scripts in a world
     * This should be called when the world is loaded or when significant changes are made
     */
    public void recompileWorldScripts(org.bukkit.World world) {
        plugin.getLogger().fine("Recompiling all scripts for world: " + world.getName());
        
        com.megacreative.models.CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) return;
        
        // Clear existing scripts
        List<CodeScript> newScripts = new ArrayList<>();
        
        // Find all event blocks in the world and compile scripts from them
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (!entry.getKey().getWorld().equals(world)) continue;
            
            CodeBlock block = entry.getValue();
            if (isEventBlock(block)) {
                CodeScript compiledScript = compileScriptFromEventBlock(block, entry.getKey());
                if (compiledScript != null) {
                    newScripts.add(compiledScript);
                }
            }
        }
        
        // Update the creative world with new scripts
        creativeWorld.setScripts(newScripts);
        plugin.getWorldManager().saveWorld(creativeWorld);
        
        plugin.getLogger().fine("Recompiled " + newScripts.size() + " scripts for world: " + world.getName());
    }
    
    /**
     * Determines if a block should connect to child blocks (conditionals, loops)
     * Only control and event blocks can have children
     */
    private boolean shouldConnectToChildren(String action, String blockType) {
        // Get the block configuration
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(action);
        if (config == null) return false; // Default to no children
        
        // Check if it's a control or event block
        return blockConfigService.isControlOrEventBlock(config.getType());
    }
}