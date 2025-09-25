package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.interfaces.ITrustedPlayerManager;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import com.megacreative.coding.events.EventSubscriber;
import com.megacreative.coding.values.DataValue;
import com.megacreative.gui.editors.player.*;
import com.megacreative.gui.editors.conditions.*;
import com.megacreative.gui.editors.events.*;
import com.megacreative.gui.editors.actions.SetVarEditor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.ChatColor;
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
public class BlockPlacementHandler implements Listener, EventSubscriber {
    private static final Logger log = Logger.getLogger(BlockPlacementHandler.class.getName());
    
    private final MegaCreative plugin;
    private final ITrustedPlayerManager trustedPlayerManager;
    private final BlockConfigService blockConfigService;
    private final Map<Location, CodeBlock> blockCodeBlocks = new HashMap<>();
    
    // Constants for subscribed events
    private static final String[] SUBSCRIBED_EVENTS = {
        "block_place_requested",
        "block_placement_confirmed"
    };

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
        
        CodeBlock newCodeBlock = new CodeBlock(block.getType().name(), actionId);
        
        // Special handling for piston blocks (brackets)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN); // Default to opening bracket
        }
        
        blockCodeBlocks.put(block.getLocation(), newCodeBlock);
        
        // Create sign for the block
        createSignForBlock(block.getLocation(), newCodeBlock);
        
        // Visual and audio feedback
        player.spawnParticle(org.bukkit.Particle.VILLAGER_HAPPY, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
        player.playSound(block.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
        
        // Fire custom event for other systems to react to
        CodeBlockPlacedEvent placedEvent = new CodeBlockPlacedEvent(player, newCodeBlock, block.getLocation());
        plugin.getServer().getPluginManager().callEvent(placedEvent);
        
        // If this is a constructor block, automatically create brackets
        if (config.isConstructor() && config.getStructure() != null) {
            createBracketsForConstructor(player, block, config);
        }
        
        // Check if we're placing a block near existing brackets and handle accordingly
        handleNearbyBrackets(player, block, config);
        
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
     * Handles nearby brackets when placing a block
     * @param player The player placing the block
     * @param block The block being placed
     * @param config The block configuration
     */
    private void handleNearbyBrackets(Player player, Block block, BlockConfigService.BlockConfig config) {
        // Check if there are existing brackets nearby that should be connected
        Location openBracketLoc = block.getLocation().clone().add(1, 0, 0);
        Location closeBracketLoc = block.getLocation().clone().add(3, 0, 0);
        
        // If there are existing brackets, make sure they're properly connected
        if (hasExistingBracket(openBracketLoc)) {
            // Update the bracket if needed
            updateBracketIfNecessary(openBracketLoc, CodeBlock.BracketType.OPEN);
        }
        
        if (hasExistingBracket(closeBracketLoc)) {
            // Update the bracket if needed
            updateBracketIfNecessary(closeBracketLoc, CodeBlock.BracketType.CLOSE);
        }
    }
    
    /**
     * Updates a bracket if necessary
     * @param location The location of the bracket
     * @param bracketType The type of bracket
     */
    private void updateBracketIfNecessary(Location location, CodeBlock.BracketType bracketType) {
        CodeBlock bracketBlock = blockCodeBlocks.get(location);
        if (bracketBlock != null && bracketBlock.isBracket()) {
            // Ensure the bracket type is correct
            bracketBlock.setBracketType(bracketType);
        }
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
        
        // Check if brackets already exist at these positions
        Location openBracketLoc = block.getLocation().clone().add(1, 0, 0);
        Location closeBracketLoc = block.getLocation().clone().add(3, 0, 0);
        
        // Only create opening bracket if position is empty or not a protected bracket
        if (!isProtectedBracket(openBracketLoc) && !hasExistingBracket(openBracketLoc)) {
            Block openBracketBlock = block.getWorld().getBlockAt(openBracketLoc);
            openBracketBlock.setType(bracketMaterial);
            // Set piston facing direction (away from the center block)
            if (bracketMaterial == Material.PISTON || bracketMaterial == Material.STICKY_PISTON) {
                org.bukkit.block.data.Directional pistonData = (org.bukkit.block.data.Directional) openBracketBlock.getBlockData();
                pistonData.setFacing(org.bukkit.block.BlockFace.EAST); // Away from the block (toward positive X)
                openBracketBlock.setBlockData(pistonData);
            }
            
            // Register brackets as code blocks
            CodeBlock openBracket = new CodeBlock(bracketMaterial.name(), "BRACKET");
            openBracket.setBracketType(CodeBlock.BracketType.OPEN);
            blockCodeBlocks.put(openBracketLoc, openBracket);
            
            // Create sign for the bracket
            createSignForBlock(openBracketLoc, openBracket);
            
            // Add visual effects
            player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, openBracketLoc.clone().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        }
        
        // Only create closing bracket if position is empty or not a protected bracket
        if (!isProtectedBracket(closeBracketLoc) && !hasExistingBracket(closeBracketLoc)) {
            Block closeBracketBlock = block.getWorld().getBlockAt(closeBracketLoc);
            closeBracketBlock.setType(bracketMaterial);
            // Set piston facing direction (toward the center block)
            if (bracketMaterial == Material.PISTON || bracketMaterial == Material.STICKY_PISTON) {
                org.bukkit.block.data.Directional pistonData = (org.bukkit.block.data.Directional) closeBracketBlock.getBlockData();
                pistonData.setFacing(org.bukkit.block.BlockFace.WEST); // Toward the block (toward negative X)
                closeBracketBlock.setBlockData(pistonData);
            }
            
            CodeBlock closeBracket = new CodeBlock(bracketMaterial.name(), "BRACKET");
            closeBracket.setBracketType(CodeBlock.BracketType.CLOSE);
            blockCodeBlocks.put(closeBracketLoc, closeBracket);
            
            // Create sign for the bracket
            createSignForBlock(closeBracketLoc, closeBracket);
            
            // Add visual effects
            player.spawnParticle(org.bukkit.Particle.ENCHANTMENT_TABLE, closeBracketLoc.clone().add(0.5, 0.5, 0.5), 5, 0.3, 0.3, 0.3, 0);
        }
        
        player.sendMessage("§a✓ Brackets created automatically for constructor block");
    }

    /**
     * Handles non-code block placement (like pistons for brackets)
     */
    private boolean handleNonCodeBlockPlacement(BlockPlaceEvent event, Player player, Block block) {
        // Special handling for pistons (brackets)
        if (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) {
            CodeBlock newCodeBlock = new CodeBlock(block.getType().name(), "BRACKET");
            newCodeBlock.setBracketType(CodeBlock.BracketType.OPEN);
            blockCodeBlocks.put(block.getLocation(), newCodeBlock);
            
            // Create sign for the bracket
            createSignForBlock(block.getLocation(), newCodeBlock);
            
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
        
        // Check if this is a bracket block that should be protected
        if (isProtectedBracket(loc)) {
            // Cancel the event to prevent breaking brackets
            event.setCancelled(true);
            player.sendMessage("§cBrackets cannot be broken directly!");
            player.playSound(loc, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            return;
        }
        
        // Remove block from our map
        if (blockCodeBlocks.containsKey(loc)) {
            CodeBlock removedBlock = blockCodeBlocks.remove(loc);
            
            // Fire custom event for other systems to react to
            CodeBlockBrokenEvent brokenEvent = new CodeBlockBrokenEvent(player, removedBlock, loc);
            plugin.getServer().getPluginManager().callEvent(brokenEvent);
            
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
        
        // Check player mode - only open GUI in DEV mode
        if (plugin != null && plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            // Open GUI for configuration only in DEV mode
            if (modeManager.isInPlayMode(player)) {
                // If player is in PLAY mode, this click may be part of their game
                // We don't open GUI in PLAY mode
                return;
            }
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
            
            // For other blocks, open the appropriate GUI based on action, condition or event
            String actionId = codeBlock.getAction();
            String conditionId = codeBlock.getCondition();
            String eventId = codeBlock.getEvent();
            
            if ((actionId == null || "NOT_SET".equals(actionId)) && 
                (conditionId == null || "NOT_SET".equals(conditionId)) &&
                (eventId == null || "NOT_SET".equals(eventId))) {
                // Check if plugin and service registry are available
                if (plugin == null || plugin.getServiceRegistry() == null) {
                    player.sendMessage("§cPlugin services not available!");
                    return;
                }
                
                // Open action selection GUI
                com.megacreative.gui.coding.ActionSelectionGUI actionGUI = new com.megacreative.gui.coding.ActionSelectionGUI(
                    plugin, player, location, codeBlock.getMaterial());
                actionGUI.open();
            } else if (actionId != null && !actionId.equals("NOT_SET")) {
                // Open specialized parameter editor based on action ID
                openParameterEditor(player, codeBlock, actionId);
            } else if (conditionId != null && !conditionId.equals("NOT_SET")) {
                // Open specialized parameter editor based on condition ID
                openConditionEditor(player, codeBlock, conditionId);
            } else if (eventId != null && !eventId.equals("NOT_SET")) {
                // Open specialized parameter editor based on event ID
                openEventEditor(player, codeBlock, eventId);
            }
            return;
        }
    }
    
    /**
     * Opens the appropriate parameter editor based on the action ID
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     * @param actionId The action ID
     */
    private void openParameterEditor(Player player, CodeBlock codeBlock, String actionId) {
        // Check if plugin is available
        if (plugin == null) {
            player.sendMessage("§cPlugin not available!");
            return;
        }
        
        // Use GUIRegistry to open the appropriate editor
        if (plugin.getServiceRegistry() != null) {
            GUIRegistry guiRegistry = plugin.getServiceRegistry().getGuiRegistry();
            if (guiRegistry != null) {
                guiRegistry.open(actionId, plugin, player, codeBlock);
                return;
            }
        }
        
        // Fallback to enhanced parameter GUI if GUIRegistry is not available
        if (plugin.getServiceRegistry() != null) {
            com.megacreative.gui.coding.EnhancedActionParameterGUI enhancedGUI = 
                new com.megacreative.gui.coding.EnhancedActionParameterGUI(plugin);
            enhancedGUI.openParameterEditor(player, codeBlock.getLocation(), actionId);
        }
    }
    
    /**
     * Opens the appropriate parameter editor based on the event ID
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     * @param eventId The event ID
     */
    private void openEventEditor(Player player, CodeBlock codeBlock, String eventId) {
        // Check if plugin is available
        if (plugin == null) {
            player.sendMessage("§cPlugin not available!");
            return;
        }
        
        // Open the appropriate editor based on event ID
        switch (eventId) {
            case "onJoin":
                new PlayerJoinEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onLeave":
                new PlayerLeaveEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onChat":
                new PlayerChatEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onBlockBreak":
                new BlockBreakEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onBlockPlace":
                new BlockPlaceEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onPlayerMove":
                new PlayerMoveEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onPlayerDeath":
                new PlayerDeathEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onCommand":
                new CommandEventEditor(plugin, player, codeBlock).open();
                break;
                
            case "onTick":
                new TickEventEditor(plugin, player, codeBlock).open();
                break;
                
            // Add more cases for other events as needed
            default:
                // For events without specialized editors, show a simple message
                player.sendMessage("§aEvent configured: " + eventId);
                player.closeInventory();
                break;
        }
    }
    
    /**
     * Opens the appropriate parameter editor based on the condition ID
     * @param player The player using the editor
     * @param codeBlock The code block being edited
     * @param conditionId The condition ID
     */
    private void openConditionEditor(Player player, CodeBlock codeBlock, String conditionId) {
        // Check if plugin is available
        if (plugin == null) {
            player.sendMessage("§cPlugin not available!");
            return;
        }
        
        // Open the appropriate editor based on condition ID
        switch (conditionId) {
            case "compareVariable":
                new CompareVariableEditor(plugin, player, codeBlock).open();
                break;
                
            case "hasItem":
                new HasItemEditor(plugin, player, codeBlock).open();
                break;
                
            case "isInWorld":
                new IsInWorldEditor(plugin, player, codeBlock).open();
                break;
                
            case "isNearBlock":
                new IsNearBlockEditor(plugin, player, codeBlock).open();
                break;
                
            case "playerHealth":
                new PlayerHealthEditor(plugin, player, codeBlock).open();
                break;
                
            case "ifVarEquals":
                new IfVarEqualsEditor(plugin, player, codeBlock).open();
                break;
                
            case "ifVarGreater":
                new IfVarGreaterEditor(plugin, player, codeBlock).open();
                break;
                
            case "ifVarLess":
                new IfVarLessEditor(plugin, player, codeBlock).open();
                break;
                
            case "isOp":
                new IsOpEditor(plugin, player, codeBlock).open();
                break;
                
            case "worldTime":
                new WorldTimeEditor(plugin, player, codeBlock).open();
                break;
                
            case "mobNear":
                new MobNearEditor(plugin, player, codeBlock).open();
                break;
                
            case "playerGameMode":
                new PlayerGameModeEditor(plugin, player, codeBlock).open();
                break;
                
            case "hasPermission":
                new HasPermissionEditor(plugin, player, codeBlock).open();
                break;
                
            case "isBlockType":
                new IsBlockTypeEditor(plugin, player, codeBlock).open();
                break;
                
            case "isPlayerHolding":
                new IsPlayerHoldingEditor(plugin, player, codeBlock).open();
                break;
                
            case "isNearEntity":
                new IsNearEntityEditor(plugin, player, codeBlock).open();
                break;
                
            case "hasArmor":
                new HasArmorEditor(plugin, player, codeBlock).open();
                break;
                
            case "isNight":
                new IsNightEditor(plugin, player, codeBlock).open();
                break;
                
            case "isRiding":
                new IsRidingEditor(plugin, player, codeBlock).open();
                break;
                
            case "checkPlayerInventory":
                new CheckPlayerInventoryEditor(plugin, player, codeBlock).open();
                break;
                
            case "checkPlayerStats":
                new CheckPlayerStatsEditor(plugin, player, codeBlock).open();
                break;
                
            case "checkServerOnline":
                new CheckServerOnlineEditor(plugin, player, codeBlock).open();
                break;
                
            case "checkWorldWeather":
                new CheckWorldWeatherEditor(plugin, player, codeBlock).open();
                break;
                
            case "worldGuardRegionCheck":
                new WorldGuardRegionCheckEditor(plugin, player, codeBlock).open();
                break;
                
            // Add more cases for other conditions as needed
            default:
                // For conditions without specialized editors, use the generic parameter GUI
                new com.megacreative.coding.CodingParameterGUI(
                    player, 
                    conditionId, 
                    codeBlock.getLocation(), 
                    parameters -> {
                        // Apply parameters to the code block
                        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                            codeBlock.setParameter(entry.getKey(), com.megacreative.coding.values.DataValue.fromObject(entry.getValue()));
                        }
                        player.sendMessage("§aПараметры условия сохранены!");
                    },
                    plugin.getGuiManager()
                ).open();
                break;
        }
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
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_");
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
        if (blockConfigService == null || !blockConfigService.isCodeBlock(material)) {
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
                
                bracketBlock.setLocation(location);
                blockCodeBlocks.put(location, bracketBlock);
                return bracketBlock;
            }
            return null;
        }
        
        // Create a new code block
        CodeBlock codeBlock = new CodeBlock(material.name(), "NOT_SET");
        codeBlock.setLocation(location);
        
        // Try to extract action/event from sign
        String[] lines = sign.getLines();
        if (lines.length > 1) {
            String line1 = lines[1];
            // Try to extract action from the sign text
            // Look for patterns like "[Action: sendMessage]" or "sendMessage"
            if (line1.contains("Action:")) {
                int start = line1.indexOf("Action:") + 7;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String action = line1.substring(start, end).trim();
                    codeBlock.setAction(action);
                }
            } else if (line1.contains("Event:")) {
                // Try to extract event from the sign text
                int start = line1.indexOf("Event:") + 6;
                int end = line1.indexOf("]", start);
                if (start > 0 && end > start) {
                    String event = line1.substring(start, end).trim();
                    codeBlock.setEvent(event);
                }
            } else if (line1.contains("§")) {
                // Try to extract action from colored text
                // This is a simplified approach - in a real implementation, 
                // you might want to store action data in the sign's persistent data
                String cleanLine = org.bukkit.ChatColor.stripColor(line1).trim();
                if (!cleanLine.isEmpty() && !"NOT_SET".equals(cleanLine)) {
                    codeBlock.setAction(cleanLine);
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
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld == null) {
            plugin.getLogger().warning("No CreativeWorld found for Bukkit world: " + world.getName());
            return;
        }
        
        // Save the world to persist any changes to code blocks
        plugin.getWorldManager().saveWorld(creativeWorld);
        plugin.getLogger().fine("Saved all code blocks in world: " + world.getName());
    }
    
    /**
     * Gets the event names this subscriber is interested in.
     * 
     * @return An array of event names to subscribe to
     */
    @Override
    public String[] getSubscribedEvents() {
        return SUBSCRIBED_EVENTS;
    }
    
    /**
     * Handles an event that has been published.
     * 
     * @param eventName The name of the event
     * @param eventData The data associated with the event
     * @param source The player that triggered the event (can be null)
     * @param worldName The world where the event occurred
     */
    @Override
    public void handleEvent(String eventName, Map<String, DataValue> eventData, Player source, String worldName) {
        switch (eventName) {
            case "block_place_requested":
                handleBlockPlaceRequest(eventData, source, worldName);
                break;
            case "block_placement_confirmed":
                handleBlockPlacementConfirmation(eventData, source, worldName);
                break;
            default:
                log.fine("BlockPlacementHandler received unhandled event: " + eventName);
                break;
        }
    }
    
    /**
     * Handles a block place request event
     */
    private void handleBlockPlaceRequest(Map<String, DataValue> eventData, Player source, String worldName) {
        // This could be used to validate or preprocess block placement requests
        log.info("Received block place request for player: " + (source != null ? source.getName() : "unknown"));
    }
    
    /**
     * Handles a block placement confirmation event
     */
    private void handleBlockPlacementConfirmation(Map<String, DataValue> eventData, Player source, String worldName) {
        // This could be used to perform additional actions after block placement is confirmed
        log.info("Received block placement confirmation for player: " + (source != null ? source.getName() : "unknown"));
    }
    
    /**
     * Gets the priority of this subscriber for event handling.
     * Higher priority subscribers are called first.
     * 
     * @return The priority level (higher numbers = higher priority)
     */
    @Override
    public int getPriority() {
        return 50; // Medium priority
    }
}