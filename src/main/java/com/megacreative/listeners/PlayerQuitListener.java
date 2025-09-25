package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.managers.PlayerModeManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player quit events
 *
 * Слушатель для событий выхода игроков
 *
 * Listener für Spieler-Verlassen-Ereignisse
 */
public class PlayerQuitListener implements Listener {
    
    private final MegaCreative plugin;
    
    /**
     * Constructor for PlayerQuitListener
     * @param plugin the main plugin
     *
     * Конструктор для PlayerQuitListener
     * @param plugin основной плагин
     *
     * Konstruktor für PlayerQuitListener
     * @param plugin das Haupt-Plugin
     */
    public PlayerQuitListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player quit events
     * @param event the player quit event
     *
     * Обрабатывает события выхода игроков
     * @param event событие выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     * @param event das Spieler-Verlassen-Ereignis
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clear player mode when they leave
        if (plugin.getServiceRegistry() != null) {
            PlayerModeManager modeManager = plugin.getServiceRegistry().getPlayerModeManager();
            modeManager.clearMode(player);
        }
        
        // Убираем игрока из менеджера скорбордов
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
        
        // Сохраняем данные игрока
        plugin.getVariableManager().savePersistentData();
        
        // Удаление игрока из онлайна всех миров
        plugin.getWorldManager().getAllPublicWorlds().forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Также проверяем приватные миры игрока
        plugin.getWorldManager().getPlayerWorlds(event.getPlayer()).forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Если игрок выходит из dev мира, нужно восстановить его инвентарь!
        // DevInventoryManager должен знать, что игрок был в dev мире
        com.megacreative.managers.DevInventoryManager devInventoryManager = plugin.getServiceRegistry().getDevInventoryManager();
        if (devInventoryManager.isPlayerInDevWorld(player)) {
            devInventoryManager.restorePlayerInventory(player);
        }
        
        // Очищаем данные из обработчика блоков
        // plugin.getBlockPlacementHandler().cleanUpPlayerData(event.getPlayer().getUniqueId());
    }
}