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
        // Обработка кликов в GUI меню
        String title = event.getView().getTitle();
        
        // Блокируем клики в GUI MegaCreative, но разрешаем в обычных инвентарях
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
        
        // Разрешаем клики в обычных инвентарях (сундуки, печки и т.д.)
        // но только если игрок не в /dev мире
        if (event.getWhoClicked() instanceof Player player) {
            if (player.getWorld().getName().endsWith("_dev")) {
                // В /dev мире дополнительная защита уже есть в DevWorldProtectionListener
                return;
            }
        }
    }
}
