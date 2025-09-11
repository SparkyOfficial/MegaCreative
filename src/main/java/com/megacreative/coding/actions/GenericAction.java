package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
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
            String actionId = block.getAction();
            Map<String, DataValue> params = block.getParameters();
            
            // Get the action handler
            BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(actionId);
            if (handler != null) {
                handler.accept(context, params);
                return ExecutionResult.success();
            } else {
                context.getPlugin().getLogger().warning("Unknown generic action: " + actionId);
                return ExecutionResult.error("Unknown action: " + actionId);
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error executing generic action " + block.getAction() + ": " + e.getMessage());
            e.printStackTrace();
            return ExecutionResult.error("Action execution error: " + e.getMessage());
        }
    }
    
    /**
     * Initialize all action handlers
     * Add new actions here instead of creating new classes
     */
    private static void initializeActionHandlers() {
        // === PLAYER ACTIONS ===
        ACTION_HANDLERS.put("sendMessage", (context, params) -> {
            String message = params.get("message").asString();
            context.getPlayer().sendMessage(message);
        });
        
        ACTION_HANDLERS.put("sendTitle", (context, params) -> {
            String title = params.get("title").asString();
            String subtitle = params.get("subtitle").asString();
            context.getPlayer().sendTitle(title, subtitle, 10, 70, 20);
        });
        
        ACTION_HANDLERS.put("sendActionBar", (context, params) -> {
            String message = params.get("message").asString();
            context.getPlayer().sendActionBar(message);
        });
        
        ACTION_HANDLERS.put("teleport", (context, params) -> {
            // Location loc = params.get("location").asLocation(); // Need to implement location handling
            String locString = params.get("location").asString();
            // Parse location string and teleport - simplified for now
            context.getPlayer().sendMessage("§aTeleport to: " + locString + " (Location parsing needed)");
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
        
        // === WORLD ACTIONS ===
        ACTION_HANDLERS.put("setBlock", (context, params) -> {
            // Location loc = params.get("location").asLocation(); // Simplified
            Material material = Material.valueOf(params.get("material").asString());
            context.getPlayer().sendMessage("§aSet block to: " + material + " (Location handling needed)");
        });
        
        ACTION_HANDLERS.put("breakBlock", (context, params) -> {
            // Location loc = params.get("location").asLocation(); // Simplified
            context.getPlayer().sendMessage("§aBreak block (Location handling needed)");
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
            }
        });
        
        // === ECONOMY ACTIONS (if Vault is available) ===
        ACTION_HANDLERS.put("giveMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Implementation would depend on economy plugin integration
            context.getPlayer().sendMessage("§a+$" + amount + " (Economy integration needed)");
        });
        
        ACTION_HANDLERS.put("takeMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Implementation would depend on economy plugin integration
            context.getPlayer().sendMessage("§c-$" + amount + " (Economy integration needed)");
        });
        
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