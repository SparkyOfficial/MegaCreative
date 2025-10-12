package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles player chat events.
 * This activator listens to PlayerChatEvent and triggers script execution.
 */
public class ChatActivator extends BukkitEventActivator {
    
    private String keyword;
    private boolean anyMessage = true;
    private boolean enabled = true;
    private com.megacreative.coding.CodeBlock script;
    
    public ChatActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
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
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public com.megacreative.coding.CodeBlock getScript() {
        return script;
    }
    
    public void setScript(com.megacreative.coding.CodeBlock script) {
        this.script = script;
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.CHAT;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.BOOK);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        if (!enabled || script == null) {
            return;
        }
        
        
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    
                    Player player = null;
                    if (!selectedEntities.isEmpty() && selectedEntities.get(0) instanceof Player) {
                        player = (Player) selectedEntities.get(0);
                    }
                    
                    
                    scriptEngine.executeBlock(action, player, "activator_chat")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "Chat activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in Chat activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in Chat activator: " + e.getMessage()
                );
            }
        }
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
        
        
        if (!anyMessage && keyword != null && !message.contains(keyword)) {
            return;
        }
        
        
        GameEvent gameEvent = new GameEvent("onChat");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        gameEvent.setMessage(message);
        
        
        Map<String, Object> customData = new HashMap<>();
        customData.put("message", message);
        gameEvent.setCustomData(customData);
        
        
        execute(gameEvent, new java.util.ArrayList<>(), 0, new java.util.concurrent.atomic.AtomicInteger(0));
    }
}