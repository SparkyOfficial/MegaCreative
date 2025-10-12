package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.events.MegaInventoryOpenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.Plugin;

/**
 * Bukkit listener that converts InventoryOpenEvent to MegaInventoryOpenEvent
 * This is a "dumb" listener that only converts Bukkit events to our custom events
 */
public class BukkitInventoryOpenListener implements Listener {
    private final Plugin plugin;
    
    public BukkitInventoryOpenListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent bukkitEvent) {
        
        if (!(bukkitEvent.getPlayer() instanceof org.bukkit.entity.Player)) {
            return;
        }
        
        
        MegaInventoryOpenEvent internalEvent = new MegaInventoryOpenEvent(
            (org.bukkit.entity.Player) bukkitEvent.getPlayer(),
            bukkitEvent.getInventory()
        );
        
        
        plugin.getServer().getPluginManager().callEvent(internalEvent);
    }
}