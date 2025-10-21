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
import com.megacreative.gui.coding.CodeBlockGUI;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Handles placement and interaction with coding blocks
 * Его роль - "Строитель". Он создает CodeBlock и сообщает об этом через кастомные события.
 */
public class BlockPlacementHandler implements Listener {
    private static final Logger log = Logger.getLogger(BlockPlacementHandler.class.getName());
    private final MegaCreative plugin;
    
    
    private final Map<Location, CodeBlock> blockCodeBlocks = new ConcurrentHashMap<>();
    
    
    private ITrustedPlayerManager trustedPlayerManager;
    private BlockConfigService blockConfigService;
    
    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
        
    }
    
    
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

        
        if (!isCorrectPlacementSurface(block)) {
            player.sendMessage("§cThis block can only be placed on the correct surface!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }

        CodeBlock newCodeBlock = createCodeBlockFor(block);
        
        if (newCodeBlock == null) {
            
            player.sendMessage("§cYou can only place special coding blocks!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }

        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        log.info("Created CodeBlock at " + block.getLocation() + " with material " + newCodeBlock.getMaterial());
        
        
        CodeBlockPlacedEvent placedEvent = new CodeBlockPlacedEvent(player, newCodeBlock, block.getLocation());
        plugin.getServer().getPluginManager().callEvent(placedEvent);
        log.info("Fired CodeBlockPlacedEvent for block at " + block.getLocation());

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

        
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            
            CodeBlockBrokenEvent brokenEvent = new CodeBlockBrokenEvent(player, removedBlock, loc);
            plugin.getServer().getPluginManager().callEvent(brokenEvent);
            
            player.sendMessage("§cCode block removed!");
        }
        
        
        else if (event.getBlock().getType() == Material.PISTON || event.getBlock().getType() == Material.STICKY_PISTON) {
            
            if (isProtectedBracket(loc)) {
                
                event.setCancelled(true);
                player.sendMessage("§cBrackets cannot be broken directly!");
                player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                return;
            }
            
            
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            
            if (removedBlock != null) {
                CodeBlockBrokenEvent brokenEvent = new CodeBlockBrokenEvent(player, removedBlock, loc);
                plugin.getServer().getPluginManager().callEvent(brokenEvent);
            }
            
            
            player.sendMessage("§cBracket removed!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
        }
    }
    
    
    private CodeBlock createCodeBlockFor(Block block) {
        BlockConfigService configService = getBlockConfigService();
        if (configService == null) {
            return null;
        }

        
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            CodeBlock bracket = new CodeBlock(block.getType().name(), "BRACKET");
            bracket.setBracketType(CodeBlock.BracketType.OPEN); 
            return bracket;
        }
        
        
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

        

        Location location = event.getClickedBlock().getLocation();
        if (!blockCodeBlocks.containsKey(location)) {
            return;
        }
        
        event.setCancelled(true); 
        CodeBlock codeBlock = blockCodeBlocks.get(location);

        
        if (codeBlock.isBracket()) {
            toggleBracketType(codeBlock, event.getClickedBlock(), player);
            return;
        }
        
        

        String blockId = getBlockIdentifier(codeBlock);

        
        if (blockId == null || blockId.equals("NOT_SET")) {
            new ActionSelectionGUI(plugin, player, location, codeBlock.getMaterial()).open();
            return;
        }

        
        String blockType = determineBlockType(codeBlock, blockId);

        
        CodeBlockGUI parameterGUI = new CodeBlockGUI(plugin, player, location, blockId, blockType);
        parameterGUI.open();
    }
    
    /**
     * Переключает тип скобки (открывающая/закрывающая)
     * @param codeBlock Блок кода-скобки
     * @param block Блок в мире
     * @param player Игрок, который кликнул
     */
    private void toggleBracketType(CodeBlock codeBlock, Block block, Player player) {
        if (codeBlock == null || !codeBlock.isBracket() || block == null || player == null) {
            return;
        }
        
        
        CodeBlock.BracketType currentType = codeBlock.getBracketType();
        CodeBlock.BracketType newType = (currentType == CodeBlock.BracketType.OPEN) ? 
            CodeBlock.BracketType.CLOSE : CodeBlock.BracketType.OPEN;
        codeBlock.setBracketType(newType);
        
        
        player.sendMessage("§aBracket type changed to: " + newType.getDisplayName());
        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
    }
    
    /**
     * Определяет тип блока для универсального GUI
     * @param codeBlock Блок кода
     * @param blockId ID блока
     * @return Тип блока (EVENT, ACTION, CONDITION, CONTROL, FUNCTION, VARIABLE)
     */
    private String determineBlockType(CodeBlock codeBlock, String blockId) {
        BlockConfigService configService = getBlockConfigService();
        if (configService == null) {
            return "ACTION"; 
        }

        BlockConfigService.BlockConfig config = configService.getBlockConfig(blockId);
        if (config != null) {
            return config.getType();
        }

        
        Material material = codeBlock.getMaterial();
        switch (material) {
            case DIAMOND_BLOCK:
                return "EVENT";
            case COBBLESTONE:
                return "ACTION";
            case OAK_PLANKS:
            case OBSIDIAN:
            case REDSTONE_BLOCK:
            case BRICKS:
                return "CONDITION";
            case EMERALD_BLOCK:
            case END_STONE:
                return "CONTROL";
            case LAPIS_BLOCK:
            case BOOKSHELF:
                return "FUNCTION";
            case IRON_BLOCK:
                return "VARIABLE";
            default:
                return "ACTION";
        }
    }
    
    /**
     * Получает идентификатор блока для определения типа GUI
     * @param codeBlock Блок кода
     * @return Идентификатор блока
     */
    private String getBlockIdentifier(CodeBlock codeBlock) {
        if (codeBlock == null) {
            return null;
        }
        
        
        String action = codeBlock.getAction();
        String event = codeBlock.getEvent();
        
        if (action != null && !action.equals("NOT_SET")) {
            return action;
        } else if (event != null && !event.equals("NOT_SET")) {
            return event;
        }
        
        return codeBlock.getParameter("id") != null ? codeBlock.getParameter("id").asString() : "NOT_SET";
    }
    
    /**
     * Checks if player is in a dev world
     */
    public boolean isInDevWorld(Player player) {
        String worldName = player.getWorld().getName();
        
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_") || worldName.contains("DEV") ||
               
               (worldName.startsWith("megacreative_") && worldName.endsWith("-code"));
    }
    
    /**
     * Checks if a bracket is protected and should not be broken
     * @param location The location to check
     * @return true if the bracket is protected, false otherwise
     */
    private boolean isProtectedBracket(Location location) {
        
        CodeBlock codeBlock = blockCodeBlocks.get(location);
        
        
        if (codeBlock != null && codeBlock.isBracket()) {
            
            Location constructorLoc = findNearbyConstructor(location);
            if (constructorLoc != null) {
                return true; 
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
        
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Location checkLoc = bracketLocation.clone().add(x, 0, z);
                CodeBlock checkBlock = blockCodeBlocks.get(checkLoc);
                
                if (checkBlock != null) {
                    
                    BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(checkBlock.getAction());
                    if (config != null && config.isConstructor()) {
                        return checkLoc; 
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
        
        Block below = block.getRelative(org.bukkit.block.BlockFace.DOWN);
        
        
        BlockConfigService configService = getBlockConfigService();
        if (configService == null) {
            return true;
        }
        
        
        BlockConfigService.BlockConfig config = configService.getBlockConfigByMaterial(block.getType());
        
        
        if (config == null) {
            return true;
        }
        
        
        if ("EVENT".equals(config.getType())) {
            
            return below.getType() == org.bukkit.Material.BLUE_STAINED_GLASS;
        } else {
            
            return below.getType() == org.bukkit.Material.GRAY_STAINED_GLASS || 
                   below.getType() == org.bukkit.Material.LIGHT_GRAY_STAINED_GLASS;
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
        
        
        BlockConfigService configService = getBlockConfigService();
        if (configService == null || !configService.isCodeBlock(material)) {
            
            if (material == Material.PISTON || material == Material.STICKY_PISTON) {
                CodeBlock bracketBlock = new CodeBlock(material.name(), "BRACKET");
                
                String[] lines = sign.getLines();
                if (lines.length > 1) {
                    String line1 = lines[1];
                    if (line1.contains("{")) {
                        bracketBlock.setBracketType(CodeBlock.BracketType.OPEN);
                    } else if (line1.contains("}")) {
                        bracketBlock.setBracketType(CodeBlock.BracketType.CLOSE);
                    } else {
                        bracketBlock.setBracketType(CodeBlock.BracketType.OPEN); 
                    }
                } else {
                    bracketBlock.setBracketType(CodeBlock.BracketType.OPEN); 
                }
                
                bracketBlock.setLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
                blockCodeBlocks.put(location, bracketBlock);
                return bracketBlock;
            }
            return null;
        }
        
        
        CodeBlock codeBlock = new CodeBlock(material.name(), "NOT_SET");
        codeBlock.setLocation(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        
        
        String[] lines = sign.getLines();
        if (lines.length > 1) {
            String line1 = lines[1];
            
            
            if (line1.contains("[Action:")) {
                int start = line1.indexOf("[Action:") + 8;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String action = line1.substring(start, end).trim();
                    codeBlock.setAction(action);
                }
            } else if (line1.contains("[Event:")) {
                
                int start = line1.indexOf("[Event:") + 7;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String event = line1.substring(start, end).trim();
                    codeBlock.setEvent(event);
                }
            } else if (line1.contains("[Condition:")) {
                
                int start = line1.indexOf("[Condition:") + 11;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String condition = line1.substring(start, end).trim();
                    
                    codeBlock.setParameter("condition", condition);
                }
            } else if (line1.contains("§")) {
                
                
                
                String cleanLine = org.bukkit.ChatColor.stripColor(line1).trim();
                if (!cleanLine.isEmpty() && !"NOT_SET".equals(cleanLine)) {
                    
                    if (isRegisteredAction(cleanLine)) {
                        codeBlock.setAction(cleanLine);
                    } else if (isRegisteredEvent(cleanLine)) {
                        codeBlock.setEvent(cleanLine);
                    } else if (isRegisteredCondition(cleanLine)) {
                        
                        codeBlock.setParameter("condition", cleanLine);
                    } else {
                        
                        codeBlock.setAction(cleanLine);
                    }
                }
            }
        }
        
        
        blockCodeBlocks.put(location, codeBlock);
        return codeBlock;
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
        
        
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) {
            plugin.getLogger().warning("No CreativeWorld found for Bukkit world: " + world.getName());
            return;
        }
        
        
        plugin.getServiceRegistry().getWorldManager().saveWorld(creativeWorld);
        plugin.getLogger().fine("Saved all code blocks in world: " + world.getName());
    }
}
