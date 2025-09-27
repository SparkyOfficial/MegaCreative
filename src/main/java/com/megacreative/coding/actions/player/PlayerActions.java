package com.megacreative.coding.actions.player;

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
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * Player-related actions handler
 * Contains actions that directly affect players
 */
@BlockMeta(id = "playerActions", displayName = "§aPlayer Actions", type = BlockType.ACTION)
public class PlayerActions implements BlockAction {
    
    // Action handlers map for player actions
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
            "sendMessage", "sendTitle", "teleport", "giveItem", "removeItem",
            "setHealth", "setFood", "addPotionEffect", "removePotionEffect",
            "playSound", "kick", "heal", "kill", "clearInventory", "fly",
            "lightning", "explosion", "setBlock", "breakBlock", "setTime",
            "setWeather", "setGameMode", "giveMoney", "takeMoney",
            "givePermission", "removePermission"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all player action handlers
     */
    private static void initializeActionHandlers() {
        // === PLAYER ACTIONS ===
        ACTION_HANDLERS.put("sendMessage", (context, params) -> {
            String message = params.containsKey("message") ? params.get("message").asString() : "Hello World";
            context.getPlayer().sendMessage(message);
        });
        
        ACTION_HANDLERS.put("sendTitle", (context, params) -> {
            String title = params.containsKey("title") ? params.get("title").asString() : "";
            String subtitle = params.containsKey("subtitle") ? params.get("subtitle").asString() : "";
            context.getPlayer().sendTitle(title, subtitle, 10, 70, 20);
        });
        
        ACTION_HANDLERS.put("sendActionBar", (context, params) -> {
            String message = params.containsKey("message") ? params.get("message").asString() : "";
            context.getPlayer().sendActionBar(message);
        });
        
        ACTION_HANDLERS.put("teleport", (context, params) -> {
            // Use proper location handling instead of string parsing
            DataValue locationValue = params.get("location");
            Location location = null;
            
            if (locationValue != null) {
                // Try to get location directly if it's already a location
                if (locationValue.getType() == com.megacreative.coding.values.ValueType.LOCATION) {
                    // Cast to LocationValue and get the location
                    com.megacreative.coding.values.types.LocationValue locValue = 
                        (com.megacreative.coding.values.types.LocationValue) locationValue;
                    location = (Location) locValue.getValue();
                } else {
                    // Parse from string as fallback
                    String locString = locationValue.asString();
                    location = parseLocationString(locString);
                }
            }
            
            if (location != null) {
                context.getPlayer().teleport(location);
                context.getPlayer().sendMessage(Constants.TELEPORT_SUCCESS + location.getX() + ", " + location.getY() + ", " + location.getZ());
            } else {
                context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + (locationValue != null ? locationValue.asString() : "null"));
            }
        });
        
        ACTION_HANDLERS.put("giveItem", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            int amount = params.get("amount").asNumber().intValue();
            ItemStack item = new ItemStack(material, amount);
            context.getPlayer().getInventory().addItem(item);
        });
        
        ACTION_HANDLERS.put("removeItem", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            int amount = params.get("amount").asNumber().intValue();
            ItemStack item = new ItemStack(material, amount);
            context.getPlayer().getInventory().removeItem(item);
        });
        
        ACTION_HANDLERS.put("setHealth", (context, params) -> {
            double health = params.get("health").asNumber().doubleValue();
            context.getPlayer().setHealth(Math.max(0, Math.min(20, health)));
        });
        
        ACTION_HANDLERS.put("setFood", (context, params) -> {
            int food = params.get("food").asNumber().intValue();
            context.getPlayer().setFoodLevel(Math.max(0, Math.min(20, food)));
        });
        
        ACTION_HANDLERS.put("addPotionEffect", (context, params) -> {
            String effectName = params.get("effect").asString();
            int duration = params.get("duration").asNumber().intValue();
            int amplifier = params.get("amplifier").asNumber().intValue();
            
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType != null) {
                PotionEffect effect = new PotionEffect(effectType, duration * 20, amplifier);
                context.getPlayer().addPotionEffect(effect);
            }
        });
        
        ACTION_HANDLERS.put("removePotionEffect", (context, params) -> {
            String effectName = params.get("effect").asString();
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType != null) {
                context.getPlayer().removePotionEffect(effectType);
            }
        });
        
        ACTION_HANDLERS.put("playSound", (context, params) -> {
            String sound = params.get("sound").asString();
            float volume = params.get("volume").asNumber().floatValue();
            float pitch = params.get("pitch").asNumber().floatValue();
            context.getPlayer().playSound(context.getPlayer().getLocation(), sound, volume, pitch);
        });
        
        // === NEW ESSENTIAL ACTIONS ===
        ACTION_HANDLERS.put("kick", (context, params) -> {
            String reason = params.get("reason").asString();
            context.getPlayer().kickPlayer(reason);
        });
        
        ACTION_HANDLERS.put("sendTitle", (context, params) -> {
            String title = params.get("title").asString();
            String subtitle = params.get("subtitle").asString();
            int fadeIn = params.containsKey("fadeIn") ? params.get("fadeIn").asNumber().intValue() : 10;
            int stay = params.containsKey("stay") ? params.get("stay").asNumber().intValue() : 70;
            int fadeOut = params.containsKey("fadeOut") ? params.get("fadeOut").asNumber().intValue() : 20;
            context.getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        });
        
        ACTION_HANDLERS.put("heal", (context, params) -> {
            context.getPlayer().setHealth(20.0);
            context.getPlayer().setFoodLevel(20);
            context.getPlayer().setSaturation(20.0f);
        });
        
        ACTION_HANDLERS.put("kill", (context, params) -> {
            context.getPlayer().setHealth(0.0);
        });
        
        ACTION_HANDLERS.put("clearInventory", (context, params) -> {
            context.getPlayer().getInventory().clear();
        });
        
        ACTION_HANDLERS.put("fly", (context, params) -> {
            boolean canFly = params.get("enabled").asBoolean();
            context.getPlayer().setAllowFlight(canFly);
            context.getPlayer().setFlying(canFly);
        });
        
        ACTION_HANDLERS.put("lightning", (context, params) -> {
            context.getPlayer().getLocation().getWorld().strikeLightning(context.getPlayer().getLocation());
        });
        
        ACTION_HANDLERS.put("explosion", (context, params) -> {
            float power = params.containsKey("power") ? params.get("power").asNumber().floatValue() : 2.0f;
            boolean breakBlocks = params.containsKey("breakBlocks") && params.get("breakBlocks").asBoolean();
            context.getPlayer().getLocation().getWorld().createExplosion(context.getPlayer().getLocation(), power, false, breakBlocks);
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