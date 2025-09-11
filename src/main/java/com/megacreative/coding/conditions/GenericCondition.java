package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.HashMap;

/**
 * Universal condition handler that can process 90% of all simple conditions
 * Instead of creating separate classes for each condition, we use a parameterized approach
 */
public class GenericCondition implements BlockCondition {
    
    // Condition handlers map for easy extension
    private static final Map<String, BiFunction<ExecutionContext, Map<String, DataValue>, Boolean>> CONDITION_HANDLERS = new HashMap<>();
    
    static {
        initializeConditionHandlers();
    }
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            String conditionId = block.getAction();
            Map<String, DataValue> params = block.getParameters();
            
            // Check for negation first
            boolean isNegated = params.containsKey("negated") && params.get("negated").asBoolean();
            
            // Get the condition handler
            BiFunction<ExecutionContext, Map<String, DataValue>, Boolean> handler = CONDITION_HANDLERS.get(conditionId);
            boolean result = false;
            
            if (handler != null) {
                result = handler.apply(context, params);
            } else {
                context.getPlugin().getLogger().warning("Unknown generic condition: " + conditionId);
                return false;
            }
            
            // Apply negation if needed
            return isNegated ? !result : result;
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating generic condition " + block.getAction() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initialize all condition handlers
     * Add new conditions here instead of creating new classes
     */
    private static void initializeConditionHandlers() {
        // === PLAYER STATE CONDITIONS ===
        CONDITION_HANDLERS.put("isSneaking", (context, params) -> {
            return context.getPlayer().isSneaking();
        });
        
        CONDITION_HANDLERS.put("isSprinting", (context, params) -> {
            return context.getPlayer().isSprinting();
        });
        
        CONDITION_HANDLERS.put("isFlying", (context, params) -> {
            return context.getPlayer().isFlying();
        });
        
        CONDITION_HANDLERS.put("isOnGround", (context, params) -> {
            return context.getPlayer().isOnGround();
        });
        
        CONDITION_HANDLERS.put("isInWater", (context, params) -> {
            return context.getPlayer().isInWater();
        });
        
        CONDITION_HANDLERS.put("isBlocking", (context, params) -> {
            return context.getPlayer().isBlocking();
        });
        
        // === NEW ESSENTIAL CONDITIONS ===
        CONDITION_HANDLERS.put("isSwimming", (context, params) -> {
            return context.getPlayer().isSwimming();
        });
        
        CONDITION_HANDLERS.put("isBurning", (context, params) -> {
            return context.getPlayer().getFireTicks() > 0;
        });
        
        CONDITION_HANDLERS.put("canFly", (context, params) -> {
            return context.getPlayer().getAllowFlight();
        });
        
        CONDITION_HANDLERS.put("isHungry", (context, params) -> {
            return context.getPlayer().getFoodLevel() < 20;
        });
        
        CONDITION_HANDLERS.put("isHurt", (context, params) -> {
            return context.getPlayer().getHealth() < 20.0;
        });
        
        CONDITION_HANDLERS.put("isAlive", (context, params) -> {
            return !context.getPlayer().isDead();
        });
        
        CONDITION_HANDLERS.put("hasEmptySlot", (context, params) -> {
            return context.getPlayer().getInventory().firstEmpty() != -1;
        });
        
        CONDITION_HANDLERS.put("isLookingAt", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            org.bukkit.block.Block target = context.getPlayer().getTargetBlock(null, 5);
            return target != null && target.getType() == material;
        });
        
        CONDITION_HANDLERS.put("nearPlayer", (context, params) -> {
            String targetName = params.get("player").asString();
            double distance = params.containsKey("distance") ? params.get("distance").asNumber().doubleValue() : 5.0;
            
            org.bukkit.entity.Player targetPlayer = context.getPlayer().getServer().getPlayer(targetName);
            if (targetPlayer == null || !targetPlayer.isOnline()) return false;
            
            return context.getPlayer().getLocation().distance(targetPlayer.getLocation()) <= distance;
        });
        
        // === PERMISSION CONDITIONS ===
        CONDITION_HANDLERS.put("hasPermission", (context, params) -> {
            String permission = params.get("permission").asString();
            return context.getPlayer().hasPermission(permission);
        });
        
        CONDITION_HANDLERS.put("isOp", (context, params) -> {
            return context.getPlayer().isOp();
        });
        
        // === GAMEMODE CONDITIONS ===
        CONDITION_HANDLERS.put("isGameMode", (context, params) -> {
            String mode = params.get("gamemode").asString().toLowerCase();
            GameMode playerMode = context.getPlayer().getGameMode();
            
            switch (mode) {
                case "survival":
                    return playerMode == GameMode.SURVIVAL;
                case "creative":
                    return playerMode == GameMode.CREATIVE;
                case "adventure":
                    return playerMode == GameMode.ADVENTURE;
                case "spectator":
                    return playerMode == GameMode.SPECTATOR;
                default:
                    return false;
            }
        });
        
        // === HEALTH/FOOD CONDITIONS ===
        CONDITION_HANDLERS.put("healthEquals", (context, params) -> {
            double targetHealth = params.get("health").asNumber().doubleValue();
            return Math.abs(context.getPlayer().getHealth() - targetHealth) < 0.1;
        });
        
        CONDITION_HANDLERS.put("healthGreaterThan", (context, params) -> {
            double targetHealth = params.get("health").asNumber().doubleValue();
            return context.getPlayer().getHealth() > targetHealth;
        });
        
        CONDITION_HANDLERS.put("healthLessThan", (context, params) -> {
            double targetHealth = params.get("health").asNumber().doubleValue();
            return context.getPlayer().getHealth() < targetHealth;
        });
        
        CONDITION_HANDLERS.put("foodEquals", (context, params) -> {
            int targetFood = params.get("food").asNumber().intValue();
            return context.getPlayer().getFoodLevel() == targetFood;
        });
        
        CONDITION_HANDLERS.put("foodGreaterThan", (context, params) -> {
            int targetFood = params.get("food").asNumber().intValue();
            return context.getPlayer().getFoodLevel() > targetFood;
        });
        
        CONDITION_HANDLERS.put("foodLessThan", (context, params) -> {
            int targetFood = params.get("food").asNumber().intValue();
            return context.getPlayer().getFoodLevel() < targetFood;
        });
        
        // === INVENTORY CONDITIONS ===
        CONDITION_HANDLERS.put("hasItem", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            int amount = params.get("amount").asNumber().intValue();
            return context.getPlayer().getInventory().containsAtLeast(new ItemStack(material), amount);
        });
        
        CONDITION_HANDLERS.put("hasItemInHand", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            ItemStack mainHand = context.getPlayer().getInventory().getItemInMainHand();
            return mainHand.getType() == material;
        });
        
        CONDITION_HANDLERS.put("inventoryFull", (context, params) -> {
            return context.getPlayer().getInventory().firstEmpty() == -1;
        });
        
        CONDITION_HANDLERS.put("inventoryEmpty", (context, params) -> {
            for (ItemStack item : context.getPlayer().getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    return false;
                }
            }
            return true;
        });
        
        // === LOCATION CONDITIONS ===
        CONDITION_HANDLERS.put("atLocation", (context, params) -> {
            // Location targetLoc = params.get("location").asLocation(); // Simplified
            String locString = params.get("location").asString();
            double distance = params.containsKey("distance") ? params.get("distance").asNumber().doubleValue() : 2.0;
            
            // For now, just return true - proper location handling needed
            return true;
        });
        
        CONDITION_HANDLERS.put("inBiome", (context, params) -> {
            String biomeName = params.get("biome").asString();
            return context.getPlayer().getLocation().getBlock().getBiome().name().equalsIgnoreCase(biomeName);
        });
        
        CONDITION_HANDLERS.put("aboveY", (context, params) -> {
            int y = params.get("y").asNumber().intValue();
            return context.getPlayer().getLocation().getBlockY() > y;
        });
        
        CONDITION_HANDLERS.put("belowY", (context, params) -> {
            int y = params.get("y").asNumber().intValue();
            return context.getPlayer().getLocation().getBlockY() < y;
        });
        
        // === TIME/WEATHER CONDITIONS ===
        CONDITION_HANDLERS.put("isDay", (context, params) -> {
            long time = context.getPlayer().getWorld().getTime();
            return time >= 0 && time < 12300;
        });
        
        CONDITION_HANDLERS.put("isNight", (context, params) -> {
            long time = context.getPlayer().getWorld().getTime();
            return time >= 12300 && time < 23850;
        });
        
        CONDITION_HANDLERS.put("isRaining", (context, params) -> {
            return context.getPlayer().getWorld().hasStorm();
        });
        
        CONDITION_HANDLERS.put("isThundering", (context, params) -> {
            return context.getPlayer().getWorld().isThundering();
        });
        
        // === POTION EFFECT CONDITIONS ===
        CONDITION_HANDLERS.put("hasPotionEffect", (context, params) -> {
            String effectName = params.get("effect").asString();
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            return effectType != null && context.getPlayer().hasPotionEffect(effectType);
        });
        
        // === BLOCK CONDITIONS ===
        CONDITION_HANDLERS.put("blockAtLocation", (context, params) -> {
            // Location loc = params.get("location").asLocation(); // Simplified
            Material material = Material.valueOf(params.get("material").asString());
            // For now return true - proper location handling needed
            return true;
        });
        
        CONDITION_HANDLERS.put("standingOnBlock", (context, params) -> {
            Material material = Material.valueOf(params.get("material").asString());
            Location loc = context.getPlayer().getLocation().subtract(0, 1, 0);
            return loc.getBlock().getType() == material;
        });
        
        // === COMPARISON CONDITIONS ===
        CONDITION_HANDLERS.put("randomChance", (context, params) -> {
            double chance = params.get("chance").asNumber().doubleValue(); // 0.0 to 1.0
            return Math.random() < chance;
        });
        
        CONDITION_HANDLERS.put("playerCount", (context, params) -> {
            int targetCount = params.get("count").asNumber().intValue();
            String operator = params.get("operator").asString(); // "equals", "greater", "less"
            int actualCount = context.getPlayer().getServer().getOnlinePlayers().size();
            
            switch (operator.toLowerCase()) {
                case "equals":
                    return actualCount == targetCount;
                case "greater":
                    return actualCount > targetCount;
                case "less":
                    return actualCount < targetCount;
                default:
                    return false;
            }
        });
        
        // === ECONOMY CONDITIONS (if Vault is available) ===
        CONDITION_HANDLERS.put("hasMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Implementation would depend on economy plugin integration
            // For now, always return true as placeholder
            return true;
        });
    }
    
    /**
     * Helper method to add new condition handlers dynamically
     * Can be used by other plugins or modules to extend functionality
     */
    public static void registerConditionHandler(String conditionId, BiFunction<ExecutionContext, Map<String, DataValue>, Boolean> handler) {
        CONDITION_HANDLERS.put(conditionId, handler);
    }
    
    /**
     * Check if condition is supported
     */
    public static boolean isSupported(String conditionId) {
        return CONDITION_HANDLERS.containsKey(conditionId);
    }
}