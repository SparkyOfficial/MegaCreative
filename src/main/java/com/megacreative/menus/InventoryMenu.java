package com.megacreative.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for inventory menus in MegaCreative
 * Implements menu system similar to OpenCreative
 */
public interface InventoryMenu extends InventoryHolder {
    
    /**
     * Opens the inventory for player
     * @param player player to open menu
     */
    void open(@NotNull Player player);
    
    /**
     * Executes when player clicks in inventory
     * @param event event of click in inventory
     */
    void onClick(@NotNull InventoryClickEvent event);
    
    /**
     * Executes when player opens inventory and sees it first time
     * @param event event of inventory open
     */
    void onOpen(@NotNull InventoryOpenEvent event);
    
    /**
     * Executes when player closes inventory
     * @param event event of inventory close
     */
    default void onClose(@NotNull InventoryCloseEvent event) {
        destroy();
    }
    
    /**
     * Returns the creation time of menu in milliseconds of Unix format
     * @return creation time of menu
     */
    long getCreationTime();
    
    /**
     * Destroys menu from memory and disables all event listeners for it
     */
    default void destroy() {
        MenusManager.getInstance().unregisterMenu(this);
    }
}