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
        // Выдача стартовых предметов через тик, чтобы инвентарь успел загрузиться
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPlayerManager().giveStarterItems(event.getPlayer());
        }, 1L);
    }
}
