package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles block place events.
 * This activator listens to BlockPlaceEvent and triggers script execution.
 */
public class BlockPlaceActivator extends BukkitEventActivator {
    
    private Material blockType;
    private boolean anyBlockType = true;
    private final GameEventFactory eventFactory;
    
    public BlockPlaceActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
        this.eventFactory = new GameEventFactory();
    }
    
    /**
     * Sets the block type this activator should listen for
     * @param blockType The block type to listen for
     */
    public void setBlockType(Material blockType) {
        this.blockType = blockType;
        this.anyBlockType = false;
    }
    
    /**
     * Sets whether this activator should listen for any block type
     * @param anyBlockType true to listen for any block type, false to listen for specific type
     */
    public void setAnyBlockType(boolean anyBlockType) {
        this.anyBlockType = anyBlockType;
    }
    
    /**
     * Gets the block type this activator listens for
     * @return The block type
     */
    public Material getBlockType() {
        return blockType;
    }
    
    /**
     * Checks if this activator listens for any block type
     * @return true if listening for any block type, false otherwise
     */
    public boolean isAnyBlockType() {
        return anyBlockType;
    }
    
    @Override
    public String getEventName() {
        return "onBlockPlace";
    }
    
    @Override
    public String getDisplayName() {
        return "Block Place Event";
    }
    
    /**
     * Activates this activator for a block place event
     * @param player The player who placed the block
     * @param block The block that was placed
     * @param itemInHand The item the player had in hand
     */
    public void activate(Player player, Block block, ItemStack itemInHand) {
        if (!enabled || script == null) {
            return;
        }
        
        // Check if we should only trigger for specific block types
        if (!anyBlockType && blockType != null && block.getType() != blockType) {
            return;
        }
        
        // Create a Bukkit event to extract data from
        BlockPlaceEvent bukkitEvent = new BlockPlaceEvent(block, block.getState(), block, itemInHand, player, true, null);
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("block", block);
        customData.put("blockType", block.getType());
        customData.put("itemInHand", itemInHand);
        
        // Create a game event with block place context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onBlockPlace", bukkitEvent, player, customData);
        gameEvent.setLocation(block.getLocation());
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}