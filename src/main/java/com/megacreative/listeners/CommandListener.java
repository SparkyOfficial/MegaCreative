package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ExecutionContext;
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
                
                // Добавляем переменные события
                context.setVariable("command", command);
                context.setVariable("commandName", command.split(" ")[0]);
                context.setVariable("commandArgs", command.substring(command.indexOf(" ") + 1));
                context.setVariable("fullCommand", command);
                
                // Выполняем скрипт
                plugin.getCodingManager().getScriptExecutor().execute(script, context, "onCommand");
            });
    }
} 