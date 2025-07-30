package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.gui.WorldCommentsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {
    private final MegaCreative plugin;

    public GuiListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String viewTitle = event.getView().getTitle();
        Inventory clickedInventory = event.getClickedInventory();
        
        if (clickedInventory == null || !viewTitle.startsWith("§")) {
            return;
        }
        
        // Handle WorldCommentsGUI clicks
        if (viewTitle.startsWith("§6§lКомментарии:")) {
            WorldCommentsGUI.handleClick(event, plugin);
            return;
        }
        
        // Add other GUI handlers here
        
        // Default: cancel the event for all our GUIs
        event.setCancelled(true);
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Handle comment input
        if (WorldCommentsGUI.handleChat(player, message, plugin)) {
            event.setCancelled(true);
        }
    }
}
