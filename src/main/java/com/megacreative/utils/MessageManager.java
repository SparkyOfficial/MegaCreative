package com.megacreative.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class MessageManager {

    private static final String PREFIX = ChatColor.translateAlternateColorCodes('&', "&a&lMegaCreative &8>> &r");

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + ChatColor.translateAlternateColorCodes('&', message));
    }
}
