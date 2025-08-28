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
        
        plugin.getLogger().info("Synchronized " + placementBlocks.size() + " blocks with AutoConnectionManager");
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
        
        // Check if this is a dev world
        if (!isDevWorld(block.getWorld())) return;
        
        // Check if this is a code block
        if (!blockConfigService.isCodeBlock(block.getType())) return;
        
        // BLOCK PLACEMENT VALIDATION
        String blockCategory = blockConfigService.getBlockCategory(block.getType());
        int blockX = location.getBlockX();
        
        // Check position validation based on block category
        if (blockX == 0) { // This is the "blue" line (start of coding line)
            if (!blockConfigService.isControlOrEventBlock(blockCategory)) {
                event.setCancelled(true);
                player.sendMessage("§cЭтот тип блока можно ставить только на серые линии!");
                player.sendMessage("§7Подсказка: EVENT, CONTROL и FUNCTION блоки должны быть в начале линии (на синем блоке)");
                return;
            }
        } else { // This is a "gray" line (continuation of coding line)
            if (blockConfigService.isControlOrEventBlock(blockCategory)) {
                event.setCancelled(true);
                player.sendMessage("§cЭтот тип блока можно ставить только в начало линии (на синий блок)!");
                player.sendMessage("§7Подсказка: ACTION и CONDITION блоки должны быть на серых линиях");
                return;
            }
        }
        
        // Log placement validation success
        plugin.getLogger().info("Block placement validated: " + blockCategory + " at X=" + blockX + " (line start: " + (blockX == 0) + ")");
        
        // Get the CodeBlock that was created by BlockPlacementHandler
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler != null && placementHandler.hasCodeBlock(location)) {
            CodeBlock codeBlock = placementHandler.getCodeBlock(location);
            if (codeBlock != null) {
                // Add to our tracking map
                locationToBlock.put(location, codeBlock);
                
                // Add to player's script blocks
                addBlockToPlayerScript(player, codeBlock);
                
                // Auto-connect with neighboring blocks
                autoConnectBlock(codeBlock, location);
                
                player.sendMessage("§aCode block placed and auto-connected!");
                plugin.getLogger().info("Auto-connected CodeBlock at " + location + " for player " + player.getName());
            }
        } else {
            // Fallback: create CodeBlock if BlockPlacementHandler didn't handle it
            plugin.getLogger().warning("BlockPlacementHandler didn't create CodeBlock at " + location + ", creating fallback");
            
            CodeBlock codeBlock = createCodeBlockFromMaterial(block.getType(), location);
            if (codeBlock == null) return;
            
            locationToBlock.put(location, codeBlock);
            addBlockToPlayerScript(player, codeBlock);
            autoConnectBlock(codeBlock, location);
            
            player.sendMessage("§aCode block created and connected!");
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
            
            // Also ensure BlockPlacementHandler is synchronized
            BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
            if (placementHandler != null) {
                // The BlockPlacementHandler should handle this in its own event handler
                plugin.getLogger().info("CodeBlock disconnected at " + location);
            }
            
            event.getPlayer().sendMessage("§cCode block removed and disconnected from chain!");
        }
    }
    
    /**
     * Enhanced automatic block connection with proper CodeBlock structure integration
     * Properly sets nextBlock and children relationships for execution flow
     */
    private void autoConnectBlock(CodeBlock codeBlock, Location location) {
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return;
        
        plugin.getLogger().info("Auto-connecting block at " + location + " (line " + line + ")");
        
        // Step 1: Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null) {
                prevBlock.setNextBlock(codeBlock);
                plugin.getLogger().info("Connected horizontal: " + prevLocation + " -> " + location);
            }
        }
        
        // Step 2: Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null) {
                codeBlock.setNextBlock(nextBlock);
                plugin.getLogger().info("Connected horizontal: " + location + " -> " + nextLocation);
            }
        }
        
        // Step 3: Handle parent-child relationships (vertical connections)
        handleParentChildConnections(codeBlock, location, line);
        
        // Step 4: Update player's script blocks
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
                plugin.getLogger().info("Added child relationship: parent at line " + line + " -> child at " + location);
            }
        }
        
        // If this is a conditional/loop block at start of line, look for children in next lines
        if (location.getBlockX() == 0 && isConditionalOrLoopBlock(codeBlock)) {
            connectChildBlocks(codeBlock, location);
        }
    }
    
    /**
     * Finds the parent block for a child block based on indentation and proximity
     */
    private CodeBlock findParentBlock(Location childLocation, int childLine) {
        // Look for parent in previous lines with less indentation
        for (int parentLine = childLine - 1; parentLine >= 0; parentLine--) {
            int parentZ = DevWorldGenerator.getZForCodeLine(parentLine);
            
            // Look for blocks with less X coordinate (less indentation)
            for (int parentX = 0; parentX < childLocation.getBlockX(); parentX++) {
                Location parentLocation = new Location(childLocation.getWorld(), parentX, childLocation.getBlockY(), parentZ);
                CodeBlock parentBlock = locationToBlock.get(parentLocation);
                
                if (parentBlock != null && isConditionalOrLoopBlock(parentBlock)) {
                    return parentBlock;
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if a block is conditional or loop block that can have children
     */
    private boolean isConditionalOrLoopBlock(CodeBlock block) {
        String action = block.getAction();
        return action != null && (action.toLowerCase().contains("if") || 
                                  action.toLowerCase().contains("loop") ||
                                  action.toLowerCase().contains("while") ||
                                  action.toLowerCase().contains("for") ||
                                  action.toLowerCase().contains("condition"));
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
                
                plugin.getLogger().info("Updated player script blocks for: " + entry.getKey());
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
        
        plugin.getLogger().info("Looking for child blocks for parent at line " + parentLine);
        
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
                        plugin.getLogger().info("Connected child: parent line " + parentLine + " -> child at (" + childX + ", " + childLine + ")");
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
                    plugin.getLogger().info("Found non-indented block at line " + childLine + ", ending child search");
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
        codeBlock.setPlugin(plugin);
        
        return codeBlock;
    }
    
    /**
     * Определяет действие блока на основе его материала
     * Теперь использует конфигурацию из coding_blocks.yml
     */
    private String determineActionFromMaterial(Material material) {
        return blockConfigService.getDefaultAction(material);
    }
    
    /**
     * Проверяет, является ли материал блоком кода
     * Теперь использует конфигурацию из coding_blocks.yml
     */
    private boolean isCodeBlock(Material material) {
        return blockConfigService.isCodeBlock(material);
    }
    
    /**
     * Проверяет, является ли мир dev-миром
     */
    private boolean isDevWorld(World world) {
        return world.getName().endsWith("_dev");
    }
    
    /**
     * Добавляет блок к скрипту игрока
     */
    private void addBlockToPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        playerScriptBlocks.computeIfAbsent(playerId, k -> new ArrayList<>()).add(codeBlock);
    }
    
    /**
     * Удаляет блок из скрипта игрока
     */
    private void removeBlockFromPlayerScript(Player player, CodeBlock codeBlock) {
        UUID playerId = player.getUniqueId();
        List<CodeBlock> blocks = playerScriptBlocks.get(playerId);
        if (blocks != null) {
            blocks.remove(codeBlock);
        }
    }
    
    /**
     * Получает все блоки скрипта игрока
     */
    public List<CodeBlock> getPlayerScriptBlocks(Player player) {
        return playerScriptBlocks.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }
    
    /**
     * Создает CodeScript из блоков игрока на определенной линии
     */
    public CodeScript createScriptFromLine(Player player, int line) {
        List<CodeBlock> allBlocks = getPlayerScriptBlocks(player);
        List<CodeBlock> lineBlocks = new ArrayList<>();
        
        // Находим все блоки на указанной линии
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            Location loc = entry.getKey();
            if (DevWorldGenerator.getCodeLineFromZ(loc.getBlockZ()) == line) {
                lineBlocks.add(entry.getValue());
            }
        }
        
        // Сортируем блоки по X координате
        lineBlocks.sort((a, b) -> {
            Location locA = getLocationForBlock(a);
            Location locB = getLocationForBlock(b);
            return Integer.compare(locA.getBlockX(), locB.getBlockX());
        });
        
        if (lineBlocks.isEmpty()) return null;
        
        // Создаем скрипт с корректным конструктором
        CodeScript script = new CodeScript("script_line_" + line, true, lineBlocks.get(0));
        
        return script;
    }
    
    /**
     * Получает позицию для блока
     */
    private Location getLocationForBlock(CodeBlock block) {
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getValue() == block) {
                return entry.getKey();
            }
        }
        return null;
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
     * Получает все доступные действия для материала
     */
    public List<String> getAvailableActions(Material material) {
        return blockConfigService.getAvailableActions(material);
    }
    
    /**
     * Получает конфигурацию блока
     */
    public BlockConfigService.BlockConfig getBlockConfig(Material material) {
        return blockConfigService.getBlockConfig(material);
    }
    
    /**
     * Rebuilds all connections for blocks in a specific world
     * Useful for initializing connections when loading existing worlds
     */
    public void rebuildWorldConnections(World world) {
        plugin.getLogger().info("Rebuilding connections for world: " + world.getName());
        
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
        
        plugin.getLogger().info("Rebuilt connections for " + worldBlocks.size() + " blocks");
    }
    
    /**
     * Forces synchronization with BlockPlacementHandler and rebuilds connections
     * Should be called during plugin initialization or world loading
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
        plugin.getLogger().info("Block configuration reloaded");
    }
}