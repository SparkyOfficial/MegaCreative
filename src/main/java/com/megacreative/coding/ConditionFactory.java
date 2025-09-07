package com.megacreative.coding;

import com.megacreative.coding.conditions.*;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Factory for creating BlockCondition instances dynamically based on configuration.
 * Replaces hardcoded condition registration with a flexible, configuration-driven approach.
 */
public class ConditionFactory {
    
    private static final Logger log = Logger.getLogger(ConditionFactory.class.getName());
    
    private final BlockConfigService blockConfigService;
    private final Map<String, Class<? extends BlockCondition>> conditionClassMap = new HashMap<>();
    
    public ConditionFactory(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
        initializeConditionClassMap();
    }
    
    /**
     * Initializes the map of condition names to their implementation classes
     */
    private void initializeConditionClassMap() {
        // Player conditions
        conditionClassMap.put("hasPermission", HasPermissionCondition.class);
        conditionClassMap.put("hasItem", HasItemCondition.class);
        conditionClassMap.put("isInWorld", IsInWorldCondition.class);
        conditionClassMap.put("compareVariable", CompareVariableCondition.class);
        
        // Variable conditions
        conditionClassMap.put("variableEquals", VariableEqualsCondition.class);
        
        // World conditions
        conditionClassMap.put("isInRegion", InRegionCondition.class);
        
        // Control flow conditions
        conditionClassMap.put("ifCondition", IfCondition.class);
        conditionClassMap.put("elseCondition", ElseCondition.class);
        
        log.info("Initialized ConditionFactory with " + conditionClassMap.size() + " condition types");
    }
    
    /**
     * Creates a BlockCondition instance for the specified condition name
     * @param conditionName The name of the condition to create
     * @param material The material associated with the block
     * @return A new BlockCondition instance, or null if not found
     */
    public BlockCondition createCondition(String conditionName, Material material) {
        if (conditionName == null) {
            return null;
        }
        
        // First try to get from the class map
        Class<? extends BlockCondition> conditionClass = conditionClassMap.get(conditionName);
        if (conditionClass != null) {
            try {
                // Try to create an instance with no parameters
                return conditionClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                log.warning("Failed to create condition instance for " + conditionName + ": " + e.getMessage());
                // Fall back to generic condition
            }
        }
        
        // If not found in class map, create a generic condition based on configuration
        var blockConfig = blockConfigService.getBlockConfig(conditionName);
        if (blockConfig != null) {
            return new GenericBlockCondition(blockConfig);
        }
        
        log.warning("No condition implementation found for: " + conditionName);
        return null;
    }
    
    /**
     * Registers a custom condition class
     * @param conditionName The name of the condition
     * @param conditionClass The implementation class
     */
    public void registerConditionClass(String conditionName, Class<? extends BlockCondition> conditionClass) {
        if (conditionName != null && conditionClass != null) {
            conditionClassMap.put(conditionName, conditionClass);
            log.info("Registered custom condition class for " + conditionName);
        }
    }
    
    /**
     * Gets the number of registered condition types
     * @return The number of registered condition types
     */
    public int getRegisteredConditionCount() {
        return conditionClassMap.size();
    }
}