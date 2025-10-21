package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player drops an item
 * This event is fired when a player drops an item, after being processed by our Bukkit listener
 */
public class MegaPlayerDropItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final ItemStack item;
    
    public MegaPlayerDropItemEvent(Player player, ItemStack item) {
        this.player = player;
        this.item = item;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    public ItemStack getItem() {
        return item;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}