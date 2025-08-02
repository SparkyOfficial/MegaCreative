package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerWorldChangeListener implements Listener {
    private final MegaCreative plugin;

    public PlayerWorldChangeListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        // Когда игрок меняет мир, просто заново устанавливаем ему скорборд.
        // Старая задача обновления отменится автоматически.
        plugin.getScoreboardManager().setScoreboard(event.getPlayer());
    } 
} 