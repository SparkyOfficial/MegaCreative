package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for all activators that trigger code execution
 * Similar to YottaCreative's activator system but adapted for MegaCreative's architecture
 */
public abstract class Activator {
    
    protected MegaCreative plugin;
    protected CreativeWorld world;
    protected List<CodeBlock> actionList;
    protected List<Entity> selectedEntities;
    protected String customName;
    protected UUID id;
    
    public Activator(MegaCreative plugin, CreativeWorld world) {
        this.plugin = plugin;
        this.world = world;
        this.id = UUID.randomUUID();
        this.actionList = new java.util.ArrayList<>();
        this.selectedEntities = new java.util.ArrayList<>();
    }
    
    /**
     * Gets the type of this activator
     */
    public abstract ActivatorType getType();
    
    /**
     * Gets the icon representation for this activator
     */
    public abstract org.bukkit.inventory.ItemStack getIcon();
    
    /**
     * Executes this activator with the given event and entities
     */
    public abstract void execute(com.megacreative.coding.events.GameEvent gameEvent, 
                               List<Entity> selectedEntities, 
                               int stackCounter, 
                               AtomicInteger callCounter);
    
    /**
     * Gets the list of actions associated with this activator
     */
    public List<CodeBlock> getActionList() {
        return actionList;
    }
    
    /**
     * Adds an action to this activator
     */
    public void addAction(CodeBlock action) {
        actionList.add(action);
    }
    
    /**
     * Gets the creative world this activator belongs to
     */
    public CreativeWorld getWorld() {
        return world;
    }
    
    /**
     * Gets the selected entities for this activator
     */
    public List<Entity> getSelectedEntities() {
        return selectedEntities;
    }
    
    /**
     * Sets the selected entities for this activator
     */
    public void setSelectedEntities(List<Entity> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }
    
    /**
     * Gets the custom name of this activator
     */
    public String getCustomName() {
        return customName;
    }
    
    /**
     * Sets the custom name of this activator
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }
    
    /**
     * Gets the unique ID of this activator
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Simple execution method that uses default entity from game event
     */
    public void execute(com.megacreative.coding.events.GameEvent gameEvent, 
                       int stackCounter, 
                       AtomicInteger callCounter) {
        // Use the player from the game event as the default entity
        List<Entity> entities = new java.util.ArrayList<>();
        if (gameEvent.getPlayer() != null) {
            entities.add(gameEvent.getPlayer());
        } else if (gameEvent.getEntity() != null) {
            entities.add(gameEvent.getEntity());
        }
        execute(gameEvent, entities, stackCounter, callCounter);
    }
}