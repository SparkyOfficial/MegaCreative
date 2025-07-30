package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Управляет всеми аспектами системы кодирования: загрузкой, сохранением и выполнением скриптов.
 */
public class CodingManager implements Listener {

    private final MegaCreative plugin;
    private final Map<UUID, List<CodeScript>> activeScripts; // World UUID -> Scripts
    private final ScriptExecutor scriptExecutor;

    public CodingManager(MegaCreative plugin) {
        this.plugin = plugin;
        this.activeScripts = new HashMap<>();
        this.scriptExecutor = new ScriptExecutor();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Загружает скрипты для мира и делает их активными.
     * @param world Мир, скрипты которого нужно загрузить.
     */
    public void loadScriptsForWorld(CreativeWorld world) {
        UUID worldUniqueId = Bukkit.getWorld(world.getWorldName()).getUID();
        activeScripts.put(worldUniqueId, world.getScripts());
        plugin.getLogger().info("Загружено " + world.getScripts().size() + " скриптов для мира " + world.getName());
    }

    /**
     * Выгружает скрипты мира, делая их неактивными.
     * @param world Мир, скрипты которого нужно выгрузить.
     */
    public void unloadScriptsForWorld(CreativeWorld world) {
        UUID worldUniqueId = Bukkit.getWorld(world.getWorldName()).getUID();
        if (activeScripts.containsKey(worldUniqueId)) {
            int count = activeScripts.get(worldUniqueId).size();
            activeScripts.remove(worldUniqueId);
            plugin.getLogger().info("Выгружено " + count + " скриптов для мира " + world.getName());
        }
    }

    // --- Обработчики событий для выполнения скриптов ---

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID worldId = event.getPlayer().getWorld().getUID();

        if (activeScripts.containsKey(worldId)) {
            List<CodeScript> scripts = activeScripts.get(worldId);
            for (CodeScript script : scripts) {
                if (script.isEnabled() && script.getRootBlock().getType() == BlockType.EVENT_PLAYER_JOIN) {
                    CreativeWorld creativeWorld = plugin.getWorldManager().getWorld(worldId.toString());
                    if (creativeWorld == null) continue;

                    ExecutionContext context = ExecutionContext.builder()
                            .player(event.getPlayer())
                            .creativeWorld(creativeWorld)
                            .event(event)
                            .build();

                    if (script.isEnabled()) {
                        scriptExecutor.execute(script, context);
                    }
                }
            }
        }
    }
}
