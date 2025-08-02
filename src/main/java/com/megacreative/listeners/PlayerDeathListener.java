package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.ExecutionContext;
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
                
                // Добавляем переменные события
                Location deathLocation = player.getLocation();
                context.setVariable("deathLocation", 
                    deathLocation.getWorld().getName() + "," + 
                    deathLocation.getBlockX() + "," + 
                    deathLocation.getBlockY() + "," + 
                    deathLocation.getBlockZ());
                context.setVariable("deathX", deathLocation.getBlockX());
                context.setVariable("deathY", deathLocation.getBlockY());
                context.setVariable("deathZ", deathLocation.getBlockZ());
                context.setVariable("deathMessage", event.getDeathMessage());
                context.setVariable("deathCause", event.getEntity().getLastDamageCause() != null ? 
                    event.getEntity().getLastDamageCause().getCause().name() : "UNKNOWN");
                
                // Выполняем скрипт
                plugin.getCodingManager().getScriptExecutor().execute(script, context, "onPlayerDeath");
            });
    }
} 