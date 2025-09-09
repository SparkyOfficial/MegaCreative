package com.megacreative.coding;

import com.megacreative.coding.conditions.*;
import com.megacreative.coding.executors.ExecutionResult;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConditionFactory {

    private final Map<String, Supplier<BlockCondition>> conditionMap = new HashMap<>();

    public ConditionFactory() {
        registerAllConditions();
    }
    
    private void register(String conditionId, Supplier<BlockCondition> supplier) {
        conditionMap.put(conditionId, supplier);
    }
    
    private void registerAllConditions() {
        // --- ADVANCED CONDITION BLOCKS ---
        register("checkPlayerStats", CheckPlayerStatsCondition::new);
        register("checkWorldWeather", CheckWorldWeatherCondition::new);
        register("checkPlayerInventory", CheckPlayerInventoryCondition::new);
        register("checkServerOnline", CheckServerOnlineCondition::new);
        
        // --- INTEGRATION BLOCKS (Conditions) ---
        register("worldGuardRegionCheck", WorldGuardRegionCheckCondition::new);
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
            // For now, we'll register a generic condition placeholder
            // In a more advanced implementation, you might want to store display names separately
            register(conditionId, () -> new BlockCondition() {
                @Override
                public boolean evaluate(CodeBlock block, ExecutionContext context) {
                    return false; // Default to false for unimplemented conditions
                }
            });
        }
    }
    
    public int getConditionCount() {
        return conditionMap.size();
    }
}