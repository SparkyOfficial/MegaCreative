package com.megacreative.coding.blocks.events;

import com.megacreative.models.CreativeWorld;
import org.bukkit.event.Event;

/**
 * Базовый класс для блоков-событий.
 * События - это специальные блоки, которые выполняются при наступлении определенных событий Bukkit.
 */
public abstract class EventBlock {
    
    /**
     * Возвращает имя события Bukkit, на которое подписан этот блок.
     * @return Имя события (например, "PlayerJoinEvent")
     */
    public abstract String getEventName();
    
    /**
     * Возвращает человекочитаемое имя события.
     * @return Человекочитаемое имя (например, "onJoin")
     */
    public abstract String getDisplayName();
    
    /**
     * Вызывается при наступлении события.
     * @param event Событие Bukkit
     * @param world Мир, в котором произошло событие
     */
    public abstract void onEvent(Event event, CreativeWorld world);
    
    /**
     * Проверяет, может ли этот блок обработать данное событие.
     * @param event Событие для проверки
     * @return true, если блок может обработать событие
     */
    public abstract boolean canHandle(Event event);
} 