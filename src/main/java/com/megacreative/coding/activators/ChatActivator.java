package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles player chat events.
 * This activator listens to PlayerChatEvent and triggers script execution.
 */
public class ChatActivator extends BukkitEventActivator {
    
    private String keyword;
    private boolean anyMessage = true;
    
    public ChatActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    /**
     * Sets the keyword this activator should listen for
     * @param keyword The keyword to listen for
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
        this.anyMessage = false;
    }
    
    /**
     * Sets whether this activator should listen for any message
     * @param anyMessage true to listen for any message, false to listen for specific keyword
     */
    public void setAnyMessage(boolean anyMessage) {
        this.anyMessage = anyMessage;
    }
    
    /**
     * Gets the keyword this activator listens for
     * @return The keyword
     */
    public String getKeyword() {
        return keyword;
    }
    
    /**
     * Checks if this activator listens for any message
     * @return true if listening for any message, false otherwise
     */
    public boolean isAnyMessage() {
        return anyMessage;
    }
    
    @Override
    public String getEventName() {
        return "onChat";
    }
    
    @Override
    public String getDisplayName() {
        return "Player Chat Event";
    }
    
    /**
     * Activates this activator for a player chat event
     * @param player The player who sent the message
     * @param message The message that was sent
     */
    public void activate(Player player, String message) {
        if (!enabled || script == null) {
            return;
        }
        
        // Check if we should only trigger for specific keywords
        if (!anyMessage && keyword != null && !message.contains(keyword)) {
            return;
        }
        
        // Create a game event with chat context
        GameEvent gameEvent = new GameEvent("onChat");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        gameEvent.setMessage(message);
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("message", message);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}