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
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class PlayerDeathListener implements Listener {
    
    private final MegaCreative plugin;
    
    public PlayerDeathListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location deathLocation = player.getLocation();
        
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
                // Проверяем, есть ли триггер onPlayerDeath
                if (hasPlayerDeathTrigger(script)) {
                    // Создаем контекст выполнения
                    ExecutionContext context = ExecutionContext.builder()
                        .player(player)
                        .plugin(plugin)
                        .creativeWorld(creativeWorld)
                        .blockLocation(deathLocation)
                        .build();
                    
                    // Выполняем скрипт
                    ScriptExecutor executor = new ScriptExecutor(plugin);
                    executor.execute(script, context, "onPlayerDeath");
                }
            }
        }
    }
    
    private boolean hasPlayerDeathTrigger(CodeScript script) {
        // Проверяем, есть ли в скрипте триггер onPlayerDeath
        return script.getRootBlock() != null && 
               script.getRootBlock().getAction().equals("onPlayerDeath");
    }
} 