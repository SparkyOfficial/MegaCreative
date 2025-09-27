package com.megacreative.interfaces;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.events.EventPublisher;

import java.util.Map;

/**
 * Interface for creating block conditions using annotation-based discovery
 */
public interface IConditionFactory extends EventPublisher {
    
    /**
     * Scans for annotated conditions and registers them
     */
    void registerAllConditions();
    
    /**
     * Creates a condition by ID
     * @param conditionId Condition ID
     * @return BlockCondition or null if not found
     */
    BlockCondition createCondition(String conditionId);
    
    /**
     * Gets the display name for a condition
     * 
     * @param conditionId The condition ID
     * @return The display name, or the condition ID if no display name is set
     */
    String getConditionDisplayName(String conditionId);
    
    /**
     * Gets all registered condition display names
     * 
     * @return A map of condition IDs to display names
     */
    Map<String, String> getConditionDisplayNames();
    
    /**
     * Gets the condition count
     * @return Number of registered conditions
     */
    int getConditionCount();
}