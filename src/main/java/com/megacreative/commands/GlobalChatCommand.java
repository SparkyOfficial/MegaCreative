package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.services.MessagingService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for sending messages to global chat using /cc <message>
 */
public class GlobalChatCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public GlobalChatCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check if player has permission
        if (!player.hasPermission("megacreative.globalchat")) {
            player.sendMessage("§cYou don't have permission to use global chat!");
            return true;
        }
        
        // Check if message is provided
        if (args.length == 0) {
            player.sendMessage("§cUsage: /cc <message>");
            return true;
        }
        
        // Join all arguments to form the message
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) {
                messageBuilder.append(" ");
            }
        }
        
        String message = messageBuilder.toString();
        
        // Get messaging service and send global chat message
        MessagingService messagingService = plugin.getServiceRegistry().getMessagingService();
        if (messagingService != null) {
            messagingService.sendGlobalChatMessage(player, message);
        } else {
            // Fallback to regular chat if messaging service is not available
            plugin.getServer().broadcastMessage("§6[Global Chat] §b" + player.getName() + ": §f" + message);
        }
        
        return true;
    }
}