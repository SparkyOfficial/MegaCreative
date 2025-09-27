package com.megacreative.coding.actions.world;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * World-related actions handler
 * Contains actions that affect the world or environment
 */
@BlockMeta(id = "worldActions", displayName = "§aWorld Actions", type = BlockType.ACTION)
public class WorldActions implements BlockAction {
    
    // Action handlers map for world actions
    private static final Map<String, BiConsumer<ExecutionContext, Map<String, DataValue>>> ACTION_HANDLERS = new HashMap<>();
    
    static {
        initializeActionHandlers();
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            String actionType = block.getAction();
            if (actionType == null) {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + "null");
            }
            
            // Check if player is required for this action
            if (requiresPlayer(actionType) && context.getPlayer() == null) {
                return ExecutionResult.error(Constants.PLAYER_REQUIRED_FOR_ACTION + actionType);
            }
            
            // Get the action handler
            BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(actionType);
            if (handler != null) {
                // Execute the handler
                handler.accept(context, block.getParameters());
                return ExecutionResult.success(Constants.ACTION_EXECUTED_SUCCESSFULLY + actionType);
            } else {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + actionType);
            }
        } catch (Exception e) {
            return ExecutionResult.error(Constants.FAILED_TO_EXECUTE_ACTION + e.getMessage());
        }
    }
    
    /**
     * Checks if a player is required for the specified action type
     */
    private boolean requiresPlayer(String actionType) {
        // Actions that require a player
        java.util.Set<String> playerRequiredActions = java.util.Set.of(
            "setBlock", "breakBlock", "setTime", "setWeather", "broadcast"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all world action handlers
     */
    private static void initializeActionHandlers() {
        // === WORLD ACTIONS ===
        ACTION_HANDLERS.put("setBlock", (context, params) -> {
            String locString = params.get("location").asString();
            Material material = Material.valueOf(params.get("material").asString());
            
            Location location = parseLocationString(locString);
            if (location != null) {
                location.getBlock().setType(material);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage(Constants.SET_BLOCK_SUCCESS + locString + " to " + material);
                }
            } else {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
                }
            }
        });
        
        ACTION_HANDLERS.put("breakBlock", (context, params) -> {
            String locString = params.get("location").asString();
            
            Location location = parseLocationString(locString);
            if (location != null) {
                location.getBlock().setType(Material.AIR);
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage(Constants.BROKE_BLOCK_SUCCESS + locString);
                }
            } else {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
                }
            }
        });
        
        ACTION_HANDLERS.put("setTime", (context, params) -> {
            long time = params.get("time").asNumber().longValue();
            context.getPlayer().getWorld().setTime(time);
        });
        
        ACTION_HANDLERS.put("setWeather", (context, params) -> {
            boolean storm = params.get("storm").asBoolean();
            context.getPlayer().getWorld().setStorm(storm);
        });
        
        ACTION_HANDLERS.put("broadcast", (context, params) -> {
            String message = params.get("message").asString();
            context.getPlayer().getServer().broadcastMessage(message);
        });
    }
    
    /**
     * Parse location string in format "x,y,z" or "x,y,z,world"
     * @param locString Location string to parse
     * @return Parsed Location object or null if parsing failed
     */
    public static Location parseLocationString(String locString) {
        String[] parts = locString.split(",");
        if (parts.length < 3) {
            return null;
        }
        
        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            double z = Double.parseDouble(parts[2]);
            
            Location location = new Location(null, x, y, z);
            if (parts.length > 3) {
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(parts[3]);
                if (world != null) {
                    location.setWorld(world);
                }
            }
            return location;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}