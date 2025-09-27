package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Custom event for when an entity takes damage
 * This event is fired when an entity takes damage, after being processed by our Bukkit listener
 */
public class MegaEntityDamageEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final double damage;
    private final EntityDamageEvent.DamageCause cause;
    
    public MegaEntityDamageEvent(Player player, double damage, EntityDamageEvent.DamageCause cause) {
        this.player = player;
        this.damage = damage;
        this.cause = cause;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public double getDamage() {
        return damage;
    }
    
    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}