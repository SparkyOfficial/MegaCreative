package ua.sparkybeta.sparkybetacreative.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    private static final Component PREFIX = Component.text("[Sparky] ", NamedTextColor.GOLD);

    public static void sendMessage(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(PREFIX.append(Component.text(message, color)));
    }

    public static void sendInfo(CommandSender sender, String message) {
        sendMessage(sender, message, NamedTextColor.YELLOW);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sendMessage(sender, message, NamedTextColor.GREEN);
    }

    public static void sendError(CommandSender sender, String message) {
        sendMessage(sender, message, NamedTextColor.RED);
    }
} 