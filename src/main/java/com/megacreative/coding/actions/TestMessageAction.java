package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;

/**
 * Простое тестовое действие для демонстрации системы
 */
public class TestMessageAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        player.sendMessage("§a✓ Система визуального программирования работает!");
        player.sendMessage("§e⚡ Скрипт выполнен успешно в " + System.currentTimeMillis() + "мс");
        player.sendMessage("§b🎯 Мир: " + (context.getCreativeWorld() != null ? context.getCreativeWorld().getName() : "неизвестен"));
        
        // Логируем выполнение для отладки
        if (context.getPlugin() != null) {
            context.getPlugin().getLogger().info("TestMessageAction выполнен для игрока: " + player.getName());
        }
    }
}
