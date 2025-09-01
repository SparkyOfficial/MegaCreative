package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {
    
    private final MegaCreative plugin;
    
    public PlayerDeathListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return; // Пропускаем выполнение если код выключен
        }
        
        // Проверяем, есть ли скрипты с событием onPlayerDeath
        creativeWorld.getScripts().stream()
            .filter(script -> script.isEnabled() && script.getRootBlock() != null)
            .filter(script -> script.getRootBlock().getMaterial() == Material.DIAMOND_BLOCK)
            .filter(script -> "onPlayerDeath".equals(script.getRootBlock().getAction()))
            .forEach(script -> {
                // Создаем контекст выполнения
                ExecutionContext context = ExecutionContext.builder()
                    .plugin(plugin)
                    .player(player)
                    .creativeWorld(creativeWorld)
                    .event(event)
                    .build();
                
                // Используем унифицированную систему извлечения данных
                EventDataExtractorRegistry extractorRegistry = plugin.getServiceRegistry().getEventDataExtractorRegistry();
                extractorRegistry.populateContext(event, context);
                
                // Выполняем скрипт через ScriptEngine
                plugin.getCodingManager().getScriptEngine().executeScript(script, context.getPlayer(), "onPlayerDeath")
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().severe("Error executing death script: " + throwable.getMessage());
                        } else if (result != null && !result.isSuccess()) {
                            plugin.getLogger().warning("Death script execution failed: " + result.getErrorMessage());
                        }
                    });
            });
    }
} 