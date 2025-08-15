package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import java.util.ArrayList;
import java.util.List;

public class PlayerWorldChangeListener implements Listener {
    private final MegaCreative plugin;

    public PlayerWorldChangeListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();
        
        // Когда игрок меняет мир, заново устанавливаем ему скорборд
        plugin.getScoreboardManager().setScoreboard(player);
        
        // Проверяем инвентарь при входе в dev-мир
        if (toWorld.getName().endsWith("_dev")) {
            List<String> missingItems = getMissingCodingItems(player);
            if (!missingItems.isEmpty()) {
                CodingItems.giveMissingItems(player, missingItems);
                player.sendMessage("§e§l!§r §eДобавлены недостающие инструменты для кодинга: " + String.join(", ", missingItems));
            }
        }
    }
    
    /**
     * Проверяет, каких предметов для кодинга не хватает игроку
     */
    private List<String> getMissingCodingItems(Player player) {
        List<String> missingItems = new ArrayList<>();
        
        // Проверяем наличие ключевых предметов
        boolean hasLinker = false;
        boolean hasInspector = false;
        boolean hasEventBlock = false;
        boolean hasActionBlock = false;
        boolean hasConditionBlock = false;
        boolean hasVariableBlock = false;
        boolean hasRepeatBlock = false;
        
        for (var item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();
                if (name.contains("Связующий жезл")) hasLinker = true;
                if (name.contains("Инспектор блоков")) hasInspector = true;
                if (name.contains("Событие игрока")) hasEventBlock = true;
                if (name.contains("Действие игрока")) hasActionBlock = true;
                if (name.contains("Условие")) hasConditionBlock = true;
                if (name.contains("Переменная")) hasVariableBlock = true;
                if (name.contains("Повторить")) hasRepeatBlock = true;
            }
        }
        
        if (!hasLinker) missingItems.add("Связующий жезл");
        if (!hasInspector) missingItems.add("Инспектор блоков");
        if (!hasEventBlock) missingItems.add("Блок события");
        if (!hasActionBlock) missingItems.add("Блок действия");
        if (!hasConditionBlock) missingItems.add("Блок условия");
        if (!hasVariableBlock) missingItems.add("Блок переменной");
        if (!hasRepeatBlock) missingItems.add("Блок повтора");
        
        return missingItems;
    } 
} 