package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from BlockBreakEvent
 */
public class BlockBreakEventDataExtractor extends AbstractEventDataExtractor<BlockBreakEvent> {
    
    public BlockBreakEventDataExtractor() {
        super(BlockBreakEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        // Player variables
        registerVariable("playerName", "Name of the player who broke the block");
        registerVariable("playerUUID", "UUID of the player who broke the block");
        registerVariable("playerDisplayName", "Display name of the player");
        registerVariable("playerHealth", "Health of the player");
        registerVariable("playerFoodLevel", "Food level of the player");
        registerVariable("playerGameMode", "Game mode of the player");
        registerVariable("playerLevel", "Level of the player");
        registerVariable("playerExp", "Experience of the player");
        
        // Block variables
        registerVariable("blockType", "Type/material of the broken block");
        registerVariable("blockX", "X coordinate of the broken block");
        registerVariable("blockY", "Y coordinate of the broken block");
        registerVariable("blockZ", "Z coordinate of the broken block");
        registerVariable("blockWorld", "World name where block was broken");
        registerVariable("blockLocation", "Complete block location as string");
        registerVariable("blockData", "Block data/state information");
        
        // Event specific variables
        registerVariable("expToDrop", "Experience points to drop from breaking block");
        registerVariable("isCancelled", "Whether the break event is cancelled");
    }
    
    @Override
    public Map<String, DataValue> extractData(BlockBreakEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        
        // Extract player data
        extractPlayerData(data, player);
        
        // Extract block location data
        extractLocationData(data, blockLocation, "block");
        
        // Block specific data
        data.put("blockType", DataValue.fromObject(block.getType().name()));
        data.put("blockData", DataValue.fromObject(block.getBlockData().getAsString()));
        
        // Event specific data
        data.put("expToDrop", DataValue.fromObject(event.getExpToDrop()));
        data.put("isCancelled", DataValue.fromObject(event.isCancelled()));
        
        return data;
    }
}