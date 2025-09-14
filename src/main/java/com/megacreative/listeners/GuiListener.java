package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for GUI-related events
 *
 * Слушатель для событий, связанных с GUI
 *
 * Listener für GUI-bezogene Ereignisse
 */
public class GuiListener implements Listener {
    
    private final MegaCreative plugin;
    
    /**
     * Constructor for GuiListener
     * @param plugin the main plugin
     *
     * Конструктор для GuiListener
     * @param plugin основной плагин
     *
     * Konstruktor für GuiListener
     * @param plugin das Haupt-Plugin
     */
    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player quit events
     * @param event the player quit event
     *
     * Обрабатывает события выхода игрока
     * @param event событие выхода игрока
     *
     * Verarbeitet Spieler-Verlassen-Ereignisse
     * @param event das Spieler-Verlassen-Ereignis
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().unregisterGUI(player);
    }
    
    /**
     * Handles player chat events
     * @param event the async player chat event
     *
     * Обрабатывает события чата игроков
     * @param event асинхронное событие чата игрока
     *
     * Verarbeitet Spieler-Chat-Ereignisse
     * @param event das asynchrone Spieler-Chat-Ereignis
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().trim();
        
        if (message.equalsIgnoreCase("УДАЛИТЬ")) {
            event.setCancelled(true);
            
            GUIManager guiManager = plugin.getGuiManager();
            if (guiManager.isAwaitingDeleteConfirmation(player)) {
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        String worldId = guiManager.getDeleteConfirmationWorldId(player);
                        var world = plugin.getWorldManager().getWorld(worldId);
                        if (world != null) {
                            plugin.getWorldManager().deleteWorld(world.getId(), player);
                            player.sendMessage("§aWorld deleted successfully!");
                        } else {
                            player.sendMessage("§cWorld not found!");
                        }
                        guiManager.clearDeleteConfirmation(player);
                    }
                }.runTask(plugin);
            } else {
                // Check legacy delete confirmation system
                java.util.Map<java.util.UUID, String> deleteConfirmations = plugin.getDeleteConfirmations();
                if (deleteConfirmations.containsKey(player.getUniqueId())) {
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            String worldId = deleteConfirmations.get(player.getUniqueId());
                            var world = plugin.getWorldManager().getWorld(worldId);
                            if (world != null) {
                                plugin.getWorldManager().deleteWorld(world.getId(), player);
                                player.sendMessage("§aWorld deleted successfully!");
                            } else {
                                player.sendMessage("§cWorld not found!");
                            }
                            deleteConfirmations.remove(player.getUniqueId());
                        }
                    }.runTask(plugin);
                } else {
                    // Try to find world from current player location as fallback
                    CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    if (currentWorld != null) {
                        new org.bukkit.scheduler.BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.getWorldManager().deleteWorld(currentWorld.getId(), player);
                                player.sendMessage("§aWorld deleted successfully!");
                            }
                        }.runTask(plugin);
                    } else {
                        player.sendMessage("§cNo deletion confirmation pending.");
                    }
                }
            }
        }
    }
}