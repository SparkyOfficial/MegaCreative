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
 * Activator that triggers when a player moves
 */
public class PlayerMoveActivator extends Activator {
    
    public PlayerMoveActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.PLAYER_MOVE;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.FEATHER);
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
                        "activator_player_move")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "PlayerMove activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in PlayerMove activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in PlayerMove activator: " + e.getMessage()
                );
            }
        }
    }
}