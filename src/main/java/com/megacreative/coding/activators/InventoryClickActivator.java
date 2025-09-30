package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles inventory click events.
 * This activator listens to InventoryClickEvent and triggers script execution.
 */
public class InventoryClickActivator extends BukkitEventActivator {
    
    public InventoryClickActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.INVENTORY_CLICK;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.CHEST);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<org.bukkit.entity.Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        // Set the selected entities
        this.selectedEntities = selectedEntities;
        
        // Execute all actions associated with this activator
        // This would integrate with the existing script execution system
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                // Get the script engine from the plugin
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    // Execute the action block
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_inventory_click");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing action in InventoryClickActivator: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Activates this activator for an inventory click event
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param clickedItem The item that was clicked
     */
    public void activate(Player player, int slot, ItemStack clickedItem) {
        // Create a game event for this activation
        GameEvent event = new GameEvent("inventory_click");
        event.setPlayer(player);
        
        // Add click information to the event
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("slot", slot);
        eventData.put("clicked_item", clickedItem != null ? clickedItem.getType().name() : "AIR");
        event.setCustomData(eventData);
        
        // Execute the activator with the event
        execute(event, List.of(player), 0, new AtomicInteger(0));
    }
}