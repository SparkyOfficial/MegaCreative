package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * ScriptCompiler handles script compilation logic
 * Listens to CodeBlockPlacedEvent and CodeBlockBrokenEvent to manage script compilation
 */
public class ScriptCompiler implements Listener {
    
    private static final Logger log = Logger.getLogger(ScriptCompiler.class.getName());
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    
    public ScriptCompiler(MegaCreative plugin, BlockConfigService blockConfigService) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
    }
    
    /**
     * Handles code block placement events
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        Player player = event.getPlayer();
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        // If this is an event block, create a script and add it to the world
        if (isEventBlock(codeBlock)) {
            createAndAddScript(codeBlock, player, location);
        }
    }
    
    /**
     * Handles code block broken events
     */
    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        Location location = event.getLocation();
        CodeBlock codeBlock = event.getCodeBlock();
        
        // If this is an event block, remove the corresponding script from the world
        if (isEventBlock(codeBlock)) {
            removeScript(codeBlock, location);
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
            
            // Find the creative world using service registry
            com.megacreative.interfaces.IWorldManager worldManager = getWorldManager();
            if (worldManager == null) {
                plugin.getLogger().warning("World manager not available");
                return;
            }
            
            com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                // Add the script to the world
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
                
                player.sendMessage("§a✓ Скрипт скомпилирован и создан для события: §f" + eventBlock.getAction());
                plugin.getLogger().fine("Compiled and added script for event block: " + eventBlock.getAction() + " in world: " + creativeWorld.getName());
            } else {
                plugin.getLogger().warning("Could not find creative world for location: " + location);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create script for event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
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
            
            com.megacreative.models.CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                // Remove the script from the world
                removeScriptFromWorld(eventBlock, creativeWorld, worldManager);
            } else {
                plugin.getLogger().warning("Could not find creative world for location: " + location);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to remove script for event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
    
    /**
     * Compiles a complete script from an event block by following all connections
     */
    public CodeScript compileScriptFromEventBlock(CodeBlock eventBlock, Location eventLocation) {
        try {
            // Create the root script
            CodeScript script = new CodeScript(eventBlock);
            script.setName("Compiled Script for " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
            script.setEnabled(true);
            script.setType(CodeScript.ScriptType.EVENT);
            
            plugin.getLogger().fine("Starting compilation of script from event block: " + eventBlock.getAction() + " at " + formatLocation(eventLocation));
            
            plugin.getLogger().fine("Successfully compiled script from event block: " + eventBlock.getAction() + " with connected blocks");
            return script;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to compile script from event block: " + e.getMessage());
            plugin.getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return null;
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
                plugin.getLogger().fine("Removed script for event block: " + eventBlock.getAction() + " from world: " + creativeWorld.getName());
            }
        }
    }
    
    /**
     * Helper method to get world manager safely
     */
    private com.megacreative.interfaces.IWorldManager getWorldManager() {
        if (plugin == null || plugin.getServiceRegistry() == null) {
            return null;
        }
        return plugin.getServiceRegistry().getWorldManager();
    }
    
    /**
     * Formats a location for logging/display purposes
     */
    private String formatLocation(Location location) {
        if (location == null) return "null";
        return String.format("(%d, %d, %d)", location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }
    
    /**
     * Recompiles all scripts in a world
     */
    public void recompileWorldScripts(org.bukkit.World world) {
        if (plugin != null) {
            plugin.getLogger().fine("Recompiling all scripts for world: " + world.getName());
        }
    }
}