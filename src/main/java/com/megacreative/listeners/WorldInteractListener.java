package com.megacreative.listeners;

import com.megacreative.MegaCreative;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class WorldInteractListener implements Listener {
    
    private final MegaCreative plugin;
    
    public WorldInteractListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
            return;
        }
        
        String displayName = item.getItemMeta().getDisplayName();
        
        
        if (item.getType() == Material.DIAMOND && displayName.contains("Мои миры")) {
            event.setCancelled(true);
            player.performCommand("myworlds");
        }
        
        
        else if (item.getType() == Material.COMPASS && displayName.contains("Браузер миров")) {
            event.setCancelled(true);
            player.performCommand("worldbrowser");
        }
    }
}
