package com.megacreative.coding.activators;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Activator that handles block place events.
 * This activator listens to BlockPlaceEvent and triggers script execution.
 */
public class BlockPlaceActivator extends BukkitEventActivator {
    
    private Material blockType;
    private boolean anyBlockType = true;
    private boolean enabled = true;
    private com.megacreative.coding.CodeBlock script;
    
    public BlockPlaceActivator(MegaCreative plugin, CreativeWorld world) {
        super(plugin, world);
    }
    
    /**
     * Sets the block type this activator should listen for
     * @param blockType The block type to listen for
     */
    public void setBlockType(Material blockType) {
        this.blockType = blockType;
        this.anyBlockType = false;
    }
    
    /**
     * Sets whether this activator should listen for any block type
     * @param anyBlockType true to listen for any block type, false to listen for specific type
     */
    public void setAnyBlockType(boolean anyBlockType) {
        this.anyBlockType = anyBlockType;
    }
    
    /**
     * Gets the block type this activator listens for
     * @return The block type
     */
    public Material getBlockType() {
        return blockType;
    }
    
    /**
     * Checks if this activator listens for any block type
     * @return true if listening for any block type, false otherwise
     */
    public boolean isAnyBlockType() {
        return anyBlockType;
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
        return ActivatorType.BLOCK_PLACE;
    }
    
    @Override
    public ItemStack getIcon() {
        return new ItemStack(Material.STONE);
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
                    
                    
                    scriptEngine.executeBlock(action, player, "activator_block_place")
                        .thenAccept(result -> {
                            if (!result.isSuccess()) {
                                plugin.getLogger().warning(
                                    "BlockPlace activator execution failed: " + result.getMessage()
                                );
                            }
                        })
                        .exceptionally(throwable -> {
                            plugin.getLogger().warning(
                                "Error in BlockPlace activator execution: " + throwable.getMessage()
                            );
                            return null;
                        });
                }
            } catch (Exception e) {
                plugin.getLogger().warning(
                    "Error executing action in BlockPlace activator: " + e.getMessage()
                );
            }
        }
    }
    
    /**
     * Activates this activator for a block place event
     * @param player The player who placed the block
     * @param block The block that was placed
     * @param itemInHand The item the player had in hand
     */
    public void activate(Player player, Block block, ItemStack itemInHand) {
        if (!enabled || script == null) {
            return;
        }
        
        
        if (!anyBlockType && blockType != null && block.getType() != blockType) {
            return;
        }
        
        
        GameEvent gameEvent = new GameEvent("onBlockPlace");
        gameEvent.setPlayer(player);
        if (location != null) {
            gameEvent.setLocation(location);
        } else if (block.getLocation() != null) {
            gameEvent.setLocation(block.getLocation());
        }
        
        
        Map<String, Object> customData = new HashMap<>();
        customData.put("block", block);
        customData.put("blockType", block.getType());
        customData.put("itemInHand", itemInHand);
        gameEvent.setCustomData(customData);
        
        
        execute(gameEvent, new java.util.ArrayList<>(), 0, new java.util.concurrent.atomic.AtomicInteger(0));
    }
}