package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Listener for player join events
 *
 * Слушатель для событий входа игроков
 *
 * Listener für Spieler-Beitritts-Ereignisse
 */
public class PlayerJoinListener implements Listener {
    
    private final MegaCreative plugin;
    
    /**
     * Constructor for PlayerJoinListener
     * @param plugin the main plugin
     *
     * Конструктор для PlayerJoinListener
     * @param plugin основной плагин
     *
     * Konstruktor für PlayerJoinListener
     * @param plugin das Haupt-Plugin
     */
    public PlayerJoinListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player join events
     * @param event the player join event
     *
     * Обрабатывает события входа игроков
     * @param event событие входа игрока
     *
     * Verarbeitet Spieler-Beitritts-Ereignisse
     * @param event das Spieler-Beitritts-Ereignis
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Загружаем данные игрока
        plugin.getVariableManager().loadPersistentData();
        
        // Устанавливаем скорборд и таб-лист
        plugin.getScoreboardManager().setScoreboard(event.getPlayer());
        com.megacreative.utils.TabListManager.setTabList(event.getPlayer());
        
        // Выдача стартовых предметов через тик, чтобы инвентарь успел загрузиться
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().giveStarterItems(event.getPlayer());
        }, 1L);
    }
}