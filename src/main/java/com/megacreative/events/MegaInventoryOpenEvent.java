package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

/**
 * Custom event for when a player opens an inventory
 * This event is fired when a player opens an inventory, after being processed by our Bukkit listener
 */
public class MegaInventoryOpenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Inventory inventory;
    
    public MegaInventoryOpenEvent(@NotNull Player player, @NotNull Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }
    
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    @NotNull
    public Inventory getInventory() {
        return inventory;
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