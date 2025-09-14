package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.models.CreativeWorld;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listener for player world change events
 *
 * Слушатель для событий смены мира игроками
 *
 * Listener für Spieler-Weltwechsel-Ereignisse
 */
public class PlayerWorldChangeListener implements Listener {
    private final MegaCreative plugin;

    /**
     * Constructor for PlayerWorldChangeListener
     * @param plugin the main plugin
     *
     * Конструктор для PlayerWorldChangeListener
     * @param plugin основной плагин
     *
     * Konstruktor für PlayerWorldChangeListener
     * @param plugin das Haupt-Plugin
     */
    public PlayerWorldChangeListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles world change events
     * @param event the player changed world event
     *
     * Обрабатывает события смены мира
     * @param event событие смены мира игроком
     *
     * Verarbeitet Weltwechsel-Ereignisse
     * @param event das Spieler-Weltwechsel-Ereignis
     */
    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();
        
        // 🎆 ENHANCED: Track world changes for analytics
        trackWorldChange(player, fromWorld, toWorld);
        
        // Когда игрок меняет мир, заново устанавливаем ему скорборд
        plugin.getScoreboardManager().setScoreboard(player);
        
        // Проверяем инвентарь при входе в dev-мир
        if (toWorld.getName().endsWith("_dev") || toWorld.getName().endsWith("-code")) {
            List<String> missingItems = getMissingCodingItems(player);
            if (!missingItems.isEmpty()) {
                CodingItems.giveMissingItems(player, missingItems);
            }
        }
    }
    
    /**
     * Handles player join events
     * @param event the player join event
     *
     * Обрабатывает события входа игроков
     * @param event событие входа игрока
     *
     * Verarbeitet Spieler-Beitritts-Ereignisse
     * @param event das Spieler-Beitritts-Ereignis
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // 🎆 ENHANCED: Track join to current world
        // 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживание входа в текущий мир
        // 🎆 VERBESSERTE: Verfolgung des Beitritts zur aktuellen Welt
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            String mode = determineWorldMode(world, creativeWorld);
            plugin.getPlayerManager().trackPlayerWorldEntry(player, creativeWorld.getId(), mode);
        }
    }
    
    /**
     * Handles player quit events
     * @param event the player quit event
     *
     * Обрабатывает события выхода игроков
     * @param event событие выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     * @param event das Spieler-Verlassen-Ereignis
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        
        // 🎆 ENHANCED: Track exit from current world
        // 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживание выхода из текущего мира
        // 🎆 VERBESSERTE: Verfolgung des Verlassens der aktuellen Welt
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            plugin.getPlayerManager().trackPlayerWorldExit(player, creativeWorld.getId());
        }
    }
    
    /**
     * 🎆 ENHANCED: Tracks world changes for dual world analytics
     *
     * 🎆 УСОВЕРШЕНСТВОВАННАЯ: Отслеживает изменения мира для аналитики парных миров
     *
     * 🎆 VERBESSERTE: Verfolgt Weltwechsel für duale Welt-Analysen
     */
    private void trackWorldChange(Player player, World fromWorld, World toWorld) {
        // Track exit from previous world
        CreativeWorld fromCreativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(fromWorld);
        if (fromCreativeWorld != null) {
            plugin.getPlayerManager().trackPlayerWorldExit(player, fromCreativeWorld.getId());
        }
        
        // Track entry to new world
        CreativeWorld toCreativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(toWorld);
        if (toCreativeWorld != null) {
            String mode = determineWorldMode(toWorld, toCreativeWorld);
            plugin.getPlayerManager().trackPlayerWorldEntry(player, toCreativeWorld.getId(), mode);
        }
    }
    
    /**
     * 🎆 ENHANCED: Determines the world mode based on world name and dual architecture
     *
     * 🎆 УСОВЕРШЕНСТВОВАННАЯ: Определяет режим мира на основе имени мира и двойной архитектуры
     *
     * 🎆 VERBESSERTE: Bestimmt den Weltmodus basierend auf dem Weltname und der dualen Architektur
     */
    private String determineWorldMode(World world, CreativeWorld creativeWorld) {
        String worldName = world.getName();
        
        if (worldName.endsWith("-code") || worldName.endsWith("_dev")) {
            return "DEV";
        } else if (worldName.endsWith("-world")) {
            return "PLAY";
        } else if (creativeWorld.getDualMode() != null) {
            return creativeWorld.getDualMode().name();
        } else {
            return "UNKNOWN";
        }
    }
    
    /**
     * Проверяет, каких предметов для кодинга не хватает игроку
     *
     * Checks which coding items are missing for the player
     *
     * Prüft, welche Coding-Items dem Spieler fehlen
     */
    private List<String> getMissingCodingItems(Player player) {
        List<String> missingItems = new ArrayList<>();
        
        // Проверяем наличие ключевых предметов
        boolean hasEventBlock = false;
        boolean hasActionBlock = false;
        boolean hasConditionBlock = false;
        boolean hasVariableBlock = false;
        boolean hasRepeatBlock = false;
        
        for (var item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String name = item.getItemMeta().getDisplayName();

                if (name.contains("Событие игрока")) hasEventBlock = true;
                if (name.contains("Действие игрока")) hasActionBlock = true;
                if (name.contains("Условие")) hasConditionBlock = true;
                if (name.contains("Переменная")) hasVariableBlock = true;
                if (name.contains("Повторить")) hasRepeatBlock = true;
            }
        }

        if (!hasEventBlock) missingItems.add("Блок события");
        if (!hasActionBlock) missingItems.add("Блок действия");
        if (!hasConditionBlock) missingItems.add("Блок условия");
        if (!hasVariableBlock) missingItems.add("Блок переменной");
        if (!hasRepeatBlock) missingItems.add("Блок повтора");
        
        return missingItems;
    }
}