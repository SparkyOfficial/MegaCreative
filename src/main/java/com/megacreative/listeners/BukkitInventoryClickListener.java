package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaInventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts InventoryClickEvent to MegaInventoryClickEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitInventoryClickListener implements Listener {
    private final Plugin plugin;
    
    public BukkitInventoryClickListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent bukkitEvent) {
        
        if (!(bukkitEvent.getWhoClicked() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        
        MegaInventoryClickEvent internalEvent = new MegaInventoryClickEvent(
            (org.bukkit.entity.Player) bukkitEvent.getWhoClicked(),
            bukkitEvent.getInventory(),
            bukkitEvent.getSlot(),
            bukkitEvent.getRawSlot(),
            bukkitEvent.getClick()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}