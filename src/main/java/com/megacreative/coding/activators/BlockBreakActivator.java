package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.coding.events.GameEventFactory;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activator that handles block break events.
 * This activator listens to BlockBreakEvent and triggers script execution.
 */
public class BlockBreakActivator extends BukkitEventActivator {
    
    private Material blockType;
    private boolean anyBlockType = true;
    private final GameEventFactory eventFactory;
    
    public BlockBreakActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
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
        return "onBlockBreak";
    }
    
    @Override
    public String getDisplayName() {
        return "Block Break Event";
    }
    
    /**
     * Activates this activator for a block break event
     * @param player The player who broke the block
     * @param block The block that was broken
     * @param drops The items that dropped from breaking the block
     */
    public void activate(Player player, Block block, List<ItemStack> drops) {
        if (!enabled || script == null) {
            return;
        }
        
        // Check if we should only trigger for specific block types
        if (!anyBlockType && blockType != null && block.getType() != blockType) {
            return;
        }
        
        // Create a Bukkit event to extract data from
        BlockBreakEvent bukkitEvent = new BlockBreakEvent(block, player);
        
        // Create custom data for backward compatibility
        Map<String, Object> customData = new HashMap<>();
        customData.put("block", block);
        customData.put("blockType", block.getType());
        customData.put("drops", drops);
        
        // Create a game event with block break context using the factory
        GameEvent gameEvent = eventFactory.createGameEvent("onBlockBreak", bukkitEvent, player, customData);
        gameEvent.setLocation(block.getLocation());
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}