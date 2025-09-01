package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.events.EventDataExtractorRegistry;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
    
    private final MegaCreative plugin;
    
    public CommandListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return; // Пропускаем выполнение если код выключен
        }
        
        String command = event.getMessage().substring(1); // Убираем "/" в начале
        
        // Проверяем, есть ли скрипты с событием onCommand
        creativeWorld.getScripts().stream()
            .filter(script -> script.isEnabled() && script.getRootBlock() != null)
            .filter(script -> script.getRootBlock().getMaterial() == Material.DIAMOND_BLOCK)
            .filter(script -> "onCommand".equals(script.getRootBlock().getAction()))
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
                plugin.getCodingManager().getScriptEngine().executeScript(script, context.getPlayer(), "onCommand")
                    .whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            plugin.getLogger().severe("Error executing command script: " + throwable.getMessage());
                        } else if (result != null && !result.isSuccess()) {
                            plugin.getLogger().warning("Command script execution failed: " + result.getErrorMessage());
                        }
                    });
            });
    }
} 