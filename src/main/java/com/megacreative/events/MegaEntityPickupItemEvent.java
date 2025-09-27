package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Custom event for when an entity picks up an item
 * This event is fired when an entity picks up an item, after being processed by our Bukkit listener
 */
public class MegaEntityPickupItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final ItemStack item;
    private final int quantity;
    
    public MegaEntityPickupItemEvent(Player player, ItemStack item, int quantity) {
        this.player = player;
        this.item = item;
        this.quantity = quantity;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}