package com.megacreative.coding.actions.base;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.Constants;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Universal action handler that can process 90% of all simple actions
 * Instead of creating separate classes for each action, we use a parameterized approach
 * Enhanced with support for complex operations, async execution, and advanced variable handling.
 */
@BlockMeta(id = "generic", displayName = "§aGeneric Action", type = BlockType.ACTION)
public class GenericAction implements BlockAction {
    
    // Action handlers map for easy extension
    private static final Map<String, BiConsumer<ExecutionContext, Map<String, DataValue>>> ACTION_HANDLERS = new HashMap<>();
    
    /**
     * Parse a location string in the format "world:x,y,z" or "x,y,z" (uses player's world)
     * @param locString The location string to parse
     * @param context The execution context
     * @return The parsed Location or null if invalid
     */
    private static Location parseLocationString(String locString, ExecutionContext context) {
        if (locString == null || locString.isEmpty()) {
            return null;
        }
        
        try {
            // Split by colon to separate world and coordinates
            String[] parts = locString.split(":");
            
            if (parts.length == 1) {
                // Format: x,y,z (use player's world)
                String[] coords = parts[0].split(",");
                if (coords.length != 3) {
                    return null;
                }
                
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                double z = Double.parseDouble(coords[2].trim());
                
                return new Location(context.getPlayer().getWorld(), x, y, z);
            } else if (parts.length == 2) {
                // Format: world:x,y,z
                String worldName = parts[0].trim();
                String[] coords = parts[1].split(",");
                if (coords.length != 3) {
                    return null;
                }
                
                double x = Double.parseDouble(coords[0].trim());
                double y = Double.parseDouble(coords[1].trim());
                double z = Double.parseDouble(coords[2].trim());
                
                org.bukkit.World world = context.getPlayer().getServer().getWorld(worldName);
                if (world == null) {
                    return null;
                }
                
                return new Location(world, x, y, z);
            }
        } catch (NumberFormatException e) {
            // Invalid number format
            return null;
        } catch (Exception e) {
            // Any other parsing error
            return null;
        }
        
        return null;
    }
    
    static {
        initializeActionHandlers();
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            String actionId = block.getAction();
            Map<String, DataValue> params = block.getParameters();
            
            // Get the action handler
            BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(actionId);
            if (handler != null) {
                handler.accept(context, params);
                return ExecutionResult.success();
            } else {
                String errorMsg = Constants.UNKNOWN_ACTION_TYPE + actionId;
                context.getPlugin().getLogger().warning(errorMsg);
                return ExecutionResult.error(errorMsg);
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error executing generic action " + block.getAction() + ": " + e.getMessage());
            context.getPlugin().getLogger().severe("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            return ExecutionResult.error("Action execution error: " + e.getMessage());
        }
    }
    
    /**
     * Initialize all action handlers
     * Add new actions here instead of creating new classes
     * Enhanced with more complex operations and better error handling.
     */
    private static void initializeActionHandlers() {
        // === PLAYER ACTIONS ===
        ACTION_HANDLERS.put("sendMessage", (context, params) -> {
            String message = params.containsKey("message") ? params.get("message").asString() : Constants.DEFAULT_MESSAGE;
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage(message);
            }
        });
        
        ACTION_HANDLERS.put("sendTitle", (context, params) -> {
            String title = params.containsKey("title") ? params.get("title").asString() : Constants.EMPTY_STRING;
            String subtitle = params.containsKey("subtitle") ? params.get("subtitle").asString() : Constants.EMPTY_STRING;
            if (context.getPlayer() != null) {
                context.getPlayer().sendTitle(title, subtitle, 10, 70, 20);
            }
        });
        
        ACTION_HANDLERS.put("sendActionBar", (context, params) -> {
            String message = params.containsKey("message") ? params.get("message").asString() : Constants.DEFAULT_MESSAGE;
            if (context.getPlayer() != null) {
                context.getPlayer().sendActionBar(message);
            }
        });
        
        ACTION_HANDLERS.put("teleport", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            String locString = params.get("location").asString();
            Location location = parseLocationString(locString, context);
            if (location != null) {
                // Add teleport effects
                context.getPlayer().getWorld().spawnParticle(org.bukkit.Particle.PORTAL, 
                    context.getPlayer().getLocation(), 50, 0.5, 1.0, 0.5, 0.1);
                context.getPlayer().playSound(context.getPlayer().getLocation(), 
                    org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                
                context.getPlayer().teleport(location);
                
                // Add arrival effects
                context.getPlayer().getWorld().spawnParticle(org.bukkit.Particle.PORTAL, 
                    location, 50, 0.5, 1.0, 0.5, 0.1);
                context.getPlayer().playSound(location, 
                    org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                
                context.getPlayer().sendMessage(Constants.TELEPORT_EFFECTS + locString);
            } else {
                context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
            }
        });
        
        ACTION_HANDLERS.put("giveItem", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                Material material = Material.valueOf(params.get("material").asString());
                int amount = params.get("amount").asNumber().intValue();
                ItemStack item = new ItemStack(material, amount);
                context.getPlayer().getInventory().addItem(item);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_GIVING_ITEM + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("removeItem", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                Material material = Material.valueOf(params.get("material").asString());
                int amount = params.get("amount").asNumber().intValue();
                ItemStack item = new ItemStack(material, amount);
                context.getPlayer().getInventory().removeItem(item);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_REMOVING_ITEM + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("setHealth", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                double health = params.get("health").asNumber().doubleValue();
                context.getPlayer().setHealth(Math.max(0, Math.min(20, health)));
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_SETTING_HEALTH + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("setFood", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                int food = params.get("food").asNumber().intValue();
                context.getPlayer().setFoodLevel(Math.max(0, Math.min(20, food)));
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_SETTING_FOOD + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("addPotionEffect", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String effectName = params.get("effect").asString();
                int duration = params.get("duration").asNumber().intValue();
                int amplifier = params.get("amplifier").asNumber().intValue();
                
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    PotionEffect effect = new PotionEffect(effectType, duration * 20, amplifier);
                    context.getPlayer().addPotionEffect(effect);
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_ADDING_POTION + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("removePotionEffect", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String effectName = params.get("effect").asString();
                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    context.getPlayer().removePotionEffect(effectType);
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_REMOVING_POTION + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("playSound", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String sound = params.get("sound").asString();
                float volume = params.get("volume").asNumber().floatValue();
                float pitch = params.get("pitch").asNumber().floatValue();
                context.getPlayer().playSound(context.getPlayer().getLocation(), sound, volume, pitch);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_PLAYING_SOUND + e.getMessage());
            }
        });
        
        // === NEW ESSENTIAL ACTIONS ===
        ACTION_HANDLERS.put("kick", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            String reason = params.get("reason").asString();
            context.getPlayer().kickPlayer(reason);
        });
        
        ACTION_HANDLERS.put("heal", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            context.getPlayer().setHealth(20.0);
            context.getPlayer().setFoodLevel(20);
            context.getPlayer().setSaturation(20.0f);
        });
        
        ACTION_HANDLERS.put("kill", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            context.getPlayer().setHealth(0.0);
        });
        
        ACTION_HANDLERS.put("clearInventory", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            context.getPlayer().getInventory().clear();
        });
        
        ACTION_HANDLERS.put("fly", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                boolean canFly = params.get("enabled").asBoolean();
                context.getPlayer().setAllowFlight(canFly);
                context.getPlayer().setFlying(canFly);
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError setting fly mode: " + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("lightning", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            context.getPlayer().getLocation().getWorld().strikeLightning(context.getPlayer().getLocation());
        });
        
        ACTION_HANDLERS.put("explosion", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                float power = params.containsKey("power") ? params.get("power").asNumber().floatValue() : 2.0f;
                boolean breakBlocks = params.containsKey("breakBlocks") && params.get("breakBlocks").asBoolean();
                context.getPlayer().getLocation().getWorld().createExplosion(context.getPlayer().getLocation(), power, false, breakBlocks);
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError creating explosion: " + e.getMessage());
            }
        });
        
        // === WORLD ACTIONS ===
        ACTION_HANDLERS.put("setBlock", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String locString = params.get("location").asString();
                Material material = Material.valueOf(params.get("material").asString());
                
                Location location = parseLocationString(locString, context);
                if (location != null) {
                    location.getBlock().setType(material);
                    context.getPlayer().sendMessage(Constants.TELEPORT_EFFECTS + locString);
                } else {
                    context.getPlayer().sendMessage(Constants.INVALID_LOCATION_FORMAT + locString);
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_SETTING_BLOCK + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("breakBlock", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String locString = params.get("location").asString();
                
                Location location = parseLocationString(locString, context);
                if (location != null) {
                    location.getBlock().setType(Material.AIR);
                    context.getPlayer().sendMessage("§aBlock broken");
                } else {
                    context.getPlayer().sendMessage("§cInvalid location: " + locString);
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_BREAKING_BLOCK + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("setTime", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                long time = params.get("time").asNumber().longValue();
                context.getPlayer().getWorld().setTime(time);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_SETTING_TIME + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("setWeather", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                boolean storm = params.get("storm").asBoolean();
                context.getPlayer().getWorld().setStorm(storm);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_SETTING_WEATHER + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("broadcast", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            String message = params.get("message").asString();
            context.getPlayer().getServer().broadcastMessage(message);
        });
        
        // === GAMEMODE ACTIONS ===
        ACTION_HANDLERS.put("setGameMode", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
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
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError setting gamemode: " + e.getMessage());
            }
        });
        
        // === ECONOMY ACTIONS (if Vault is available) ===
        ACTION_HANDLERS.put("giveMoney", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            double amount = params.get("amount").asNumber().doubleValue();
            // Implementation would depend on economy plugin integration
            context.getPlayer().sendMessage("§a+$" + amount + " (Economy integration needed)");
        });
        
        ACTION_HANDLERS.put("takeMoney", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            double amount = params.get("amount").asNumber().doubleValue();
            // Implementation would depend on economy plugin integration
            context.getPlayer().sendMessage("§c-$" + amount + " (Economy integration needed)");
        });
        
        // === PERMISSION ACTIONS ===
        ACTION_HANDLERS.put("givePermission", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            String permission = params.get("permission").asString();
            // This would require a permissions plugin integration
            context.getPlayer().sendMessage("§aPermission granted: " + permission + " (Permissions plugin needed)");
        });
        
        ACTION_HANDLERS.put("removePermission", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            String permission = params.get("permission").asString();
            // This would require a permissions plugin integration
            context.getPlayer().sendMessage("§cPermission removed: " + permission + " (Permissions plugin needed)");
        });
        
        // === ADVANCED VARIABLE OPERATIONS ===
        ACTION_HANDLERS.put("incrementVariable", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String varName = params.get("variable").asString();
                double increment = params.get("amount").asNumber().doubleValue();
                
                VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
                UUID playerId = context.getPlayer().getUniqueId();
                
                // Get current value
                DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
                double currentNumber = 0.0;
                
                if (currentValue != null && currentValue.getType().isNumber()) {
                    currentNumber = currentValue.asNumber().doubleValue();
                }
                
                // Set new value
                double newValue = currentNumber + increment;
                variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
                
                context.getPlayer().sendMessage("§aVariable " + varName + " incremented to " + newValue);
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError incrementing variable: " + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("decrementVariable", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String varName = params.get("variable").asString();
                double decrement = params.get("amount").asNumber().doubleValue();
                
                VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
                UUID playerId = context.getPlayer().getUniqueId();
                
                // Get current value
                DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
                double currentNumber = 0.0;
                
                if (currentValue != null && currentValue.getType().isNumber()) {
                    currentNumber = currentValue.asNumber().doubleValue();
                }
                
                // Set new value
                double newValue = currentNumber - decrement;
                variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
                
                context.getPlayer().sendMessage("§aVariable " + varName + " decremented to " + newValue);
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError decrementing variable: " + e.getMessage());
            }
        });
        
        // === ASYNC OPERATIONS ===
        ACTION_HANDLERS.put("delayedAction", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                int delay = params.get("delay").asNumber().intValue();
                String action = params.get("action").asString();
                
                // Schedule delayed execution
                context.getPlugin().getServer().getScheduler().runTaskLater(
                    context.getPlugin(), 
                    () -> {
                        // Execute the delayed action
                        BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(action);
                        if (handler != null) {
                            // Create a new context for the delayed execution
                            ExecutionContext newContext = new ExecutionContext(
                                context.getPlugin(),
                                context.getPlayer(),
                                context.getCreativeWorld(),
                                context.getEvent(),
                                context.getBlockLocation(),
                                context.getCurrentBlock()
                            );
                            handler.accept(newContext, params);
                        }
                    }, 
                    delay * 20L // Convert seconds to ticks
                );
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError scheduling delayed action: " + e.getMessage());
            }
        });
        
        // === LIST OPERATIONS ===
        ACTION_HANDLERS.put("createList", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String listName = params.get("name").asString();
                
                VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
                UUID playerId = context.getPlayer().getUniqueId();
                
                // Create empty list
                List<DataValue> emptyList = new ArrayList<>();
                variableManager.setPlayerVariable(playerId, listName, DataValue.of(emptyList));
                
                context.getPlayer().sendMessage("§aCreated list: " + listName);
            } catch (Exception e) {
                context.getPlayer().sendMessage(Constants.ERROR_GIVING_ITEM + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("addToList", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String listName = params.get("list").asString();
                DataValue value = params.get("value");
                
                VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
                UUID playerId = context.getPlayer().getUniqueId();
                
                // Get current list
                DataValue currentListValue = variableManager.getPlayerVariable(playerId, listName);
                List<DataValue> list = new ArrayList<>();
                
                if (currentListValue != null) {
                    // Try to convert to list
                    Object rawValue = currentListValue.getValue();
                    if (rawValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<DataValue> existingList = (List<DataValue>) rawValue;
                        list.addAll(existingList);
                    }
                }
                
                // Add new value
                list.add(value);
                variableManager.setPlayerVariable(playerId, listName, DataValue.of(list));
                
                context.getPlayer().sendMessage("§aAdded item to list: " + listName);
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError adding to list: " + e.getMessage());
            }
        });
        
        ACTION_HANDLERS.put("removeFromList", (context, params) -> {
            if (context.getPlayer() == null) return;
            
            try {
                String listName = params.get("list").asString();
                DataValue value = params.get("value");
                
                VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
                UUID playerId = context.getPlayer().getUniqueId();
                
                // Get current list
                DataValue currentListValue = variableManager.getPlayerVariable(playerId, listName);
                
                if (currentListValue != null) {
                    // Try to convert to list
                    Object rawValue = currentListValue.getValue();
                    if (rawValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<DataValue> list = (List<DataValue>) rawValue;
                        list.remove(value);
                        variableManager.setPlayerVariable(playerId, listName, DataValue.of(list));
                        context.getPlayer().sendMessage("§aRemoved item from list: " + listName);
                    }
                }
            } catch (Exception e) {
                context.getPlayer().sendMessage("§cError removing from list: " + e.getMessage());
            }
        });
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
}