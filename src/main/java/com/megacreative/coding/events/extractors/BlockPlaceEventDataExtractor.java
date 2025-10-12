package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from BlockPlaceEvent
 */
public class BlockPlaceEventDataExtractor extends AbstractEventDataExtractor<BlockPlaceEvent> {
    
    public BlockPlaceEventDataExtractor() {
        super(BlockPlaceEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        
        registerVariable("playerName", "Name of the player who placed the block");
        registerVariable("playerUUID", "UUID of the player who placed the block");
        registerVariable("playerDisplayName", "Display name of the player");
        registerVariable("playerHealth", "Health of the player");
        registerVariable("playerFoodLevel", "Food level of the player");
        registerVariable("playerGameMode", "Game mode of the player");
        registerVariable("playerLevel", "Level of the player");
        registerVariable("playerExp", "Experience of the player");
        
        
        registerVariable("blockType", "Type/material of the placed block");
        registerVariable("blockX", "X coordinate of the placed block");
        registerVariable("blockY", "Y coordinate of the placed block");
        registerVariable("blockZ", "Z coordinate of the placed block");
        registerVariable("blockWorld", "World name where block was placed");
        registerVariable("blockLocation", "Complete block location as string");
        registerVariable("blockData", "Block data/state information");
        
        
        registerVariable("itemType", "Type/material of the item used to place the block");
        registerVariable("itemName", "Display name of the item used to place the block");
        registerVariable("itemAmount", "Amount of items in the stack used to place the block");
        
        
        registerVariable("isCancelled", "Whether the place event is cancelled");
        registerVariable("canBuild", "Whether the player can build at this location");
        registerVariable("hand", "Which hand was used to place the block");
    }
    
    @Override
    public Map<String, DataValue> extractData(BlockPlaceEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location blockLocation = block.getLocation();
        ItemStack itemInHand = event.getItemInHand();
        
        
        extractPlayerData(data, player);
        
        
        extractLocationData(data, blockLocation, "block");
        
        
        data.put("blockType", DataValue.fromObject(block.getType().name()));
        data.put("blockData", DataValue.fromObject(block.getBlockData().getAsString()));
        
        
        if (itemInHand != null) {
            data.put("itemType", DataValue.fromObject(itemInHand.getType().name()));
            if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
                data.put("itemName", DataValue.fromObject(itemInHand.getItemMeta().getDisplayName()));
            } else {
                data.put("itemName", DataValue.fromObject(""));
            }
            data.put("itemAmount", DataValue.fromObject(itemInHand.getAmount()));
        }
        
        
        data.put("isCancelled", DataValue.fromObject(event.isCancelled()));
        data.put("canBuild", DataValue.fromObject(event.canBuild()));
        data.put("hand", DataValue.fromObject(event.getHand().name()));
        
        return data;
    }
}