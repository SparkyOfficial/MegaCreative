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

/**
 * Отвечает ИСКЛЮЧИТЕЛЬНО за компиляцию и управление CodeScript'ами.
 * Слушает события добавления/удаления блоков и пересобирает скрипты в мире.
 */
public class ScriptCompiler implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(ScriptCompiler.class.getName());

    private final MegaCreative plugin;
    private final BlockConfigService blockConfigService;
    private final BlockLinker blockLinker; // Нужен для сборки цепочки блоков

    public ScriptCompiler(MegaCreative plugin, BlockConfigService blockConfigService, BlockLinker blockLinker) {
        this.plugin = plugin;
        this.blockConfigService = blockConfigService;
        this.blockLinker = blockLinker;
    }
    
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        if (isEventBlock(event.getCodeBlock())) {
            createAndAddScript(event.getCodeBlock(), event.getPlayer(), event.getLocation());
        }
    }

    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        if (isEventBlock(event.getCodeBlock())) {
            removeScript(event.getCodeBlock(), event.getLocation());
        }
    }

    private boolean isEventBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) return false;
        BlockConfigService.BlockConfig config = blockConfigService.getBlockConfig(block.getAction());
        if (config == null) return false;
        return "EVENT".equals(config.getType());
    }
    
    private void createAndAddScript(CodeBlock eventBlock, Player player, Location location) {
        try {
            CodeScript script = compileScriptFromEventBlock(eventBlock);
            if (script == null) {
                player.sendMessage("§cОшибка компиляции скрипта!");
                return;
            }

            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(location.getWorld());

            if (creativeWorld != null) {
                // Create and register activator for this script
                createAndRegisterActivator(eventBlock, script, creativeWorld, location);
                
                addScriptToWorld(eventBlock, script, creativeWorld, worldManager);
                player.sendMessage("§a✓ Скрипт скомпилирован для события: §f" + eventBlock.getAction());
                LOGGER.fine("Compiled and added script for event block: " + eventBlock.getAction());
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to create script: " + e.getMessage());
            e.printStackTrace();
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
                LOGGER.warning("No code handler found for world: " + creativeWorld.getId());
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
                LOGGER.fine("Registered activator for event: " + eventBlock.getAction());
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to create activator for event block: " + eventBlock.getAction() + " - " + e.getMessage());
            e.printStackTrace();
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
                        LOGGER.fine("Removed script for event block: " + eventBlock.getAction());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Failed to remove script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private CodeScript compileScriptFromEventBlock(CodeBlock eventBlock) {
        // Логика компиляции здесь. Она просто следует по `nextBlock` и `children` связям,
        // которые уже установил BlockLinker.
        
        // ВАЖНО: Мы не перестраиваем связи здесь. Мы им доверяем.
        // BlockLinker отвечает за структуру, а компилятор - за ее чтение.
        
        return new CodeScript(eventBlock); // Для простоты пока создаем скрипт только с корневым блоком
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
        LOGGER.fine("Recompiling all scripts for world: " + world.getName());
        
        try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            if (worldManager == null) {
                LOGGER.warning("World manager not available");
                return;
            }
            
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
            if (creativeWorld == null) return;
            
            // Clear existing scripts
            List<CodeScript> newScripts = new ArrayList<>();
            
            // Find all event blocks in the world and compile scripts from them
            int scriptCount = 0;
            int errorCount = 0;
            
            for (Map.Entry<Location, CodeBlock> entry : blockLinker.getWorldBlocks(world).entrySet()) {
                CodeBlock block = entry.getValue();
                if (isEventBlock(block)) {
                    try {
                        CodeScript compiledScript = compileScriptFromEventBlock(block);
                        if (compiledScript != null) {
                            newScripts.add(compiledScript);
                            scriptCount++;
                            LOGGER.fine("Successfully compiled script: " + compiledScript.getName());
                        } else {
                            errorCount++;
                            LOGGER.warning("Failed to compile script from event block at " + entry.getKey());
                        }
                    } catch (Exception e) {
                        errorCount++;
                        LOGGER.log(java.util.logging.Level.SEVERE, "Error compiling script from event block at " + entry.getKey() + ": " + e.getMessage(), e);
                    }
                }
            }
            
            // Update the creative world with new scripts
            creativeWorld.setScripts(newScripts);
            worldManager.saveWorld(creativeWorld);
            
            LOGGER.fine("Recompiled " + scriptCount + " scripts for world: " + world.getName() + " with " + errorCount + " errors");
        } catch (Exception e) {
            LOGGER.severe("Failed to recompile world scripts: " + e.getMessage());
            e.printStackTrace();
        }
    }
}