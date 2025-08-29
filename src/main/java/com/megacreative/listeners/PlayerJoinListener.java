package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    
    private final MegaCreative plugin;
    
    public PlayerJoinListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Загружаем данные игрока
        plugin.getVariableManager().loadPlayerData(event.getPlayer());
        
        // Устанавливаем скорборд и таб-лист
        plugin.getScoreboardManager().setScoreboard(event.getPlayer());
        com.megacreative.utils.TabListManager.setTabList(event.getPlayer());
        
        // Выдача стартовых предметов через тик, чтобы инвентарь успел загрузиться
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().giveStarterItems(event.getPlayer());
        }, 1L);
    }
}
