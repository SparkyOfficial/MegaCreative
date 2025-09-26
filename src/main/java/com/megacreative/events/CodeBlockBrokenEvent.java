package com.megacreative.events;

import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Event fired when a CodeBlock is broken in a development world.
 * This event allows other systems to react to block removal without
 * directly accessing BlockPlacementHandler's internal data structures.
 */
public class CodeBlockBrokenEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final CodeBlock codeBlock;
    private final Location location;
    
    public CodeBlockBrokenEvent(Player player, CodeBlock codeBlock, Location location) {
        this.player = player;
        this.codeBlock = codeBlock;
        this.location = location;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public CodeBlock getCodeBlock() {
        return codeBlock;
    }
    
    public Location getLocation() {
        return location;
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