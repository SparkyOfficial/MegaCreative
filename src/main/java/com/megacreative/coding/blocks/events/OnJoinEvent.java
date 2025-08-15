package com.megacreative.coding.blocks.events;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingManager;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Блок-событие для обработки входа игрока в мир.
 */
public class OnJoinEvent extends EventBlock {
    
    @Override
    public String getEventName() {
        return "PlayerJoinEvent";
    }
    
    @Override
    public String getDisplayName() {
        return "onJoin";
    }
    
    @Override
    public void onEvent(Event event, CreativeWorld world) {
        if (!(event instanceof PlayerJoinEvent)) return;
        
        PlayerJoinEvent joinEvent = (PlayerJoinEvent) event;
        Player player = joinEvent.getPlayer();
        
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
            .build();
        
        // Запускаем скрипты onJoin для этого мира
        CodingManager codingManager = MegaCreative.getInstance().getCodingManager();
        // Пока используем существующую логику - в будущем это будет заменено на новую систему
        // TODO: Реализовать новую систему выполнения событий
        // codingManager.getScriptExecutor().executeScriptsForEvent("onJoin", context);
    }
    
    @Override
    public boolean canHandle(Event event) {
        return event instanceof PlayerJoinEvent;
    }
} 