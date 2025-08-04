package com.megacreative.coding.core;

import com.megacreative.coding.blocks.Block;
import com.megacreative.coding.blocks.events.EventBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Система обработки событий для блоков-событий.
 * Регистрирует слушатели событий и запускает соответствующие скрипты.
 */
public class EventSystem implements Listener {
    private final JavaPlugin plugin;
    private final ScriptEngine scriptEngine;
    private final VariableManager variableManager;
    private final Map<Class<? extends Event>, List<EventBlock>> eventHandlers = new ConcurrentHashMap<>();
    private final Map<String, EventBlock> registeredEvents = new ConcurrentHashMap<>();
    
    public EventSystem(JavaPlugin plugin, ScriptEngine scriptEngine, VariableManager variableManager) {
        this.plugin = plugin;
        this.scriptEngine = scriptEngine;
        this.variableManager = variableManager;
        
        // Регистрируем этот класс как слушатель событий
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Регистрирует блок-событие для обработки указанного типа события.
     */
    public void registerEvent(EventBlock eventBlock, Class<? extends Event> eventClass) {
        String eventId = generateEventId(eventBlock);
        
        if (registeredEvents.containsKey(eventId)) {
            throw new IllegalArgumentException("Событие уже зарегистрировано: " + eventId);
        }
        
        eventHandlers.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(eventBlock);
        registeredEvents.put(eventId, eventBlock);
        
        // Если это кастомное событие, регистрируем его
        if (eventBlock instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) eventBlock, plugin);
        }
    }
    
    /**
     * Отменяет регистрацию блока-события.
     */
    public void unregisterEvent(EventBlock eventBlock) {
        String eventId = generateEventId(eventBlock);
        EventBlock removed = registeredEvents.remove(eventId);
        
        if (removed != null) {
            // Удаляем из списка обработчиков
            for (List<EventBlock> handlers : eventHandlers.values()) {
                handlers.removeIf(block -> block.equals(eventBlock));
            }
            
            // Удаляем пустые записи
            eventHandlers.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
    }
    
    /**
     * Обработчик всех зарегистрированных событий.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEvent(Event event) {
        List<EventBlock> handlers = eventHandlers.get(event.getClass());
        if (handlers == null || handlers.isEmpty()) {
            return;
        }
        
        for (EventBlock eventBlock : new ArrayList<>(handlers)) {
            try {
                // Создаем контекст выполнения
                Player player = eventBlock.extractPlayer(event);
                BlockContext context = BlockContext.builder(plugin)
                        .scriptId(UUID.randomUUID().toString())
                        .player(player)
                        .event(event)
                        .build();
                
                // Запускаем скрипт
                scriptEngine.executeScript("event_" + eventBlock.getId(), eventBlock, context);
            } catch (Exception e) {
                plugin.getLogger().severe("Ошибка при обработке события " + event.getEventName() + 
                                       " в блоке " + eventBlock.getId() + 
                                       ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Генерирует уникальный идентификатор для события.
     */
    private String generateEventId(EventBlock eventBlock) {
        return eventBlock.getId() + "_" + eventBlock.getEventClass().getSimpleName();
    }
    
    /**
     * Очищает все зарегистрированные события.
     */
    public void clearAllEvents() {
        registeredEvents.clear();
        eventHandlers.clear();
    }
}
