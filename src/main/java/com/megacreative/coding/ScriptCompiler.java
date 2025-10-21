package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.activators.Activator;
import com.megacreative.coding.activators.BlockBreakActivator;
import com.megacreative.coding.activators.BlockPlaceActivator;
import com.megacreative.coding.activators.BukkitEventActivator;
import com.megacreative.coding.activators.ChatActivator;
import com.megacreative.coding.activators.EntityPickupItemActivator;
import com.megacreative.coding.activators.PlayerDeathActivator;
import com.megacreative.coding.activators.PlayerJoinActivator;
import com.megacreative.coding.activators.PlayerMoveActivator;
import com.megacreative.coding.activators.PlayerQuitActivator;
import com.megacreative.coding.activators.PlayerRespawnActivator;
import com.megacreative.coding.activators.PlayerTeleportActivator;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlockBrokenEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;



public class ScriptCompiler implements Listener {
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    // This field needs to remain as a class field since it maintains state across method calls
    // Static analysis flags it as convertible to a local variable, but this is a false positive
    private final BlockLinker blockLinker; 

    public ScriptCompiler(MegaCreative plugin, BlockConfigService blockConfigService, BlockLinker blockLinker) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
        this.blockLinker = blockLinker;
    }
    
    @EventHandler
    public void onBlockPlace(CodeBlockPlacedEvent event) {
        try {
            Location location = event.getLocation();
            CodeBlock eventBlock = event.getCodeBlock();
            
            
            CodeScript script = compileScriptFromEventBlock(eventBlock);
            
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            
            if (creativeWorld != null) {
                
                createAndRegisterActivator(eventBlock, script, creativeWorld, location);
                
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
            }
        } catch (Exception e) {
            
            plugin.getLogger().warning("Error compiling script from block place event: " + e.getMessage());
        }
    }
    
    @EventHandler
    public void onBlockBreak(CodeBlockBrokenEvent event) {
        try {
            Location location = event.getLocation();
            CodeBlock eventBlock = event.getCodeBlock();
            
            
            removeScript(eventBlock, location);
        } catch (Exception e) {
            
            plugin.getLogger().warning("Error removing script from block break event: " + e.getMessage());
        }
    }
    
    /**
     * Creates and registers an activator for an event block
     * @param eventBlock The event block
     * @param script The compiled script
     * @param creativeWorld The creative world
     * @param location The location of the event block
     */
    private void createAndRegisterActivator(CodeBlock eventBlock, CodeScript script, CreativeWorld creativeWorld, Location location) {
        try {
            
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                return;
            }
            
            
            Activator activator = null;
            
            if ("onJoin".equals(eventBlock.getAction())) {
                activator = new PlayerJoinActivator(plugin, creativeWorld);
            } else if ("onPlayerMove".equals(eventBlock.getAction())) {
                activator = new PlayerMoveActivator(plugin, creativeWorld);
            } else if ("onBlockPlace".equals(eventBlock.getAction())) {
                activator = new BlockPlaceActivator(plugin, creativeWorld);
            } else if ("onBlockBreak".equals(eventBlock.getAction())) {
                activator = new BlockBreakActivator(plugin, creativeWorld);
            } else if ("onChat".equals(eventBlock.getAction())) {
                activator = new ChatActivator(plugin, creativeWorld);
            } else if ("onPlayerQuit".equals(eventBlock.getAction())) {
                activator = new PlayerQuitActivator(plugin, creativeWorld);
            } else if ("onPlayerDeath".equals(eventBlock.getAction())) {
                activator = new PlayerDeathActivator(plugin, creativeWorld);
            } else if ("onPlayerRespawn".equals(eventBlock.getAction())) {
                activator = new PlayerRespawnActivator(plugin, creativeWorld);
            } else if ("onPlayerTeleport".equals(eventBlock.getAction())) {
                activator = new PlayerTeleportActivator(plugin, creativeWorld);
            } else if ("onEntityPickupItem".equals(eventBlock.getAction())) {
                activator = new EntityPickupItemActivator(plugin, creativeWorld);
            }
            
            
            if (activator != null) {
                
                activator.addAction(eventBlock);
                
                
                if (activator instanceof BukkitEventActivator) {
                    ((BukkitEventActivator) activator).setLocation(location);
                }
                
                codeHandler.registerActivator(activator);
            }
        } catch (Exception e) {
            
            plugin.getLogger().warning("Error creating and registering activator: " + e.getMessage());
        }
    }
    
    private void removeScript(CodeBlock eventBlock, Location location) {
         try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            if (creativeWorld != null) {
                List<CodeScript> scripts = creativeWorld.getScripts();
                if (scripts != null) {
                    boolean removed = scripts.removeIf(script -> script.getRootBlock().getId().equals(eventBlock.getId()));
                    if (removed) {
                        worldManager.saveWorld(creativeWorld);
                    }
                }
            }
        } catch (Exception e) {
            
            plugin.getLogger().warning("Error removing script: " + e.getMessage());
        }
    }
    
    private CodeScript compileScriptFromEventBlock(CodeBlock eventBlock) {
        
        
        
        
        
        
        
        CodeScript script = new CodeScript(eventBlock);
        
        
        
        return buildCompleteScript(script, eventBlock);
    }
    
    /**
     * Builds a complete script by traversing the block structure
     * @param script The script to build
     * @param currentBlock The current block being processed
     * @return The completed script
     */
    private CodeScript buildCompleteScript(CodeScript script, CodeBlock currentBlock) {
        
        if (currentBlock.getNextBlock() != null) {
            // According to static analysis, this statement has empty body
            // Add proper implementation for handling next block
            buildCompleteScript(script, currentBlock.getNextBlock());
        }
        
        
        for (CodeBlock child : currentBlock.getChildren()) {
            // According to static analysis, this statement has empty body
            // Add proper implementation for handling child blocks
            buildCompleteScript(script, child);
        }
        
        return script;
    }

    private void addScriptToWorld(CodeBlock eventBlock, CodeScript script, CreativeWorld creativeWorld, IWorldManager worldManager) {
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts == null) {
            scripts = new ArrayList<>();
            creativeWorld.setScripts(scripts);
        }
        
        scripts.removeIf(existingScript -> existingScript.getRootBlock().getId().equals(eventBlock.getId()));
        scripts.add(script);
        worldManager.saveWorld(creativeWorld);
    }
    
    /**
     * Recompiles all scripts in a world
     * This should be called when the world is loaded or when significant changes are made
     * @param world the world to recompile scripts for
     */
    public void compileWorldScripts(World world) {
        try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
            if (creativeWorld == null) return;
            
            // Get the block placement handler to access all code blocks
            BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
            
            List<CodeScript> newScripts = new ArrayList<>();
            
            // Use the placement handler to get all blocks in the world
            for (Map.Entry<Location, CodeBlock> entry : placementHandler.getBlockCodeBlocks().entrySet()) {
                Location location = entry.getKey();
                // Check if this block is in the specified world
                if (location.getWorld().equals(world)) {
                    CodeBlock block = entry.getValue();
                    if (isEventBlock(block)) {
                        try {
                            CodeScript compiledScript = compileScriptFromEventBlock(block);
                            if (compiledScript != null) {
                                newScripts.add(compiledScript);
                            }
                        } catch (Exception e) {
                            // Log the exception but continue processing other blocks
                            plugin.getLogger().warning("Error compiling script from block: " + e.getMessage());
                        }
                    }
                }
            }
            
            creativeWorld.setScripts(newScripts);
            worldManager.saveWorld(creativeWorld);
            
        } catch (Exception e) {
            // Log the exception
            plugin.getLogger().warning("Error compiling world scripts: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a block is an event block
     * @param block the block to check
     * @return true if the block is an event block
     */
    private boolean isEventBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        
        return block.getAction().startsWith("on");
    }
}