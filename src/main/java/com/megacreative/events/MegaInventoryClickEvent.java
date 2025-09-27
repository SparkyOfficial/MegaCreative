package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;

/**
 * Custom event for when a player clicks in an inventory
 * This event is fired when a player clicks in an inventory, after being processed by our Bukkit listener
 */
public class MegaInventoryClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final int rawSlot;
    private final ClickType clickType;
    
    public MegaInventoryClickEvent(Player player, Inventory inventory, int slot, int rawSlot, ClickType clickType) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.rawSlot = rawSlot;
        this.clickType = clickType;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public int getSlot() {
        return slot;
    }
    
    public int getRawSlot() {
        return rawSlot;
    }
    
    public ClickType getClickType() {
        return clickType;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}