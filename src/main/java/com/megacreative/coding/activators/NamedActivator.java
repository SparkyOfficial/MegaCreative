package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.coding.events.GameEvent;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for activators that have a custom name
 * Similar to YottaCreative's NamedActivator
 */
public abstract class NamedActivator extends Activator {
    
    protected String customName;
    
    public NamedActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    public String getCustomName() {
        return customName;
    }
    
    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        // Set the selected entities
        this.selectedEntities = selectedEntities;
        
        // Execute all actions associated with this activator
        // This would integrate with the existing script execution system
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                // Get the script engine from the plugin
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    // Execute the action block
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_" + getType().name().toLowerCase())
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "Activator execution failed for " + getType().getDisplayName() + ": " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in activator execution for " + getType().getDisplayName() + ": " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in activator " + getType().getDisplayName() + ": " + e.getMessage()
                );
            }
        }
    }
}