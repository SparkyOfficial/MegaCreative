package com.megacreative.coding.events;

import com.megacreative.coding.CodeBlock;
import com.megacreative.models.CreativeWorld;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when the structure of a script changes due to block connections being modified.
 * This event allows the ScriptCompiler to react to structural changes and recompile scripts.
 */
public class ScriptStructureChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final CreativeWorld creativeWorld;
    private final CodeBlock modifiedBlock;
    private final ChangeType changeType;
    
    public enum ChangeType {
        BLOCK_ADDED,
        BLOCK_REMOVED,
        CONNECTION_CHANGED
    }
    
    public ScriptStructureChangedEvent(CreativeWorld creativeWorld, CodeBlock modifiedBlock, ChangeType changeType) {
        this.creativeWorld = creativeWorld;
        this.modifiedBlock = modifiedBlock;
        this.changeType = changeType;
    }
    
    public CreativeWorld getCreativeWorld() {
        return creativeWorld;
    }
    
    public CodeBlock getModifiedBlock() {
        return modifiedBlock;
    }
    
    public ChangeType getChangeType() {
        return changeType;
    }
    
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}