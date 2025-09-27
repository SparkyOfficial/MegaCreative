package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlockBrokenEvent;
import com.megacreative.events.MegaBlockPlaceEvent;
import com.megacreative.coding.values.DataValue;
import com.megacreative.gui.coding.ActionSelectionGUI;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Handles placement and interaction with coding blocks
 * Его роль - "Строитель". Он создает CodeBlock и сообщает об этом через кастомные события.
 */
public class BlockPlacementHandler implements Listener {
    private static final Logger log = Logger.getLogger(BlockPlacementHandler.class.getName());
    
    private final MegaCreative plugin;
    private ITrustedPlayerManager trustedPlayerManager;
    private BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    
    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
        // Dependencies will be lazily initialized when needed
    }
    
    // Lazy initialization methods
    private ITrustedPlayerManager getTrustedPlayerManager() {
        if (trustedPlayerManager == null && plugin != null && plugin.getServiceRegistry() != null) {
            trustedPlayerManager = plugin.getServiceRegistry().getTrustedPlayerManager();
        }
        return trustedPlayerManager;
    }
    
    private BlockConfigService getBlockConfigService() {
        if (blockConfigService == null && plugin != null && plugin.getServiceRegistry() != null) {
            blockConfigService = plugin.getServiceRegistry().getBlockConfigService();
        }
        return blockConfigService;
    }
    
    /**
     * Checks if an action ID is registered in the action factory
     * @param actionId The action ID to check
     * @return true if the action is registered, false otherwise
     */
    private boolean isRegisteredAction(String actionId) {
        if (plugin == null || plugin.getServiceRegistry() == null || actionId == null) {
            return false;
        }
        // Try to create the action to see if it's registered
        return plugin.getServiceRegistry().getActionFactory().createAction(actionId) != null;
    }
    
    /**
     * Checks if an event ID is registered in the block config service
     * @param eventId The event ID to check
     * @return true if the event is registered, false otherwise
     */
    private boolean isRegisteredEvent(String eventId) {
        BlockConfigService configService = getBlockConfigService();
        if (configService == null || eventId == null) {
            return false;
        }
        // Try to get the block config to see if it's registered
        BlockConfigService.BlockConfig config = configService.getBlockConfig(eventId);
        return config != null;
    }
    
    /**
     * Checks if a condition ID is registered in the condition factory
     * @param conditionId The condition ID to check
     * @return true if the condition is registered, false otherwise
     */
    private boolean isRegisteredCondition(String conditionId) {
        if (plugin == null || plugin.getServiceRegistry() == null || conditionId == null) {
            return false;
        }
        // Try to create the condition to see if it's registered
        return plugin.getServiceRegistry().getConditionFactory().createCondition(conditionId) != null;
    }

    /**
     * Создает CodeBlock и генерирует событие CodeBlockPlacedEvent.
     * Не занимается логикой соединений или компиляции.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || !isInDevWorld(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        // Проверка на поверхность установки
        if (!isCorrectPlacementSurface(block)) {
            player.sendMessage("§cThis block can only be placed on the correct surface!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }

        CodeBlock newCodeBlock = createCodeBlockFor(block);
        
        if (newCodeBlock == null) {
            // Если это не наш специальный блок, отменяем установку.
            player.sendMessage("§cYou can only place special coding blocks!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }

        // 1. Сохраняем созданный блок
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // 2. Создаем табличку для визуализации
        createSignForBlock(block.getLocation(), newCodeBlock);
        
        // 3. Отправляем наше собственное событие в систему!
        CodeBlockPlacedEvent placedEvent = new CodeBlockPlacedEvent(player, newCodeBlock, block.getLocation());
        plugin.getServer().getPluginManager().callEvent(placedEvent);

        player.sendMessage("§a✓ Code block placed!");
    }

    /**
     * Ловит уничтожение блока, удаляет CodeBlock и генерирует CodeBlockBrokenEvent.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled() || !isInDevWorld(event.getPlayer())) {
            return;
        }
        
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        
        if (isProtectedBracket(loc)) {
            event.setCancelled(true);
            player.sendMessage("§cBrackets cannot be broken directly!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }

        // Если по этому адресу был наш CodeBlock...
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // 1. Удаляем табличку
            removeSignFromBlock(loc);
            
            // 2. Отправляем наше событие об уничтожении!
            CodeBlockBrokenEvent brokenEvent = new CodeBlockBrokenEvent(player, removedBlock, loc);
            plugin.getServer().getPluginManager().callEvent(brokenEvent);
            
            player.sendMessage("§cCode block removed!");
        }
        
        // Special handling for piston brackets
        else if (event.getBlock().getType() == Material.PISTON || event.getBlock().getType() == Material.STICKY_PISTON) {
            // Check if this bracket is protected
            if (isProtectedBracket(loc)) {
                // Cancel the event to prevent breaking brackets
                event.setCancelled(true);
                player.sendMessage("§cBrackets cannot be broken directly!");
                player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            
            // This is a piston bracket, remove it from our map
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // Fire custom event for other systems to react to
            if (removedBlock != null) {
                CodeBlockBrokenEvent brokenEvent = new CodeBlockBrokenEvent(player, removedBlock, loc);
                plugin.getServer().getPluginManager().callEvent(brokenEvent);
            }
            
            // Enhanced feedback for bracket removal
            player.sendMessage("§cBracket removed!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
        }
    }
    
    // Вспомогательный метод для создания CodeBlock
    private CodeBlock createCodeBlockFor(Block block) {
        BlockConfigService configService = getBlockConfigService();
        if (configService == null) {
            return null;
        }

        // Это блок-скобка?
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            CodeBlock bracket = new CodeBlock(block.getType().name(), "BRACKET");
            bracket.setBracketType(CodeBlock.BracketType.OPEN); // По умолчанию
            return bracket;
        }
        
        // Это наш кодовый блок?
        if (configService.isCodeBlock(block.getType())) {
            return new CodeBlock(block.getType().name(), "NOT_SET");
        }

        return null;
    }
    
    /**
     * Обрабатывает клик правой кнопкой мыши. 
     * Теперь полностью делегирует открытие GUI реестру GUIRegistry.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND || 
            event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK ||
            event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        if (!isInDevWorld(player)) return;

        // ... (проверка на режим PLAY остается, если она вам нужна)

        Location location = event.getClickedBlock().getLocation();
        if (!blockCodeBlocks.containsKey(location)) {
            return;
        }
        
        event.setCancelled(true); // Предотвращаем стандартное поведение
        CodeBlock codeBlock = blockCodeBlocks.get(location);

        // Обработка клика по скобкам (эта логика остается здесь)
        if (codeBlock.isBracket()) {
            toggleBracketType(codeBlock, event.getClickedBlock(), player);
            return;
        }
        
        // --- НОВАЯ, УПРОЩЕННАЯ ЛОГИКА ---
        
        String blockId = getBlockIdentifier(codeBlock);
        
        // Получаем наш реестр
        if (plugin == null || plugin.getServiceRegistry() == null) {
            player.sendMessage("§cError: Service registry not available!");
            return;
        }
        
        GUIRegistry guiRegistry = plugin.getServiceRegistry().getGuiRegistry();
        if (guiRegistry == null) {
            player.sendMessage("§cError: GUIRegistry not found!");
            return;
        }
        
        // Пытаемся открыть специализированный редактор
        boolean opened = guiRegistry.open(blockId, plugin, player, codeBlock);
        
        // Если для блока еще не задано действие ИЛИ не нашлось редактора,
        // открываем GUI выбора действия.
        if (!opened) {
            new ActionSelectionGUI(plugin, player, location, codeBlock.getMaterial()).open();
        }
    }
    
    /**
     * Вспомогательный метод для получения уникального ID блока (actionId, eventId и т.д.).
     */
    private String getBlockIdentifier(CodeBlock codeBlock) {
        if (codeBlock.getAction() != null && !codeBlock.getAction().equals("NOT_SET")) {
            return codeBlock.getAction();
        }
        if (codeBlock.getEvent() != null && !codeBlock.getEvent().equals("NOT_SET")) {
            return codeBlock.getEvent();
        }
        if (codeBlock.getCondition() != null && !codeBlock.getCondition().equals("NOT_SET")) {
            return codeBlock.getCondition();
        }
        return null; // ID не установлен
    }
    
    /**
     * Toggles bracket type (open/closed) for a code block
     */
    private void toggleBracketType(CodeBlock codeBlock, Block block, Player player) {
        CodeBlock.BracketType newType = codeBlock.getBracketType() == CodeBlock.BracketType.OPEN ? 
            CodeBlock.BracketType.CLOSE : CodeBlock.BracketType.OPEN;
        codeBlock.setBracketType(newType);
        
        // Update the sign to reflect the new bracket type
        createSignForBlock(block.getLocation(), codeBlock);
        
        player.sendMessage("§aBracket switched to: " + newType.getDisplayName());
    }
    
    /**
     * Checks if player is in a dev world
     */
    public boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        // Enhanced detection for dev worlds with new naming scheme
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_") || worldName.contains("DEV") ||
               // Check for dual world mode dev worlds
               (worldName.startsWith("megacreative_") && worldName.endsWith("-code"));
    }
    
    /**
     * Checks if a bracket is protected and should not be broken
     * @param location The location to check
     * @return true if the bracket is protected, false otherwise
     */
    private boolean isProtectedBracket(Location location) {
        // Get the code block at this location
        CodeBlock codeBlock = blockCodeBlocks.get(location);
        
        // If this is a bracket block, check if it's part of a constructor structure
        if (codeBlock != null && codeBlock.isBracket()) {
            // Find nearby constructor blocks
            Location constructorLoc = findNearbyConstructor(location);
            if (constructorLoc != null) {
                return true; // Bracket is protected as part of constructor structure
            }
        }
        
        return false;
    }
    
    /**
     * Checks if there's already a bracket at the specified location
     * @param location The location to check
     * @return true if there's already a bracket at this location, false otherwise
     */
    private boolean hasExistingBracket(Location location) {
        CodeBlock existingBlock = blockCodeBlocks.get(location);
        return existingBlock != null && existingBlock.isBracket();
    }
    
    /**
     * Finds a nearby constructor block that might be associated with a bracket
     * @param bracketLocation The location of the bracket
     * @return Location of the constructor block, or null if none found
     */
    private Location findNearbyConstructor(Location bracketLocation) {
        // Check adjacent locations for constructor blocks
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Location checkLoc = bracketLocation.clone().add(x, 0, z);
                CodeBlock checkBlock = blockCodeBlocks.get(checkLoc);
                
                if (checkBlock != null) {
                    // Get block configuration
                    BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(checkBlock.getAction());
                    if (config != null && config.isConstructor()) {
                        return checkLoc; // Found a constructor block
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if the block is being placed on the correct surface
     * Events (DIAMOND_BLOCK) should only be placed on blue glass
     * Other blocks should only be placed on grey glass
     */
    private boolean isCorrectPlacementSurface(Block block) {
        // Get the block below the placed block
        Block below = block.getRelative(org.bukkit.block.BlockFace.DOWN);
        
        // If blockConfigService is not available, allow placement (fallback)
        BlockConfigService configService = getBlockConfigService();
        if (configService == null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("BlockConfigService is null, allowing placement");
            return true;
        }
        
        // Get block config
        BlockConfigService.BlockConfig config = configService.getBlockConfigByMaterial(block.getType());
        
        // If no config found, allow placement (fallback)
        if (config == null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("No config found for material " + block.getType() + ", allowing placement");
            return true;
        }
        
        // Reduced logging - only log when debugging
        // plugin.getLogger().info("Checking placement for block type: " + block.getType() + ", config type: " + config.getType());
        // plugin.getLogger().info("Block below type: " + below.getType());
        
        // Check if this is an EVENT block (DIAMOND_BLOCK)
        if ("EVENT".equals(config.getType())) {
            // EVENT blocks should only be placed on blue glass
            boolean correct = below.getType() == org.bukkit.Material.BLUE_STAINED_GLASS;
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("EVENT block placement check: " + correct + " (should be on blue glass)");
            return correct;
        } else {
            // All other blocks should only be placed on grey glass
            boolean correct = below.getType() == org.bukkit.Material.GRAY_STAINED_GLASS || 
                   below.getType() == org.bukkit.Material.LIGHT_GRAY_STAINED_GLASS;
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Non-EVENT block placement check: " + correct + " (should be on grey glass)");
            return correct;
        }
    }
    
    /**
     * Gets CodeBlock by location
     */
    public CodeBlock getCodeBlock(Location location) {
        return blockCodeBlocks.get(location);
    }
    
    /**
     * Recreates a CodeBlock from an existing physical block and sign
     * Used during world hydration to restore code blocks
     */
    public CodeBlock recreateCodeBlockFromExisting(Block block, Sign sign) {
        if (block == null || sign == null) {
            return null;
        }
        
        Location location = block.getLocation();
        Material material = block.getType();
        
        // Check if this is a code block material
        BlockConfigService configService = getBlockConfigService();
        if (configService == null || !configService.isCodeBlock(material)) {
            // Handle brackets specially
            if (material == Material.PISTON || material == Material.STICKY_PISTON) {
                CodeBlock bracketBlock = new CodeBlock(material.name(), "BRACKET");
                // Determine bracket type from sign text or default to OPEN
                String[] lines = sign.getLines();
                if (lines.length > 1) {
                    String line1 = lines[1];
                    if (line1.contains("{")) {
                        bracketBlock.setBracketType(CodeBlock.BracketType.OPEN);
                    } else if (line1.contains("}")) {
                        bracketBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                    } else {
                        bracketBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default
                    }
                } else {
                    bracketBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default
                }
                
                bracketBlock.setLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
                blockCodeBlocks.put(location, bracketBlock);
                return bracketBlock;
            }
            return null;
        }
        
        // Create a new code block
        CodeBlock codeBlock = new CodeBlock(material.name(), "NOT_SET");
        codeBlock.setLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        
        // Try to extract action/event from sign
        String[] lines = sign.getLines();
        if (lines.length > 1) {
            String line1 = lines[1];
            // Try to extract action from the sign text
            // Look for patterns like "[Action: sendMessage]" or "sendMessage"
            if (line1.contains("[Action:")) {
                int start = line1.indexOf("[Action:") + 8;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String action = line1.substring(start, end).trim();
                    codeBlock.setAction(action);
                }
            } else if (line1.contains("[Event:")) {
                // Try to extract event from the sign text
                int start = line1.indexOf("[Event:") + 7;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String event = line1.substring(start, end).trim();
                    codeBlock.setEvent(event);
                }
            } else if (line1.contains("[Condition:")) {
                // Try to extract condition from the sign text
                int start = line1.indexOf("[Condition:") + 11;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String condition = line1.substring(start, end).trim();
                    // Store condition in parameters since CodeBlock doesn't have a setCondition method
                    codeBlock.setParameter("condition", condition);
                }
            } else if (line1.contains("§")) {
                // Try to extract action from colored text
                // This is a simplified approach - in a real implementation, 
                // you might want to store action data in the sign's persistent data
                String cleanLine = org.bukkit.ChatColor.stripColor(line1).trim();
                if (!cleanLine.isEmpty() && !"NOT_SET".equals(cleanLine)) {
                    // Try to determine if this is an action, event, or condition based on registered items
                    if (isRegisteredAction(cleanLine)) {
                        codeBlock.setAction(cleanLine);
                    } else if (isRegisteredEvent(cleanLine)) {
                        codeBlock.setEvent(cleanLine);
                    } else if (isRegisteredCondition(cleanLine)) {
                        // Store condition in parameters since CodeBlock doesn't have a setCondition method
                        codeBlock.setParameter("condition", cleanLine);
                    } else {
                        // Default to action if not found
                        codeBlock.setAction(cleanLine);
                    }
                }
            }
        }
        
        // Add to tracking
        blockCodeBlocks.put(location, codeBlock);
        return codeBlock;
    }
    
    /**
     * Creates a sign for a code block
     * @param location Location of the code block
     * @param codeBlock The code block
     */
    public void createSignForBlock(Location location, CodeBlock codeBlock) {
        if (location == null || codeBlock == null) {
            return;
        }
        
        // Remove any existing signs
        removeSignFromBlock(location);
        
        Block block = location.getBlock();
        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.EAST, 
                                             org.bukkit.block.BlockFace.SOUTH, org.bukkit.block.BlockFace.WEST};
        
        String displayName = "NOT_SET";
        if (blockConfigService != null) {
            // First try to get display name from action
            if (codeBlock.getAction() != null && !"NOT_SET".equals(codeBlock.getAction())) {
                BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getAction());
                if (config != null) {
                    displayName = config.getDisplayName();
                } else {
                    displayName = codeBlock.getAction();
                }
            } 
            // If no action, try to get display name from event
            else if (codeBlock.getEvent() != null && !"NOT_SET".equals(codeBlock.getEvent())) {
                BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(codeBlock.getEvent());
                if (config != null) {
                    displayName = config.getDisplayName();
                } else {
                    displayName = codeBlock.getEvent();
                }
            }
        }
        
        // Determine color based on block type
        String colorCode = "§7"; // Default
        if (codeBlock.isBracket()) {
            colorCode = "§6"; // Brackets
            displayName = codeBlock.getBracketType() != null ? codeBlock.getBracketType().getDisplayName() : "Bracket";
        } else {
            // Get the block config to determine the type
            if (blockConfigService != null) {
                BlockConfigService.BlockConfig config = null;
                String actionOrEvent = null;
                
                // Try to get config for action first
                if (codeBlock.getAction() != null && !"NOT_SET".equals(codeBlock.getAction())) {
                    config = blockConfigService.getBlockConfig(codeBlock.getAction());
                    actionOrEvent = codeBlock.getAction();
                }
                // If no action config, try event
                else if (codeBlock.getEvent() != null && !"NOT_SET".equals(codeBlock.getEvent())) {
                    config = blockConfigService.getBlockConfig(codeBlock.getEvent());
                    actionOrEvent = codeBlock.getEvent();
                }
                
                if (config != null) {
                    String type = config.getType();
                    if ("EVENT".equals(type)) {
                        colorCode = "§e"; // Events
                    } else if ("ACTION".equals(type)) {
                        colorCode = "§a"; // Actions
                    } else if ("CONDITION".equals(type)) {
                        colorCode = "§6"; // Conditions
                    } else if ("CONTROL".equals(type)) {
                        colorCode = "§c"; // Control
                    } else if ("FUNCTION".equals(type)) {
                        colorCode = "§d"; // Functions
                    } else if ("VARIABLE".equals(type)) {
                        colorCode = "§b"; // Variables
                    }
                } else if (actionOrEvent != null && !"NOT_SET".equals(actionOrEvent)) {
                    // Default color for unknown actions/events
                    colorCode = "§f";
                }
            }
        }
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getType().isAir()) {
                signBlock.setType(Material.OAK_WALL_SIGN, false);
                
                org.bukkit.block.data.type.WallSign wallSignData = (org.bukkit.block.data.type.WallSign) signBlock.getBlockData();
                wallSignData.setFacing(face);
                signBlock.setBlockData(wallSignData);
                
                org.bukkit.block.Sign signState = (org.bukkit.block.Sign) signBlock.getState();
                signState.setLine(0, "§8============");
                String line2 = displayName.length() > 15 ? displayName.substring(0, 15) : displayName;
                signState.setLine(1, colorCode + line2);
                signState.setLine(2, "§7Кликните ПКМ");
                signState.setLine(3, "§8============");
                signState.update(true);
                return;
            }
        }
    }
    
    /**
     * Removes sign from a block
     * @param location Location of the block
     */
    private void removeSignFromBlock(Location location) {
        if (location == null) return;
        
        Block block = location.getBlock();
        org.bukkit.block.BlockFace[] faces = {org.bukkit.block.BlockFace.NORTH, org.bukkit.block.BlockFace.SOUTH, 
                                             org.bukkit.block.BlockFace.EAST, org.bukkit.block.BlockFace.WEST};
        
        for (org.bukkit.block.BlockFace face : faces) {
            Block signBlock = block.getRelative(face);
            if (signBlock.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
                signBlock.setType(Material.AIR);
            }
        }
    }

    /**
     * Checks if there's a CodeBlock at location
     */
    public boolean hasCodeBlock(Location location) {
        return blockCodeBlocks.containsKey(location);
    }

    /**
     * Gets all CodeBlocks
     */
    public Map<Location, CodeBlock> getAllCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
    }

    /**
     * Gets all CodeBlocks (for compatibility)
     */
    public Map<Location, CodeBlock> getBlockCodeBlocks() {
        return new HashMap<>(blockCodeBlocks);
    }
    
    /**
     * Clears all CodeBlocks in world
     */
    public void clearAllCodeBlocksInWorld(World world) {
        blockCodeBlocks.entrySet().removeIf(entry -> entry.getKey().getWorld().equals(world));
        // Reduced logging - only log when debugging
        // plugin.getLogger().info("Cleared all code blocks from world: " + world.getName() + " in BlockPlacementHandler.");
    }
    
    /**
     * Adds a code block to location tracking map
     * Used during world hydration to register existing blocks
     */
    public void addCodeBlock(Location location, CodeBlock codeBlock) {
        if (!blockCodeBlocks.containsKey(location)) {
            blockCodeBlocks.put(location, codeBlock);
            plugin.getLogger().fine("Added CodeBlock to tracking at " + location);
        }
    }
    
    /**
     * Saves all code blocks in a world to persistent storage
     * This method should be called when switching between worlds or shutting down
     */
    public void saveAllCodeBlocksInWorld(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to save code blocks in null world!");
            return;
        }
        
        // Get the creative world associated with this Bukkit world
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) {
            plugin.getLogger().warning("No CreativeWorld found for Bukkit world: " + world.getName());
            return;
        }
        
        // Save the world to persist any changes to code blocks
        plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        plugin.getLogger().fine("Saved all code blocks in world: " + world.getName());
    }
}
