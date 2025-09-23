package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.Constants;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Universal action handler that can process 90% of all simple actions
 * Instead of creating separate classes for each action, we use a parameterized approach
 */
public class GenericAction implements BlockAction {
    
    // Action handlers map for easy extension
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
     * Helper method to add new action handlers dynamically
     * Can be used by other plugins or modules to extend functionality
     */
    public static void registerActionHandler(String actionId, BiConsumer<ExecutionContext, Map<String, DataValue>> handler) {
        ACTION_HANDLERS.put(actionId, handler);
    }
    
    /**
     * Check if action is supported
     */
    public static boolean isSupported(String actionId) {
        return ACTION_HANDLERS.containsKey(actionId);
    }
    
    /**
     * Parse location string in format "x,y,z" or "x,y,z,world"
     * @param locString Location string to parse
     * @param defaultLocation Default location to use if world is not specified
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
    
    /**
     * Checks if a player is required for the specified action type
     */
    private boolean requiresPlayer(String actionType) {
        // Actions that require a player
        Set<String> playerRequiredActions = new HashSet<>(Arrays.asList(
            "sendMessage", "sendTitle", "teleport", "giveItem", "removeItem",
            "setHealth", "setFood", "addPotionEffect", "removePotionEffect",
            "playSound", "kick", "heal", "kill", "clearInventory", "fly",
            "lightning", "explosion", "setBlock", "breakBlock", "setTime",
            "setWeather", "setGameMode", "giveMoney", "takeMoney",
            "givePermission", "removePermission"
        ));
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all action handlers
     * Add new actions here instead of creating new classes
     */
    private static void initializeActionHandlers() {
        initializePlayerActions();
        initializeWorldActions();
        initializeGameModeActions();
        initializeEconomyActions();
        initializePermissionActions();
        initializeListActions();
    }
    
    /**
     * Initialize player-related action handlers
     */
    private static void initializePlayerActions() {
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
     * Initialize world-related action handlers
     */
    private static void initializeWorldActions() {
        // === WORLD ACTIONS ===
        ACTION_HANDLERS.put("setBlock", (context, params) -> {
            String locString = params.get("location").asString();
            Material material = Material.valueOf(params.get("material").asString());
            
            Location location = parseLocationString(locString);
            if (location != null) {
                location.getBlock().setType(material);
                context.getPlayer().sendMessage(Constants.SET_BLOCK_SUCCESS + locString + " to " + material);
            } else {
                context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
            }
        });
        
        ACTION_HANDLERS.put("breakBlock", (context, params) -> {
            String locString = params.get("location").asString();
            
            Location location = parseLocationString(locString);
            if (location != null) {
                location.getBlock().setType(Material.AIR);
                context.getPlayer().sendMessage(Constants.BROKE_BLOCK_SUCCESS + locString);
            } else {
                context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
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
     * Initialize game mode action handlers
     */
    private static void initializeGameModeActions() {
        // === GAMEMODE ACTIONS ===
        ACTION_HANDLERS.put("setGameMode", (context, params) -> {
            String mode = params.get("gamemode").asString().toLowerCase();
            switch (mode) {
                case "survival":
                    context.getPlayer().setGameMode(org.bukkit.GameMode.SURVIVAL);
                    break;
                case "creative":
                    context.getPlayer().setGameMode(org.bukkit.GameMode.CREATIVE);
                    break;
                case "adventure":
                    context.getPlayer().setGameMode(org.bukkit.GameMode.ADVENTURE);
                    break;
                case "spectator":
                    context.getPlayer().setGameMode(org.bukkit.GameMode.SPECTATOR);
                    break;
                default:
                    context.getPlayer().sendMessage("§cInvalid gamemode: " + mode);
                    break;
            }
        });
    }
    
    /**
     * Initialize economy-related action handlers
     */
    private static void initializeEconomyActions() {
        // === ECONOMY ACTIONS (placeholder implementation) ===
        ACTION_HANDLERS.put("giveMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Placeholder implementation - in a full implementation, this would integrate with an economy plugin
            context.getPlayer().sendMessage("§a+$" + amount + " (Economy integration placeholder - not implemented)");
        });
        
        ACTION_HANDLERS.put("takeMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Placeholder implementation - in a full implementation, this would integrate with an economy plugin
            context.getPlayer().sendMessage("§c-$" + amount + " (Economy integration placeholder - not implemented)");
        });
    }
    
    /**
     * Initialize permission-related action handlers
     */
    private static void initializePermissionActions() {
        // === PERMISSION ACTIONS ===
        ACTION_HANDLERS.put("givePermission", (context, params) -> {
            String permission = params.get("permission").asString();
            // This would require a permissions plugin integration
            context.getPlayer().sendMessage("§aPermission granted: " + permission + " (Permissions plugin needed)");
        });
        
        ACTION_HANDLERS.put("removePermission", (context, params) -> {
            String permission = params.get("permission").asString();
            // This would require a permissions plugin integration
            context.getPlayer().sendMessage("§cPermission removed: " + permission + " (Permissions plugin needed)");
        });
    }
    
    /**
     * Initialize list-related action handlers
     */
    private static void initializeListActions() {
        // === LIST ACTIONS ===
        ACTION_HANDLERS.put("addItemToList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the list from player variables
            DataValue listValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), listName);
            if (listValue instanceof ListValue) {
                ListValue list = (ListValue) listValue;
                list.add(value);
                // Update the variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, list);
                context.getPlayer().sendMessage("§aAdded item to list " + listName);
            } else {
                context.getPlayer().sendMessage("§cVariable " + listName + " is not a list");
            }
        });
        
        ACTION_HANDLERS.put("removeItemFromList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the list from player variables
            DataValue listValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), listName);
            if (listValue instanceof ListValue) {
                ListValue list = (ListValue) listValue;
                list.remove(value);
                // Update the variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, list);
                context.getPlayer().sendMessage("§aRemoved item from list " + listName);
            } else {
                context.getPlayer().sendMessage("§cVariable " + listName + " is not a list");
            }
        });
        
        ACTION_HANDLERS.put("createList", (context, params) -> {
            String listName = params.get("name").asString();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Create a new empty list
            ListValue newList = new ListValue();
            
            // Store the list in player variables
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, newList);
            context.getPlayer().sendMessage("§aCreated new list " + listName);
        });
    }
}