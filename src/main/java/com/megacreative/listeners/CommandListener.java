package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ScriptExecutor;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class CommandListener implements Listener {
    
    private final MegaCreative plugin;
    
    public CommandListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1); // Убираем "/" в начале
        Location commandLocation = player.getLocation();
        
        // Получаем все скрипты для этого мира
        String worldName = player.getWorld().getName();
        CreativeWorld creativeWorld = plugin.getWorldManager().getWorldByName(worldName);
        
        if (creativeWorld != null) {
            // Проверяем, включен ли код в этом мире
            if (!creativeWorld.getMode().isCodeEnabled()) {
                return; // Пропускаем выполнение если код выключен
            }
            
            List<CodeScript> scripts = creativeWorld.getScripts();
            
            for (CodeScript script : scripts) {
                // Проверяем, есть ли триггер /command
                if (hasCommandTrigger(script)) {
                    // Создаем контекст выполнения
                    ExecutionContext context = ExecutionContext.builder()
                        .player(player)
                        .plugin(plugin)
                        .creativeWorld(creativeWorld)
                        .blockLocation(commandLocation)
                        .build();
                    
                    // Добавляем команду в переменные контекста
                    context.setVariable("command", command);
                    
                    // Выполняем скрипт
                    ScriptExecutor executor = new ScriptExecutor(plugin);
                    executor.execute(script, context, "/command");
                }
            }
        }
    }
    
    private boolean hasCommandTrigger(CodeScript script) {
        // Проверяем, есть ли в скрипте триггер /command
        return script.getRootBlock() != null && 
               script.getRootBlock().getAction().equals("/command");
    }
} 