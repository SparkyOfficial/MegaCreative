package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
 * Simplified to only handle block placement - the "Builder"
 */
public class BlockPlacementHandler implements Listener {
    private static final Logger log = Logger.getLogger(BlockPlacementHandler.class.getName());
    
    private final MegaCreative plugin;
    private final ITrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();

    public BlockPlacementHandler(MegaCreative plugin) {
        this.plugin = plugin;
        ServiceRegistry registry = plugin.getServiceRegistry();
        this.trustedPlayerManager = registry != null ? registry.getTrustedPlayerManager() : null;
        this.blockConfigService = registry != null ? registry.getBlockConfigService() : null;
    }
    
    /**
     * Handles placement of coding blocks
     * Only creates CodeBlock objects and stores them - no connection logic
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        
        // Only process in dev worlds
        if (!isInDevWorld(player)) {
            return;
        }
        
        // Check if the block is being placed on the correct surface
        if (!isCorrectPlacementSurface(block)) {
            player.sendMessage("§cThis block can only be placed on the correct surface!");
            player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
            event.setCancelled(true);
            return;
        }
        
        // Handle code block placement
        if (handleCodeBlockPlacement(event, player, block)) {
            return;
        }
        
        // For regular blocks that aren't code blocks
        player.sendMessage("§cYou can only place special coding blocks!");
        player.playSound(block.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 1.0f, 0.8f);
        event.setCancelled(true);
    }

    /**
     * Handles code block placement logic
     */
    private boolean handleCodeBlockPlacement(BlockPlaceEvent event, Player player, Block block) {
        // Check if blockConfigService is available
        if (blockConfigService == null) {
            player.sendMessage("§cBlock configuration service not available!");
            return false;
        }
        
        // Check if this is a universal coding block
        if (!blockConfigService.isCodeBlock(block.getType())) {
            return handleNonCodeBlockPlacement(event, player, block);
        }
        
        // Get block config from material
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
        
        if (config == null) {
            return false;
        }
        
        // Handle regular code blocks - create "empty" block to be configured via GUI
        String actionId = "NOT_SET"; // Empty block without action
        
        CodeBlock newCodeBlock = new CodeBlock(block.getType(), actionId);
        
        // Special handling for piston blocks (brackets)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default to opening bracket
        }
        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Visual and audio feedback
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        
        // If this is a constructor block, automatically create brackets
        if (config.isConstructor() && config.getStructure() != null) {
            createBracketsForConstructor(player, block, config);
        }
        
        if (config.isConstructor()) {
            player.sendMessage("§a✓ Constructor block placed!");
            player.sendMessage("§7Right-click to configure parameters");
        } else {
            player.sendMessage("§a✓ Code block placed: " + config.getDisplayName());
            player.sendMessage("§7Right-click to select action");
        }
        
        // Reduced logging - only log when debugging
        // plugin.getLogger().info("Code block placed by " + player.getName() + " at " + block.getLocation() + " with action: " + actionId);
        return true;
    }

    /**
     * Creates brackets for constructor blocks
     */
    private void createBracketsForConstructor(Player player, Block block, BlockConfigService.BlockConfig config) {
        // Get structure configuration
        BlockConfigService.StructureConfig structure = config.getStructure();
        if (structure == null) {
            return;
        }
        
        Material bracketMaterial = structure.getBrackets();
        int bracketDistance = structure.getBracketDistance();
        
        // Create opening bracket (to the left) - facing east (toward the center)
        Location openBracketLoc = block.getLocation().clone().add(-bracketDistance, 0, 0);
        Block openBracketBlock = block.getWorld().getBlockAt(openBracketLoc);
        openBracketBlock.setType(bracketMaterial);
        // Set piston facing direction (east = toward positive X, which is toward the center block)
        if (bracketMaterial == Material.PISTON || bracketMaterial == Material.STICKY_PISTON) {
            org.bukkit.block.data.Directional pistonData = (org.bukkit.block.data.Directional) openBracketBlock.getBlockData();
            pistonData.setFacing(org.bukkit.block.BlockFace.EAST);
            openBracketBlock.setBlockData(pistonData);
        }
        
        // Create closing bracket (to the right) - facing west (toward the center)
        Location closeBracketLoc = block.getLocation().clone().add(bracketDistance, 0, 0);
        Block closeBracketBlock = block.getWorld().getBlockAt(closeBracketLoc);
        closeBracketBlock.setType(bracketMaterial);
        // Set piston facing direction (west = toward negative X, which is toward the center block)
        if (bracketMaterial == Material.PISTON || bracketMaterial == Material.STICKY_PISTON) {
            org.bukkit.block.data.Directional pistonData = (org.bukkit.block.data.Directional) closeBracketBlock.getBlockData();
            pistonData.setFacing(org.bukkit.block.BlockFace.WEST);
            closeBracketBlock.setBlockData(pistonData);
        }
        
        // Register brackets as code blocks
        CodeBlock openBracket = new CodeBlock(bracketMaterial, "BRACKET");
        openBracket.setBracketType(CodeBlock.BracketType.OPEN);
        blockCodeBlocks.put(openBracketLoc, openBracket);
        
        CodeBlock closeBracket = new CodeBlock(bracketMaterial, "BRACKET");
        closeBracket.setBracketType(CodeBlock.BracketType.CLOSE);
        blockCodeBlocks.put(closeBracketLoc, closeBracket);
        
        // Add visual effects
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, openBracketLoc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, closeBracketLoc.add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        
        player.sendMessage("§a✓ Brackets created automatically for constructor block");
    }

    /**
     * Handles non-code block placement (like pistons for brackets)
     */
    private boolean handleNonCodeBlockPlacement(BlockPlaceEvent event, Player player, Block block) {
        // Special handling for pistons (brackets)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            CodeBlock newCodeBlock = new CodeBlock(block.getType(), "BRACKET");
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN);
            blockCodeBlocks.put(block.getLocation(), newCodeBlock);
            
            // Enhanced feedback for bracket placement
            player.sendMessage("§a✓ Bracket placed: " + CodeBlock.BracketType.OPEN.getDisplayName());
            player.sendMessage("§7Right-click to toggle bracket type");
            
            // Add visual effects
            Location effectLoc = block.getLocation().add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, effectLoc, 10, 0.3, 0.3, 0.3, 0);
            player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_PISTON_EXTEND, 1.0f, 1.5f);
            
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Bracket placed by " + player.getName() + " at " + block.getLocation());
            return true;
        }
        return false;
    }

    /**
     * Handles breaking of coding blocks
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        
        Location loc = event.getBlock().getLocation();
        Player player = event.getPlayer();
        
        // Remove block from our map
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // Enhanced feedback for block removal
            player.sendMessage("§cCode block removed!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8f, 0.8f);
            
            // Add visual effect for block removal
            Location effectLoc = loc.add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.CLOUD, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
            
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("CodeBlock removed from " + loc + " with action: " + (removedBlock != null ? removedBlock.getAction() : "unknown"));
        }
        
        // Special handling for piston brackets
        else if (event.getBlock().getType() == Material.PISTON || event.getBlock().getType() == Material.STICKY_PISTON) {
            // This is a piston bracket, remove it from our map
            blockCodeBlocks.remove(loc);
            
            // Enhanced feedback for bracket removal
            player.sendMessage("§cBracket removed!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_PISTON_CONTRACT, 0.8f, 1.2f);
            
            // Add visual effect for bracket removal
            Location effectLoc = loc.add(0.5, 0.5, 0.5);
            player.spawnParticle(org.bukkit.Particle.SMOKE_NORMAL, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
            player.spawnParticle(org.bukkit.Particle.FLAME, effectLoc, 3, 0.2, 0.2, 0.2, 0.05);
            
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("Bracket piston removed from " + loc);
        }
    }
    
    /**
     * Handles player interaction with blocks
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Fix double firing by only processing main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Only process right clicks on existing blocks
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        
        Location location = clickedBlock.getLocation();
        
        // Only process in dev worlds
        if (!isInDevWorld(player)) {
            return;
        }
        
        // Handle code block interactions
        if (blockCodeBlocks.containsKey(location)) {
            event.setCancelled(true); // Important to prevent opening containers etc.
            
            // Open GUI for block configuration
            CodeBlock codeBlock = blockCodeBlocks.get(location);
            
            // Special handling for bracket blocks - toggle bracket type instead of opening GUI
            if (codeBlock.isBracket()) {
                toggleBracketType(codeBlock, event.getClickedBlock(), player);
                player.sendMessage("§aBracket toggled!");
                return;
            }
            
            // For other blocks, open the appropriate GUI
            // Open ActionSelectionGUI for blocks without actions or Parameter GUI for blocks with actions
            if (codeBlock.getAction() == null || "NOT_SET".equals(codeBlock.getAction())) {
                // Check if plugin and service registry are available
                if (plugin == null || plugin.getServiceRegistry() == null) {
                    player.sendMessage("§cPlugin services not available!");
                    return;
                }
                
                // Open action selection GUI
                com.megacreative.gui.coding.ActionSelectionGUI actionGUI = new com.megacreative.gui.coding.ActionSelectionGUI(
                    plugin, player, location, codeBlock.getMaterial());
                actionGUI.open();
            } else {
                // Check if plugin and service registry are available
                if (plugin == null || plugin.getServiceRegistry() == null) {
                    player.sendMessage("§cPlugin services not available!");
                    return;
                }
                
                // Open enhanced parameter configuration GUI
                com.megacreative.gui.coding.EnhancedActionParameterGUI enhancedGUI = new com.megacreative.gui.coding.EnhancedActionParameterGUI(plugin);
                enhancedGUI.openParameterEditor(player, location, codeBlock.getAction());
            }
            return;
        }
    }

    /**
     * Toggles bracket type (open/closed) for a code block
     */
    private void toggleBracketType(CodeBlock codeBlock, Block block, Player player) {
        CodeBlock.BracketType newType = codeBlock.getBracketType() == CodeBlock.BracketType.OPEN ? 
            CodeBlock.BracketType.CLOSE : CodeBlock.BracketType.OPEN;
        codeBlock.setBracketType(newType);
        player.sendMessage("§aBracket switched to: " + newType.getDisplayName());
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
               worldName.contains("megacreative_");
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
        if (blockConfigService == null) {
            // Reduced logging - only log when debugging
            // plugin.getLogger().info("BlockConfigService is null, allowing placement");
            return true;
        }
        
        // Get block config
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfigByMaterial(block.getType());
        
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
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) {
            plugin.getLogger().warning("No CreativeWorld found for Bukkit world: " + world.getName());
            return;
        }
        
        // Save the world to persist any changes to code blocks
        plugin.getWorldManager().saveWorld(creativeWorld);
        plugin.getLogger().fine("Saved all code blocks in world: " + world.getName());
    }
}