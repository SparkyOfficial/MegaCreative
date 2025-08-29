package com.megacreative.coding;

import com.megacreative.MegaCreative;
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

/**
 * Управляет всеми аспектами системы кодирования: загрузкой, сохранением и выполнением скриптов.
 */
public class CodingManagerImpl implements ICodingManager, Listener {

    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    private final ScriptExecutor scriptExecutor;
    private final Map<String, List<CodeScript>> worldScripts = new HashMap<>();
    private final Map<String, Object> globalVariables = new HashMap<>();
    private final Map<String, Object> serverVariables = new HashMap<>();
    
    /**
     * Получает исполнитель скриптов.
     * @return ScriptExecutor для выполнения скриптов
     */
    public ScriptExecutor getScriptExecutor() {
        return scriptExecutor;
    }

    public CodingManagerImpl(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
        this.scriptExecutor = new ScriptExecutor(plugin);
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
    public void executeScript(CodeScript script, Player player, String trigger) {
        ExecutionContext context = ExecutionContext.builder()
                .plugin(plugin)
                .player(player)
                .creativeWorld(worldManager.findCreativeWorldByBukkit(player.getWorld()))
                .build();
        scriptExecutor.execute(script, context, trigger);
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
    public Object getGlobalVariable(String name) {
        return globalVariables.get(name);
    }
    
    @Override
    public void setGlobalVariable(String name, Object value) {
        globalVariables.put(name, value);
    }
    
    @Override
    public Object getServerVariable(String name) {
        return serverVariables.get(name);
    }
    
    @Override
    public void setServerVariable(String name, Object value) {
        serverVariables.put(name, value);
    }
    
    @Override
    public Map<String, Object> getGlobalVariables() {
        return new HashMap<>(globalVariables);
    }
    
    @Override
    public Map<String, Object> getServerVariables() {
        return new HashMap<>(serverVariables);
    }
    
    @Override
    public void clearVariables() {
        globalVariables.clear();
        serverVariables.clear();
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
