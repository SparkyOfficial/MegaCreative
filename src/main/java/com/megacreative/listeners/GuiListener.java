package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.managers.GUIManager;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GuiListener implements Listener {
    
    private final MegaCreative plugin;
    
    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().unregisterGUI(player);
    }
    
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