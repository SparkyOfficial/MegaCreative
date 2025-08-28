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
        // Player variables
        registerVariable("playerName", "Name of the interacting player");
        registerVariable("playerUUID", "UUID of the interacting player");
        registerVariable("playerDisplayName", "Display name of the interacting player");
        registerVariable("playerHealth", "Health of the interacting player");
        registerVariable("playerFoodLevel", "Food level of the interacting player");
        registerVariable("playerGameMode", "Game mode of the interacting player");
        registerVariable("playerLevel", "Level of the interacting player");
        registerVariable("playerExp", "Experience of the interacting player");
        
        // Interaction variables
        registerVariable("action", "Type of interaction (LEFT_CLICK_BLOCK, RIGHT_CLICK_AIR, etc.)");
        registerVariable("hand", "Which hand was used (MAIN_HAND or OFF_HAND)");
        registerVariable("isCancelled", "Whether the interaction is cancelled");
        
        // Block variables (if interacting with a block)
        registerVariable("hasClickedBlock", "Whether a block was clicked");
        registerVariable("blockType", "Type/material of the clicked block");
        registerVariable("blockX", "X coordinate of the clicked block");
        registerVariable("blockY", "Y coordinate of the clicked block");
        registerVariable("blockZ", "Z coordinate of the clicked block");
        registerVariable("blockWorld", "World name where block was clicked");
        registerVariable("blockLocation", "Complete block location as string");
        registerVariable("blockFace", "Face of the block that was clicked");
        
        // Item variables (item in hand)
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
        
        // Extract player data
        extractPlayerData(data, player);
        
        // Interaction data
        data.put("action", DataValue.fromObject(event.getAction().name()));
        data.put("hand", DataValue.fromObject(event.getHand() != null ? event.getHand().name() : "UNKNOWN"));
        data.put("isCancelled", DataValue.fromObject(event.isCancelled()));
        
        // Block data (if present)
        Block clickedBlock = event.getClickedBlock();
        data.put("hasClickedBlock", DataValue.fromObject(clickedBlock != null));
        if (clickedBlock != null) {
            extractLocationData(data, clickedBlock.getLocation(), "block");
            data.put("blockType", DataValue.fromObject(clickedBlock.getType().name()));
            data.put("blockFace", DataValue.fromObject(event.getBlockFace().name()));
        } else {
            // Set default values for when no block is clicked
            data.put("blockType", DataValue.fromObject("AIR"));
            data.put("blockX", DataValue.fromObject(0));
            data.put("blockY", DataValue.fromObject(0));
            data.put("blockZ", DataValue.fromObject(0));
            data.put("blockWorld", DataValue.fromObject(""));
            data.put("blockLocation", DataValue.fromObject(""));
            data.put("blockFace", DataValue.fromObject(""));
        }
        
        // Item data
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
                item.hasItemMeta() && item.getItemMeta().hasLore() ? 
                String.join("\\n", item.getItemMeta().getLore()) : 
                ""));
        } else {
            // Set default values for when no item is held
            data.put("itemType", DataValue.fromObject("AIR"));
            data.put("itemAmount", DataValue.fromObject(0));
            data.put("itemName", DataValue.fromObject(""));
            data.put("itemLore", DataValue.fromObject(""));
        }
        
        return data;
    }
}