package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles entity damage events.
 * This activator listens to EntityDamageEvent and triggers script execution.
 */
public class EntityDamageActivator extends BukkitEventActivator {
    
    public EntityDamageActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    @Override
    public ActivatorType getType() {
        return ActivatorType.ENTITY_DAMAGE;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.DIAMOND_SWORD);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<org.bukkit.entity.Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        
        this.selectedEntities = selectedEntities;
        
        
        
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    
                    scriptEngine.executeBlock(action, 
                        selectedEntities.isEmpty() ? null : (org.bukkit.entity.Player) selectedEntities.get(0), 
                        "activator_entity_damage");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error executing action in EntityDamageActivator: " + e.getMessage());
                plugin.getLogger().log(java.util.logging.Level.SEVERE, "Error executing action in EntityDamageActivator", e);
            }
        }
    }
    
    /**
     * Activates this activator for an entity damage event
     * @param entity The entity that was damaged
     * @param damage The amount of damage
     * @param cause The cause of the damage
     */
    public void activate(Entity entity, double damage, String cause) {
        
        GameEvent event = new GameEvent("entity_damage");
        event.setEntity(entity);
        
        
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("damage", damage);
        eventData.put("cause", cause);
        event.setCustomData(eventData);
        
        
        execute(event, List.of(entity), 0, new AtomicInteger(0));
    }
}