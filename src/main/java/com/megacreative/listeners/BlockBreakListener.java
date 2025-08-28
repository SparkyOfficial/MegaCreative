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
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {
    
    private final MegaCreative plugin;
    
    public BlockBreakListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null || !creativeWorld.getMode().isCodeEnabled()) {
            return; // Пропускаем выполнение если код выключен
        }
        
        // Проверяем, есть ли скрипты с событием onBlockBreak
        creativeWorld.getScripts().stream()
            .filter(script -> script.isEnabled() && script.getRootBlock() != null)
            .filter(script -> script.getRootBlock().getMaterial() == Material.DIAMOND_BLOCK)
            .filter(script -> "onBlockBreak".equals(script.getRootBlock().getAction()))
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
                
                // Выполняем скрипт
                plugin.getCodingManager().getScriptExecutor().execute(script, context, "onBlockBreak");
            });
    }
} 