package com.megacreative.coding.events.extractors;

import com.megacreative.coding.events.AbstractEventDataExtractor;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts data from PlayerInteractEvent
 */
public class PlayerInteractEventDataExtractor extends AbstractEventDataExtractor<PlayerInteractEvent> {
    
    public PlayerInteractEventDataExtractor() {
        super(PlayerInteractEvent.class);
    }
    
    @Override
    protected void initializeVariables() {
        
        registerVariable("playerName", "Name of the interacting player");
        registerVariable("playerUUID", "UUID of the interacting player");
        registerVariable("playerDisplayName", "Display name of the interacting player");
        registerVariable("playerHealth", "Health of the interacting player");
        registerVariable("playerFoodLevel", "Food level of the interacting player");
        registerVariable("playerGameMode", "Game mode of the interacting player");
        registerVariable("playerLevel", "Level of the interacting player");
        registerVariable("playerExp", "Experience of the interacting player");
        
        
        registerVariable("action", "Type of interaction (LEFT_CLICK_BLOCK, RIGHT_CLICK_AIR, etc.)");
        registerVariable("hand", "Which hand was used (MAIN_HAND or OFF_HAND)");
        registerVariable("isCancelled", "Whether the interaction is cancelled");
        
        
        registerVariable("hasClickedBlock", "Whether a block was clicked");
        registerVariable("blockType", "Type/material of the clicked block");
        registerVariable("blockX", "X coordinate of the clicked block");
        registerVariable("blockY", "Y coordinate of the clicked block");
        registerVariable("blockZ", "Z coordinate of the clicked block");
        registerVariable("blockWorld", "World name where block was clicked");
        registerVariable("blockLocation", "Complete block location as string");
        registerVariable("blockFace", "Face of the block that was clicked");
        
        
        registerVariable("hasItem", "Whether player has an item in hand");
        registerVariable("itemType", "Type/material of the item in hand");
        registerVariable("itemAmount", "Amount of the item in hand");
        registerVariable("itemName", "Display name of the item in hand");
        registerVariable("itemLore", "Lore/description of the item in hand");
    }
    
    @Override
    public Map<String, DataValue> extractData(PlayerInteractEvent event) {
        Map<String, DataValue> data = new HashMap<>();
        
        Player player = event.getPlayer();
        
        
        extractPlayerData(data, player);
        
        
        data.put("action", DataValue.fromObject(event.getAction().name()));
        data.put("hand", DataValue.fromObject(event.getHand() != null ? event.getHand().name() : "UNKNOWN"));
        data.put("isCancelled", DataValue.fromObject(event.isCancelled()));
        
        
        Block clickedBlock = event.getClickedBlock();
        data.put("hasClickedBlock", DataValue.fromObject(clickedBlock != null));
        if (clickedBlock != null) {
            extractLocationData(data, clickedBlock.getLocation(), "block");
            data.put("blockType", DataValue.fromObject(clickedBlock.getType().name()));
            data.put("blockFace", DataValue.fromObject(event.getBlockFace().name()));
        } else {
            
            data.put("blockType", DataValue.fromObject("AIR"));
            data.put("blockX", DataValue.fromObject(0));
            data.put("blockY", DataValue.fromObject(0));
            data.put("blockZ", DataValue.fromObject(0));
            data.put("blockWorld", DataValue.fromObject(""));
            data.put("blockLocation", DataValue.fromObject(""));
            data.put("blockFace", DataValue.fromObject(""));
        }
        
        
        ItemStack item = event.getItem();
        data.put("hasItem", DataValue.fromObject(item != null));
        if (item != null) {
            data.put("itemType", DataValue.fromObject(item.getType().name()));
            data.put("itemAmount", DataValue.fromObject(item.getAmount()));
            data.put("itemName", DataValue.fromObject(
                item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? 
                item.getItemMeta().getDisplayName() : 
                item.getType().name()));
            data.put("itemLore", DataValue.fromObject(
                item.hasItemMeta() ? 
                // According to static analysis, item.getItemMeta().getLore() might be null
                // We need to check for null before using it
                (item.getItemMeta().hasLore() ? 
                    (item.getItemMeta().getLore() != null ? String.join("\\n", item.getItemMeta().getLore()) : "") 
                    : "") :
                ""));
        } else {
            
            data.put("itemType", DataValue.fromObject("AIR"));
            data.put("itemAmount", DataValue.fromObject(0));
            data.put("itemName", DataValue.fromObject(""));
            data.put("itemLore", DataValue.fromObject(""));
        }
        
        return data;
    }
}