package com.megacreative.coding.containers;

import com.megacreative.coding.containers.ContainerType;
import org.bukkit.Location;

/**
 * Block container data
 * 
 * Данные контейнера блока
 * 
 * @author Андрій Будильников
 */
public class BlockContainer {
    private final Location blockLocation;
    private final Location containerLocation;
    private final ContainerType type;
    private final String action;
    private final long createdTime;
    
    public BlockContainer(Location blockLocation, Location containerLocation, ContainerType type, String action) {
        this.blockLocation = blockLocation;
        this.containerLocation = containerLocation;
        this.type = type;
        this.action = action;
        this.createdTime = System.currentTimeMillis();
    }
    
    public Location getBlockLocation() { return blockLocation; }
    public Location getContainerLocation() { return containerLocation; }
    public ContainerType getType() { return type; }
    public String getAction() { return action; }
    public long getCreatedTime() { return createdTime; }
}