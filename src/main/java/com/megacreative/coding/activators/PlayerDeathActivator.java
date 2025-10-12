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
 * Activator that handles player death events.
 * This activator listens to PlayerDeathEvent and triggers script execution.
 */
public class PlayerDeathActivator extends BukkitEventActivator {
    
    private boolean enabled = true;
    private com.megacreative.coding.CodeBlock script;
    
    public PlayerDeathActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
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
        return ActivatorType.PLAYER_DEATH;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.BONE);
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
                    
                    
                    scriptEngine.executeBlock(action, player, "activator_player_death")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "PlayerDeath activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in PlayerDeath activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in PlayerDeath activator: " + e.getMessage()
                );
            }
        }
    }
    
    public String getEventName() {
        return "onPlayerDeath";
    }
    
    public String getDisplayName() {
        return "Player Death Event";
    }
    
    /**
     * Activates this activator for a player death event
     * @param player The player who died
     * @param deathMessage The death message
     */
    public void activate(Player player, String deathMessage) {
        if (!enabled || script == null) {
            return;
        }
        
        
        GameEvent gameEvent = new GameEvent("onPlayerDeath");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (player.getLocation() != null) {
            gameEvent.setLocation(player.getLocation());
        }
        
        
        Map<String, Object> customData = new HashMap<>();
        customData.put("deathMessage", deathMessage);
        gameEvent.setCustomData(customData);
        
        
        execute(gameEvent, new java.util.ArrayList<>(), 0, new java.util.concurrent.atomic.AtomicInteger(0));
    }
}