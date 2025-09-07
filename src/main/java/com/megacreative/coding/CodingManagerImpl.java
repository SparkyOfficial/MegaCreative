package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.debug.VisualDebugger;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Управляет всеми аспектами системы кодирования: загрузкой, сохранением и выполнением скриптов.
 */
public class CodingManagerImpl implements ICodingManager, Listener {

    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final ScriptEngine scriptEngine;
    private final VariableManager variableManager;
    private final VisualDebugger debugger;
    private final Map<String, List<CodeScript>> worldScripts = new HashMap<>();
    // Global and server variables are now managed by VariableManager in ScriptEngine
    
    /**
     * Выполняет скрипт с указанным триггером.
     * @param script Скрипт для выполнения
     * @param player Игрок, инициировавший выполнение
     * @param trigger Триггер выполнения
     */
    @Override
    public void executeScript(CodeScript script, Player player, String trigger) {
        if (scriptEngine == null) {
            plugin.getLogger().severe("Cannot execute script: ScriptEngine is not available");
            return;
        }
        
        // Execute script asynchronously
        scriptEngine.executeScript(script, player, trigger)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    plugin.getLogger().severe("Error executing script " + script.getId() + ": " + throwable.getMessage());
                    if (debugger != null) {
                        debugger.logError(player, "Error executing script: " + throwable.getMessage());
                    }
                } else if (result != null && !result.isSuccess() && debugger != null) {
                    debugger.logError(player, "Script execution failed: " + result.getErrorMessage());
                }
            });
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return scriptEngine;
    }
    
    @Override
    public void cancelScriptExecution(String scriptId) {
        if (scriptEngine != null) {
            scriptEngine.stopExecution(scriptId);
        }
    }
    
    @Override
    public void shutdown() {
        // Clear world scripts cache
        worldScripts.clear();
        
        // ScriptEngine and its resources are managed by ServiceRegistry
    }
    
    public CodingManagerImpl(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        
        // Get services from ServiceRegistry
        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        this.scriptEngine = serviceRegistry.getService(ScriptEngine.class);
        this.variableManager = scriptEngine.getVariableManager();
        this.debugger = serviceRegistry.getService(VisualDebugger.class);
        
        if (scriptEngine == null) {
            throw new IllegalStateException("ScriptEngine service is not available");
        }
        
        // Register event listeners
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public void loadScriptsForWorld(CreativeWorld world) {
        UUID worldUniqueId = Bukkit.getWorld(world.getWorldName()).getUID();
        worldScripts.put(world.getId(), world.getScripts());
        plugin.getLogger().info("Загружено " + world.getScripts().size() + " скриптов для мира " + world.getName());
    }

    @Override
    public void unloadScriptsForWorld(CreativeWorld world) {
        UUID worldUniqueId = Bukkit.getWorld(world.getWorldName()).getUID();
        if (worldScripts.containsKey(world.getId())) {
            int count = worldScripts.get(world.getId()).size();
            worldScripts.remove(world.getId());
            plugin.getLogger().info("Выгружено " + count + " скриптов для мира " + world.getName());
        }
    }
    
    @Override
    public CodeScript getScript(String name) {
        // Поиск скрипта по имени
        for (List<CodeScript> scripts : worldScripts.values()) {
            for (CodeScript script : scripts) {
                if (script.getName().equals(name)) {
                    return script;
                }
            }
        }
        return null;
    }
    
    @Override
    public List<CodeScript> getWorldScripts(CreativeWorld world) {
        return worldScripts.getOrDefault(world.getId(), new ArrayList<>());
    }
    
    @Override
    public void saveScript(CodeScript script) {
        // Сохранение скрипта
    }
    
    @Override
    public void deleteScript(String scriptName) {
        // Удаление скрипта
    }
    
    @Override
    public Object getGlobalVariable(String key) {
        return scriptEngine.getVariableManager().getGlobalVariable(key);
    }

    @Override
    public void setGlobalVariable(String key, Object value) {
        scriptEngine.getVariableManager().setGlobalVariable(key, value);
    }

    @Override
    public Object getServerVariable(String key) {
        return scriptEngine.getVariableManager().getServerVariable(key);
    }

    @Override
    public void setServerVariable(String key, Object value) {
        scriptEngine.getVariableManager().setServerVariable(key, value);
    }
    
    @Override
    public Map<String, Object> getGlobalVariables() {
        return scriptEngine.getVariableManager().getGlobalVariables();
    }
    
    @Override
    public Map<String, Object> getServerVariables() {
        return scriptEngine.getVariableManager().getServerVariables();
    }
    
    @Override
    public void clearVariables() {
        scriptEngine.getVariableManager().clearGlobalVariables();
        scriptEngine.getVariableManager().clearServerVariables();
    }



    // --- Обработчики событий для выполнения скриптов ---

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String worldId = event.getPlayer().getWorld().getName();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onJoin".equals(script.getRootBlock().getAction())) {
                    CreativeWorld creativeWorld = worldManager.getWorldByName(worldId);
                    if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
                        continue; // Пропускаем выполнение если код выключен
                    }
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    
                    // Use unified event data extraction system
                    EventDataExtractorRegistry extractorRegistry = plugin.getServiceRegistry().getEventDataExtractorRegistry();
                    extractorRegistry.populateContext(event, context);
                    
                    scriptExecutor.execute(script, context, "onJoin");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String worldId = event.getPlayer().getWorld().getName();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onChat".equals(script.getRootBlock().getAction())) {
                    CreativeWorld creativeWorld = worldManager.getWorldByName(worldId);
                    if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
                        continue; // Пропускаем выполнение если код выключен
                    }
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    
                    // Use unified event data extraction system
                    EventDataExtractorRegistry extractorRegistry = plugin.getServiceRegistry().getEventDataExtractorRegistry();
                    extractorRegistry.populateContext(event, context);
                    
                    scriptExecutor.execute(script, context, "onChat");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String worldId = event.getPlayer().getWorld().getName();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onLeave".equals(script.getRootBlock().getAction())) {
                    CreativeWorld creativeWorld = worldManager.getWorldByName(worldId);
                    if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
                        continue; // Пропускаем выполнение если код выключен
                    }
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    
                    // Use unified event data extraction system
                    EventDataExtractorRegistry extractorRegistry = plugin.getServiceRegistry().getEventDataExtractorRegistry();
                    extractorRegistry.populateContext(event, context);
                    
                    scriptExecutor.execute(script, context, "onLeave");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        String worldId = event.getPlayer().getWorld().getName();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onInteract".equals(script.getRootBlock().getAction())) {
                    CreativeWorld creativeWorld = worldManager.getWorldByName(worldId);
                    if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
                        continue; // Пропускаем выполнение если код выключен
                    }
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    
                    // Use unified event data extraction system
                    EventDataExtractorRegistry extractorRegistry = plugin.getServiceRegistry().getEventDataExtractorRegistry();
                    extractorRegistry.populateContext(event, context);
                    
                    scriptExecutor.execute(script, context, "onInteract");
                }
            }
        }
    }

}
