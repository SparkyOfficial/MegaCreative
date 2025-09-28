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
 * Activator that handles player respawn events.
 * This activator listens to PlayerRespawnEvent and triggers script execution.
 */
public class PlayerRespawnActivator extends BukkitEventActivator {
    
    private boolean enabled = true;
    private com.megacreative.coding.CodeBlock script;
    
    public PlayerRespawnActivator(MegaCreative plugin, CreativeWorld world) {
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
        return ActivatorType.PLAYER_RESPAWN;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(org.bukkit.Material.TOTEM_OF_UNDYING);
    }
    
    @Override
    public void execute(GameEvent gameEvent, List<Entity> selectedEntities, int stackCounter, AtomicInteger callCounter) {
        if (!enabled || script == null) {
            return;
        }
        
        // Execute all actions associated with this activator
        for (com.megacreative.coding.CodeBlock action : actionList) {
            try {
                // Get the script engine from the plugin
                com.megacreative.coding.ScriptEngine scriptEngine = plugin.getServiceRegistry().getScriptEngine();
                
                if (scriptEngine != null) {
                    // Convert the first entity to a player if possible
                    Player player = null;
                    if (!selectedEntities.isEmpty() && selectedEntities.get(0) instanceof Player) {
                        player = (Player) selectedEntities.get(0);
                    }
                    
                    // Execute the action block
                    scriptEngine.executeBlock(action, player, "activator_player_respawn")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "PlayerRespawn activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in PlayerRespawn activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in PlayerRespawn activator: " + e.getMessage()
                );
            }
        }
    }
    
    public String getEventName() {
        return "onPlayerRespawn";
    }
    
    public String getDisplayName() {
        return "Player Respawn Event";
    }
    
    /**
     * Activates this activator for a player respawn event
     * @param player The player who respawned
     * @param respawnLocation The location where the player respawned
     * @param isBedSpawn Whether the respawn was at a bed
     */
    public void activate(Player player, Location respawnLocation, boolean isBedSpawn) {
        if (!enabled || script == null) {
            return;
        }
        
        // Create a game event with player respawn context
        GameEvent gameEvent = new GameEvent("onPlayerRespawn");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (respawnLocation != null) {
            gameEvent.setLocation(respawnLocation);
        }
        
        // Add custom data
        Map<String, Object> customData = new HashMap<>();
        customData.put("respawnLocation", respawnLocation);
        customData.put("isBedSpawn", isBedSpawn);
        gameEvent.setCustomData(customData);
        
        // Activate the script
        execute(gameEvent, new java.util.ArrayList<>(), 0, new java.util.concurrent.atomic.AtomicInteger(0));
    }
}