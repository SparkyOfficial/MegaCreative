package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.events.TickEvent;
import org.bukkit.Bukkit;

/**
 * Manages the tick system for the plugin
 * Fires TickEvents that other systems can listen to
 *
 * Управляет системой тиков для плагина
 * Запускает TickEvents, на которые могут подписаться другие системы
 *
 * Verwaltet das Tick-System für das Plugin
 * Feuert TickEvents, auf die andere Systeme hören können
 */
public class TickManager {
    private final MegaCreative plugin;
    private long tickCounter = 0;
    private long lastTPSCheck = System.currentTimeMillis();
    private int tpsCheckTicks = 0;
    private double currentTPS = 20.0;
    
    /**
     * Constructor for TickManager
     * @param plugin the main plugin instance
     *
     * Конструктор для TickManager
     * @param plugin основной экземпляр плагина
     *
     * Konstruktor für TickManager
     * @param plugin die Haupt-Plugin-Instanz
     */
    public TickManager(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Called on every server tick to update the tick counter and fire events
     *
     * Вызывается на каждом тике сервера для обновления счетчика тиков и запуска событий
     *
     * Wird bei jedem Server-Tick aufgerufen, um den Tick-Zähler zu aktualisieren und Ereignisse auszulösen
     */
    public void tick() {
        tickCounter++;
        tpsCheckTicks++;
        
        // Update TPS every 20 ticks (1 second)
        if (tpsCheckTicks >= 20) {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastTPSCheck;
            if (timeDiff > 0) {
                currentTPS = (20.0 * 1000.0) / timeDiff;
                // Ensure TPS doesn't exceed 20
                if (currentTPS > 20.0) {
                    currentTPS = 20.0;
                }
            }
            lastTPSCheck = currentTime;
            tpsCheckTicks = 0;
        }
        
        // Fire the tick event
        TickEvent tickEvent = new TickEvent(tickCounter, currentTPS);
        Bukkit.getPluginManager().callEvent(tickEvent);
    }
    
    /**
     * Gets the current tick counter
     * @return the tick counter
     *
     * Получает текущий счетчик тиков
     * @return счетчик тиков
     *
     * Gibt den aktuellen Tick-Zähler zurück
     * @return der Tick-Zähler
     */
    public long getTickCounter() {
        return tickCounter;
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
    public double getCurrentTPS() {
        return currentTPS;
    }
    
    /**
     * Resets the tick counter
     *
     * Сбрасывает счетчик тиков
     *
     * Setzt den Tick-Zähler zurück
     */
    public void resetTickCounter() {
        tickCounter = 0;
    }
}