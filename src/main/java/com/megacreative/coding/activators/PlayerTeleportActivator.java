package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles player teleport events.
 * This activator listens to PlayerTeleportEvent and triggers script execution.
 */
public class PlayerTeleportActivator extends BukkitEventActivator {
    
    public PlayerTeleportActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.PLAYER_TELEPORT;
    }
    
    @Override
    public org.bukkit.inventory.ItemStack getIcon() {
        return new org.bukkit.inventory.ItemStack(org.bukkit.Material.ENDER_PEARL);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        
        this.selectedEntities = selectedEntities;
        
        
        
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_player_teleport");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing action in PlayerTeleportActivator: " + e.getMessage());
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error executing action in PlayerTeleportActivator", e);
            }
        }
    }
    
    /**
     * Activates this activator for a player teleport event
     * @param player The player who teleported
     * @param from The location the player teleported from
     * @param to The location the player teleported to
     * @param cause The cause of the teleportation
     */
    public void activate(Player player, Location from, Location to, String cause) {
        
        GameEvent event = new GameEvent("player_teleport");
        event.setPlayer(player);
        event.setLocation(to);
        
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("from_x", from.getX());
        eventData.put("from_y", from.getY());
        eventData.put("from_z", from.getZ());
        eventData.put("from_world", from.getWorld().getName());
        eventData.put("to_x", to.getX());
        eventData.put("to_y", to.getY());
        eventData.put("to_z", to.getZ());
        eventData.put("to_world", to.getWorld().getName());
        eventData.put("cause", cause);
        event.setCustomData(eventData);
        
        
        execute(event, List.of(player), 0, new AtomicInteger(0));
    }
}