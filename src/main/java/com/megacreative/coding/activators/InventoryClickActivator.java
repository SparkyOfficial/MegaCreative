package com.megacreative.coding.activators;

import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;

/**
 * Activator that handles inventory click events.
 * This activator listens to InventoryClickEvent and triggers script execution.
 */
public class InventoryClickActivator extends BukkitEventActivator {
    
    public InventoryClickActivator(CreativeWorld creativeWorld, ScriptEngine scriptEngine) {
        super(creativeWorld, scriptEngine);
    }
    
    @Override
    public String getEventName() {
        return "onInventoryClick";
    }
    
    @Override
    public String getDisplayName() {
        return "Inventory Click Event";
    }
    
    /**
     * Activates this activator for an inventory click event
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param clickedItem The item that was clicked
     */
    public void activate(Player player, int slot, ItemStack clickedItem) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with inventory click context
        GameEvent gameEvent = new GameEvent("onInventoryClick");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("slot", slot);
        if (clickedItem != null) {
            customData.put("itemType", clickedItem.getType().name());
            customData.put("itemName", clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName() 
                ? clickedItem.getItemMeta().getDisplayName() : "");
        }
        gameEvent.setCustomData(customData);
        
        // Activate the script
        super.activate(gameEvent, player);
    }
}