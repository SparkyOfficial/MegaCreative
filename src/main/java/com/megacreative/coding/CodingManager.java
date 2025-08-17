package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Управляет всеми аспектами системы кодирования: загрузкой, сохранением и выполнением скриптов.
 */
public class CodingManager implements Listener {

    private final MegaCreative plugin;
    private final HybridScriptExecutor scriptExecutor;
    private final Map<String, List<CodeScript>> worldScripts = new HashMap<>();
    private final Map<String, Object> globalVariables = new HashMap<>();
    private final Map<String, Object> serverVariables = new HashMap<>();
    
    /**
     * Получает исполнитель скриптов.
     * @return HybridScriptExecutor для выполнения скриптов
     */
    public HybridScriptExecutor getScriptExecutor() {
        return scriptExecutor;
    }

    public CodingManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.scriptExecutor = new HybridScriptExecutor(plugin);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Загружает скрипты для мира и делает их активными.
     * @param world Мир, скрипты которого нужно загрузить.
     */
    public void loadScriptsForWorld(CreativeWorld world) {
        worldScripts.put(world.getId(), world.getScripts());
        plugin.getLogger().info("Загружено " + world.getScripts().size() + " скриптов для мира " + world.getName());
    }

    /**
     * Выгружает скрипты мира, делая их неактивными.
     * @param world Мир, скрипты которого нужно выгрузить.
     */
    public void unloadScriptsForWorld(CreativeWorld world) {
        if (worldScripts.containsKey(world.getId())) {
            int count = worldScripts.get(world.getId()).size();
            worldScripts.remove(world.getId());
            plugin.getLogger().info("Выгружено " + count + " скриптов для мира " + world.getName());
        }
    }

    // --- Обработчики событий для выполнения скриптов ---

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return;
        }

        String worldId = creativeWorld.getId();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onJoin".equals(script.getRootBlock().getAction())) {
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    scriptExecutor.execute(script, context, "onJoin");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        // Переключаемся на основной поток для безопасности
        Bukkit.getScheduler().runTask(plugin, () -> {
            CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
            if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
                return;
            }

            String worldId = creativeWorld.getId();
            if (worldScripts.containsKey(worldId)) {
                List<CodeScript> scripts = worldScripts.get(worldId);
                for (CodeScript script : scripts) {
                    if (script.isEnabled() && script.getRootBlock() != null &&
                        script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                        "onChat".equals(script.getRootBlock().getAction())) {
                        ExecutionContext context = ExecutionContext.builder()
                                .plugin(plugin)
                                .player(event.getPlayer())
                                .creativeWorld(creativeWorld)
                                .event(event)
                                .build();
                        scriptExecutor.execute(script, context, "onChat");
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return;
        }

        String worldId = creativeWorld.getId();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onLeave".equals(script.getRootBlock().getAction())) {
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    scriptExecutor.execute(script, context, "onLeave");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(event.getPlayer().getWorld());
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return;
        }

        String worldId = creativeWorld.getId();
        if (worldScripts.containsKey(worldId)) {
            List<CodeScript> scripts = worldScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock() != null &&
                    script.getRootBlock().getMaterial() == org.bukkit.Material.DIAMOND_BLOCK &&
                    "onInteract".equals(script.getRootBlock().getAction())) {
                    ExecutionContext context = ExecutionContext.builder()
                            .plugin(plugin)
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();
                    scriptExecutor.execute(script, context, "onInteract");
                }
            }
        }
    }

    /**
     * Получает глобальную переменную
     * @param name Имя переменной
     * @return Значение переменной или null
     */
    public Object getGlobalVariable(String name) {
        return globalVariables.get(name);
    }
    
    /**
     * Получает серверную переменную
     * @param name Имя переменной
     * @return Значение переменной или null
     */
    public Object getServerVariable(String name) {
        return serverVariables.get(name);
    }
}
