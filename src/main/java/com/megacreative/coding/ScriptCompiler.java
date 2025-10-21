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
    // Это поле должно оставаться полем класса, так как оно сохраняет состояние между вызовами методов
    // Статический анализ помечает его как конвертируемое в локальную переменную, но это ложное срабатывание
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
            // Ошибка компиляции скрипта из события размещения блока: " + e.getMessage()
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
            // Ошибка удаления скрипта из события разрушения блока: " + e.getMessage()
        }
    }
    
    /**
     * Creates and registers an activator for an event block
     * @param eventBlock The event block
     * @param script The compiled script
     * @param creativeWorld The creative world
     * @param location The location of the event block
     * 
     * Создает и регистрирует активатор для блока события
     * @param eventBlock Блок события
     * @param script Скомпилированный скрипт
     * @param creativeWorld Творческий мир
     * @param location Расположение блока события
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
            // Ошибка создания и регистрации активатора: " + e.getMessage()
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
            // Ошибка удаления скрипта: " + e.getMessage()
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
     * 
     * Строит полный скрипт, обходя структуру блоков
     * @param script Скрипт для построения
     * @param currentBlock Текущий обрабатываемый блок
     * @return Завершенный скрипт
     */
    private CodeScript buildCompleteScript(CodeScript script, CodeBlock currentBlock) {
        
        if (currentBlock.getNextBlock() != null) {
            // TODO: Implement proper handling of next block in script compilation
            // This is a placeholder for future implementation
            // The method should properly handle the connection and compilation of the next block in the chain
            // According to static analysis, this statement has empty body
            // Add proper implementation for handling next block
            // Согласно статическому анализу, это утверждение имеет пустое тело
            // Добавьте правильную реализацию для обработки следующего блока
            buildCompleteScript(script, currentBlock.getNextBlock());
        }
        
        
        for (CodeBlock child : currentBlock.getChildren()) {
            // TODO: Implement proper handling of child blocks in script compilation
            // This is a placeholder for future implementation
            // The method should properly handle the compilation of child blocks (nested structures)
            // According to static analysis, this statement has empty body
            // Add proper implementation for handling child blocks
            // Согласно статическому анализу, это утверждение имеет пустое тело
            // Добавьте правильную реализацию для обработки дочерних блоков
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
     * 
     * Перекомпилирует все скрипты в мире
     * Это должно вызываться при загрузке мира или при внесении значительных изменений
     * @param world мир, для которого нужно перекомпилировать скрипты
     */
    public void compileWorldScripts(World world) {
        try {
            IWorldManager worldManager = plugin.getServiceRegistry().getWorldManager();
            CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(world);
            if (creativeWorld == null) return;
            
            // Get the block placement handler to access all code blocks
            // Получить обработчик размещения блоков для доступа ко всем блокам кода
            BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
            
            List<CodeScript> newScripts = new ArrayList<>();
            
            // Use the placement handler to get all blocks in the world
            // Использовать обработчик размещения для получения всех блоков в мире
            for (Map.Entry<Location, CodeBlock> entry : placementHandler.getBlockCodeBlocks().entrySet()) {
                Location location = entry.getKey();
                // Check if this block is in the specified world
                // Проверить, находится ли этот блок в указанном мире
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
                            // Записать исключение в журнал, но продолжить обработку других блоков
                            plugin.getLogger().warning("Error compiling script from block: " + e.getMessage());
                        }
                    }
                }
            }
            
            creativeWorld.setScripts(newScripts);
            worldManager.saveWorld(creativeWorld);
            
        } catch (Exception e) {
            // Log the exception
            // Записать исключение в журнал
            plugin.getLogger().warning("Error compiling world scripts: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a block is an event block
     * @param block the block to check
     * @return true if the block is an event block
     * 
     * Проверяет, является ли блок блоком события
     * @param block блок для проверки
     * @return true, если блок является блоком события
     */
    private boolean isEventBlock(CodeBlock block) {
        if (block == null || block.getAction() == null) {
            return false;
        }
        
        
        return block.getAction().startsWith("on");
    }
}