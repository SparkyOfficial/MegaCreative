package com.megacreative.coding.events;

import com.megacreative.coding.CodeBlock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when two code blocks are connected
 */
public class CodeBlocksConnectedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final CodeBlock previousBlock;
    private final CodeBlock currentBlock;
    
    public CodeBlocksConnectedEvent(CodeBlock previousBlock, CodeBlock currentBlock) {
        this.previousBlock = previousBlock;
        this.currentBlock = currentBlock;
    }
    
    public CodeBlock getPreviousBlock() {
        return previousBlock;
    }
    
    public CodeBlock getCurrentBlock() {
        return currentBlock;
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