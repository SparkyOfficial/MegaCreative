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
 * Activator that represents a callable function
 * Similar to YottaCreative's FunctionActivator
 */
public class FunctionActivator extends NamedActivator {
    
    public FunctionActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.FUNCTION;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.LAPIS_BLOCK);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        // Set the selected entities
        this.selectedEntities = selectedEntities;
        
        // Execute all actions associated with this function
        super.execute(gameEvent, selectedEntities, stackCounter, callCounter);
    }
}