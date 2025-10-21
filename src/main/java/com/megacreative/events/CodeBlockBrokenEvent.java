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
 * 
 * Событие, возникающее при разрушении CodeBlock в мире разработки.
 * Это событие позволяет другим системам реагировать на удаление блоков без
 * прямого доступа к внутренним структурам данных BlockPlacementHandler.
 * 
 * Ereignis, das ausgelöst wird, wenn ein CodeBlock in einer Entwicklungs-Welt zerstört wird.
 * Dieses Ereignis ermöglicht es anderen Systemen, auf Blockentfernungen zu reagieren, ohne
 * direkt auf die internen Datenstrukturen von BlockPlacementHandler zuzugreifen.
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