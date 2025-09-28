package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles tick events.
 * This activator listens to tick events and triggers script execution.
 */
public class TickActivator extends Activator {
    
    private Location location;
    private final GameEventFactory eventFactory;
    private String tickType; // "onTick", "onSecond", "onMinute"
    private boolean enabled = true;
    private CodeBlock script;
    
    public TickActivator(MegaCreative plugin, CreativeWorld world, String tickType) {
        super(plugin, world);
        this.eventFactory = new GameEventFactory();
        this.tickType = tickType;
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.TICK;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.CLOCK);
    }
    
    /**
     * Sets the tick type for this activator
     */
    public void setTickType(String tickType) {
        this.tickType = tickType;
    }
    
    /**
     * Gets the tick type for this activator
     */
    public String getTickType() {
        return tickType;
    }
    
    /**
     * Sets the location of this activator in the world
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public CodeBlock getScript() {
        return script;
    }
    
    public void setScript(CodeBlock script) {
        this.script = script;
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        if (!enabled || script == null) {
            return;
        }
        
        // Execute all actions associated with this activator
        for (CodeBlock action : actionList) {
            try {
                // Get the script engine from the plugin
                ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    // Convert the first entity to a player if possible
                    Player player = null;
                    if (!selectedEntities.isEmpty() && selectedEntities.get(0) instanceof Player) {
                        player = (Player) selectedEntities.get(0);
                    }
                    
                    // Execute the action block
                    scriptEngine.executeBlock(action, player, "activator_tick")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "Tick activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in Tick activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in Tick activator: " + e.getMessage()
                );
            }
        }
    }
    
    /**
     * Activates this activator for a tick event
     * @param tickNumber The current tick number
     */
    public void activate(long tickNumber) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with tick context
        GameEvent gameEvent = new GameEvent(tickType);
        gameEvent.setCustomData("tick", tickNumber);
        
        // Activate the script
        execute(gameEvent, new java.util.ArrayList<>(), 0, new AtomicInteger(0));
    }
}