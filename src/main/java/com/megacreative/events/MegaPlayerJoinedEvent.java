package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player joins the server
 * This event is fired when a player joins, after being processed by our Bukkit listener
 * 
 * Пользовательское событие для присоединения игрока к серверу
 * Это событие срабатывает при присоединении игрока, после обработки нашим слушателем Bukkit
 * 
 * Benutzerdefiniertes Ereignis für den Beitritt eines Spielers zum Server
 * Dieses Ereignis wird ausgelöst, wenn ein Spieler beitritt, nachdem es von unserem Bukkit-Listener verarbeitet wurde
 */
public class MegaPlayerJoinedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final boolean isFirstJoin;
    
    public MegaPlayerJoinedEvent(@NotNull Player player, boolean isFirstJoin) {
        this.player = player;
        this.isFirstJoin = isFirstJoin;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    public boolean isFirstJoin() {
        return isFirstJoin;
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