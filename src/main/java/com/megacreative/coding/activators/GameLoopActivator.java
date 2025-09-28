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
 * Activator that triggers repeatedly at specified intervals
 * Similar to YottaCreative's GameLoopActivator
 */
public class GameLoopActivator extends NamedActivator {
    
    private int ticks = 20; // Default to 1 second (20 ticks)
    private int currentTicks = ticks;
    private GameEvent gameEvent;
    
    public GameLoopActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.GAME_LOOP;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.EMERALD_BLOCK);
    }
    
    public int getTicks() {
        return ticks;
    }
    
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }
    
    public int getCurrentTicks() {
        return currentTicks;
    }
    
    public void setCurrentTicks(int currentTicks) {
        this.currentTicks = currentTicks;
    }
    
    public void setGameEvent(GameEvent gameEvent) {
        this.gameEvent = gameEvent;
    }
    
    public GameEvent getGameEvent() {
        return gameEvent;
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        // For game loop, we might want to select all players in the world
        // This would be implemented based on the specific requirements
        super.execute(gameEvent, selectedEntities, stackCounter, callCounter);
    }
}