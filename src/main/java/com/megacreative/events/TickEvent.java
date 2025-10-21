package com.megacreative.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event that is fired on every server tick
 * Used for triggering time-based script executions
 *
 * Событие, которое срабатывает на каждом тике сервера
 * Используется для запуска выполнения скриптов по времени
 *
 * Ereignis, das bei jedem Server-Tick ausgelöst wird
 * Wird für zeitbasierte Skriptausführungen verwendet
 */
public class TickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final long tick;
    private final long timestamp;
    private final double tps;
    
    /**
     * Constructor for TickEvent
     * @param tick the current tick count
     * @param tps the current server TPS
     *
     * Конструктор для TickEvent
     * @param tick текущий счетчик тиков
     * @param tps текущий TPS сервера
     *
     * Konstruktor für TickEvent
     * @param tick der aktuelle Tick-Zähler
     * @param tps der aktuelle Server-TPS
     */
    public TickEvent(long tick, double tps) {
        this.tick = tick;
        this.timestamp = System.currentTimeMillis();
        this.tps = tps;
    }
    
    /**
     * Gets the current tick count
     * @return the tick count
     *
     * Получает текущий счетчик тиков
     * @return счетчик тиков
     *
     * Gibt den aktuellen Tick-Zähler zurück
     * @return der Tick-Zähler
     */
    public long getTick() {
        return tick;
    }
    
    /**
     * Gets the timestamp when this event was created
     * @return the timestamp
     *
     * Получает временную метку создания события
     * @return временная метка
     *
     * Gibt den Zeitstempel zurück, wann dieses Ereignis erstellt wurde
     * @return der Zeitstempel
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the current server TPS
     * @return the TPS
     *
     * Получает текущий TPS сервера
     * @return TPS
     *
     * Gibt den aktuellen Server-TPS zurück
     * @return der TPS
     */
    public double getTPS() {
        return tps;
    }
    
    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }
    
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}