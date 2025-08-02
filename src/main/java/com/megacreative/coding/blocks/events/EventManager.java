package com.megacreative.coding.blocks.events;

import com.megacreative.models.CreativeWorld;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Менеджер для управления блоками-событиями.
 * Регистрирует события и обрабатывает их при наступлении.
 */
public class EventManager {
    
    private final Map<String, List<EventBlock>> eventBlocks = new HashMap<>();
    
    /**
     * Регистрирует блок-событие для определенного типа события.
     * @param eventName Имя события Bukkit
     * @param eventBlock Блок-событие для регистрации
     */
    public void registerEventBlock(String eventName, EventBlock eventBlock) {
        eventBlocks.computeIfAbsent(eventName, k -> new java.util.ArrayList<>()).add(eventBlock);
    }
    
    /**
     * Обрабатывает событие, вызывая все зарегистрированные блоки-события.
     * @param event Событие Bukkit
     * @param world Мир, в котором произошло событие
     */
    public void handleEvent(Event event, CreativeWorld world) {
        String eventName = event.getClass().getSimpleName();
        List<EventBlock> blocks = eventBlocks.get(eventName);
        
        if (blocks != null) {
            for (EventBlock block : blocks) {
                if (block.canHandle(event)) {
                    try {
                        block.onEvent(event, world);
                    } catch (Exception e) {
                        // Логируем ошибку, но не прерываем выполнение других блоков
                        System.err.println("Ошибка при выполнении блока-события " + block.getDisplayName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    /**
     * Возвращает все зарегистрированные блоки-события для указанного типа события.
     * @param eventName Имя события
     * @return Список блоков-событий
     */
    public List<EventBlock> getEventBlocks(String eventName) {
        return eventBlocks.getOrDefault(eventName, new java.util.ArrayList<>());
    }
    
    /**
     * Очищает все зарегистрированные блоки-события.
     */
    public void clearEventBlocks() {
        eventBlocks.clear();
    }
} 