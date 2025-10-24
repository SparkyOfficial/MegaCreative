package com.megacreative.services;

import com.megacreative.MegaCreative;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

/**
 * Service for handling modern messaging with Adventure API
 * Provides enhanced chat formatting and global messaging capabilities
 */
public class MessagingService {
    
    private final MegaCreative plugin;
    private final BukkitAudiences adventure;
    private final Logger logger;
    
    public MessagingService(MegaCreative plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.adventure = BukkitAudiences.create(plugin);
        logger.fine("MessagingService initialized with Adventure API");
    }
    
    /**
     * Sends a formatted message to a player using Adventure API
     * @param player The player to send the message to
     * @param message The message content
     */
    public void sendMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        
        Component component = Component.text()
            .content(message)
            .color(NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD)
            .build();
            
        adventure.player(player).sendMessage(component);
    }
    
    /**
     * Sends a formatted message to a command sender using Adventure API
     * @param sender The command sender to send the message to
     * @param message The message content
     */
    public void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null || message.isEmpty()) {
            return;
        }
        
        Component component = Component.text()
            .content(message)
            .color(NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD)
            .build();
            
        adventure.sender(sender).sendMessage(component);
    }
    
    /**
     * Sends a global chat message to all players with enhanced formatting
     * @param player The player who sent the message
     * @param message The message content
     */
    public void sendGlobalChatMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        
        
        Component chatMessage = Component.text()
            .append(Component.text("[Глобальный Чат] ", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text(player.getName() + ": ", NamedTextColor.AQUA))
            .append(Component.text(message, NamedTextColor.WHITE))
            .build();
        
        
        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            adventure.player(onlinePlayer).sendMessage(chatMessage);
        }
        
        
        adventure.console().sendMessage(chatMessage);
        
        logger.fine("[Global Chat] " + player.getName() + ": " + message);
    }
    
    /**
     * Sends an error message to a player with red formatting
     * @param player The player to send the error message to
     * @param message The error message content
     */
    public void sendErrorMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        
        Component component = Component.text()
            .content("❌ " + message)
            .color(NamedTextColor.RED)
            .decorate(TextDecoration.BOLD)
            .build();
            
        adventure.player(player).sendMessage(component);
    }
    
    /**
     * Sends a success message to a player with green formatting
     * @param player The player to send the success message to
     * @param message The success message content
     */
    public void sendSuccessMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        
        Component component = Component.text()
            .content("✅ " + message)
            .color(NamedTextColor.GREEN)
            .decorate(TextDecoration.BOLD)
            .build();
            
        adventure.player(player).sendMessage(component);
    }
    
    /**
     * Sends a warning message to a player with yellow formatting
     * @param player The player to send the warning message to
     * @param message The warning message content
     */
    public void sendWarningMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) {
            return;
        }
        
        Component component = Component.text()
            .content("⚠️ " + message)
            .color(NamedTextColor.YELLOW)
            .decorate(TextDecoration.BOLD)
            .build();
            
        adventure.player(player).sendMessage(component);
    }
    
    /**
     * Creates a component with gradient colors
     * @param text The text to apply gradient to
     * @return Component with gradient colors
     */
    public Component createGradientText(String text) {
        
        return Component.text(text)
            .color(NamedTextColor.GOLD);
    }
    
    /**
     * Gets the Adventure audience for a player
     * @param player The player
     * @return The Adventure audience
     */
    public Audience getPlayerAudience(Player player) {
        return adventure.player(player);
    }
    
    /**
     * Gets the Adventure audience for the console
     * @return The Adventure audience for console
     */
    public Audience getConsoleAudience() {
        return adventure.console();
    }
    
    /**
     * Gets the Adventure audience for a command sender
     * @param sender The command sender
     * @return The Adventure audience
     */
    public Audience getSenderAudience(CommandSender sender) {
        return adventure.sender(sender);
    }
    
    /**
     * Shuts down the Adventure API
     */
    public void shutdown() {
        if (adventure != null) {
            adventure.close();
            logger.fine("Adventure API shut down");
        }
    }
}