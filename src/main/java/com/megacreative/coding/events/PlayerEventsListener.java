package com.megacreative.coding.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.executors.ExecutionResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.concurrent.CompletableFuture;

// Этот класс будет слушать реальные события Bukkit
public class PlayerEventsListener implements Listener {
    private final MegaCreative plugin;
    private final ScriptEngine scriptEngine;

    public PlayerEventsListener(MegaCreative plugin) {
        this.plugin = plugin;
        // Получаем ScriptEngine из ServiceRegistry
        this.scriptEngine = plugin.getServiceRegistry().getService(ScriptEngine.class);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Здесь мы будем искать все активные скрипты, начинающиеся с "onPlayerMove",
        // и запускать их выполнение через scriptEngine.execute(...)
        // For now, we'll just log that the event was triggered
        plugin.getLogger().info("Player moved: " + event.getPlayer().getName());
    }
}