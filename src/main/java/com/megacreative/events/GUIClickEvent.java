package com.megacreative.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Event fired when a player clicks on an item in a custom GUI
 */
public class GUIClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ItemStack item;
    private final String clickType;
    private final String menuTitle;
    
    public GUIClickEvent(Player player, Inventory inventory, int slot, ItemStack item, String clickType, String menuTitle) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.item = item;
        this.clickType = clickType;
        this.menuTitle = menuTitle;
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
    
    public ItemStack getItem() {
        return item;
    }
    
    public String getClickType() {
        return clickType;
    }
    
    public String getMenuTitle() {
        return menuTitle;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}