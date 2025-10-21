package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when an entity picks up an item
 * This event is fired when an entity picks up an item, after being processed by our Bukkit listener
 */
public class MegaEntityPickupItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final ItemStack item;
    private final int quantity;
    
    public MegaEntityPickupItemEvent(@NotNull Player player, @NotNull ItemStack item, int quantity) {
        this.player = player;
        this.item = item;
        this.quantity = quantity;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public ItemStack getItem() {
        return item;
    }
    
    public int getQuantity() {
        return quantity;
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