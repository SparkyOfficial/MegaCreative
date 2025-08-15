package com.megacreative.coding.blocks.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Блок-событие для обработки разрушения блока.
 */
public class OnBlockBreakEvent extends EventBlock {
    
    @Override
    public String getEventName() {
        return "BlockBreakEvent";
    }
    
    @Override
    public String getDisplayName() {
        return "onBlockBreak";
    }
    
    @Override
    public void onEvent(Event event, CreativeWorld world) {
        if (!(event instanceof BlockBreakEvent)) return;
        
        BlockBreakEvent breakEvent = (BlockBreakEvent) event;
        Player player = breakEvent.getPlayer();
        Location blockLocation = breakEvent.getBlock().getLocation();
        
        // Проверяем, что игрок находится в нужном мире
        if (!player.getWorld().getName().equals(world.getWorldName())) {
            return;
        }
        
        // Создаем контекст выполнения
        ExecutionContext context = ExecutionContext.builder()
            .player(player)
            .creativeWorld(world)
            .plugin(MegaCreative.getInstance())
            .event(event)
            .blockLocation(blockLocation)
            .build();
        
        // Запускаем скрипты onBlockBreak для этого мира
        CodingManager codingManager = MegaCreative.getInstance().getCodingManager();
        // TODO: Реализовать новую систему выполнения событий
        // codingManager.getScriptExecutor().executeScriptsForEvent("onBlockBreak", context);
    }
    
    @Override
    public boolean canHandle(Event event) {
        return event instanceof BlockBreakEvent;
    }
} 