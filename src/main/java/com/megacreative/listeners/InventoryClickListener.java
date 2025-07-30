package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    
    private final MegaCreative plugin;
    
    public InventoryClickListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Обработка кликов в GUI меню
        // Пока что базовая заглушка
        if (event.getView().getTitle().contains("MegaCreative")) {
            event.setCancelled(true);
        }
    }
}
