package com.megacreative.coding.groups;

import com.megacreative.coding.CodeBlock;
import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.Objects;

/**
 * Represents a group of code blocks that can be collapsed and expanded as a unit
 */
public class BlockGroup {
    
    private final UUID id;
    private final String name;
    private final UUID owner;
    private final Map<Location, CodeBlock> blocks;
    private final BlockGroupManager.GroupBounds bounds;
    
    private boolean collapsed = false;
    private long createdTime;
    private long lastModified;
    
    public BlockGroup(UUID id, String name, UUID owner, 
                     Map<Location, CodeBlock> blocks, 
                     BlockGroupManager.GroupBounds bounds) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.blocks = blocks;
        this.bounds = bounds;
        this.createdTime = System.currentTimeMillis();
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Updates the last modified timestamp
     */
    public void touch() {
        this.lastModified = System.currentTimeMillis();
    }
    
    /**
     * Sets the collapsed state and updates last modified time
     */
    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
        touch();
    }
    
    /**
     * Gets the number of blocks in this group
     */
    public int getBlockCount() {
        return blocks.size();
    }
    
    /**
     * Checks if this group contains a specific location
     */
    public boolean containsLocation(Location location) {
        return blocks.containsKey(location);
    }
    
    /**
     * Gets a code block at a specific location
     */
    public CodeBlock getBlockAt(Location location) {
        return blocks.get(location);
    }
    
    /**
     * Adds a block to this group
     */
    public void addBlock(Location location, CodeBlock block) {
        blocks.put(location, block);
        touch();
    }
    
    /**
     * Removes a block from this group
     */
    public CodeBlock removeBlock(Location location) {
        CodeBlock removed = blocks.remove(location);
        if (removed != null) {
            touch();
        }
        return removed;
    }
    
    /**
     * Gets the display info for this group
     */
    public String getDisplayInfo() {
        String status = collapsed ? "Collapsed" : "Expanded";
        return String.format("%s [%s] - %d blocks", name, status, getBlockCount());
    }
    
    public UUID getId() { return id; }
    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public Map<Location, CodeBlock> getBlocks() { return blocks; }
    public BlockGroupManager.GroupBounds getBounds() { return bounds; }
    public boolean isCollapsed() { return collapsed; }
    public long getCreatedTime() { return createdTime; }
    public long getLastModified() { return lastModified; }
}