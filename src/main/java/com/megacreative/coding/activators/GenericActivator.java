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
 * Generic activator that can be used for any activator type
 */
public class GenericActivator extends Activator {
    
    private final ActivatorType type;
    private final ItemStack icon;
    
    public GenericActivator(MegaCreative plugin, CreativeWorld world, ActivatorType type) {
        super(plugin, world);
        this.type = type;
        this.icon = type.getIcon();
    }
    
    @Override
    public ActivatorType getType() {
        return type;
    }
    
    @Override
    public ItemStack getIcon() {
        return icon;
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        
        this.selectedEntities = selectedEntities;
        
        
        
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_" + getType().name().toLowerCase())
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "Generic activator execution failed for " + getType().getDisplayName() + ": " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in generic activator execution for " + getType().getDisplayName() + ": " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in generic activator " + getType().getDisplayName() + ": " + e.getMessage()
                );
            }
        }
    }
}