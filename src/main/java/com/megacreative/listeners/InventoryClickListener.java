package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
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
        
        String title = event.getView().getTitle();
        
        
        if (title.contains("MegaCreative") || 
            title.contains("Выберите действие") ||
            title.contains("Настройка:") ||
            title.contains("Создать шаблон данных") ||
            title.contains("Мои миры") ||
            title.contains("Браузер миров") ||
            title.contains("Настройки мира") ||
            title.contains("Скрипты") ||
            title.contains("Шаблоны")) {
            event.setCancelled(true);
        }
        
        
        
        if (event.getWhoClicked() instanceof Player player) {
            if (player.getWorld().getName().endsWith("_dev")) {
                
                return;
            }
        }
    }
}
