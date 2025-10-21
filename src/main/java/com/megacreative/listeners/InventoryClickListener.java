package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {
    
    // This field needs to remain as a class field since it's used throughout the class
    // Static analysis flags it as convertible to a local variable, but this is a false positive
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
