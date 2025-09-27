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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;

// Отвечает ИСКЛЮЧИТЕЛЬНО за компиляцию и управление CodeScript'ами.
// Слушает события добавления/удаления блоков и пересобирает скрипты в мире.
public class ScriptCompiler implements Listener {
    
    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final BlockLinker blockLinker; // Нужен для сборки цепочки блоков

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
            
            // Compile script from this event block
            CodeScript script = compileScriptFromEventBlock(eventBlock);
            
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());
            
            if (creativeWorld != null) {
                // Create and register activator for this script
                createAndRegisterActivator(eventBlock, script, creativeWorld, location);
                
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
            }
        } catch (Exception e) {
            // Ошибка компиляции скрипта
        }
    }
    
    @EventHandler
    public void onBlockBreak(CodeBlockBrokenEvent event) {
        try {
            Location location = event.getLocation();
            CodeBlock eventBlock = event.getCodeBlock();
            
            // Remove script associated with this event block
            removeScript(eventBlock, location);
        } catch (Exception e) {
            // Ошибка удаления скрипта
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
            // Get the code handler for this world
            CodeHandler codeHandler = creativeWorld.getCodeHandler();
            if (codeHandler == null) {
                return;
            }
            
            // Get the script engine
            ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
            
            // Create the appropriate activator based on the event type
            Activator activator = null;
            
            if ("onJoin".equals(eventBlock.getAction())) {
                activator = new PlayerJoinActivator(creativeWorld, scriptEngine);
            } else if ("onPlayerMove".equals(eventBlock.getAction())) {
                activator = new PlayerMoveActivator(creativeWorld, scriptEngine);
            } else if ("onBlockPlace".equals(eventBlock.getAction())) {
                activator = new BlockPlaceActivator(creativeWorld, scriptEngine);
            } else if ("onBlockBreak".equals(eventBlock.getAction())) {
                activator = new BlockBreakActivator(creativeWorld, scriptEngine);
            } else if ("onChat".equals(eventBlock.getAction())) {
                activator = new ChatActivator(creativeWorld, scriptEngine);
            } else if ("onPlayerQuit".equals(eventBlock.getAction())) {
                activator = new PlayerQuitActivator(creativeWorld, scriptEngine);
            } else if ("onPlayerDeath".equals(eventBlock.getAction())) {
                activator = new PlayerDeathActivator(creativeWorld, scriptEngine);
            } else if ("onPlayerRespawn".equals(eventBlock.getAction())) {
                activator = new PlayerRespawnActivator(creativeWorld, scriptEngine);
            } else if ("onPlayerTeleport".equals(eventBlock.getAction())) {
                activator = new PlayerTeleportActivator(creativeWorld, scriptEngine);
            } else if ("onEntityPickupItem".equals(eventBlock.getAction())) {
                activator = new EntityPickupItemActivator(creativeWorld, scriptEngine);
            }
            
            // If we created an activator, configure it and register it
            if (activator != null) {
                activator.setEventBlock(eventBlock);
                activator.setScript(script);
                
                // Set location for BukkitEventActivator instances
                if (activator instanceof BukkitEventActivator) {
                    ((BukkitEventActivator) activator).setLocation(location);
                } else if (activator instanceof PlayerJoinActivator) {
                    ((PlayerJoinActivator) activator).setLocation(location);
                }
                
                codeHandler.registerActivator(activator);
            }
        } catch (Exception e) {
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
        }
    }
    
    private CodeScript compileScriptFromEventBlock(CodeBlock eventBlock) {
        // Compilation logic here. It simply follows the `nextBlock` and `children` links
        // that were already set up by BlockLinker.
        
        // IMPORTANT: We don't rebuild the links here. We trust them.
        // BlockLinker is responsible for the structure, and the compiler is responsible for reading it.
        
        return new CodeScript(eventBlock); // For now, create a script with only the root block
    }

    private void addScriptToWorld(CodeBlock eventBlock, CodeScript script, CreativeWorld creativeWorld, IWorldManager worldManager) {
        List<CodeScript> scripts = creativeWorld.getScripts();
        if (scripts == null) {
            scripts = new ArrayList<>();
            creativeWorld.setScripts(scripts);
        }
        // Избегаем дубликатов, удаляя старый скрипт, если он был привязан к этому же блоку
        scripts.removeIf(existingScript -> existingScript.getRootBlock().getId().equals(eventBlock.getId()));
        scripts.add(script);
        worldManager.saveWorld(creativeWorld);
    }
    
    /**
     * Recompiles all scripts in a world
     * This should be called when the world is loaded or when significant changes are made
     * @param world the world to recompile scripts for
     */
    public void recompileWorldScripts(org.bukkit.World world) {
        try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            if (worldManager == null) {
                return;
            }
            
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
            if (creativeWorld == null) return;
            
            // Clear existing scripts
            List<CodeScript> newScripts = new ArrayList<>();
            
            // Find all event blocks in the world and compile scripts from them
            for (Map.Entry<Location, CodeBlock> entry : blockLinker.getWorldBlocks(world).entrySet()) {
                CodeBlock block = entry.getValue();
                if (isEventBlock(block)) {
                    try {
                        CodeScript compiledScript = compileScriptFromEventBlock(block);
                        if (compiledScript != null) {
                            newScripts.add(compiledScript);
                        }
                    } catch (Exception e) {
                        // Ignore errors during compilation
                    }
                }
            }
            
            // Update the creative world with new scripts
            creativeWorld.setScripts(newScripts);
            worldManager.saveWorld(creativeWorld);
            
        } catch (Exception e) {
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
        
        // Check if the block action starts with "on" which indicates an event
        return block.getAction().startsWith("on");
    }
}