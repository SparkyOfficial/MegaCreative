package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodingItems;
import com.megacreative.managers.PlayerModeManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.GameMode;
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
        plugin.getServiceRegistry().getScoreboardManager().setScoreboard(player);
        
        // Save inventory when leaving dev world
        if (isDevWorld(fromWorld.getName())) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }

        // Determine the creative world
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(toWorld);
        if (creativeWorld == null) {
            // If player switched to a normal world (not dev/play), restore their inventory
            plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
            player.setGameMode(GameMode.SURVIVAL); // or another default mode
            return;
        }

        // Configure player based on the type of world they entered
        if (isDevWorld(toWorld.getName())) {
            // Player entered DEV world
            player.getInventory().clear();
            CodingItems.giveCodingItems(player, plugin); // Give coding items ONLY here
            player.setGameMode(GameMode.CREATIVE);
            plugin.getServiceRegistry().getPlayerModeManager().setMode(player, PlayerModeManager.PlayerMode.DEV);
            player.sendMessage("§eВы вошли в режим разработки.");
        } else {
            // Player entered PLAY world or another world
            plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
            player.setGameMode(GameMode.ADVENTURE); // Game mode for playing
            plugin.getServiceRegistry().getPlayerModeManager().setMode(player, PlayerModeManager.PlayerMode.PLAY);
            player.sendMessage("§aВы вошли в игровой режим.");
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
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            String mode = determineWorldMode(world, creativeWorld);
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, creativeWorld.getId(), mode);
        }
        
        // Configure player based on the world they're in
        if (isDevWorld(world.getName())) {
            // Player is in DEV world
            player.getInventory().clear();
            CodingItems.giveCodingItems(player, plugin); // Give coding items ONLY here
            player.setGameMode(GameMode.CREATIVE);
            plugin.getServiceRegistry().getPlayerModeManager().setMode(player, PlayerModeManager.PlayerMode.DEV);
            player.sendMessage("§eВы вошли в режим разработки.");
        } else if (creativeWorld != null) {
            // Player is in PLAY world
            plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
            player.setGameMode(GameMode.ADVENTURE);
            plugin.getServiceRegistry().getPlayerModeManager().setMode(player, PlayerModeManager.PlayerMode.PLAY);
            player.sendMessage("§aВы вошли в игровой режим.");
        } else {
            // Player is in a non-creative world (hub, etc.)
            // Give them starter items
            plugin.getServiceRegistry().getPlayerManager().giveStarterItems(player);
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
        CreativeWorld creativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(world);
        if (creativeWorld != null) {
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldExit(player, creativeWorld.getId());
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
        CreativeWorld fromCreativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(fromWorld);
        if (fromCreativeWorld != null) {
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldExit(player, fromCreativeWorld.getId());
        }
        
        // Track entry to new world
        CreativeWorld toCreativeWorld = plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(toWorld);
        if (toCreativeWorld != null) {
            String mode = determineWorldMode(toWorld, toCreativeWorld);
            plugin.getServiceRegistry().getPlayerManager().trackPlayerWorldEntry(player, toCreativeWorld.getId(), mode);
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
     * Check if a world is a development world
     */
    private boolean isDevWorld(String worldName) {
        return worldName.endsWith("_dev") || worldName.contains("-code");
    }
}