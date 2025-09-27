package com.megacreative.coding.activators;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all activators.
 * Activators are the "glue" that connects Bukkit events to script execution.
 * They listen to Bukkit events, package them into GameEvents, and trigger script execution.
 */
public abstract class Activator {
    
    protected final String id;
    protected final CreativeWorld creativeWorld;
    protected final ScriptEngine scriptEngine;
    protected CodeBlock eventBlock;
    protected CodeScript script;
    protected boolean enabled = true;
    
    public Activator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        this.id = UUID.randomUUID().toString();
        this.creativeWorld = creativeWorld;
        this.scriptEngine = scriptEngine;
    }
    
    /**
     * Gets the unique ID of this activator
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the creative world this activator belongs to
     */
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    /**
     * Sets the event block that triggers this activator
     */
    public void setEventBlock(CodeBlock eventBlock) {
        this.eventBlock = eventBlock;
    }
    
    /**
     * Gets the event block that triggers this activator
     */
    public CodeBlock getEventBlock() {
        return eventBlock;
    }
    
    /**
     * Sets the script associated with this activator
     */
    public void setScript(CodeScript script) {
        this.script = script;
    }
    
    /**
     * Gets the script associated with this activator
     */
    public CodeScript getScript() {
        return script;
    }
    
    /**
     * Checks if this activator is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Sets whether this activator is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Activates this activator, triggering script execution
     * @param gameEvent The game event that triggered this activation
     * @param player The player associated with the event (can be null)
     */
    public void activate(GameEvent gameEvent, Player player) {
        if (!enabled || script == null) {
            return;
        }
        
        // Execute the script through the script engine
        scriptEngine.executeScript(script, player, gameEvent.getEventName());
    }
    
    /**
     * Gets the name of the event this activator handles
     */
    public abstract String getEventName();
    
    /**
     * Gets the display name of this activator
     */
    public abstract String getDisplayName();
    
    /**
     * Gets the location of this activator in the world
     */
    public abstract Location getLocation();
}