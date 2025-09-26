package com.megacreative.coding.events;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CodeBlocksConnectedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Location fromLocation;
    private final Location toLocation;

    public CodeBlocksConnectedEvent(Location from, Location to) {
        this.fromLocation = from;
        this.toLocation = to;
    }

    public Location getFromLocation() {
        return fromLocation;
    }

    public Location getToLocation() {
        return toLocation;
    }

    @NotNull @Override public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}