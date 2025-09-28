package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.GameEvent;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that triggers when a player quits the world
 */
public class PlayerQuitActivator extends Activator {
    
    public PlayerQuitActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.PLAYER_QUIT;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.IRON_DOOR);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        // Set the selected entities
        this.selectedEntities = selectedEntities;
        
        // Execute all actions associated with this activator
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                // Get the script engine from the plugin
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    // Execute the action block
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_player_quit")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "PlayerQuit activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in PlayerQuit activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in PlayerQuit activator: " + e.getMessage()
                );
            }
        }
    }
}