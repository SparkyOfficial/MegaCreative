package com.megacreative.coding;

import com.megacreative.coding.conditions.*;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConditionFactory {

    // Constants for condition types
    private static final String CONDITION_TYPE_EQUALS = "equals";
    private static final String CONDITION_TYPE_EQUAL = "equal";
    private static final String CONDITION_TYPE_GREATER = "greater";
    private static final String CONDITION_TYPE_GREATER_THAN = "greater_than";
    private static final String CONDITION_TYPE_LESS = "less";
    private static final String CONDITION_TYPE_LESS_THAN = "less_than";
    private static final String CONDITION_TYPE_CONTAINS = "contains";
    private static final String CONDITION_TYPE_NOT_EMPTY = "not_empty";
    private static final String CONDITION_TYPE_IS_TRUE = "is_true";
    private static final String CONDITION_TYPE_TRUE = "true";
    private static final String CONDITION_TYPE_IS_FALSE = "is_false";
    private static final String CONDITION_TYPE_FALSE = "false";

    private final Map<String, Supplier<BlockCondition>> conditionMap = new HashMap<>();

    public ConditionFactory() {
        registerAllConditions();
    }
    
    private void register(String conditionId, Supplier<BlockCondition> supplier) {
        conditionMap.put(conditionId, supplier);
    }
    
    private void registerAllConditions() {
        // --- BASIC PLAYER CONDITIONS ---
        register("isOp", IsOpCondition::new);
        register("hasPermission", HasPermissionCondition::new);
        register("isNearBlock", IsNearBlockCondition::new);
        register("mobNear", MobNearCondition::new);
        register("playerGameMode", PlayerGameModeCondition::new);
        register("playerHealth", PlayerHealthCondition::new);
        register("isInWorld", IsInWorldCondition::new);
        register("hasItem", HasItemCondition::new);
        register("ifVarEquals", IfVarEqualsCondition::new);
        
        // --- NEW BASIC PLAYER CONDITIONS ---
        register("compareVariable", CompareVariableCondition::new);
        register("worldTime", WorldTimeCondition::new);
        register("ifVarGreater", IfVarGreaterCondition::new);
        register("ifVarLess", IfVarLessCondition::new);
        register("isBlockType", IsBlockTypeCondition::new);
        register("isPlayerHolding", IsPlayerHoldingCondition::new);
        register("isNearEntity", IsNearEntityCondition::new);
        register("hasArmor", HasArmorCondition::new);
        register("isNight", IsNightCondition::new);
        register("isRiding", IsRidingCondition::new);
        
        // --- ADVANCED CONDITION BLOCKS ---
        register("checkPlayerStats", CheckPlayerStatsCondition::new);
        register("checkWorldWeather", CheckWorldWeatherCondition::new);
        register("checkPlayerInventory", CheckPlayerInventoryCondition::new);
        register("checkServerOnline", CheckServerOnlineCondition::new);
        
        // --- INTEGRATION BLOCKS (Conditions) ---
        register("worldGuardRegionCheck", WorldGuardRegionCheckCondition::new);
        
        // === GENERIC CONDITIONS - Mass Production System ===
        // Register all simple conditions that can be handled by GenericCondition
        registerGenericConditions();
    }
    
    /**
     * Register all simple conditions that can be handled by GenericCondition
     * This enables rapid addition of new functionality without creating new classes
     */
    private void registerGenericConditions() {
        // Player state conditions
        registerGeneric("isSneaking");
        registerGeneric("isSprinting");
        registerGeneric("isFlying");
        registerGeneric("isOnGround");
        registerGeneric("isInWater");
        registerGeneric("isBlocking");
        
        // Gamemode conditions
        registerGeneric("isGameMode");
        
        // Health/food conditions
        registerGeneric("healthEquals");
        registerGeneric("healthGreaterThan");
        registerGeneric("healthLessThan");
        registerGeneric("foodEquals");
        registerGeneric("foodGreaterThan");
        registerGeneric("foodLessThan");
        
        // Inventory conditions
        registerGeneric("hasItem");
        registerGeneric("hasItemInHand");
        registerGeneric("inventoryFull");
        registerGeneric("inventoryEmpty");
        
        // Location conditions
        registerGeneric("atLocation");
        registerGeneric("inBiome");
        registerGeneric("aboveY");
        registerGeneric("belowY");
        
        // Time/weather conditions
        registerGeneric("isDay");
        registerGeneric("isNight");
        registerGeneric("isRaining");
        registerGeneric("isThundering");
        
        // Potion effect conditions
        registerGeneric("hasPotionEffect");
        
        // Block conditions
        registerGeneric("blockAtLocation");
        registerGeneric("standingOnBlock");
        
        // Comparison conditions
        registerGeneric("randomChance");
        registerGeneric("playerCount");
        
        // Economy conditions
        registerGeneric("hasMoney");
    }
    
    /**
     * Helper method to register a generic condition
     */
    private void registerGeneric(String conditionId) {
        register(conditionId, GenericCondition::new);
    }

    public BlockCondition createCondition(String conditionId) {
        Supplier<BlockCondition> supplier = conditionMap.get(conditionId);
        if (supplier != null) {
            return supplier.get();
        }
        return null;
    }
    
    // Add missing methods with proper implementation
    public void registerCondition(String conditionId, String displayName) {
        // Register the condition if it's not already registered
        if (!conditionMap.containsKey(conditionId)) {
            // Register a generic condition that evaluates based on parameters
            register(conditionId, () -> new BlockCondition() {
                @Override
                public boolean evaluate(CodeBlock block, ExecutionContext context) {
                    try {
                        // Get condition parameters
                        DataValue conditionType = block.getParameter("type");
                        DataValue conditionValue = block.getParameter("value");
                        DataValue expectedValue = block.getParameter("expected");
                        
                        // If we have a specific evaluation type, use it
                        if (conditionType != null) {
                            switch (conditionType.asString().toLowerCase()) {
                                case CONDITION_TYPE_EQUALS:
                                case CONDITION_TYPE_EQUAL:
                                    return conditionValue != null && expectedValue != null && 
                                           conditionValue.asString().equals(expectedValue.asString());
                                case CONDITION_TYPE_GREATER:
                                case CONDITION_TYPE_GREATER_THAN:
                                    if (conditionValue != null && expectedValue != null) {
                                        try {
                                            double val1 = Double.parseDouble(conditionValue.asString());
                                            double val2 = Double.parseDouble(expectedValue.asString());
                                            return val1 > val2;
                                        } catch (NumberFormatException e) {
                                            return false;
                                        }
                                    }
                                    break;
                                case CONDITION_TYPE_LESS:
                                case CONDITION_TYPE_LESS_THAN:
                                    if (conditionValue != null && expectedValue != null) {
                                        try {
                                            double val1 = Double.parseDouble(conditionValue.asString());
                                            double val2 = Double.parseDouble(expectedValue.asString());
                                            return val1 < val2;
                                        } catch (NumberFormatException e) {
                                            return false;
                                        }
                                    }
                                    break;
                                case CONDITION_TYPE_CONTAINS:
                                    if (conditionValue != null && expectedValue != null) {
                                        return conditionValue.asString().contains(expectedValue.asString());
                                    }
                                    break;
                                case CONDITION_TYPE_NOT_EMPTY:
                                    return conditionValue != null && !conditionValue.asString().isEmpty();
                                case CONDITION_TYPE_IS_TRUE:
                                case CONDITION_TYPE_TRUE:
                                    if (conditionValue != null) {
                                        return Boolean.parseBoolean(conditionValue.asString());
                                    }
                                    break;
                                case CONDITION_TYPE_IS_FALSE:
                                case CONDITION_TYPE_FALSE:
                                    if (conditionValue != null) {
                                        return !Boolean.parseBoolean(conditionValue.asString());
                                    }
                                    break;
                                default:
                                    // Default evaluation - check if condition value is truthy
                                    if (conditionValue != null) {
                                        String value = conditionValue.asString();
                                        return !value.isEmpty() && !"false".equalsIgnoreCase(value) && !"0".equals(value);
                                    }
                                    break;
                            }
                        }
                        
                        // Default evaluation - check if condition value is truthy
                        if (conditionValue != null) {
                            String value = conditionValue.asString();
                            return !value.isEmpty() && !"false".equalsIgnoreCase(value) && !"0".equals(value);
                        }
                        
                        // If no parameters, default to true to allow execution
                        return true;
                    } catch (Exception e) {
                        context.getPlugin().getLogger().warning("Error evaluating condition " + conditionId + ": " + e.getMessage());
                        // Default to false on error for safety
                        return false;
                    }
                }
            });
        }
    }
    
    public int getConditionCount() {
        return conditionMap.size();
    }
}