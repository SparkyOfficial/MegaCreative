package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles entity pickup item events.
 * This activator listens to EntityPickupItemEvent and triggers script execution.
 */
public class EntityPickupItemActivator extends Activator {
    
    private Location location;
    private boolean enabled = true;
    private com.megacreative.coding.CodeBlock script;
    
    public EntityPickupItemActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    /**
     * Sets the location of this activator in the world
     * @param location The location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public com.megacreative.coding.CodeBlock getScript() {
        return script;
    }
    
    public void setScript(com.megacreative.coding.CodeBlock script) {
        this.script = script;
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.ENTITY_PICKUP_ITEM;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.HOPPER);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        if (!enabled || script == null) {
            return;
        }
        
        
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    
                    Player player = null;
                    if (!selectedEntities.isEmpty() && selectedEntities.get(0) instanceof Player) {
                        player = (Player) selectedEntities.get(0);
                    }
                    
                    
                    scriptEngine.executeBlock(action, player, "activator_entity_pickup_item")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "EntityPickupItem activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in EntityPickupItem activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in EntityPickupItem activator: " + e.getMessage()
                );
            }
        }
    }
    
    /**
     * Activates this activator for an entity pickup item event
     * @param player The player who picked up the item
     * @param item The item that was picked up
     * @param quantity The quantity of items picked up
     */
    public void activate(Player player, ItemStack item, int quantity) {
        if (!enabled || script == null) {
            return;
        }
        
        
        GameEvent gameEvent = new GameEvent("onEntityPickupItem");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        }
        
        
        Map<String, Object> customData = new HashMap<>();
        if (item != null) {
            customData.put("itemType", item.getType().name());
            customData.put("itemName", item.hasItemMeta() && item.getItemMeta().hasDisplayName() 
                ? item.getItemMeta().getDisplayName() : "");
        }
        customData.put("quantity", quantity);
        gameEvent.setCustomData(customData);
        
        
        execute(gameEvent, new java.util.ArrayList<>(), 0, new java.util.concurrent.atomic.AtomicInteger(0));
    }
}