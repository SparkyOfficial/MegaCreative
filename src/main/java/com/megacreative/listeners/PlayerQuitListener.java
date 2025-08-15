package com.megacreative.listeners;

import com.megacreative.MegaCreative;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    
    private final MegaCreative plugin;
    
    public PlayerQuitListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Убираем игрока из менеджера скорбордов
        plugin.getScoreboardManager().removeScoreboard(event.getPlayer());
        
        // Сохраняем данные игрока
        plugin.getDataManager().savePlayerData(event.getPlayer());
        
        // Удаление игрока из онлайна всех миров
        plugin.getWorldManager().getAllPublicWorlds().forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Также проверяем приватные миры игрока
        plugin.getWorldManager().getPlayerWorlds(event.getPlayer()).forEach(world -> 
            world.removeOnlinePlayer(event.getPlayer().getUniqueId())
        );
        
        // Очищаем данные из обработчика блоков
        plugin.getBlockPlacementHandler().cleanUpPlayerData(event.getPlayer().getUniqueId());
    }
}
