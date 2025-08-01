package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldComment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import com.megacreative.gui.WorldCommentsGUI;

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
        
        // Handle WorldCommentsGUI clicks (now handled by the GUI itself)
        if (viewTitle.startsWith("§6§lКомментарии:")) {
            // The GUI handles its own clicks now
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

        // Handle удаление мира
        if (plugin.getDeleteConfirmations().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            if (message.equalsIgnoreCase("УДАЛИТЬ")) {
                // Получаем worldId
                String worldId = plugin.getDeleteConfirmations().get(player.getUniqueId());
                var world = plugin.getWorldManager().getWorld(worldId);
                if (world != null) {
                    plugin.getWorldManager().deleteWorld(world.getId(), player);
                    player.sendMessage("§aМир успешно удалён!");
                } else {
                    player.sendMessage("§cМир не найден!");
                }
            } else {
                player.sendMessage("§cУдаление отменено.");
            }
            plugin.getDeleteConfirmations().remove(player.getUniqueId());
            return;
        }

        // Handle comment input
        if (plugin.getCommentInputs().containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            
            CreativeWorld world = plugin.getCommentInputs().get(player.getUniqueId());
            
            // Check for cancel command
            if (message.equalsIgnoreCase("отмена")) {
                player.sendMessage("§cДобавление комментария отменено.");
                plugin.getCommentInputs().remove(player.getUniqueId());
                return;
            }
            
            // Validate message length
            if (message.length() > 100) {
                player.sendMessage("§cКомментарий слишком длинный! Максимум 100 символов.");
                return;
            }
            
            // Create and add the comment
            WorldComment comment = new WorldComment(
                player.getUniqueId(),
                player.getName(),
                message,
                System.currentTimeMillis()
            );
            
            world.addComment(comment);
            
            // Save the world to persist the comment
            plugin.getWorldManager().saveWorld(world);
            
            // Remove from inputs and show the updated comments GUI
            plugin.getCommentInputs().remove(player.getUniqueId());
            
            // Open the comments GUI on the first page
            new WorldCommentsGUI(plugin, player, world, 0).open();
            
            return;
        }

        // Handle coding block action input (removed - now using GUI)
        // if (plugin.getBlockPlacementHandler().handleActionInput(player, message)) {
        //     event.setCancelled(true);
        //     return;
        // }
    }
}
